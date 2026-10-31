/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 */
package com.vicmatskiv.pointblank.client;

import net.minecraft.util.Mth;

public final class RealtimeLinearEaser {
    private long startTime;
    private long durationNano;
    private float initialValue;
    private float currentValue = Float.NaN;
    private float targetValue = Float.NaN;

    public RealtimeLinearEaser(long duration) {
        this.durationNano = duration * 1000000L;
    }

    private void reset() {
        this.startTime = System.nanoTime();
        this.initialValue = this.currentValue;
    }

    public float update(float targetValue) {
        return this.update(targetValue, false);
    }

    public float update(float targetValue, boolean immediate) {
        if (targetValue != this.targetValue) {
            this.targetValue = targetValue;
            if (immediate || Float.isNaN(this.currentValue)) {
                this.currentValue = targetValue;
            }
            this.reset();
        }
        this.currentValue = Mth.lerp((float)this.getProgress(), (float)this.initialValue, (float)targetValue);
        return this.currentValue;
    }

    private float getProgress() {
        return Mth.clamp((float)((float)(System.nanoTime() - this.startTime) / (float)this.durationNano), (float)0.0f, (float)1.0f);
    }
}

