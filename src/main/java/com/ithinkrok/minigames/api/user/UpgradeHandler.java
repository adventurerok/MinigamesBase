package com.ithinkrok.minigames.api.user;

import com.ithinkrok.minigames.api.event.user.game.UserUpgradeEvent;
import com.ithinkrok.util.math.ExpressionCalculator;
import com.ithinkrok.util.math.Variables;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 03/01/16.
 */
public class UpgradeHandler implements Variables {

    private final User user;
    private final Map<String, Double> upgradeLevels = new HashMap<>();

    private final Map<String, Variables> customLookupHandlers = new HashMap<>();

    public UpgradeHandler(User user) {
        this.user = user;
    }

    public double getUpgradeLevel(String upgrade) {
        if(upgrade.startsWith("@")) {
            int hashTagIndex = upgrade.indexOf('#');

            if(hashTagIndex > 1 && hashTagIndex < upgrade.length() - 1) {
                String handlerName = upgrade.substring(1, hashTagIndex);
                String varName = upgrade.substring(hashTagIndex + 1);

                if(customLookupHandlers.containsKey(handlerName)) {
                     return (int) customLookupHandlers.get(handlerName).getVariable(varName);
                } else return 0;
            }
        }

        Double level = upgradeLevels.get(upgrade);

        return level == null ? 0 : level;
    }

    public void addCustomLevelLookup(String name, Variables variables) {
        customLookupHandlers.put(name, variables);
    }

    public void removeCustomLevelLookup(String name) {
        customLookupHandlers.remove(name);
    }

    @SuppressWarnings("unchecked")
    public void setUpgradeLevel(String upgrade, double level) {
        if(ExpressionCalculator.isNumber(upgrade))
            throw new RuntimeException("Please do not use numbers as upgrade names");
        if(ExpressionCalculator.isOperatorOrFunction(upgrade))
            throw new RuntimeException(upgrade + " is an operator or function name. It cannot be used for upgrades");

        double oldLevel = getUpgradeLevel(upgrade);
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
