package com.ithinkrok.minigames.base.map;

import com.ithinkrok.minigames.api.map.GameMapInfo;
import com.ithinkrok.minigames.api.map.MapType;
import com.ithinkrok.minigames.api.map.MapWorldInfo;
import com.ithinkrok.minigames.base.util.io.FileLoader;
import com.ithinkrok.util.StringUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 01/01/16.
 */
public class BaseMapInfo implements GameMapInfo {

    private final String name;
    private final String configPath;
    private final Config config;

    private final Map<String, MapWorldInfo> worlds = new HashMap<>();

    public BaseMapInfo(FileLoader fileLoader, String name, String configPath) {
        this.name = name;
        this.configPath = configPath;
        this.config = fileLoader.loadConfig(getConfigName());

        String defaultWorldName = config.getString("default_world", "map");

        if (config.contains("worlds")) {
            Config worldConfigs = config.getConfigOrEmpty("worlds");


            for (String worldName : worldConfigs.getKeys(false)) {
                boolean isDefault = defaultWorldName.equals(worldName);
                Config worldConfig = worldConfigs.getConfigOrNull(worldName);

                worlds.put(worldName, new MapWorldInfo(worldName, worldConfig, isDefault));
            }

        } else {
            worlds.put(defaultWorldName, new MapWorldInfo("map", config, true));
        }
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
    public Config getConfig() {
        return config;
    }

    @Override
    public List<String> getCredit() {
        List<String> result = new ArrayList<>();

        for (int count = 0; ; ++count) {
            String configString = "credit." + count;

            if (!config.contains(configString)) return result;

            String credit = StringUtils.convertAmpersandToSelectionCharacter(config.getString(configString));
            result.add(credit);
        }
    }

    @Override
    public Map<String, MapWorldInfo> getWorlds() {
        return worlds;
    }

    @Override
    public String getName() {
        return name;
    }
}
