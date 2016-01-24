package com.ithinkrok.minigames.util.disguise;

import com.ithinkrok.minigames.User;
import org.bukkit.entity.EntityType;

/**
 * Created by paul on 21/01/16.
 */
public interface DisguiseController {

    void disguise(User user, EntityType type);
    void unDisguise(User user);
}
