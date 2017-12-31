package com.ithinkrok.minigames.util.metadata;

import com.ithinkrok.minigames.api.database.Database;
import com.ithinkrok.minigames.api.metadata.Metadata;
import com.ithinkrok.minigames.api.metadata.MetadataHolder;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.msm.common.economy.Account;
import com.ithinkrok.util.NullReplacements;
import com.ithinkrok.util.config.Config;

import java.math.BigDecimal;

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

    public int getMessageLevel() {
        return messageLevel;
    }

    public void setMessageLevel(int messageLevel) {
        if (this.messageLevel == messageLevel) return;

        this.messageLevel = messageLevel;

        Database database = user.getGameGroup().getDatabase();
        database.setIntUserValue(user, "money_message_level", messageLevel);
    }

    @Override
    protected Account getAccount() {
        return user.getEconomyAccount();
    }

    @Override
    public void addMoney(int amount, boolean message) {
        Account account = getAccount();
        account.deposit("game", BigDecimal.valueOf(amount), "Money.addMoney",
                        result -> {

                            user.updateScoreboard();
                            if (!messageUser(message)) return;
                            user.sendLocale(addMoneyLocale, amount);
                            user.sendLocale(newMoneyLocale, result.getBalanceChange().getNewBalance().intValue());

                        });
    }

    public boolean messageUser(boolean message) {
        return (message ? 1 : 0) + messageLevel >= 2;
    }

    @Override
    public boolean subtractMoney(int amount, boolean message) {
        if (!hasMoney(amount)) return false;

        Account account = getAccount();
        account.withdraw("game", BigDecimal.valueOf(amount), "Money.subtractMoney",
                         result -> {

                             user.updateScoreboard();
                             if (!messageUser(message)) return;
                             user.sendLocale(subtractMoneyLocale, amount);
                             user.sendLocale(newMoneyLocale, result.getBalanceChange().getNewBalance().intValue());

                         });


        return true;
    }

    @Override
    public MetadataHolder<? extends Metadata> getOwner() {
        return user;
    }

}
