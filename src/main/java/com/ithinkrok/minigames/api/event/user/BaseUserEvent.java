package com.ithinkrok.minigames.api.event.user;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.user.User;

/**
 * Created by paul on 31/12/15.
 */
public class BaseUserEvent implements UserEvent {

    private final User user;

    public BaseUserEvent(User user) {
        this.user = user;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public GameGroup getGameGroup() {
        return user.getGameGroup();
    }
}
