package com.ithinkrok.minigames.base.util.disguise;

import com.ithinkrok.minigames.base.User;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.entity.EntityType;

/**
 * Created by paul on 30/01/16.
 */
public class LDDisguiseController implements DisguiseController {

    @Override
    public void disguise(User user, EntityType type) {
        DisguiseType disguiseType = DisguiseType.getType(type);

        Disguise disguise = disguiseType.isMob() ? new MobDisguise(disguiseType) : new MiscDisguise(disguiseType);

        DisguiseAPI.disguiseEntity(user.getEntity(), disguise);
    }

    @Override
    public void unDisguise(User user) {
        DisguiseAPI.undisguiseToAll(user.getEntity());
    }
}
