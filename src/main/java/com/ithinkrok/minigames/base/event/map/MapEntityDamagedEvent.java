package com.ithinkrok.minigames.base.event.map;

import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.map.GameMap;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Created by paul on 19/02/16.
 */
public class MapEntityDamagedEvent extends MapEvent {

    private final EntityDamageEvent event;

    private final User attackerUser;

    public MapEntityDamagedEvent(GameGroup gameGroup, GameMap map, EntityDamageEvent event, User attackerUser) {
        super(gameGroup, map);
        this.event = event;
        this.attackerUser = attackerUser;
    }

    public Entity getEntity() {
        return event.getEntity();
    }

    public Entity getAttacker() {
        if(hasAttacker()) return ((EntityDamageByEntityEvent)event).getDamager();
        else return null;
    }

    public boolean hasAttacker() {
        return event instanceof EntityDamageByEntityEvent;
    }

    public EntityDamageEvent.DamageCause getDamageCause() {
        return event.getCause();
    }

    public double getDamage() {
        return event.getDamage();
    }

    public double getFinalDamage() {
        return event.getFinalDamage();
    }

    public void setDamage(double damage) {
        event.setDamage(damage);
    }

    public boolean hasAttackerUser() {
        return attackerUser != null;
    }

    public User getAttackerUser() {
        return attackerUser;
    }
}
