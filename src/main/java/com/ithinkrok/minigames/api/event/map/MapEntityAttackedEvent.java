package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.user.User;
import org.bukkit.entity.Entity;

/**
 * Created by paul on 01/01/17.
 */
public interface MapEntityAttackedEvent extends MapEntityDamagedEvent {

    Entity getAttacker();

    boolean wasAttackedByUser();

    User getAttackerUser();

}
