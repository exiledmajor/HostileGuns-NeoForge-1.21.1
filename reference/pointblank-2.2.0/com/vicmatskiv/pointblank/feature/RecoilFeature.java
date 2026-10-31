/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.network.chat.Component
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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class RecoilFeature
extends ConditionalFeature {
    private static final float MIN_RECOIL_MODIFIER = 0.01f;
    private static final float MAX_RECOIL_MODIFIER = 10.0f;
    private float recoilModifier;
    private Component description;

    private RecoilFeature(FeatureProvider owner, Predicate<ConditionContext> predicate, float recoilModifier) {
        super(owner, predicate);
        this.recoilModifier = recoilModifier;
        this.description = recoilModifier < 1.0f ? Component.translatable((String)"description.pointblank.reducesRecoil").append((Component)Component.literal((String)String.format(" %.0f%%", Float.valueOf(100.0f * (1.0f - recoilModifier))))) : Component.translatable((String)"description.pointblank.increasesRecoil").append((Component)Component.literal((String)String.format(" %.0f%%", Float.valueOf(100.0f * (recoilModifier - 1.0f)))));
    }

    @Override
    public Component getDescription() {
        return this.description;
    }

    public float getRecoilModifier() {
        return this.recoilModifier;
    }

    public static float getRecoilModifier(ItemStack itemStack) {
        List<Features.EnabledFeature> enabledRecoilFeatures = Features.getEnabledFeatures(itemStack, RecoilFeature.class);
        float recoilModifier = 1.0f;
        for (Features.EnabledFeature enabledFeature : enabledRecoilFeatures) {
            RecoilFeature recoilFeature = (RecoilFeature)enabledFeature.feature();
            recoilModifier *= recoilFeature.getRecoilModifier();
        }
        return Mth.clamp((float)recoilModifier, (float)0.01f, (float)10.0f);
    }

    public static class Builder
    implements FeatureBuilder<Builder, RecoilFeature> {
        private Predicate<ConditionContext> condition = ctx -> true;
        private float recoilModifier;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withRecoilModifier(double recoilModifier) {
            this.recoilModifier = (float)recoilModifier;
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            this.withRecoilModifier(JsonUtil.getJsonFloat(obj, "recoilModifier"));
            return this;
        }

        @Override
        public RecoilFeature build(FeatureProvider featureProvider) {
            return new RecoilFeature(featureProvider, this.condition, this.recoilModifier);
        }
    }
}

