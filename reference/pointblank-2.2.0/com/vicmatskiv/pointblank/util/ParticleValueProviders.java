/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult
 */
package com.vicmatskiv.pointblank.util;

import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.SimpleHitResult;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public class ParticleValueProviders {

    public static class BoundingBoxBasedParticleWidthProvider
    implements ParticleWidthProvider {
        private double maxBoundBoxSize;
        private double maxWidth;

        public BoundingBoxBasedParticleWidthProvider(double maxBoundBoxSize, double maxWidth) {
            this.maxBoundBoxSize = maxBoundBoxSize;
            this.maxWidth = maxWidth;
        }

        @Override
        public float getWidth(EffectBuilder.Context effectContext) {
            float result = (float)this.maxWidth;
            HitResult hitResult = effectContext.getHitResult();
            if (hitResult instanceof SimpleHitResult) {
                AABB bb;
                SimpleHitResult simpleHitResult = (SimpleHitResult)hitResult;
                int entityId = simpleHitResult.getEntityId();
                Entity entity = ClientUtil.getClientLevel().getEntity(entityId);
                if (entity != null && (bb = entity.getBoundingBox()) != null) {
                    result = (float)Mth.clamp((double)(this.maxWidth * bb.getSize() / this.maxBoundBoxSize), (double)0.0, (double)this.maxWidth);
                }
            }
            return result;
        }
    }

    public static class DamageBasedParticleCountProvider
    implements ParticleCountProvider {
        private int maxCount;
        private float maxDamage;

        public DamageBasedParticleCountProvider(int maxCount, float maxDamage) {
            this.maxCount = maxCount;
            this.maxDamage = maxDamage;
        }

        @Override
        public int getCount(EffectBuilder.Context effectContext) {
            float damage = effectContext.getDamage();
            return Math.round(Mth.clamp((float)((float)this.maxCount * damage / this.maxDamage), (float)0.0f, (float)this.maxCount));
        }
    }

    public static class RandomParticleCountProvider
    implements ParticleCountProvider {
        private Random random = new Random();
        private int origin;
        private int bound;

        public RandomParticleCountProvider(int origin, int bound) {
            this.origin = origin;
            this.bound = bound;
        }

        @Override
        public int getCount(EffectBuilder.Context effectContext) {
            return this.random.nextInt(this.origin, this.bound);
        }
    }

    public static class ConstantParticleCountProvider
    implements ParticleCountProvider {
        private int count;

        public ConstantParticleCountProvider(int count) {
            this.count = count;
        }

        @Override
        public int getCount(EffectBuilder.Context effectContext) {
            return this.count;
        }
    }

    public static interface ParticleWidthProvider {
        public float getWidth(EffectBuilder.Context var1);
    }

    public static interface ParticleCountProvider {
        public int getCount(EffectBuilder.Context var1);
    }
}

