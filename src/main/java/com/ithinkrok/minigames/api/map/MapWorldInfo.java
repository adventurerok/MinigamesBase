package com.ithinkrok.minigames.api.map;

import com.ithinkrok.util.config.Config;
import org.bukkit.World;

public class MapWorldInfo {

    private final String name;
    private final Config config;

    public MapWorldInfo(String name, Config config) {
        this.name = name;
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public boolean isWeatherEnabled() {
        return config.getBoolean("enable_weather", true);
    }

    public World.Environment getEnvironment() {
        String envName = config.getString("environment", "normal").toUpperCase();

        return World.Environment.valueOf(envName);
    }

    public MapType getMapType() {
        return MapType.valueOf(config.getString("type", "instance").toUpperCase());
    }

    public Config getConfig() {
        return config;
    }

    public String getWorldFolder() {
        return config.getString("folder");
    }

}
