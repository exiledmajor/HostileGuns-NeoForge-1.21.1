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
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class AmmoCapacityFeature
extends ConditionalFeature {
    private static final int MIN_AMMO = 1;
    private static final int MAX_AMMO = Integer.MAX_VALUE;
    private IntUnaryOperator ammoCapacityTransformer;
    private Component description;

    private AmmoCapacityFeature(FeatureProvider owner, Predicate<ConditionContext> predicate, IntUnaryOperator ammoCapacityTransformer, Component description) {
        super(owner, predicate);
        this.description = description;
        this.ammoCapacityTransformer = ammoCapacityTransformer;
    }

    @Override
    public Component getDescription() {
        return this.description;
    }

    public static int modifyAmmoCapacity(ItemStack itemStack, int ammoCapacity) {
        Features.EnabledFeature enabledExtendedAmmoFeature = Features.getFirstEnabledFeature(itemStack, AmmoCapacityFeature.class);
        if (enabledExtendedAmmoFeature != null) {
            return ((AmmoCapacityFeature)enabledExtendedAmmoFeature.feature()).ammoCapacityTransformer.applyAsInt(ammoCapacity);
        }
        return ammoCapacity;
    }

    public static class Builder
    implements FeatureBuilder<Builder, AmmoCapacityFeature> {
        private Predicate<ConditionContext> condition = ctx -> true;
        private IntUnaryOperator ammoCapacityTransformer;
        private Component description;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withAmmoCapacityModifier(int ammoCapacityModifier) {
            this.ammoCapacityTransformer = ammo -> Mth.clamp((int)(ammo * ammoCapacityModifier), (int)1, (int)Integer.MAX_VALUE);
            this.description = Component.translatable((String)"description.pointblank.extendsAmmoCapacity").append((Component)Component.literal((String)String.format(" %d%%", (ammoCapacityModifier - 1) * 100)));
            return this;
        }

        public Builder withAmmoCapacity(int ammoCapacity) {
            this.ammoCapacityTransformer = ammo -> Mth.clamp((int)ammoCapacity, (int)1, (int)Integer.MAX_VALUE);
            this.description = Component.translatable((String)"description.pointblank.changesAmmoCapacity").append((Component)Component.literal((String)("" + ammoCapacity)));
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            if (obj.has("ammoCapacityModifier")) {
                this.withAmmoCapacityModifier(JsonUtil.getJsonInt(obj, "ammoCapacityModifier"));
            } else if (obj.has("ammoCapacity")) {
                this.withAmmoCapacity(JsonUtil.getJsonInt(obj, "ammoCapacity"));
            }
            return this;
        }

        @Override
        public AmmoCapacityFeature build(FeatureProvider featureProvider) {
            if (this.ammoCapacityTransformer == null) {
                throw new IllegalStateException("Either ammoCapacity ammoCapacityModifier must be set");
            }
            return new AmmoCapacityFeature(featureProvider, this.condition, this.ammoCapacityTransformer, this.description);
        }
    }
}

