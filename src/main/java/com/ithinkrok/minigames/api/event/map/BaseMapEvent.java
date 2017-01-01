package com.ithinkrok.minigames.api.event.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.game.BaseGameEvent;
import com.ithinkrok.minigames.api.map.GameMap;

/**
 * Created by paul on 05/01/16.
 */
public class BaseMapEvent extends BaseGameEvent implements MapEvent {

    private final GameMap map;

    public BaseMapEvent(GameGroup gameGroup, GameMap map) {
        super(gameGroup);
        this.map = map;
    }

    @Override
    public GameMap getMap() {
        return map;
    }
}
