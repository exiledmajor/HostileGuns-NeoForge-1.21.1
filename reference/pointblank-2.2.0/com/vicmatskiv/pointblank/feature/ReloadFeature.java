/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
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
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.JsonUtil;
import java.util.function.Predicate;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class ReloadFeature
extends ConditionalFeature {
    private static final int MIN_AMMO_PER_RELOAD_ITERATION = 1;
    private static final int MAX_AMMO_PER_RELOAD_ITERATION = Integer.MAX_VALUE;
    private int maxAmmoPerReloadIteration;

    private ReloadFeature(FeatureProvider owner, Predicate<ConditionContext> predicate, int maxAmmoPerReloadIteration) {
        super(owner, predicate);
        this.maxAmmoPerReloadIteration = maxAmmoPerReloadIteration;
    }

    public static int getMaxAmmoPerReloadIteration(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return 0;
        }
        Features.EnabledFeature enabledReloadFeature = Features.getFirstEnabledFeature(itemStack, ReloadFeature.class);
        if (enabledReloadFeature != null) {
            return ((ReloadFeature)enabledReloadFeature.feature()).maxAmmoPerReloadIteration;
        }
        return Integer.MAX_VALUE;
    }

    public static class Builder
    implements FeatureBuilder<Builder, ReloadFeature> {
        private Predicate<ConditionContext> condition = ctx -> true;
        private int maxAmmoPerReloadIteration;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withMaxAmmoPerReloadIteration(int maxAmmoPerReloadIteration) {
            this.maxAmmoPerReloadIteration = Mth.clamp((int)maxAmmoPerReloadIteration, (int)1, (int)Integer.MAX_VALUE);
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            if (obj.has("maxAmmoPerReloadIteration")) {
                this.withMaxAmmoPerReloadIteration(JsonUtil.getJsonInt(obj, "maxAmmoPerReloadIteration"));
            }
            return this;
        }

        @Override
        public ReloadFeature build(FeatureProvider featureProvider) {
            return new ReloadFeature(featureProvider, this.condition, this.maxAmmoPerReloadIteration);
        }
    }
}

