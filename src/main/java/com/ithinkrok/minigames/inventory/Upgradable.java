package com.ithinkrok.minigames.inventory;

import com.ithinkrok.minigames.inventory.event.BuyablePurchaseEvent;
import com.ithinkrok.minigames.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.util.InventoryUtils;
import com.ithinkrok.minigames.util.math.MapVariables;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 14/01/16.
 */
public class Upgradable extends Buyable {

    private String upgradeName;
    private String upgradeDisplayLang;
    private int minLevel, maxLevel;
    private String customItem;
    private boolean giveItem = true;

    public Upgradable(ItemStack baseDisplay) {
        super(baseDisplay);
    }

    @Override
    public void configure(ConfigurationSection config) {
        super.configure(config);
        upgradeName = config.getString("upgrade_name");
        upgradeDisplayLang = config.getString("upgrade_display_locale");

        minLevel = config.getInt("min_level", 1);
        maxLevel = config.getInt("max_level", Integer.MAX_VALUE);

        customItem = config.getString("upgrade_item", null);
        giveItem = config.getBoolean("give_upgrade_item", true);
    }

    @Override
    public void onCalculateItem(CalculateItemForUserEvent event) {
        int nextLevel = event.getUser().getUpgradeLevel(upgradeName) + 1;
        if (nextLevel > maxLevel) {
            event.setDisplay(null);
            return;
        }

        ItemStack display = event.getDisplay();

        if (customItem != null) {
            MapVariables variables = new MapVariables();
            variables.setVariable(upgradeName, nextLevel);
            display = event.getUserGameGroup().getCustomItem(customItem)
                    .createWithVariables(event.getUser().getLanguageLookup(), variables);

            display = InventoryUtils.removeIdentifier(display);
        }

        if (upgradeDisplayLang != null) {
            InventoryUtils
                    .setItemName(display, event.getUser().getLanguageLookup().getLocale(upgradeDisplayLang, nextLevel));
        }

        event.setDisplay(display);
        super.onCalculateItem(event);
    }


    @Override
    public boolean canBuy(BuyablePurchaseEvent event) {
        if(!super.canBuy(event)) return false;
        int level = event.getUser().getUpgradeLevel(upgradeName) + 1;

        return minLevel <= level && maxLevel >= level;
    }

    @Override
    public boolean onPurchase(BuyablePurchaseEvent event) {
        event.getUser().setUpgradeLevel(upgradeName, event.getUser().getUpgradeLevel(upgradeName) + 1);

        if (customItem != null && giveItem) {
            CustomItem cust = event.getUserGameGroup().getCustomItem(customItem);
            boolean found = false;

            for (ItemStack item : event.getUser().getInventory()) {
                if (InventoryUtils.getIdentifier(item) == cust.getIdentifier()) {
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
