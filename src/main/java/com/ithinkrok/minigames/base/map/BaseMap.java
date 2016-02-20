package com.ithinkrok.minigames.base.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.map.GameMapInfo;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.base.BaseGameGroup;
import com.ithinkrok.minigames.base.util.io.ConfigHolder;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.item.IdentifierMap;
import com.ithinkrok.minigames.api.schematic.PastedSchematic;
import com.ithinkrok.minigames.api.schematic.Schematic;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.task.TaskList;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.util.BoundingBox;
import com.ithinkrok.minigames.api.util.JSONBook;
import com.ithinkrok.minigames.base.util.io.ConfigParser;
import com.ithinkrok.minigames.base.util.io.DirectoryUtils;
import com.ithinkrok.msm.common.util.ConfigUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;
import com.ithinkrok.util.lang.MultipleLanguageLookup;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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
public class BaseMap implements GameMap, ConfigHolder {

    private final GameMapInfo gameMapInfo;
    private final MultipleLanguageLookup languageLookup = new MultipleLanguageLookup();
    private final List<CustomListener> listeners = new ArrayList<>();
    private final Map<String, CustomListener> listenerMap = new HashMap<>();
    private final Map<String, Schematic> schematicMap = new HashMap<>();
    private final Map<String, JSONBook> bookMap = new HashMap<>();
    private final TaskList mapTaskList = new TaskList();
    private final IdentifierMap<CustomItem> customItemIdentifierMap = new IdentifierMap<>();
    private final HashMap<String, Config> sharedObjects = new HashMap<>();
    private final List<PastedSchematic> pastedSchematics = new ArrayList<>();
    private World world;
    private Path ramdiskPath;

    public BaseMap(BaseGameGroup gameGroup, GameMapInfo gameMapInfo) {
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

        if (ramdisk) {
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

        if (ramdisk) {
            try {
                Path linkLocation = Paths.get("./" + randomWorldName + "/");
                Files.deleteIfExists(linkLocation);

                Files.createSymbolicLink(linkLocation, copyTo);
            } catch (IOException e) {
                System.out.println("Failed to create symbolic link for map: " + randomWorldName);
                e.printStackTrace();
            }
        }

        try {
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

    private String getRandomWorldName(String mapName) {
        int count = 0;
        String randomWorldName;
        do {
            randomWorldName = mapName + "-" + String.format("%04X", count++);
        } while (Bukkit.getWorld(randomWorldName) != null);

        return randomWorldName;
    }

    private void configureWorld() {
        Config config = gameMapInfo.getConfig();

        if (config.contains("start_time")) {
            world.setTime(config.getLong("start_time"));
        }

        if (config.contains("start_weather")) {
            switch (config.getString("start_weather").toLowerCase()) {
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

        if (config.contains("enable_time")) {
            world.setGameRuleValue("doDaylightCycle", Boolean.toString(config.getBoolean("enable_time")));
        }

        if (config.contains("game_rules")) {
            Config gameRules = config.getConfigOrNull("game_rules");
            for (String rule : gameRules.getKeys(false)) {
                world.setGameRuleValue(rule, gameRules.getString("rule"));
            }
        }
    }

    @Override public World getWorld() {
        return world;
    }

    @Override public void addPastedSchematic(PastedSchematic schematic) {
        pastedSchematics.add(schematic);
    }

    @Override public void removePastedSchematic(PastedSchematic schematic) {
        pastedSchematics.remove(schematic);
    }

    @Override public GameMapInfo getInfo() {
        return gameMapInfo;
    }

    @Override public CustomItem getCustomItem(String name) {
        return customItemIdentifierMap.get(name);
    }

    @Override public CustomItem getCustomItem(int identifier) {
        return customItemIdentifierMap.get(identifier);
    }

    @Override public void bindTaskToMap(GameTask task) {
        mapTaskList.addTask(task);
    }

    @Override public void unloadMap() {
        mapTaskList.cancelAllTasks();

        List<PastedSchematic> pastedSchematics = new ArrayList<>(this.pastedSchematics);

        pastedSchematics.forEach(PastedSchematic::removed);

        if (!world.getPlayers().isEmpty()) System.out.println("There are still players in an unloading map!");

        for (Player player : world.getPlayers()) {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        boolean success = Bukkit.unloadWorld(world, false);

        if (ramdiskPath != null) {
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

    @Override
    public boolean hasSharedObject(String name) {
        return sharedObjects.containsKey(name);
    }

    @Override
    public Config getSharedObject(String name) {
        return sharedObjects.get(name);
    }

    @Override
    public Config getSharedObjectOrEmpty(String name) {
        Config sharedObject = getSharedObject(name);

        return sharedObject != null ? sharedObject : ConfigUtils.EMPTY_CONFIG;
    }

    @Override public void teleportUser(User user) {
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

    @Override public List<CustomListener> getListeners() {
        return listeners;
    }

    @Override
    public void addListener(String name, CustomListener listener) {
        listeners.add(listener);
        listenerMap.put(name, listener);
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
    public void addSharedObject(String name, Config config) {
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

    @Override
    public void addBook(JSONBook book) {
        bookMap.put(book.getName(), book);
    }

    @Override public Map<String, CustomListener> getListenerMap() {
        return listenerMap;
    }

    @Override public Schematic getSchematic(String name) {
        return schematicMap.get(name);
    }

    @Override public JSONBook getBook(String name) {
        return bookMap.get(name);
    }

    @Override
    public boolean canPaste(BoundingBox bounds) {
        for (PastedSchematic schematic : pastedSchematics) {
            if (!schematic.canPaste(bounds)) return false;
        }

        return true;
    }

    @Override public Entity spawnEntity(Vector location, EntityType type) {
        return spawnEntity(getLocation(location), type);
    }

    @Override public Entity spawnEntity(Location location, EntityType type) {
        return world.spawnEntity(location, type);
    }

    @Override public Location getLocation(Vector location) {
        if (location == null) return null;
        return new Location(world, location.getX(), location.getY(), location.getZ());
    }

    @Override public Location getLocation(double x, double y, double z) {
        return new Location(world, x, y, z);
    }

    @Override public Block getBlock(Vector location) {
        return getLocation(location).getBlock();
    }

    @Override public Block getBlock(int x, int y, int z) {
        return world.getBlockAt(x, y, z);
    }

    @Override public Location getSpawn() {
        return world.getSpawnLocation();
    }
}