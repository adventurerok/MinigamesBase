package com.ithinkrok.minigames;

import com.comphenix.protocol.ProtocolLibrary;
import com.ithinkrok.minigames.database.DatabaseTask;
import com.ithinkrok.minigames.database.DatabaseTaskRunner;
import com.ithinkrok.minigames.database.Persistence;
import com.ithinkrok.minigames.event.map.*;
import com.ithinkrok.minigames.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.event.user.inventory.UserInventoryCloseEvent;
import com.ithinkrok.minigames.event.user.state.UserAttackedEvent;
import com.ithinkrok.minigames.event.user.state.UserDamagedEvent;
import com.ithinkrok.minigames.event.user.state.UserDeathEvent;
import com.ithinkrok.minigames.event.user.state.UserFoodLevelChangeEvent;
import com.ithinkrok.minigames.event.user.world.*;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.item.IdentifierMap;
import com.ithinkrok.minigames.lang.LangFile;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.lang.MultipleLanguageLookup;
import com.ithinkrok.minigames.map.GameMap;
import com.ithinkrok.minigames.map.GameMapInfo;
import com.ithinkrok.minigames.schematic.Schematic;
import com.ithinkrok.minigames.task.GameRunnable;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.task.TaskScheduler;
import com.ithinkrok.minigames.team.Team;
import com.ithinkrok.minigames.team.TeamIdentifier;
import com.ithinkrok.minigames.user.UserResolver;
import com.ithinkrok.minigames.util.EntityUtils;
import com.ithinkrok.minigames.util.InventoryUtils;
import com.ithinkrok.minigames.util.InvisiblePlayerAttacker;
import com.ithinkrok.minigames.util.disguise.DCDisguiseController;
import com.ithinkrok.minigames.util.disguise.DisguiseController;
import com.ithinkrok.minigames.util.disguise.MinigamesDisguiseController;
import com.ithinkrok.minigames.util.io.ConfigHolder;
import com.ithinkrok.minigames.util.io.ConfigParser;
import com.ithinkrok.minigames.util.io.FileLoader;
import com.ithinkrok.minigames.util.io.ResourceHandler;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * Created by paul on 31/12/15.
 * <p>
 * In future: Will be a TaskScheduler, UserResolver, FileLoader and DatabaseTaskRunner only
 */
@SuppressWarnings("unchecked")
public class Game implements LanguageLookup, TaskScheduler, UserResolver, FileLoader, ConfigHolder, DatabaseTaskRunner {

    private ConcurrentMap<UUID, User> usersInServer = new ConcurrentHashMap<>();
    private List<GameGroup> gameGroups = new ArrayList<>();

    private Plugin plugin;

    private GameGroup spawnGameGroup;

    private DisguiseController disguiseController;

    private ConfigurationSection config;

    private MultipleLanguageLookup multipleLanguageLookup = new MultipleLanguageLookup();

    private IdentifierMap<CustomItem> customItemIdentifierMap = new IdentifierMap<>();

    private HashMap<String, Listener> defaultListeners = new HashMap<>();
    private WeakHashMap<String, GameGroup> mapToGameGroup = new WeakHashMap<>();

    private Map<String, GameState> gameStates = new HashMap<>();
    private Map<String, TeamIdentifier> teamIdentifiers = new HashMap<>();
    private Map<String, Kit> kits = new HashMap<>();

    private Map<String, GameMapInfo> maps = new HashMap<>();
    private Map<String, Schematic> schematicMap = new HashMap<>();
    private Map<String, ConfigurationSection> sharedObjects = new HashMap<>();

    private Persistence persistence;

    private String startMapName;
    private String startGameStateName;

    public Game(MinigamesPlugin plugin) {
        this.plugin = plugin;

        persistence = new Persistence(plugin);

        InvisiblePlayerAttacker.enablePlayerAttacker(this, plugin, ProtocolLibrary.getProtocolManager());

        unloadDefaultWorlds();

        setupDisguiseController();
    }

    private void unloadDefaultWorlds() {
        if (Bukkit.getWorlds().size() != 1) System.out.println("You should disable the nether/end worlds to save RAM!");

        for (World world : Bukkit.getWorlds()) {
            world.setKeepSpawnInMemory(false);

            for (Chunk chunk : world.getLoadedChunks()) {
                chunk.unload(false, true);
            }
        }
    }

