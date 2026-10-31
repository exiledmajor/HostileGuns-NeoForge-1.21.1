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
import java.util.Random;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ViewShakeAnimationController2
extends AbstractProceduralAnimationController
implements GunStateListener {
    private double initialAmplitude;
    private double initialAmplitudeAdj;
    private double rateOfAmplitudeDecay;
    private double rateOfAmplitudeDecayAdj;
    private double initialAngularFrequency;
    private double initialAngularFrequencyAdj;
    private double rateOfFrequencyIncrease;
    private double rateOfFrequencyIncreaseAdj;
    private Random random = new Random();

    public ViewShakeAnimationController2(double initialAmplitude, double rateOfAmplitudeDecay, double initialAngularFrequency, double rateOfFrequencyIncrease, long duration) {
        super(duration);
        this.initialAmplitude = initialAmplitude;
        this.rateOfAmplitudeDecay = rateOfAmplitudeDecay;
        this.initialAngularFrequency = initialAngularFrequency;
        this.rateOfFrequencyIncrease = rateOfFrequencyIncrease;
        this.reset();
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
        if (!this.isDone) {
            double recoilPosZ = ViewShakeAnimationController2.oscillatingFunction(this.progress * 10.0, this.initialAmplitudeAdj, this.rateOfAmplitudeDecayAdj, this.initialAngularFrequencyAdj, this.rateOfFrequencyIncreaseAdj);
            this.roll = recoilPosZ * 10.0;
        }
    }

    @Override
    public void reset() {
        double randomness = 0.1;
        this.initialAmplitudeAdj = this.initialAmplitude + (this.random.nextDouble() - 0.5) * this.initialAmplitude * randomness;
        this.rateOfAmplitudeDecayAdj = this.rateOfAmplitudeDecay + (this.random.nextDouble() - 0.5) * this.rateOfAmplitudeDecay * randomness;
        this.initialAngularFrequencyAdj = this.initialAngularFrequency + (this.random.nextDouble() - 0.5) * this.initialAngularFrequency * randomness;
        this.rateOfFrequencyIncreaseAdj = this.rateOfFrequencyIncrease + (this.random.nextDouble() - 0.5) * this.rateOfFrequencyIncrease * 0.01;
        super.reset();
    }

    public void reset(double initialAmplitude, double rateOfAmplitudeDecay, double initialAngularFrequency, double rateOfFrequencyIncrease) {
        double randomness = 0.1;
        this.initialAmplitudeAdj = initialAmplitude + (this.random.nextDouble() - 0.5) * initialAmplitude * randomness;
        this.rateOfAmplitudeDecayAdj = rateOfAmplitudeDecay + (this.random.nextDouble() - 0.5) * rateOfAmplitudeDecay * randomness;
        this.initialAngularFrequencyAdj = initialAngularFrequency + (this.random.nextDouble() - 0.5) * initialAngularFrequency * randomness;
        this.rateOfFrequencyIncreaseAdj = rateOfFrequencyIncrease + (this.random.nextDouble() - 0.5) * rateOfFrequencyIncrease * 0.01;
        super.reset();
    }

    @Override
    public void onStartFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
        this.reset();
    }

    @Override
    public double getRoll() {
        return this.roll;
    }
}

