package com.ithinkrok.minigames.api.util;

import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by paul on 01/03/16.
 */
public class NamedSounds {

    private static final Map<String, Sound> NAMED_SOUNDS = new HashMap<>();

    static {
        String mcVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];

        int minorVersion = Integer.parseInt(mcVersion.split("\\.")[1]);

        if (minorVersion < 9) {
            load1_8Sounds();
        } else {
            load1_9Sounds();
        }
    }

    private static void load1_9Sounds() {
        for (Sound sound : Sound.values()) {
            NAMED_SOUNDS.put(sound.toString(), sound);
        }
    }

    private static void load1_8Sounds() {

        NAMED_SOUNDS.put("AMBIENT_CAVE", Sound.valueOf("AMBIENCE_CAVE"));
        NAMED_SOUNDS.put("WEATHER_RAIN", Sound.valueOf("AMBIENCE_RAIN"));
        NAMED_SOUNDS.put("ENTITY_LIGHTNING_THUNDER", Sound.valueOf("AMBIENCE_THUNDER"));

        NAMED_SOUNDS.put("BLOCK_ANVIL_BREAK", Sound.valueOf("ANVIL_BREAK"));
        NAMED_SOUNDS.put("BLOCK_ANVIL_LAND", Sound.valueOf("ANVIL_LAND"));

        NAMED_SOUNDS.put("ENTITY_GENERIC_EXPLODE", Sound.valueOf("EXPLODE"));

        NAMED_SOUNDS.put("ENTITY_EXPERIENCE_ORB_PICKUP", Sound.valueOf("ORB_PICKUP"));

        NAMED_SOUNDS.put("ENTITY_PLAYER_HURT", Sound.valueOf("HURT_FLESH"));
        NAMED_SOUNDS.put("ENTITY_PLAYER_LEVELUP", Sound.valueOf("LEVEL_UP"));

        NAMED_SOUNDS.put("ENTITY_ZOMBIE_VILLAGER_CURE", Sound.valueOf("ZOMBIE_UNFECT"));

        NAMED_SOUNDS.put("ENTITY_BLAZE_HURT", Sound.valueOf("BLAZE_HIT"));

        NAMED_SOUNDS.put("ENTITY_ARROW_SHOOT", Sound.valueOf("SHOOT_ARROW"));

        NAMED_SOUNDS.put("ENTITY_WITHER_SPAWN", Sound.valueOf("WITHER_SPAWN"));
        NAMED_SOUNDS.put("ENTITY_WITHER_SHOOT", Sound.valueOf("WITHER_SHOOT"));

        NAMED_SOUNDS.put("ENTITY_FIREWORK_TWINKLE", Sound.valueOf("FIREWORK_TWINKLE"));
    }

    public static Sound fromName(String name) {
        return NAMED_SOUNDS.get(name.toUpperCase());
    }

    public static Set<String> names() {
        return NAMED_SOUNDS.keySet();
    }

    public static Collection<Sound> values() {
        return NAMED_SOUNDS.values();
    }
}
