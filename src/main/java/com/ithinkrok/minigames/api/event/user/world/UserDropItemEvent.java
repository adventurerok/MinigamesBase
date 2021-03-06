package com.ithinkrok.minigames.api.event.user.world;

import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Created by paul on 02/01/16.
 */
public class UserDropItemEvent extends BaseUserEvent implements Cancellable {

    private final PlayerDropItemEvent event;

    public UserDropItemEvent(User user, PlayerDropItemEvent event) {
        super(user);
        this.event = event;
    }

    public Item getItem() {
        return event.getItemDrop();
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
