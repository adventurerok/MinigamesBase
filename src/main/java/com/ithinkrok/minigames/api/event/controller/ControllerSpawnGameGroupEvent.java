package com.ithinkrok.minigames.api.event.controller;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.protocol.event.GameGroupSpawnedEvent;

/**
 * Created by paul on 20/02/16.
 */
public class ControllerSpawnGameGroupEvent extends ControllerGameGroupEvent {

    public ControllerSpawnGameGroupEvent(GameGroup gameGroup, GameGroupSpawnedEvent event) {
        super(gameGroup, event);
    }
}
