/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.client.controller;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.GunStateListener;
import com.vicmatskiv.pointblank.client.RealtimeLinearEaser;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class InertiaController
implements GunStateListener {
    private static final double MIN_DELTA_TIME_SECONDS = 1.0E-4;
    private double previousYaw;
    private double targetYawTiltAngle;
    private double previousPitch;
    private double targetPitchTiltAngle;
    private double maxTiltAngle;
    private double maxRollAngle;
    private double tiltFactor;
    private double smoothingFactor;
    private double dampingFactor = 0.01;
    private float threshold = 0.01f;
    private long lastUpdateTime;
    private double roll;
    private double pitch;
    private double yaw;
    private float dynamicModifier;
    private RealtimeLinearEaser dynamicModifierEaser = new RealtimeLinearEaser(1000L);

    public InertiaController(double tiltFactor, double smoothingFactor, double maxTiltAngle) {
        this(tiltFactor, smoothingFactor, maxTiltAngle, maxTiltAngle);
    }

    public InertiaController(double tiltFactor, double smoothingFactor, double maxTiltAngle, double maxRollAngle) {
        this.lastUpdateTime = System.nanoTime();
        this.tiltFactor = tiltFactor;
        this.smoothingFactor = smoothingFactor;
        this.maxTiltAngle = maxTiltAngle;
        this.maxRollAngle = maxRollAngle;
        this.reset();
    }

    @Override
    public void onUpdateState(LivingEntity player, GunClientState state) {
        long currentTime = System.nanoTime();
        double deltaTime = (double)(currentTime - this.lastUpdateTime) / 1.0E9;
        this.lastUpdateTime = currentTime;
        float currentYaw = player.getYRot();
        double deltaYaw = Mth.wrapDegrees((float)((float)((double)currentYaw - this.previousYaw)));
        double safeDeltaTime = Math.max(deltaTime, 1.0E-4);
        double yawTurnSpeed = deltaYaw / safeDeltaTime;
        float currentPitch = player.getXRot();
        double deltaPitch = Mth.wrapDegrees((float)((float)((double)currentPitch - this.previousPitch)));
        double pitchTurnSpeed = deltaPitch / safeDeltaTime;
        double scaledTiltFactor = this.tiltFactor * (double)this.dynamicModifierEaser.update(this.dynamicModifier);
        double yawInterpolationSpeed = InertiaController.clampInterpolationFactor(this.smoothingFactor * safeDeltaTime);
        if (Math.abs(yawTurnSpeed) >= (double)this.threshold) {
            this.targetYawTiltAngle = yawTurnSpeed * (state.isAiming() ? 0.2 : 1.0) * scaledTiltFactor;
        } else {
            this.targetYawTiltAngle *= this.dampingFactor;
            yawInterpolationSpeed = InertiaController.clampInterpolationFactor(yawInterpolationSpeed * 90.0);
        }
        double pitchInterpolationSpeed = InertiaController.clampInterpolationFactor(this.smoothingFactor * safeDeltaTime);
        if (Math.abs(pitchTurnSpeed) >= (double)this.threshold) {
            this.targetPitchTiltAngle = pitchTurnSpeed * (state.isAiming() ? 0.2 : 1.0) * scaledTiltFactor;
        } else {
            this.targetPitchTiltAngle *= this.dampingFactor;
            pitchInterpolationSpeed = InertiaController.clampInterpolationFactor(pitchInterpolationSpeed * 90.0);
        }
        this.targetYawTiltAngle = Mth.clamp((double)this.targetYawTiltAngle, (double)(-this.maxTiltAngle), (double)this.maxTiltAngle);
        this.targetPitchTiltAngle = Mth.clamp((double)this.targetPitchTiltAngle, (double)(-this.maxTiltAngle), (double)this.maxTiltAngle);
        this.yaw = InertiaController.integrateTowards(this.yaw, this.targetYawTiltAngle, yawInterpolationSpeed, this.maxTiltAngle);
        this.pitch = InertiaController.integrateTowards(this.pitch, this.targetPitchTiltAngle, pitchInterpolationSpeed, this.maxTiltAngle);
        this.roll = InertiaController.integrateTowards(this.roll, this.targetYawTiltAngle, yawInterpolationSpeed, this.maxRollAngle);
        this.previousYaw = currentYaw;
        this.previousPitch = currentPitch;
    }

    @Override
    public void onRenderTick(LivingEntity player, GunClientState state, ItemStack itemStack, ItemDisplayContext itemDisplayContext, float partialTicks) {
    }

    public void reset() {
        this.yaw = 0.0;
        this.pitch = 0.0;
        this.roll = 0.0;
        this.targetYawTiltAngle = 0.0;
        this.targetPitchTiltAngle = 0.0;
        this.previousYaw = 0.0;
        this.previousPitch = 0.0;
        this.dynamicModifier = 1.0f;
        this.lastUpdateTime = System.nanoTime();
    }

    public void reset(Player player) {
        this.yaw = 0.0;
        this.pitch = 0.0;
        this.roll = 0.0;
        this.targetYawTiltAngle = 0.0;
        this.targetPitchTiltAngle = 0.0;
        this.previousYaw = player.getYRot();
        this.previousPitch = player.getXRot();
        this.dynamicModifier = 1.0f;
        this.lastUpdateTime = System.nanoTime();
    }

    public double getRoll() {
        return this.roll;
    }

    public double getPitch() {
        return this.pitch;
    }

    public double getYaw() {
        return this.yaw;
    }

    void setTiltFactor(double tiltFactor) {
        this.tiltFactor = tiltFactor;
    }

    void setSmoothingFactor(double smoothingFactor) {
        this.smoothingFactor = smoothingFactor;
    }

    void setMaxTiltAngle(double maxTiltAngle) {
        this.maxTiltAngle = maxTiltAngle;
    }

    void setMaxRollAngle(double maxRollAngle) {
        this.maxRollAngle = maxRollAngle;
    }

    public void setDynamicModifier(float dynamicModifier) {
        if (dynamicModifier < this.dynamicModifier) {
            this.dynamicModifierEaser.update(dynamicModifier, true);
        }
        this.dynamicModifier = dynamicModifier;
    }

    private static double clampInterpolationFactor(double interpolationFactor) {
        return Math.max(0.0, Math.min(1.0, interpolationFactor));
    }

    private static double integrateTowards(double currentValue, double targetValue, double interpolationFactor, double maxMagnitude) {
        double nextValue = currentValue + (targetValue - currentValue) * interpolationFactor;
        return Mth.clamp((double)nextValue, (double)(-maxMagnitude), (double)maxMagnitude);
    }
}

