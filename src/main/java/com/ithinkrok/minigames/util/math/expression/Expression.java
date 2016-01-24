package com.ithinkrok.minigames.util.math.expression;

import com.ithinkrok.minigames.util.math.Calculator;

/**
 * Created by paul on 03/01/16.
 */
public interface Expression extends Calculator{

    /**
     *
     * @return If calculate() will always return the same result regardless of the variables
     */
    boolean isStatic();
}
