package com.ithinkrok.minigames.base.event.map;

import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.map.GameMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Collection;

/**
 * Created by paul on 17/01/16.
 */
public class MapPotionSplashEvent extends MapEvent implements Cancellable {

    private final PotionSplashEvent event;
    private final User thrower;

    public MapPotionSplashEvent(GameGroup gameGroup, GameMap map, PotionSplashEvent event, User thrower) {
        super(gameGroup, map);
        this.event = event;
        this.thrower = thrower;
    }

    public ProjectileSource getThrower() {
        return getPotion().getShooter();
    }

    public User getThrowerUser() {
        return thrower;
    }

    public boolean hasThrowerUser() {
        return getThrowerUser() != null;
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }

    public ThrownPotion getPotion() {
        return event.getPotion();
    }

    public Collection<LivingEntity> getAffected() {
        return event.getAffectedEntities();
    }

    public double getIntensity(LivingEntity entity) {
        return event.getIntensity(entity);
    }

    public void setIntensity(LivingEntity entity, double intensity) {
        event.setIntensity(entity, intensity);
    }


}
