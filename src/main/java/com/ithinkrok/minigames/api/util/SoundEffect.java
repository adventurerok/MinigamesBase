package com.ithinkrok.minigames.api.util;

import com.ithinkrok.util.config.Config;
import org.bukkit.Sound;

/**
 * Created by paul on 03/01/16.
 */
public class SoundEffect {

    private final Sound sound;
    private final float volume;
    private final float pitch;

    public SoundEffect(Config config) {
        this.sound = Sound.valueOf(config.getString("sound").toUpperCase());
        volume = (float) config.getDouble("volume", 1.0);
        pitch = (float) config.getDouble("pitch", 1.0);
    }

    public SoundEffect(String config) {
        String[] parts = config.trim().split(",");

        sound = Sound.valueOf(parts[0].trim().toUpperCase());

        if(parts.length > 1) volume = Float.parseFloat(parts[1].trim());
        else volume = 1.0f;

        if(parts.length > 2) pitch = Float.parseFloat(parts[2].trim());
        else pitch = 1.0f;
    }

    public SoundEffect(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}
