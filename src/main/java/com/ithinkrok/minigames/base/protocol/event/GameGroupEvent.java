package com.ithinkrok.minigames.base.protocol.event;

import com.ithinkrok.minigames.base.protocol.data.GameGroupInfo;
import org.bukkit.event.Event;

/**
 * Created by paul on 16/02/16.
 */
public abstract class GameGroupEvent extends Event {

    private final GameGroupInfo gameGroupInfo;

    public GameGroupEvent(GameGroupInfo gameGroupInfo) {
        this.gameGroupInfo = gameGroupInfo;
    }

    public GameGroupInfo getGameGroup() {
        return gameGroupInfo;
    }

}
