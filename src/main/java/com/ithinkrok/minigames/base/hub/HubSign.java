package com.ithinkrok.minigames.base.hub;

import org.bukkit.Location;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Created by paul on 16/02/16.
 */
public class HubSign {

    private final Location location;

    private String gameGroupType;

    private boolean spectatorSign = false;

    public HubSign(SignChangeEvent event) {
        location = event.getBlock().getLocation();

        gameGroupType = event.getLine(1);

        spectatorSign = event.getLine(2).equalsIgnoreCase("spectators");
    }
}
