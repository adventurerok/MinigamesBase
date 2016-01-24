package com.ithinkrok.minigames.util.math;

/**
 * Created by paul on 03/01/16.
 *
 * Calculates a value based on variable input
 */
public interface Calculator {

    double calculate(Variables variables);

    default boolean calculateBoolean(Variables variables) {
        return (int)(Math.floor(calculate(variables))) != 0;
    }
}
