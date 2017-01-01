package com.ithinkrok.minigames.api.event.game;

import com.ithinkrok.minigames.api.Countdown;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.InfoSignEvent;

/**
 * Created by paul on 17/02/16.
 */
public abstract class CountdownEvent extends BaseGameEvent implements InfoSignEvent {

    private final Countdown countdown;


    public CountdownEvent(GameGroup gameGroup, Countdown countdown) {
        super(gameGroup);
        this.countdown = countdown;
    }

    public Countdown getCountdown() {
        return countdown;
    }
}
