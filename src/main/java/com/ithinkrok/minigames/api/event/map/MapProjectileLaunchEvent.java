package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileLaunchEvent;

/**
 * Created by paul on 01/01/17.
 */
public class MapProjectileLaunchEvent extends MapEvent implements Cancellable {

    private final ProjectileLaunchEvent event;

    public MapProjectileLaunchEvent(GameGroup gameGroup, GameMap map, ProjectileLaunchEvent event) {
        super(gameGroup, map);
        this.event = event;
    }


    Projectile getEntity() {
        return event.getEntity();
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
