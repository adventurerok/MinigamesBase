package com.ithinkrok.minigames.base.map;

import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.MinigamesEventHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

/**
 * Created by paul on 04/01/16.
 */
public class StartCountdownOnMapLoad implements Listener {

    @MinigamesEventHandler
    public void onListenerEnabled(ListenerLoadedEvent<GameGroup, GameMap> event) {
        ConfigurationSection config = event.getConfig();

        String name = config.getString("name");
        String localeStub = config.getString("locale_stub");
        int seconds = config.getInt("seconds");

        event.getCreator().startCountdown(name, localeStub, seconds);
    }
}