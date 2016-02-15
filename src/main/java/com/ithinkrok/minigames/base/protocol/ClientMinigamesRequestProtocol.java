package com.ithinkrok.minigames.base.protocol;

import com.ithinkrok.msm.client.Client;
import com.ithinkrok.msm.client.ClientListener;
import com.ithinkrok.msm.common.Channel;
import com.ithinkrok.util.config.Config;

/**
 * Created by paul on 15/02/16.
 */
public class ClientMinigamesRequestProtocol implements ClientListener {
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
