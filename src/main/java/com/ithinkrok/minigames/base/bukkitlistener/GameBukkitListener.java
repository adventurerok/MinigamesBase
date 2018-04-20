package com.ithinkrok.minigames.base.bukkitlistener;

import com.ithinkrok.minigames.api.Game;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.command.MinigamesCommand;
import com.ithinkrok.minigames.api.event.controller.ControllerKillGameGroupEvent;
import com.ithinkrok.minigames.api.event.controller.ControllerSpawnGameGroupEvent;
import com.ithinkrok.minigames.api.event.controller.ControllerUpdateGameGroupEvent;
import com.ithinkrok.minigames.api.event.map.*;
import com.ithinkrok.minigames.api.event.user.game.UserCommandEvent;
import com.ithinkrok.minigames.api.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.api.event.user.inventory.*;
import com.ithinkrok.minigames.api.event.user.state.*;
import com.ithinkrok.minigames.api.event.user.world.*;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.map.MapWorldInfo;
import com.ithinkrok.minigames.api.protocol.event.GameGroupKilledEvent;
import com.ithinkrok.minigames.api.protocol.event.GameGroupSpawnedEvent;
import com.ithinkrok.minigames.api.protocol.event.GameGroupUpdateEvent;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.EntityUtils;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.NamedSounds;
import com.ithinkrok.minigames.base.event.map.BaseMapEntityAttackedEvent;
import com.ithinkrok.minigames.base.event.map.BaseMapEntityDamagedEvent;
import com.ithinkrok.minigames.base.event.map.BaseMapEntityRegenHealthEvent;
import com.ithinkrok.msm.bukkit.MSMPlugin;
import com.ithinkrok.msm.bukkit.protocol.ClientAPIProtocol;
import com.ithinkrok.util.command.CommandUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by paul on 27/02/16.
 */
public class GameBukkitListener implements Listener {

    private final Game game;

    private final Map<UUID, Integer> notInGameGroupErrors = new ConcurrentHashMap<>();


    public GameBukkitListener(Game game) {
        this.game = game;
    }


    @EventHandler
    public void eventPlayerJoined(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        game.rejoinPlayer(event.getPlayer());
    }


    @EventHandler
    public void eventWeatherChanged(WeatherChangeEvent event) {
        GameGroup gameGroup;
        try {
            gameGroup = getGameGroup(event.getWorld());
        } catch (UnknownWorldException ignored) {
            return;
        }

        GameMap map = gameGroup.getCurrentMap();
        MapWorldInfo worldInfo = map.getWorldInfo(event.getWorld());

        if (worldInfo.isWeatherEnabled()) return;
        event.setCancelled(true);
    }


    /**
     * @throws UnknownWorldException If there is no gamegroup for the world
     */
    private GameGroup getGameGroup(World world) {
        Objects.requireNonNull(world, "World provided to getGameGroup is null");

        GameGroup gg = game.getGameGroupFromWorldName(world.getName());

        if (gg == null) {
            throw new UnknownWorldException(world);
        }

        return gg;
    }


    @EventHandler
    public void eventPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        UserQuitEvent userEvent = new UserQuitEvent(user, UserQuitEvent.QuitReason.QUIT_SERVER);
        user.getGameGroup().userEvent(userEvent);

        game.doInFuture(task -> game.checkResourcesRestart());

