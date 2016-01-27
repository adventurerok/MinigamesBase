package com.ithinkrok.minigames.base.inventory;

import com.ithinkrok.minigames.base.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.base.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.base.item.Identifiable;
import com.ithinkrok.minigames.base.util.InventoryUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 02/01/16.
 */
public abstract class ClickableItem implements Identifiable {

    private static int clickableItemCount = 0;

    protected ItemStack baseDisplay;
    private int identifier = clickableItemCount++;

    public ClickableItem(ItemStack baseDisplay) {
        if(baseDisplay != null) this.baseDisplay = InventoryUtils.addIdentifier(baseDisplay.clone(), identifier);
    }

    public void configure(ConfigurationSection config) {}

    public int getIdentifier() {
        return identifier;
    }

    public ItemStack getBaseDisplayStack() {
        return baseDisplay;
    }

    public  void onCalculateItem(CalculateItemForUserEvent event) {
        //Does nothing by default as the event uses item.getBaseDisplayStack() by default
    }

    public abstract void onClick(UserClickItemEvent event);
}
