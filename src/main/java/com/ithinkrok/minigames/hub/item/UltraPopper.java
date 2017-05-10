package com.ithinkrok.minigames.hub.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.NamedSounds;
import com.ithinkrok.minigames.api.util.SoundEffect;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 18/09/16.
 */
public class UltraPopper implements CustomListener {

    double lowSpeed, highSpeed;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?,?> event) {
        Config config = event.getConfigOrEmpty();

        lowSpeed = config.getDouble("low_speed", 0.1);
        highSpeed = config.getDouble("high_speed", 2);
    }


    @CustomEventHandler
    public void onUserRightClick(UserInteractEvent event) {
        User user = event.getUser();

        Arrow arrow = user.launchProjectile(Arrow.class);

        if(event.getInteractType() == UserInteractEvent.InteractType.RIGHT_CLICK) {
            arrow.setVelocity(arrow.getVelocity().multiply(highSpeed));

            event.setStartCooldownAfterAction(true);


        } else if(event.getInteractType() == UserInteractEvent.InteractType.LEFT_CLICK) {
            arrow.setVelocity(arrow.getVelocity().multiply(lowSpeed));
        }

        Location loc = event.getUser().getLocation();
        SoundEffect sound = new SoundEffect(NamedSounds.fromName("ENTITY_ARROW_SHOOT"), 1.0f, 1.0f);
        sound.playToAll(event.getGameGroup(), loc);
    }
}
