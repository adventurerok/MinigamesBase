package com.ithinkrok.minigames.base.gamestate;

import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.GameState;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.MinigamesEventHandler;
import com.ithinkrok.minigames.base.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.base.util.CustomItemGiver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

/**
 * Created by paul on 31/01/16.
 */
public class SimpleInGameListener implements Listener {

    protected CustomItemGiver spectatorItems;

    protected String spectatorJoinLocaleStub;

    protected GameState gameState;

    @MinigamesEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        gameState = event.getRepresenting();

        ConfigurationSection config = event.getConfigOrEmpty();

        spectatorItems = new CustomItemGiver(config.getConfigurationSection("spectator_items"));
        spectatorJoinLocaleStub = config.getString("spectator_join_locale_stub", "spectator.join");
    }

    @MinigamesEventHandler
    public void onUserJoined(UserJoinEvent event) {
        if(event.getUser().isInGame()) return;

        event.getUser().teleport(event.getUserGameGroup().getCurrentMap().getSpawn());

        makeUserSpectator(event.getUser());

        for (int counter = 0; ; ++counter) {
            String message = event.getUserGameGroup().getLocale(spectatorJoinLocaleStub + "." + counter);
            if (message == null) break;

            event.getUser().sendMessage(message);
        }

        event.getUser().doInFuture(task -> event.getUser().setSpectator(true), 2);
    }

    public void makeUserSpectator(User user) {
        user.setSpectator(true);
        spectatorItems.giveToUser(user);
    }
}