package com.ithinkrok.minigames.base.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.map.BaseMapEvent;
import com.ithinkrok.minigames.api.event.map.MapEntityRegainHealthEvent;
import com.ithinkrok.minigames.api.map.GameMap;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityRegainHealthEvent;

/**
 * Created by paul on 03/01/17.
 */
public class BaseMapEntityRegenHealthEvent extends BaseMapEvent implements MapEntityRegainHealthEvent {

    private final EntityRegainHealthEvent event;

    public BaseMapEntityRegenHealthEvent(GameGroup gameGroup, GameMap map, EntityRegainHealthEvent event) {
        super(gameGroup, map);
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
