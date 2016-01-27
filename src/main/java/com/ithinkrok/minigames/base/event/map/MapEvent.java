package com.ithinkrok.minigames.base.event.map;

import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.map.GameMap;
import com.ithinkrok.minigames.base.event.game.GameEvent;

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
