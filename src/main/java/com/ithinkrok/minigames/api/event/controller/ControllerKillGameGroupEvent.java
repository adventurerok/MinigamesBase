package com.ithinkrok.minigames.api.event.controller;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.protocol.event.GameGroupKilledEvent;
import com.ithinkrok.minigames.api.protocol.event.GameGroupUpdateEvent;

/**
 * Created by paul on 20/02/16.
 */
public class ControllerKillGameGroupEvent extends ControllerGameGroupEvent {

    public ControllerKillGameGroupEvent(GameGroup gameGroup, GameGroupKilledEvent event) {
        super(gameGroup, event);
    }
}
