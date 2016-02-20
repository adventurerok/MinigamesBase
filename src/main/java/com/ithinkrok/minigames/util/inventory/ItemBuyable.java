package com.ithinkrok.minigames.util.inventory;

import com.ithinkrok.minigames.api.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.minigames.api.util.math.Calculator;
import com.ithinkrok.minigames.api.util.math.ExpressionCalculator;
import com.ithinkrok.minigames.util.inventory.event.BuyablePurchaseEvent;
import com.ithinkrok.util.config.Config;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

/**
 * Created by paul on 10/01/16.
 */
public class ItemBuyable extends Buyable {

    protected ItemStack purchase;
    protected String customItem;
    private Calculator amount;

    private String noSpaceLocale;

    public ItemBuyable(ItemStack baseDisplay) {
        super(baseDisplay);
    }

    @Override
    public void configure(Config config) {
        super.configure(config);

        ItemStack purchase = MinigamesConfigs.getItemStack(config, "item");

        if (purchase == null && config.contains("custom_item")) {
            customItem = config.getString("custom_item");
            amount = new ExpressionCalculator(config.getString("amount", "1"));
        } else if (purchase != null) {
            this.purchase = purchase.clone();
            if (baseDisplay == null) baseDisplay = purchase.clone();
        }

        noSpaceLocale = config.getString("no_inventory_space_locale", "item_buyable.no_space");
    }

    @Override
    public void onCalculateItem(CalculateItemForUserEvent event) {
        if (event.getDisplay() == null && customItem != null) {
            ItemStack item = event.getUserGameGroup().getCustomItem(customItem).createForUser(event.getUser());

            item.setAmount((int) (item.getAmount() * amount.calculate(event.getUser().getUpgradeLevels())));

            item = InventoryUtils.removeIdentifier(item);

            event.setDisplay(item);
        }

        super.onCalculateItem(event);
    }

    @Override
    public boolean onPurchase(BuyablePurchaseEvent event) {
        if (customItem == null) return giveUserItem(event.getUser(), purchase);
        else {
            CustomItem customItem = event.getUserGameGroup().getCustomItem(this.customItem);
            ItemStack item = customItem.createForUser(event.getUser());

            item.setAmount((int) (item.getAmount() * amount.calculate(event.getUser().getUpgradeLevels())));

            return giveUserItem(event.getUser(), item);
        }
    }

    protected boolean giveUserItem(User user, ItemStack purchase) {
        PlayerInventory inv = user.getInventory();

        Map<Integer, ItemStack> failedItems = inv.addItem(purchase.clone());
        if (failedItems.isEmpty()) return true;

        int removeAmount = purchase.getAmount() - failedItems.get(0).getAmount();

        if (removeAmount != 0) {
            ItemStack remove = purchase.clone();
            remove.setAmount(removeAmount);

            inv.removeItem(remove);
        }

        user.sendLocale(noSpaceLocale);
        return false;
    }
}
