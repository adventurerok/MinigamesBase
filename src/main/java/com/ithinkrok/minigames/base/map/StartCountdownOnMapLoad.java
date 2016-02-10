package com.ithinkrok.minigames.base.map;

import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

/**
 * Created by paul on 04/01/16.
 */
public class StartCountdownOnMapLoad implements CustomListener {

    @CustomEventHandler
    public void onListenerEnabled(ListenerLoadedEvent<GameGroup, GameMap> event) {
        Config config = event.getConfig();

        String name = config.getString("name");
        String localeStub = config.getString("locale_stub");
        int seconds = config.getInt("seconds");

        event.getCreator().startCountdown(name, localeStub, seconds);
    }
}
