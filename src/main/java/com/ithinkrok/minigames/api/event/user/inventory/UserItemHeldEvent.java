package com.ithinkrok.minigames.api.event.user.inventory;

import com.ithinkrok.minigames.api.user.User;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 08/12/16.
 */
public class UserItemHeldEvent extends UserInventoryEvent {

    private final PlayerItemHeldEvent event;

    public UserItemHeldEvent(User user, PlayerItemHeldEvent event) {
        super(user);
        this.event = event;
    }

    @Override
    public Inventory getInventory() {
        return getUser().getInventory();
    }

    public int getNewSlot() {
        return event.getNewSlot();
    }

    public int getOldSlot() {
        return event.getPreviousSlot();
    }

    public ItemStack getNewHeldItem() {
        return getInventory().getItem(getNewSlot());
    }

    public ItemStack getOldHeldItem() {
        return getInventory().getItem(getOldSlot());
    }

}
