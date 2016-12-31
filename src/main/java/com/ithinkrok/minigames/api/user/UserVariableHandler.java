package com.ithinkrok.minigames.api.user;

import com.ithinkrok.minigames.api.event.user.game.UserVariableChangeEvent;
import com.ithinkrok.util.math.ExpressionCalculator;
import com.ithinkrok.util.math.Variables;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 03/01/16.
 */
public class UserVariableHandler implements Variables {

    private final User user;
    private final Map<String, Double> variables = new HashMap<>();

    private final Map<String, Variables> customLookupHandlers = new HashMap<>();

    public UserVariableHandler(User user) {
        this.user = user;
    }

    @Override
    public double getVariable(String upgrade) {
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

        Double level = variables.get(upgrade);

        return level == null ? 0 : level;
    }

    public void addCustomVariableLookup(String name, Variables variables) {
        customLookupHandlers.put(name, variables);
    }

    public void removeCustomVariableLookup(String name) {
        customLookupHandlers.remove(name);
    }

    @SuppressWarnings("unchecked")
    public void setVariable(String upgrade, double level) {
        if(ExpressionCalculator.isNumber(upgrade))
            throw new RuntimeException("Please do not use numbers as variable names");
        if(ExpressionCalculator.isOperatorOrFunction(upgrade))
            throw new RuntimeException(upgrade + " is an operator or function name. It cannot be used for variables");

        double oldLevel = getVariable(upgrade);
        if (oldLevel == level && variables.containsKey(upgrade)) return;

        variables.put(upgrade, level);

        UserVariableChangeEvent event = new UserVariableChangeEvent(user, upgrade, oldLevel, level);

        user.getGameGroup().userEvent(event);
    }

    public void clearUpgrades() {
        variables.clear();
    }

}
