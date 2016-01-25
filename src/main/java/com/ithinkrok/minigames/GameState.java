package com.ithinkrok.minigames;

import com.ithinkrok.minigames.util.io.ListenerLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by paul on 31/12/15.
 */
public class GameState implements Nameable {

    private final String name;
    private final String formattedName;
    private final Collection<ConfigurationSection> listeners;

    public GameState(String name, Collection<ConfigurationSection> listeners) {
        this(name, name, listeners);
    }

    public GameState(String name, String formattedName, Collection<ConfigurationSection> listeners) {
        this.name = name;
        this.formattedName = formattedName;
        this.listeners = listeners;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormattedName() {
        return formattedName;
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
