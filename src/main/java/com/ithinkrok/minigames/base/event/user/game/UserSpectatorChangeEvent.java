package com.ithinkrok.minigames.base.event.user.game;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.user.UserEvent;

/**
 * Created by paul on 16/01/16.
 */
public class UserSpectatorChangeEvent extends UserEvent {

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
