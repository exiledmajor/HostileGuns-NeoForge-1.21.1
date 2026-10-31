/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.ConditionalFeature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.feature.Features;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.function.Predicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class DualWieldFeature
extends ConditionalFeature {
    private DualWieldFeature(FeatureProvider owner, Predicate<ConditionContext> predicate) {
        super(owner, predicate);
    }

    public static boolean isEnabledForStack(ItemStack itemStack) {
        Item item;
        if (itemStack == null || itemStack.isEmpty() || !((item = itemStack.getItem()) instanceof FeatureProvider)) {
            return false;
        }
        FeatureProvider featureProvider = (FeatureProvider)item;
        if (MiscUtil.getTag(itemStack) != null) {
            return Features.getFirstEnabledFeature(itemStack, DualWieldFeature.class) != null;
        }
        DualWieldFeature feature = featureProvider.getFeature(DualWieldFeature.class);
        return feature != null && feature.isEnabled(itemStack);
    }

    public static final class Builder
    implements FeatureBuilder<Builder, DualWieldFeature> {
        private Predicate<ConditionContext> condition = context -> true;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson(obj.get("condition")));
            }
            return this;
        }

        @Override
        public DualWieldFeature build(FeatureProvider featureProvider) {
            return new DualWieldFeature(featureProvider, this.condition);
        }
    }
}

