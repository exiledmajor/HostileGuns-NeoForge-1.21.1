/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.client;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.GunStateListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BiDirectionalInterpolator
implements GunStateListener {
    private long startTime;
    private long fullDuration;
    private long duration;
    private boolean isDone;
    private Position targetPosition;
    private double value;
    private double progress;

    public BiDirectionalInterpolator(long durationMillis) {
        this.fullDuration = durationMillis * 1000000L;
        this.isDone = true;
        this.set(Position.START, true);
    }

    public double getValue() {
        return this.value;
    }

    public boolean isDone() {
        return this.isDone;
    }

    private double calculateProgress() {
        long elapsedTime = System.nanoTime() - this.startTime;
        double t = (double)elapsedTime / (double)this.duration;
        t = t < 0.5 ? 4.0 * t * t * t : (t - 1.0) * (2.0 * t - 2.0) * (2.0 * t - 2.0) + 1.0;
        this.progress = Mth.clamp((double)t, (double)0.0, (double)1.0);
        return this.progress;
    }

    public void set(Position position) {
        this.set(position, true);
    }

    public void set(Position targetPosition, boolean immediate) {
        if (immediate) {
            this.completeTransition(targetPosition);
        } else if (targetPosition != this.targetPosition) {
            this.startTransition(targetPosition);
        }
    }

    private void startTransition(Position targetPosition) {
        this.targetPosition = targetPosition;
        if (this.isDone) {
            this.isDone = false;
            this.duration = this.fullDuration;
            this.value = targetPosition == Position.START ? 0.0 : 1.0;
            this.startTime = System.nanoTime();
        } else {
            this.startTime = System.nanoTime() - (long)((double)this.fullDuration * (1.0 - this.progress));
        }
    }

    private void completeTransition(Position targetPosition) {
        this.isDone = true;
        this.value = targetPosition == Position.START ? 0.0 : 1.0;
    }

    @Override
    public void onRenderTick(LivingEntity player, GunClientState state, ItemStack itemStack, ItemDisplayContext itemDisplayContext, float partialTicks) {
        this.update();
    }

    public void update() {
        if (this.isDone) {
            return;
        }
        this.calculateProgress();
        this.value = this.targetPosition == Position.END ? this.progress : 1.0 - this.progress;
        if (this.progress >= 1.0) {
            this.completeTransition(this.targetPosition);
        }
    }

    public static enum Position {
        START,
        END;

    }
}

