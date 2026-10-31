/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 */
package com.vicmatskiv.pointblank.util;

import com.vicmatskiv.pointblank.util.TimeUnit;
import java.util.Random;
import net.minecraft.util.Mth;

public class Interpolators {

    public static class EaseOutFloatProvider
    implements FloatInterpolator {
        private float value;

        public EaseOutFloatProvider(float value) {
            this.value = value;
        }

        @Override
        public float getValue(float progress) {
            return this.value * Mth.cos((float)(progress * (float)Math.PI * 0.5f));
        }
    }

    public static class EaseInFloatProvider
    implements FloatInterpolator {
        private float value;

        public EaseInFloatProvider(float value) {
            this.value = value;
        }

        @Override
        public float getValue(float progress) {
            return this.value * Mth.sin((float)(progress * (float)Math.PI * 0.5f));
        }
    }

    public static class AnotherEaseInEaseOutFloatProvider
    implements FloatInterpolator {
        private float value;
        private float fadeIn;
        private float fadeOut;

        public AnotherEaseInEaseOutFloatProvider(float value, float fadeIn, float fadeOut) {
            this.value = value;
            this.fadeIn = fadeIn;
            this.fadeOut = fadeOut;
        }

        @Override
        public float getValue(float progress) {
            float factor;
            if (progress < this.fadeIn) {
                float ap = progress / this.fadeIn;
                factor = Mth.sin((float)(1.5707964f * ap));
            } else if (progress > this.fadeOut) {
                float ap = (progress - this.fadeOut) / (1.0f - this.fadeOut);
                factor = Mth.cos((float)(1.5707964f * ap));
            } else {
                factor = 1.0f;
            }
            factor = Mth.clamp((float)factor, (float)0.0f, (float)1.0f);
            return factor * this.value;
        }
    }

    public static class EaseInEaseOutFloatProvider2
    implements FloatInterpolator {
        private float value;

        public EaseInEaseOutFloatProvider2(float value) {
            this.value = value;
        }

        @Override
        public float getValue(float progress) {
            return this.value * Mth.sin((float)(progress * (float)Math.PI));
        }
    }

    public static class EaseInEaseOutFloatProvider
    implements FloatInterpolator {
        private float value;

        public EaseInEaseOutFloatProvider(float value) {
            this.value = value;
        }

        @Override
        public float getValue(float progress) {
            return this.value * Mth.sin((float)(Mth.sqrt((float)progress) * (float)Math.PI));
        }
    }

    public static class LinearInterpolatorFloatProvider
    implements FloatInterpolator {
        private float startValue;
        private float endValue;

        public LinearInterpolatorFloatProvider(float startValue, float endValue) {
            this.startValue = startValue;
            this.endValue = endValue;
        }

        @Override
        public float getValue(float progress) {
            return Mth.lerp((float)progress, (float)this.startValue, (float)this.endValue);
        }
    }

    public static class ConstantFloatProvider
    implements FloatInterpolator {
        private float value;

        public ConstantFloatProvider(float value) {
            this.value = value;
        }

        @Override
        public float getValue(float progress) {
            return this.value;
        }
    }

    @FunctionalInterface
    public static interface FloatInterpolator {
        public float getValue(float var1);
    }

    public static class RandomFloatProvider
    implements FloatProvider {
        private static Random random = new Random();
        private float value;

        public RandomFloatProvider(float maxValue) {
            this.value = maxValue;
        }

        @Override
        public float getValue() {
            return this.value * random.nextFloat();
        }
    }

    public static class RealTimeProgressProvider
    implements FloatProvider {
        private long startTime;
        private long lifetimeNanos;

        public RealTimeProgressProvider(long lifetimeMillis) {
            this(lifetimeMillis, 0L);
        }

        public RealTimeProgressProvider(long lifetimeMillis, long delayMillis) {
            this.startTime = System.nanoTime() + TimeUnit.MILLISECOND.toNanos(delayMillis);
            this.lifetimeNanos = TimeUnit.MILLISECOND.toNanos(lifetimeMillis);
        }

        @Override
        public float getValue() {
            if (this.startTime > System.nanoTime()) {
                return Float.NEGATIVE_INFINITY;
            }
            float progress = Mth.clamp((float)((float)(System.nanoTime() - this.startTime) / (float)this.lifetimeNanos), (float)0.0f, (float)1.0f);
            return progress;
        }
    }

    public static interface FloatProvider {
        public float getValue();
    }
}

