package com.ithinkrok.minigames.base.event.user.world;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.user.UserEvent;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Created by paul on 02/01/16.
 */
public class UserPickupItemEvent extends UserEvent implements Cancellable {

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
