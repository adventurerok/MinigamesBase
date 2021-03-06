package com.ithinkrok.minigames.api.inventory.event;

import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.user.User;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 02/01/16.
 */
public class CalculateItemForUserEvent extends BaseUserEvent {

    private final ClickableInventory inventory;
    private final ClickableItem item;

    private ItemStack display;

    /**
     * @return The item to display in the inventory. Null indicates that this item should be hidden in the shop
     */
    public ItemStack getDisplay() {
        return display;
    }

    /**
     *
     * @param display The item to display in the inventory. Setting this to null will hide the item in the inventory
     */
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
