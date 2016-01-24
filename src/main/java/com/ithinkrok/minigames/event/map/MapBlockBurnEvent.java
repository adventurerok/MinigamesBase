package com.ithinkrok.minigames.event.map;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.map.GameMap;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBurnEvent;

/**
 * Created by paul on 17/01/16.
 */
public class MapBlockBurnEvent extends MapEvent implements Cancellable {

    private final BlockBurnEvent event;

    public MapBlockBurnEvent(GameGroup gameGroup, GameMap map, BlockBurnEvent event) {
        super(gameGroup, map);
        this.event = event;
    }

    public Block getBlock() {
        return event.getBlock();
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
