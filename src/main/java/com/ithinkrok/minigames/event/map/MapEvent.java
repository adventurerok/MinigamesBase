package com.ithinkrok.minigames.event.map;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.event.game.GameEvent;
import com.ithinkrok.minigames.map.GameMap;

/**
 * Created by paul on 05/01/16.
 */
public class MapEvent extends GameEvent {

    private final GameMap map;

    public MapEvent(GameGroup gameGroup, GameMap map) {
        super(gameGroup);
        this.map = map;
    }

    public GameMap getMap() {
        return map;
    }
}
