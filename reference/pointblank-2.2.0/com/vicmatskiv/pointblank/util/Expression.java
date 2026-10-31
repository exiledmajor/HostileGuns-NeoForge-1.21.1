/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.function.TriFunction
 */
package com.vicmatskiv.pointblank.util;

import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.function.TriFunction;

public class Expression {
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("^(\\w+)\\s*(<=|>=|<|>|==)\\s*(-?\\d+(?:\\.\\d+)?)$");

    public static Predicate<ConditionContext> compile(String expression) {
        Matcher matcher = EXPRESSION_PATTERN.matcher(expression);
        if (matcher.find()) {
            TriFunction tf;
            String varName = matcher.group(1);
            if (varName.equals("reloadIterationIndex")) {
                tf = (player, gunState, itemStack) -> gunState.getReloadIterationIndex();
            } else if (varName.equals("ammoCount")) {
                tf = (player, gunState, itemStack) -> gunState.getAmmoCount(GunItem.getFireModeInstance(itemStack));
            } else {
                throw new IllegalArgumentException("Unknown variable: " + varName);
            }
            String op = matcher.group(2);
            double value = Double.parseDouble(matcher.group(3));
            switch (op) {
                case "<": {
                    return context -> ((Number)tf.apply((Object)context.player(), (Object)context.gunClientState(), (Object)context.currentItemStack())).doubleValue() < value;
                }
                case ">": {
                    return context -> ((Number)tf.apply((Object)context.player(), (Object)context.gunClientState(), (Object)context.currentItemStack())).doubleValue() > value;
                }
                case "<=": {
                    return context -> ((Number)tf.apply((Object)context.player(), (Object)context.gunClientState(), (Object)context.currentItemStack())).doubleValue() <= value;
                }
                case ">=": {
                    return context -> ((Number)tf.apply((Object)context.player(), (Object)context.gunClientState(), (Object)context.currentItemStack())).doubleValue() >= value;
                }
                case "==": {
                    return context -> ((Number)tf.apply((Object)context.player(), (Object)context.gunClientState(), (Object)context.currentItemStack())).doubleValue() == value;
                }
            }
        }
        throw new IllegalArgumentException("Invalid expression: " + expression);
    }
}

