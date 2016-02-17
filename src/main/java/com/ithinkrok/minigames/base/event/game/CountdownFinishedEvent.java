package com.ithinkrok.minigames.base.event.game;

import com.ithinkrok.minigames.base.Countdown;
import com.ithinkrok.minigames.base.GameGroup;

/**
 * Created by paul on 04/01/16.
 */
public class CountdownFinishedEvent extends CountdownEvent {

    public CountdownFinishedEvent(GameGroup gameGroup, Countdown countdown) {
        super(gameGroup, countdown);
    }
}
