package com.ithinkrok.minigames.base.inventory.event;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.inventory.ClickableItem;
import com.ithinkrok.minigames.base.event.user.UserEvent;
import com.ithinkrok.minigames.base.inventory.ClickableInventory;
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