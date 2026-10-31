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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractProceduralAnimationController
implements GunStateListener {
    protected double pitch;
    protected double yaw;
    protected double roll;
    protected double posX;
    protected double posY;
    protected double posZ;
    protected double startPosX;
    protected double startPosY;
    protected double startPosZ;
    protected double endPosX;
    protected double endPosY;
    protected double endPosZ;
    protected double startRoll;
    protected double startYaw;
    protected double startPitch;
    protected double endRoll;
    protected double endYaw;
    protected double endPitch;
    protected double progress;
    protected long startTime;
    protected long nanoDuration;
    protected boolean isDone;

    protected AbstractProceduralAnimationController(long duration) {
        this.nanoDuration = duration * 1000000L;
    }

    protected double getProgress(GunClientState gunClientState, float partialTicks) {
        double progress = (double)(System.nanoTime() - this.startTime) / (double)this.nanoDuration;
        if (progress > 1.0) {
            progress = 1.0;
        }
        return progress;
    }

    public boolean isDone() {
        return this.isDone;
    }

    public void reset() {
        this.isDone = false;
        this.startTime = System.nanoTime();
        this.progress = 0.0;
    }

    @Override
    public void onRenderTick(LivingEntity player, GunClientState state, ItemStack itemStack, ItemDisplayContext itemDisplayContext, float partialTicks) {
        if (this.isDone) {
            return;
        }
        this.progress = this.getProgress(state, partialTicks);
        if (this.progress >= 1.0) {
            this.isDone = true;
        }
    }

    public double getPosX() {
        return this.posX;
    }

    public double getPosY() {
        return this.posY;
    }

    public double getPosZ() {
        return this.posZ;
    }

    public double getPitch() {
        return this.pitch;
    }

    public double getYaw() {
        return this.yaw;
    }

    public double getRoll() {
        return this.roll;
    }
}

