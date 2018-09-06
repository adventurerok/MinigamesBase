package com.ithinkrok.minigames.util.inventory;

import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.util.metadata.Money;
import com.ithinkrok.msm.common.economy.AccountIdentifier;
import com.ithinkrok.msm.common.economy.Currency;
import com.ithinkrok.msm.common.economy.batch.Batch;
import com.ithinkrok.msm.common.economy.batch.BatchResult;
import com.ithinkrok.msm.common.economy.batch.Update;
import com.ithinkrok.msm.common.economy.batch.UpdateType;
import com.ithinkrok.msm.common.economy.result.Balance;
import com.ithinkrok.msm.common.economy.result.TransactionResult;
import com.ithinkrok.util.NullReplacements;
import com.ithinkrok.util.Pair;
import com.ithinkrok.util.math.Calculator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

class BuyablePurchaseHandler {


    private final Map<Currency, BigDecimal> payMoney;
    private final Map<Currency, BigDecimal> rewardMoney;
    boolean isTeamPurchase;
    Map<String, Integer> customItemAmounts = new HashMap<>();
    Map<ItemStack, Integer> itemAmounts = new HashMap<>();
    //BigDecimal cost;
    Money userMoney;
    Money teamMoney;
    UserClickItemEvent event;
    User user;
    Buyable item;
    BigDecimal amountChargedToTeam, amountChargedToUser;
    BatchResult paymentBatchResult;
    private Currency currency;


    public BuyablePurchaseHandler(Buyable item, UserClickItemEvent event) {
        this.item = item;
        this.event = event;
        this.user = event.getUser();

        Map<Currency, BigDecimal> cost = item.getCost(user).entrySet().stream()
                .map(entry -> {
                    return new Pair<>(item.getCurrency(user, entry.getKey()), entry.getValue());
                })
                .collect(Pair.collect());

        payMoney = cost.entrySet().stream()
                .filter(entry -> entry.getValue().signum() == 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        rewardMoney = cost.entrySet().stream()
                .filter(entry -> entry.getValue().signum() == -1)
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue().abs()))
                .collect(Pair.collect());

        userMoney = Money.getOrCreate(user);

