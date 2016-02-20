package com.ithinkrok.minigames.base.map;

import com.ithinkrok.minigames.api.GameGroup;
import org.bukkit.World;

/**
 * Created by paul on 20/02/16.
 */
public interface WorldHandler {

    World loadWorld(GameGroup gameGroup, BaseMap map);

    void unloadWorld(BaseMap map);
}
