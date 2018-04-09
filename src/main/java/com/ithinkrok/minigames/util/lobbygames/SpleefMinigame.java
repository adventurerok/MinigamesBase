package com.ithinkrok.minigames.util.lobbygames;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserTeleportEvent;
import com.ithinkrok.minigames.api.event.user.state.UserDamagedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserBreakBlockEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractWorldEvent;
import com.ithinkrok.minigames.api.map.MapPoint;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.BoundingBox;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by paul on 01/01/16.
 */
public class SpleefMinigame implements CustomListener {

    private Material spadeMaterial;

    private final Map<MapPoint, Arena> queueButtons = new HashMap<>();
    private final Map<UUID, Arena> queueLookups = new HashMap<>();
    private final Map<UUID, Arena> gameLookups = new HashMap<>();

    @CustomEventHandler
    public void configure(ListenerLoadedEvent<?, ?> event) {
        spadeMaterial = Material.matchMaterial(event.getConfig().getString("spade", "IRON_SPADE"));

        Config arenasConfig = event.getConfig().getConfigOrNull("arenas");
        for(String key : arenasConfig.getKeys(false)){
            Config arenaConfig = arenasConfig.getConfigOrNull(key);

            Arena arena = new Arena(arenaConfig);

            for(MapPoint button : arena.queueButtons){
                queueButtons.put(button, arena);
            }
        }
    }

    @CustomEventHandler
    public void onUserTeleport(UserTeleportEvent event) {
        Arena arena = gameLookups.get(event.getUser().getUuid());

        if(arena == null) return;

        double x = event.getUser().getLocation().getX();
        double z = event.getUser().getLocation().getZ();

        if(arena.checkUserInBounds(x, z)) return;

        arena.spleefUserKilled(event.getUser(), false);
    }

    @CustomEventHandler(priority = CustomEventHandler.HIGH)
    public void onUserBreakBlock(UserBreakBlockEvent event){
        if(event.getBlock().getType() == Material.SNOW_BLOCK) event.setCancelled(false);
    }

    @CustomEventHandler
    public void onUserDamaged(UserDamagedEvent event) {
        if(event.getDamageCause() != EntityDamageEvent.DamageCause.LAVA) return;

        Arena arena = gameLookups.get(event.getUser().getUuid());
        if(arena == null) return;

        event.setCancelled(true);

        arena.spleefUserKilled(event.getUser(), true);
    }

    @CustomEventHandler(priority = CustomEventHandler.HIGH)
    public void onUserInteractWorld(UserInteractWorldEvent event) {
        if(!event.hasBlock()) return;

        switch(event.getClickedBlock().getType()) {
            case SNOW_BLOCK:
                if(event.getInteractType() == UserInteractEvent.InteractType.LEFT_CLICK) event.setCancelled(false);
                return;
            case STONE_BUTTON:
                if(event.getInteractType() != UserInteractEvent.InteractType.RIGHT_CLICK) return;

                MapPoint point = event.getUser().getMap().getMapPoint(event.getClickedBlock().getLocation());
                Arena arena = queueButtons.get(point);
                if(arena == null) return;

                arena.addUserToQueue(event.getUser());
        }
    }

    private class Arena {
        private final List<MapPoint> queueButtons;
        private final List<MapPoint> spawnLocations;
        private final MapPoint exitLocation;
        private final BoundingBox snowBounds;
        private final int extraRadius;

        private final List<UUID> usersInSpleef = new ArrayList<>();
        private final LinkedHashSet<UUID> queue = new LinkedHashSet<>();

        public Arena(Config config) {
            queueButtons = MinigamesConfigs.getMapPointList(config, "queue_buttons");
            spawnLocations = MinigamesConfigs.getMapPointList(config, "spawn_locations");
            exitLocation = MinigamesConfigs.getMapPoint(config, "exit_location");
            snowBounds = MinigamesConfigs.getBounds(config, "snow");
            extraRadius = config.getInt("extra_radius");
        }

        public boolean checkUserInBounds(double x, double z) {
            if(x + extraRadius < snowBounds.getMin().getX() || x - extraRadius > snowBounds.getMax().getX()) return false;

            return z + extraRadius >= snowBounds.getMin().getZ() && z - extraRadius <= snowBounds.getMax().getZ();
        }

        public void spleefUserKilled(User user, boolean teleport) {
            if(!usersInSpleef.remove(user.getUuid())) return;
            gameLookups.remove(user.getUuid());

            removeUserFromSpleef(user, teleport);
            user.getGameGroup().sendLocale("spleef.loser", user.getFormattedName());

            if(usersInSpleef.size() == 1) {
                User winner = user.getUser(usersInSpleef.remove(0));
                gameLookups.remove(winner.getUuid());

                removeUserFromSpleef(winner, true);
                user.getGameGroup().sendLocale("spleef.winner", winner.getFormattedName());

                tryStartGame(user);
            }

        }

        public void resetArena(User aUser) {
            World world = aUser.getLocation().getWorld();

            for(int x = snowBounds.getMin().getBlockX(); x <= snowBounds.getMax().getBlockX(); ++x) {
                for(int y = snowBounds.getMin().getBlockY(); y <= snowBounds.getMax().getBlockY(); ++y) {
                    for(int z = snowBounds.getMin().getBlockZ(); z <= snowBounds.getMax().getBlockZ(); ++z) {
                        world.getBlockAt(x, y, z).setType(Material.SNOW_BLOCK);
                    }
                }
            }
        }

        public void addUserToQueue(User user) {
            if(gameLookups.containsKey(user.getUuid())) return;

            boolean success = queue.add(user.getUuid());
            if(!success){
                user.sendLocale("spleef.queue.already_joined");
                return;
            }

            Arena old = queueLookups.get(user.getUuid());
            if(old != null){
                old.queue.remove(user.getUuid());
                user.sendLocale("spleef.queue.remove");
            }

            queueLookups.put(user.getUuid(), this);

            user.sendLocale("spleef.queue.join");

            tryStartGame(user);
        }

        public void tryStartGame(User aUser) {
            if(!usersInSpleef.isEmpty() || queue.size() < spawnLocations.size()) return;

            resetArena(aUser);

            Iterator<UUID> iterator = queue.iterator();
            for(MapPoint spawn : spawnLocations) {
                UUID joiningUUID = iterator.next();
                iterator.remove();

                queueLookups.remove(joiningUUID);

                User joining = aUser.getUser(joiningUUID);

                joining.teleport(spawn);
                joining.getInventory().addItem(new ItemStack(spadeMaterial));
                joining.setGameMode(GameMode.SURVIVAL);
                usersInSpleef.add(joining.getUuid());
                gameLookups.put(joining.getUuid(), this);
            }

            aUser.getGameGroup().sendLocale("spleef.begin");
        }

        @SuppressWarnings("unchecked")
        private void removeUserFromSpleef(User user, boolean teleport) {
            if(teleport) user.teleport(exitLocation);

            user.getInventory().remove(spadeMaterial);

            user.setGameMode(GameMode.ADVENTURE);

            user.doInFuture(task -> user.setFireTicks(null, 0));
        }
    }
}
