package com.ithinkrok.minigames.base.protocol;

import com.ithinkrok.minigames.base.Game;
import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.msm.client.Client;
import com.ithinkrok.msm.client.ClientListener;
import com.ithinkrok.msm.common.Channel;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created by paul on 14/02/16.
 */
public class ClientMinigamesProtocol implements ClientListener {

    private final Game game;

    private Client client;
    private Channel channel;

    public ClientMinigamesProtocol(Game game) {
        this.game = game;
    }

    @Override
    public void connectionOpened(Client client, Channel channel) {
        this.client = client;
        this.channel = channel;

        Config payload = new MemoryConfig();

        payload.set("mode", "Login");

        payload.set("gamegroup_types", game.getAvailableGameGroupTypes());

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
        }
    }

    private void handleJoinGameGroup(Config payload) {
        String type = payload.getString("type");
        String name = payload.getString("name"); //This can be null

        UUID playerUUID = UUID.fromString(payload.getString("player"));

        game.preJoinGameGroup(playerUUID, type, name);

        if(client == null) return;

        client.changePlayerServer(playerUUID, client.getMinecraftServerInfo().getName());
    }
}
