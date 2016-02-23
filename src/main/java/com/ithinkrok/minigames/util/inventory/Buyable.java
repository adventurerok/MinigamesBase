package com.ithinkrok.minigames.util.inventory;

import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.SoundEffect;
import com.ithinkrok.minigames.api.util.math.Calculator;
import com.ithinkrok.minigames.api.util.math.ExpressionCalculator;
import com.ithinkrok.minigames.util.inventory.event.BuyablePurchaseEvent;
import com.ithinkrok.minigames.util.metadata.Money;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.lang.LanguageLookup;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 08/01/16.
 */
public abstract class Buyable extends ClickableItem {

    private String teamNoMoneyLocale;
    private String userNoMoneyLocale;
    private String cannotBuyLocale;
    private String userPayTeamLocale;
    private String teamDescriptionLocale;
    private String userDescriptionLocale;
    private Calculator cost;
    private Calculator team;

    private Calculator canBuy;

    private final Map<String, Calculator> upgradeOnBuy = new HashMap<>();

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

        if(config.contains("upgrade_on_buy")) configureUpgradeOnBuy(config.getConfigOrNull("upgrade_on_buy"));
    }

    private void configureUpgradeOnBuy(Config config) {
        for(String upgrade : config.getKeys(false)) {
            upgradeOnBuy.put(upgrade, new ExpressionCalculator(config.getString(upgrade)));
        }
    }

    @Override
    public void onCalculateItem(CalculateItemForUserEvent event) {
        if(event.getDisplay() == null) return;
        BuyablePurchaseEvent purchaseEvent = new BuyablePurchaseEvent(event.getUser(), event.getInventory(), this);

        if (!canBuy(purchaseEvent)) {
            event.setDisplay(null);
            return;
        }

        Money userMoney = Money.getOrCreate(event.getUser());

        int cost = getCost(event.getUser());
        boolean hasMoney = true;

        boolean team = this.team.calculateBoolean(event.getUser().getUpgradeLevels());

        if (team) {
            Money teamMoney = Money.getOrCreate(event.getUser().getTeam());
            if (userMoney.getMoney() + teamMoney.getMoney() < cost) hasMoney = false;
        } else if (!userMoney.hasMoney(cost)) hasMoney = false;

        String costString = (hasMoney ? ChatColor.GREEN : ChatColor.RED) + Integer.toString(cost);
        LanguageLookup lookup = event.getUser().getLanguageLookup();

        ItemStack display = event.getDisplay();

        if(team) {
            display = InventoryUtils.addLore(display, lookup.getLocale(teamDescriptionLocale, costString));
        } else {
            display = InventoryUtils.addLore(display, lookup.getLocale(userDescriptionLocale, costString));
        }

        event.setDisplay(display);
    }

    public boolean canBuy(BuyablePurchaseEvent event) {
        return canBuy.calculateBoolean(event.getUser().getUpgradeLevels());
    }

    public int getCost(User user) {
        return (int) cost.calculate(user.getUpgradeLevels());
    }

    @Override
    public void onClick(UserClickItemEvent event) {
        Money userMoney = Money.getOrCreate(event.getUser());
        Money teamMoney = null;

        int cost = getCost(event.getUser());

        boolean team = this.team.calculateBoolean(event.getUser().getUpgradeLevels());

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

        event.getUser().playSound(event.getUser().getLocation(), new SoundEffect(Sound.BLAZE_HIT, 1.0f, 1.0f));

        event.getUser().redoInventory();
    }

    private void doUpgradesOnBuy(User user) {
        for(Map.Entry<String, Calculator> upgrades : upgradeOnBuy.entrySet()) {
            user.setUpgradeLevel(upgrades.getKey(), (int) upgrades.getValue().calculate(user.getUpgradeLevels()));
        }
    }

    public abstract boolean onPurchase(BuyablePurchaseEvent event);

    public boolean buyWithTeamMoney(User user) {
        return team.calculateBoolean(user.getUpgradeLevels());
    }

}