        notInGameGroupErrors.remove(user.getUuid());
    }


    private void notInGameGroupError(Entity player) {
        if (player instanceof Player && !Bukkit.getOnlinePlayers().contains(player)) {
            System.out.println("Not in GG called for an offline player " + player.getName() + " in world " +
                               player.getWorld().getName());
            return;
        }

        System.out
                .println("Player not in GG: '" + player.getName() + "' in world '" + player.getWorld().getName() + "'");

        int count = notInGameGroupErrors
                .compute(player.getUniqueId(), (uuid, integer) -> integer == null ? 0 : integer + 1);

        if (count > 100) {

            //Schedule a server restart due to this
            //Allow 1 player online when restarting as at least 1 player is not in a gamegroup
            ClientAPIProtocol apiProtocol = MSMPlugin.getApiProtocol();
            if (apiProtocol.isRestartScheduled()) return;

            apiProtocol.scheduleRestart(10, 1);

            Bukkit.broadcastMessage("Server restart scheduled due to a NOT-IN-GAMEGROUP error");
            Bukkit.broadcastMessage("The server will restart if 1 or less players are online");
            Bukkit.broadcastMessage(
                    "If you believe you are glitched please move to another server in the network or disconnect");
        } else if (player instanceof Player) {
            game.rejoinPlayer((Player) player);
        }
    }


    public void eventLingeringPotionSplash(LingeringPotionSplashEvent event) {
        event.getAreaEffectCloud();
    }


    @EventHandler
    public void eventPotionSplash(PotionSplashEvent event) {
        GameGroup gameGroup;
        try {
            gameGroup = getGameGroup(event.getPotion().getWorld());
        } catch (UnknownWorldException ignored) {
            //Potion splash in world not managed by us
            return;
        }

        GameMap map = gameGroup.getCurrentMap();

        ProjectileSource thrower = event.getPotion().getShooter();
        User throwerUser = null;
        if (thrower instanceof Entity) throwerUser = EntityUtils.getRepresentingUser(gameGroup, (Entity) thrower);

        gameGroup.gameEvent(new MapPotionSplashEvent(gameGroup, map, event, throwerUser));
    }


    @EventHandler
    public void eventPlayerChat(AsyncPlayerChatEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
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

        String worldName = event.getBlock().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromWorldName(worldName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();
        checkWorldIsInMap(event.getBlock().getWorld(), map);

        gameGroup.gameEvent(new MapBlockBreakNaturallyEvent(gameGroup, map, event));
    }


    private void checkWorldIsInMap(World world, GameMap map) {
        MapWorldInfo worldInfo = map.getWorldInfo(world);

        if (worldInfo == null) {
            throw new RuntimeException("Map still registered to old GameGroup");
        }
    }


    @EventHandler
    public void eventBlockBurn(BlockBurnEvent event) {
        String mapName = event.getBlock().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromWorldName(mapName);
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
        GameGroup gameGroup = game.getGameGroupFromWorldName(mapName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();

        gameGroup.gameEvent(new MapBlockGrowEvent(gameGroup, map, event));
    }


    @EventHandler
    public void eventItemSpawn(ItemSpawnEvent event) {
        String mapName = event.getEntity().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromWorldName(mapName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();
        checkWorldIsInMap(event.getEntity().getWorld(), map);

        gameGroup.gameEvent(new MapItemSpawnEvent(gameGroup, map, event));
    }


    @EventHandler
    public void eventCreatureSpawn(CreatureSpawnEvent event) {
        String mapName = event.getEntity().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromWorldName(mapName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();
        checkWorldIsInMap(event.getEntity().getWorld(), map);

        gameGroup.gameEvent(new MapCreatureSpawnEvent(gameGroup, map, event));
    }


    @EventHandler
    public void eventEntityTarget(EntityTargetEvent event) {
        String mapName = event.getEntity().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromWorldName(mapName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();
        checkWorldIsInMap(event.getEntity().getWorld(), map);

        gameGroup.gameEvent(new MapEntityTargetEvent(gameGroup, map, event));
    }


    @EventHandler
    public void eventEntityDeath(EntityDeathEvent event) {
        String mapName = event.getEntity().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromWorldName(mapName);
        if (gameGroup == null) return;

        //Don't send entity death events for users
        if (EntityUtils.getActualUser(gameGroup, event.getEntity()) != null) return;

        GameMap map = gameGroup.getCurrentMap();
        checkWorldIsInMap(event.getEntity().getWorld(), map);

        gameGroup.gameEvent(new MapEntityDeathEvent(gameGroup, map, event));
    }


    @EventHandler
    public void eventRegainHealth(EntityRegainHealthEvent event) {
        String mapName = event.getEntity().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromWorldName(mapName);
        if (gameGroup == null) return;

        User user = EntityUtils.getActualUser(gameGroup, event.getEntity());
        if (user != null) {
            gameGroup.userEvent(new UserRegainHealthEvent(user, event));
        } else {
            GameMap map = gameGroup.getCurrentMap();
            checkWorldIsInMap(event.getEntity().getWorld(), map);

            gameGroup.gameEvent(new BaseMapEntityRegenHealthEvent(gameGroup, map, event));
        }
    }


    @EventHandler
    public void eventProjectileLaunch(ProjectileLaunchEvent event) {
        String mapName = event.getEntity().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromWorldName(mapName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();
        checkWorldIsInMap(event.getEntity().getWorld(), map);

        gameGroup.gameEvent(new MapProjectileLaunchEvent(gameGroup, map, event));
    }


    @EventHandler
    public void eventProjectileHit(ProjectileHitEvent event) {
        String mapName = event.getEntity().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromWorldName(mapName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();
        checkWorldIsInMap(event.getEntity().getWorld(), map);

        gameGroup.gameEvent(new MapProjectileHitEvent(gameGroup, map, event));
    }


    @EventHandler
    public void eventBlockIgnite(BlockIgniteEvent event) {
        String mapName = event.getBlock().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromWorldName(mapName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();
        checkWorldIsInMap(event.getBlock().getWorld(), map);

        gameGroup.gameEvent(new MapBlockIgniteEvent(gameGroup, map, event));
    }


    @EventHandler
    public void eventBlockExplode(BlockExplodeEvent event) {
        String mapName = event.getBlock().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromWorldName(mapName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();
        checkWorldIsInMap(event.getBlock().getWorld(), map);

        gameGroup.gameEvent(new MapBlockExplodeEvent(gameGroup, map, event));
    }


    @EventHandler
    public void eventEntityExplode(EntityExplodeEvent event) {
        String mapName = event.getEntity().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromWorldName(mapName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();
        checkWorldIsInMap(event.getEntity().getWorld(), map);

        gameGroup.gameEvent(new MapEntityExplodeEvent(gameGroup, map, event));
    }


    @EventHandler
    public void eventPlayerDropItem(PlayerDropItemEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        user.getGameGroup().userEvent(new UserDropItemEvent(user, event));

        if (event.isCancelled()) return;
        user.getGameGroup().userEvent(new UserInventoryUpdateEvent(user));
    }


    @EventHandler
    public void eventPlayerPickupItem(PlayerPickupItemEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        user.getGameGroup().userEvent(new UserPickupItemEvent(user, event));

        if (event.isCancelled()) return;
        user.getGameGroup().userEvent(new UserInventoryUpdateEvent(user));
    }


    @EventHandler
    public void eventPlayerItemHeld(PlayerItemHeldEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        user.getGameGroup().userEvent(new UserItemHeldEvent(user, event));
    }


    @EventHandler
    public void eventPlayerInteractWorld(PlayerInteractEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        user.getGameGroup().userEvent(new UserInteractWorldEvent(user, event));

        if (event.isCancelled() && InventoryUtils.isArmor(event.getItem())) event.getPlayer().updateInventory();
    }


    @EventHandler
    public void eventPlayerRespawn(PlayerRespawnEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        user.getGameGroup().userEvent(new UserRespawnEvent(user, event));
    }


    @EventHandler
    public void eventPlayerItemConsume(PlayerItemConsumeEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        user.getGameGroup().userEvent(new UserItemConsumeEvent(user, event));
    }


    @EventHandler
    public void eventBlockBreak(BlockBreakEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        user.getGameGroup().userEvent(new UserBreakBlockEvent(user, event));
    }


    @EventHandler
    public void eventBlockPlace(BlockPlaceEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        user.getGameGroup().userEvent(new UserPlaceBlockEvent(user, event));
    }


    @EventHandler
    public void eventCommandPreprocess(PlayerCommandPreprocessEvent event) {
        User sender = game.getUser(event.getPlayer());
        if (sender == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        List<String> correctedArgs = CommandUtils.splitStringIntoArguments(event.getMessage());
        if (correctedArgs.isEmpty()) return;

        String commandName = correctedArgs.get(0).toLowerCase();
        correctedArgs.remove(0);

        Map<String, Object> arguments = CommandUtils.parseArgumentListToMap(correctedArgs);

        User user = null;
        TeamIdentifier teamIdentifier;

        if (arguments.containsKey("u")) {
            OfflinePlayer player = Bukkit.getPlayer(arguments.get("u").toString());
            if (player != null) {
                User other = sender.getUser(player.getUniqueId());
                if (other != null) {
                    user = other;
                }
            }
        }

        if (user == null) {
            user = sender;
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
        boolean mobFire = gameConfig.getBoolean("mob_fire", true);
        boolean selfHarm = gameConfig.getBoolean("self_harm", true);

        if (gameConfig.getBoolean("all_fire")) {
            friendlyFire = noTeamFire = notInGameFire = true;
        }

        Team attackerTeam = EntityUtils.getRepresentingTeam(gameGroup, event.getDamager());
        Team targetTeam = EntityUtils.getRepresentingTeam(gameGroup, event.getEntity());

        User attacker = EntityUtils.getRepresentingUser(gameGroup, event.getDamager());

        if (attacker == null) {
            User targetRepresenting = EntityUtils.getRepresentingUser(gameGroup, event.getEntity());

            if (Objects.equals(attackerTeam, targetTeam) && !friendlyFire && targetRepresenting != null) {
                event.setCancelled(true);
            }
            return;
        }

        if ((attackerTeam == null && !noTeamFire) && (event.getDamager() instanceof Player || !mobFire)) {
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
            //You can hurt yourself if you are representing (you didn't hit yourself, you got your arrow or whatever
            // to hit you) and you are your target, and self harm is enabled
            boolean willAttackSelf = representing && attacker == target && selfHarm;
            if (!willAttackSelf && !friendlyFire) {
                event.setCancelled(true);
                return;
            }
        }

        gameGroup.userEvent(new UserAttackEvent(attacker, event, target, representing));
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void eventEntityDamaged(EntityDamageEvent event) {
        GameGroup gameGroup = getGameGroup(event.getEntity().getWorld());

        User attacked = EntityUtils.getActualUser(gameGroup, event.getEntity());
        User attacker = null;

        if (attacked == null) {
            if (gameGroup == null) return;

            if (event instanceof EntityDamageByEntityEvent) {
                attacker = EntityUtils.getRepresentingUser(gameGroup, ((EntityDamageByEntityEvent) event).getDamager());

                gameGroup.gameEvent(new BaseMapEntityAttackedEvent(gameGroup, gameGroup.getCurrentMap(),
                                                                   (EntityDamageByEntityEvent) event, attacker));
            }

            gameGroup.gameEvent(new BaseMapEntityDamagedEvent(gameGroup, gameGroup.getCurrentMap(), event));
            return;
        }

        if (event instanceof EntityDamageByEntityEvent) {
            attacker = EntityUtils.getRepresentingUser(attacked, ((EntityDamageByEntityEvent) event).getDamager());
            attacked.getGameGroup()
                    .userEvent(new UserAttackedEvent(attacked, (EntityDamageByEntityEvent) event, attacker));
        } else {
            attacked.getGameGroup().userEvent(new UserDamagedEvent(attacked, event));
        }

        //Skip playing the sound if you take no damage
        //TODO maybe check if the event is cancelled?
        if (event.getFinalDamage() < 0.01 || event.isCancelled()) {
            return;
        }

        System.out.println(attacked.getName() + " damaged: " + event.getFinalDamage());

        if (attacked.getHealth() - event.getFinalDamage() > 0.01) {
            if (attacker != null) attacked.setLastAttacker(attacker);

            if (attacked.isCloaked() && attacked.isInGame()) {
                attacked.getLocation().getWorld()
                        .playSound(attacked.getLocation(), NamedSounds.fromName("ENTITY_PLAYER_HURT"), 1.0f, 1.0f);
            }

            return;
        }
        Config gameShared = gameGroup.getSharedObjectOrEmpty("game");
        boolean cancelDeath = gameShared.getBoolean("cancel_death", true);

        if (attacked.isPlayer() && cancelDeath) event.setCancelled(true);

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

        boolean attackedInGame = attacked.isInGame();


        UserDeathEvent deathEvent = new UserDeathEvent(attacked, event, attacker, assist);
        attacked.getGameGroup().userEvent(deathEvent);

        //Log death messages
        if (attacker != null) {
            System.out.println(attacked.getName() + " died: reason=" + event.getCause() + ", finalDamage=" +
                               event.getFinalDamage() + ", damage=" + event.getDamage() + ", attacker=" +
                               attacker.getName() + ", holding=" + attacker.getInventory().getItemInHand());
        } else {
            System.out.println(attacked.getName() + " died: reason=" + event.getCause() + ", finalDamage=" +
                               event.getFinalDamage() + ", attacker=null");
        }

        if (!deathEvent.getPlayDeathSound() || !attackedInGame) return;

        attacked.getLocation().getWorld()
                .playSound(attacked.getLocation(), EntityUtils.getDeathSound(attacked.getVisibleEntityType()), 1.0f,
                           1.0f);
    }


    @EventHandler
    public void eventPlayerPortal(PlayerPortalEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        GameMap map = user.getMap();
        MapWorldInfo info = map.getWorldInfo(user.getLocation().getWorld());
        if (info == null) {
            throw new RuntimeException("User should be in valid map world: " + user.getName() + " in world " +
                                       user.getLocation().getWorld().getName());
        }

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            String netherWorld = info.getNetherWorld();
            World nether = map.getWorld(netherWorld);
            if (nether != null) {
                double scaledX = event.getFrom().getX() * info.getNetherScale();
                double scaledZ = event.getFrom().getZ() * info.getNetherScale();
                Location newTo = new Location(nether, scaledX, event.getFrom().getY(), scaledZ);
                event.setTo(newTo);
                event.useTravelAgent(true);
                event.getPortalTravelAgent().setCanCreatePortal(true);
            }
        } else if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            String endWorld = info.getEndWorld();
            World end = map.getWorld(endWorld);
            if(end != null) {
                if (end.getEnvironment() == World.Environment.THE_END) {
                    event.setTo(end.getSpawnLocation());
                } else {
                    event.getPortalTravelAgent().setCanCreatePortal(false);
                    if (user.isPlayer() && user.getPlayer().getBedSpawnLocation() != null &&
                        user.getPlayer().getBedSpawnLocation().getWorld().equals(end)) {
                        event.setTo(user.getPlayer().getBedSpawnLocation());
                    } else {
                        event.setTo(end.getSpawnLocation());
                    }
                }
            }
        }
    }


    @EventHandler
    public void eventPlayerFoodLevelChange(FoodLevelChangeEvent event) {
        User user = game.getUser(event.getEntity());
        if (user == null) {
            notInGameGroupError(event.getEntity());
            return;
        }

        user.getGameGroup().userEvent(new UserFoodLevelChangeEvent(user, event));
    }


    @EventHandler
    public void eventPlayerInteractEntity(PlayerInteractEntityEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        user.getGameGroup().userEvent(new UserRightClickEntityEvent(user, event));
    }


    @EventHandler
    public void eventPlayerInventoryClick(InventoryClickEvent event) {
        User user = game.getUser(event.getWhoClicked());
        if (user == null) {
            notInGameGroupError(event.getWhoClicked());
            return;
        }

        user.getGameGroup().userEvent(new UserInventoryClickEvent(user, event));

        if (event.isCancelled()) return;
        user.getGameGroup().userEvent(new UserInventoryUpdateEvent(user));
    }


    @EventHandler
    public void eventPlayerInventoryClose(InventoryCloseEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        user.getGameGroup().userEvent(new UserInventoryCloseEvent(user, event));
    }


    @EventHandler
    public void eventGameGroupSpawned(GameGroupSpawnedEvent event) {
        for (GameGroup gameGroup : game.getGameGroups()) {
            gameGroup.gameEvent(new ControllerSpawnGameGroupEvent(gameGroup, event));
        }
    }


    @EventHandler
    public void eventGameGroupUpdate(GameGroupUpdateEvent event) {
        for (GameGroup gameGroup : game.getGameGroups()) {
            gameGroup.gameEvent(new ControllerUpdateGameGroupEvent(gameGroup, event));
        }
    }


    @EventHandler
    public void eventGameGroupKilled(GameGroupKilledEvent event) {
        for (GameGroup gameGroup : game.getGameGroups()) {
            gameGroup.gameEvent(new ControllerKillGameGroupEvent(gameGroup, event));
        }
    }


    @EventHandler
    public void eventSignChange(SignChangeEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        user.getGameGroup().userEvent(new UserEditSignEvent(user, event));
    }


    private static class UnknownWorldException extends RuntimeException {

        public UnknownWorldException(World world) {
            super("No GameGroup for world: " + world.getName());
        }
    }
}
