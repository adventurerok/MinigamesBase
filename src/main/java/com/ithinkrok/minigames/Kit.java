package com.ithinkrok.minigames;

import com.ithinkrok.minigames.util.io.ListenerLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by paul on 07/01/16.
 */
public class Kit {

    private final String name;
    private final String formattedName;
    private final Collection<ConfigurationSection> listeners;

    public Kit(String name, String formattedName, Collection<ConfigurationSection> listeners) {
        this.name = name;
        this.formattedName = (formattedName != null) ? formattedName : name;
        this.listeners = listeners;
    }

    public String getName() {
        return name;
    }

    public String getFormattedName() {
        return formattedName;
    }

    public Collection<Listener> createListeners(User user) {
        Collection<Listener> result = new ArrayList<>();

        for(ConfigurationSection listenerConfig : listeners) {
            try {
                result.add(ListenerLoader.loadListener(user, this, listenerConfig));
            } catch (Exception e) {
                System.out.println("Failed to create listener for kit: " + name);
                e.printStackTrace();
            }
        }

        return result;
    }
}
