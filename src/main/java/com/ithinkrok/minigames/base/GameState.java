package com.ithinkrok.minigames.base;

import com.ithinkrok.minigames.base.util.io.ListenerLoader;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomListener;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by paul on 31/12/15.
 */
public class GameState implements Nameable {

    private final String name;
    private final String formattedName;
    private final Collection<Config> listeners;

    public GameState(String name, Collection<Config> listeners) {
        this(name, name, listeners);
    }

    public GameState(String name, String formattedName, Collection<Config> listeners) {
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

    public Collection<Config> getListeners() {
        return listeners;
    }

    public Collection<CustomListener> createListeners(GameGroup gameGroup) {
        Collection<CustomListener> result = new ArrayList<>();

        for (Config listenerConfig : listeners) {
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
