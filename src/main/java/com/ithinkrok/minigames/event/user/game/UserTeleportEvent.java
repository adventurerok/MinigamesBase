package com.ithinkrok.minigames.event.user.game;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;

/**
 * Created by paul on 01/01/16.
 */
public class UserTeleportEvent extends UserEvent implements Cancellable{

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
