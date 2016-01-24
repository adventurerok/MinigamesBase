package com.ithinkrok.minigames.event.game;

import com.ithinkrok.minigames.Countdown;
import com.ithinkrok.minigames.GameGroup;

/**
 * Created by paul on 04/01/16.
 */
public class CountdownFinishedEvent extends GameEvent {

    private final Countdown countdown;

    public CountdownFinishedEvent(GameGroup gameGroup, Countdown countdown) {
        super(gameGroup);
        this.countdown = countdown;
    }

    public Countdown getCountdown() {
        return countdown;
    }
}
