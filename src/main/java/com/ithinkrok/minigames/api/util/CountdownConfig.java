package com.ithinkrok.minigames.api.util;

import com.ithinkrok.util.config.Config;

/**
 * Created by paul on 30/01/16.
 */
public class CountdownConfig {

    private final String name;
    private final int seconds;
    private final String localeStub;

    public CountdownConfig(String name, int seconds, String localeStub) {
        this.name = name;
        this.seconds = seconds;
        this.localeStub = localeStub;
    }

    public CountdownConfig(Config config, String defaultName, int defaultSeconds, String defaultStub) {
        this.name = config.getString("name", defaultName);
        this.seconds = config.getInt("seconds", defaultSeconds);
        this.localeStub = config.getString("locale_stub", defaultStub);
    }

    public CountdownConfig(Config config) {
        this(config, null, 0, null);
    }

    public String getName() {
        return name;
    }

    public int getSeconds() {
        return seconds;
    }

    public String getLocaleStub() {
        return localeStub;
    }
}
