/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.client;

import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface PositionProvider {
    public Vec3[] getPositionAndDirection();
}

