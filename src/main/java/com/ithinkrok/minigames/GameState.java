package com.ithinkrok.minigames;

import com.ithinkrok.minigames.util.io.ListenerLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by paul on 31/12/15.
 */
public class GameState {

    private final String name;
    private final Collection<ConfigurationSection> listeners;

    public GameState(String name, Collection<ConfigurationSection> listeners) {
        this.name = name;
        this.listeners = listeners;
    }

    public String getName() {
        return name;
    }

    public Collection<ConfigurationSection> getListeners() {
        return listeners;
    }

    public Collection<Listener> createListeners(GameGroup gameGroup) {
        Collection<Listener> result = new ArrayList<>();

        for (ConfigurationSection listenerConfig : listeners) {
            try {
                result.add(ListenerLoader.loadListener(gameGroup, this, listenerConfig));
            } catch (Exception e) {
                System.out.println("Failed to create listener for gamestate: " + name);
                e.printStackTrace();
            }
        }

        return result;
    }
}
