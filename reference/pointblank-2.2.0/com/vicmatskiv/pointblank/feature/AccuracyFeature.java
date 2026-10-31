/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.ConditionalFeature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.feature.Features;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.JsonUtil;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class AccuracyFeature
extends ConditionalFeature {
    private static final float MIN_ACCURACY_MODIFIER = 0.1f;
    private static final float MAX_ACCURACY_MODIFIER = 10.0f;
    private float accuracyModifier;

    private AccuracyFeature(FeatureProvider owner, Predicate<ConditionContext> predicate, float accuracyModifier) {
        super(owner, predicate);
        this.accuracyModifier = accuracyModifier;
    }

    public MutableComponent getDescription() {
        if (this.accuracyModifier < 1.0f) {
            return Component.translatable((String)"description.pointblank.reducesAccuracy").append((Component)Component.literal((String)String.format(" %.0f%%", Float.valueOf(100.0f * (1.0f - this.accuracyModifier)))));
        }
        return Component.translatable((String)"description.pointblank.increasesAccuracy").append((Component)Component.literal((String)String.format(" %.0f%%", Float.valueOf(100.0f * (this.accuracyModifier - 1.0f)))));
    }

    public float getAccuracyModifier() {
        return this.accuracyModifier;
    }

    public static float getAccuracyModifier(ItemStack itemStack) {
        List<Features.EnabledFeature> enabledAccuracyFeatures = Features.getEnabledFeatures(itemStack, AccuracyFeature.class);
        float accuracyModifier = 1.0f;
        for (Features.EnabledFeature enabledFeature : enabledAccuracyFeatures) {
            AccuracyFeature accuracyFeature = (AccuracyFeature)enabledFeature.feature();
            accuracyModifier *= accuracyFeature.getAccuracyModifier();
        }
        return Mth.clamp((float)accuracyModifier, (float)0.1f, (float)10.0f);
    }

    public static class Builder
    implements FeatureBuilder<Builder, AccuracyFeature> {
        private Predicate<ConditionContext> condition = ctx -> true;
        private float accuracyModifier;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withAccuracyModifier(double accuracyModifier) {
            this.accuracyModifier = (float)accuracyModifier;
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            this.withAccuracyModifier(JsonUtil.getJsonFloat(obj, "accuracyModifier"));
            return this;
        }

        @Override
        public AccuracyFeature build(FeatureProvider featureProvider) {
            return new AccuracyFeature(featureProvider, this.condition, this.accuracyModifier);
        }
    }
}

