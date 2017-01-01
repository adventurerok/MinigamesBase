package com.ithinkrok.minigames.base.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.map.BaseMapEvent;
import com.ithinkrok.minigames.api.event.map.MapEntityDamagedEvent;
import com.ithinkrok.minigames.api.map.GameMap;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Created by paul on 19/02/16.
 */
public class BaseMapEntityDamagedEvent extends BaseMapEvent implements MapEntityDamagedEvent {

    private final EntityDamageEvent event;

    public BaseMapEntityDamagedEvent(GameGroup gameGroup, GameMap map, EntityDamageEvent event) {
        super(gameGroup, map);
        this.event = event;
    }

    @Override
    public Entity getEntity() {
        return event.getEntity();
    }

    @Override
    public EntityDamageEvent.DamageCause getDamageCause() {
        return event.getCause();
    }

    @Override
    public double getDamage() {
        return event.getDamage();
    }

    @Override
    public double getFinalDamage() {
        return event.getFinalDamage();
    }

    @Override
    public void setDamage(double damage) {
        event.setDamage(damage);
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }
}
