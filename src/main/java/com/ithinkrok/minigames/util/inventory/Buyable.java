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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 08/01/16.
 */
public abstract class Buyable extends ClickableItem {

    private final Map<String, Calculator> upgradeOnBuy = new HashMap<>();
    private final List<ItemStack> itemCosts = new ArrayList<>();
    private final Map<String, Integer> customItemCosts = new HashMap<>();
    private String teamNoMoneyLocale;
    private String userNoMoneyLocale;
    private String cannotBuyLocale;
    private String userPayTeamLocale;
    private String teamDescriptionLocale;
    private String userDescriptionLocale;
    private String extraCostsLocale;
    private String extraCostsOnlyLocale;
    private String costsItemLocale;
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

        if (config.contains("upgrade_on_buy")) configureUpgradeOnBuy(config.getConfigOrNull("upgrade_on_buy"));

        if (config.contains("item_costs")) {
            itemCosts.addAll(MinigamesConfigs.getItemStackList(config, "item_costs"));
        }

        if (config.contains("custom_item_costs")) {
            for (String itemToAmount : config.getStringList("custom_item_costs")) {
                String[] parts = itemToAmount.split(",");

                try {
                    customItemCosts.put(parts[0], Integer.parseInt(parts[1]));
                } catch (Exception e) {
                    System.err.println("Bad buyable custom_item_costs: " + itemToAmount);
                    e.printStackTrace();
                }
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
        BuyablePurchaseEvent purchaseEvent = new BuyablePurchaseEvent(event.getUser(), event.getInventory(), this);

        if (!canBuy(purchaseEvent)) {
            event.setDisplay(null);
            return;
        }

        addMoneyCostLore(event);
        addItemCostLore(event);
    }

    public boolean canBuy(BuyablePurchaseEvent event) {
        return canBuy.calculateBoolean(event.getUser().getUserVariables());
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

        for (Map.Entry<String, Integer> customItemToAmount : customItemCosts.entrySet()) {
            CustomItem customItem = event.getGameGroup().getCustomItem(customItemToAmount.getKey());

            int userAmount = InventoryUtils.getAmountOfItemsWithIdentifier(inventory, customItem.getIdentifier());
            int requiredAmount = customItemToAmount.getValue();

            String prefix = ((userAmount >= requiredAmount) ? ChatColor.GREEN : ChatColor.RED).toString();

            String itemName = lookup.getLocale(customItem.getDisplayNameLocale());

            String lore = lookup.getLocale(costsItemLocale, prefix + requiredAmount, prefix + itemName);
            display = InventoryUtils.addLore(display, lore);
        }

        for (ItemStack itemCost : itemCosts) {
            boolean hasAmount = inventory.containsAtLeast(itemCost, itemCost.getAmount());

            String prefix = (hasAmount ? ChatColor.GREEN : ChatColor.RED).toString();

            String itemName = InventoryUtils.getItemStackDefaultName(itemCost);

            String lore = lookup.getLocale(costsItemLocale, prefix + itemCost.getAmount(), prefix + itemName);
            display = InventoryUtils.addLore(display, lore);
        }


        event.setDisplay(display);
    }

    public int getCost(User user) {
        return (int) cost.calculate(user.getUserVariables());
    }

    @Override
    public void onClick(UserClickItemEvent event) {
        Money userMoney = Money.getOrCreate(event.getUser());
        Money teamMoney = null;

        int cost = getCost(event.getUser());

        boolean team = this.team.calculateBoolean(event.getUser().getUserVariables());

        if (team) {
            teamMoney = Money.getOrCreate(event.getUser().getTeam());
            if (userMoney.getMoney() + teamMoney.getMoney() < cost) {
                event.getUser().sendLocale(teamNoMoneyLocale);
                return;
            }
        } else if (!userMoney.hasMoney(cost)) {
            event.getUser().sendLocale(userNoMoneyLocale);
            return;
        }

        BuyablePurchaseEvent purchaseEvent = new BuyablePurchaseEvent(event.getUser(), event.getInventory(), this);

        if (!canBuy(purchaseEvent)) {
            event.getUser().sendLocale(cannotBuyLocale);
            event.getUser().redoInventory();
            return;
        }

        if (!onPurchase(purchaseEvent)) return;

        doUpgradesOnBuy(event.getUser());

        if (team) {
            int teamAmount = Math.min(cost, teamMoney.getMoney());
            int userAmount = cost - teamAmount;

            if (teamAmount > 0) teamMoney.subtractMoney(teamAmount, true);
            if (userAmount > 0) {
                userMoney.subtractMoney(userAmount, true);
                event.getUser().sendLocale(userPayTeamLocale, userAmount);
            }
        } else {
            userMoney.subtractMoney(cost, true);
        }

        event.getUser().playSound(event.getUser().getLocation(),
                                  new SoundEffect(NamedSounds.fromName("ENTITY_BLAZE_HURT"), 1.0f, 1.0f));

        event.getUser().redoInventory();
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
