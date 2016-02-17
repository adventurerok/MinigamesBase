package com.ithinkrok.minigames.base.protocol.data;

import com.ithinkrok.util.config.Config;

/**
 * Created by paul on 16/02/16.
 */
public class GameGroupInfo {

    private final ControllerInfo controller;

    private final String name;

    private final String type;

    private boolean acceptingPlayers = false;

    private String motd = "A motd";

    private int playerCount = 0;

    private int maxPlayerCount = 40;

    public GameGroupInfo(ControllerInfo controller, Config config) {
        this.controller = controller;
        this.name = config.getString("name");
        this.type = config.getString("type");

        fromConfig(config);
    }

    public void fromConfig(Config config) {
        if(config.contains("accepting")) acceptingPlayers = config.getBoolean("accepting");

        if(config.contains("motd")) motd = config.getString("motd");

        if(config.contains("player_count")) playerCount = config.getInt("player_count");

        if(config.contains("max_players")) maxPlayerCount = config.getInt("max_players");
    }

    public ControllerInfo getController() {
        return controller;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isAcceptingPlayers() {
        return acceptingPlayers;
    }

    public String getMotd() {
        return motd;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }
}
