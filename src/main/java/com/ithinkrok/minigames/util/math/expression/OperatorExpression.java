package com.ithinkrok.minigames.util.math.expression;

import com.ithinkrok.minigames.util.math.Variables;

import java.util.Arrays;
import java.util.List;

/**
 * Created by paul on 03/01/16.
 */
public class OperatorExpression implements Expression {

    private final Expression[] subExpressions;
    private final Operator operator;
    private final boolean dynamic;

    public OperatorExpression(Operator operator, boolean dynamic, boolean simplify, List<Expression> expressions) {
        this(operator, dynamic, simplify, toArray(expressions));
    }

    private static Expression[] toArray(List<Expression> expressions) {
        Expression[] array = new Expression[expressions.size()];
        return expressions.toArray(array);
    }

    public OperatorExpression(Operator operator, boolean dynamic, boolean simplify, Expression... expressions) {
        this.dynamic = dynamic;
        subExpressions = expressions;

        if(simplify) {
            for (int i = 0; i < subExpressions.length; ++i) {
                if (subExpressions[i].isStatic())
                    subExpressions[i] = new NumberExpression(subExpressions[i].calculate(null));
            }
        }

        this.operator = operator;
    }

    @Override
    public double calculate(Variables variables) {
        double[] numbers = new double[subExpressions.length];

        for(int index = 0; index < subExpressions.length; ++index) {
            numbers[index] = subExpressions[index].calculate(variables);
        }

        return operator.getExecutor().operate(numbers);
    }

    @Override
    public boolean isStatic() {
        if(dynamic) return false;

        for(Expression expression : subExpressions) {
            if(!expression.isStatic()) return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperatorExpression that = (OperatorExpression) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(subExpressions, that.subExpressions)) return false;
        return operator.equals(that.operator);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(subExpressions);
        result = 31 * result + operator.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if(operator.isFunction()) {
            result.append(operator.getName()).append('(');

            boolean appendComma = false;

            for(Expression expression :  subExpressions) {
                if(appendComma) {
                    result.append(',');
                } else appendComma = true;

                result.append('(').append(expression.toString()).append(')');
            }
            result.append(')');
        } else {
            if(subExpressions.length == 2) {
                result.append('(').append(subExpressions[0].toString()).append(')');
            }
            result.append(operator.getName());
            result.append('(').append(subExpressions[subExpressions.length - 1].toString()).append(')');
        }

        return result.toString();
    }
}
