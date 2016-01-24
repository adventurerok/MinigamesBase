package com.ithinkrok.minigames.event.user.game;

import com.ithinkrok.minigames.Kit;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;

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
