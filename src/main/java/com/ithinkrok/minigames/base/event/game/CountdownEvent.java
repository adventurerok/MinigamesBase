package com.ithinkrok.minigames.base.event.game;

import com.ithinkrok.minigames.base.Countdown;
import com.ithinkrok.minigames.api.GameGroup;

/**
 * Created by paul on 17/02/16.
 */
public abstract class CountdownEvent extends GameEvent {

    private final Countdown countdown;


    public CountdownEvent(GameGroup gameGroup, Countdown countdown) {
        super(gameGroup);
        this.countdown = countdown;
    }

    public Countdown getCountdown() {
        return countdown;
    }
}
