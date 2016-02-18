package com.ithinkrok.minigames.base.event.user.world;

import com.ithinkrok.minigames.base.User;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 02/01/16.
 */
public class UserRightClickEntityEvent extends UserInteractEvent {

    private final PlayerInteractEntityEvent event;

    public UserRightClickEntityEvent(User user, PlayerInteractEntityEvent event) {
        super(user);
        this.event = event;
    }

    @Override
    public Block getClickedBlock() {
        return null;
    }

    @Override
    public Entity getClickedEntity() {
        return event.getRightClicked();
    }

    @Override
    public InteractType getInteractType() {
        return InteractType.RIGHT_CLICK;
    }

    @Override
    public BlockFace getBlockFace() {
        return null;
    }

    @Override
    public ItemStack getItem() {
        return getUser().getInventory().getItemInHand();
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean b) {
        event.setCancelled(b);
    }
}

