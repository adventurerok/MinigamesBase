package com.ithinkrok.minigames.util.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserAttackEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.math.Calculator;
import com.ithinkrok.util.math.ExpressionCalculator;
import org.bukkit.block.Block;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Created by paul on 04/01/16.
 */
public class LightningWand implements CustomListener {

    private Calculator lightingMultiplier;
    private int maxRange;

    @CustomEventHandler
    public void onListenerEnabled(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfig();

        maxRange = config.getInt("max_range", 200);
        lightingMultiplier = new ExpressionCalculator(config.getString("damage_multiplier", "1"));
    }

    @CustomEventHandler
    public void onUserAttack(UserAttackEvent event) {
        if(event.getInteractType() != UserInteractEvent.InteractType.REPRESENTING) return;

        if(event.getDamageCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            event.setDamage(event.getDamage() * lightingMultiplier.calculate(event.getUser().getUserVariables()));
        }
    }

    @CustomEventHandler
    public void onUserInteract(UserInteractEvent event) {
        if(event.getInteractType() != UserInteractEvent.InteractType.RIGHT_CLICK) return;
        Block target = event.getUser().rayTraceBlocks(maxRange);
        if(target == null) return;

        event.setStartCooldownAfterAction(true);

        LightningStrike strike = event.getUser().getLocation().getWorld().strikeLightning(target.getLocation());
        event.getUser().makeEntityRepresentUser(strike);
    }
}
