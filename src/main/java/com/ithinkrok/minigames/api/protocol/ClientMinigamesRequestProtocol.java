package com.ithinkrok.minigames.api.protocol;

import com.ithinkrok.minigames.api.protocol.data.ControllerInfo;
import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import com.ithinkrok.minigames.api.protocol.event.GameGroupKilledEvent;
import com.ithinkrok.minigames.api.protocol.event.GameGroupSpawnedEvent;
import com.ithinkrok.minigames.api.protocol.event.GameGroupUpdateEvent;
import com.ithinkrok.msm.client.Client;
import com.ithinkrok.msm.client.ClientListener;
import com.ithinkrok.msm.common.Channel;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by paul on 15/02/16.
 */
public class ClientMinigamesRequestProtocol implements ClientListener {

    private final Plugin plugin;
    private Channel channel;

    private boolean gameGroupInfoEnabled = false;

    private final ControllerInfo controllerInfo = new ControllerInfo();

    public ClientMinigamesRequestProtocol(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connectionOpened(Client client, Channel channel) {
        this.channel = channel;

        if(gameGroupInfoEnabled) enableControllerInfo();
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
                handleEventGameGroupSpawned(payload);
                return;
            case "EventGameGroupUpdate":
                handleEventGameGroupUpdate(payload);
                return;
            case "EventGameGroupKilled":
                handleEventGameGroupKilled(payload);
        }
    }

    private void handleEventGameGroupSpawned(Config payload) {
        GameGroupInfo gameGroup = controllerInfo.updateGameGroup(payload);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            GameGroupSpawnedEvent event = new GameGroupSpawnedEvent(gameGroup);
            plugin.getServer().getPluginManager().callEvent(event);
        });
    }

    public ControllerInfo getControllerInfo() {
        return controllerInfo;
    }

    private void handleEventGameGroupKilled(Config payload) {
        GameGroupInfo gameGroup = controllerInfo.removeGameGroup(payload.getString("name"));

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            GameGroupKilledEvent event = new GameGroupKilledEvent(gameGroup);
            plugin.getServer().getPluginManager().callEvent(event);
        });
    }

    private void handleEventGameGroupUpdate(Config payload) {
        GameGroupInfo gameGroup = controllerInfo.updateGameGroup(payload);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            GameGroupUpdateEvent event = new GameGroupUpdateEvent(gameGroup);
            plugin.getServer().getPluginManager().callEvent(event);
        });
    }

    private void handleResponseGameGroupInfo(Config payload) {
        List<Config> response = payload.getConfigList("response");

        controllerInfo.clearGameGroups();

        for(Config gameGroupConfig : response) {
            controllerInfo.updateGameGroup(gameGroupConfig);
        }
    }

    public void sendJoinGameGroupPacket(UUID playerUUID, String type, String name, List<String> params) {
        if(channel == null) return;
        Config payload = new MemoryConfig();

        payload.set("name", name);
        payload.set("type", type);
        payload.set("params", params);

        payload.set("player", playerUUID.toString());

        payload.set("mode", "JoinGameGroup");

        channel.write(payload);
    }

    public void enableControllerInfo() {
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
