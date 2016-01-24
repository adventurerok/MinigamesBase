package com.ithinkrok.minigames.event.user.game;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;

/**
 * Created by paul on 31/12/15.
 *
 * Called after a user changes their isInGame
 */
public class UserInGameChangeEvent extends UserEvent {

    public UserInGameChangeEvent(User user) {
        super(user);
    }

    public boolean isInGame() {
        return getUser().isInGame();
    }
}
