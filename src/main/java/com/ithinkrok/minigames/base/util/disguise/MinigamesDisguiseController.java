package com.ithinkrok.minigames.base.util.disguise;

import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.disguise.Disguise;
import com.ithinkrok.minigames.api.util.disguise.DisguiseController;
import org.bukkit.entity.EntityType;

/**
 * Created by paul on 21/01/16.
 */
public class MinigamesDisguiseController implements DisguiseController {


    @Override
    public void disguise(User user, Disguise disguise) {

    }

    @Override
    public void disguise(User user, EntityType type) {
        //Just use LibsDisguises if you want disguise support. This is to prevent NPEs
    }

    @Override
    public void unDisguise(User user) {
        //we also do nothing.
    }
}
