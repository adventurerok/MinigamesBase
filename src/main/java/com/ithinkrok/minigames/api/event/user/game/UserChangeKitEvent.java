package com.ithinkrok.minigames.api.event.user.game;

import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.api.Kit;

/**
 * Created by paul on 08/01/16.
 */
public class UserChangeKitEvent extends UserEvent {

    private final Kit oldKit;
    private final Kit newKit;

    public UserChangeKitEvent(User user, Kit oldKit, Kit newKit) {
        super(user);
        this.oldKit = oldKit;
        this.newKit = newKit;
    }

    public Kit getOldKit() {
        return oldKit;
    }

    public Kit getNewKit() {
        return newKit;
    }
}
