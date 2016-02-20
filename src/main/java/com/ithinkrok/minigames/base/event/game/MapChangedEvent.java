package com.ithinkrok.minigames.base.event.game;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.base.map.GameMap;

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
