package com.ithinkrok.minigames.util.gamestate;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.util.ItemGiver;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

import java.util.List;

/**
 * Created by paul on 31/01/16.
 */
public class SimpleInGameListener implements CustomListener {

    protected ItemGiver spectatorItems;

    protected String spectatorJoinLocaleStub;

    protected GameState gameState;

    protected String mapCreditsLocale;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        gameState = event.getRepresenting();

        Config config = event.getConfigOrEmpty();

        spectatorItems = new ItemGiver(config.getConfigOrNull("spectator_items"));
        mapCreditsLocale = config.getString("map_credits_locale", "start.map_credits");
        spectatorJoinLocaleStub = config.getString("spectator_join_locale_stub", "spectator.join");
    }

    @CustomEventHandler
    public void onUserJoined(UserJoinEvent event) {
        if(event.getUser().isInGame()) return;

        event.getUser().teleport(event.getUserGameGroup().getCurrentMap().getSpawn());

        makeUserSpectator(event.getUser());

        List<String> credits = event.getUserGameGroup().getCurrentMap().getInfo().getCredit();

        if(!credits.isEmpty()) {
            event.getUser().sendLocale(mapCreditsLocale);

            for(String credit : credits) {
                event.getUser().sendMessage(credit);
            }
        }

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
