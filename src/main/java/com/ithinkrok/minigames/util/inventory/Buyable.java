package com.ithinkrok.minigames.util.inventory;

import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.minigames.api.util.NamedSounds;
import com.ithinkrok.minigames.api.util.SoundEffect;
import com.ithinkrok.util.math.Calculator;
import com.ithinkrok.util.math.ExpressionCalculator;
import com.ithinkrok.minigames.util.inventory.event.BuyablePurchaseEvent;
import com.ithinkrok.minigames.util.metadata.Money;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.lang.LanguageLookup;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 08/01/16.
 */
public abstract class Buyable extends ClickableItem {

    private final Map<String, Calculator> upgradeOnBuy = new HashMap<>();
    private final Map<ItemStack, Calculator> itemCosts = new HashMap<>();
    private final Map<String, Calculator> customItemCosts = new HashMap<>();
    private String teamNoMoneyLocale;
    private String userNoMoneyLocale;
    private String cannotBuyLocale;
    private String userPayTeamLocale;
    private String teamDescriptionLocale;
    private String userDescriptionLocale;
    private String extraCostsLocale;
    private String extraCostsOnlyLocale;
    private String costsItemLocale;

    private String noItemLocale;
    private String itemsTakenLocale;

    private Calculator cost;
    private Calculator team;
    private Calculator canBuy;

    public Buyable(ItemStack baseDisplay, int slot) {
        super(baseDisplay, slot);
    }

    @Override
    public void configure(Config config) {
        cost = new ExpressionCalculator(config.getString("cost"));
        team = new ExpressionCalculator(config.getString("team", "false"));
        canBuy = new ExpressionCalculator(config.getString("can_buy", "true"));

        teamNoMoneyLocale = config.getString("team_no_money_locale", "buyable.team.no_money");
        userNoMoneyLocale = config.getString("user_no_money_locale", "buyable.user.no_money");
        cannotBuyLocale = config.getString("cannot_buy_locale", "buyable.cannot_buy");
        userPayTeamLocale = config.getString("user_pay_team_locale", "buyable.user.pay_team");
        teamDescriptionLocale = config.getString("team_description_locale", "buyable.team.description");
        userDescriptionLocale = config.getString("user_description_locale", "buyable.user.description");

        extraCostsLocale = config.getString("extra_costs_locale", "buyable.costs.extra");
        extraCostsOnlyLocale = config.getString("extra_costs_only_locale", "buyable.costs.extra_only");
        costsItemLocale = config.getString("costs_item_locale", "buyable.costs.item");
        noItemLocale = config.getString("no_item_locale", "buyable.no_item");
        itemsTakenLocale = config.getString("items_taken_locale", "buyable.items_taken");

        if (config.contains("upgrade_on_buy")) configureUpgradeOnBuy(config.getConfigOrNull("upgrade_on_buy"));

        if (config.contains("item_costs")) {
            for (Config itemConfig : config.getConfigList("item_costs")) {
                ItemStack item = MinigamesConfigs.getItemStack(itemConfig, "item");

                Calculator amount = new ExpressionCalculator(itemConfig.getString("amount", "1"));

                itemCosts.put(item, amount);
            }

        }

        if (config.contains("custom_item_costs")) {
            for (Config customItemConfig : config.getConfigList("custom_item_costs")) {
                String customItem = customItemConfig.getString("name");

                Calculator amount = new ExpressionCalculator(customItemConfig.getString("amount", "1"));

                customItemCosts.put(customItem, amount);
            }


        }
    }

    private void configureUpgradeOnBuy(Config config) {
        for (String upgrade : config.getKeys(false)) {
            upgradeOnBuy.put(upgrade, new ExpressionCalculator(config.getString(upgrade)));
        }
    }

    @Override
    public void onCalculateItem(CalculateItemForUserEvent event) {
        if (event.getDisplay() == null) return;

        if (!isAvailable(event.getUser())) {
            event.setDisplay(null);
            return;
        }

        addMoneyCostLore(event);
        addItemCostLore(event);
    }

    /**
     *
     * @param user Check if this item is available to buy for this user
     * @return If the item is available for the user
     */
    public boolean isAvailable(User user) {
        return canBuy.calculateBoolean(user.getUserVariables());
    }

