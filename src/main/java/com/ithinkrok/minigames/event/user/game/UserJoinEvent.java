package com.ithinkrok.minigames.event.user.game;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;

/**
 * Created by paul on 31/12/15.
 */
public class UserJoinEvent extends UserEvent {

    private final JoinReason reason;

    public UserJoinEvent(User user, JoinReason reason) {
        super(user);
        this.reason = reason;
    }

    public JoinReason getReason() {
        return reason;
    }

    public enum JoinReason {
        JOINED_SERVER,
        CHANGED_GAMEGROUP
    }
}
