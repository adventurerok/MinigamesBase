package com.ithinkrok.minigames.api.event.user.world;

import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.block.Block;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Created by paul on 20/02/16.
 */
public class UserEditSignEvent extends BaseUserEvent implements Cancellable {

    private final SignChangeEvent event;

    public UserEditSignEvent(User user, SignChangeEvent event) {
        super(user);
        this.event = event;
    }


    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }

    public String[] getLines() {
        return event.getLines();
    }

    public String getLine(int index) {
        return event.getLine(index);
    }

    public void setLine(int index, String line) {
        event.setLine(index, line);
    }

    public Block getBlock() {
        return event.getBlock();
    }
}
