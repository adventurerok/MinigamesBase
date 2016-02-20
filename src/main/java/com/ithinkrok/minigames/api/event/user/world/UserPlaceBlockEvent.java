package com.ithinkrok.minigames.api.event.user.world;

import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.block.Block;
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
