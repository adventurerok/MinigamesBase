package com.ithinkrok.minigames.map;

import com.ithinkrok.minigames.util.io.FileLoader;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 01/01/16.
 */
public class GameMapInfo {

    private final String name;
    private final String configPath;
    private final ConfigurationSection config;

    public GameMapInfo(FileLoader fileLoader, String name, String configPath) {
        this.name = name;
        this.configPath = configPath;
        this.config = fileLoader.loadConfig(getConfigName());
    }

    public String getName() {
        return name;
    }

    public String getConfigName() {
        return configPath;
    }

    public String getDescription() {
        return config.getString("description");
    }

    public boolean getWeatherEnabled() {
        return config.getBoolean("enable_weather", true);
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public String getMapFolder() {
        return config.getString("folder");
    }

    public World.Environment getEnvironment() {
        String envName = config.getString("environment", "normal").toUpperCase();

        return World.Environment.valueOf(envName);
    }

}
