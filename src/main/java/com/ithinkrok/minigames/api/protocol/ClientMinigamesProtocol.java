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
    }

    private void sendLoginPacket(Channel channel) {
        Config payload = new MemoryConfig();

        payload.set("mode", "Login");

        payload.set("gamegroup_types", game.getAvailableGameGroupTypes());

        payload.set("primary", primary);

        Collection<? extends GameGroup> gameGroups = game.getGameGroups();
        List<Config> gameGroupConfigs = new ArrayList<>();

        for (GameGroup gameGroup : gameGroups) {
            gameGroupConfigs.add(gameGroup.toConfig());
        }

        payload.set("gamegroups", gameGroupConfigs);

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
        if (mode == null) return;

        switch (mode) {
            case "JoinGameGroup":
                handleJoinGameGroup(payload);
        }
    }

    private void handleJoinGameGroup(Config payload) {
        String type = payload.getString("type");
        String name = payload.getString("name"); //This can be null
        List<String> params = payload.getStringList("params");

        UUID playerUUID = UUID.fromString(payload.getString("player"));

        game.preJoinGameGroup(playerUUID, type, name, params);

        if (client == null || Bukkit.getPlayer(playerUUID) != null) return;

        client.changePlayerServer(playerUUID, client.getMinecraftServerInfo().getName());
    }

    public void sendGameGroupSpawnedPayload(GameGroup gameGroup) {
        if (channel == null) return;

        Config payload = gameGroup.toConfig();

        payload.set("mode", "GameGroupSpawned");

        channel.write(payload);
    }

    public void sendGameGroupUpdatePayload(GameGroup gameGroup) {
        if (channel == null) return;

        Config payload = gameGroup.toConfig();

        payload.set("mode", "GameGroupUpdate");

        channel.write(payload);
    }

    public void sendGameGroupKilledPayload(GameGroup gameGroup) {
        if (channel == null) return;

        Config payload = new MemoryConfig();

        payload.set("name", gameGroup.getName());
        payload.set("mode", "GameGroupKilled");

        channel.write(payload);
    }

    public Client getClient() {
        return client;
    }
}
