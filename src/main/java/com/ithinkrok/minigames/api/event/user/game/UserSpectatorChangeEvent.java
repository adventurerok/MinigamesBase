package com.ithinkrok.minigames.api.event.user.game;

import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.user.User;

/**
 * Created by paul on 16/01/16.
 */
public class UserSpectatorChangeEvent extends BaseUserEvent {

    private final boolean isSpectator;

    public UserSpectatorChangeEvent(User user, boolean isSpectator) {
        super(user);
        this.isSpectator = isSpectator;
    }

    public boolean isSpectator() {
        return isSpectator;
    }

    public boolean wasSpectator() {
        return !isSpectator;
    }
}
