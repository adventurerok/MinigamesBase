package com.ithinkrok.minigames.base.item;

import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

/**
 * Created by paul on 17/01/16.
 */
public class InvisibleUserToggle implements CustomListener {

    @CustomEventHandler
    public void onUserInteract(UserInteractEvent event) {
        event.getUser().setShowCloakedUsers(!event.getUser().showCloakedUsers());

        event.setCancelled(true);
        event.setStartCooldownAfterAction(true);
    }
}
