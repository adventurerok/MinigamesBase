package com.ithinkrok.minigames.metadata;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.team.Team;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 05/01/16.
 */
public abstract class Money extends UserMetadata {

    protected String removeGameState;
    protected String addMoneyLocale;
    protected String subtractMoneyLocale;
    protected String newMoneyLocale;
    protected int money;

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

    protected void loadValues(ConfigurationSection config) {
        removeGameState = config.getString("remove_gamestate");
        addMoneyLocale = config.getString("add_locale");
        subtractMoneyLocale = config.getString("subtract_locale");
        newMoneyLocale = config.getString("new_locale");
    }

    public int getMoney() {
        return money;
    }

    public boolean hasMoney(int amount) {
        return money >= amount;
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
