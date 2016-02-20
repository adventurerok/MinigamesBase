package com.ithinkrok.minigames.api.protocol.event;

import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import org.bukkit.event.HandlerList;

/**
 * Created by paul on 16/02/16.
 */
public class GameGroupSpawnedEvent extends GameGroupEvent {

    private static final HandlerList handlers = new HandlerList();

    public GameGroupSpawnedEvent(GameGroupInfo gameGroupInfo) {
        super(gameGroupInfo);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
