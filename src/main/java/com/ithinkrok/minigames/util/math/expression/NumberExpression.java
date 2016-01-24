package com.ithinkrok.minigames.util.math.expression;

import com.ithinkrok.minigames.util.math.Variables;

/**
 * Created by paul on 03/01/16.
 */
public class NumberExpression implements Expression {

    private final double number;

    public NumberExpression(double number) {
        this.number = number;
    }

    @Override
    public double calculate(Variables variables) {
        return number;
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NumberExpression that = (NumberExpression) o;

        return Double.compare(that.number, number) == 0;

    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(number);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        return Double.toString(number);
    }
}
