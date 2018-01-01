package com.ithinkrok.minigames.api.map;

import com.ithinkrok.util.config.Config;
import org.bukkit.World;

import java.util.List;
import java.util.Map;

/**
 * Created by paul on 20/02/16.
 */
public interface GameMapInfo {
    String getName();

    String getConfigName();

    String getDescription();

    Config getConfig();

    List<String> getCredit();

    Map<String, MapWorldInfo> getWorlds();
}
