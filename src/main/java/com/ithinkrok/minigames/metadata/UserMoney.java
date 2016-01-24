package com.ithinkrok.minigames.metadata;

import com.ithinkrok.minigames.User;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 06/01/16.
 */
public class UserMoney extends Money {

    private final User user;

    private int messageLevel = 1;

    public UserMoney(User user) {
        this.user = user;

        ConfigurationSection config = user.getGameGroup().getSharedObject("user_money_metadata");

        loadValues(config);
    }

    public void setMessageLevel(int messageLevel) {
        this.messageLevel = messageLevel;
    }

    public int getMessageLevel() {
        return messageLevel;
    }

    public boolean messageUser(boolean message) {
        return (message ? 1 : 0) + messageLevel >= 2;
    }

    @Override
    public void addMoney(int amount, boolean message) {
        money += amount;

        user.updateScoreboard();
        if(!messageUser(message)) return;
        user.sendLocale(addMoneyLocale, amount);
        user.sendLocale(newMoneyLocale, money);
    }

    @Override
    public boolean subtractMoney(int amount, boolean message) {
        if(!hasMoney(amount)) return false;

        money -= amount;

        user.updateScoreboard();
        if(!messageUser(message)) return true;
        user.sendLocale(subtractMoneyLocale, amount);
        user.sendLocale(newMoneyLocale, money);

        return true;
    }

    @Override
    public MetadataHolder<? extends Metadata> getOwner() {
        return user;
    }

}
