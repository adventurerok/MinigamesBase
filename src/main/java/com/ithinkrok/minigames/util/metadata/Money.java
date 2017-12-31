package com.ithinkrok.minigames.util.metadata;

import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.api.metadata.Metadata;
import com.ithinkrok.minigames.api.metadata.MetadataHolder;
import com.ithinkrok.minigames.api.metadata.UserMetadata;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.msm.common.economy.Account;
import com.ithinkrok.msm.common.economy.result.Balance;
import com.ithinkrok.util.config.Config;

import java.util.Optional;

/**
 * Created by paul on 05/01/16.
 */
public abstract class Money extends UserMetadata {

    protected String removeGameState;
    protected String addMoneyLocale;
    protected String subtractMoneyLocale;
    protected String newMoneyLocale;


    public static Money getOrCreate(MetadataHolder<? super UserMetadata> holder) {
        if (holder.hasMetadata(Money.class)) return holder.getMetadata(Money.class);

        Money money;
        if (holder instanceof User) {
            money = new UserMoney((User) holder);
        } else if(holder instanceof Team){
            money = new TeamMoney((Team)holder);
        } else {
            throw new RuntimeException("Unsupported MetadataHolder type:" + holder.getClass());
        }

        holder.setMetadata(money);
        return money;
    }

    protected void loadValues(Config config, String type) {
        removeGameState = config.getString("remove_gamestate", null);
        addMoneyLocale = config.getString("add_locale", "money.balance." + type + ".add");
        subtractMoneyLocale = config.getString("subtract_locale", "money.balance." + type + ".subtract");
        newMoneyLocale = config.getString("new_locale", "money.balance." + type + ".new");
    }

    protected abstract Account getAccount();

    public int getMoney() {
        Optional<Balance> bal = getAccount().getBalance("game");

        if(bal.isPresent()) {
            return bal.get().getAmountAsInt();
        } else {
            return 0;
        }
    }

    public boolean hasMoney(int amount) {
        return getMoney() >= amount;
    }

    public abstract void addMoney(int amount, boolean message);

    public abstract boolean subtractMoney(int amount, boolean message);

    public abstract MetadataHolder<? extends Metadata> getOwner();

    @Override
    public boolean removeOnInGameChange(UserInGameChangeEvent event) {
        return !event.isInGame();
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return false;
    }

    @Override
    public Class<? extends UserMetadata> getMetadataClass() {
        return Money.class;
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return event.getNewGameState().getName().equals(removeGameState);
    }
}
