package com.ithinkrok.minigames.api.schematic.blockentity;

import com.ithinkrok.minigames.api.schematic.Facing;
import com.ithinkrok.util.config.Config;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;

/**
 * Created by paul on 19/02/16.
 */
public class SkullBlockEntity implements BlockEntity {

    private final SkullType type;
    private final int rotation;

    private final String ownerName;

    public SkullBlockEntity(Config config) {
        int skullTypeId = config.getInt("SkullType");

        this.type = getSkullType(skullTypeId);

        this.rotation = config.getInt("Rot");

        if(config.contains("Owner")) {
            ownerName = config.getConfigOrNull("Owner").getString("Name");
        } else {
            ownerName = config.getString("ExtraType");
        }
    }

    private SkullType getSkullType(int skullTypeId) {
        SkullType type;

        switch (skullTypeId) {
            case 0:
                type = SkullType.SKELETON;
                break;
            case 1:
                type = SkullType.WITHER;
                break;
            case 2:
                type = SkullType.ZOMBIE;
                break;
            case 3:
                type = SkullType.PLAYER;
                break;
            case 4:
                type = SkullType.CREEPER;
                break;
            default:
                type = null;
        }
        return type;
    }

    @Override
    public void paste(Block block, int rotation) {
        if(block.getType() != Material.SKULL || type == null) return;

        Skull skull = (Skull) block.getState();

        skull.setSkullType(type);

        if(type == SkullType.PLAYER) {
            skull.setOwner(ownerName);
        }

        skull.setRotation(Facing.signRotationToFacing(Facing.rotateSign(this.rotation, rotation)));
    }
}
