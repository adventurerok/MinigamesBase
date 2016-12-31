package com.ithinkrok.minigames.api.event.user.game;

import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.api.user.User;

/**
 * Created by paul on 03/01/16.
 */
public class UserVariableChangeEvent extends UserEvent {

    private final String upgradeName;
    private final double oldLevel, newLevel;

    public UserVariableChangeEvent(User user, String upgradeName, double oldLevel, double newLevel) {
        super(user);
        this.upgradeName = upgradeName;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public String getUpgradeName() {
        return upgradeName;
    }

    public double getOldLevel() {
        return oldLevel;
    }

    public double getNewLevel() {
        return newLevel;
    }
}
