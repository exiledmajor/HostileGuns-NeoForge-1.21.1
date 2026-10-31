/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.util;

import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class VelocityProviders {
    private static Random random = new Random();

    public static Supplier<Vec3> randomVelocityProvider(double magnitude) {
        return () -> new Vec3((random.nextDouble() - 0.5) * 2.0 * magnitude, (random.nextDouble() - 0.5) * 2.0 * magnitude, (random.nextDouble() - 0.5) * 2.0 * magnitude);
    }

    public static Supplier<Vec3> sphereVelocityProvider(float radius, Distribution radialDistribution) {
        return () -> {
            double adjustedRadius = radialDistribution.transform(radius);
            float theta = (float)Math.PI * 2 * random.nextFloat();
            float phi = (float)Math.acos(2.0f * random.nextFloat() - 1.0f);
            float sinPhi = Mth.sin((float)phi);
            double x = adjustedRadius * (double)sinPhi * (double)Mth.cos((float)theta);
            double z = adjustedRadius * (double)sinPhi * (double)Mth.sin((float)theta);
            double y = adjustedRadius * Math.cos(phi);
            return new Vec3(x, y, z);
        };
    }

    public static Supplier<Vec3> hemisphereVelocityProvider(double radius, Distribution radialDistribution) {
        return () -> {
            double adjustedRadius = radialDistribution.transform(radius);
            float theta = (float)Math.PI * 2 * random.nextFloat();
            float phi = (float)Math.acos(random.nextFloat());
            float sinPhi = Mth.sin((float)phi);
            double x = adjustedRadius * (double)sinPhi * (double)Mth.cos((float)theta);
            double z = adjustedRadius * (double)sinPhi * (double)Mth.sin((float)theta);
            double y = adjustedRadius * Math.cos(phi);
            return new Vec3(x, y, z);
        };
    }

    public static enum Distribution {
        CONSTANT,
        UNIFORM,
        NORMAL(0.0f, 1.0f, 0.0f, Float.POSITIVE_INFINITY),
        TIGHT(0.5f, 0.25f, 0.25f, 2.0f);

        private float mean;
        private float standardDeviation;
        private float lowerBound;
        private float upperBound;

        private Distribution(float mean, float standardDeviation, float lowerBound, float upperBound) {
            this.mean = mean;
            this.standardDeviation = standardDeviation;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        private Distribution() {
        }

        private double transform(double value) {
            return switch (this.ordinal()) {
                case 0 -> value;
                case 1 -> random.nextDouble() * value;
                default -> Mth.clamp((double)((random.nextGaussian() * (double)this.mean + (double)this.standardDeviation) * value), (double)this.lowerBound, (double)this.upperBound);
            };
        }
    }
}