    private void setupDisguiseController() {
        if (Bukkit.getPluginManager().getPlugin("DisguiseCraft") != null) {
            disguiseController = new DCDisguiseController();
        } else {
            disguiseController = new MinigamesDisguiseController();
        }
    }

    @Override
    public void addListener(String name, Listener listener) {
        defaultListeners.put(name, listener);
    }

    @Override
    public void addLanguageLookup(LanguageLookup languageLookup) {
        multipleLanguageLookup.addLanguageLookup(languageLookup);
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
    public void addCustomItem(CustomItem item) {
        customItemIdentifierMap.put(item.getName(), item);
    }

    public CustomItem getCustomItem(String name) {
        return customItemIdentifierMap.get(name);
    }

    public CustomItem getCustomItem(int identifier) {
        return customItemIdentifierMap.get(identifier);
    }

    public Collection<GameState> getGameStates() {
        return gameStates.values();
    }

    public void registerListeners() {
        Listener listener = new GameListener();
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    public GameGroup getSpawnGameGroup() {
        return spawnGameGroup;
    }

    public void reloadConfig() {
        plugin.reloadConfig();

        config = plugin.getConfig();

        reloadMaps();

        startGameStateName = config.getString("start_game_state");

        teamIdentifiers.clear();
        gameStates.clear();
        kits.clear();
        sharedObjects.clear();
        defaultListeners.clear();
        multipleLanguageLookup = new MultipleLanguageLookup();
        customItemIdentifierMap.clear();

        ConfigParser.parseConfig(this, this, this, this, "config.yml", config);
    }

    private void reloadMaps() {
        maps.clear();

        File mapsFolder = new File(plugin.getDataFolder(), GameMapInfo.MAPS_FOLDER);
        if (!mapsFolder.exists() || mapsFolder.isFile()) {
            throw new RuntimeException("Maps directory does not exist!");
        }

        String[] mapNames = mapsFolder.list((dir, name) -> name.endsWith(".yml"));

        for (String mapNameWithYml : mapNames) {
            String mapNameWithoutYml = mapNameWithYml.substring(0, mapNameWithYml.length() - 4);

            loadMapInfo(mapNameWithoutYml);
        }

        startMapName = config.getString("start_map");
    }

    private void loadMapInfo(String mapName) {
        maps.put(mapName, new GameMapInfo(this, mapName));
    }

    @Override
    public void addKit(Kit kit) {
        kits.put(kit.getName(), kit);
    }

    @Override
    public void addGameState(GameState gameState) {
        gameStates.put(gameState.getName(), gameState);
    }

    @Override
    public void addTeamIdentifier(TeamIdentifier teamIdentifier) {
        teamIdentifiers.put(teamIdentifier.getName(), teamIdentifier);
    }

    public ConfigurationSection getSharedObject(String name) {
        return sharedObjects.get(name);
    }

    @Override
    public LangFile loadLangFile(String path) {
        return new LangFile(ResourceHandler.getPropertiesResource(plugin, path));
    }

    public void removeUser(User user) {
        usersInServer.values().remove(user);
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    public Schematic getSchematic(String name) {
        return schematicMap.get(name);
    }

    public GameMapInfo getStartMapInfo() {
        return maps.get(startMapName);
    }

    public GameMapInfo getMapInfo(String mapName) {
        return maps.get(mapName);
    }

    @Override
    public ConfigurationSection loadConfig(String path) {
        return ResourceHandler.getConfigResource(plugin, path);
    }

    public ConfigurationSection getConfig() {
        return config;
    }


    @Override
    public User getUser(UUID uuid) {
        return usersInServer.get(uuid);
    }

    private GameGroup createGameGroup() {
        GameGroup gameGroup = new GameGroup(this);

        gameGroup.setDefaultListeners(defaultListeners);
        gameGroup.setTeamIdentifiers(teamIdentifiers.values());
        gameGroup.setKits(kits.values());

        gameGroup.prepareStart();
        gameGroup.changeGameState(startGameStateName);
        gameGroup.changeMap(startMapName);

        return gameGroup;
    }

    private User createUser(GameGroup gameGroup, Team team, UUID uuid, LivingEntity entity) {
        User user = new User(gameGroup, team, uuid, entity);

        usersInServer.put(user.getUuid(), user);
        return user;
    }

    public String getChatPrefix() {
        return ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + "ColonyWars" + ChatColor.GRAY + "] " + ChatColor.YELLOW;
    }

    @Override
    public boolean hasLocale(String name) {
        return multipleLanguageLookup.hasLocale(name);
    }

    @Override
    public String getLocale(String name) {
        return multipleLanguageLookup.getLocale(name);
    }

    @Override
    public String getLocale(String name, Object... args) {
        return multipleLanguageLookup.getLocale(name, args);
    }

    public void unload() {
        gameGroups.forEach(GameGroup::unload);

        persistence.onPluginDisabled();
    }

    @Override
    public void doDatabaseTask(DatabaseTask task) {
        persistence.doTask(task);
    }

    @Override
    public GameTask doInFuture(GameRunnable task) {
        return doInFuture(task, 1);
    }

    @Override
    public GameTask doInFuture(GameRunnable task, int delay) {
        GameTask gameTask = new GameTask(task);

        gameTask.schedule(plugin, delay);
        return gameTask;
    }

    @Override
    public GameTask repeatInFuture(GameRunnable task, int delay, int period) {
        GameTask gameTask = new GameTask(task);

        gameTask.schedule(plugin, delay, period);
        return gameTask;
    }

    @Override
    public void cancelAllTasks() {
        throw new RuntimeException("You cannot cancel all game tasks");
    }

    public void makeEntityRepresentUser(User user, Entity entity) {
        entity.setMetadata("rep", new FixedMetadataValue(plugin, user.getUuid()));
    }

    public void makeEntityActualUser(User user, Entity entity) {
        entity.setMetadata("actual", new FixedMetadataValue(plugin, user.getUuid()));
    }

    public void setGameGroupForMap(GameGroup gameGroup, String mapName) {
        mapToGameGroup.values().remove(gameGroup);
        mapToGameGroup.put(mapName, gameGroup);
    }

    public TeamIdentifier getTeamIdentifier(String team) {
        return teamIdentifiers.get(team);
    }

    public Kit getKit(String kitName) {
        return kits.get(kitName);
    }

    public void makeEntityRepresentTeam(Team team, Entity entity) {
        entity.setMetadata("team", new FixedMetadataValue(plugin, team.getName()));
    }

    public void disguiseUser(User user, EntityType type) {
        disguiseController.disguise(user, type);
    }

    public void unDisguiseUser(User user) {
        disguiseController.unDisguise(user);
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }

    private class GameListener implements Listener {

        @EventHandler
        public void eventPlayerJoined(PlayerJoinEvent event) {
            event.setJoinMessage(null);

            Player player = event.getPlayer();

            User user = getUser(player.getUniqueId());
            GameGroup gameGroup;

            if (user != null) {
                gameGroup = user.getGameGroup();
                user.becomePlayer(player);
            } else {
                if (spawnGameGroup == null) {
                    spawnGameGroup = createGameGroup();
                    gameGroups.add(spawnGameGroup);
                }

                gameGroup = spawnGameGroup;
                user = createUser(gameGroup, null, player.getUniqueId(), player);
                System.out.println(Bukkit.getOfflinePlayer(player.getName()).getUniqueId());
            }

            gameGroup.userEvent(new UserJoinEvent(user, UserJoinEvent.JoinReason.JOINED_SERVER));
        }

        @EventHandler
        public void eventWeatherChanged(WeatherChangeEvent event) {
            GameGroup gameGroup = getGameGroup(event.getWorld());
            if (gameGroup == null) return;

            if (gameGroup.getCurrentMap().getInfo().getWeatherEnabled()) return;
            event.setCancelled(true);
        }

        private GameGroup getGameGroup(World world) {
            return mapToGameGroup.get(world.getName());
        }

        @EventHandler
        public void eventPlayerQuit(PlayerQuitEvent event) {
            event.setQuitMessage(null);

            User user = getUser(event.getPlayer().getUniqueId());

            UserQuitEvent userEvent = new UserQuitEvent(user, UserQuitEvent.QuitReason.QUIT_SERVER);
            user.getGameGroup().userEvent(userEvent);
        }

        @EventHandler
        public void eventPotionSplash(PotionSplashEvent event) {
            String mapName = event.getPotion().getWorld().getName();
            GameGroup gameGroup = mapToGameGroup.get(mapName);
            if (gameGroup == null) return;

            GameMap map = gameGroup.getCurrentMap();

            ProjectileSource thrower = event.getPotion().getShooter();
            User throwerUser = null;
            if (thrower instanceof Entity) throwerUser = EntityUtils.getRepresentingUser(Game.this, (Entity) thrower);

            gameGroup.gameEvent(new MapPotionSplashEvent(gameGroup, map, event, throwerUser));
        }

        @EventHandler
        public void eventPlayerChat(AsyncPlayerChatEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());

            Iterator<Player> it = event.getRecipients().iterator();
            while (it.hasNext()) {
                Player player = it.next();
                if (user.getUser(player.getUniqueId()) != null) continue;

                it.remove();
            }

            UserChatEvent userEvent = new UserChatEvent(user, event);

            user.getGameGroup().userEvent(userEvent);
        }

        @EventHandler
        public void eventBlockExp(BlockExpEvent event) {
            if (event instanceof BlockBreakEvent) return;

            String mapName = event.getBlock().getWorld().getName();
            GameGroup gameGroup = mapToGameGroup.get(mapName);
            if (gameGroup == null) return;

            GameMap map = gameGroup.getCurrentMap();
            if (!map.getWorld().getName().equals(mapName))
                throw new RuntimeException("Map still registered to old GameGroup");

            gameGroup.gameEvent(new MapBlockBreakNaturallyEvent(gameGroup, map, event));
        }

        @EventHandler
        public void eventBlockBurn(BlockBurnEvent event) {
            String mapName = event.getBlock().getWorld().getName();
            GameGroup gameGroup = mapToGameGroup.get(mapName);
            if (gameGroup == null) return;

            GameMap map = gameGroup.getCurrentMap();

            gameGroup.gameEvent(new MapBlockBurnEvent(gameGroup, map, event));
        }

        @EventHandler
        public void eventBlockSpread(BlockSpreadEvent event) {
            eventBlockGrow(event);
        }

        @EventHandler
        public void eventBlockGrow(BlockGrowEvent event) {
            String mapName = event.getBlock().getWorld().getName();
            GameGroup gameGroup = mapToGameGroup.get(mapName);
            if (gameGroup == null) return;

            GameMap map = gameGroup.getCurrentMap();

            gameGroup.gameEvent(new MapBlockGrowEvent(gameGroup, map, event));
        }

        @EventHandler
        public void eventItemSpawn(ItemSpawnEvent event) {
            String mapName = event.getEntity().getWorld().getName();
            GameGroup gameGroup = mapToGameGroup.get(mapName);
            if (gameGroup == null) return;

            GameMap map = gameGroup.getCurrentMap();
            if (!map.getWorld().getName().equals(mapName))
                throw new RuntimeException("Map still registered to old GameGroup");

            gameGroup.gameEvent(new MapItemSpawnEvent(gameGroup, map, event));
        }

        private GameGroup getGameGroup(Location location) {
            return getGameGroup(location.getWorld());
        }

        @EventHandler
        public void eventCreatureSpawn(CreatureSpawnEvent event) {
            String mapName = event.getEntity().getWorld().getName();
            GameGroup gameGroup = mapToGameGroup.get(mapName);
            if (gameGroup == null) return;

            GameMap map = gameGroup.getCurrentMap();
            if (!map.getWorld().getName().equals(mapName))
                throw new RuntimeException("Map still registered to old GameGroup");

            gameGroup.gameEvent(new MapCreatureSpawnEvent(gameGroup, map, event));
        }

        @EventHandler
        public void eventPlayerDropItem(PlayerDropItemEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserDropItemEvent(user, event));
        }

        @EventHandler
        public void eventPlayerPickupItem(PlayerPickupItemEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserPickupItemEvent(user, event));
        }

        @EventHandler
        public void eventPlayerInteractWorld(PlayerInteractEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserInteractWorldEvent(user, event));

            if (event.isCancelled() && InventoryUtils.isArmor(event.getItem())) event.getPlayer().updateInventory();
        }

