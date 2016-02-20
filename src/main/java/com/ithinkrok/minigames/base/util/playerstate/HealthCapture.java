package com.ithinkrok.minigames.base.util.playerstate;

import org.bukkit.entity.LivingEntity;

/**
 * Created by paul on 01/01/16.
 */
public class HealthCapture {


    private final double health;
    private final double maxHealth;
    private final int maxAir;
    private final int remainingAir;
    private int hitDelayTicks;

    public HealthCapture(LivingEntity capture){
        health = capture.getHealth();
        maxHealth = capture.getMaxHealth();
        maxAir = capture.getMaximumAir();
        remainingAir = capture.getRemainingAir();
        hitDelayTicks = capture.getMaximumNoDamageTicks();
    }

    public void restore(LivingEntity to){
        to.setMaxHealth(maxHealth);
        to.setHealth(health);

        to.setMaximumAir(maxAir);
        to.setRemainingAir(remainingAir);

        to.setMaximumNoDamageTicks(hitDelayTicks);
    }

    public void setHitDelayTicks(int hitDelayTicks) {
        this.hitDelayTicks = hitDelayTicks;
    }

    public int getHitDelayTicks() {
        return hitDelayTicks;
    }
}
