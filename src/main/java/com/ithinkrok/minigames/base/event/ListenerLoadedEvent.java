package com.ithinkrok.minigames.base.event;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

/**
 * Created by paul on 02/01/16.
 *
 * Called on a listener when it is enabled (but not necessarily before it starts receiving events)
 */
public class ListenerLoadedEvent<C, R> implements MinigamesEvent {

    private final MemoryConfiguration EMPTY_CONFIG = new MemoryConfiguration();

    private final C creator;
    private final R representing;
    private final ConfigurationSection config;

    public ListenerLoadedEvent(C creator, R representing, ConfigurationSection config) {
        this.creator = creator;
        this.representing = representing;
        this.config = config;
    }

    public C getCreator() {
        return creator;
    }

    public R getRepresenting() {
        return representing;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public boolean hasConfig() {
        return getConfig() != null;
    }

    public ConfigurationSection getConfigOrEmpty() {
        return config != null ? config : EMPTY_CONFIG;
    }
}
