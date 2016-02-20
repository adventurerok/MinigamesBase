package com.ithinkrok.minigames.api.event.user.inventory;

import com.ithinkrok.minigames.api.User;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/**
 * Created by paul on 02/01/16.
 */
public class UserInventoryCloseEvent extends UserInventoryEvent {

    private final InventoryCloseEvent event;

    public UserInventoryCloseEvent(User user, InventoryCloseEvent event) {
        super(user);
        this.event = event;
    }

    @Override
    public Inventory getInventory() {
        return event.getInventory();
    }

    @Override
    public InventoryView getInventoryView() {
        return event.getView();
    }
}
