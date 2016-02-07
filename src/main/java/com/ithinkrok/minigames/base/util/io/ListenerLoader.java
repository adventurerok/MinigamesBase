package com.ithinkrok.minigames.base.util.io;

import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.util.event.CustomEventExecutor;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 04/01/16.
 */
public class ListenerLoader {

    @SuppressWarnings("unchecked")
    public static <C, R> CustomListener loadListener(C creator, R representing, ConfigurationSection listenerConfig)
            throws Exception {
        String className = listenerConfig.getString("class");

        Class<? extends CustomListener> clazz = (Class<? extends CustomListener>) Class.forName(className);

        CustomListener listener = clazz.newInstance();

        ConfigurationSection config = null;
        if (listenerConfig.contains("config")) config = listenerConfig.getConfigurationSection("config");

        CustomEventExecutor.executeEvent(new ListenerLoadedEvent<>(creator, representing, config), listener);

        return listener;

    }
}
