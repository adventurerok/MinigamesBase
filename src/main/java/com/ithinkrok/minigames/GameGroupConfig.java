package com.ithinkrok.minigames;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 24/01/16.
 */
public class GameGroupConfig {

    private final String name;
    private final String configName;
    private final ConfigurationSection baseConfig;

    private final String startGameStateName;
    private final String startMapName;

    public GameGroupConfig(String name, String configName, ConfigurationSection baseConfig) {
        this.name = name;
        this.configName = configName;
        this.baseConfig = baseConfig;

        startGameStateName = baseConfig.getString("start_game_state");
        startMapName = baseConfig.getString("start_map");
    }

    public String getStartGameStateName() {
        return startGameStateName;
    }

    public String getStartMapName() {
        return startMapName;
    }

    public String getName() {
        return name;
    }

    public ConfigurationSection getBaseConfig() {
        return baseConfig;
    }

    public String getConfigName() {
        return configName;
    }

}
