package com.ithinkrok.minigames.api.event.user.game;

import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.user.User;

/**
 * Created by paul on 31/12/15.
 *
 * Called after a user changes their isInGame
 */
public class UserInGameChangeEvent extends BaseUserEvent {

    public UserInGameChangeEvent(User user) {
        super(user);
    }

    public boolean isInGame() {
        return getUser().isInGame();
    }
}
