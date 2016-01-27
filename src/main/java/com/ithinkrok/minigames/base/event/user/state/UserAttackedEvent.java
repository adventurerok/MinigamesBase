package com.ithinkrok.minigames.base.event.user.state;

import com.ithinkrok.minigames.base.User;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 03/01/16.
 */
public class UserAttackedEvent extends UserDamagedEvent {

    private final EntityDamageByEntityEvent event;
    private final User attacker;

    public UserAttackedEvent(User user, EntityDamageByEntityEvent event, User attacker) {
        super(user, event);
        this.event = event;
        this.attacker = attacker;
    }

    public Entity getAttacker() {
        return event.getDamager();
    }

    public boolean wasAttackedByUser() {
        return attacker != null;
    }

    public User getAttackerUser() {
        return attacker;
    }

    public ItemStack getWeapon() {
        if(attacker == null || getDamageCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return null;
        return attacker.getInventory().getItemInHand();
    }
}
