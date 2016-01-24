package com.ithinkrok.minigames.event.game;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.map.GameMap;

/**
 * Created by paul on 02/01/16.
 */
public class MapChangedEvent extends GameEvent {

    private final GameMap oldMap;
    private final GameMap newMap;

    public MapChangedEvent(GameGroup gameGroup, GameMap oldMap, GameMap newMap) {
        super(gameGroup);
        this.oldMap = oldMap;
        this.newMap = newMap;
    }

    public GameMap getOldMap() {
        return oldMap;
    }

    public GameMap getNewMap() {
        return newMap;
    }
}
