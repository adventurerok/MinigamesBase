package com.ithinkrok.minigames.base.util.io;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventExecutor;
import com.ithinkrok.util.event.CustomListener;

/**
 * Created by paul on 04/01/16.
 */
public class ListenerLoader {

    @SuppressWarnings("unchecked")
    public static <C, R> CustomListener loadListener(C creator, R representing, Config listenerConfig)
            throws Exception {
        String className = listenerConfig.getString("class");

        Class<? extends CustomListener> clazz = (Class<? extends CustomListener>) Class.forName(className);

        CustomListener listener = clazz.newInstance();

        Config config = listenerConfig.getConfigOrEmpty("config");

        CustomEventExecutor.executeEvent(new ListenerLoadedEvent<>(creator, representing, config), listener);

        return listener;

    }
}
