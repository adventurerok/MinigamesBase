package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

/**
 * Created by paul on 21/12/16.
 */
public class MapEntityExplodeEvent extends MapEvent implements Cancellable {


    private final EntityExplodeEvent event;

    public MapEntityExplodeEvent(GameGroup gameGroup, GameMap map, EntityExplodeEvent event) {
        super(gameGroup, map);
        this.event = event;


    }

    public Entity getEntity() {
        return event.getEntity();
    }

    public List<Block> getBlockList() {
        return event.blockList();
    }

    public float getYield() {
        return event.getYield();
    }

    public void setYield(float yield) {
        event.setYield(yield);
    }

    public Location getLocation() {
        return event.getLocation();
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
