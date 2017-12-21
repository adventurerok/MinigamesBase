package com.ithinkrok.minigames.api;

import com.ithinkrok.minigames.api.database.DatabaseObject;
import com.ithinkrok.minigames.base.BasePlugin;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.InvalidConfigException;
import com.ithinkrok.util.config.MemoryConfig;
import com.ithinkrok.util.config.YamlConfigIO;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by paul on 17/01/16.
 */
public class SpecificPlugin extends JavaPlugin {


    @Override
    public void onEnable() {
        boolean success = loadFromInternalConfig();
        success |= loadFromExternalConfig();

        if(!success) {
            getLogger().warning("No gamegroup configs were loaded! This is odd.");
        }
    }

    private boolean loadFromExternalConfig() {
        Config config;

        try {
            config = YamlConfigIO.loadToConfig(getDataFolder().toPath().resolve("config.yml"), new MemoryConfig());
        } catch (IOException ignored) {
            return false;
        }

        return loadGameGroupConfigs(config);
    }

    private boolean loadFromInternalConfig() {
        Config config;
        try {
            config = YamlConfigIO.loadToConfig(getResource("gamegroups.yml"), new MemoryConfig());
        } catch (InvalidConfigException ignored) {
            return false;
        }

        return loadGameGroupConfigs(config);
    }

    private boolean loadGameGroupConfigs(Config config) {
        Config gameGroupConfigs = config.getConfigOrEmpty("gamegroup_configs");

        Game game = BasePlugin.getGame();

        boolean added = false;

        for(String name : gameGroupConfigs.getKeys(false)) {
            game.registerGameGroupConfig(name, gameGroupConfigs.getString(name));
            added = true;
        }

        return added;
    }

    public Collection<Class<? extends DatabaseObject>> getDatabaseClasses() {
        return Collections.emptyList();
    }
}
