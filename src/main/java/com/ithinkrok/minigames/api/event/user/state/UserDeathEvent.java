package com.ithinkrok.minigames.api.event.user.state;

import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 16/01/16.
 */
public class UserDeathEvent extends UserEvent implements Cancellable {

    private final EntityDamageEvent event;
    private final User killer, assist;

    private boolean playDeathSound = true;

    public UserDeathEvent(User user, EntityDamageEvent event, User killer, User assist) {
        super(user);
        this.event = event;
        this.killer = killer;
        this.assist = assist;
    }

    public User getKillerUser() {
        return killer;
    }

    public Entity getKiller() {
        if(!(event instanceof EntityDamageByEntityEvent)) return null;

        return ((EntityDamageByEntityEvent) event).getDamager();
    }

    public boolean hasKiller() {
        return getKiller() != null;
    }

    public User getAssistUser() {
        return assist;
    }

    public boolean hasKillerUser() {
        return getKillerUser() != null;
    }

    public boolean hasAssistUser() {
        return getAssistUser() != null;
    }

    public void setPlayDeathSound(boolean playDeathSound) {
        this.playDeathSound = playDeathSound;
    }

    public boolean getPlayDeathSound() {
        return playDeathSound;
    }

    public ItemStack getWeapon() {
        if(!hasKillerUser()) return null;

        return getKillerUser().getInventory().getItemInHand();
    }

    public EntityDamageEvent.DamageCause getKillCause() {
        return event.getCause();
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
