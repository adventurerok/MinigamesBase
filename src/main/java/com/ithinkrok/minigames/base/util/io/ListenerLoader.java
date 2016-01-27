package com.ithinkrok.minigames.base.util.io;

import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.util.EventExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

/**
 * Created by paul on 04/01/16.
 */
public class ListenerLoader {

    @SuppressWarnings("unchecked")
    public static <C, R> Listener loadListener(C creator, R representing, ConfigurationSection listenerConfig) throws Exception {
        String className = listenerConfig.getString("class");

        Class<? extends Listener> clazz = (Class<? extends Listener>) Class.forName(className);

        Listener listener = clazz.newInstance();

        ConfigurationSection config = null;
        if (listenerConfig.contains("config")) config = listenerConfig.getConfigurationSection("config");

        EventExecutor.executeEvent(new ListenerLoadedEvent<>(creator, representing, config), listener);

        return listener;

    }
}
