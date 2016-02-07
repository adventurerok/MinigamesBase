package com.ithinkrok.minigames.base.item;

import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.minigames.base.event.user.world.UserInteractEvent;
import org.bukkit.event.Listener;

/**
 * Created by paul on 17/01/16.
 */
public class InvisibleUserToggle implements Listener {

    @CustomEventHandler
    public void onUserInteract(UserInteractEvent event) {
        event.getUser().setShowCloakedUsers(!event.getUser().showCloakedUsers());

        event.setCancelled(true);
        event.setStartCooldownAfterAction(true);
    }
}