    private void addMoneyCostLore(CalculateItemForUserEvent event) {
        Money userMoney = Money.getOrCreate(event.getUser());

        int cost = getCost(event.getUser());
        if (cost <= 0) return;
        boolean hasMoney = true;

        boolean team = this.team.calculateBoolean(event.getUser().getUserVariables());

        if (team) {
            Money teamMoney = Money.getOrCreate(event.getUser().getTeam());
            if (userMoney.getMoney() + teamMoney.getMoney() < cost) hasMoney = false;
        } else if (!userMoney.hasMoney(cost)) hasMoney = false;

        String costString = (hasMoney ? ChatColor.GREEN : ChatColor.RED) + Integer.toString(cost);
        LanguageLookup lookup = event.getUser().getLanguageLookup();

        ItemStack display = event.getDisplay();

        if (team) {
            display = InventoryUtils.addLore(display, lookup.getLocale(teamDescriptionLocale, costString));
        } else {
            display = InventoryUtils.addLore(display, lookup.getLocale(userDescriptionLocale, costString));
        }

        event.setDisplay(display);
    }

    private void addItemCostLore(CalculateItemForUserEvent event) {
        if (itemCosts.isEmpty() && customItemCosts.isEmpty()) return;

        ItemStack display = event.getDisplay();
        LanguageLookup lookup = event.getUser().getLanguageLookup();

        int moneyCost = getCost(event.getUser());

        if (moneyCost > 0) {
            display = InventoryUtils.addLore(display, lookup.getLocale(extraCostsLocale));
        } else {
            display = InventoryUtils.addLore(display, lookup.getLocale(extraCostsOnlyLocale));
        }

        PlayerInventory inventory = event.getUser().getInventory();

        for (Map.Entry<String, Calculator> customItemToAmount : customItemCosts.entrySet()) {
            int requiredAmount = (int) customItemToAmount.getValue().calculate(event.getUser().getUserVariables());
            if(requiredAmount <= 0) continue;

            CustomItem customItem = event.getGameGroup().getCustomItem(customItemToAmount.getKey());

            int userAmount = InventoryUtils.getAmountOfItemsWithIdentifier(inventory, customItem.getIdentifier());

            String prefix = ((userAmount >= requiredAmount) ? ChatColor.GREEN : ChatColor.RED).toString();

            String itemName = lookup.getLocale(customItem.getDisplayNameLocale());

            String lore = lookup.getLocale(costsItemLocale, prefix + requiredAmount, prefix + itemName);
            display = InventoryUtils.addLore(display, lore);
        }

        for (Map.Entry<ItemStack, Calculator> itemToAmount : itemCosts.entrySet()) {
            int requiredAmount = (int) itemToAmount.getValue().calculate(event.getUser().getUserVariables());
            if(requiredAmount <= 0) continue;

            boolean hasAmount = inventory.containsAtLeast(itemToAmount.getKey(), requiredAmount);

            String prefix = (hasAmount ? ChatColor.GREEN : ChatColor.RED).toString();

            String itemName = InventoryUtils.getItemStackDefaultName(itemToAmount.getKey());

            String lore = lookup.getLocale(costsItemLocale, prefix + requiredAmount, prefix + itemName);
            display = InventoryUtils.addLore(display, lore);
        }


        event.setDisplay(display);
    }

    public int getCost(User user) {
        return (int) cost.calculate(user.getUserVariables());
    }

