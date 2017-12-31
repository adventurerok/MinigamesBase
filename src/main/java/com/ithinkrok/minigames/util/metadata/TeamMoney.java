package com.ithinkrok.minigames.util.metadata;

import com.ithinkrok.minigames.api.metadata.Metadata;
import com.ithinkrok.minigames.api.metadata.MetadataHolder;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.msm.common.economy.Account;
import com.ithinkrok.util.config.Config;

import java.math.BigDecimal;

/**
 * Created by paul on 07/01/16.
 */
public class TeamMoney extends Money {

    private final Team team;

    public TeamMoney(Team team) {
        this.team = team;

        Config config = team.getGameGroup().getSharedObjectOrEmpty("team_money_metadata");

        loadValues(config, "team");
    }

    @Override
    protected Account getAccount() {
        return team.getEconomyAccount();
    }

    @Override
    public void addMoney(int amount, boolean message) {
        Account account = getAccount();
        account.deposit("game", BigDecimal.valueOf(amount), "Money.addMoney",
                        result -> {

                            team.updateUserScoreboards();

                            sendUserLocales(amount, message, addMoneyLocale);

                        });
    }

    private void sendUserLocales(int amount, boolean message, String amountLocale) {
        int money = getMoney();

        for (User user : team.getUsers()) {
            UserMoney userMoney = (UserMoney) Money.getOrCreate(user);
            if (!userMoney.messageUser(message)) continue;

            user.sendLocale(amountLocale, amount);
            user.sendLocale(newMoneyLocale, money);
        }
    }

    @Override
    public boolean subtractMoney(int amount, boolean message) {
        if (!hasMoney(amount)) return false;

        Account account = getAccount();
        account.withdraw("game", BigDecimal.valueOf(amount), "Money.subtractMoney",
                         result -> {

                             team.updateUserScoreboards();

                             sendUserLocales(amount, message, subtractMoneyLocale);

                         });

        return true;
    }

    @Override
    public MetadataHolder<? extends Metadata> getOwner() {
        return team;
    }


}
