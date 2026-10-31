/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.util;

import net.minecraft.world.phys.Vec3;

public interface TrajectoryPhaseListener<T extends Enum<T>> {
    public void onStartPhase(T var1, Vec3 var2);
}

