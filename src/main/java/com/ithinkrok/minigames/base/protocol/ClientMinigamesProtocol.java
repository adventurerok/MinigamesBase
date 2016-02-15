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

/**
 * Created by paul on 14/02/16.
 */
public class ClientMinigamesProtocol implements ClientListener {

    private final Game game;

    public ClientMinigamesProtocol(Game game) {
        this.game = game;
    }

    @Override
    public void connectionOpened(Client client, Channel channel) {

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

    @Override
    public void connectionClosed(Client client) {

    }

    @Override
    public void packetRecieved(Client client, Channel channel, Config payload) {

    }
}
