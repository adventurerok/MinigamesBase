package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBurnEvent;

/**
 * Created by paul on 17/01/16.
 */
public class MapBlockBurnEvent extends BaseMapEvent implements Cancellable {

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
