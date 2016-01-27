package com.ithinkrok.minigames.base.util.math.expression;

import com.ithinkrok.minigames.base.util.math.Variables;

/**
 * Created by paul on 03/01/16.
 */
public class VariableExpression implements Expression {

    private final String variable;

    public VariableExpression(String variable) {
        this.variable = variable;
    }

    @Override
    public double calculate(Variables variables) {
        return variables != null ? variables.getVariable(variable) : 0;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public String toString() {
        return variable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariableExpression that = (VariableExpression) o;

        return variable.equals(that.variable);

    }

    @Override
    public int hashCode() {
        return variable.hashCode();
    }
}
