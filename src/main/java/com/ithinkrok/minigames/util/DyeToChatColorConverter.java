package com.ithinkrok.minigames.util;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by paul on 24/11/15.
 *
 * Converts DyeColors to ChatColors
 */
public class DyeToChatColorConverter {

    private static Map<DyeColor, ChatColor> dyeChatMap = new EnumMap<>(DyeColor.class);

    static {
        dyeChatMap.put(DyeColor.BLACK, ChatColor.DARK_GRAY);
        dyeChatMap.put(DyeColor.BLUE, ChatColor.DARK_BLUE);
        dyeChatMap.put(DyeColor.BROWN, ChatColor.GOLD);
        dyeChatMap.put(DyeColor.CYAN, ChatColor.AQUA);
        dyeChatMap.put(DyeColor.GRAY, ChatColor.GRAY);
        dyeChatMap.put(DyeColor.GREEN, ChatColor.DARK_GREEN);
        dyeChatMap.put(DyeColor.LIGHT_BLUE, ChatColor.BLUE);
        dyeChatMap.put(DyeColor.LIME, ChatColor.GREEN);
        dyeChatMap.put(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE);
        dyeChatMap.put(DyeColor.ORANGE, ChatColor.GOLD);
        dyeChatMap.put(DyeColor.PINK, ChatColor.LIGHT_PURPLE);
        dyeChatMap.put(DyeColor.PURPLE, ChatColor.DARK_PURPLE);
        dyeChatMap.put(DyeColor.RED, ChatColor.RED);
        dyeChatMap.put(DyeColor.SILVER, ChatColor.GRAY);
        dyeChatMap.put(DyeColor.WHITE, ChatColor.WHITE);
        dyeChatMap.put(DyeColor.YELLOW, ChatColor.YELLOW);
    }

    public static ChatColor convert(DyeColor dyeColor){
        return dyeChatMap.get(dyeColor);
    }
}
