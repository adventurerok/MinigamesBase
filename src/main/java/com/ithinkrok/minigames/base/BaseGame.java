package com.ithinkrok.minigames.base;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.collect.MapMaker;
import com.ithinkrok.minigames.api.Game;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.command.MinigamesCommand;
import com.ithinkrok.minigames.api.database.DatabaseTask;
import com.ithinkrok.minigames.api.database.Persistence;
import com.ithinkrok.minigames.api.event.controller.ControllerKillGameGroupEvent;
import com.ithinkrok.minigames.api.event.controller.ControllerSpawnGameGroupEvent;
import com.ithinkrok.minigames.api.event.controller.ControllerUpdateGameGroupEvent;
import com.ithinkrok.minigames.api.event.map.*;
import com.ithinkrok.minigames.api.event.user.game.UserCommandEvent;
import com.ithinkrok.minigames.api.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.api.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.api.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.api.event.user.inventory.UserInventoryCloseEvent;
import com.ithinkrok.minigames.api.event.user.state.UserAttackedEvent;
import com.ithinkrok.minigames.api.event.user.state.UserDamagedEvent;
import com.ithinkrok.minigames.api.event.user.state.UserDeathEvent;
import com.ithinkrok.minigames.api.event.user.state.UserFoodLevelChangeEvent;
import com.ithinkrok.minigames.api.event.user.world.*;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.protocol.ClientMinigamesProtocol;
import com.ithinkrok.minigames.api.protocol.event.GameGroupKilledEvent;
import com.ithinkrok.minigames.api.protocol.event.GameGroupSpawnedEvent;
import com.ithinkrok.minigames.api.protocol.event.GameGroupUpdateEvent;
import com.ithinkrok.minigames.api.task.GameRunnable;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.EntityUtils;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.JSONBook;
import com.ithinkrok.minigames.api.util.disguise.Disguise;
import com.ithinkrok.minigames.api.util.disguise.DisguiseController;
import com.ithinkrok.minigames.base.util.InvisiblePlayerAttacker;
import com.ithinkrok.minigames.base.util.disguise.DCDisguiseController;
import com.ithinkrok.minigames.base.util.disguise.LDDisguiseController;
import com.ithinkrok.minigames.base.util.disguise.MinigamesDisguiseController;
import com.ithinkrok.minigames.base.util.io.FileLoader;
import com.ithinkrok.msm.bukkit.util.BukkitConfig;
import com.ithinkrok.msm.client.Client;
import com.ithinkrok.msm.client.impl.MSMClient;
import com.ithinkrok.util.command.CommandUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.lang.LangFile;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
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
public class BaseGame implements Game, FileLoader {

    private final String name;

    private final String hubServer;

    private final ConcurrentMap<UUID, BaseUser> usersInServer = new ConcurrentHashMap<>();

    private final Plugin plugin;
    private final Persistence persistence;

    /**
     * Maps game group type names to their config locations
     * <p>
     * e.g. colony_wars -> colony_wars/colony_wars.yml
     */
    private final Map<String, String> gameGroupConfigMap = new HashMap<>();
    private final String fallbackConfig;

    private final Map<String, BaseGameGroup> mapToGameGroup = new MapMaker().weakValues().makeMap();
    private final Map<String, BaseGameGroup> nameToGameGroup = new MapMaker().weakValues().makeMap();

    /**
     * Maps player UUID to game group type
     */
    private final Map<UUID, String> playersJoiningGameGroupTypes = new ConcurrentHashMap<>();

    /**
     * Maps player UUID to game group name
     */
    private final Map<UUID, String> playersJoinGameGroups = new ConcurrentHashMap<>();

    private final Path configDirectory;
    private final Path mapDirectory;
    private final Path assetsDirectory;
    private final Path ramdiskDirectory;

    private final ClientMinigamesProtocol protocol;

    private DisguiseController disguiseController;

    public BaseGame(BasePlugin plugin, Config config) {
        this.plugin = plugin;

        name = config.getString("bungee.name");

        hubServer = config.getString("bungee.hub");

        fallbackConfig = config.getString("fallback_gamegroup");

        configDirectory = Paths.get(config.getString("directories.config"));
        mapDirectory = Paths.get(config.getString("directories.maps"));
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

        //Is this minecraft server the primary server on this server machine
        boolean primary = config.getBoolean("primary", false);
        protocol = new ClientMinigamesProtocol(this, primary);

        MSMClient.addProtocol("Minigames", protocol);
    }

