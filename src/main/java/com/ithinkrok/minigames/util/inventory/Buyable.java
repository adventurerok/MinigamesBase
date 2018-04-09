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
import com.ithinkrok.msm.common.economy.Currency;
import com.ithinkrok.msm.common.economy.CurrencyType;
import com.ithinkrok.msm.common.economy.result.Balance;
import com.ithinkrok.util.math.Calculator;
import com.ithinkrok.util.math.ExpressionCalculator;
import com.ithinkrok.minigames.util.inventory.event.BuyablePurchaseEvent;
import com.ithinkrok.minigames.util.metadata.Money;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.lang.LanguageLookup;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by paul on 08/01/16.
 */
public abstract class Buyable extends ClickableItem {

    final Map<ItemStack, Calculator> itemCosts = new HashMap<>();
    final Map<String, Calculator> customItemCosts = new HashMap<>();
    private final Map<String, Calculator> upgradeOnBuy = new HashMap<>();
    String teamNoMoneyLocale;
    String userNoMoneyLocale;
    String cannotBuyLocale;
    String userPayTeamLocale;
    String teamCostLocale;
    String userCostLocale;
    String currencyCostLocale;
    String currencyAmountLocale;
    String extraCostsLocale;
    String extraCostsOnlyLocale;
    String costsItemLocale;
    String purchaseLocale;
    String notAccreditedLocale;

    String noItemLocale;
    String itemsTakenLocale;

    String currency;
    Calculator cost;
    Calculator team;
    Calculator canBuy;


    public Buyable(ItemStack baseDisplay, int slot) {
        super(baseDisplay, slot);
    }

