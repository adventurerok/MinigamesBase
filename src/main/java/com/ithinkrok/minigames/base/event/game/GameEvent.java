package com.ithinkrok.minigames.base.event.game;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.base.event.MinigamesEvent;

/**
 * Created by paul on 02/01/16.
 */
public class GameEvent implements MinigamesEvent {

    private final GameGroup gameGroup;

    public GameEvent(GameGroup gameGroup) {
        this.gameGroup = gameGroup;
    }

    public GameGroup getGameGroup() {
        return gameGroup;
    }
}
