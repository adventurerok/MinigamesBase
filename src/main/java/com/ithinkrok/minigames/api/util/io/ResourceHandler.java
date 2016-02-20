package com.ithinkrok.minigames.api.util.io;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by paul on 20/11/15.
 *
 * Handles plugin resource files
 */
public class ResourceHandler {

    /**
     *
     * @param plugin The plugin to load resource for
     * @param name The name of the resource in the plugin's data folder
     * @return The File representing the resource, or {@code null} if the file does not exist
     */
    public static File getResource(Plugin plugin, String name){
        File file = new File(plugin.getDataFolder(), name);
        if(!file.exists()){
            try {
                plugin.saveResource(name, false);
            } catch(IllegalArgumentException e){
                return null;
            }
        }
        return file;
    }

    /**
     *
     * @param plugin The plugin to load the properties file for
     * @param name The name of the properties file in the plugin's data folder
     * @return The Properties file, or an empty Properties file if it does not exist
     */
    public static Properties getPropertiesResource(Plugin plugin, String name){
        File file = getResource(plugin, name);

        Properties properties = new Properties();
        if(file == null) return properties;

        try(FileInputStream in = new FileInputStream(file)){
            properties.load(in);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load properties file: " + name);
            e.printStackTrace();
        }

        return properties;
    }

    /**
     *
     * @param plugin The plugin to load the config file for
     * @param name The name of the config file in the plugin's data folder
     * @return A {@code YamlConfiguration} representing the config, or an empty one if the config does not exist
     */
    public static YamlConfiguration getConfigResource(Plugin plugin, String name){
        File file = getResource(plugin, name);
        if(file == null) return new YamlConfiguration();

        return YamlConfiguration.loadConfiguration(file);
    }
}
