package com.ithinkrok.minigames.api.event.user.inventory;

import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 21/12/16.
 */
public class UserItemConsumeEvent extends BaseUserEvent implements Cancellable {

    private final PlayerItemConsumeEvent event;

    public UserItemConsumeEvent(User user, PlayerItemConsumeEvent event) {
        super(user);
        this.event = event;
    }

    public ItemStack getItem() {
        return event.getItem();
    }

    public void setItem(ItemStack item) {
        event.setItem(item);
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
