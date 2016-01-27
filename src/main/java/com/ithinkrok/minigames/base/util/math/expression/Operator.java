package com.ithinkrok.minigames.base.util.math.expression;

/**
 * Created by paul on 20/01/16.
 */
public class Operator {
    private final String name;
    private final Executor executor;
    private final boolean isFunction;
    private final boolean isDynamic;
    private final int maxArguments;
    private final int minArguments;
    private final int precedence;


    public Operator(String name, Executor executor, boolean isFunction, boolean isDynamic, int precedence, int minArguments,
                    int maxArguments) {
        this.name = name;
        this.executor = executor;
        this.isFunction = isFunction;
        this.isDynamic = isDynamic;
        this.maxArguments = maxArguments;
        this.minArguments = minArguments;
        this.precedence = precedence;
    }

    public String getName() {
        return name;
    }

    public Executor getExecutor() {
        return executor;
    }

    public boolean isFunction() {
        return isFunction;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public int getMaxArguments() {
        return maxArguments;
    }

    public int getMinArguments() {
        return minArguments;
    }

    public int getPrecedence() {
        return precedence;
    }

    /**
     * Created by paul on 20/01/16.
     */
    public interface Executor {
        double operate(double... numbers);
    }
}
