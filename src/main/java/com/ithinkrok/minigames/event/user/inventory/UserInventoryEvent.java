package com.ithinkrok.minigames.event.user.inventory;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/**
 * Created by paul on 02/01/16.
 */
public abstract class UserInventoryEvent extends UserEvent {


    public UserInventoryEvent(User user) {
        super(user);
    }

    public abstract Inventory getInventory();
    public abstract InventoryView getInventoryView();
}