        isTeamPurchase = item.team.calculateBoolean(user.getUserVariables());
    }


    public boolean isTeamPurchase() {
        return isTeamPurchase;
    }


    public boolean checkHasMoney() {
        if (payMoney.isEmpty()) return true;

        if (isTeamPurchase) {
            teamMoney = Money.getOrCreate(user.getTeam());

            //Teams can only pay with game currency.
            if (payMoney.size() > 1 || !payMoney.keySet().iterator().next().getName().equals("game")) {
                return false;
            }

            int moneyCost = payMoney.values().iterator().next().intValue();

            return moneyCost <= userMoney.getMoney() + teamMoney.getMoney();
        } else {
            for (Map.Entry<Currency, BigDecimal> entry : payMoney.entrySet()) {
                Currency currency = entry.getKey();
                BigDecimal amount = entry.getValue();

                Optional<Balance> balance = user.getEconomyAccount().getBalance(currency);

                if (balance.isPresent() && balance.get().getAmount().compareTo(amount) < 0) {
                    return false;
                }

                //we can't check if the optional is not present, but we check later when we attempt the transaction.
            }

            return true;
        }

    }


    /**
     * @return map of the names of the items we don't have, and how much we need
     */
    public Map<String, Integer> checkHasItems() {
        Map<String, Integer> missingItems = new HashMap<>();

        calculateAndCheckCustomItemCosts(missingItems);
        calculateAndCheckNormalItemCosts(missingItems);

        return missingItems;
    }


    private void calculateAndCheckCustomItemCosts(Map<String, Integer> missingItems) {
        PlayerInventory inventory = user.getInventory();

        for (Map.Entry<String, Calculator> customItemToAmount : item.customItemCosts.entrySet()) {
            int requiredAmount = (int) customItemToAmount.getValue().calculate(user.getUserVariables());
            if (requiredAmount <= 0) continue;

            //Store the amount in the customItemAmounts map to ensure we charge the same amount later
            customItemAmounts.put(customItemToAmount.getKey(), requiredAmount);

            CustomItem customItem = event.getGameGroup().getCustomItem(customItemToAmount.getKey());

            int userAmount = InventoryUtils.getAmountOfItemsWithIdentifier(inventory, customItem.getName());

            if (userAmount < requiredAmount) {
                //We don't have enough to pay with
                String itemName = user.getLanguageLookup().getLocale(customItem.getDisplayNameLocale());

                missingItems.put(itemName, requiredAmount);
            }
        }
    }


    private void calculateAndCheckNormalItemCosts(Map<String, Integer> missingItems) {
        PlayerInventory inventory = user.getInventory();

        for (Map.Entry<ItemStack, Calculator> itemToAmount : item.itemCosts.entrySet()) {
            int requiredAmount = (int) itemToAmount.getValue().calculate(user.getUserVariables());
            if (requiredAmount <= 0) continue;

            //Store the amount in the itemAmounts map to ensure we charge the same amount later
            itemAmounts.put(itemToAmount.getKey(), requiredAmount);

            boolean hasAmount = inventory.containsAtLeast(itemToAmount.getKey(), requiredAmount);
            if (!hasAmount) {
                //Don't have enough to pay with
                String itemName = InventoryUtils.getItemStackDefaultName(itemToAmount.getKey());

                missingItems.put(itemName, requiredAmount);
            }
        }
    }


    public void chargeMoney(Runnable success, Runnable failure) {
        if (!payMoney.isEmpty()) {
            if (isTeamPurchase) {

                //we already checked in checkHasMoney() that there is a single team currency cost in "game" currency
                int moneyCost = payMoney.values().iterator().next().intValue();
                //Use old API for team purchase
                int teamAmount = Math.min(moneyCost, teamMoney.getMoney());
                int userAmount = moneyCost - teamAmount;

                if (teamAmount > 0) teamMoney.subtractMoney(teamAmount, true);
                if (userAmount > 0) {
                    userMoney.subtractMoney(userAmount, true);
                    user.sendLocale(item.userPayTeamLocale, userAmount);
                }

                amountChargedToTeam = BigDecimal.valueOf(teamAmount);
                amountChargedToUser = BigDecimal.valueOf(userAmount);

                success.run();
            } else {
                //Use new API

                //amountCharged is just the payMoney
                //but we would also need to revert rewardMoney

                Batch paymentBatch = new Batch();

                for (Map.Entry<Currency, BigDecimal> entry : payMoney.entrySet()) {
                    AccountIdentifier account = new AccountIdentifier(user.getUuid(), entry.getKey());
                    paymentBatch.addUpdate(new Update(account, UpdateType.WITHDRAW, entry.getValue()));
                }

                for (Map.Entry<Currency, BigDecimal> entry : rewardMoney.entrySet()) {
                    AccountIdentifier account = new AccountIdentifier(user.getUuid(), entry.getKey());
                    paymentBatch.addUpdate(new Update(account, UpdateType.DEPOSIT, entry.getValue()));
                }

                user.getEconomyAccount().getProvider().executeBatch(paymentBatch, "buyable purchase", result -> {
                    if (result.wasSuccessful()) {
                        paymentBatchResult = result;
                        user.doInFuture(task -> success.run());
                    } else {
                        user.doInFuture(task -> failure.run());
                    }
                });


            }
        } else {
            success.run();
        }
    }


    public void refundMoney() {
        if (payMoney.isEmpty()) return;

        if (isTeamPurchase) {
            teamMoney.addMoney(amountChargedToTeam.intValue(), true);
            userMoney.addMoney(amountChargedToUser.intValue(), true);
        } else {
            Batch rollback = Batch.rollback(paymentBatchResult);

            user.getEconomyAccount().getProvider()
                    .executeBatch(rollback, "buyable refund", NullReplacements.nullConsumer());
        }
    }


    public boolean chargeItems() {
        boolean itemsTaken; //have any items been taken from the inventory?

        //charge the user the items required
        itemsTaken = chargeCustomItems();
        itemsTaken |= chargeNormalItems();

        return itemsTaken;
    }


    private boolean chargeCustomItems() {
        PlayerInventory inventory = user.getInventory();
        boolean itemsTaken = false;

        for (Map.Entry<String, Integer> customItemToAmount : customItemAmounts.entrySet()) {
            int requiredAmount = customItemToAmount.getValue();
            if (requiredAmount <= 0) continue;

            CustomItem customItem = event.getGameGroup().getCustomItem(customItemToAmount.getKey());

            InventoryUtils.removeItemsWithIdentifier(inventory, customItem.getName(), requiredAmount);

            itemsTaken = true;
        }
        return itemsTaken;
    }


    private boolean chargeNormalItems() {
        PlayerInventory inventory = user.getInventory();
        boolean itemsTaken = false;

        for (Map.Entry<ItemStack, Integer> itemToAmount : itemAmounts.entrySet()) {
            ItemStack itemCost = itemToAmount.getKey().clone();

            itemCost.setAmount(itemToAmount.getValue());

            inventory.removeItem(itemCost);

            itemsTaken = true;
        }
        return itemsTaken;
    }
}
