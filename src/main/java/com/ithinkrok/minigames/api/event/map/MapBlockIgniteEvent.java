package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockIgniteEvent;

/**
 * Created by paul on 21/12/16.
 */
public class MapBlockIgniteEvent extends MapEvent implements Cancellable {


    private final BlockIgniteEvent event;

    public MapBlockIgniteEvent(GameGroup gameGroup, GameMap map, BlockIgniteEvent event) {
        super(gameGroup, map);
        this.event = event;
    }

    public Block getIgnitingBlock() {
        return event.getIgnitingBlock();
    }

    public Entity getIgnitingEntity() {
        return event.getIgnitingEntity();
    }

    public User getIgnitingUser() {
        Player player = event.getPlayer();
        if(player == null) return null;

        return getGameGroup().getUser(player.getUniqueId());
    }

    /**
     * @return The block being ignited
     */
    public Block getBlock() {
        return event.getBlock();
    }

    public BlockIgniteEvent.IgniteCause getIgniteCause() {
        return event.getCause();
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
