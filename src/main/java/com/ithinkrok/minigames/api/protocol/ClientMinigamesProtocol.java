package com.ithinkrok.minigames.api.protocol;

import com.ithinkrok.minigames.api.Game;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.msm.client.Client;
import com.ithinkrok.msm.client.ClientListener;
import com.ithinkrok.msm.common.Channel;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;

/**
 * Created by paul on 14/02/16.
 */
public class ClientMinigamesProtocol implements ClientListener {

    private final Game game;
    private final boolean primary;

    private Client client;
    private Channel channel;

    public ClientMinigamesProtocol(Game game, boolean primary) {
        this.game = game;
        this.primary = primary;
    }

    @Override
    public void connectionOpened(Client client, Channel channel) {
        this.client = client;
        this.channel = channel;

        sendLoginPacket(channel);
        if(primary) sendDataInfoPacket(channel);
    }

    private void sendDataInfoPacket(Channel channel) {
        Config versions = new MemoryConfig('\n');

        addVersionsToConfig(game.getAssetDirectory(), "assets/", versions);
        addVersionsToConfig(game.getConfigDirectory(), "config/", versions);
        addVersionsToConfig(game.getMapDirectory(), "maps/", versions);

        Config payload = new MemoryConfig('\n');

        payload.set("versions", versions);
        payload.set("mode", "DataInfo");

        channel.write(payload);
    }

    private void addVersionsToConfig(Path path, String prefix, Config config) {
        for(Map.Entry<String, Instant> entry : getVersionsInPath(path).entrySet()) {
            config.set(prefix + entry.getKey(), entry.getValue().toEpochMilli());
        }
    }

    private Map<String, Instant> getVersionsInPath(Path path) {
        Map<String, Instant> result = new HashMap<>();

        Set<FileVisitOption> options = new HashSet<>();
        options.add(FileVisitOption.FOLLOW_LINKS);

        try {
            Files.walkFileTree(path, options, Integer.MAX_VALUE, new SimpleFileVisitor<Path>(){

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Instant dateModified = Files.getLastModifiedTime(file).toInstant();

                    result.put(path.relativize(file).toString(), dateModified);

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.out.println("Failed to walk file tree for path: " + path);
            e.printStackTrace();
        }

        return result;
    }

    private void sendLoginPacket(Channel channel) {
        Config payload = new MemoryConfig();

        payload.set("mode", "Login");

        payload.set("gamegroup_types", game.getAvailableGameGroupTypes());

        payload.set("primary", primary);

        Collection<? extends GameGroup> gameGroups = game.getGameGroups();
        List<Config> gameGroupConfigs = new ArrayList<>();

        for(GameGroup gameGroup : gameGroups) {
            gameGroupConfigs.add(gameGroup.toConfig());
        }

        payload.set("gamegroups", gameGroupConfigs);

        channel.write(payload);
    }

    public void sendGameGroupSpawnedPayload(GameGroup gameGroup) {
        if(channel == null) return;

        Config payload = gameGroup.toConfig();

        payload.set("mode", "GameGroupSpawned");

        channel.write(payload);
    }

    public void sendGameGroupUpdatePayload(GameGroup gameGroup) {
        if(channel == null) return;

        Config payload = gameGroup.toConfig();

        payload.set("mode", "GameGroupUpdate");

        channel.write(payload);
    }

    public void sendGameGroupKilledPayload(GameGroup gameGroup) {
        if(channel == null) return;

        Config payload = new MemoryConfig();

        payload.set("name", gameGroup.getName());
        payload.set("mode", "GameGroupKilled");

        channel.write(payload);
    }

    @Override
    public void connectionClosed(Client client) {
        this.client = null;
        this.channel = null;
    }

    @Override
    public void packetRecieved(Client client, Channel channel, Config payload) {
        String mode = payload.getString("mode");
        if(mode == null) return;

        switch(mode) {
            case "JoinGameGroup":
                handleJoinGameGroup(payload);
                return;
            case "DataUpdate":
                handleDataUpdate(payload);
        }
    }

    private void handleDataUpdate(Config payload) {
        String type = payload.getString("data_type");

        Path dataDir;

        switch(type) {
            case "config":
                dataDir = game.getConfigDirectory();
                break;
            case "map":
                dataDir = game.getMapDirectory();
                break;
            case "asset":
                dataDir = game.getAssetDirectory();
                break;
            default:
                return;
        }

        String dataSubpath = payload.getString("data_path");

        Path filePath = dataDir.resolve(dataSubpath);
        Path fileDirectory = filePath.getParent();

        if(!Files.exists(fileDirectory)) {
            try {
                Files.createDirectories(fileDirectory);
            } catch (IOException e) {
                System.out.println("Failed to create directory for data update : " + fileDirectory);
                e.printStackTrace();
                return;
            }
        }

        byte[] updateBytes = payload.getByteArray("bytes");

        boolean append = payload.getBoolean("append", false);

        try{
            if(append) Files.write(filePath, updateBytes, StandardOpenOption.APPEND);
            else Files.write(filePath, updateBytes);
        } catch (IOException e) {
            System.out.println("Error while saving minigames config data at:" + filePath);
            e.printStackTrace();
        }

        boolean finish = payload.getBoolean("finish", true);

        if(finish) {
            System.out.println("Updated minigames resource at: " + filePath);
        }
    }

    private void handleJoinGameGroup(Config payload) {
        String type = payload.getString("type");
        String name = payload.getString("name"); //This can be null
        List<String> params = payload.getStringList("params");

        UUID playerUUID = UUID.fromString(payload.getString("player"));

        game.preJoinGameGroup(playerUUID, type, name, params);

        if(client == null || Bukkit.getPlayer(playerUUID) != null) return;

        client.changePlayerServer(playerUUID, client.getMinecraftServerInfo().getName());
    }

    public Client getClient() {
        return client;
    }
}
