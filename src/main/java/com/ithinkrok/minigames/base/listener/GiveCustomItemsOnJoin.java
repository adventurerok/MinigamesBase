package com.ithinkrok.minigames.base.listener;

import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.base.util.CustomItemGiver;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

/**
 * Created by paul on 04/01/16.
 */
public class GiveCustomItemsOnJoin implements CustomListener {

    private CustomItemGiver customItemGiver;

    @CustomEventHandler
    public void onListenerEnabled(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfig();

        customItemGiver = new CustomItemGiver(config);
    }

    @CustomEventHandler(priority = CustomEventHandler.LOW)
    public void onUserJoin(UserJoinEvent event) {
        customItemGiver.giveToUser(event.getUser());
    }


}
