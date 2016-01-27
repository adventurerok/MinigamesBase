package com.ithinkrok.minigames.base.event.user.game;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.user.UserEvent;

/**
 * Created by paul on 03/01/16.
 */
public class UserUpgradeEvent extends UserEvent {

    private final String upgradeName;
    private final int oldLevel, newLevel;

    public UserUpgradeEvent(User user, String upgradeName, int oldLevel, int newLevel) {
        super(user);
        this.upgradeName = upgradeName;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public String getUpgradeName() {
        return upgradeName;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }
}
