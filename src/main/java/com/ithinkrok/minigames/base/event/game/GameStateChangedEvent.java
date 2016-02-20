package com.ithinkrok.minigames.base.event.game;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.base.GameState;

/**
 * Created by paul on 02/01/16.
 */
public class GameStateChangedEvent extends GameEvent {

    private final GameState oldGameState;
    private final GameState newGameState;

    public GameStateChangedEvent(GameGroup gameGroup, GameState oldGameState, GameState newGameState) {
        super(gameGroup);
        this.oldGameState = oldGameState;
        this.newGameState = newGameState;
    }

    public GameState getOldGameState() {
        return oldGameState;
    }

    public GameState getNewGameState() {
        return newGameState;
    }
}
