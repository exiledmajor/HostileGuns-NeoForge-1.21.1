/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.util.math;

import java.util.Random;

public class SmoothRandomizer {
    private final Random random = new Random();
    private final double minValue;
    private final double maxValue;
    private final long cycleDuration;
    private double controlPoint1;
    private double controlPoint2;
    private double startValue;
    private double endValue;
    private long cycleStartTime;

    public SmoothRandomizer(double minValue, double maxValue, long cycleDuration) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.cycleDuration = cycleDuration;
        this.cycleStartTime = System.currentTimeMillis();
        this.endValue = this.randomInRange();
        this.regeneratePoints();
    }

    private void regeneratePoints() {
        this.startValue = this.endValue;
        this.endValue = this.randomInRange();
        this.controlPoint1 = this.randomInRange();
        this.controlPoint2 = this.randomInRange();
    }

    private double randomInRange() {
        return this.minValue + this.random.nextDouble() * (this.maxValue - this.minValue);
    }

    private double cubicBezier(double t, double p0, double p1, double p2, double p3) {
        double oneMinusT = 1.0 - t;
        return oneMinusT * oneMinusT * oneMinusT * p0 + 3.0 * oneMinusT * oneMinusT * t * p1 + 3.0 * oneMinusT * t * t * p2 + t * t * t * p3;
    }

    public double sample() {
        long elapsed = System.currentTimeMillis() - this.cycleStartTime;
        if (elapsed >= this.cycleDuration) {
            long cycles = elapsed / this.cycleDuration;
            this.cycleStartTime += cycles * this.cycleDuration;
            int i = 0;
            while ((long)i < cycles) {
                this.regeneratePoints();
                ++i;
            }
            elapsed %= this.cycleDuration;
        }
        double t = (double)elapsed / (double)this.cycleDuration;
        return this.cubicBezier(t, this.startValue, this.controlPoint1, this.controlPoint2, this.endValue);
    }

    public void reset() {
        this.cycleStartTime = System.currentTimeMillis();
        this.endValue = this.randomInRange();
        this.regeneratePoints();
    }
}

