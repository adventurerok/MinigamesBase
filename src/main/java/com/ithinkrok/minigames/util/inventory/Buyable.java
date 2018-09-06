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
import com.ithinkrok.util.Pair;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by paul on 08/01/16.
 */
public abstract class Buyable extends ClickableItem {

    final Map<ItemStack, Calculator> itemCosts = new HashMap<>();
    final Map<String, Calculator> customItemCosts = new HashMap<>();
    final Map<String, Calculator> multiCurrencyCost = new HashMap<>();
    private final Map<String, Calculator> upgradeOnBuy = new HashMap<>();
    String teamNoMoneyLocale;
    String userNoMoneyLocale;
    String cannotBuyLocale;
    String userPayTeamLocale;
    String teamCostLocale;
    String userCostLocale;
    String currencyCostLocale;
    String currencyAmountLocale;
    String currencyRewardLocale;
    String extraCostsLocale;
    String extraCostsOnlyLocale;
    String costsItemLocale;
    String purchaseLocale;
    String broadcastLocale;
    String notAccreditedLocale;
    String unknownCurrencyLocale;
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

        team = new ExpressionCalculator(config.getString("team", "false"));
        canBuy = new ExpressionCalculator(config.getString("can_buy", "true"));

        cost = new ExpressionCalculator(config.getString("cost"));
        currency = config.getString("currency", "game");
        multiCurrencyCost.put(currency, cost);

        //add in costs in other currencies
        Config multiCostConfig = config.getConfigOrEmpty("costs");
        for (String otherCurrency : multiCostConfig.getKeys(false)) {
            multiCurrencyCost.put(otherCurrency, new ExpressionCalculator(multiCostConfig.getString(otherCurrency)));
        }

        teamNoMoneyLocale = config.getString("team_no_money_locale", "buyable.team.no_money");
        userNoMoneyLocale = config.getString("user_no_money_locale", "buyable.user.no_money");
        cannotBuyLocale = config.getString("cannot_buy_locale", "buyable.cannot_buy");
        userPayTeamLocale = config.getString("user_pay_team_locale", "buyable.user.pay_team");
        teamCostLocale = config.getString("cost_team_locale", "buyable.cost.team");
        userCostLocale = config.getString("cost_user_locale", "buyable.cost.user");
        currencyCostLocale = config.getString("cost_currency_locale", "buyable.cost.currency");
        currencyAmountLocale = config.getString("currency_amount_locale", "buyable.currency_amount");
        currencyRewardLocale = config.getString("currency_reward_locale", "buyable.reward.currency");
        purchaseLocale = config.getString("purchase_locale");
        broadcastLocale = config.getString("broadcast_locale");
        notAccreditedLocale = config.getString("not_accredited_locale", "buyable.not_accredited");
        unknownCurrencyLocale = config.getString("unknown_currency_locale", "buyable.unknown_currency");

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

        BuyableLoreHandler loreHandler = new BuyableLoreHandler(this, event);
        loreHandler.addLore();
    }


    /**
     * @param user Check if this item is available to buy for this user
     * @return If the item is available for the user
     */
    public boolean isAvailable(User user) {
        return canBuy.calculateBoolean(user.getUserVariables());
    }


    @Override
    public void onClick(UserClickItemEvent event) {
        User user = event.getUser();
        Set<String> badCurrencies = getInvalidCurrencies(user);
        if (!badCurrencies.isEmpty()) {
            user.sendLocale(unknownCurrencyLocale, String.join(", ", badCurrencies));
            return;
        }

        BuyablePurchaseHandler handler = new BuyablePurchaseHandler(this, event);

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
        if (!user.getGameGroup().isAccredited() && currencyObj.getCurrencyType() != CurrencyType.MINIGAME_SPECIFIC) {
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

            if (purchaseLocale != null) {
                user.sendLocale(purchaseLocale);
            }

            if (broadcastLocale != null) {
                user.getGameGroup().sendLocale(broadcastLocale, user.getFormattedName());
            }

            user.playSound(user.getLocation(), new SoundEffect(NamedSounds.fromName("ENTITY_BLAZE_HURT"), 1.0f, 1.0f));

            user.redoInventory();
            user.updateScoreboard();

        }, /*failure*/() -> {
            //Tell the user they couldn't afford the item
            user.sendLocale(userNoMoneyLocale);
        });

    }


    public Currency getCurrency(User user, String currencyName) {
        return user.getEconomyAccount().lookupCurrency(currencyName);
    }


    public abstract boolean onPurchase(BuyablePurchaseEvent event);


    private void doUpgradesOnBuy(User user) {
        for (Map.Entry<String, Calculator> upgrades : upgradeOnBuy.entrySet()) {
            user.setUserVariable(upgrades.getKey(), (int) upgrades.getValue().calculate(user.getUserVariables()));
        }
    }


    /**
     *
     * @param user The user to price this Buyable for
     * @return A map of currency name to cost in that currency. Negative cost means the user is given that currency.
     */
    public Map<String, BigDecimal> getCost(User user) {
        return multiCurrencyCost.entrySet().stream()
                .map(entry -> {
                    String currencyName = entry.getKey();
                    Currency currency = getCurrency(user, currencyName);

                    MathContext mathContext = currency != null ? currency.getMathContext() : MathContext.DECIMAL32;
                    Calculator costCalculator = entry.getValue();
                    BigDecimal cost = costCalculator.calculateDecimal(user.getUserVariables(), mathContext);

                    return new Pair<>(currencyName, cost);
                })
                //remove costs that are zero
                .filter(pair -> pair.second().compareTo(BigDecimal.ZERO) != 0)
                .collect(Pair.collect());
    }


    /**
     * @return If all currencies are valid, false otherwise
     */
    Set<String> getInvalidCurrencies(User user) {
        Set<String> result = new HashSet<>();

        for (String currencyName : multiCurrencyCost.keySet()) {
            if (getCurrency(user, currencyName) == null) {
                result.add(currencyName);
            }
        }

        return result;
    }

}
