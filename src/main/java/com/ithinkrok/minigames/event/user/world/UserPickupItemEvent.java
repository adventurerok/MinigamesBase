package com.ithinkrok.minigames.event.user.world;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Created by paul on 02/01/16.
 */
public class UserPickupItemEvent extends UserEvent implements Cancellable{

    private final PlayerPickupItemEvent event;

    public UserPickupItemEvent(User user, PlayerPickupItemEvent event) {
        super(user);
        this.event = event;
    }

    public Item getItem() {
        return event.getItem();
    }

    public int getTicksBeforeDespawn() {
        return event.getRemaining();
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
