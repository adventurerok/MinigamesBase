package com.ithinkrok.minigames.inventory.event;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.inventory.ClickableInventory;
import com.ithinkrok.minigames.inventory.ClickableItem;
import org.bukkit.event.inventory.ClickType;

/**
 * Created by paul on 02/01/16.
 *
 * Called when a user clicks on a ClickableItem in an inventory
 */
public class UserClickItemEvent extends UserEvent {

    private final ClickableInventory inventory;
    private final ClickableItem clicked;
    private final ClickType clickType;

    public UserClickItemEvent(User user, ClickableInventory inventory, ClickableItem clicked, ClickType clickType) {
        super(user);
        this.inventory = inventory;
        this.clicked = clicked;
        this.clickType = clickType;
    }

    public ClickableInventory getInventory() {
        return inventory;
    }

    public ClickableItem getClickedItem() {
        return clicked;
    }

    public ClickType getClickType() {
        return clickType;
    }
}
