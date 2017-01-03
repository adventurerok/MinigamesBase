package com.ithinkrok.minigames.api.event.user.state;

import com.ithinkrok.minigames.api.event.map.MapEntityRegainHealthEvent;
import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.user.User;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityRegainHealthEvent;

/**
 * Created by paul on 03/01/17.
 */
public class UserRegainHealthEvent extends BaseUserEvent implements MapEntityRegainHealthEvent {

    private final EntityRegainHealthEvent event;

    public UserRegainHealthEvent(User user, EntityRegainHealthEvent event) {
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
    public GameMap getMap() {
        return getGameGroup().getCurrentMap();
    }

    @Override
    public Entity getEntity() {
        return event.getEntity();
    }

    @Override
    public double getAmount() {
        return event.getAmount();
    }

    @Override
    public void setAmount(double amount) {
        event.setAmount(amount);
    }

    @Override
    public EntityRegainHealthEvent.RegainReason getRegainReason() {
        return event.getRegainReason();
    }
}
