package com.ithinkrok.minigames.hub;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.api.event.user.state.UserDeathEvent;
import com.ithinkrok.minigames.api.sign.InfoSigns;
import com.ithinkrok.minigames.hub.sign.GameChooseSign;
import com.ithinkrok.minigames.hub.sign.HighScoreSign;
import com.ithinkrok.minigames.hub.sign.JoinLobbySign;
import com.ithinkrok.minigames.util.ItemGiver;
import com.ithinkrok.minigames.util.map.SignListener;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;

/**
 * Created by paul on 20/02/16.
 */
public class HubListener extends SignListener {

    static {
        InfoSigns.registerSignType("%lobby_sign%", JoinLobbySign.class, JoinLobbySign::new);
        InfoSigns.registerSignType("%choose_sign%", GameChooseSign.class, GameChooseSign::new);
        InfoSigns.registerSignType("%high_score%", HighScoreSign.class, HighScoreSign::new);
    }


    private ItemGiver itemGiver;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, ?> event) {
        super.onListenerLoaded(event);

        Config config = event.getConfigOrEmpty();

        if(config.contains("items")) {
            itemGiver = new ItemGiver(config.getConfigOrNull("items"));
        }
    }

    @CustomEventHandler
    public void onUserJoin(UserJoinEvent event) {
        if(itemGiver != null) {
            itemGiver.giveToUser(event.getUser());
        }
    }

    @CustomEventHandler
    public void onUserDeath(UserDeathEvent event) {
        event.setCancelled(false);
    }


}
