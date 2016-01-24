package com.ithinkrok.minigames.util.playerstate;

import org.bukkit.entity.Player;

/**
 * Created by paul on 01/01/16.
 */
public class FoodCapture {

    private float exhaustion;
    private float saturation;
    private int foodLevel;

    public FoodCapture(Player capture){
        exhaustion = capture.getExhaustion();
        saturation = capture.getSaturation();
        foodLevel = capture.getFoodLevel();
    }

    public void restore(Player to){
        to.setExhaustion(exhaustion);
        to.setSaturation(saturation);
        to.setFoodLevel(foodLevel);
    }

    public float getExhaustion() {
        return exhaustion;
    }

    public float getSaturation() {
        return saturation;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public void setExhaustion(float exhaustion) {
        this.exhaustion = exhaustion;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }
}
