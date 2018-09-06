package com.ithinkrok.minigames.util.inventory;

import com.ithinkrok.minigames.api.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.util.metadata.Money;
import com.ithinkrok.msm.common.economy.Currency;
import com.ithinkrok.msm.common.economy.CurrencyType;
import com.ithinkrok.msm.common.economy.result.Balance;
import com.ithinkrok.util.Pair;
import com.ithinkrok.util.StringUtils;
import com.ithinkrok.util.lang.LanguageLookup;
import com.ithinkrok.util.math.Calculator;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class BuyableLoreHandler {

    private final Buyable item;
    private final User user;
    private final CalculateItemForUserEvent event;
    private final LanguageLookup lookup;
    private final ItemStack display;

    private final String comma;
    private final String and;

    private Map<Currency, BigDecimal> cost;


    public BuyableLoreHandler(Buyable item, CalculateItemForUserEvent event) {
        this.item = item;
        user = event.getUser();
        this.event = event;
        lookup = user.getLanguageLookup();
        display = event.getDisplay();

        comma = lookup.getLocale("buyable.comma");
        and = lookup.getLocale("buyable.and");
    }


    public void addLore() {
        if (checkCurrenciesAreValid()) {

            //replace currency name with its currency object, now we know they are valid
            cost = item.getCost(user).entrySet().stream()
                    .map(entry -> {
                        return new Pair<>(item.getCurrency(user, entry.getKey()), entry.getValue());
                    })
                    .collect(Pair.collect());

            addMoneyCostLore();
        }

        addItemCostLore();

        event.setDisplay(display);
    }


    private boolean checkCurrenciesAreValid() {
        Set<String> badCurrencies = item.getInvalidCurrencies(user);
        if (!badCurrencies.isEmpty()) {
            System.out.println("Found buyable with unknown currency: " + display +
                               " with unknown currencies " + badCurrencies);
            String badLore = lookup.getLocale(item.unknownCurrencyLocale, String.join(", ", badCurrencies));
            InventoryUtils.addLore(display, badLore);
            return false;
        }

        return true;
    }


    private void addMoneyCostLore() {
        if (cost.isEmpty()) return;


        if (item.team.calculateBoolean(user.getUserVariables())) {
            addTeamMoneyCostLore();
        } else if (cost.size() == 1 &&
                   cost.keySet().iterator().next().getName().equals("game")) {
            addGameMoneyCostLore();
        } else {
            addEconomyMoneyCostLore();
        }


    }


    private void addItemCostLore() {
        if (item.itemCosts.isEmpty() && item.customItemCosts.isEmpty()) return;

        ItemStack display = event.getDisplay();
        LanguageLookup lookup = event.getUser().getLanguageLookup();

        if (!cost.isEmpty()) {
            //if we also cost money
            InventoryUtils.addLore(display, lookup.getLocale(item.extraCostsLocale));
        } else {
            //we only cost items
            InventoryUtils.addLore(display, lookup.getLocale(item.extraCostsOnlyLocale));
        }

        PlayerInventory inventory = event.getUser().getInventory();

        for (Map.Entry<String, Calculator> customItemToAmount : item.customItemCosts.entrySet()) {
            int requiredAmount = (int) customItemToAmount.getValue().calculate(event.getUser().getUserVariables());
            if (requiredAmount <= 0) continue;

            CustomItem customItem = event.getGameGroup().getCustomItem(customItemToAmount.getKey());

            int userAmount = InventoryUtils.getAmountOfItemsWithIdentifier(inventory, customItem.getName());

            String prefix = ((userAmount >= requiredAmount) ? ChatColor.GREEN : ChatColor.RED).toString();

            String itemName = lookup.getLocale(customItem.getDisplayNameLocale());

            String lore = lookup.getLocale(item.costsItemLocale, prefix + requiredAmount, prefix + itemName);
            InventoryUtils.addLore(display, lore);
        }

        for (Map.Entry<ItemStack, Calculator> itemToAmount : item.itemCosts.entrySet()) {
            int requiredAmount = (int) itemToAmount.getValue().calculate(event.getUser().getUserVariables());
            if (requiredAmount <= 0) continue;

            boolean hasAmount = inventory.containsAtLeast(itemToAmount.getKey(), requiredAmount);

            String prefix = (hasAmount ? ChatColor.GREEN : ChatColor.RED).toString();

            String itemName = InventoryUtils.getItemStackDefaultName(itemToAmount.getKey());

            String lore = lookup.getLocale(item.costsItemLocale, prefix + requiredAmount, prefix + itemName);
            InventoryUtils.addLore(display, lore);
        }


        event.setDisplay(display);
    }


    private void addTeamMoneyCostLore() {
        if (cost.size() > 1) {
            throw new RuntimeException("Team support is for 1 currency only");
        }

        BigDecimal moneyCost = cost.values().iterator().next();
        if (moneyCost == null) {
            throw new RuntimeException("Team support is only for the game currency");
        }

        //TODO gaining team currency

        Money userMoney = Money.getOrCreate(user);

        boolean hasMoney = true;

        //First "if team"
        Money teamMoney = Money.getOrCreate(user.getTeam());

        if (userMoney.getMoney() + teamMoney.getMoney() < moneyCost.intValue()) hasMoney = false;
        ChatColor costColor = hasMoney ? ChatColor.GREEN : ChatColor.RED;

        Currency gameCurrency = item.getCurrency(user, "game");
        String costString = costColor + gameCurrency.format(moneyCost);

        InventoryUtils.addLore(display, lookup.getLocale(item.teamCostLocale, costString));
    }


    private void addGameMoneyCostLore() {
        //we know that the cost is in only one currency, game currency
        BigDecimal moneyCost = cost.values().iterator().next();

        //TODO gaining game user currency

        Money userMoney = Money.getOrCreate(user);

        ChatColor costColor = userMoney.hasMoney(moneyCost.intValue()) ? ChatColor.GREEN : ChatColor.RED;

        Currency gameCurrency = item.getCurrency(user, "game");
        String costString = costColor + gameCurrency.format(moneyCost);

        InventoryUtils.addLore(display, lookup.getLocale(item.userCostLocale, costString));
    }


    private void addEconomyMoneyCostLore() {
        Map<Currency, BigDecimal> payMoney = cost.entrySet().stream()
                .filter(entry -> entry.getValue().signum() == 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<Currency, BigDecimal> rewardMoney = cost.entrySet().stream()
                .filter(entry -> entry.getValue().signum() == -1)
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue().abs()))
                .collect(Pair.collect());


        addRewardMoneyLore(rewardMoney);
        addPayMoneyLore(payMoney);
    }


    private void addRewardMoneyLore(Map<Currency, BigDecimal> rewardMoney) {
        if (rewardMoney.isEmpty()) return;

        List<String> formattedRewards = rewardMoney.entrySet().stream()
                .map(entry -> entry.getKey().format(entry.getValue()))
                .collect(Collectors.toList());

        String rewardString = StringUtils.listToString(formattedRewards, comma, and);
        InventoryUtils.addLore(display, lookup.getLocale(item.currencyRewardLocale, rewardString));
    }


    private void addPayMoneyLore(Map<Currency, BigDecimal> payMoney) {
        if (payMoney.isEmpty()) return;

        //all costs will go on one list, as a list with commas and ands
        List<String> formattedCosts = payMoney.entrySet().stream()
                .map(entry -> formatCost(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        String costString = StringUtils.listToString(formattedCosts, comma, and);
        InventoryUtils.addLore(display, lookup.getLocale(item.currencyCostLocale, costString));

        //known balances also go on one line
        List<String> knownBalances = payMoney.entrySet().stream()
                .map(entry -> {
                    Currency currency = entry.getKey();
                    Optional<Balance> balance = user.getEconomyAccount().getBalance(currency);

                    return new Pair<>(currency, balance.isPresent() ? balance.get().getAmount() : null);
                })
                .filter(pair -> pair.second() != null)
                .map(pair -> pair.first().format(pair.second()))
                .collect(Collectors.toList());


        if (!knownBalances.isEmpty()) {
            String knownBalance = StringUtils.listToString(knownBalances, comma, and);

            InventoryUtils.addLore(display, lookup.getLocale(item.currencyAmountLocale, knownBalance));
        }
    }


    private String formatCost(Currency currency, BigDecimal cost) {
        ChatColor costColor;

        //Use new API
        Optional<Balance> balance = user.getEconomyAccount().getBalance(currency);

        if (balance.isPresent()) {
            BigDecimal knownBalance = balance.get().getAmount();
            costColor = knownBalance.compareTo(cost) >= 0 ? ChatColor.GREEN : ChatColor.RED;
        } else {
            costColor = ChatColor.BLUE;
        }

        return costColor + currency.format(cost);
    }
}
