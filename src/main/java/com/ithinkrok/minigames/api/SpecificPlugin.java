package com.ithinkrok.minigames.api;

import com.ithinkrok.minigames.base.BasePlugin;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.InvalidConfigException;
import com.ithinkrok.util.config.MemoryConfig;
import com.ithinkrok.util.config.YamlConfigIO;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by paul on 17/01/16.
 */
public class SpecificPlugin extends JavaPlugin {


    @Override
    public void onEnable() {
        Config config;
        try {
            config = YamlConfigIO.loadToConfig(getResource("gamegroups.yml"), new MemoryConfig());
        } catch (InvalidConfigException e) {
            System.out.println("Failed to load gamegroup configs config");
            e.printStackTrace();
            return;
        }

        Config gameGroupConfigs = config.getConfigOrEmpty("gamegroup_configs");

        Game game = BasePlugin.getGame();

        for(String name : gameGroupConfigs.getKeys(false)) {
            game.registerGameGroupConfig(name, gameGroupConfigs.getString(name));
        }
    }
}
