package com.ithinkrok.minigames.api.util;

import com.ithinkrok.util.config.Config;

/**
 * Created by paul on 30/01/16.
 */
public class CountdownConfig {

    private final String name;
    private final int seconds;
    private final String localeStub;

    private SoundEffect tickSound;
    private SoundEffect finishedSound;
    private SoundEffect cancelledSound;


    public CountdownConfig(String name, int seconds, String localeStub) {
        this.name = name;
        this.seconds = seconds;
        this.localeStub = localeStub;
    }

    public CountdownConfig(Config config, String defaultName, int defaultSeconds, String defaultStub) {
        this.name = config.getString("name", defaultName);
        this.seconds = config.getInt("seconds", defaultSeconds);
        this.localeStub = config.getString("locale_stub", defaultStub);

        if(config.contains("tick_sound")) {
            tickSound = MinigamesConfigs.getSoundEffect(config, "tick_sound");
        } else if(config.contains("finished_sound")) {
            finishedSound = MinigamesConfigs.getSoundEffect(config, "finished_sound");
        } else if(config.contains("cancelled_sound")) {
            cancelledSound = MinigamesConfigs.getSoundEffect(config, "cancelled_sound");
        }
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

    public SoundEffect getTickSound() {
        return tickSound;
    }

    public SoundEffect getFinishedSound() {
        return finishedSound;
    }

    public SoundEffect getCancelledSound() {
        return cancelledSound;
    }
}
