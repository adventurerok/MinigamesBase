package com.ithinkrok.minigames.base;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

/**
 * Created by paul on 06/01/16.
 */
public interface SharedObjectAccessor {

    MemoryConfiguration EMPTY_CONFIG = new MemoryConfiguration();

    boolean hasSharedObject(String name);

    ConfigurationSection getSharedObject(String name);

    ConfigurationSection getSharedObjectOrEmpty(String name);
}
