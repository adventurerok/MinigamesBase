package com.ithinkrok.minigames.api.event.game;

import com.ithinkrok.minigames.api.GameGroup;

/**
 * Created by paul on 02/01/16.
 */
public class BaseGameEvent implements GameEvent {

    private final GameGroup gameGroup;

    public BaseGameEvent(GameGroup gameGroup) {
        this.gameGroup = gameGroup;
    }

    @Override
    public GameGroup getGameGroup() {
        return gameGroup;
    }
}
