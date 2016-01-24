package com.ithinkrok.minigames.map;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.GameState;
import com.ithinkrok.minigames.Kit;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.item.IdentifierMap;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.lang.MultipleLanguageLookup;
import com.ithinkrok.minigames.schematic.PastedSchematic;
import com.ithinkrok.minigames.schematic.Schematic;
import com.ithinkrok.minigames.schematic.SchematicPaster;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.task.TaskList;
import com.ithinkrok.minigames.team.TeamIdentifier;
import com.ithinkrok.minigames.util.BoundingBox;
import com.ithinkrok.minigames.util.io.ConfigHolder;
import com.ithinkrok.minigames.util.io.ConfigParser;
import com.ithinkrok.minigames.util.io.DirectoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 01/01/16.
 */
public class GameMap implements LanguageLookup, ConfigHolder, SchematicPaster.BoundsChecker {

    private GameMapInfo gameMapInfo;
    private World world;
    private MultipleLanguageLookup languageLookup = new MultipleLanguageLookup();
    private List<Listener> listeners = new ArrayList<>();
    private Map<String, Listener> listenerMap = new HashMap<>();
    private Map<String, Schematic> schematicMap = new HashMap<>();

    private TaskList mapTaskList = new TaskList();
    private IdentifierMap<CustomItem> customItemIdentifierMap = new IdentifierMap<>();
    private HashMap<String, ConfigurationSection> sharedObjects = new HashMap<>();

    private List<PastedSchematic> pastedSchematics = new ArrayList<>();

    public GameMap(GameGroup gameGroup, GameMapInfo gameMapInfo) {
        this.gameMapInfo = gameMapInfo;

        loadMap();
        ConfigParser
                .parseConfig(gameGroup, this, gameGroup, this, gameMapInfo.getConfigName(), gameMapInfo.getConfig());
    }

    private void loadMap() {

        String randomWorldName = getRandomWorldName(gameMapInfo.getName());
        String copyFrom = "./" + gameMapInfo.getMapFolder() + "/";
        String copyTo = "./" + randomWorldName + "/";

        try {
            DirectoryUtils.copy(Paths.get(copyFrom), Paths.get(copyTo));
        } catch (IOException e) {
            e.printStackTrace();
        }

        File uid = new File(copyTo, "uid.dat");
        if (uid.exists()) {
            boolean deleted = uid.delete();
            if (!deleted) System.out.println("Could not delete uid.dat for world. This could cause errors");
        }

        WorldCreator creator = new WorldCreator(randomWorldName);

        creator.environment(gameMapInfo.getEnvironment());

        world = creator.createWorld();
        world.setAutoSave(false);

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

    public List<Listener> getListeners() {
        return listeners;
    }

    @Override
    public void addListener(String name, Listener listener) {
        listeners.add(listener);
        listenerMap.put(name, listener);
    }

    public Map<String, Listener> getListenerMap() {
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
