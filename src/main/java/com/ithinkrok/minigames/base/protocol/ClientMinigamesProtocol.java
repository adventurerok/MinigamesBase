package com.ithinkrok.minigames.base.protocol;

import com.ithinkrok.minigames.base.Game;
import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.msm.client.Client;
import com.ithinkrok.msm.client.ClientListener;
import com.ithinkrok.msm.common.Channel;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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

        Config payload = new MemoryConfig();

        payload.set("mode", "Login");

        payload.set("gamegroup_types", game.getAvailableGameGroupTypes());

        payload.set("primary", primary);

        Collection<GameGroup> gameGroups = game.getGameGroups();
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
        Path fileDirectory = dataDir.getParent();

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
    }

    private void handleJoinGameGroup(Config payload) {
        String type = payload.getString("type");
        String name = payload.getString("name"); //This can be null

        UUID playerUUID = UUID.fromString(payload.getString("player"));

        game.preJoinGameGroup(playerUUID, type, name);

        if(client == null || game.getUser(playerUUID) != null) return;

        client.changePlayerServer(playerUUID, client.getMinecraftServerInfo().getName());
    }
}
