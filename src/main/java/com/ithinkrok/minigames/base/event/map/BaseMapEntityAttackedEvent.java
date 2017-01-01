package com.ithinkrok.minigames.base.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.map.MapEntityAttackedEvent;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.user.User;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Created by paul on 01/01/17.
 */
public class BaseMapEntityAttackedEvent extends BaseMapEntityDamagedEvent implements MapEntityAttackedEvent {

    private final EntityDamageByEntityEvent event;

    private final User attackerUser;

    public BaseMapEntityAttackedEvent(GameGroup gameGroup, GameMap map, EntityDamageByEntityEvent event, User
            attackerUser) {
        super(gameGroup, map, event);

        this.event = event;
        this.attackerUser = attackerUser;
    }

    @Override
    public Entity getAttacker() {
        return event.getDamager();
    }

    @Override
    public boolean wasAttackedByUser() {
        return attackerUser != null;
    }

    @Override
    public User getAttackerUser() {
        return attackerUser;
    }


}
