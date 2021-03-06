package com.ithinkrok.minigames.util.inventory;

import com.ithinkrok.minigames.api.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.util.math.MapVariables;
import com.ithinkrok.minigames.util.inventory.event.BuyablePurchaseEvent;
import com.ithinkrok.util.config.Config;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * Created by paul on 14/01/16.
 */
public class Upgradable extends Buyable {

    private String upgradeName;
    private String upgradeDisplayLang;
    private double minLevel, maxLevel;
    private String customItem;
    private ItemStack item;
    private boolean giveItem = true;

    public Upgradable(ItemStack baseDisplay, int slot) {
        super(baseDisplay, slot);
    }

    @Override
    public void configure(Config config) {
        super.configure(config);
        upgradeName = config.getString("upgrade_name");
        upgradeDisplayLang = config.getString("upgrade_display_locale");

        minLevel = config.getDouble("min_level", 1);
        maxLevel = config.getDouble("max_level", Integer.MAX_VALUE);

        customItem = config.getString("upgrade_item", null);
        if(customItem == null) {
            item = MinigamesConfigs.getItemStack(config, "item");
        }

        giveItem = config.getBoolean("give_upgrade_item", true);
    }

    @Override
    public void onCalculateItem(CalculateItemForUserEvent event) {
        double nextLevel = event.getUser().getUserVariable(upgradeName) + 1;
        if (nextLevel > maxLevel) {
            event.setDisplay(null);
            return;
        }

        ItemStack display = event.getDisplay();

        if (customItem != null) {
            MapVariables variables = new MapVariables();
            variables.setVariable(upgradeName, nextLevel);
            display = event.getGameGroup().getCustomItem(customItem)
                    .createWithVariables(event.getUser().getLanguageLookup(), variables);

            display = InventoryUtils.removeIdentifier(display);
        } else if(item != null) {
            display = item.clone();
        }

        if (upgradeDisplayLang != null) {
            InventoryUtils
                    .setItemName(display, event.getUser().getLanguageLookup().getLocale(upgradeDisplayLang, nextLevel));
        }

        event.setDisplay(display);
        super.onCalculateItem(event);
    }


    @Override
    public boolean isAvailable(User user) {
        if(!super.isAvailable(user)) return false;
        double level = user.getUserVariable(upgradeName) + 1;

        return minLevel <= level && maxLevel >= level;
    }

    @Override
    public boolean onPurchase(BuyablePurchaseEvent event) {
        event.getUser().setUserVariable(upgradeName, event.getUser().getUserVariable(upgradeName) + 1);

        if (customItem != null && giveItem) {
            CustomItem cust = event.getGameGroup().getCustomItem(customItem);
            boolean found = false;

            for (ItemStack item : event.getUser().getInventory()) {
                if (Objects.equals(InventoryUtils.getIdentifier(item), cust.getName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                event.getUser().getInventory().addItem(cust.createForUser(event.getUser()));
            }
        }

        return true;
    }
}
