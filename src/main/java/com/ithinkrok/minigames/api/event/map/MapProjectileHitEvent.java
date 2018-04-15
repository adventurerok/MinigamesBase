package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.EntityUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;

public class MapProjectileHitEvent extends BaseMapEvent {

    private final ProjectileHitEvent event;


    public MapProjectileHitEvent(GameGroup gameGroup, GameMap map, ProjectileHitEvent event) {
        super(gameGroup, map);
        this.event = event;
    }

    public Projectile getProjectile() {
        return event.getEntity();
    }

    public Block getHitBlock(){
        return event.getHitBlock();
    }

    public Entity getHitEntity() {
        return event.getHitEntity();
    }

    public User getHitUser() {
        return getHitEntity() != null ? EntityUtils.getActualUser(getGameGroup(), getHitEntity()) : null;
    }
}
