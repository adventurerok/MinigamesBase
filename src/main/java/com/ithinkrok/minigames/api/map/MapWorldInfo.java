package com.ithinkrok.minigames.api.map;

import com.ithinkrok.util.config.Config;
import org.bukkit.World;

public class MapWorldInfo {

    private final String name;
    private final Config config;
    private final boolean defaultWorld;

    public MapWorldInfo(String name, Config config, boolean defaultWorld) {
        this.name = name;
        this.config = config;
        this.defaultWorld = defaultWorld;
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


    /**
     *
     * Returns the bukkit chunk generator string.
     * null is interpreted to mean a void generator for instance worlds, and the default generator otherwise.
     *
     * @return The generator code to use for this world.
     */
    public String getGenerator() {
        return config.getString("generator", null);
    }

    public String getWorldFolder() {
        return config.getString("folder");
    }

    public String getNetherWorld() {
        String defaultNether = name.endsWith("_nether") ? name.substring(0, name.length() - 7) : name + "_nether";
        return config.getString("nether_world", defaultNether);
    }

    public double getNetherScale() {
        double defaultScale = name.endsWith("_nether") ? 8 : (1/8d);
        return config.getDouble("nether_scale", defaultScale);
    }

    public String getEndWorld() {
        String defaultEnd = name.endsWith("_the_end") ? name.substring(0, name.length() - 8) : name + "_the_end";
        return config.getString("end_world", defaultEnd);
    }

    /**
     * @return If we are the default world for the map
     */
    public boolean isDefaultWorld() {
        return defaultWorld;
    }
}
