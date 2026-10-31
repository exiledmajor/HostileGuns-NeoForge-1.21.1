/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.client.controller;

import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.GunStateListener;
import com.vicmatskiv.pointblank.feature.RecoilFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PlayerRecoilController
implements GunStateListener {
    private static final double BASE_MIDDLE_PROGRESS = 1.0 - Math.sqrt(2.0) / 2.0;
    private static final double MIN_PROGRESS_STEP = 1.0E-6;
    private static final double RETURN_DURATION_MULTIPLIER = 7.0;
    private static final double RETURN_EASE_POWER = 2.35;
    private static final double MIN_RETURN_DURATION_SCALE = 0.9;
    private static final long MIN_RETURN_DURATION_NANO = 300000000L;
    private static final double YAW_BIAS_MULTIPLIER = 0.18;
    private int elapsedTime;
    private double ticksPerTransition;
    private double progress;
    private double prevProgress;
    private double actualAmplitude;
    private double amplitude;
    private double dPitch;
    private double dYaw;
    private double dPitchResetRatio = 1.0;
    private double startPitch;
    private double maxPitch;
    private double initialPitch;
    private double accumulatedPitchOffset;
    private double accumulatedYawOffset;
    private double trackedPitch;
    private double trackedYaw;
    private boolean hasTrackedPitch;
    private boolean hasTrackedYaw;
    private boolean hadAccumulatedRecoil;
    private long startTime;
    private long recoilDurationNano;
    private long recoilReturnDurationNano;
    private long nanoSecPerTransition;
    private long resetDurationNano;
    private double forwardProgressLimit;
    private State currentState = State.IDLE;

    public PlayerRecoilController(double amplitude, double maxPitch, double duration) {
        double totalDuration = duration * 7.0;
        this.nanoSecPerTransition = this.recoilDurationNano = (long)(totalDuration * 1000000.0);
        this.amplitude = amplitude;
        this.actualAmplitude = amplitude;
        this.ticksPerTransition = totalDuration;
        this.maxPitch = -Math.abs(maxPitch);
        this.recoilReturnDurationNano = Math.max((long)((double)this.recoilDurationNano * (1.0 - BASE_MIDDLE_PROGRESS)), 300000000L);
        this.resetDurationNano = 1400000000L;
        this.forwardProgressLimit = BASE_MIDDLE_PROGRESS / 7.0;
    }

    @Override
    public void onUpdateState(LivingEntity player, GunClientState state) {
        if ((double)this.elapsedTime < this.ticksPerTransition) {
            ++this.elapsedTime;
        }
    }

    @Override
    public void onRenderTick(LivingEntity player, GunClientState state, ItemStack itemStack, ItemDisplayContext itemDisplayContext, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        if (this.currentState == State.IDLE) {
            return;
        }
        float currentPitch = player.getXRot();
        float currentYaw = player.getYRot();
        if (this.hasTrackedPitch) {
            this.accumulatedPitchOffset = Math.min(0.0, this.accumulatedPitchOffset + Math.abs((double)currentPitch - this.trackedPitch));
        }
        if (this.hasTrackedYaw) {
            this.accumulatedYawOffset = Math.max(0.0, this.accumulatedYawOffset - Math.abs((double)currentYaw - this.trackedYaw));
        }
        this.trackedPitch = currentPitch;
        this.trackedYaw = currentYaw;
        this.hasTrackedPitch = true;
        this.hasTrackedYaw = true;
        this.prevProgress = this.progress;
        this.progress = this.getProgress(state, partialTicks);
        if (this.currentState == State.RECOILING && this.progress >= 1.0) {
            if (Config.resetAutoFirePitchEnabled && this.resetDurationNano > 0L && this.hadAccumulatedRecoil && (this.accumulatedPitchOffset < 0.0 || this.accumulatedYawOffset > 0.0)) {
                this.currentState = State.RESETTING;
                this.startTime = System.nanoTime();
                this.prevProgress = 0.0;
                this.progress = 0.0;
                this.nanoSecPerTransition = this.resetDurationNano;
            } else {
                this.currentState = State.IDLE;
                this.accumulatedPitchOffset = 0.0;
                this.accumulatedYawOffset = 0.0;
            }
        }
        if (this.currentState == State.RESETTING) {
            double easedProgress = PlayerRecoilController.easeReturnProgress(this.progress);
            double easedPrevProgress = PlayerRecoilController.easeReturnProgress(this.prevProgress);
            double dProgress = easedProgress - easedPrevProgress;
            double remainingProgress = Math.max(1.0 - easedPrevProgress, 1.0E-6);
            float prevPitch = player.getXRot();
            float prevYaw = player.getYRot();
            this.dPitch = -this.accumulatedPitchOffset * dProgress / remainingProgress;
            this.dYaw = -this.accumulatedYawOffset * dProgress / remainingProgress;
            float newXRot = (float)((double)prevPitch + this.dPitch);
            float newYRot = (float)((double)prevYaw + this.dYaw);
            player.setXRot(newXRot);
            player.setYRot(newYRot);
            this.accumulatedPitchOffset += (double)(newXRot - prevPitch);
            this.accumulatedYawOffset += (double)(newYRot - prevYaw);
            this.accumulatedPitchOffset = Math.min(this.accumulatedPitchOffset, 0.0);
            this.accumulatedYawOffset = Math.max(this.accumulatedYawOffset, 0.0);
            this.trackedPitch = newXRot;
            this.trackedYaw = newYRot;
            if (this.progress >= 1.0 || this.accumulatedPitchOffset == 0.0 && this.accumulatedYawOffset == 0.0) {
                this.currentState = State.IDLE;
                this.accumulatedPitchOffset = 0.0;
                this.accumulatedYawOffset = 0.0;
            }
            return;
        }
        double currentRecoilOffset = this.getRecoilOffset(this.progress);
        double previousRecoilOffset = this.getRecoilOffset(this.prevProgress);
        double currentYawRecoilOffset = this.getYawRecoilOffset(this.progress);
        double previousYawRecoilOffset = this.getYawRecoilOffset(this.prevProgress);
        this.dPitch = currentRecoilOffset - previousRecoilOffset;
        this.dYaw = currentYawRecoilOffset - previousYawRecoilOffset;
        float prevPitch = player.getXRot();
        float prevYaw = player.getYRot();
        float newXRot = (float)((double)prevPitch + this.dPitch);
        float newYRot = (float)((double)prevYaw + this.dYaw);
        if ((double)newXRot < this.startPitch || this.dPitch <= 0.0) {
            player.setXRot(newXRot);
            this.accumulatedPitchOffset += (double)(newXRot - prevPitch);
            this.accumulatedPitchOffset = Math.min(this.accumulatedPitchOffset, 0.0);
            this.trackedPitch = newXRot;
        }
        player.setYRot(newYRot);
        this.accumulatedYawOffset += (double)(newYRot - prevYaw);
        this.accumulatedYawOffset = Math.max(this.accumulatedYawOffset, 0.0);
        this.trackedYaw = newYRot;
    }

    @Override
    public void onStartFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
        this.currentState = State.RECOILING;
        this.elapsedTime = -1;
        this.startTime = System.nanoTime();
        this.nanoSecPerTransition = this.recoilDurationNano;
        this.startPitch = player.getXRot();
        this.prevProgress = 0.0;
        this.progress = 0.0;
        this.actualAmplitude = state.isAiming() ? this.amplitude * 0.5 : this.amplitude;
        this.actualAmplitude *= (double)RecoilFeature.getRecoilModifier(itemStack);
        if (state.getTotalUninterruptedShots() <= 1 || this.startPitch > this.initialPitch) {
            this.initialPitch = this.startPitch;
            this.accumulatedPitchOffset = 0.0;
            this.accumulatedYawOffset = 0.0;
            this.trackedPitch = this.startPitch;
            this.trackedYaw = player.getYRot();
            this.hasTrackedPitch = true;
            this.hasTrackedYaw = true;
            this.hadAccumulatedRecoil = false;
        }
        if (state.getTotalUninterruptedShots() <= 1 || Math.abs(this.startPitch - this.initialPitch) >= Math.abs(this.maxPitch)) {
            this.dPitchResetRatio = 1.0;
        } else {
            this.dPitchResetRatio = 0.1;
            this.hadAccumulatedRecoil = true;
        }
    }

    @Override
    public void onStopAutoFiring(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
        if (Config.resetAutoFirePitchEnabled) {
            this.startImmediateReturn();
        }
    }

    @Override
    public void onCompleteFiring(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
        if (Config.resetAutoFirePitchEnabled) {
            this.startImmediateReturn();
        }
    }

    protected double getProgress(GunClientState gunClientState, float partialTicks) {
        double progress = (double)(System.nanoTime() - this.startTime) / (double)this.nanoSecPerTransition;
        if (progress > 1.0) {
            progress = 1.0;
        }
        return progress;
    }

    private double getReturnProgress(double progress) {
        return Math.max(0.0, Math.min(1.0, (progress - this.forwardProgressLimit) / (1.0 - this.forwardProgressLimit)));
    }

    private double getRecoilOffset(double progress) {
        if (progress <= this.forwardProgressLimit) {
            return -this.actualAmplitude * progress / this.forwardProgressLimit;
        }
        if (!Config.resetAutoFirePitchEnabled) {
            return -this.actualAmplitude;
        }
        return -this.actualAmplitude + this.dPitchResetRatio * this.actualAmplitude * PlayerRecoilController.easeReturnProgress(this.getReturnProgress(progress));
    }

    private double getYawRecoilOffset(double progress) {
        double yawAmplitude = this.actualAmplitude * 0.18;
        if (progress <= this.forwardProgressLimit) {
            return yawAmplitude * progress / this.forwardProgressLimit;
        }
        if (!Config.resetAutoFirePitchEnabled) {
            return yawAmplitude;
        }
        return yawAmplitude - this.dPitchResetRatio * yawAmplitude * PlayerRecoilController.easeReturnProgress(this.getReturnProgress(progress));
    }

    private static double easeReturnProgress(double progress) {
        return 1.0 - Math.pow(1.0 - progress, 2.35);
    }

    private void startImmediateReturn() {
        if (this.currentState != State.RECOILING || this.progress >= 1.0 || this.accumulatedPitchOffset >= 0.0 && this.accumulatedYawOffset <= 0.0) {
            return;
        }
        this.currentState = State.RESETTING;
        this.startTime = System.nanoTime();
        this.progress = 0.0;
        this.prevProgress = 0.0;
        double normalizedPitchOffset = Math.min(1.0, Math.abs(this.accumulatedPitchOffset) / Math.max(this.actualAmplitude, 1.0E-6));
        double normalizedYawOffset = Math.min(1.0, this.accumulatedYawOffset / Math.max(this.actualAmplitude * 0.18, 1.0E-6));
        double normalizedOffset = Math.max(normalizedPitchOffset, normalizedYawOffset);
        double returnDurationScale = 0.9 + 0.09999999999999998 * Math.sqrt(normalizedOffset);
        this.nanoSecPerTransition = Math.max((long)((double)this.recoilReturnDurationNano * returnDurationScale), 300000000L);
    }

    private static enum State {
        IDLE,
        RECOILING,
        RESETTING;

    }
}

