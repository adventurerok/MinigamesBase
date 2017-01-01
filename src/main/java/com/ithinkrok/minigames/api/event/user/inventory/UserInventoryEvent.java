package com.ithinkrok.minigames.api.event.user.inventory;

import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.user.User;
import org.bukkit.inventory.Inventory;

/**
 * Created by paul on 02/01/16.
 */
public abstract class UserInventoryEvent extends BaseUserEvent {


    public UserInventoryEvent(User user) {
        super(user);
    }

    public abstract Inventory getInventory();
}
