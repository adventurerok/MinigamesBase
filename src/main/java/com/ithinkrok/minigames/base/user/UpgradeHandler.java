package com.ithinkrok.minigames.base.user;

import com.ithinkrok.minigames.api.User;
import com.ithinkrok.minigames.base.event.user.game.UserUpgradeEvent;
import com.ithinkrok.minigames.base.util.math.ExpressionCalculator;
import com.ithinkrok.minigames.base.util.math.Variables;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 03/01/16.
 */
public class UpgradeHandler implements Variables {

    private final User user;
    private final Map<String, Integer> upgradeLevels = new HashMap<>();

    public UpgradeHandler(User user) {
        this.user = user;
    }

    public int getUpgradeLevel(String upgrade) {
        Integer level = upgradeLevels.get(upgrade);

        return level == null ? 0 : level;
    }

    @SuppressWarnings("unchecked")
    public void setUpgradeLevel(String upgrade, int level) {
        if(ExpressionCalculator.isNumber(upgrade))
            throw new RuntimeException("Please do not use numbers as upgrade names");
        if(ExpressionCalculator.isOperatorOrFunction(upgrade))
            throw new RuntimeException(upgrade + " is an operator or function name. It cannot be used for upgrades");

        int oldLevel = getUpgradeLevel(upgrade);
        if (oldLevel == level && upgradeLevels.containsKey(upgrade)) return;

        upgradeLevels.put(upgrade, level);

        UserUpgradeEvent event = new UserUpgradeEvent(user, upgrade, oldLevel, level);

        user.getGameGroup().userEvent(event);
    }

    public void clearUpgrades() {
        upgradeLevels.clear();
    }

    @Override
    public double getVariable(String name) {
        return getUpgradeLevel(name);
    }
}
