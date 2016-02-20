package com.ithinkrok.minigames.api.util.disguise;

import com.ithinkrok.minigames.api.user.User;
import org.bukkit.entity.EntityType;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

/**
 * Created by paul on 21/01/16.
 */
public class DCDisguiseController implements DisguiseController {

    private final DisguiseCraftAPI dcAPI = DisguiseCraft.getAPI();

    @Override
    public void disguise(User user, com.ithinkrok.minigames.api.util.disguise.Disguise disguise) {
        disguise(user, disguise.getEntityType());
    }

    @Override
    public void disguise(User user, EntityType type) {
        if(!user.isPlayer()) return;

        //Uses a hack that will convert most EntityTypes to DisguiseTypes. DOES NOT WORK FOR ALL (e.g. TNT)
        Disguise disguise = new Disguise(dcAPI.newEntityID(), DisguiseType.fromString(type.name().replace("_", "")));

        if(dcAPI.isDisguised(user.getPlayer())) dcAPI.changePlayerDisguise(user.getPlayer(), disguise);
        else dcAPI.disguisePlayer(user.getPlayer(), disguise);
    }

    @Override
    public void unDisguise(User user) {
        if(!user.isPlayer()) return;

        if(dcAPI.isDisguised(user.getPlayer())){
            dcAPI.undisguisePlayer(user.getPlayer());
        }
    }
}
