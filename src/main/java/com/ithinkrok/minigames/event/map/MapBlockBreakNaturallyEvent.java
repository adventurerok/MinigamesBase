package com.ithinkrok.minigames.event.map;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.map.GameMap;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockExpEvent;

/**
 * Created by paul on 05/01/16.
 *
 * Called when a block is broken naturally (i.e. not through a user breaking it).
 * This includes explosions, leaves decaying, etc...
 */
public class MapBlockBreakNaturallyEvent extends MapEvent {

    private final BlockExpEvent event;

    public MapBlockBreakNaturallyEvent(GameGroup gameGroup, GameMap map, BlockExpEvent event) {
        super(gameGroup, map);
        this.event = event;
    }

    public Block getBlock() {
        return event.getBlock();
    }
}
