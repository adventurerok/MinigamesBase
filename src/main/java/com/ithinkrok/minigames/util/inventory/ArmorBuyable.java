package com.ithinkrok.minigames.util.inventory;

import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.minigames.util.inventory.event.BuyablePurchaseEvent;
import com.ithinkrok.util.config.Config;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 21/12/16.
 */
public class ArmorBuyable extends Buyable {

    protected ItemStack helmet;
    protected String customHelmet;

    protected ItemStack chestplate;
    protected String customChestplate;

    protected ItemStack leggings;
    protected String customLeggings;

    protected ItemStack boots;
    protected String customBoots;

    public ArmorBuyable(ItemStack baseDisplay, int slot) {
        super(baseDisplay, slot);
    }

    @Override
    public void configure(Config config) {
        super.configure(config);

        helmet = MinigamesConfigs.getItemStack(config, "helmet");
        chestplate = MinigamesConfigs.getItemStack(config, "chestplate");
        leggings = MinigamesConfigs.getItemStack(config, "leggings");
        boots = MinigamesConfigs.getItemStack(config, "boots");

        customHelmet = config.getString("custom_helmet", null);
        customChestplate = config.getString("custom_chestplate", null);
        customLeggings = config.getString("custom_leggings", null);
        customBoots = config.getString("custom_boots", null);

        if(baseDisplay == null) {
            if (helmet != null) {
                baseDisplay = new ItemStack(helmet);
            } else if (boots != null) {
                baseDisplay = new ItemStack(boots);
            } else if(chestplate != null) {
                baseDisplay = new ItemStack(chestplate);
            } else if(leggings != null) {
                baseDisplay = new ItemStack(leggings);
            }
        }
    }

    @Override
    public boolean onPurchase(BuyablePurchaseEvent event) {
        ItemStack helmet = this.helmet;

        if(customHelmet != null) {
            CustomItem customItem = event.getGameGroup().getCustomItem(customHelmet);
            helmet = customItem.createForUser(event.getUser());
        }

        if(helmet != null) {
            event.getUser().getInventory().setHelmet(new ItemStack(helmet));
        }


        ItemStack chestplate = this.chestplate;

        if(customChestplate != null) {
            CustomItem customItem = event.getGameGroup().getCustomItem(customChestplate);
            chestplate = customItem.createForUser(event.getUser());
        }

        if(chestplate != null) {
            event.getUser().getInventory().setChestplate(new ItemStack(chestplate));
        }


        ItemStack leggings = this.leggings;

        if(customLeggings != null) {
            CustomItem customItem = event.getGameGroup().getCustomItem(customLeggings);
            leggings = customItem.createForUser(event.getUser());
        }

        if(leggings != null) {
            event.getUser().getInventory().setLeggings(new ItemStack(leggings));
        }

        ItemStack boots = this.boots;

        if(customBoots != null) {
            CustomItem customItem = event.getGameGroup().getCustomItem(customBoots);
            boots = customItem.createForUser(event.getUser());
        }

        if(boots != null) {
            event.getUser().getInventory().setBoots(new ItemStack(boots));
        }

        return true;
    }
}
