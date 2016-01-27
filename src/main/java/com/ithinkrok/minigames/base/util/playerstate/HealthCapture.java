package com.ithinkrok.minigames.base.util.playerstate;

import org.bukkit.entity.LivingEntity;

/**
 * Created by paul on 01/01/16.
 */
public class HealthCapture {


    private double health;
    private double maxHealth;
    private int maxAir;
    private int remainingAir;

    public HealthCapture(LivingEntity capture){
        health = capture.getHealth();
        maxHealth = capture.getMaxHealth();
        maxAir = capture.getMaximumAir();
        remainingAir = capture.getRemainingAir();
    }

    public void restore(LivingEntity to){
        to.setMaxHealth(maxHealth);
        to.setHealth(health);

        to.setMaximumAir(maxAir);
        to.setRemainingAir(remainingAir);
    }
}
