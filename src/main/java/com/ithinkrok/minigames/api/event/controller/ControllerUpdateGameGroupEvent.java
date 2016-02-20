package com.ithinkrok.minigames.api.event.controller;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.protocol.event.GameGroupSpawnedEvent;
import com.ithinkrok.minigames.api.protocol.event.GameGroupUpdateEvent;

/**
 * Created by paul on 20/02/16.
 */
public class ControllerUpdateGameGroupEvent extends ControllerGameGroupEvent {

    public ControllerUpdateGameGroupEvent(GameGroup gameGroup, GameGroupUpdateEvent event) {
        super(gameGroup, event);
    }
}
