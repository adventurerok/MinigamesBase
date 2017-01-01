package com.ithinkrok.minigames.api.event;

import com.ithinkrok.util.event.Cancellable;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Created by paul on 01/01/17.
 *
 * When something is damaging someone. Use a subclass, as this does not provide enough information
 */
public interface DamageEvent extends Cancellable {

    double getDamage();
    double getFinalDamage();

    void setDamage(double damage);

    EntityDamageEvent.DamageCause getDamageCause();

}