    @Override
    public void onClick(UserClickItemEvent event) {
        User user = event.getUser();

        Money userMoney = Money.getOrCreate(user);
        Money teamMoney = null;

        //check if the user has the money
        int cost = getCost(user);

        boolean team = this.team.calculateBoolean(user.getUserVariables());

        if (team) {
            teamMoney = Money.getOrCreate(user.getTeam());
            if (userMoney.getMoney() + teamMoney.getMoney() < cost) {
                user.sendLocale(teamNoMoneyLocale);
                return;
            }
        } else if (!userMoney.hasMoney(cost)) {
            user.sendLocale(userNoMoneyLocale);
            return;
        }

        PlayerInventory inventory = user.getInventory();

        //We need to store the calculated amounts as if the user variables change these could change
        Map<String, Integer> customItemAmounts = new HashMap<>();

        //check if the user has the items
        for (Map.Entry<String, Calculator> customItemToAmount : customItemCosts.entrySet()) {
            int requiredAmount = (int) customItemToAmount.getValue().calculate(user.getUserVariables());
            if(requiredAmount <= 0) continue;

            customItemAmounts.put(customItemToAmount.getKey(), requiredAmount);

            CustomItem customItem = event.getGameGroup().getCustomItem(customItemToAmount.getKey());

            int userAmount = InventoryUtils.getAmountOfItemsWithIdentifier(inventory, customItem.getIdentifier());


            if (userAmount >= requiredAmount) continue;

            String itemName = user.getLanguageLookup().getLocale(customItem.getDisplayNameLocale());

            user.sendLocale(noItemLocale, requiredAmount, itemName);
            return;
        }

        Map<ItemStack, Integer> itemAmounts = new HashMap<>();

        for (Map.Entry<ItemStack, Calculator> itemToAmount : itemCosts.entrySet()) {
            int requiredAmount = (int) itemToAmount.getValue().calculate(user.getUserVariables());
            if(requiredAmount <= 0) continue;

            itemAmounts.put(itemToAmount.getKey(), requiredAmount);

            boolean hasAmount = inventory.containsAtLeast(itemToAmount.getKey(), requiredAmount);
            if (hasAmount) continue;

            String itemName = InventoryUtils.getItemStackDefaultName(itemToAmount.getKey());

            user.sendLocale(noItemLocale, requiredAmount, itemName);
            return;
        }

        //Try and buy
        BuyablePurchaseEvent purchaseEvent = new BuyablePurchaseEvent(user, event.getInventory(), this);

        if (!isAvailable(user)) {
            user.sendLocale(cannotBuyLocale);
            user.redoInventory();
            return;
        }

        if (!onPurchase(purchaseEvent)) return;

        doUpgradesOnBuy(user);

        //charge the user the money required
        if (cost > 0) {
            if (team) {
                int teamAmount = Math.min(cost, teamMoney.getMoney());
                int userAmount = cost - teamAmount;

                if (teamAmount > 0) teamMoney.subtractMoney(teamAmount, true);
                if (userAmount > 0) {
                    userMoney.subtractMoney(userAmount, true);
                    user.sendLocale(userPayTeamLocale, userAmount);
                }
            } else {
                userMoney.subtractMoney(cost, true);
            }
        }

        boolean itemsTaken = false;

        //charge the user the items required
        for (Map.Entry<String, Integer> customItemToAmount : customItemAmounts.entrySet()) {
            int requiredAmount = customItemToAmount.getValue();
            if(requiredAmount <= 0) continue;

            CustomItem customItem = event.getGameGroup().getCustomItem(customItemToAmount.getKey());

            InventoryUtils.removeItemsWithIdentifier(inventory, customItem.getIdentifier(), requiredAmount);

            itemsTaken = true;
        }

        for (Map.Entry<ItemStack, Integer> itemToAmount : itemAmounts.entrySet()) {
            ItemStack itemCost = itemToAmount.getKey().clone();

            itemCost.setAmount(itemToAmount.getValue());

            inventory.removeItem(itemCost);

            itemsTaken = true;
        }

        if(itemsTaken) {
            user.sendLocale(itemsTakenLocale);
        }

        user.playSound(user.getLocation(), new SoundEffect(NamedSounds.fromName("ENTITY_BLAZE_HURT"), 1.0f, 1.0f));

        user.redoInventory();
    }

    public abstract boolean onPurchase(BuyablePurchaseEvent event);

    private void doUpgradesOnBuy(User user) {
        for (Map.Entry<String, Calculator> upgrades : upgradeOnBuy.entrySet()) {
            user.setUserVariable(upgrades.getKey(), (int) upgrades.getValue().calculate(user.getUserVariables()));
        }
    }

    public boolean buyWithTeamMoney(User user) {
        return team.calculateBoolean(user.getUserVariables());
    }

}
