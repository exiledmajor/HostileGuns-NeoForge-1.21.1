/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 */
package com.vicmatskiv.pointblank.client;

import com.vicmatskiv.pointblank.util.TimeUnit;
import net.minecraft.util.Mth;

public class SegmentsProviders {
    public static final SegmentsProvider ZERO_PROVIDER = new ZeroProvider();

    private static class ZeroProvider
    implements SegmentsProvider {
        private static float[][] SEGMENT = new float[][]{{0.0f, 0.0f}};

        private ZeroProvider() {
        }

        @Override
        public float[][] getSegments(float length, float progress, long lifetimeNanos) {
            return SEGMENT;
        }

        @Override
        public boolean shouldDetach() {
            return true;
        }
    }

    public static interface SegmentsProvider {
        public float[][] getSegments(float var1, float var2, long var3);

        public boolean shouldDetach();
    }

    public static class MovingPointProvider
    implements SegmentsProvider {
        public static final int DEFAULT_SPEED_BLOCKS_PER_SECOND = 100;
        public static final int DEFAULT_ACCELERATION = -10;
        private float speedBlocksPerSecond;
        private float acceleration;

        public MovingPointProvider(float speedBlocksPerSecond, float accelerationBlocksPerSecond) {
            this.speedBlocksPerSecond = speedBlocksPerSecond;
            this.acceleration = accelerationBlocksPerSecond;
        }

        @Override
        public float[][] getSegments(float length, float progress, long lifetimeNanos) {
            float start = 0.0f;
            float initialSpeed = this.speedBlocksPerSecond;
            float lifetimeSeconds = (float)TimeUnit.NANOSECOND.toSeconds(lifetimeNanos);
            float time = lifetimeSeconds * progress;
            float speed = initialSpeed + this.acceleration * time;
            start = Mth.clamp((float)(speed * time), (float)0.0f, (float)length);
            return new float[][]{{start, start}};
        }

        @Override
        public boolean shouldDetach() {
            return true;
        }
    }

    public static class StaticBeamSegmentsProvider
    implements SegmentsProvider {
        @Override
        public float[][] getSegments(float length, float progress, long lifetimeNanos) {
            float start = 0.0f;
            float end = length;
            float offset = Mth.clamp((float)(length * 0.1f), (float)0.01f, (float)1.0f);
            float[][] beamSegments = new float[][]{{start, start + offset}, {start + offset, end - offset}, {end - offset, end}};
            return beamSegments;
        }

        @Override
        public boolean shouldDetach() {
            return false;
        }
    }

    public static class MovingSegmentsProvider
    implements SegmentsProvider {
        public static final int DEFAULT_SPEED_BLOCKS_PER_SECOND = 100;
        public static final int DEFAULT_ACCELERATION = -10;
        public static final float DEFAULT_MAX_SEGMENT_LENGTH = 10.0f;
        public static final float DEFAULT_MIN_SEGMENT_LENGTH = 0.1f;
        private float speedBlocksPerSecond;
        private float acceleration;
        private float minSegmentLength = 0.1f;
        private float maxSegmentLength = 10.0f;

        public MovingSegmentsProvider(float speedBlocksPerSecond, float accelerationBlocksPerSecond) {
            this(speedBlocksPerSecond, accelerationBlocksPerSecond, 0.1f, 10.0f);
        }

        public MovingSegmentsProvider(float speedBlocksPerSecond, float accelerationBlocksPerSecond, float minSegmentLength, float maxSegmentLength) {
            this.speedBlocksPerSecond = speedBlocksPerSecond;
            this.acceleration = accelerationBlocksPerSecond;
            this.minSegmentLength = minSegmentLength;
            this.maxSegmentLength = maxSegmentLength;
        }

        @Override
        public float[][] getSegments(float length, float progress, long lifetimeNanos) {
            float start = 0.0f;
            float end = length;
            float initialSpeed = this.speedBlocksPerSecond;
            float lifetimeSeconds = (float)TimeUnit.NANOSECOND.toSeconds(lifetimeNanos);
            float time = lifetimeSeconds * progress;
            float speed = Mth.clamp((float)(initialSpeed + this.acceleration * time), (float)0.0f, (float)Float.MAX_VALUE);
            start = Mth.clamp((float)(speed * time), (float)0.0f, (float)length);
            float segmentLength = Mth.clamp((float)(length * 0.5f), (float)this.minSegmentLength, (float)this.maxSegmentLength);
            end = Mth.clamp((float)(start + segmentLength), (float)1.0f, (float)length);
            float[][] beamSegments = new float[][]{{start, end}};
            return beamSegments;
        }

        @Override
        public boolean shouldDetach() {
            return true;
        }
    }

    public static class SingleSegmentProvider
    implements SegmentsProvider {
        @Override
        public float[][] getSegments(float length, float progress, long lifetimeNanos) {
            return new float[][]{{0.0f, length}};
        }

        @Override
        public boolean shouldDetach() {
            return true;
        }
    }
}

