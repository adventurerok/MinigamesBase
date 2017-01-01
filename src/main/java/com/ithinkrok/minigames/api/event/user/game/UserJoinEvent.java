package com.ithinkrok.minigames.api.event.user.game;

import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.user.User;

/**
 * Created by paul on 31/12/15.
 */
public class UserJoinEvent extends BaseUserEvent {

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
