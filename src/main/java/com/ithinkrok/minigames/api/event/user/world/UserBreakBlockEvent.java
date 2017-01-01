package com.ithinkrok.minigames.api.event.user.world;

import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Created by paul on 31/12/15.
 */
public class UserBreakBlockEvent extends BaseUserEvent implements Cancellable {

    private final BlockBreakEvent event;

    public UserBreakBlockEvent(User user, BlockBreakEvent event) {
        super(user);
        this.event = event;
    }

    public Block getBlock(){
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
