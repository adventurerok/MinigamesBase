package com.ithinkrok.minigames.base.protocol;

import com.ithinkrok.minigames.base.protocol.data.ControllerInfo;
import com.ithinkrok.msm.client.Client;
import com.ithinkrok.msm.client.ClientListener;
import com.ithinkrok.msm.common.Channel;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Created by paul on 15/02/16.
 */
public class ClientMinigamesRequestProtocol implements ClientListener {

    private Channel channel;

    private boolean gameGroupInfoEnabled = false;

    private final ControllerInfo controllerInfo = new ControllerInfo();

    @Override
    public void connectionOpened(Client client, Channel channel) {
        this.channel = channel;

        if(gameGroupInfoEnabled) enableGameGroupInfo();
    }

    @Override
    public void connectionClosed(Client client) {
        this.channel = null;
    }

    @Override
    public void packetRecieved(Client client, Channel channel, Config payload) {
        String mode = payload.getString("mode");
        if(mode == null) return;

        switch(mode) {
            case "ResponseGameGroupInfo":
                handleResponseGameGroupInfo(payload);
                return;
            case "EventGameGroupSpawned":
            case "EventGameGroupUpdate":
                handleEventGameGroupUpdate(payload);
                return;
            case "EventGameGroupKilled":
                handleEventGameGroupKilled(payload);
        }
    }

    public ControllerInfo getControllerInfo() {
        return controllerInfo;
    }

    private void handleEventGameGroupKilled(Config payload) {
        controllerInfo.removeGameGroup(payload.getString("name"));
    }

    private void handleEventGameGroupUpdate(Config payload) {
        controllerInfo.updateGameGroup(payload);
    }

    private void handleResponseGameGroupInfo(Config payload) {
        List<Config> response = payload.getConfigList("response");

        controllerInfo.clearGameGroups();

        for(Config gameGroupConfig : response) {
            controllerInfo.updateGameGroup(gameGroupConfig);
        }
    }

    public void enableGameGroupInfo() {
        gameGroupInfoEnabled = true;

        if(channel == null) return;

        Config listenPayload = new MemoryConfig();
        listenPayload.set("mode", "Listen");

        listenPayload.set("listen_modes", Arrays.asList("GameGroupSpawned", "GameGroupKilled", "GameGroupUpdate"));
        channel.write(listenPayload);

        Config requestGameGroupInfoPayload = new MemoryConfig();
        requestGameGroupInfoPayload.set("mode", "RequestGameGroupInfo");
        channel.write(requestGameGroupInfoPayload);
    }
}
