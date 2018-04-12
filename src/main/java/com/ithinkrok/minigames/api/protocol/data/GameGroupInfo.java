package com.ithinkrok.minigames.api.protocol.data;

import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.ConfigSerializable;
import com.ithinkrok.util.config.MemoryConfig;

import java.util.*;

/**
 * Created by paul on 16/02/16.
 */
public class GameGroupInfo implements ConfigSerializable {

    private final ControllerInfo controller;

    private final String name;
    private final String type;
    private final List<String> params;

    private boolean acceptingPlayers = false;

    private String motd = "A motd";

    private Set<UUID> players = new HashSet<>();

    private int maxPlayerCount = 40;

    public GameGroupInfo(ControllerInfo controller, Config config) {
        this.controller = controller;
        this.name = config.getString("name");
        this.type = config.getString("type");
        this.params = config.getStringList("params");

        fromConfig(config);
    }

    public GameGroupInfo(GameGroupInfo copy) {
        this.controller = copy.controller;
        this.name = copy.name;
        this.type = copy.type;
        this.params = new ArrayList<>(copy.params);

        this.acceptingPlayers = copy.acceptingPlayers;
        this.motd = copy.motd;
        this.players = new HashSet<>(copy.players);
        this.maxPlayerCount = copy.maxPlayerCount;
    }

    @SuppressWarnings("Duplicates")
    public void fromConfig(Config config) {
        if(config.contains("accepting")) acceptingPlayers = config.getBoolean("accepting");

        if(config.contains("motd")) motd = config.getString("motd");

        if(config.contains("players")){
            List<String> playerUUIDStrings = config.getStringList("players");

            Set<UUID> newPlayers = new HashSet<>();

            for (String uuidString : playerUUIDStrings) {
                newPlayers.add(UUID.fromString(uuidString));
            }

            players = newPlayers;
        }

        if(config.contains("max_players")) maxPlayerCount = config.getInt("max_players");
    }

    public List<String> getParams() {
        return params;
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
        return players.size();
    }


    public Set<UUID> getPlayers() {
        return new HashSet<>(players);
    }


    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    @Override
    public Config toConfig() {
        Config result = new MemoryConfig();

        result.set("name", name);
        result.set("type", type);
        result.set("params", params);

        result.set("accepting", acceptingPlayers);
        result.set("motd", motd);
        result.set("player_count", players.size());
        result.set("max_players", maxPlayerCount);

        List<String> playerUUIDStrings = new ArrayList<>();
        for (UUID playerUUID : players) {
            playerUUIDStrings.add(playerUUID.toString());
        }
        result.set("players", playerUUIDStrings);

        return result;
    }
}
