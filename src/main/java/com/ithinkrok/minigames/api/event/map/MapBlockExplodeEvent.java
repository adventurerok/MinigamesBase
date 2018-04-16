package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.List;

public class MapBlockExplodeEvent extends BaseMapEvent implements Cancellable {

    private final BlockExplodeEvent event;


    public MapBlockExplodeEvent(GameGroup gameGroup, GameMap map, BlockExplodeEvent event) {
        super(gameGroup, map);
        this.event = event;
    }

    public Block getBlock() {
        return event.getBlock();
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


    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }


    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }
}
