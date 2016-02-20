package com.ithinkrok.minigames.base.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.base.map.GameMap;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Created by paul on 14/01/16.
 */
public class MapCreatureSpawnEvent extends MapEvent implements Cancellable {

    private final CreatureSpawnEvent event;

    public MapCreatureSpawnEvent(GameGroup gameGroup, GameMap map, CreatureSpawnEvent event) {
        super(gameGroup, map);
        this.event = event;
    }

    public LivingEntity getEntity() {
        return event.getEntity();
    }

    public CreatureSpawnEvent.SpawnReason getSpawnReason() {
        return event.getSpawnReason();
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }
}
