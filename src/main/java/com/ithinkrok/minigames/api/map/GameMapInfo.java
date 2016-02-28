package com.ithinkrok.minigames.api.map;

import com.ithinkrok.util.config.Config;
import org.bukkit.World;

import java.util.List;

/**
 * Created by paul on 20/02/16.
 */
public interface GameMapInfo {
    String getName();

    String getConfigName();

    String getDescription();

    boolean getWeatherEnabled();

    Config getConfig();

    String getMapFolder();

    List<String> getCredit();

    World.Environment getEnvironment();

    MapType getMapType();
}