    /**
     * Unloads all chunks in all worlds
     */
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
        if (Bukkit.getPluginManager().getPlugin("LibsDisguises") != null) {
            disguiseController = new LDDisguiseController();
        } else if (Bukkit.getPluginManager().getPlugin("DisguiseCraft") != null) {
            disguiseController = new DCDisguiseController();
        } else {
            disguiseController = new MinigamesDisguiseController();
        }
    }

    @Override
    public ClientMinigamesProtocol getProtocol() {
        return protocol;
    }

    @Override
    public Collection<String> getAvailableGameGroupTypes() {
        return gameGroupConfigMap.keySet();
    }

    @Override
    public Collection<BaseGameGroup> getGameGroups() {
        return nameToGameGroup.values();
    }

    @Override
    public Path getAssetDirectory() {
        return assetsDirectory;
    }

    @Override
    public Path getRamdiskDirectory() {
        return ramdiskDirectory;
    }

    @Override
    public Path getConfigDirectory() {
        return configDirectory;
    }

    @Override
    public Path getMapDirectory() {
        return mapDirectory;
    }

    @Override
    public void registerListeners() {
        Listener listener = new GameListener();
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    @Override
    public void sendPlayerToHub() {

    }

    @Override
    public void registerGameGroupConfig(String name, String configFile) {
        gameGroupConfigMap.put(name, configFile);
    }

    @Override
    public void removeUser(User user) {
        Validate.notNull(user, "user cannot be null");
        if (!(user instanceof BaseUser)) throw new UnsupportedOperationException("Only supports BaseUser");

        usersInServer.values().remove(user);

        user.removeFromGameGroup();
        user.cancelAllTasks();
    }

    @Override
    public BaseGameGroup getGameGroup(String ggName) {
        return nameToGameGroup.get(ggName);
    }

    @Override
    public void unload() {
        nameToGameGroup.values().forEach(GameGroup::unload);

        persistence.onPluginDisabled();
    }

    @Override
    public void removeGameGroup(GameGroup gameGroup) {
        Validate.notNull(gameGroup, "gameGroup cannot be null");
        if (!(gameGroup instanceof BaseGameGroup)) {
            throw new UnsupportedOperationException("Only supports BaseGameGroup");
        }

        nameToGameGroup.values().remove(gameGroup);

        protocol.sendGameGroupKilledPayload(gameGroup);
    }

    @Override
    public void makeEntityRepresentUser(User user, Entity entity) {
        entity.setMetadata("rep", new FixedMetadataValue(plugin, user.getUuid()));
    }

    @Override
    public void makeEntityActualUser(User user, Entity entity) {
        entity.setMetadata("actual", new FixedMetadataValue(plugin, user.getUuid()));
    }

    @Override
    public void setGameGroupForMap(GameGroup gameGroup, String mapName) {
        Validate.notNull(gameGroup, "gameGroup cannot be null");

        if (!(gameGroup instanceof BaseGameGroup)) {
            throw new UnsupportedOperationException("Only supports BaseGameGroup");
        }

        mapToGameGroup.put(mapName, (BaseGameGroup) gameGroup);
    }

    public void removeGameGroupForMap(String mapName) {
        mapToGameGroup.remove(mapName);
    }

    @Override
    public void makeEntityRepresentTeam(Team team, Entity entity) {
        entity.setMetadata("team", new FixedMetadataValue(plugin, team.getName()));
    }

    @Override
    public void disguiseUser(User user, EntityType type) {
        disguiseController.disguise(user, type);
    }

    @Override
    public void disguiseUser(User user, Disguise disguise) {
        disguiseController.disguise(user, disguise);
    }

    @Override
    public void unDisguiseUser(User user) {
        disguiseController.unDisguise(user);
    }

    @Override
    public void rejoinPlayer(Player player) {
        BaseUser user = getUser(player.getUniqueId());
        BaseGameGroup gameGroup = getGameGroupForJoining(player.getUniqueId());

        if (user != null) {
            BaseGameGroup oldGameGroup = user.getGameGroup();

            if (oldGameGroup == gameGroup || gameGroup == null) {
                user.becomePlayer(player);
                gameGroup = oldGameGroup;
            } else {
                UserQuitEvent quitEvent = new UserQuitEvent(user, UserQuitEvent.QuitReason.CHANGED_GAMEGROUP);

                oldGameGroup.userEvent(quitEvent);

                removeUser(user);

                user = createUser(gameGroup, null, player.getUniqueId(), player);
            }
        } else {
            if (gameGroup == null) {
                if (nameToGameGroup.isEmpty()) {
                    createGameGroup(fallbackConfig);
                }

                gameGroup = getSpawnGameGroup();
            }

            user = createUser(gameGroup, null, player.getUniqueId(), player);
        }

        gameGroup.userEvent(new UserJoinEvent(user, UserJoinEvent.JoinReason.JOINED_SERVER));
    }

    @Override
    public boolean sendPlayerToHub(Player player) {
        Client client = protocol.getClient();

        if (client == null) return false;

        return client.changePlayerServer(player.getUniqueId(), hubServer);
    }

    @Override
    public BaseGameGroup createGameGroup(String type) {
        BaseGameGroup gameGroup = new BaseGameGroup(this, nextGameGroupName(type), type, gameGroupConfigMap.get(type));

        nameToGameGroup.put(gameGroup.getName(), gameGroup);

        getLogger().info("Created " + type + " gamegroup: " + gameGroup.getName());

        protocol.sendGameGroupSpawnedPayload(gameGroup);

        return gameGroup;
    }

    @Override
    public BaseGameGroup getSpawnGameGroup() {
        if (nameToGameGroup.isEmpty()) return null;

        return nameToGameGroup.values().iterator().next();
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    public void preJoinGameGroup(UUID playerUUID, String type, String name) {
        doInFuture(task -> {
            playersJoiningGameGroupTypes.put(playerUUID, type);
            if (name != null) playersJoinGameGroups.put(playerUUID, name);

            User user = getUser(playerUUID);

            if (user != null) {
                if (!user.isPlayer()) return;

                rejoinPlayer(user.getPlayer());
            }

        });
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormattedName() {
        return name;
    }

    @Override
    public Config loadConfig(String path) {
        try {
            return new BukkitConfig(YamlConfiguration.loadConfiguration(configDirectory.resolve(path).toFile()));
        } catch (Exception e) {
            throw new RuntimeException("Error while loading config at: " + path, e);
        }
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
    public JSONBook loadBook(String name, String pathName) {
        Path path = configDirectory.resolve(pathName);

        try {
            List<String> lines = Files.readAllLines(path);

            StringBuilder json = new StringBuilder();

            for (String line : lines) {
                json.append(line);
            }

            return new JSONBook(name, json.toString());
        } catch (IOException e) {
            System.out.println("Failed to load json book file: " + pathName);
            e.printStackTrace();
            return new JSONBook(name, "failed to load");
        }
    }

    @Override
    public void doDatabaseTask(DatabaseTask task) {
        persistence.doTask(task);
    }

    private BaseGameGroup getGameGroupForJoining(UUID uniqueId) {
        String gameGroupName = playersJoinGameGroups.remove(uniqueId);

        if (gameGroupName != null && nameToGameGroup.containsKey(gameGroupName)) {
            return nameToGameGroup.get(gameGroupName);
        }

        String gameGroupType = playersJoiningGameGroupTypes.remove(uniqueId);

        if (gameGroupType != null) {
            for (BaseGameGroup gameGroup : getGameGroups()) {
                if (!gameGroup.getType().equals(gameGroupType)) continue;
                if (!gameGroup.isAcceptingPlayers()) continue;

                return gameGroup;
            }

            return createGameGroup(gameGroupType);
        }

        return null;
    }

    @Override
    public BaseUser getUser(UUID uuid) {
        return usersInServer.get(uuid);
    }

    private BaseUser getUser(Entity entity) {
        String mapName = entity.getWorld().getName();
        BaseGameGroup gameGroup = mapToGameGroup.get(mapName);

        if (gameGroup == null) return null;
        return gameGroup.getUser(entity.getUniqueId());
    }

    private BaseUser createUser(BaseGameGroup gameGroup, BaseTeam team, UUID uuid, LivingEntity entity) {
        BaseUser user = new BaseUser(gameGroup, team, uuid, entity);

        usersInServer.put(user.getUuid(), user);
        return user;
    }

    private String nextGameGroupName(String configName) {
        String result;
        int counter = 0;

        do {
            result = name + "_" + configName + "_" + counter;
            ++counter;
        } while (nameToGameGroup.containsKey(result));

        return result;
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

    private class GameListener implements Listener {

        @EventHandler
        public void eventPlayerJoined(PlayerJoinEvent event) {
            event.setJoinMessage(null);

            rejoinPlayer(event.getPlayer());
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
            GameGroup gameGroup = getGameGroup(event.getPotion().getWorld());
            if (gameGroup == null) return;

            GameMap map = gameGroup.getCurrentMap();

            ProjectileSource thrower = event.getPotion().getShooter();
            User throwerUser = null;
            if (thrower instanceof Entity)
                throwerUser = EntityUtils.getRepresentingUser(BaseGame.this, (Entity) thrower);

            gameGroup.gameEvent(new MapPotionSplashEvent(gameGroup, map, event, throwerUser));
        }

        @EventHandler
        public void eventPlayerChat(AsyncPlayerChatEvent event) {
            User user = getUser(event.getPlayer());
            if(user == null) {
                System.out.println("Player not in gamegroup: " + event.getPlayer().getName());
                return;
            }

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
            User user = getUser(event.getPlayer());
            if(user == null) {
                System.out.println("Player not in gamegroup: " + event.getPlayer().getName());
                return;
            }

            user.getGameGroup().userEvent(new UserDropItemEvent(user, event));
        }

        @EventHandler
        public void eventPlayerPickupItem(PlayerPickupItemEvent event) {
            User user = getUser(event.getPlayer());
            if(user == null) {
                System.out.println("Player not in gamegroup: " + event.getPlayer().getName());
                return;
            }

            user.getGameGroup().userEvent(new UserPickupItemEvent(user, event));
        }

        @EventHandler
        public void eventPlayerInteractWorld(PlayerInteractEvent event) {
            User user = getUser(event.getPlayer());
            if(user == null) {
                System.out.println("Player not in gamegroup: " + event.getPlayer().getName());
                return;
            }

            user.getGameGroup().userEvent(new UserInteractWorldEvent(user, event));

            if (event.isCancelled() && InventoryUtils.isArmor(event.getItem())) event.getPlayer().updateInventory();
        }

        @EventHandler
        public void eventBlockBreak(BlockBreakEvent event) {
            User user = getUser(event.getPlayer());
            if(user == null) {
                System.out.println("Player not in gamegroup: " + event.getPlayer().getName());
                return;
            }

            user.getGameGroup().userEvent(new UserBreakBlockEvent(user, event));
        }

        @EventHandler
        public void eventBlockPlace(BlockPlaceEvent event) {
            User user = getUser(event.getPlayer());
            if(user == null) {
                System.out.println("Player not in gamegroup: " + event.getPlayer().getName());
                return;
            }

            user.getGameGroup().userEvent(new UserPlaceBlockEvent(user, event));
        }

        @EventHandler
        public void eventCommandPreprocess(PlayerCommandPreprocessEvent event) {
            User sender = getUser(event.getPlayer());
            if(sender == null) {
                System.out.println("Player not in gamegroup: " + event.getPlayer().getName());
                return;
            }

            List<String> correctedArgs = CommandUtils.splitStringIntoArguments(event.getMessage());
            String commandName = correctedArgs.get(0).toLowerCase();
            correctedArgs.remove(0);

            Map<String, Object> arguments = CommandUtils.parseArgumentListToMap(correctedArgs);

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

            MinigamesCommand gameCommand =
                    new MinigamesCommand(commandName, arguments, sender.getGameGroup(), user, teamIdentifier, kit);

            UserCommandEvent commandEvent = new UserCommandEvent(sender, gameCommand);

            sender.getGameGroup().userEvent(commandEvent);

            if (!commandEvent.isHandled()) return;

            event.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOW)
        public void eventEntityDamagedByEntity(EntityDamageByEntityEvent event) {
            GameGroup gameGroup = getGameGroup(event.getEntity().getWorld());

            Config gameConfig = gameGroup.getSharedObjectOrEmpty("game");

            boolean friendlyFire = gameConfig.getBoolean("friendly_fire");
            boolean noTeamFire = gameConfig.getBoolean("no_team_fire");
            boolean notInGameFire = gameConfig.getBoolean("not_in_game_fire");

            if (gameConfig.getBoolean("all_fire")) {
                friendlyFire = noTeamFire = notInGameFire = true;
            }

            Team attackerTeam = EntityUtils.getRepresentingTeam(gameGroup, event.getDamager());
            Team targetTeam = EntityUtils.getRepresentingTeam(gameGroup, event.getEntity());

            User attacker = EntityUtils.getRepresentingUser(gameGroup, event.getDamager());
            if (attacker == null) {
                if (Objects.equals(attackerTeam, targetTeam) && !friendlyFire) {
                    event.setCancelled(true);
                }
                return;
            }

            if (attackerTeam == null && !noTeamFire) {
                event.setCancelled(true);
                return;
            }

            User target = EntityUtils.getActualUser(attacker, event.getEntity());
            boolean representing = !attacker.equals(EntityUtils.getActualUser(gameGroup, event.getDamager()));

            if (target != null && !target.isInGame() && !notInGameFire) {
                event.setCancelled(true);
                return;
            }

            if (Objects.equals(attackerTeam, targetTeam)) {
                if (!(representing && attacker == target) && !friendlyFire) {
                    event.setCancelled(true);
                    return;
                }
            }

            gameGroup.userEvent(new UserAttackEvent(attacker, event, target, representing));
        }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void eventEntityDamaged(EntityDamageEvent event) {
            GameGroup gameGroup = getGameGroup(event.getEntity().getWorld());

            User attacked = EntityUtils.getActualUser(gameGroup, event.getEntity());
            User attacker = null;

            if (attacked == null) {
                if (gameGroup == null) return;

                if (event instanceof EntityDamageByEntityEvent) {
                    attacker = EntityUtils
                            .getRepresentingUser(gameGroup, ((EntityDamageByEntityEvent) event).getDamager());
                }

                gameGroup.gameEvent(new MapEntityDamagedEvent(gameGroup, gameGroup.getCurrentMap(), event, attacker));
                return;
            }

            if (event instanceof EntityDamageByEntityEvent) {
                attacker = EntityUtils.getRepresentingUser(attacked, ((EntityDamageByEntityEvent) event).getDamager());
                attacked.getGameGroup()
                        .userEvent(new UserAttackedEvent(attacked, (EntityDamageByEntityEvent) event, attacker));
            } else {
                attacked.getGameGroup().userEvent(new UserDamagedEvent(attacked, event));
            }

            if (attacked.getHeath() - event.getFinalDamage() > 0) {
                if (attacker != null) attacked.setLastAttacker(attacker);

                if (attacked.isCloaked()) {
                    attacked.getLocation().getWorld().playSound(attacked.getLocation(), Sound.HURT_FLESH, 1.0f, 1.0f);
                }

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


            UserDeathEvent deathEvent = new UserDeathEvent(attacked, event, attacker, assist);
            attacked.getGameGroup().userEvent(deathEvent);

            if (!deathEvent.getPlayDeathSound()) return;

            attacked.getLocation().getWorld()
                    .playSound(attacked.getLocation(), EntityUtils.getDeathSound(attacked.getVisibleEntityType()), 1.0f,
                            1.0f);
        }

        @EventHandler
        public void eventPlayerFoodLevelChange(FoodLevelChangeEvent event) {
            User user = getUser(event.getEntity());
            if(user == null) {
                System.out.println("Player not in gamegroup: " + event.getEntity().getName());
                return;
            }

            user.getGameGroup().userEvent(new UserFoodLevelChangeEvent(user, event));
        }

        @EventHandler
        public void eventPlayerInteractEntity(PlayerInteractEntityEvent event) {
            User user = getUser(event.getPlayer());
            if(user == null) {
                System.out.println("Player not in gamegroup: " + event.getPlayer().getName());
                return;
            }

            user.getGameGroup().userEvent(new UserRightClickEntityEvent(user, event));
        }

        @EventHandler
        public void eventPlayerInventoryClick(InventoryClickEvent event) {
            User user = getUser(event.getWhoClicked());
            if(user == null) {
                System.out.println("Player not in gamegroup: " + event.getWhoClicked().getName());
                return;
            }

            user.getGameGroup().userEvent(new UserInventoryClickEvent(user, event));
        }

        @EventHandler
        public void eventPlayerInventoryClose(InventoryCloseEvent event) {
            User user = getUser(event.getPlayer());
            if(user == null) {
                System.out.println("Player not in gamegroup: " + event.getPlayer().getName());
                return;
            }

            user.getGameGroup().userEvent(new UserInventoryCloseEvent(user, event));
        }

        @EventHandler
        public void eventGameGroupSpawned(GameGroupSpawnedEvent event) {
            for (GameGroup gameGroup : getGameGroups()) {
                gameGroup.gameEvent(new ControllerSpawnGameGroupEvent(gameGroup, event));
            }
        }

        @EventHandler
        public void eventGameGroupUpdate(GameGroupUpdateEvent event) {
            for (GameGroup gameGroup : getGameGroups()) {
                gameGroup.gameEvent(new ControllerUpdateGameGroupEvent(gameGroup, event));
            }
        }

        @EventHandler
        public void eventGameGroupKilled(GameGroupKilledEvent event) {
            for (GameGroup gameGroup : getGameGroups()) {
                gameGroup.gameEvent(new ControllerKillGameGroupEvent(gameGroup, event));
            }
        }

        @EventHandler
        public void eventSignChange(SignChangeEvent event) {
            User user = getUser(event.getPlayer());
            if(user == null) {
                System.out.println("Player not in gamegroup: " + event.getPlayer().getName());
                return;
            }

            user.getGameGroup().userEvent(new UserEditSignEvent(user, event));
        }
    }
}
