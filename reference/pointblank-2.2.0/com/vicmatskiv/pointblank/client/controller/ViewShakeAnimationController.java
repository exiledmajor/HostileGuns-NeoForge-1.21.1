/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 */
package com.vicmatskiv.pointblank.client.controller;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.controller.PryAnimationController;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import net.minecraft.world.entity.LivingEntity;

public class ViewShakeAnimationController
extends PryAnimationController {
    private final double speed;
    private final double amplitude;
    private double rollAmplitude = 0.0;
    private double yawAmplitude = 0.0;
    private double currentSpeed;
    private double currentAmplitude;
    private long currentNanoDuration;

    public ViewShakeAnimationController(double amplitude, double speed, double decay, long duration) {
        super(duration);
        this.amplitude = amplitude;
        this.speed = speed;
        this.currentSpeed = speed;
        this.currentAmplitude = amplitude;
        this.currentNanoDuration = this.nanoDuration;
    }

    @Override
    protected double getProgress(GunClientState gunClientState, float partialTicks) {
        double progress = (double)(System.nanoTime() - this.startTime) / (double)this.currentNanoDuration;
        if (progress > 1.0) {
            progress = 1.0;
        }
        return progress;
    }

    @Override
    public void onUpdateState(LivingEntity player, GunClientState state) {
        super.onUpdateState(player, state);
        double adjustedProgress = 1.0 - Math.pow(1.0 - this.progress, 0.1);
        this.rollAmplitude = (1.0 - adjustedProgress) * this.rollAmplitude;
        this.yawAmplitude = (1.0 - adjustedProgress) * this.yawAmplitude;
    }

    @Override
    public void reset(double endPitch, double endYaw, double endRoll) {
        this.rollAmplitude = this.currentAmplitude * (1.0 + Math.random());
        if (Math.random() > 0.5) {
            this.rollAmplitude = -this.rollAmplitude;
        }
        this.yawAmplitude = this.currentAmplitude * (1.0 + Math.random());
        if (Math.random() > 0.5) {
            this.yawAmplitude = -this.yawAmplitude;
        }
        super.reset(endPitch, endYaw, endRoll);
    }

    public void reset(FireModeInstance.ViewShakeDescriptor descriptor) {
        if (descriptor != null) {
            this.currentAmplitude = descriptor.amplitude();
            this.currentSpeed = descriptor.speed();
            this.currentNanoDuration = descriptor.duration() * 1000000L;
        } else {
            this.currentAmplitude = this.amplitude;
            this.currentSpeed = this.speed;
            this.currentNanoDuration = this.nanoDuration;
        }
        this.rollAmplitude = this.currentAmplitude * (1.0 + Math.random());
        if (Math.random() > 0.5) {
            this.rollAmplitude = -this.rollAmplitude;
        }
        this.yawAmplitude = this.currentAmplitude * (1.0 + Math.random());
        if (Math.random() > 0.5) {
            this.yawAmplitude = -this.yawAmplitude;
        }
        super.reset(this.pitch, this.yaw, this.roll);
    }

    @Override
    protected double updateRoll(double progress) {
        double angle = progress * this.currentSpeed;
        this.roll = this.rollAmplitude * Math.sin(angle * Math.PI);
        return -this.roll;
    }

    @Override
    protected double updateYaw(double progress) {
        double angle = progress * this.currentSpeed;
        this.yaw = this.yawAmplitude * Math.sin(angle * Math.PI);
        return -this.yaw;
    }
}

