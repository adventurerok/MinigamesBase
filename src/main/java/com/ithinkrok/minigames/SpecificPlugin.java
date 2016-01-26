package com.ithinkrok.minigames;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by paul on 17/01/16.
 */
public class SpecificPlugin extends JavaPlugin {


    @Override
    public void onEnable() {
        ConfigurationSection config = getConfig();

        ConfigurationSection gameGroupConfigs = config.getConfigurationSection("gamegroup_configs");

        Game game = BasePlugin.getGame();

        for(String name : gameGroupConfigs.getKeys(false)) {
            game.registerGameGroupConfig(name, gameGroupConfigs.getString(name));
        }
    }
}
