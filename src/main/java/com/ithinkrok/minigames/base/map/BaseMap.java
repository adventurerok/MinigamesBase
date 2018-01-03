package com.ithinkrok.minigames.base.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.entity.CustomEntity;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.map.GameMapInfo;
import com.ithinkrok.minigames.api.map.MapPoint;
import com.ithinkrok.minigames.api.map.MapWorldInfo;
import com.ithinkrok.minigames.api.schematic.PastedSchematic;
import com.ithinkrok.minigames.api.schematic.Schematic;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.task.TaskList;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.BoundingBox;
import com.ithinkrok.minigames.api.util.JSONBook;
import com.ithinkrok.minigames.base.BaseGameGroup;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.minigames.base.util.io.ConfigHolder;
import com.ithinkrok.minigames.base.util.io.ConfigParser;
import com.ithinkrok.msm.bukkit.util.BukkitConfigUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.ConfigUtils;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;
import com.ithinkrok.util.lang.MultipleLanguageLookup;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.*;

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
    private final Map<String, CustomItem> customItemIdentifierMap = new HashMap<>();
    private final Map<String, CustomEntity> customEntityMap = new HashMap<>();
    private final HashMap<String, Config> sharedObjects = new HashMap<>();
    private final List<PastedSchematic> pastedSchematics = new ArrayList<>();


    private final Map<String, Config> currencyConfigs = new HashMap<>();

    private final Map<String, World> worlds = new HashMap<>();
    private final Map<String, String> reverseWorldNames = new HashMap<>();
    private final Map<String, WorldHandler> worldHandlers = new HashMap<>();

    private String defaultWorldName = "map";

    private Location spawn;


    public BaseMap(BaseGameGroup gameGroup, GameMapInfo gameMapInfo) {
        this.gameMapInfo = gameMapInfo;

        loadMapWorlds(gameGroup);

        ConfigParser
                .parseConfig(gameGroup, this, gameGroup, this, gameMapInfo.getConfigName(), gameMapInfo.getConfig());

    }


    private void loadMapWorlds(GameGroup gameGroup) {
        for (MapWorldInfo worldInfo : getInfo().getWorlds().values()) {

            WorldHandler worldHandler;

            switch (worldInfo.getMapType()) {
                case INSTANCE:
                    worldHandler = new InstanceWorldHandler();
                    break;
                case SAVED:
                    worldHandler = new SavedWorldHandler();
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported map type: " + worldInfo.getMapType());
            }

            worldHandlers.put(worldInfo.getName(), worldHandler);

            World world = worldHandler.loadWorld(gameGroup, this, worldInfo);
            worlds.put(worldInfo.getName(), world);
            reverseWorldNames.put(world.getName(), worldInfo.getName());

            configureWorld(world, worldInfo.getConfig());

            if (worldInfo.isDefaultWorld()) {
                defaultWorldName = worldInfo.getName();
            }
        }


        //TODO a way to specify which world to use for spawn
        spawn = worlds.get(defaultWorldName).getSpawnLocation();
    }


    @Override
    public GameMapInfo getInfo() {
        return gameMapInfo;
    }


    private void configureWorld(World world, Config config) {


        if (config.contains("spawn")) {
            spawn = BukkitConfigUtils.getLocation(config, world, "spawn");
            if (spawn == null) {
                System.err.println("Invalid map spawn for map " + gameMapInfo.getName());
                spawn = world.getSpawnLocation();
            } else {
                world.setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
            }
        }

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

        if (config.contains("border")) {
            WorldBorder worldBorder = world.getWorldBorder();

            Vector center = BukkitConfigUtils.getVector(config, "border.center");
            if (center != null) worldBorder.setCenter(center.getX(), center.getZ());

            double size = config.getDouble("border.size");
            worldBorder.setSize(size);
        }
    }


    @Override
    public CustomItem getCustomItem(String name) {
        return customItemIdentifierMap.get(name);
    }


    @Override
    public Collection<CustomItem> getAllCustomItems() {
        return customItemIdentifierMap.values();
    }


    @Override
    public CustomEntity getCustomEntity(String name) {
        return customEntityMap.get(name);
    }    @Override
    public World getDefaultWorld() {
        return worlds.get(defaultWorldName);
    }


    @Override
    public void bindTaskToMap(GameTask task) {
        mapTaskList.addTask(task);
    }


    @Override
    public void unloadMap() {
        mapTaskList.cancelAllTasks();

        List<PastedSchematic> pastedSchematics = new ArrayList<>(this.pastedSchematics);

        pastedSchematics.forEach(PastedSchematic::removed);

        for (Map.Entry<String, World> worldEntry : worlds.entrySet()) {
            worldHandlers.get(worldEntry.getKey()).unloadWorld(worldEntry.getValue());
        }

    }


    @Override
    public boolean teleportUser(User user) {
        if (user.getLocation().getWorld().equals(getSpawn().getWorld())) return true;
        return user.teleport(getSpawn());
    }


    @Override
    public List<CustomListener> getListeners() {
        return listeners;
    }


    @Override
    public Map<String, CustomListener> getListenerMap() {
        return listenerMap;
    }    @Override
    public World getWorld(String name) {
        World world = worlds.get(name);

        if (world == null && DEFAULT_WORLD_NAME.equals(name)) {
            //handle a different default world name being used
            return getDefaultWorld();
        } else {
            return world;
        }
    }


    @Override
    public Schematic getSchematic(String name) {
        return schematicMap.get(name);
    }


    @Override
    public JSONBook getBook(String name) {
        return bookMap.get(name);
    }


    @Override
    public Location getLocation(MapPoint point) {
        World world = worlds.get(point.getWorld());

        float yaw = point.getYaw();
        float pitch = point.getPitch();

        //Account for the fact that NaN yaw/pitch indicate that they shouldn't be changed / are not present
        if (Float.isNaN(yaw)) yaw = 0;
        if (Float.isNaN(pitch)) pitch = 0;

        return new Location(world, point.getX(), point.getY(), point.getZ(), yaw, pitch);
    }


    @Override
    public MapPoint getMapPoint(Location loc) {
        String worldName = getWorldInfo(loc.getWorld()).getName();

        return new MapPoint(worldName, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }


    @Override
    public MapWorldInfo getWorldInfo(World world) {
        return getInfo().getWorlds().get(reverseWorldNames.get(world.getName()));
    }    @Override
    public Collection<World> getWorlds() {
        return worlds.values();
    }


    @Override
    public void addPastedSchematic(PastedSchematic schematic) {
        pastedSchematics.add(schematic);
    }


    @Override
    public void removePastedSchematic(PastedSchematic schematic) {
        pastedSchematics.remove(schematic);
    }


    @Override
    public Block getBlock(MapPoint point) {
        return getLocation(point).getBlock();
    }


    @Override
    public Location getSpawn() {
        return spawn;
    }


    @Override
    public boolean hasSharedObject(String name) {
        return sharedObjects.containsKey(name);
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
    public void addCustomEntity(CustomEntity customEntity) {
        customEntityMap.put(customEntity.getName(), customEntity);
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


    @Override
    public void addCurrency(String name, Config config) {
        currencyConfigs.put(name, config);
    }


    public Map<String, Config> getCurrencies() {
        return currencyConfigs;
    }


    @Override
    public boolean canPaste(BoundingBox bounds) {
        for (PastedSchematic schematic : pastedSchematics) {
            if (!schematic.canPaste(bounds)) return false;
        }

        return true;
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


}
