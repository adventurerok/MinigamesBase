package com.ithinkrok.minigames.api;

import com.ithinkrok.util.config.Config;

/**
 * Created by paul on 06/01/16.
 */
public interface SharedObjectAccessor {

    boolean hasSharedObject(String name);

    Config getSharedObject(String name);

    Config getSharedObjectOrEmpty(String name);
}
