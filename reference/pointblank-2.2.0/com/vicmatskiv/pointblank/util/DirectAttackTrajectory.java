/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.util;

import com.vicmatskiv.pointblank.util.Trajectory;
import net.minecraft.world.phys.Vec3;

public class DirectAttackTrajectory
implements Trajectory<Phase> {
    private double gravity;
    private Vec3 deltaMovement;
    private Vec3 endOfTickPosition;
    private Vec3 startOfTickPosition;

    public DirectAttackTrajectory(Vec3 startPosition, Vec3 deltaMovement, double gravity) {
        this.deltaMovement = deltaMovement;
        this.startOfTickPosition = startPosition;
        this.endOfTickPosition = startPosition;
        this.gravity = gravity;
    }

    @Override
    public Vec3 getStartOfTickPosition() {
        return this.startOfTickPosition;
    }

    @Override
    public Vec3 getEndOfTickPosition() {
        return this.endOfTickPosition;
    }

    @Override
    public Vec3 getDeltaMovement() {
        return this.deltaMovement;
    }

    @Override
    public void setTargetPosition(Vec3 updatedTargetPosition) {
    }

    @Override
    public void tick() {
        this.startOfTickPosition = this.endOfTickPosition;
        this.deltaMovement = this.deltaMovement.subtract(0.0, this.gravity, 0.0);
        this.endOfTickPosition = this.startOfTickPosition.add(this.deltaMovement);
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    public static enum Phase {
        NONE;

    }
}

