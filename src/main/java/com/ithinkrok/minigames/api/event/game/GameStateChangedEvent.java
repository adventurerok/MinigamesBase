package com.ithinkrok.minigames.api.event.game;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.event.InfoSignEvent;

/**
 * Created by paul on 02/01/16.
 */
public class GameStateChangedEvent extends BaseGameEvent implements InfoSignEvent {

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
