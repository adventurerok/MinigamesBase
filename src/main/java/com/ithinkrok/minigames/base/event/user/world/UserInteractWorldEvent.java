package com.ithinkrok.minigames.base.event.user.world;

import com.ithinkrok.minigames.api.User;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 01/01/16.
 */
public class UserInteractWorldEvent extends UserInteractEvent {

    private final PlayerInteractEvent event;

    public UserInteractWorldEvent(User user, PlayerInteractEvent event) {
        super(user);
        this.event = event;
    }

    @Override
    public Block getClickedBlock() {
        return event.getClickedBlock();
    }

    @Override
    public Entity getClickedEntity() {
        return null;
    }

    @Override
    public InteractType getInteractType() {
        switch(event.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                return InteractType.LEFT_CLICK;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                return InteractType.RIGHT_CLICK;
            case PHYSICAL:
                return InteractType.PHYSICAL;
            default:
                throw new UnsupportedOperationException("Bukkit added a new PlayerInteractEvent Action");
        }
    }

    @Override
    public BlockFace getBlockFace() {
        return event.getBlockFace();
    }

    @Override
    public ItemStack getItem() {
        return event.getItem();
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
