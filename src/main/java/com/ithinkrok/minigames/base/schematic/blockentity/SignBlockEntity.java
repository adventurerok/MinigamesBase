package com.ithinkrok.minigames.base.schematic.blockentity;

import com.ithinkrok.util.config.Config;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/**
 * Created by paul on 19/02/16.
 */
public class SignBlockEntity implements BlockEntity {

    private final String[] lines = new String[4];

    public SignBlockEntity(Config config) {
        lines[0] = config.getString("Text1");
        lines[1] = config.getString("Text2");
        lines[2] = config.getString("Text3");
        lines[3] = config.getString("Text4");
    }


    @Override
    public void paste(Block block, int rotation) {
        if(block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) return;

        Sign sign = (Sign) block.getState();

        for(int index = 0; index < 4; ++index) {
            sign.setLine(index, lines[index]);
        }

        sign.update();
    }
}
