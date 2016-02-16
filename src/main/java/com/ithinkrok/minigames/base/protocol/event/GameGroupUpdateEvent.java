package com.ithinkrok.minigames.base.protocol.event;

import com.ithinkrok.minigames.base.protocol.data.GameGroupInfo;
import org.bukkit.event.HandlerList;

/**
 * Created by paul on 16/02/16.
 */
public class GameGroupUpdateEvent extends GameGroupEvent {

    private static final HandlerList handlers = new HandlerList();

    public GameGroupUpdateEvent(GameGroupInfo gameGroupInfo) {
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
