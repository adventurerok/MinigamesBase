package com.ithinkrok.minigames.base.lang;

/**
 * Created by paul on 01/01/16.
 */
public interface LanguageLookup {

    String getLocale(String name);
    String getLocale(String name, Object... args);
    boolean hasLocale(String name);
}
