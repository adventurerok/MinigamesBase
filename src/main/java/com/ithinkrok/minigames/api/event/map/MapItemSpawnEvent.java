package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.ItemSpawnEvent;

/**
 * Created by paul on 05/01/16.
 */
public class MapItemSpawnEvent extends BaseMapEvent implements Cancellable {

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
