package com.ithinkrok.minigames.util.metadata;

import com.ithinkrok.minigames.api.database.Database;
import com.ithinkrok.minigames.api.metadata.Metadata;
import com.ithinkrok.minigames.api.metadata.MetadataHolder;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.config.Config;

/**
 * Created by paul on 06/01/16.
 */
public class UserMoney extends Money {

    private final User user;

    private int messageLevel = 1;

    public UserMoney(User user) {
        this.user = user;

        Config config = user.getGameGroup().getSharedObjectOrEmpty("user_money_metadata");

        loadValues(config, "user");

        Database database = user.getGameGroup().getDatabase();
        database.getIntUserValue(user, "money_message_level", value -> {
            messageLevel = value;
        }, 1);
    }

    public void setMessageLevel(int messageLevel) {
        if(this.messageLevel == messageLevel) return;

        this.messageLevel = messageLevel;

        Database database = user.getGameGroup().getDatabase();
        database.setIntUserValue(user, "money_message_level", messageLevel);
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
