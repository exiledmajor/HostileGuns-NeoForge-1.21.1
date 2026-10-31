/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  java.lang.MatchException
 *  net.minecraft.client.Minecraft
 */
package com.vicmatskiv.pointblank.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

public enum KeyModifierType {
    NONE,
    ALT,
    CONTROL,
    SHIFT;


    public boolean isDown() {
        if (this == NONE) {
            return true;
        }
        long window = Minecraft.getInstance().getWindow().getWindow();
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> {
                if (InputConstants.isKeyDown((long)window, (int)342) || InputConstants.isKeyDown((long)window, (int)346)) {
                    yield true;
                }
                yield false;
            }
            case 2 -> {
                if (InputConstants.isKeyDown((long)window, (int)341) || InputConstants.isKeyDown((long)window, (int)345)) {
                    yield true;
                }
                yield false;
            }
            case 3 -> {
                if (InputConstants.isKeyDown((long)window, (int)340) || InputConstants.isKeyDown((long)window, (int)344)) {
                    yield true;
                }
                yield false;
            }
            case 0 -> true;
        };
    }

    public static KeyModifierType getActiveModifier() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        if (InputConstants.isKeyDown((long)window, (int)340) || InputConstants.isKeyDown((long)window, (int)344)) {
            return SHIFT;
        }
        if (InputConstants.isKeyDown((long)window, (int)341) || InputConstants.isKeyDown((long)window, (int)345)) {
            return CONTROL;
        }
        if (InputConstants.isKeyDown((long)window, (int)342) || InputConstants.isKeyDown((long)window, (int)346)) {
            return ALT;
        }
        return NONE;
    }
}

