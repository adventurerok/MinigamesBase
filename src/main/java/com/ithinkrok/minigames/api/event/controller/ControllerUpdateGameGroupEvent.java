package com.ithinkrok.minigames.api.event.controller;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import com.ithinkrok.minigames.api.protocol.event.GameGroupUpdateEvent;

/**
 * Created by paul on 20/02/16.
 */
public class ControllerUpdateGameGroupEvent extends ControllerGameGroupEvent {

    private final GameGroupUpdateEvent event;


    public ControllerUpdateGameGroupEvent(GameGroup gameGroup, GameGroupUpdateEvent event) {
        super(gameGroup, event);
        this.event = event;
    }


    /**
     *
     * @return A copy of the game group object with the changes from this update removed
     */
    public GameGroupInfo getOldControllerGameGroup(){
        return event.getOldGameGroup();
    }
}
