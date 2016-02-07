package com.ithinkrok.minigames.base;

import com.ithinkrok.minigames.base.util.io.ListenerLoader;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.configuration.ConfigurationSection;

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

    public Collection<CustomListener> createListeners(GameGroup gameGroup) {
        Collection<CustomListener> result = new ArrayList<>();

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
