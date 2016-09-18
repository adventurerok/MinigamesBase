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
import com.ithinkrok.minigames.api.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.api.event.user.inventory.UserInventoryCloseEvent;
import com.ithinkrok.minigames.api.event.user.state.UserAttackedEvent;
import com.ithinkrok.minigames.api.event.user.state.UserDamagedEvent;
import com.ithinkrok.minigames.api.event.user.state.UserDeathEvent;
import com.ithinkrok.minigames.api.event.user.state.UserFoodLevelChangeEvent;
import com.ithinkrok.minigames.api.event.user.world.*;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.protocol.event.GameGroupKilledEvent;
import com.ithinkrok.minigames.api.protocol.event.GameGroupSpawnedEvent;
import com.ithinkrok.minigames.api.protocol.event.GameGroupUpdateEvent;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.EntityUtils;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.NamedSounds;
import com.ithinkrok.msm.bukkit.MSMPlugin;
import com.ithinkrok.msm.bukkit.protocol.ClientAPIProtocol;
import com.ithinkrok.util.command.CommandUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Bukkit;
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by paul on 27/02/16.
 */
public class GameBukkitListener implements Listener {

    private final Game game;

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
        GameGroup gameGroup = getGameGroup(event.getWorld());
        if (gameGroup == null) return;

        if (gameGroup.getCurrentMap().getInfo().getWeatherEnabled()) return;
        event.setCancelled(true);
    }

    private GameGroup getGameGroup(World world) {
        return game.getGameGroupFromMapName(world.getName());
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
    }

    private void notInGameGroupError(Entity player) {
        System.out
                .println("Player not in GG: '" + player.getName() + "' in world '" + player.getWorld().getName() + "'");

        //Schedule a server restart due to this
        //Allow 1 player online when restarting as at least 1 player is not in a gamegroup
        ClientAPIProtocol apiProtocol = MSMPlugin.getApiProtocol();
        if (apiProtocol.isRestartScheduled()) return;

        apiProtocol.scheduleRestart(10, 1);

        Bukkit.broadcastMessage("Server restart scheduled due to a NOT-IN-GAMEGROUP error");
        Bukkit.broadcastMessage("The server will restart if 1 or less players are online");
        Bukkit.broadcastMessage(
                "If you believe you are glitched please move to another server in the network or disconnect");
    }

    @EventHandler
    public void eventPotionSplash(PotionSplashEvent event) {
        GameGroup gameGroup = getGameGroup(event.getPotion().getWorld());
        if (gameGroup == null) return;

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

        String mapName = event.getBlock().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromMapName(mapName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();
        if (!map.getWorld().getName().equals(mapName))
            throw new RuntimeException("Map still registered to old GameGroup");

        gameGroup.gameEvent(new MapBlockBreakNaturallyEvent(gameGroup, map, event));
    }

    @EventHandler
    public void eventBlockBurn(BlockBurnEvent event) {
        String mapName = event.getBlock().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromMapName(mapName);
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
        GameGroup gameGroup = game.getGameGroupFromMapName(mapName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();

        gameGroup.gameEvent(new MapBlockGrowEvent(gameGroup, map, event));
    }

    @EventHandler
    public void eventItemSpawn(ItemSpawnEvent event) {
        String mapName = event.getEntity().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromMapName(mapName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();
        if (!map.getWorld().getName().equals(mapName))
            throw new RuntimeException("Map still registered to old GameGroup");

        gameGroup.gameEvent(new MapItemSpawnEvent(gameGroup, map, event));
    }

    @EventHandler
    public void eventCreatureSpawn(CreatureSpawnEvent event) {
        String mapName = event.getEntity().getWorld().getName();
        GameGroup gameGroup = game.getGameGroupFromMapName(mapName);
        if (gameGroup == null) return;

        GameMap map = gameGroup.getCurrentMap();
        if (!map.getWorld().getName().equals(mapName))
            throw new RuntimeException("Map still registered to old GameGroup");

        gameGroup.gameEvent(new MapCreatureSpawnEvent(gameGroup, map, event));
    }

    @EventHandler
    public void eventPlayerDropItem(PlayerDropItemEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        user.getGameGroup().userEvent(new UserDropItemEvent(user, event));
    }

    @EventHandler
    public void eventPlayerPickupItem(PlayerPickupItemEvent event) {
        User user = game.getUser(event.getPlayer());
        if (user == null) {
            notInGameGroupError(event.getPlayer());
            return;
        }

        user.getGameGroup().userEvent(new UserPickupItemEvent(user, event));
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
                attacker = EntityUtils.getRepresentingUser(gameGroup, ((EntityDamageByEntityEvent) event).getDamager());
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

        //Skip playing the sound if you take no damage
        if (event.getFinalDamage() < 0.01) {
            return;
        }

        if (attacked.getHealth() - event.getFinalDamage() > 0.01) {
            if (attacker != null) attacked.setLastAttacker(attacker);

            if (attacked.isCloaked() && attacked.isInGame()) {
                attacked.getLocation().getWorld()
                        .playSound(attacked.getLocation(), NamedSounds.fromName("ENTITY_PLAYER_HURT"), 1.0f, 1.0f);
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

        //Log death messages
        if (attacker != null) {
            System.out.println(attacked.getName() + " died: reason=" + event.getCause() + ", finalDamage=" +
                                       event.getFinalDamage() + ", damage=" + event.getDamage() + ", attacker=" +
                                       attacker.getName() + ", holding=" + attacker.getInventory().getItemInHand());
        } else {
            System.out.println(attacked.getName() + " died: reason=" + event.getCause() + ", finalDamage=" +
                                       event.getFinalDamage() + ", attacker=null");
        }

        if (!deathEvent.getPlayDeathSound() || !attacked.isInGame()) return;

        attacked.getLocation().getWorld()
                .playSound(attacked.getLocation(), EntityUtils.getDeathSound(attacked.getVisibleEntityType()), 1.0f,
                           1.0f);
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
}
