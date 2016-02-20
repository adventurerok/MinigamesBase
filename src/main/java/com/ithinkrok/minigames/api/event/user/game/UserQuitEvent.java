package com.ithinkrok.minigames.api.event.user.game;

import com.ithinkrok.minigames.api.User;
import com.ithinkrok.minigames.api.event.user.UserEvent;

/**
 * Created by paul on 02/01/16.
 */
public class UserQuitEvent extends UserEvent {

    private final QuitReason reason;
    private boolean removeUser = true;

    public UserQuitEvent(User user, QuitReason reason) {
        super(user);
        this.reason = reason;
    }

    public boolean getRemoveUser() {
        return removeUser;
    }

    public void setRemoveUser(boolean removeUser) {
        if(!removeUser && reason != QuitReason.QUIT_SERVER) {
            throw new UnsupportedOperationException("The user can only be not removed if they are quitting the server");
        }

        this.removeUser = removeUser;
    }

    public QuitReason getReason() {
        return reason;
    }

    public enum QuitReason {
        QUIT_SERVER,
        NON_PLAYER_REMOVED,
        CHANGED_GAMEGROUP
    }
}
