package com.ithinkrok.minigames.api.event.game;

import com.ithinkrok.minigames.api.Countdown;
import com.ithinkrok.minigames.api.GameGroup;

/**
 * Created by paul on 04/01/16.
 */
public class CountdownFinishedEvent extends CountdownEvent {

    public CountdownFinishedEvent(GameGroup gameGroup, Countdown countdown) {
        super(gameGroup, countdown);
    }
}
