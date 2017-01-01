package com.ithinkrok.minigames.api.event.user;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.MinigamesEvent;
import com.ithinkrok.minigames.api.user.User;

/**
 * Created by paul on 31/12/15.
 */
public class UserEvent implements MinigamesEvent {

    private final User user;

    public UserEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public GameGroup getGameGroup() {
        return user.getGameGroup();
    }
}
