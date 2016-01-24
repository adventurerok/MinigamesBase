package com.ithinkrok.minigames.event.user;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.MinigamesEvent;

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
