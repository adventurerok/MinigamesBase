package com.ithinkrok.minigames.util.disguise;

import com.ithinkrok.minigames.User;
import org.bukkit.entity.EntityType;

/**
 * Created by paul on 21/01/16.
 */
public class MinigamesDisguiseController implements DisguiseController {


    @Override
    public void disguise(User user, EntityType type) {
        //we do nothing. Only DisguiseCraft is support at the moment.
    }

    @Override
    public void unDisguise(User user) {
        //we also do nothing.
    }
}