    @Override
    public void configure(Config config) {
        cost = new ExpressionCalculator(config.getString("cost"));
        team = new ExpressionCalculator(config.getString("team", "false"));
        canBuy = new ExpressionCalculator(config.getString("can_buy", "true"));
        currency = config.getString("currency", "game");

        teamNoMoneyLocale = config.getString("team_no_money_locale", "buyable.team.no_money");
        userNoMoneyLocale = config.getString("user_no_money_locale", "buyable.user.no_money");
        cannotBuyLocale = config.getString("cannot_buy_locale", "buyable.cannot_buy");
        userPayTeamLocale = config.getString("user_pay_team_locale", "buyable.user.pay_team");
        teamCostLocale = config.getString("cost_team_locale", "buyable.cost.team");
        userCostLocale = config.getString("cost_user_locale", "buyable.cost.user");
        currencyCostLocale = config.getString("cost_currency_locale", "buyable.cost.currency");
        currencyAmountLocale = config.getString("currency_amount_locale", "buyable.currency_amount");
        purchaseLocale = config.getString("purchase_locale");
        notAccreditedLocale = config.getString("not_accredited_locale", "buyable.not_accredited");

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

    @Override
    public void onClick(UserClickItemEvent event) {
        BuyablePurchaseHandler handler = new BuyablePurchaseHandler(this, event);
        User user = event.getUser();

        //Check if we have the money to afford the purchase
        if (!handler.checkHasMoney()) {
            //We don't have the money
            if (handler.isTeamPurchase()) {
                user.sendLocale(teamNoMoneyLocale);
            } else {
                user.sendLocale(userNoMoneyLocale);
            }

            return;
        }

        //Check if we have the items to afford the purchase
        Map<String, Integer> missingItems = handler.checkHasItems();
        if (!missingItems.isEmpty()) {
            //We don't have the items
            for (Map.Entry<String, Integer> missing : missingItems.entrySet()) {
                user.sendLocale(noItemLocale, missing.getKey(), missing.getValue());
            }

            return;
        }

        //Try and buy
        BuyablePurchaseEvent purchaseEvent = new BuyablePurchaseEvent(user, event.getInventory(), this);

        if (!isAvailable(user)) {
            user.sendLocale(cannotBuyLocale);
            user.redoInventory();
            return;
        }

        Currency currencyObj = user.getEconomyAccount().getContext().lookupCurrency(this.currency);
        if(!user.getGameGroup().isAccredited() && currencyObj.getCurrencyType() != CurrencyType.MINIGAME_SPECIFIC) {
            user.sendLocale(notAccreditedLocale);
            return;
        }

        //charge the user the money required
        handler.chargeMoney(/*success*/() -> {
            if (!onPurchase(purchaseEvent)) {
                handler.refundMoney();
                return;
            }

            doUpgradesOnBuy(user);

            if (handler.chargeItems()) {
                user.sendLocale(itemsTakenLocale);
            }

            if(purchaseLocale != null) {
                user.sendLocale(purchaseLocale);
            }

            user.playSound(user.getLocation(), new SoundEffect(NamedSounds.fromName("ENTITY_BLAZE_HURT"), 1.0f, 1.0f));

            user.redoInventory();
            user.updateScoreboard();

        }, /*failure*/() -> {
            //Tell the user they couldn't afford the item
            user.sendLocale(userNoMoneyLocale);
        });

    }

    /**
     * @param user Check if this item is available to buy for this user
     * @return If the item is available for the user
     */
    public boolean isAvailable(User user) {
        return canBuy.calculateBoolean(user.getUserVariables());
    }

    public abstract boolean onPurchase(BuyablePurchaseEvent event);

    private void doUpgradesOnBuy(User user) {
        for (Map.Entry<String, Calculator> upgrades : upgradeOnBuy.entrySet()) {
            user.setUserVariable(upgrades.getKey(), (int) upgrades.getValue().calculate(user.getUserVariables()));
        }
    }

    private void addMoneyCostLore(CalculateItemForUserEvent event) {
        User user = event.getUser();

        //Check if the cost exists
        BigDecimal cost = getCost(user);
        if (cost.compareTo(BigDecimal.ZERO) <= 0) return;

        Money userMoney = Money.getOrCreate(user);
        Currency currency = getCurrency(user);
        boolean hasMoney = true;
        boolean unknownMoney = false;

        boolean team = this.team.calculateBoolean(user.getUserVariables());

        BigDecimal knownBalance = null;

        if (team) {
            Money teamMoney = Money.getOrCreate(user.getTeam());
            if (userMoney.getMoney() + teamMoney.getMoney() < cost.intValue()) hasMoney = false;

        } else {
            //Use new API
            Optional<Balance> balance = user.getEconomyAccount().getBalance(currency);

            if(balance.isPresent()) {
                knownBalance = balance.get().getAmount();
                hasMoney = knownBalance.compareTo(cost) >= 0;
            } else {
                unknownMoney = true;
            }
        }

        ChatColor costColor = (unknownMoney ? ChatColor.BLUE : hasMoney ? ChatColor.GREEN : ChatColor.RED);

        String costString = costColor + currency.format(cost);
        LanguageLookup lookup = user.getLanguageLookup();

        ItemStack display = event.getDisplay();

        if (team) {
            display = InventoryUtils.addLore(display, lookup.getLocale(teamCostLocale, costString));
        } else if(currency.getCurrencyType().equals(CurrencyType.MINIGAME_SPECIFIC)){
            display = InventoryUtils.addLore(display, lookup.getLocale(userCostLocale, costString));
        } else {
            display = InventoryUtils.addLore(display, lookup.getLocale(currencyCostLocale, costString));

            if(knownBalance != null) {
                display = InventoryUtils.addLore(display, lookup.getLocale(currencyAmountLocale, currency.format(knownBalance)));
            }
        }

        event.setDisplay(display);
    }

    private void addItemCostLore(CalculateItemForUserEvent event) {
        if (itemCosts.isEmpty() && customItemCosts.isEmpty()) return;

        ItemStack display = event.getDisplay();
        LanguageLookup lookup = event.getUser().getLanguageLookup();

        BigDecimal moneyCost = getCost(event.getUser());

        if (moneyCost.compareTo(BigDecimal.ZERO) > 0) {
            display = InventoryUtils.addLore(display, lookup.getLocale(extraCostsLocale));
        } else {
            display = InventoryUtils.addLore(display, lookup.getLocale(extraCostsOnlyLocale));
        }

        PlayerInventory inventory = event.getUser().getInventory();

        for (Map.Entry<String, Calculator> customItemToAmount : customItemCosts.entrySet()) {
            int requiredAmount = (int) customItemToAmount.getValue().calculate(event.getUser().getUserVariables());
            if (requiredAmount <= 0) continue;

            CustomItem customItem = event.getGameGroup().getCustomItem(customItemToAmount.getKey());

            int userAmount = InventoryUtils.getAmountOfItemsWithIdentifier(inventory, customItem.getName());

            String prefix = ((userAmount >= requiredAmount) ? ChatColor.GREEN : ChatColor.RED).toString();

            String itemName = lookup.getLocale(customItem.getDisplayNameLocale());

            String lore = lookup.getLocale(costsItemLocale, prefix + requiredAmount, prefix + itemName);
            display = InventoryUtils.addLore(display, lore);
        }

        for (Map.Entry<ItemStack, Calculator> itemToAmount : itemCosts.entrySet()) {
            int requiredAmount = (int) itemToAmount.getValue().calculate(event.getUser().getUserVariables());
            if (requiredAmount <= 0) continue;

            boolean hasAmount = inventory.containsAtLeast(itemToAmount.getKey(), requiredAmount);

            String prefix = (hasAmount ? ChatColor.GREEN : ChatColor.RED).toString();

            String itemName = InventoryUtils.getItemStackDefaultName(itemToAmount.getKey());

            String lore = lookup.getLocale(costsItemLocale, prefix + requiredAmount, prefix + itemName);
            display = InventoryUtils.addLore(display, lore);
        }


        event.setDisplay(display);
    }

    public BigDecimal getCost(User user) {
        Currency currency = getCurrency(user);

        return cost.calculateDecimal(user.getUserVariables(), new MathContext(currency.getDecimalPlaces()));
    }

    public Currency getCurrency(User user) {
        return user.getEconomyAccount().lookupCurrency(currency);
    }

}
