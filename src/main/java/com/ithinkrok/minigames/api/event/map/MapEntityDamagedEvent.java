package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.event.DamageEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Created by paul on 01/01/17.
 */
public interface MapEntityDamagedEvent extends MapEvent, DamageEvent {

    Entity getEntity();

}
