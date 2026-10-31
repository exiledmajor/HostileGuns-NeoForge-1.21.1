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

public class DamageFeature
extends ConditionalFeature {
    private static final float MIN_DAMAGE_MODIFIER = 0.01f;
    private static final float MAX_DAMAGE_MODIFIER = 10.0f;
    private float hitScanDamageModifier;

    private DamageFeature(FeatureProvider owner, Predicate<ConditionContext> predicate, float hitScanDamageModifier) {
        super(owner, predicate);
        this.hitScanDamageModifier = hitScanDamageModifier;
    }

    public MutableComponent getDescription() {
        if (this.hitScanDamageModifier < 1.0f) {
            return Component.translatable((String)"description.pointblank.reducesDamage").append((Component)Component.literal((String)String.format(" %.0f%%", Float.valueOf(100.0f * (1.0f - this.hitScanDamageModifier)))));
        }
        return Component.translatable((String)"description.pointblank.increasesDamage").append((Component)Component.literal((String)String.format(" %.0f%%", Float.valueOf(100.0f * (this.hitScanDamageModifier - 1.0f)))));
    }

    public float getHitScanDamageModifier() {
        return this.hitScanDamageModifier;
    }

    public static float getHitScanDamageModifier(ItemStack itemStack) {
        List<Features.EnabledFeature> enabledDamageFeatures = Features.getEnabledFeatures(itemStack, DamageFeature.class);
        float hitScanDamageModifier = 1.0f;
        for (Features.EnabledFeature enabledFeature : enabledDamageFeatures) {
            DamageFeature damageFeature = (DamageFeature)enabledFeature.feature();
            hitScanDamageModifier *= damageFeature.getHitScanDamageModifier();
        }
        return Mth.clamp((float)hitScanDamageModifier, (float)0.01f, (float)10.0f);
    }

    public static class Builder
    implements FeatureBuilder<Builder, DamageFeature> {
        private Predicate<ConditionContext> condition = ctx -> true;
        private float hitScanDamageModifier;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withHitScanDamageModifier(double damageModifier) {
            this.hitScanDamageModifier = (float)damageModifier;
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            this.withHitScanDamageModifier(JsonUtil.getJsonFloat(obj, "hitScanDamageModifier"));
            return this;
        }

        @Override
        public DamageFeature build(FeatureProvider featureProvider) {
            return new DamageFeature(featureProvider, this.condition, this.hitScanDamageModifier);
        }
    }
}

