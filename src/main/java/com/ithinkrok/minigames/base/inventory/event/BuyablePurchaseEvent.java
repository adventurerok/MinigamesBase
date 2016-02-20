package com.ithinkrok.minigames.base.inventory.event;

import com.ithinkrok.minigames.api.User;
import com.ithinkrok.minigames.base.event.user.UserEvent;
import com.ithinkrok.minigames.base.inventory.Buyable;
import com.ithinkrok.minigames.base.inventory.ClickableInventory;

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
