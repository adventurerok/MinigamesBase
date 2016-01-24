package com.ithinkrok.minigames.inventory.event;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.inventory.ClickableInventory;
import com.ithinkrok.minigames.inventory.ClickableItem;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 02/01/16.
 */
public class CalculateItemForUserEvent extends UserEvent {

    private final ClickableInventory inventory;
    private final ClickableItem item;

    private ItemStack display;

    public ItemStack getDisplay() {
        return display;
    }

    public void setDisplay(ItemStack display) {
        this.display = display;
    }

    public CalculateItemForUserEvent(User user, ClickableInventory inventory, ClickableItem item) {
        super(user);
        this.inventory = inventory;
        this.item = item;

        if(item.getBaseDisplayStack() != null){
            this.display = item.getBaseDisplayStack().clone();
        }
    }

    public ClickableInventory getInventory() {
        return inventory;
    }

    public ClickableItem getItem() {
        return item;
    }
}
