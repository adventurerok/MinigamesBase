package com.ithinkrok.minigames.base.protocol;

import com.ithinkrok.minigames.base.Game;
import com.ithinkrok.msm.client.Client;
import com.ithinkrok.msm.client.ClientListener;
import com.ithinkrok.msm.common.Channel;
import com.ithinkrok.util.config.Config;

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

    }

    @Override
    public void connectionClosed(Client client) {

    }

    @Override
    public void packetRecieved(Client client, Channel channel, Config payload) {

    }
}
