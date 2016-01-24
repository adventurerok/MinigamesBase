package com.ithinkrok.minigames.event;

import java.lang.annotation.*;

/**
 * Created by paul on 19/01/16.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MinigamesEventHandler {

    int FIRST = 100;
    int LOWEST = 200;
    int LOWER = 300;
    int LOW = 400;
    int NORMAL = 500;
    int HIGH = 600;
    int HIGHER = 700;
    int HIGHEST = 800;
    int MONITOR = 900;

    int INTERNAL_FIRST = Integer.MIN_VALUE;
    int INTERNAL_LAST = Integer.MAX_VALUE;


    int priority() default NORMAL;

    boolean ignoreCancelled() default false;

}
