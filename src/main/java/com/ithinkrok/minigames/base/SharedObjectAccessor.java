package com.ithinkrok.minigames.base;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 06/01/16.
 */
public interface SharedObjectAccessor {

    boolean hasSharedObject(String name);

    ConfigurationSection getSharedObject(String name);

    ConfigurationSection getSharedObjectOrEmpty(String name);
}
