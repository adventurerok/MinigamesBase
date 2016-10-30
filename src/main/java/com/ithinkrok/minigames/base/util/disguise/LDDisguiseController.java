package com.ithinkrok.minigames.base.util.disguise;

import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.disguise.DisguiseController;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.*;
import org.bukkit.entity.EntityType;

/**
 * Created by paul on 30/01/16.
 */
public class LDDisguiseController implements DisguiseController {

    @Override
    public void disguise(User user, com.ithinkrok.minigames.api.util.disguise.Disguise disguise) {
        DisguiseType disguiseType = DisguiseType.getType(disguise.getEntityType());

        Disguise libsDisguise;

        if (disguiseType.isPlayer()) {
            libsDisguise = new PlayerDisguise(disguise.getPlayerName(), disguise.getPlayerSkin());
        } else if (disguiseType.isMob()) {
            libsDisguise = new MobDisguise(disguiseType);

            if (disguise.isShowUserNameAboveEntity()) {
                libsDisguise.getWatcher().setCustomName(user.getName());
            }
        } else if (disguiseType.isMisc()) {
            if (disguise.getBlockMaterial() == null) {
                libsDisguise = new MiscDisguise(disguiseType);
            } else {
                libsDisguise =
                        new MiscDisguise(disguiseType, disguise.getBlockMaterial().getId(), disguise.getBlockData());
            }
        } else throw new RuntimeException("Unsupported disguise: " + disguiseType);

        libsDisguise.setViewSelfDisguise(disguise.isViewSelfDisguise());
        libsDisguise.setModifyBoundingBox(disguise.isModifyBoundingBox());
        libsDisguise.setReplaceSounds(disguise.isReplaceSounds());

        libsDisguise.setShowName(disguise.isShowName());


        DisguiseAPI.disguiseToAll(user.getEntity(), libsDisguise);
    }

    @Override
    public void disguise(User user, EntityType type) {
        DisguiseType disguiseType = DisguiseType.getType(type);

        Disguise disguise = disguiseType.isMob() ? new MobDisguise(disguiseType) : new MiscDisguise(disguiseType);

        DisguiseAPI.disguiseToAll(user.getEntity(), disguise);
    }

    @Override
    public void unDisguise(User user) {
        DisguiseAPI.undisguiseToAll(user.getEntity());
    }
}
