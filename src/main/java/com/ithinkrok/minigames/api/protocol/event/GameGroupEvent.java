package com.ithinkrok.minigames.api.protocol.event;

import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import com.ithinkrok.util.event.CustomEvent;
import org.bukkit.event.Event;

/**
 * Created by paul on 16/02/16.
 */
public abstract class GameGroupEvent extends Event implements CustomEvent {

    private final GameGroupInfo gameGroupInfo;

    public GameGroupEvent(GameGroupInfo gameGroupInfo) {
        this.gameGroupInfo = gameGroupInfo;
    }

    public GameGroupInfo getGameGroup() {
        return gameGroupInfo;
    }

}
