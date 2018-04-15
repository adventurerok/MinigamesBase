package com.ithinkrok.minigames.base.util.playerstate;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/**
 * Created by paul on 31/12/15.
 */
public class PlayerState {

    private ArmorCapture armorCapture;
    private InventoryCapture inventoryCapture;
    private EffectsCapture effectsCapture;
    private ModeCapture modeCapture;
    private HealthCapture healthCapture;
    private FoodCapture foodCapture;
    private NameCapture nameCapture;

    /**
     * The entity that is placeholding for the player. Can be null
     */
    private LivingEntity placeholder;

    public void capture(LivingEntity entity) {
        capture(entity, CaptureParts.ALL);
    }

    public void capture(LivingEntity entity, CaptureParts... captureParts) {
        if (captureParts[0] == CaptureParts.ALL) {
            capture(entity, CaptureParts.values());
            return;
        }

        if (ArrayUtils.contains(captureParts, CaptureParts.ARMOR)) {
            armorCapture = new ArmorCapture(entity.getEquipment().getArmorContents());
        }

        if (ArrayUtils.contains(captureParts, CaptureParts.INVENTORY) && (entity instanceof Player)) {
            inventoryCapture = new InventoryCapture(this, ((Player) entity).getInventory().getContents());
        }

        if (ArrayUtils.contains(captureParts, CaptureParts.EFFECTS)) {
            effectsCapture = new EffectsCapture(entity.getActivePotionEffects(), entity.getFireTicks());
        }

        if (ArrayUtils.contains(captureParts, CaptureParts.MODE) && (entity instanceof Player)) {
            modeCapture = new ModeCapture((Player) entity);
        }

        if (ArrayUtils.contains(captureParts, CaptureParts.HEALTH)) {
            healthCapture = new HealthCapture(entity);
        }

        if (ArrayUtils.contains(captureParts, CaptureParts.FOOD) && (entity instanceof Player)) {
            foodCapture = new FoodCapture((Player) entity);
        }

        if(ArrayUtils.contains(captureParts, CaptureParts.NAME)) {
            if(nameCapture == null) nameCapture = new NameCapture();
            nameCapture.capture(entity);
        }
    }

    public void restore(LivingEntity entity, CaptureParts... captureParts) {
        if (captureParts[0] == CaptureParts.ALL) {
            restore(entity, CaptureParts.values());
            return;
        }

        if (armorCapture != null && ArrayUtils.contains(captureParts, CaptureParts.ARMOR)) {
            entity.getEquipment().setArmorContents(armorCapture.getArmorContents());
        }

        if (inventoryCapture != null && ArrayUtils.contains(captureParts, CaptureParts.INVENTORY) &&
                (entity instanceof Player)) {
            ((Player) entity).getInventory().setContents(inventoryCapture.getContents());
        }

        if (effectsCapture != null && ArrayUtils.contains(captureParts, CaptureParts.EFFECTS)) {
            for (PotionEffect effect : effectsCapture.getPotionEffects()) {
                entity.addPotionEffect(effect, true);
            }

            entity.setFireTicks(effectsCapture.getFireTicks());
        }

        if (modeCapture != null && ArrayUtils.contains(captureParts, CaptureParts.MODE) && (entity instanceof Player)) {
            modeCapture.restore((Player) entity);
        }

        if (healthCapture != null && ArrayUtils.contains(captureParts, CaptureParts.HEALTH)) {
            healthCapture.restore(entity);
        }

        if (foodCapture != null && ArrayUtils.contains(captureParts, CaptureParts.FOOD) && (entity instanceof Player)) {
            foodCapture.restore((Player) entity);
        }

        if(nameCapture != null && ArrayUtils.contains(captureParts, CaptureParts.NAME)) {
            nameCapture.restore(entity);
        }
    }

    public ArmorCapture getEquipment() {
        return armorCapture;
    }

    public InventoryCapture getInventory() {
        return inventoryCapture;
    }

    public void restore(LivingEntity entity) {
        restore(entity, CaptureParts.ALL);
    }

    public LivingEntity getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(LivingEntity placeholder) {
        this.placeholder = placeholder;
    }

    public float getExp() {
        return modeCapture.getExp();
    }

    public void setExp(float exp) {
        modeCapture.setExp(exp);
    }

    public float getFlySpeed() {
        return modeCapture.getFlySpeed();
    }

    public void setFlySpeed(float flySpeed) {
        modeCapture.setFlySpeed(flySpeed);
    }

    public float getWalkSpeed() {
        return modeCapture.getWalkSpeed();
    }

    public void setWalkSpeed(float walkSpeed) {
        modeCapture.setWalkSpeed(walkSpeed);
    }

    public int getLevel() {
        return modeCapture.getLevel();
    }

    public void setLevel(int level) {
        modeCapture.setLevel(level);
    }

    public boolean getAllowFlight() {
        return modeCapture.getAllowFlight();
    }

    public void setAllowFlight(boolean allowFlight) {
        modeCapture.setAllowFlight(allowFlight);
    }

    public GameMode getGameMode() {
        return modeCapture.getGameMode();
    }

    public void setGameMode(GameMode gameMode) {
        modeCapture.setGameMode(gameMode);
    }

    public int getFireTicks() {
        return effectsCapture.getFireTicks();
    }

    public void setFireTicks(int fireTicks) {
        effectsCapture.setFireTicks(fireTicks);
    }

    public void setFoodLevel(int foodLevel) {
        foodCapture.setFoodLevel(foodLevel);
    }

    public void setSaturation(float saturation) {
        foodCapture.setSaturation(saturation);
    }

    public String getTabListName() {
        return nameCapture.getTabListName();
    }

    public void setTabListName(String name) {
        nameCapture.setTabListName(name);
    }

    public void setHitDelayTicks(int ticks) {
        healthCapture.setHitDelayTicks(ticks);
    }

    public int getHitDelayTicks() {
        return healthCapture.getHitDelayTicks();
    }

    public enum CaptureParts {
        INVENTORY,
        ARMOR,
        EFFECTS,
        MODE,
        HEALTH,
        FOOD,
        NAME,
        ALL
    }

    //private static class
}
