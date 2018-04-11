package com.ithinkrok.minigames.api.user;

import com.ithinkrok.minigames.api.event.user.game.UserVariableChangeEvent;
import com.ithinkrok.util.math.ExpressionCalculator;
import com.ithinkrok.util.math.MutableVariables;
import com.ithinkrok.util.math.Variables;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 03/01/16.
 */
public class UserVariableHandler implements MutableVariables {

    private final User user;
    private final Map<String, Double> variables = new HashMap<>();

    private final Map<String, Variables> customHandlers = new HashMap<>();

    public UserVariableHandler(User user) {
        this.user = user;

        addDefaultHandlers();
    }


    private void addDefaultHandlers() {
        addCustomVariableHandler("mg", new MutableVariables() {
            @Override
            public void setVariable(String name, double value) {
                user.getMinigameSpecificConfig().set(name, value);
            }

            @Override
            public double getVariable(String name) {
                return user.getMinigameSpecificConfig().getDouble(name, 0);
            }
        });

        addCustomVariableHandler("gu", new MutableVariables() {
            @Override
            public void setVariable(String name, double value) {
                user.getGlobalConfig().set(name, value);
            }

            @Override
            public double getVariable(String name) {
                return user.getGlobalConfig().getDouble(name, 0);
            }
        });

        addCustomVariableHandler("global", user.getGameGroup().getGlobalVariables());
    }


    @Override
    public double getVariable(String upgrade) {
        if(upgrade.startsWith("@")) {
            int hashTagIndex = upgrade.indexOf('#');

            if(hashTagIndex > 1 && hashTagIndex < upgrade.length() - 1) {
                String handlerName = upgrade.substring(1, hashTagIndex);
                String varName = upgrade.substring(hashTagIndex + 1);

                if(customHandlers.containsKey(handlerName)) {
                     return (int) customHandlers.get(handlerName).getVariable(varName);
                } else {
                    System.out.println("Custom variable was requested with no handler: " + upgrade);
                    return 0;
                }
            }
        }

        Double level = variables.get(upgrade);

        return level == null ? 0 : level;
    }

    public void addCustomVariableHandler(String name, Variables variables) {
        customHandlers.put(name, variables);
    }

    public void removeCustomVariableHandler(String name) {
        customHandlers.remove(name);
    }

    @SuppressWarnings("unchecked")
    public void setVariable(String upgrade, double level) {
        if(ExpressionCalculator.isNumber(upgrade))
            throw new RuntimeException("Please do not use numbers as variable names");
        if(ExpressionCalculator.isOperatorOrFunction(upgrade))
            throw new RuntimeException(upgrade + " is an operator or function name. It cannot be used for variables");

        double oldLevel = getVariable(upgrade);
        boolean isCustom = upgrade.startsWith("@");
        if (oldLevel == level && (variables.containsKey(upgrade) || isCustom)) return;

        if(isCustom) {
            int hashTagIndex = upgrade.indexOf('#');

            if(hashTagIndex > 1 && hashTagIndex < upgrade.length() - 1) {
                String handlerName = upgrade.substring(1, hashTagIndex);
                String varName = upgrade.substring(hashTagIndex + 1);

                if(customHandlers.containsKey(handlerName)) {
                    Variables handler = customHandlers.get(handlerName);
                    if(!(handler instanceof MutableVariables)) {
                        throw new RuntimeException("Not a mutable variable: " + upgrade + " (tried to upgrade to level " + level + ")");
                    }
                    ((MutableVariables) handler).setVariable(varName, level);
                } else {
                    throw new RuntimeException("Could not update variable " + upgrade + " to " + level + " as no handler");
                }
            }
        } else {
            variables.put(upgrade, level);
        }

        UserVariableChangeEvent event = new UserVariableChangeEvent(user, upgrade, oldLevel, level);

        user.getGameGroup().userEvent(event);
    }

    public void clearUpgrades() {
        variables.clear();
    }

}
