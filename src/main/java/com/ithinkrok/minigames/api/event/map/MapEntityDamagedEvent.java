package com.ithinkrok.minigames.api.event.map;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Created by paul on 01/01/17.
 */
public interface MapEntityDamagedEvent extends MapEvent {

    Entity getEntity();

    EntityDamageEvent.DamageCause getDamageCause();

    double getDamage();

    double getFinalDamage();

    void setDamage(double damage);
}
