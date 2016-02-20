package com.ithinkrok.minigames.api.event.game;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.Countdown;

/**
 * Created by paul on 17/02/16.
 */
public class CountdownMessageEvent extends CountdownEvent {

    private String message;

    public CountdownMessageEvent(GameGroup gameGroup, Countdown countdown, String message) {
        super(gameGroup, countdown);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
