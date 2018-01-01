package com.ithinkrok.minigames.base.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.map.MapWorldInfo;
import org.bukkit.World;

/**
 * Created by paul on 20/02/16.
 */
public interface WorldHandler {

    World loadWorld(GameGroup gameGroup, GameMap map, MapWorldInfo info);

    void unloadWorld(World world);
}
