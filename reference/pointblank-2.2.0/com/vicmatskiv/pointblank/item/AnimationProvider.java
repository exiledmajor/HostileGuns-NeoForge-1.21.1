/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.item;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.util.TimeUnit;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface AnimationProvider {
    public Descriptor getDescriptor(LivingEntity var1, ItemStack var2, GunClientState var3);

    public static class Simple
    implements AnimationProvider {
        public Descriptor descriptor;

        public Simple(String animationName) {
            this.descriptor = new Descriptor(ctx -> true, 0L, TimeUnit.MILLISECOND, animationName);
        }

        @Override
        public Descriptor getDescriptor(LivingEntity player, ItemStack itemStack, GunClientState gunClientState) {
            return this.descriptor;
        }
    }

    public record Descriptor(Predicate<ConditionContext> predicate, long duration, TimeUnit timeUnit, String animationName) {
    }
}

