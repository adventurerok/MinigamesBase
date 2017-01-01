package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.event.game.GameEvent;
import com.ithinkrok.minigames.api.map.GameMap;

/**
 * Created by paul on 01/01/17.
 */
public interface MapEvent extends GameEvent {
    GameMap getMap();
}
