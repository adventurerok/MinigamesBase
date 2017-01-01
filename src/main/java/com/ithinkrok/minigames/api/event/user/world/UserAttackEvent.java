package com.ithinkrok.minigames.api.event.user.world;

import com.ithinkrok.minigames.api.event.DamageEvent;
import com.ithinkrok.minigames.api.user.User;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 03/01/16.
 */
public class UserAttackEvent extends UserInteractEvent implements DamageEvent {

    private final EntityDamageByEntityEvent event;
    private final User target;
    private final boolean representing;

    public UserAttackEvent(User user, EntityDamageByEntityEvent event, User target, boolean representing) {
        super(user);
        this.event = event;
        this.target = target;
        this.representing = representing;
    }

    @Override
    public double getDamage() {
        return event.getDamage();
    }

    @Override
    public double getFinalDamage() {
        return event.getFinalDamage();
    }

    @Override
    public void setDamage(double damage) {
        event.setDamage(damage);
    }

    @Override
    public EntityDamageEvent.DamageCause getDamageCause() {
        return event.getCause();
    }

    public boolean isAttackingUser() {
        return target != null;
    }

    public User getTargetUser() {
        return target;
    }

    @Override
    public Block getClickedBlock() {
        return null;
    }

    @Override
    public Entity getClickedEntity() {
        return event.getEntity();
    }

    @Override
    public InteractType getInteractType() {
        return representing ? InteractType.REPRESENTING : InteractType.LEFT_CLICK;
    }

    @Override
    public BlockFace getBlockFace() {
        return null;
    }

    @Override
    public ItemStack getItem() {
        return getUser().getInventory().getItemInHand();
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }

}
