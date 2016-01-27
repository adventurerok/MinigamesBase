package com.ithinkrok.minigames;

import com.comphenix.protocol.ProtocolLibrary;
import com.ithinkrok.minigames.command.Command;
import com.ithinkrok.minigames.command.GameCommandHandler;
import com.ithinkrok.minigames.database.DatabaseTask;
import com.ithinkrok.minigames.database.DatabaseTaskRunner;
import com.ithinkrok.minigames.database.Persistence;
import com.ithinkrok.minigames.event.map.*;
import com.ithinkrok.minigames.event.user.game.UserCommandEvent;
import com.ithinkrok.minigames.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.event.user.inventory.UserInventoryCloseEvent;
import com.ithinkrok.minigames.event.user.state.UserAttackedEvent;
import com.ithinkrok.minigames.event.user.state.UserDamagedEvent;
import com.ithinkrok.minigames.event.user.state.UserDeathEvent;
import com.ithinkrok.minigames.event.user.state.UserFoodLevelChangeEvent;
import com.ithinkrok.minigames.event.user.world.*;
import com.ithinkrok.minigames.lang.LangFile;
import com.ithinkrok.minigames.map.GameMap;
import com.ithinkrok.minigames.map.GameMapInfo;
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
import com.ithinkrok.minigames.util.io.FileLoader;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class Game implements TaskScheduler, UserResolver, FileLoader, DatabaseTaskRunner {

    private final ConcurrentMap<UUID, User> usersInServer = new ConcurrentHashMap<>();
    private final List<GameGroup> gameGroups = new ArrayList<>();

    private final Plugin plugin;
    private final WeakHashMap<String, GameGroup> mapToGameGroup = new WeakHashMap<>();
    private final Persistence persistence;
    private final Map<String, String> gameGroupConfigMap = new HashMap<>();
    private final String gameGroupConfig = "colony_wars";

    private final Path configDirectory, mapDirectory, mapConfigDirectory, assetsDirectory;
    private final Path ramdiskDirectory;
    private GameGroup spawnGameGroup;
    private DisguiseController disguiseController;

    public Game(BasePlugin plugin, ConfigurationSection config) {
        this.plugin = plugin;

        configDirectory = Paths.get(config.getString("directories.config"));
        mapDirectory = Paths.get(config.getString("directories.maps"));
        mapConfigDirectory = Paths.get(config.getString("directories.map_config"));
        assetsDirectory = Paths.get(config.getString("directories.assets"));

        ramdiskDirectory =
                config.getBoolean("ramdisk.enable") ? Paths.get(config.getString("ramdisk.directory")) : null;

        if (ramdiskDirectory != null && !Files.exists(ramdiskDirectory)) {
            try {
                Files.createDirectory(ramdiskDirectory);
            } catch (IOException e) {
                System.out.println("Failed to create ramdisk directory");
                e.printStackTrace();
            }
        }

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

    public Path getRamdiskDirectory() {
        return ramdiskDirectory;
    }

    public Path getConfigDirectory() {
        return configDirectory;
    }

    public Path getMapDirectory() {
        return mapDirectory;
    }

    public void registerListeners() {
        Listener listener = new GameListener();
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    public void registerGameGroupConfig(String name, String configFile) {
        gameGroupConfigMap.put(name, configFile);
    }

    public GameGroup getSpawnGameGroup() {
        return spawnGameGroup;
    }

    public void removeUser(User user) {
        usersInServer.values().remove(user);
    }

    @Override
    public ConfigurationSection loadConfig(String path) {
        return YamlConfiguration.loadConfiguration(configDirectory.resolve(path).toFile());
    }

    @Override
    public LangFile loadLangFile(String path) {
        try {
            return new LangFile(configDirectory.resolve(path));
        } catch (IOException e) {
            System.out.println("Failed to load lang file: " + path);
            e.printStackTrace();
            return new LangFile(new Properties());
        }
    }

    @Override
    public Path getAssetDirectory() {
        return assetsDirectory;
    }

    @Override
    public User getUser(UUID uuid) {
        return usersInServer.get(uuid);
    }

    private GameGroup createGameGroup(String name) {
        return new GameGroup(this, gameGroupConfigMap.get(name));

    }

    private User createUser(GameGroup gameGroup, Team team, UUID uuid, LivingEntity entity) {
        User user = new User(gameGroup, team, uuid, entity);

        usersInServer.put(user.getUuid(), user);
        return user;
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
                    spawnGameGroup = createGameGroup(gameGroupConfig);
                    gameGroups.add(spawnGameGroup);
                }

                gameGroup = spawnGameGroup;
                user = createUser(gameGroup, null, player.getUniqueId(), player);
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

        @EventHandler
        public void eventCommandPreprocess(PlayerCommandPreprocessEvent event) {
            User sender = getUser(event.getPlayer().getUniqueId());

            List<String> correctedArgs = GameCommandHandler.splitStringIntoArguments(event.getMessage());
            String commandName = correctedArgs.get(0).toLowerCase();
            correctedArgs.remove(0);

            Map<String, Object> arguments = GameCommandHandler.parseArgumentListToMap(correctedArgs);

            User user = sender;
            TeamIdentifier teamIdentifier;

            if (arguments.containsKey("u")) {
                OfflinePlayer player = Bukkit.getPlayer(arguments.get("u").toString());
                if (player != null) {
                    User other = sender.getUser(player.getUniqueId());
                    if (other != null) user = other;
                }
            }

            teamIdentifier = user.getTeamIdentifier();


            if (arguments.containsKey("t")) {
                teamIdentifier = sender.getGameGroup().getTeamIdentifier(arguments.get("t").toString());
            }

            Kit kit = null;
            if (arguments.containsKey("k")) {
                kit = sender.getGameGroup().getKit(arguments.get("k").toString());
            }

            Command gameCommand = new Command(commandName, arguments, sender.getGameGroup(), user, teamIdentifier, kit);

            UserCommandEvent commandEvent = new UserCommandEvent(sender, gameCommand);

            sender.getGameGroup().userEvent(commandEvent);

            if (!commandEvent.isHandled()) return;

            event.setCancelled(true);
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
