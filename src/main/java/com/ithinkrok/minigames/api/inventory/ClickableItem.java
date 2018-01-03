package com.ithinkrok.minigames.api.inventory;

import com.ithinkrok.minigames.api.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.item.Identifiable;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 02/01/16.
 */
public abstract class ClickableItem {

    private static int clickableItemCount = 0;

    protected ItemStack baseDisplay;
    private final String identifier = Integer.toString(clickableItemCount++);

    private final int slot;

    public ClickableItem(ItemStack baseDisplay) {
        this(baseDisplay, -1);
    }

    public ClickableItem(ItemStack baseDisplay, int slot) {
        this.slot = slot;
        if(baseDisplay != null) this.baseDisplay = InventoryUtils.addIdentifier(baseDisplay.clone(), identifier);
    }

    public int getSlot() {
        return slot;
    }

    public void configure(Config config) {}

    public String getIdentifier() {
        return identifier;
    }

    public ItemStack getBaseDisplayStack() {
        return baseDisplay;
    }

    public void onCalculateItem(CalculateItemForUserEvent event) {
        //Does nothing by default as the event uses item.getBaseDisplayStack() by default
    }

    public abstract void onClick(UserClickItemEvent event);
}
