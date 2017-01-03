package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.util.event.Cancellable;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityRegainHealthEvent;

/**
 * Created by paul on 03/01/17.
 */
public interface MapEntityRegainHealthEvent extends MapEvent, Cancellable {

    Entity getEntity();

    double getAmount();

    void setAmount(double amount);

    EntityRegainHealthEvent.RegainReason getRegainReason();

}
