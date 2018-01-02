package com.ithinkrok.minigames.util.map;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.state.UserDamagedEvent;
import com.ithinkrok.minigames.api.event.user.state.UserDeathEvent;
import com.ithinkrok.minigames.api.map.MapPoint;
import com.ithinkrok.minigames.api.util.BoundingBox;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.msm.bukkit.util.BukkitConfigUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by paul on 15/12/16.
 */
public class RespawnZoneListener implements CustomListener {

    private final List<RespawnZone> zones = new ArrayList<>();

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfigOrEmpty();

        List<Config> zones = config.getConfigList("zones");

        for (Config zoneConfig : zones) {
            this.zones.add(new RespawnZone(zoneConfig));
        }
    }

    @CustomEventHandler
    public void onUserDamaged(UserDamagedEvent event) {
        for (RespawnZone zone : zones) {
            if (!zone.bounds.containsLocation(event.getUser().getLocation())) continue;

            if (!zone.damageCauses.contains(event.getDamageCause())) continue;

            if(event.isCancelled() && !zone.doCancelledDamage) continue;

            event.getUser().teleport(zone.respawn);

            //Reset stats and do it again in future to prevent fire
            if(zone.resetStats) {
                event.getUser().resetUserStats(true);

                event.getUser().doInFuture(task -> {
                    event.getUser().resetUserStats(true);
                });
            }
        }
    }

    @CustomEventHandler
    public void onUserDeath(UserDeathEvent event) {
        for (RespawnZone zone : zones) {
            if (!zone.bounds.containsLocation(event.getUser().getLocation())) continue;

            if (!zone.doDeath) continue;

            event.getUser().teleport(zone.respawn);

            if(zone.resetStats) {
                event.getUser().resetUserStats(true);

                event.getUser().doInFuture(task -> {
                    event.getUser().resetUserStats(true);
                });
            }

            event.setCancelled(true);
        }
    }

    private class RespawnZone {

        private final BoundingBox bounds;

        private final Set<DamageCause> damageCauses = new HashSet<>();

        private final boolean doDeath;

        private final boolean doCancelledDamage;

        private final MapPoint respawn;

        private final boolean resetStats;

        public RespawnZone(Config config) {
            bounds = MinigamesConfigs.getBounds(config, "bounds");

            if (config.contains("damage_causes")) {
                for (String cause : config.getStringList("damage_causes")) {
                    DamageCause damageCause = DamageCause.valueOf(cause.toUpperCase());

                    damageCauses.add(damageCause);
                }
            }

            respawn = MinigamesConfigs.getMapPoint(config, "respawn");

            doDeath = config.getBoolean("do_death", true);

            doCancelledDamage = config.getBoolean("do_cancelled_damage", true);

            resetStats = config.getBoolean("reset_stats", true);
        }
    }

}
