package com.ithinkrok.minigames.util.inventory;

import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.util.metadata.Money;
import com.ithinkrok.util.math.Calculator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

class BuyablePurchaseHandler {


    boolean isTeamPurchase;
    Map<String, Integer> customItemAmounts = new HashMap<>();
    Map<ItemStack, Integer> itemAmounts = new HashMap<>();

    int cost;
    Money userMoney;
    Money teamMoney;

    UserClickItemEvent event;
    User user;

    Buyable item;

    public BuyablePurchaseHandler(Buyable item, UserClickItemEvent event) {
        this.item = item;
        this.event = event;
        this.user = event.getUser();

        cost = item.getCost(user);

        userMoney = Money.getOrCreate(user);

        isTeamPurchase = item.team.calculateBoolean(user.getUserVariables());
    }


    public boolean isTeamPurchase() {
        return isTeamPurchase;
    }

    public boolean checkHasMoney() {
        if (isTeamPurchase) {
            teamMoney = Money.getOrCreate(user.getTeam());

            return cost <= userMoney.getMoney() + teamMoney.getMoney();
        } else {
            return userMoney.hasMoney(cost);
        }

    }

    /**
     *
     * @return map of the names of the items we don't have, and how much we need
     */
    public Map<String, Integer> checkHasItems() {
        Map<String, Integer> missingItems = new HashMap<>();

        calculateAndCheckCustomItemCosts(missingItems);
        calculateAndCheckNormalItemCosts(missingItems);

        return missingItems;
    }

    public void chargeMoney() {
        if (cost > 0) {
            if (isTeamPurchase) {
                int teamAmount = Math.min(cost, teamMoney.getMoney());
                int userAmount = cost - teamAmount;

                if (teamAmount > 0) teamMoney.subtractMoney(teamAmount, true);
                if (userAmount > 0) {
                    userMoney.subtractMoney(userAmount, true);
                    user.sendLocale(item.userPayTeamLocale, userAmount);
                }
            } else {
                userMoney.subtractMoney(cost, true);
            }
        }
    }

    public boolean chargeItems() {
        boolean itemsTaken; //have any items been taken from the inventory?

        //charge the user the items required
        itemsTaken = chargeCustomItems();
        itemsTaken |= chargeNormalItems();

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

    private boolean chargeCustomItems() {
        PlayerInventory inventory = user.getInventory();
        boolean itemsTaken = false;

        for (Map.Entry<String, Integer> customItemToAmount : customItemAmounts.entrySet()) {
            int requiredAmount = customItemToAmount.getValue();
            if(requiredAmount <= 0) continue;

            CustomItem customItem = event.getGameGroup().getCustomItem(customItemToAmount.getKey());

            InventoryUtils.removeItemsWithIdentifier(inventory, customItem.getIdentifier(), requiredAmount);

            itemsTaken = true;
        }
        return itemsTaken;
    }

    private void calculateAndCheckNormalItemCosts(Map<String, Integer> missingItems) {
        PlayerInventory inventory = user.getInventory();

        for (Map.Entry<ItemStack, Calculator> itemToAmount : item.itemCosts.entrySet()) {
            int requiredAmount = (int) itemToAmount.getValue().calculate(user.getUserVariables());
            if(requiredAmount <= 0) continue;

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

    private void calculateAndCheckCustomItemCosts(Map<String, Integer> missingItems) {
        PlayerInventory inventory = user.getInventory();

        for (Map.Entry<String, Calculator> customItemToAmount : item.customItemCosts.entrySet()) {
            int requiredAmount = (int) customItemToAmount.getValue().calculate(user.getUserVariables());
            if(requiredAmount <= 0) continue;

            //Store the amount in the customItemAmounts map to ensure we charge the same amount later
            customItemAmounts.put(customItemToAmount.getKey(), requiredAmount);

            CustomItem customItem = event.getGameGroup().getCustomItem(customItemToAmount.getKey());

            int userAmount = InventoryUtils.getAmountOfItemsWithIdentifier(inventory, customItem.getIdentifier());

            if (userAmount < requiredAmount) {
                //We don't have enough to pay with
                String itemName = user.getLanguageLookup().getLocale(customItem.getDisplayNameLocale());

                missingItems.put(itemName, requiredAmount);
            }
        }
    }
}
