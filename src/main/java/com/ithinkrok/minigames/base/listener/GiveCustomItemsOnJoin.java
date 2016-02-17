package com.ithinkrok.minigames.base.listener;

import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.base.util.ItemGiver;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

/**
 * Created by paul on 04/01/16.
 */
public class GiveCustomItemsOnJoin implements CustomListener {

    private ItemGiver itemGiver;

    @CustomEventHandler
    public void onListenerEnabled(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfig();

        itemGiver = new ItemGiver(config);
    }

    @CustomEventHandler(priority = CustomEventHandler.LOW)
    public void onUserJoin(UserJoinEvent event) {
        itemGiver.giveToUser(event.getUser());
    }


}
