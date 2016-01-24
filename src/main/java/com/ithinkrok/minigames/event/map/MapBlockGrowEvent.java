package com.ithinkrok.minigames.event.map;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.map.GameMap;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;

/**
 * Created by paul on 17/01/16.
 */
public class MapBlockGrowEvent extends MapEvent implements Cancellable {

    private final BlockGrowEvent event;

    public MapBlockGrowEvent(GameGroup gameGroup, GameMap map, BlockGrowEvent event) {
        super(gameGroup, map);
        this.event = event;
    }

    public Block getBlock() {
        return event.getBlock();
    }

    public BlockState getNewState() {
        return event.getNewState();
    }

    public boolean isFormEvent() {
        return event instanceof BlockFormEvent;
    }

    public boolean isSpreadEvent() {
        return event instanceof BlockSpreadEvent;
    }

    public Block getSpreadSource() {
        if(!isSpreadEvent()) return null;
        return ((BlockSpreadEvent)event).getSource();
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
