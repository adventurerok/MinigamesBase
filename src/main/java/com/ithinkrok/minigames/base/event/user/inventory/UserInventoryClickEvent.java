package com.ithinkrok.minigames.base.event.user.inventory;

import com.ithinkrok.minigames.base.User;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 02/01/16.
 */
public class UserInventoryClickEvent extends UserInventoryEvent implements Cancellable{

    private final InventoryClickEvent event;

    public UserInventoryClickEvent(User user, InventoryClickEvent event) {
        super(user);
        this.event = event;
    }

    public ItemStack getItemOnCursor() {
        return event.getCursor();
    }

    public ItemStack getItemInSlot() {
        return event.getCurrentItem();
    }

    public InventoryType.SlotType getSlotType() {
        return event.getSlotType();
    }

    public ClickType getClickType() {
        return event.getClick();
    }

    public InventoryAction getAction() {
        return event.getAction();
    }

    @Override
    public Inventory getInventory() {
        return event.getInventory();
    }

    @Override
    public InventoryView getInventoryView() {
        return event.getView();
    }

    public int getSlot() {
        return event.getSlot();
    }

    public int getRawSlot() {
        return event.getRawSlot();
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }
}
