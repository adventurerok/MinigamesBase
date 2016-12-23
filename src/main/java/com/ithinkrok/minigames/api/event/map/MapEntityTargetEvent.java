package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * Created by paul on 23/12/16.
 */
public class MapEntityTargetEvent extends MapEvent implements Cancellable {


    private final EntityTargetEvent event;

    public MapEntityTargetEvent(GameGroup gameGroup, GameMap map, EntityTargetEvent event) {
        super(gameGroup, map);
        this.event = event;
    }

    public Entity getTarget() {
        return event.getTarget();
    }

    public Entity getEntity() {
        return event.getEntity();
    }

    public EntityTargetEvent.TargetReason getTargetReason() {
        return event.getReason();
    }

    public void setTarget(Entity target) {
        event.setTarget(target);
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
