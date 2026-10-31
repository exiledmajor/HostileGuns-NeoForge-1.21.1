/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.client.controller;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.GunStateListener;
import com.vicmatskiv.pointblank.client.controller.AbstractProceduralAnimationController;
import com.vicmatskiv.pointblank.feature.RecoilFeature;
import com.vicmatskiv.pointblank.util.math.SmoothRandomizer;
import com.vicmatskiv.pointblank.util.physics.DampedSpring;
import java.util.Random;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GunRecoilAnimationController
extends AbstractProceduralAnimationController
implements GunStateListener {
    public static final double DEFAULT_SUSTAINED_PITCH_INCREMENT = 0.0025;
    public static final double DEFAULT_SUSTAINED_POSY_INCREMENT = -0.0125;
    public static final double DEFAULT_SUSTAINED_POSZ_INCREMENT = 0.01;
    public static final double DEFAULT_SUSTAINED_YAW_MULTIPLIER = 0.05;
    public static final double DEFAULT_SUSTAINED_PITCH_CAP = 0.01;
    public static final double DEFAULT_SUSTAINED_POSY_CAP = -0.05;
    public static final double DEFAULT_SUSTAINED_POSZ_CAP = 0.08;
    public static final long DEFAULT_SUSTAINED_DECAY_TIME = 400L;
    private double initialAmplitude;
    private double initialAmplitudeAdj;
    private double rateOfAmplitudeDecay;
    private double rateOfAmplitudeDecayAdj;
    private double initialAngularFrequency;
    private double initialAngularFrequencyAdj;
    private double rateOfFrequencyIncrease;
    private double rateOfFrequencyIncreaseAdj;
    private double pitchMultiplier;
    private int shotCount;
    private int shotsPerRecoil;
    private Random random = new Random();
    private final SmoothRandomizer lateralDrift;
    private final SmoothRandomizer rotationalWobble;
    private final SmoothRandomizer verticalShift;
    private double cachedDrift;
    private double cachedWobble;
    private double cachedVShift;
    private final DampedSpring aimingSpring;
    private double sustainedPitch;
    private double sustainedYaw;
    private double sustainedPosY;
    private double sustainedPosZ;
    private long lastFireTime;
    private long aimingStartTime;
    private final long sustainedDecayTime;
    private final double sustainedPitchIncrement;
    private final double sustainedPosYIncrement;
    private final double sustainedPosZIncrement;
    private final double sustainedYawMultiplier;
    private final double sustainedPitchCap;
    private final double sustainedPosYCap;
    private final double sustainedPosZCap;
    private final boolean sustainedRecoilOnAim;
    private int consecutiveShots;

    public GunRecoilAnimationController(double initialAmplitude, double rateOfAmplitudeDecay, double initialAngularFrequency, double rateOfFrequencyIncrease, double pitchMultiplier, long ticksPerTransition, int shotsPerRecoil) {
        this(initialAmplitude, rateOfAmplitudeDecay, initialAngularFrequency, rateOfFrequencyIncrease, pitchMultiplier, ticksPerTransition, shotsPerRecoil, 0.0, 0.0, 0.0, 0.0, 0.01, -0.05, 0.08, false, 400L);
    }

    public GunRecoilAnimationController(double initialAmplitude, double rateOfAmplitudeDecay, double initialAngularFrequency, double rateOfFrequencyIncrease, double pitchMultiplier, long ticksPerTransition, int shotsPerRecoil, double sustainedPitchIncrement, double sustainedPosYIncrement, double sustainedPosZIncrement, double sustainedYawMultiplier, double sustainedPitchCap, double sustainedPosYCap, double sustainedPosZCap, boolean sustainedRecoilOnAim, long sustainedDecayTime) {
        super(ticksPerTransition);
        this.initialAmplitude = initialAmplitude;
        this.rateOfAmplitudeDecay = rateOfAmplitudeDecay;
        this.initialAngularFrequency = initialAngularFrequency;
        this.rateOfFrequencyIncrease = rateOfFrequencyIncrease;
        this.shotsPerRecoil = shotsPerRecoil;
        this.pitchMultiplier = pitchMultiplier;
        this.sustainedPitchIncrement = sustainedPitchIncrement;
        this.sustainedPosYIncrement = sustainedPosYIncrement;
        this.sustainedPosZIncrement = sustainedPosZIncrement;
        this.sustainedYawMultiplier = sustainedYawMultiplier;
        this.sustainedPitchCap = sustainedPitchCap;
        this.sustainedPosYCap = sustainedPosYCap;
        this.sustainedPosZCap = sustainedPosZCap;
        this.sustainedRecoilOnAim = sustainedRecoilOnAim;
        this.sustainedDecayTime = sustainedDecayTime;
        this.lateralDrift = new SmoothRandomizer(-0.25, 0.25, 350L);
        this.rotationalWobble = new SmoothRandomizer(-0.018, 0.018, 180L);
        this.verticalShift = new SmoothRandomizer(-0.05, 0.05, 420L);
        this.aimingSpring = new DampedSpring(15.0, 8.0, 1.0, 1.0);
        this.sustainedPitch = 0.0;
        this.sustainedYaw = 0.0;
        this.sustainedPosY = 0.0;
        this.sustainedPosZ = 0.0;
        this.lastFireTime = 0L;
        this.consecutiveShots = 0;
        this.reset();
        this.isDone = true;
    }

    static double oscillatingFunction(double t, double a0, double alpha, double omega0, double beta) {
        double aT = a0 * Math.exp(-alpha * t);
        double omegaT = omega0 + beta * t;
        return aT * Math.sin(omegaT * t);
    }

    static double derivativeOscillatingFunction(double t, double a0, double alpha, double omega0, double beta) {
        double aT = a0 * Math.exp(-alpha * t);
        double dAT = -alpha * aT;
        double omega_t = omega0 + beta * t;
        double domega_t = beta;
        double sinTerm = Math.sin(omega_t * t);
        double cosTerm = Math.cos(omega_t * t);
        return dAT * sinTerm + aT * (domega_t * t + omega_t) * cosTerm;
    }

    @Override
    public void onRenderTick(LivingEntity player, GunClientState gunClientState, ItemStack itemStack, ItemDisplayContext itemDisplayContext, float partialTicks) {
        super.onRenderTick(player, gunClientState, itemStack, itemDisplayContext, partialTicks);
        long currentTime = System.currentTimeMillis();
        long timeSinceLastFire = currentTime - this.lastFireTime;
        if (timeSinceLastFire > 100L) {
            double decayProgress = Math.min(1.0, (double)(timeSinceLastFire - 100L) / (double)this.sustainedDecayTime);
            double decayFactor = 1.0 - decayProgress;
            this.sustainedPitch *= decayFactor;
            this.sustainedPosY *= decayFactor;
            this.sustainedPosZ *= decayFactor;
            this.sustainedYaw *= decayFactor;
        }
        if (gunClientState.isAiming() && !this.sustainedRecoilOnAim && this.aimingStartTime > 0L) {
            long timeSinceAiming = currentTime - this.aimingStartTime;
            double aimDecayProgress = Math.min(1.0, (double)timeSinceAiming / (double)this.sustainedDecayTime);
            double aimDecayFactor = 1.0 - aimDecayProgress * aimDecayProgress;
            this.sustainedPitch *= aimDecayFactor;
            this.sustainedPosY *= aimDecayFactor;
            this.sustainedPosZ *= aimDecayFactor;
            this.sustainedYaw *= aimDecayFactor;
        }
        if (!this.isDone) {
            double aimReduction = this.aimingSpring.update(gunClientState.isAiming() ? 0.4 : 1.0);
            double coreRecoil = GunRecoilAnimationController.oscillatingFunction(this.progress * 10.0, this.initialAmplitudeAdj, this.rateOfAmplitudeDecayAdj, this.initialAngularFrequencyAdj, this.rateOfFrequencyIncreaseAdj);
            double envelope = Math.sin(this.progress * Math.PI);
            double rotationEnvelope = Math.sin(this.progress * Math.PI);
            rotationEnvelope *= rotationEnvelope;
            double drift = this.cachedDrift * envelope * aimReduction;
            double vShift = this.cachedVShift * envelope * 0.3;
            double wobble = this.cachedWobble * rotationEnvelope * aimReduction;
            this.posX = drift * 0.03;
            if (this.sustainedPosYIncrement == 0.0) {
                this.posY = vShift * 0.015;
            } else {
                double downwardOffset = -0.025 * envelope * aimReduction;
                this.posY = downwardOffset + this.sustainedPosY;
            }
            this.posZ = coreRecoil + this.sustainedPosZ;
            this.pitch = coreRecoil * 0.03 * this.pitchMultiplier + wobble * 0.15 + this.sustainedPitch;
            this.yaw = wobble * 0.5 + this.sustainedYaw;
            this.roll = coreRecoil * 0.015 + drift * 0.05;
        } else {
            this.posX = 0.0;
            this.posY = this.sustainedPosYIncrement != 0.0 ? this.sustainedPosY : 0.0;
            this.posZ = this.sustainedPosZIncrement != 0.0 ? this.sustainedPosZ : 0.0;
            this.pitch = this.sustainedPitchIncrement != 0.0 ? this.sustainedPitch : 0.0;
            this.yaw = this.sustainedYawMultiplier != 0.0 ? this.sustainedYaw : 0.0;
            this.roll = 0.0;
        }
    }

    @Override
    public void reset() {
        if (this.shotCount % this.shotsPerRecoil == 0) {
            double randomness = 0.1;
            this.initialAmplitudeAdj = this.initialAmplitude + (this.random.nextDouble() - 0.5) * this.initialAmplitude * randomness;
            this.rateOfAmplitudeDecayAdj = this.rateOfAmplitudeDecay + (this.random.nextDouble() - 0.5) * this.rateOfAmplitudeDecay * randomness;
            this.initialAngularFrequencyAdj = this.initialAngularFrequency + (this.random.nextDouble() - 0.5) * this.initialAngularFrequency * randomness;
            this.rateOfFrequencyIncreaseAdj = this.rateOfFrequencyIncrease + (this.random.nextDouble() - 0.5) * this.rateOfFrequencyIncrease * 0.01;
            super.reset();
        }
    }

    @Override
    public void onStartAutoFiring(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
        super.onStartAutoFiring(player, gunClientState, itemStack);
        this.shotCount = 0;
        this.consecutiveShots = 0;
    }

    @Override
    public void onStartFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
        this.reset();
        ++this.shotCount;
        ++this.consecutiveShots;
        this.cachedDrift = this.lateralDrift.sample();
        this.cachedWobble = this.rotationalWobble.sample();
        this.cachedVShift = this.verticalShift.sample();
        double rapidFireMultiplier = 1.0 + Math.min((double)this.consecutiveShots * 0.08, 0.5);
        if (state.isAiming()) {
            this.initialAmplitudeAdj *= 0.5;
        }
        this.initialAmplitudeAdj *= (double)RecoilFeature.getRecoilModifier(itemStack) * rapidFireMultiplier;
        if (!state.isAiming() || this.sustainedRecoilOnAim) {
            double aimingMultiplier = state.isAiming() ? 0.5 : 1.0;
            this.sustainedPitch += this.sustainedPitchIncrement * rapidFireMultiplier * aimingMultiplier;
            this.sustainedPosY += this.sustainedPosYIncrement * rapidFireMultiplier * aimingMultiplier;
            this.sustainedPosZ += this.sustainedPosZIncrement * rapidFireMultiplier * aimingMultiplier;
            this.sustainedYaw += this.cachedWobble * this.sustainedYawMultiplier * rapidFireMultiplier;
            if (this.sustainedPitchIncrement > 0.0) {
                this.sustainedPitch = Math.min(this.sustainedPitch, this.sustainedPitchCap);
            } else if (this.sustainedPitchIncrement < 0.0) {
                this.sustainedPitch = Math.max(this.sustainedPitch, this.sustainedPitchCap);
            }
            if (this.sustainedPosYIncrement < 0.0) {
                this.sustainedPosY = Math.max(this.sustainedPosY, this.sustainedPosYCap);
            } else if (this.sustainedPosYIncrement > 0.0) {
                this.sustainedPosY = Math.min(this.sustainedPosY, this.sustainedPosYCap);
            }
            if (this.sustainedPosZIncrement > 0.0) {
                this.sustainedPosZ = Math.min(this.sustainedPosZ, this.sustainedPosZCap);
            } else if (this.sustainedPosZIncrement < 0.0) {
                this.sustainedPosZ = Math.max(this.sustainedPosZ, this.sustainedPosZCap);
            }
            this.sustainedYaw = Math.max(-0.08, Math.min(this.sustainedYaw, 0.08));
        }
        this.lastFireTime = System.currentTimeMillis();
    }

    @Override
    public void onToggleAiming(boolean isAiming) {
        this.aimingStartTime = isAiming && !this.sustainedRecoilOnAim ? System.currentTimeMillis() : 0L;
    }
}

