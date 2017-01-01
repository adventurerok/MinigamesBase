package com.ithinkrok.minigames.api.event.game;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.InfoSignEvent;
import com.ithinkrok.minigames.api.map.GameMap;

/**
 * Created by paul on 02/01/16.
 */
public class MapChangedEvent extends BaseGameEvent implements InfoSignEvent {

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
