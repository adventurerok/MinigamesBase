package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockExpEvent;

/**
 * Created by paul on 05/01/16.
 *
 * Called when a block is broken naturally (i.e. not through a user breaking it).
 * This includes explosions, leaves decaying, etc...
 */
public class MapBlockBreakNaturallyEvent extends BaseMapEvent {

    private final BlockExpEvent event;

    public MapBlockBreakNaturallyEvent(GameGroup gameGroup, GameMap map, BlockExpEvent event) {
        super(gameGroup, map);
        this.event = event;
    }

    public Block getBlock() {
        return event.getBlock();
    }
}
