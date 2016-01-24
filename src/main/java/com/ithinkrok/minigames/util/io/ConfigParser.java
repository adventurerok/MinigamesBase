package com.ithinkrok.minigames.util.io;

import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.schematic.Schematic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 04/01/16.
 */
public class ConfigParser {

    private final FileLoader loader;
    private final ConfigHolder holder;
    private final Object listenerCreator;
    private final Object listenerRepresenting;
    private final List<String> loaded = new ArrayList<>();

    private ConfigParser(FileLoader loader, ConfigHolder holder, Object listenerCreator, Object listenerRepresenting) {
        this.loader = loader;
        this.holder = holder;
        this.listenerCreator = listenerCreator;
        this.listenerRepresenting = listenerRepresenting;
    }

    public static void parseConfig(FileLoader loader, ConfigHolder holder, Object listenerCreator,
                                   Object listenerRepresenting, String name, ConfigurationSection config) {
        ConfigParser parser = new ConfigParser(loader, holder, listenerCreator, listenerRepresenting);

        parser.load(name, config);
    }

    private void load(String name, ConfigurationSection config) {
        if (loaded.contains(name)) return;

        loaded.add(name);

        if (config.contains("lang_files")) loadLangFiles(config.getStringList("lang_files"));
        if (config.contains("custom_items")) loadCustomItems(config.getConfigurationSection("custom_items"));
        if (config.contains("listeners")) loadListeners(config.getConfigurationSection("listeners"));
        if (config.contains("schematics")) loadSchematics(config.getConfigurationSection("schematics"));
        if (config.contains("shared_objects")) loadSharedObjects(config.getConfigurationSection("shared_objects"));
        if (config.contains("additional_configs")) loadAdditionalConfigs(config.getStringList("additional_configs"));
    }

    private void loadSchematics(ConfigurationSection config) {
        for (String name : config.getKeys(false)) {
            ConfigurationSection schemConfig = config.getConfigurationSection(name);
            Schematic schem = new Schematic(name, loader.getDataFolder(), schemConfig);

            holder.addSchematic(schem);
        }
    }

    private void loadSharedObjects(ConfigurationSection config) {
        for (String name : config.getKeys(false)) {
            holder.addSharedObject(name, config.getConfigurationSection(name));
        }
    }

    private void loadListeners(ConfigurationSection config) {
        for (String name : config.getKeys(false)) {
            ConfigurationSection listenerConfig = config.getConfigurationSection(name);

            try {
                Listener listener = ListenerLoader.loadListener(listenerCreator, listenerRepresenting, listenerConfig);
                holder.addListener(name, listener);
            } catch (Exception e) {
                System.out.println("Failed to load listener: " + name);
                e.printStackTrace();
            }
        }
    }

    private void loadCustomItems(ConfigurationSection config) {
        for (String name : config.getKeys(false)) {
            ConfigurationSection itemConfig = config.getConfigurationSection(name);

            CustomItem item = new CustomItem(name, itemConfig);
            holder.addCustomItem(item);
        }
    }

    private void loadLangFiles(List<String> langFiles) {
        for (String file : langFiles) {
            holder.addLanguageLookup(loader.loadLangFile(file));
        }
    }

    private void loadAdditionalConfigs(List<String> configFiles) {
        for (String file : configFiles) {
            if (loaded.contains(file)) continue;
            load(file, loader.loadConfig(file));
        }
    }
}
