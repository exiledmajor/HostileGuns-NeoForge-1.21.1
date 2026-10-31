/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.util;

import com.vicmatskiv.pointblank.util.Trajectory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface TrajectoryProvider {
    public Trajectory<?> createTrajectory(Level var1, Vec3 var2, Vec3 var3);
}

