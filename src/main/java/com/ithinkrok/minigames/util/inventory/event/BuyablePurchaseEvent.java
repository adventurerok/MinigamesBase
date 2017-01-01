package com.ithinkrok.minigames.util.inventory.event;

import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.util.inventory.Buyable;

/**
 * Created by paul on 08/01/16.
 */
public class BuyablePurchaseEvent extends BaseUserEvent {

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
