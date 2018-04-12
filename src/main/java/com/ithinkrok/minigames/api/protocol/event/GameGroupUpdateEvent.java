package com.ithinkrok.minigames.api.protocol.event;

import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import org.bukkit.event.HandlerList;

/**
 * Created by paul on 16/02/16.
 */
public class GameGroupUpdateEvent extends GameGroupEvent {

    private final GameGroupInfo oldGameGroup;
    private static final HandlerList handlers = new HandlerList();

    public GameGroupUpdateEvent(GameGroupInfo gameGroupInfo, GameGroupInfo oldGameGroup) {
        super(gameGroupInfo);
        this.oldGameGroup = oldGameGroup;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


    public GameGroupInfo getOldGameGroup() {
        return oldGameGroup;
    }
}
