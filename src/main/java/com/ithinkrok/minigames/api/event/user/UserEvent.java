package com.ithinkrok.minigames.api.event.user;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.event.MinigamesEvent;

/**
 * Created by paul on 31/12/15.
 */
public class UserEvent implements MinigamesEvent {

    private User user;

    public UserEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public GameGroup getUserGameGroup() {
        return user.getGameGroup();
    }
}
