package com.ithinkrok.minigames.api.event.user.inventory;

import com.ithinkrok.minigames.api.user.User;
import org.bukkit.inventory.Inventory;

/**
 * Called when the user's inventory is changed, including when the hotbar is scrolled
 */
public class UserInventoryUpdateEvent extends UserInventoryEvent {

    public UserInventoryUpdateEvent(User user) {
        super(user);
    }

    @Override
    public Inventory getInventory() {
        return getUser().getInventory();
    }
}
