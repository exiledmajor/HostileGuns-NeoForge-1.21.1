/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.HitResult
 */
package com.vicmatskiv.pointblank.entity;

import com.vicmatskiv.pointblank.client.effect.Effect;
import com.vicmatskiv.pointblank.util.Trajectory;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public interface ProjectileLike {
    public ItemStack getItem();

    public List<Effect> getActiveAttachedEffects();

    public float getProgress(float var1);

    public long getElapsedTimeMillis();

    public void launchAtTargetEntity(LivingEntity var1, HitResult var2, Entity var3);

    public void launchAtLookTarget(LivingEntity var1, double var2, long var4);

    public double getInitialVelocityBlocksPerTick();

    default public void setTrailEffects(List<EffectInfo> trailEffects) {
    }

    default public void setAttachedEffects(List<EffectInfo> attachedEffects) {
    }

    default public Trajectory<?> getTrajectory() {
        return null;
    }

    public record EffectInfo(Effect effect, Predicate<ProjectileLike> predicate) {
    }
}

