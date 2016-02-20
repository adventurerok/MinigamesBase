package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
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
