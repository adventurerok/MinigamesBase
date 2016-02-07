package com.ithinkrok.minigames.base.map;

import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.GameState;
import com.ithinkrok.minigames.base.Kit;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.util.io.ConfigParser;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.minigames.base.item.CustomItem;
import com.ithinkrok.minigames.base.item.IdentifierMap;
import com.ithinkrok.minigames.base.lang.LanguageLookup;
import com.ithinkrok.minigames.base.lang.MultipleLanguageLookup;
import com.ithinkrok.minigames.base.schematic.PastedSchematic;
import com.ithinkrok.minigames.base.schematic.Schematic;
import com.ithinkrok.minigames.base.schematic.SchematicPaster;
import com.ithinkrok.minigames.base.task.GameTask;
import com.ithinkrok.minigames.base.task.TaskList;
import com.ithinkrok.minigames.base.team.TeamIdentifier;
import com.ithinkrok.minigames.base.util.BoundingBox;
import com.ithinkrok.minigames.base.util.io.ConfigHolder;
import com.ithinkrok.minigames.base.util.io.DirectoryUtils;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Weather;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 01/01/16.
 */
public class GameMap implements LanguageLookup, ConfigHolder, SchematicPaster.BoundsChecker {

    private final GameMapInfo gameMapInfo;
    private World world;
    private final MultipleLanguageLookup languageLookup = new MultipleLanguageLookup();
    private final List<CustomListener> listeners = new ArrayList<>();
    private final Map<String, CustomListener> listenerMap = new HashMap<>();
    private final Map<String, Schematic> schematicMap = new HashMap<>();

    private final TaskList mapTaskList = new TaskList();
    private final IdentifierMap<CustomItem> customItemIdentifierMap = new IdentifierMap<>();
    private final HashMap<String, ConfigurationSection> sharedObjects = new HashMap<>();

    private final List<PastedSchematic> pastedSchematics = new ArrayList<>();
    private Path ramdiskPath;

    public GameMap(GameGroup gameGroup, GameMapInfo gameMapInfo) {
        this.gameMapInfo = gameMapInfo;

        loadMap(gameGroup);
        ConfigParser
                .parseConfig(gameGroup, this, gameGroup, this, gameMapInfo.getConfigName(), gameMapInfo.getConfig());
    }

