package com.ithinkrok.minigames.base.map;

import com.ithinkrok.minigames.api.map.GameMapInfo;
import com.ithinkrok.minigames.api.map.MapType;
import com.ithinkrok.minigames.base.util.io.FileLoader;
import com.ithinkrok.util.config.Config;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 01/01/16.
 */
public class BaseMapInfo implements GameMapInfo {

    private final String name;
    private final String configPath;
    private final Config config;

    public BaseMapInfo(FileLoader fileLoader, String name, String configPath) {
        this.name = name;
        this.configPath = configPath;
        this.config = fileLoader.loadConfig(getConfigName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getConfigName() {
        return configPath;
    }

    @Override
    public String getDescription() {
        return config.getString("description");
    }

    @Override
    public boolean getWeatherEnabled() {
        return config.getBoolean("enable_weather", true);
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public String getMapFolder() {
        return config.getString("folder");
    }

    @Override
    public List<String> getCredit() {
        List<String> result = new ArrayList<>();

        for(int count = 0;; ++count) {
            String configString = "credit." + count;

            if(!config.contains(configString)) return result;

            result.add(config.getString(configString));
        }
    }

    @Override
    public World.Environment getEnvironment() {
        String envName = config.getString("environment", "normal").toUpperCase();

        return World.Environment.valueOf(envName);
    }

    @Override
    public MapType getMapType() {
        return MapType.valueOf(config.getString("type", "instance").toUpperCase());
    }

}
