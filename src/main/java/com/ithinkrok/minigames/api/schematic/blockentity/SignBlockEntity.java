package com.ithinkrok.minigames.api.schematic.blockentity;

import com.ithinkrok.util.config.Config;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by paul on 19/02/16.
 */
public class SignBlockEntity implements BlockEntity {

    private final String[] lines = new String[4];

    public SignBlockEntity(Config config) {
        lines[0] = replaceMinecraftJSONWithChatCodes(config.getString("Text1"));
        lines[1] = replaceMinecraftJSONWithChatCodes(config.getString("Text2"));
        lines[2] = replaceMinecraftJSONWithChatCodes(config.getString("Text3"));
        lines[3] = replaceMinecraftJSONWithChatCodes(config.getString("Text4"));
    }

    private String replaceMinecraftJSONWithChatCodes(String text) {
        if(text.equals("\"\"")) return "";

        Pattern pattern = Pattern.compile("\\{\"extra\":\\[\"(\\w+)\"\\],\"text\":\"\"\\}");
        Matcher matcher = pattern.matcher(text);

        if(!matcher.find()) return text;

        return matcher.group(1);
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
