package com.ithinkrok.minigames.util.io;

import com.ithinkrok.minigames.GameState;
import com.ithinkrok.minigames.Kit;
import com.ithinkrok.minigames.command.CommandConfig;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.schematic.Schematic;
import com.ithinkrok.minigames.team.TeamIdentifier;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
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
        if (config.contains("shared_objects")) loadSharedObjects(config.getConfigurationSection("shared_objects"));

        if (config.contains("custom_items")) loadCustomItems(config.getConfigurationSection("custom_items"));
        if (config.contains("schematics")) loadSchematics(config.getConfigurationSection("schematics"));

        if (config.contains("kits")) loadKits(config.getConfigurationSection("kits"));
        if (config.contains("team_identifiers")) loadTeams(config.getConfigurationSection("team_identifiers"));
        if (config.contains("game_states")) loadGameStates(config.getConfigurationSection("game_states"));

        if (config.contains("listeners")) loadListeners(config.getConfigurationSection("listeners"));
        if(config.contains("commands")) loadCommands(config.getConfigurationSection("commands"));

        if (config.contains("additional_configs")) loadAdditionalConfigs(config.getStringList("additional_configs"));
    }

    private void loadCommands(ConfigurationSection config) {
        for(String name : config.getKeys(false)) {
            CommandConfig commandConfig = new CommandConfig(name, config.getConfigurationSection(name),
                    listenerCreator);

            holder.addCommand(commandConfig);
        }
    }

    private void loadGameStates(ConfigurationSection config) {
        for (String name : config.getKeys(false)) {
            ConfigurationSection gameStateConfig = config.getConfigurationSection(name);
            List<ConfigurationSection> listeners = new ArrayList<>();

            ConfigurationSection listenersConfig = gameStateConfig.getConfigurationSection("listeners");

            for (String listenerName : listenersConfig.getKeys(false)) {
                ConfigurationSection listenerConfig = listenersConfig.getConfigurationSection(listenerName);

                listeners.add(listenerConfig);
            }
            holder.addGameState(new GameState(name, listeners));
        }
    }

    private void loadTeams(ConfigurationSection config) {
        for (String name : config.getKeys(false)) {
            ConfigurationSection teamConfig = config.getConfigurationSection(name);

            DyeColor dyeColor = DyeColor.valueOf(teamConfig.getString("dye_color").toUpperCase());

            String formattedName = teamConfig.getString("formatted_name", null);

            String armorColorString = teamConfig.getString("armor_color", null);
            Color armorColor = null;
            if (armorColorString != null) {
                armorColor = Color.fromRGB(Integer.parseInt(armorColorString.replace("#", "")));
            }
            String chatColorString = teamConfig.getString("chat_color", null);
            ChatColor chatColor = null;
            if (chatColorString != null) {
                chatColor = ChatColor.valueOf(chatColorString);
            }

            holder.addTeamIdentifier(new TeamIdentifier(name, formattedName, dyeColor, armorColor, chatColor));
        }
    }

    private void loadKits(ConfigurationSection config) {
        for (String name : config.getKeys(false)) {
            ConfigurationSection kitConfig = config.getConfigurationSection(name);

            String formattedName = kitConfig.getString("formatted_name", null);

            ConfigurationSection listenersConfig = kitConfig.getConfigurationSection("listeners");

            Collection<ConfigurationSection> listeners = new ArrayList<>();

            for (String listenerName : listenersConfig.getKeys(false)) {
                ConfigurationSection listenerConfig = listenersConfig.getConfigurationSection(listenerName);

                listeners.add(listenerConfig);
            }

            holder.addKit(new Kit(name, formattedName, listeners));
        }
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
