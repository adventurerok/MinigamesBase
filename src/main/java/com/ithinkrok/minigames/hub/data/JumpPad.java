package com.ithinkrok.minigames.hub.data;

import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.minigames.api.util.SoundEffect;
import com.ithinkrok.util.config.Config;
import org.bukkit.Material;

/**
 * Created by paul on 17/09/16.
 */
public class JumpPad {

    private final Material material;
    private final double power;
    private final SoundEffect sound;

    public JumpPad(Config config) {

        material = Material.matchMaterial(config.getString("material"));
        power = config.getDouble("power");
        sound = MinigamesConfigs.getSoundEffect(config, "sound");
    }

    public Material getMaterial() {
        return material;
    }

    public double getPower() {
        return power;
    }

    public SoundEffect getSound() {
        return sound;
    }
}
