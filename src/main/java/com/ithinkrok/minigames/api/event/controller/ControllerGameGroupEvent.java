package com.ithinkrok.minigames.api.event.controller;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.InfoSignEvent;
import com.ithinkrok.minigames.api.event.game.BaseGameEvent;
import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import com.ithinkrok.minigames.api.protocol.event.GameGroupEvent;

/**
 * Created by paul on 20/02/16.
 */
public abstract class ControllerGameGroupEvent extends BaseGameEvent implements InfoSignEvent {

    private final GameGroupEvent event;

    public ControllerGameGroupEvent(GameGroup gameGroup, GameGroupEvent event) {
        super(gameGroup);
        this.event = event;
    }

    public GameGroupInfo getControllerGameGroup() {
        return event.getGameGroup();
    }


}
