package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by paul on 24/12/16.
 */
public class MapEntityDeathEvent extends BaseMapEvent {

    private final EntityDeathEvent event;

    public MapEntityDeathEvent(GameGroup gameGroup, GameMap map, EntityDeathEvent event) {
        super(gameGroup, map);
        this.event = event;
    }

    public int getDroppedExp() {
        return event.getDroppedExp();
    }

    public List<ItemStack> getDrops() {
        return event.getDrops();
    }

    public LivingEntity getEntity() {
        return event.getEntity();
    }

    public void setDroppedExp(int exp) {
        event.setDroppedExp(exp);
    }
}
