/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.compat.playeranimator;

import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationPartGroup;

public record PlayerAnimation<T>(String name, String ownerId, T keyframeAnimation, PlayerAnimationPartGroup group) {
}

