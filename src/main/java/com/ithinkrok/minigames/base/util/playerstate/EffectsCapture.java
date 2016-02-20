package com.ithinkrok.minigames.base.util.playerstate;

import org.bukkit.potion.PotionEffect;

import java.util.Collection;

/**
 * Created by paul on 31/12/15.
 */
public class EffectsCapture {

    private Collection<PotionEffect> potionEffects;
    private int fireTicks;

    public EffectsCapture(Collection<PotionEffect> potionEffects, int fireTicks) {
        this.potionEffects = potionEffects;
        this.fireTicks = fireTicks;
    }

    public Collection<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public int getFireTicks() {
        return fireTicks;
    }

    public void setFireTicks(int fireTicks) {
        this.fireTicks = fireTicks;
    }
}
