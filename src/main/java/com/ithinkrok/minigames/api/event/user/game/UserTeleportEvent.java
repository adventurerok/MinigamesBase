package com.ithinkrok.minigames.api.event.user.game;

import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.Location;

/**
 * Created by paul on 01/01/16.
 */
public class UserTeleportEvent extends BaseUserEvent implements Cancellable {

    private final Location from;
    private Location to;

    private boolean cancelled = false;

    public UserTeleportEvent(User user, Location from, Location to) {
        super(user);
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public void setTo(Location to) {
        this.to = to;
    }
}
