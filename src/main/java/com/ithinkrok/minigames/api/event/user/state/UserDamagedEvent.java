package com.ithinkrok.minigames.api.event.user.state;

import com.ithinkrok.minigames.api.event.map.MapEntityDamagedEvent;
import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Created by paul on 02/01/16.
 */
public class UserDamagedEvent extends BaseUserEvent implements Cancellable, MapEntityDamagedEvent {

    private final EntityDamageEvent event;

    public UserDamagedEvent(User user, EntityDamageEvent event) {
        super(user);
        this.event = event;
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }

    @Override
    public Entity getEntity() {
        return event.getEntity();
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

    @Override
    public GameMap getMap() {
        return getGameGroup().getCurrentMap();
    }
}
