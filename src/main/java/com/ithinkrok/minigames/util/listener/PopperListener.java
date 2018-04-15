package com.ithinkrok.minigames.util.listener;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.state.UserAttackedEvent;
import com.ithinkrok.minigames.api.item.WeaponStats;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.minigames.api.util.SoundEffect;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PopperListener implements CustomListener {

    private double superPopperPower;
    private String superPopperVictimLocale;
    private String superPopperAttackerLocale;
    private String superPopperPvpLocale;

    private SoundEffect superPopperVictimSound;
    private SoundEffect superPopperAttackerSound;

    private double yGain;
    private boolean useArrowDirection;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, ?> event) {
        Config config = event.getConfigOrEmpty();

        superPopperPower = config.getDouble("power");

        superPopperVictimLocale = config.getString("victim_locale");
        superPopperAttackerLocale = config.getString("attacker_locale");
        superPopperPvpLocale = config.getString("pvp_locale");

        superPopperVictimSound = MinigamesConfigs.getSoundEffect(config, "victim_sound");
        superPopperAttackerSound = MinigamesConfigs.getSoundEffect(config, "attacker_sound");

        yGain = config.getDouble("y_gain", 0.5);
        useArrowDirection = config.getBoolean("use_arrow_direction", false);
    }


    @CustomEventHandler(priority = CustomEventHandler.LOW)
    public void onUserAttackedByUser(UserAttackedEvent event) {
        if (event.getDamageCause() == EntityDamageEvent.DamageCause.PROJECTILE) {

            //Prevent pvp users from being affected by the super popper
            ItemStack held = event.getUser().getInventory().getItemInMainHand();

            if (held != null && WeaponStats.isWeapon(held.getType())) {
                event.getAttackerUser().showAboveHotbarLocale(superPopperPvpLocale);
                event.setCancelled(true);
                return;
            }

            Vector velocity;
            if(!useArrowDirection) {
                velocity = event.getUser().getLocation().getDirection();
            } else {
                velocity = event.getAttacker().getVelocity().normalize();
            }

            velocity.setY(yGain);
            velocity.multiply(superPopperPower);

            event.getUser().setVelocity(velocity);

            event.getUser().showAboveHotbarLocale(superPopperVictimLocale, event.getAttackerUser().getDisplayName());

            if (event.getUser() != event.getAttackerUser()) {
                event.getAttackerUser()
                        .showAboveHotbarLocale(superPopperAttackerLocale, event.getUser().getDisplayName());
            }

            if (superPopperVictimSound != null) {
                event.getUser().playSound(event.getUser().getLocation(), superPopperVictimSound);
            }

            if (superPopperAttackerSound != null) {
                event.getAttackerUser().playSound(event.getAttackerUser().getLocation(), superPopperAttackerSound);
            }

            event.setCancelled(true);
            event.getAttacker().remove();
        }
    }

}
