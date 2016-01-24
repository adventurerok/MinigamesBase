package com.ithinkrok.minigames.inventory.event;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.inventory.Buyable;
import com.ithinkrok.minigames.inventory.ClickableInventory;

/**
 * Created by paul on 08/01/16.
 */
public class BuyablePurchaseEvent extends UserEvent{

    private final ClickableInventory inventory;
    private final Buyable purchased;

    public BuyablePurchaseEvent(User user, ClickableInventory inventory, Buyable purchased) {
        super(user);
        this.inventory = inventory;
        this.purchased = purchased;
    }

    public Buyable getPurchased() {
        return purchased;
    }

    public ClickableInventory getInventory() {
        return inventory;
    }
}