    private void loadMap(GameGroup gameGroup) {

        String randomWorldName = getRandomWorldName(gameMapInfo.getName());
        Path copyFrom = gameGroup.getGame().getMapDirectory().resolve(gameMapInfo.getMapFolder());

        boolean ramdisk = gameGroup.getGame().getRamdiskDirectory() != null;
        Path copyTo;

        if(ramdisk) {
            ramdiskPath = gameGroup.getGame().getRamdiskDirectory().resolve(randomWorldName);
            copyTo = ramdiskPath;
        } else {
            copyTo = Paths.get("./" + randomWorldName + "/");
        }

        try {
            DirectoryUtils.copy(copyFrom, copyTo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(ramdisk) {
            try {
                Path linkLocation = Paths.get("./" + randomWorldName + "/");
                Files.deleteIfExists(linkLocation);

                Files.createSymbolicLink(linkLocation, copyTo);
            } catch (IOException e) {
                System.out.println("Failed to create symbolic link for map: " + randomWorldName);
                e.printStackTrace();
            }
        }

        try{
            Files.deleteIfExists(copyTo.resolve("uid.dat"));
        } catch (IOException e) {
            System.out.println("Could not delete uid.dat for world. This could cause errors");
            e.printStackTrace();
        }

        WorldCreator creator = new WorldCreator(randomWorldName);

        creator.environment(gameMapInfo.getEnvironment());

        world = creator.createWorld();
        world.setAutoSave(false);

        configureWorld();
    }

    private void configureWorld() {
        ConfigurationSection config = gameMapInfo.getConfig();

        if(config.contains("start_time")) {
            world.setTime(config.getLong("start_time"));
        }

        if(config.contains("start_weather")) {
            switch(config.getString("start_weather").toLowerCase()) {
                case "clear":
                case "sun":
                    world.setStorm(false);
                    world.setThundering(false);
                case "rain":
                    world.setThundering(false);
                    world.setStorm(true);
                case "thunder":
                case "storm":
                    world.setStorm(true);
                    world.setThundering(true);
                default:
                    throw new RuntimeException("Invalid weather condition: " + config.getString("start_weather"));
            }
        }

        if(config.contains("enable_time")) {
            world.setGameRuleValue("doDaylightCycle", Boolean.toString(config.getBoolean("enable_time")));
        }

        if(config.contains("game_rules")) {
            ConfigurationSection gameRules = config.getConfigurationSection("game_rules");
            for(String rule : gameRules.getKeys(false)) {
                world.setGameRuleValue(rule, gameRules.getString("rule"));
            }
        }
    }

    private String getRandomWorldName(String mapName) {
        int count = 0;
        String randomWorldName;
        do {
            randomWorldName = mapName + "-" + String.format("%04X", count++);
        } while (Bukkit.getWorld(randomWorldName) != null);

        return randomWorldName;
    }

    public World getWorld() {
        return world;
    }

    public void addPastedSchematic(PastedSchematic schematic) {
        pastedSchematics.add(schematic);
    }

    public void removePastedSchematic(PastedSchematic schematic) {
        pastedSchematics.remove(schematic);
    }

    public GameMapInfo getInfo() {
        return gameMapInfo;
    }

    public CustomItem getCustomItem(String name) {
        return customItemIdentifierMap.get(name);
    }

    public CustomItem getCustomItem(int identifier) {
        return customItemIdentifierMap.get(identifier);
    }

    public void bindTaskToMap(GameTask task) {
        mapTaskList.addTask(task);
    }

    public void unloadMap() {
        mapTaskList.cancelAllTasks();

        List<PastedSchematic> pastedSchematics = new ArrayList<>(this.pastedSchematics);

        pastedSchematics.forEach(PastedSchematic::removed);

        if (world.getPlayers().size() != 0) System.out.println("There are still players in an unloading map!");

        for (Player player : world.getPlayers()) {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        boolean success = Bukkit.unloadWorld(world, false);

        if(ramdiskPath != null) {
            try {
                DirectoryUtils.delete(ramdiskPath);
            } catch (IOException e) {
                System.out.println("Failed to delete map on ramdisk.");
                e.printStackTrace();
            }
        }

        try {
            DirectoryUtils.delete(world.getWorldFolder().toPath());
        } catch (IOException e) {
            System.out.println("Failed to unload map. When bukkit tried to unload, it returned " + success);
            System.out.println("Please make sure there are no players in the map before deleting?");
            e.printStackTrace();
        }

    }

    public ConfigurationSection getSharedObject(String name) {
        return sharedObjects.get(name);
    }

    public void teleportUser(User user) {
        if (user.getLocation().getWorld().equals(world)) return;
        user.teleport(world.getSpawnLocation());
    }

    @Override
    public String getLocale(String name) {
        return languageLookup.getLocale(name);
    }

    @Override
    public String getLocale(String name, Object... args) {
        return languageLookup.getLocale(name, args);
    }

    @Override
    public boolean hasLocale(String name) {
        return languageLookup.hasLocale(name);
    }

    public List<CustomListener> getListeners() {
        return listeners;
    }

    @Override
    public void addListener(String name, CustomListener listener) {
        listeners.add(listener);
        listenerMap.put(name, listener);
    }

    public Map<String, CustomListener> getListenerMap() {
        return listenerMap;
    }

    @Override
    public void addCustomItem(CustomItem item) {
        customItemIdentifierMap.put(item.getName(), item);
    }

    @Override
    public void addLanguageLookup(LanguageLookup languageLookup) {
        this.languageLookup.addLanguageLookup(languageLookup);
    }

    @Override
    public void addSharedObject(String name, ConfigurationSection config) {
        sharedObjects.put(name, config);
    }

    @Override
    public void addSchematic(Schematic schematic) {
        schematicMap.put(schematic.getName(), schematic);
    }

    @Override
    public void addTeamIdentifier(TeamIdentifier teamIdentifier) {
        //TODO custom TeamIdentifier support for maps
    }

    @Override
    public void addGameState(GameState gameState) {
        //TODO custom GameState support for maps
    }

    @Override
    public void addKit(Kit kit) {
        //TODO custom Kit support for maps
    }

    @Override
    public void addCommand(CommandConfig command) {
        //TODO custom CommandConfig support for maps
    }

    @Override
    public void addMapInfo(GameMapInfo mapInfo) {
        //TODO custom GameMapInfo support for maps
    }

    public Schematic getSchematic(String name) {
        return schematicMap.get(name);
    }

    @Override
    public boolean canPaste(BoundingBox bounds) {
        for (PastedSchematic schematic : pastedSchematics) {
            if (!schematic.canPaste(bounds)) return false;
        }

        return true;
    }

    public Entity spawnEntity(Vector location, EntityType type) {
        return spawnEntity(getLocation(location), type);
    }

    public Entity spawnEntity(Location location, EntityType type) {
        return world.spawnEntity(location, type);
    }

    public Location getLocation(Vector location) {
        if (location == null) return null;
        return new Location(world, location.getX(), location.getY(), location.getZ());
    }

    public Location getLocation(double x, double y, double z) {
        return new Location(world, x, y, z);
    }

    public Block getBlock(Vector location) {
        return getLocation(location).getBlock();
    }

    public Block getBlock(int x, int y, int z) {
        return world.getBlockAt(x, y, z);
    }

    public Location getSpawn() {
        return world.getSpawnLocation();
    }
}
