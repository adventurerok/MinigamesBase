package com.ithinkrok.minigames.event.user.world;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 31/12/15.
 */
public class UserPlaceBlockEvent extends UserEvent implements Cancellable {

    private final BlockPlaceEvent event;

    public UserPlaceBlockEvent(User user, BlockPlaceEvent event) {
        super(user);
        this.event = event;
    }

    public Block getBlock() {
        return event.getBlock();
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    public ItemStack getItemPlaced() {
        return event.getItemInHand();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }
}
