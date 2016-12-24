package com.ithinkrok.minigames.base.util.io;

import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.entity.CustomEntity;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.schematic.Schematic;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.minigames.api.util.io.ListenerLoader;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.minigames.base.map.BaseMapInfo;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by paul on 04/01/16.
 */
public final class ConfigParser {

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
                                   Object listenerRepresenting, String name, Config config) {
        ConfigParser parser = new ConfigParser(loader, holder, listenerCreator, listenerRepresenting);

        parser.load(name, config);
    }

    private void load(String name, Config config) {
        if (loaded.contains(name)) return;

        loaded.add(name);

        if (config.contains("lang_files")) loadLangFiles(config.getStringList("lang_files"));
        if(config.contains("books")) loadBooks(config.getConfigOrNull("books"));
        if (config.contains("shared_objects")) loadSharedObjects(config.getConfigOrNull("shared_objects"));

        if (config.contains("custom_items")) loadCustomItems(config.getConfigOrNull("custom_items"));
        if (config.contains("custom_entities")) loadCustomEntities(config.getConfigOrNull("custom_entities"));
        if (config.contains("schematics")) loadSchematics(config.getConfigOrNull("schematics"));

        if (config.contains("kits")) loadKits(config.getConfigOrNull("kits"));
        if (config.contains("team_identifiers")) loadTeams(config.getConfigOrNull("team_identifiers"));
        if (config.contains("game_states")) loadGameStates(config.getConfigOrNull("game_states"));
        if (config.contains("maps")) loadMaps(config.getConfigOrNull("maps"));

        if (config.contains("listeners")) loadListeners(config.getConfigOrNull("listeners"));
        if (config.contains("commands")) loadCommands(config.getConfigOrNull("commands"));

        if (config.contains("additional_configs")) loadAdditionalConfigs(config.getStringList("additional_configs"));
    }

    private void loadBooks(Config books) {
        for(String name : books.getKeys(false)) {
            String bookPath = books.getString(name);

            holder.addBook(loader.loadBook(name, bookPath));
        }
    }

    private void loadMaps(Config maps) {
        for (String name : maps.getKeys(false)) {
            holder.addMapInfo(new BaseMapInfo(loader, name, maps.getString(name)));
        }
    }

    private void loadCommands(Config config) {
        for (String name : config.getKeys(false)) {
            CommandConfig commandConfig =
                    new CommandConfig(name, config.getConfigOrNull(name), listenerCreator);

            holder.addCommand(commandConfig);
        }
    }

    private void loadGameStates(Config config) {
        for (String name : config.getKeys(false)) {
            Config gameStateConfig = config.getConfigOrNull(name);
            List<Config> listeners = new ArrayList<>();

            Config listenersConfig = gameStateConfig.getConfigOrNull("listeners");

            for (String listenerName : listenersConfig.getKeys(false)) {
                Config listenerConfig = listenersConfig.getConfigOrNull(listenerName);

                listeners.add(listenerConfig);
            }
            holder.addGameState(new GameState(name, listeners));
        }
    }

    private void loadTeams(Config config) {
        for (String name : config.getKeys(false)) {
            Config teamConfig = config.getConfigOrNull(name);

            DyeColor dyeColor = DyeColor.valueOf(teamConfig.getString("dye_color").toUpperCase());

            String formattedName = teamConfig.getString("formatted_name", null);

            String armorColorString = teamConfig.getString("armor_color", null);
            Color armorColor = null;
            if (armorColorString != null) {
                armorColor = Color.fromRGB(Integer.parseInt(armorColorString.replace("#", ""), 16));
            }
            String chatColorString = teamConfig.getString("chat_color", null);
            ChatColor chatColor = null;
            if (chatColorString != null) {
                chatColor = ChatColor.valueOf(chatColorString);
            }

            holder.addTeamIdentifier(new TeamIdentifier(name, formattedName, dyeColor, armorColor, chatColor));
        }
    }

    private void loadKits(Config config) {
        for (String name : config.getKeys(false)) {
            Config kitConfig = config.getConfigOrNull(name);

            String formattedName = kitConfig.getString("formatted_name", null);

            String description = kitConfig.getString("description", "No description");

            ItemStack kitItem;
            if(kitConfig.contains("item")){
                kitItem = MinigamesConfigs.getItemStack(kitConfig, "item");
            } else {
                kitItem = null;
            }

            Config listenersConfig = kitConfig.getConfigOrNull("listeners");

            Collection<Config> listeners = new ArrayList<>();

            for (String listenerName : listenersConfig.getKeys(false)) {
                Config listenerConfig = listenersConfig.getConfigOrNull(listenerName);

                listeners.add(listenerConfig);
            }

            holder.addKit(new Kit(name, formattedName, description, kitItem, listeners));
        }
    }

    private void loadSchematics(Config config) {
        for (String name : config.getKeys(false)) {
            Config schemConfig = config.getConfigOrNull(name);
            Schematic schem = new Schematic(name, loader.getAssetDirectory(), schemConfig);

            holder.addSchematic(schem);
        }
    }

    private void loadSharedObjects(Config config) {
        for (String name : config.getKeys(false)) {
            holder.addSharedObject(name, config.getConfigOrNull(name));
        }
    }

    private void loadListeners(Config config) {
        for (String name : config.getKeys(false)) {
            Config listenerConfig = config.getConfigOrNull(name);

            try {
                CustomListener listener =
                        ListenerLoader.loadListener(listenerCreator, listenerRepresenting, listenerConfig);
                holder.addListener(name, listener);
            } catch (Exception e) {
                System.out.println("Failed to load listener: " + name);
                e.printStackTrace();
            }
        }
    }

    private void loadCustomItems(Config config) {
        for (String name : config.getKeys(false)) {
            Config itemConfig = config.getConfigOrNull(name);

            CustomItem item = new CustomItem(name, itemConfig);
            holder.addCustomItem(item);
        }
    }

    private void loadCustomEntities(Config config) {
        for (String name : config.getKeys(false)) {
            Config entityConfig = config.getConfigOrNull(name);

            CustomEntity entity = new CustomEntity(name, entityConfig);
            holder.addCustomEntity(entity);
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
