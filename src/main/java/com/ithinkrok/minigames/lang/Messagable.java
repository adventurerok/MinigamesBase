package com.ithinkrok.minigames.lang;

/**
 * Created by paul on 02/01/16.
 */
public interface Messagable {

    void sendMessage(String message);
    void sendMessageNoPrefix(String message);
    void sendLocale(String locale, Object... args);
    void sendLocaleNoPrefix(String locale, Object... args);
    LanguageLookup getLanguageLookup();
}