        @EventHandler
        public void eventBlockBreak(BlockBreakEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserBreakBlockEvent(user, event));
        }

        @EventHandler
        public void eventBlockPlace(BlockPlaceEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserPlaceBlockEvent(user, event));
        }

        @EventHandler(priority = EventPriority.LOW)
        public void eventEntityDamagedByEntity(EntityDamageByEntityEvent event) {
            GameGroup gameGroup = getGameGroup(event.getEntity().getWorld());

            Team attackerTeam = EntityUtils.getRepresentingTeam(gameGroup, event.getDamager());
            Team targetTeam = EntityUtils.getRepresentingTeam(gameGroup, event.getEntity());

            User attacker = EntityUtils.getRepresentingUser(gameGroup, event.getDamager());
            if (attacker == null) {
                if (Objects.equals(attackerTeam, targetTeam)) {
                    event.setCancelled(true);
                }
                return;
            }

            if (attackerTeam == null) {
                event.setCancelled(true);
                return;
            }

            User target = EntityUtils.getActualUser(attacker, event.getEntity());
            boolean representing = !attacker.equals(EntityUtils.getActualUser(gameGroup, event.getDamager()));

            if (target != null && !target.isInGame()) {
                event.setCancelled(true);
                return;
            }

            if (attackerTeam.equals(targetTeam)) {
                if (!(representing && attacker == target)) {
                    event.setCancelled(true);
                    return;
                }
            }

            gameGroup.userEvent(new UserAttackEvent(attacker, event, target, representing));
        }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void eventEntityDamaged(EntityDamageEvent event) {
            User attacked = EntityUtils.getActualUser(Game.this, event.getEntity());
            if (attacked == null) return;

            User attacker = null;
            if (event instanceof EntityDamageByEntityEvent) {
                attacker = EntityUtils.getRepresentingUser(attacked, ((EntityDamageByEntityEvent) event).getDamager());
                attacked.getGameGroup()
                        .userEvent(new UserAttackedEvent(attacked, (EntityDamageByEntityEvent) event, attacker));
            } else {
                attacked.getGameGroup().userEvent(new UserDamagedEvent(attacked, event));
            }

            if (attacked.isCloaked()) {
                attacked.getLocation().getWorld().playSound(attacked.getLocation(), Sound.HURT_FLESH, 1.0f, 1.0f);
            }

            if (attacked.getHeath() - event.getFinalDamage() > 0) {
                if (attacker != null) attacked.setLastAttacker(attacker);
                return;
            }
            if (attacked.isPlayer()) event.setCancelled(true);

            if (attacker == null) {
                switch (event.getCause()) {
                    case FIRE_TICK:
                        attacker = attacked.getFireAttacker();
                        break;
                    case WITHER:
                        attacker = attacked.getWitherAttacker();
                        break;
                }
            }

            User assist = attacked.getLastAttacker();
            if (assist == attacker) assist = null;

            for (Entity e : attacked.getLocation().getWorld().getEntities()) {
                if (!(e instanceof Creature)) continue;

                Creature creature = (Creature) e;
                if (creature.getTarget() == null || creature.getTarget() != attacked.getEntity()) continue;
                creature.setTarget(null);
            }

            attacked.getGameGroup().userEvent(new UserDeathEvent(attacked, event, attacker, assist));
        }

        @EventHandler
        public void eventPlayerFoodLevelChange(FoodLevelChangeEvent event) {
            User user = getUser(event.getEntity().getUniqueId());
            user.getGameGroup().userEvent(new UserFoodLevelChangeEvent(user, event));
        }

        @EventHandler
        public void eventPlayerInteractEntity(PlayerInteractEntityEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserRightClickEntityEvent(user, event));
        }

        @EventHandler
        public void eventPlayerInventoryClick(InventoryClickEvent event) {
            User user = getUser(event.getWhoClicked().getUniqueId());
            user.getGameGroup().userEvent(new UserInventoryClickEvent(user, event));
        }

        @EventHandler
        public void eventPlayerInventoryClose(InventoryCloseEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserInventoryCloseEvent(user, event));
        }
    }
}
