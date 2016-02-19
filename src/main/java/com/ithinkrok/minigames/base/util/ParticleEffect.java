package com.ithinkrok.minigames.base.util;

import com.ithinkrok.util.config.Config;
import org.bukkit.Effect;
import org.bukkit.Location;

/**
 * Created by paul on 19/02/16.
 */
public class ParticleEffect {

    private final Effect effect;
    private final int id;
    private final int data;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;
    private final float speed;
    private final int particleCount;
    private final int radius;

    public ParticleEffect(Config config) {
        this.effect = Effect.valueOf(config.getString("effect").toUpperCase());
        this.id = config.getInt("id");
        this.data = config.getInt("data");
        this.offsetX = (float) config.getDouble("offset_x", config.getDouble("r"));
        this.offsetY = (float) config.getDouble("offset_y", config.getDouble("g"));
        this.offsetZ = (float) config.getDouble("offset_z", config.getDouble("b"));
        this.speed = (float) config.getDouble("speed", 1d);
        this.particleCount = config.getInt("count");
        this.radius = config.getInt("radius");
    }

    public void playEffect(Location location) {
        location.getWorld().spigot().playEffect(location, effect, id, data, offsetX, offsetY, offsetZ, speed,
                particleCount, radius);
    }

    public Effect getEffect() {
        return effect;
    }
}
