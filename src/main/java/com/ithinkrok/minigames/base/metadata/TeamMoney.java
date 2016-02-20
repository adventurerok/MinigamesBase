package com.ithinkrok.minigames.base.metadata;

import com.ithinkrok.minigames.api.Team;
import com.ithinkrok.minigames.api.User;
import com.ithinkrok.util.config.Config;

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
    public void addMoney(int amount, boolean message) {
        money += amount;

        team.updateUserScoreboards();

        sendUserLocales(amount, message, addMoneyLocale);
    }

    private void sendUserLocales(int amount, boolean message, String amountLocale) {
        for (User user : team.getUsers()) {
            UserMoney userMoney = (UserMoney) Money.getOrCreate(user);
            if (!userMoney.messageUser(message)) continue;

            user.sendLocale(amountLocale, amount);
            user.sendLocale(newMoneyLocale, money);
        }
    }

    @Override
    public boolean subtractMoney(int amount, boolean message) {
        if(!hasMoney(amount)) return false;

        money -= amount;

        team.updateUserScoreboards();

        sendUserLocales(amount, message, subtractMoneyLocale);
        return true;
    }

    @Override
    public MetadataHolder<? extends Metadata> getOwner() {
        return team;
    }


}
