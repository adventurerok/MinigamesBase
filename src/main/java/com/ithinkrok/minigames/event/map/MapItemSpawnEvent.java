package com.ithinkrok.minigames.event.map;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.map.GameMap;
import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.ItemSpawnEvent;

/**
 * Created by paul on 05/01/16.
 */
public class MapItemSpawnEvent extends MapEvent implements Cancellable {

    private final ItemSpawnEvent event;

    public MapItemSpawnEvent(GameGroup gameGroup, GameMap map, ItemSpawnEvent event) {
        super(gameGroup, map);
        this.event = event;
    }

    public Item getItem() {
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
