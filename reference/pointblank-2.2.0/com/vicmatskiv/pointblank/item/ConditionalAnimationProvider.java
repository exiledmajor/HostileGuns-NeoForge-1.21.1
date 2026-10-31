/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.item;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.item.AnimationProvider;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.TimeUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ConditionalAnimationProvider
implements AnimationProvider {
    private List<AnimationProvider.Descriptor> conditionalAnimations;
    private int randomUpperBound;
    private static final Random random = new Random();

    private ConditionalAnimationProvider(List<AnimationProvider.Descriptor> conditionalAnimations) {
        this.conditionalAnimations = Collections.unmodifiableList(conditionalAnimations);
        this.randomUpperBound = (int)conditionalAnimations.stream().filter(p -> p.predicate() == Conditions.RANDOM_PICK).count();
    }

    @Override
    public AnimationProvider.Descriptor getDescriptor(LivingEntity player, ItemStack itemStack, GunClientState gunClientState) {
        AnimationProvider.Descriptor result = null;
        int randomValue = this.randomUpperBound > 0 ? random.nextInt(this.randomUpperBound) : 0;
        int i = 0;
        for (AnimationProvider.Descriptor descriptor : this.conditionalAnimations) {
            ConditionContext ctx = new ConditionContext(player, itemStack, itemStack, gunClientState, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, randomValue, i);
            if (descriptor.predicate() == Conditions.RANDOM_PICK) {
                ++i;
            }
            if (!descriptor.predicate().test(ctx)) continue;
            result = descriptor;
            break;
        }
        return result;
    }

    public static class Builder {
        private List<AnimationProvider.Descriptor> conditionalAnimations = new ArrayList<AnimationProvider.Descriptor>();

        public Builder withAnimation(String animation, Predicate<ConditionContext> condition, long duration, TimeUnit timeUnit) {
            this.conditionalAnimations.add(new AnimationProvider.Descriptor(condition, duration, timeUnit, animation));
            return this;
        }

        public Builder withAnimation(String animation, Predicate<ConditionContext> condition) {
            return this.withAnimation(animation, condition, 0L, TimeUnit.MILLISECOND);
        }

        public List<AnimationProvider.Descriptor> getAnimations() {
            return this.conditionalAnimations;
        }

        public AnimationProvider build() {
            return new ConditionalAnimationProvider(this.conditionalAnimations);
        }
    }
}

