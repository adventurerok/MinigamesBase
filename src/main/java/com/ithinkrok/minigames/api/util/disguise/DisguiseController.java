package com.ithinkrok.minigames.api.util.disguise;

import com.ithinkrok.minigames.api.user.User;
import org.bukkit.entity.EntityType;

/**
 * Created by paul on 21/01/16.
 */
public interface DisguiseController {

    void disguise(User user, Disguise disguise);
    void disguise(User user, EntityType type);
    void unDisguise(User user);
}
