package com.ithinkrok.minigames.base;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 06/01/16.
 */
public interface SharedObjectAccessor {

    ConfigurationSection getSharedObject(String name);
}
