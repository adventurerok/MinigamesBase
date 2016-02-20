package com.ithinkrok.minigames.base.event.user.game;

import com.ithinkrok.minigames.base.Kit;
import com.ithinkrok.minigames.api.User;
import com.ithinkrok.minigames.base.event.user.UserEvent;

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
