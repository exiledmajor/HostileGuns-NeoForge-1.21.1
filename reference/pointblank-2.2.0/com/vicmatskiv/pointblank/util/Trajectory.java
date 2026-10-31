/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.util;

import com.vicmatskiv.pointblank.util.TrajectoryPhaseListener;
import net.minecraft.world.phys.Vec3;

public interface Trajectory<P extends Enum<P>> {
    public Vec3 getStartOfTickPosition();

    public Vec3 getEndOfTickPosition();

    public Vec3 getDeltaMovement();

    public void setTargetPosition(Vec3 var1);

    public void tick();

    public boolean isCompleted();

    default public void addListener(TrajectoryPhaseListener<P> listener) {
    }
}

