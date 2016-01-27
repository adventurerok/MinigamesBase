package com.ithinkrok.minigames.base.lang;

/**
 * Created by paul on 01/01/16.
 */
public class NullLanguageLookup implements LanguageLookup {
    @Override
    public String getLocale(String name) {
        return null;
    }

    @Override
    public String getLocale(String name, Object... args) {
        return null;
    }

    @Override
    public boolean hasLocale(String name) {
        return false;
    }
}
