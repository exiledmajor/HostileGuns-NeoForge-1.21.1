/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.ConditionalFeature;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.feature.Features;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.JsonUtil;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class OverheatFeature
extends ConditionalFeature {
    public static final long DEFAULT_COOLDOWN_DELAY_MILLIS = 500L;
    private final double maxHeat;
    private final double heatPerShot;
    private final double cooldownPerSecond;
    private final long cooldownDelayMillis;
    private final String heatUi;

    private OverheatFeature(FeatureProvider owner, Predicate<ConditionContext> predicate, double maxHeat, double heatPerShot, double cooldownPerSecond, long cooldownDelayMillis, String heatUi) {
        super(owner, predicate);
        this.maxHeat = maxHeat;
        this.heatPerShot = heatPerShot;
        this.cooldownPerSecond = cooldownPerSecond;
        this.cooldownDelayMillis = cooldownDelayMillis;
        this.heatUi = heatUi;
    }

    public double getMaxHeat() {
        return this.maxHeat;
    }

    public double getHeatPerShot() {
        return this.heatPerShot;
    }

    public double getCooldownPerSecond() {
        return this.cooldownPerSecond;
    }

    public long getCooldownDelayMillis() {
        return this.cooldownDelayMillis;
    }

    public String getHeatUi() {
        return this.heatUi;
    }

    public boolean isConfigured() {
        return this.maxHeat > 0.0 && this.heatPerShot > 0.0;
    }

    @Nullable
    public static Features.EnabledFeature getEnabled(ItemStack itemStack) {
        for (Features.EnabledFeature enabledFeature : Features.getEnabledFeatures(itemStack, OverheatFeature.class)) {
            OverheatFeature overheatFeature;
            Feature feature = enabledFeature.feature();
            if (!(feature instanceof OverheatFeature) || !(overheatFeature = (OverheatFeature)feature).isConfigured()) continue;
            return enabledFeature;
        }
        return null;
    }

    @Nullable
    public static OverheatFeature get(ItemStack itemStack) {
        Features.EnabledFeature enabledFeature = OverheatFeature.getEnabled(itemStack);
        return enabledFeature != null ? (OverheatFeature)enabledFeature.feature() : null;
    }

    public static class Builder
    implements FeatureBuilder<Builder, OverheatFeature> {
        private Predicate<ConditionContext> condition = ctx -> true;
        private double maxHeat;
        private double heatPerShot;
        private double cooldownPerSecond;
        private long cooldownDelayMillis = 500L;
        private String heatUi = "default";

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withMaxHeat(double maxHeat) {
            this.maxHeat = Math.max(0.0, maxHeat);
            return this;
        }

        public Builder withHeatPerShot(double heatPerShot) {
            this.heatPerShot = Math.max(0.0, heatPerShot);
            return this;
        }

        public Builder withCooldownPerSecond(double cooldownPerSecond) {
            this.cooldownPerSecond = Math.max(0.0, cooldownPerSecond);
            return this;
        }

        public Builder withCooldownDelayMillis(long cooldownDelayMillis) {
            this.cooldownDelayMillis = Math.max(0L, cooldownDelayMillis);
            return this;
        }

        public Builder withHeatUi(String heatUi) {
            this.heatUi = heatUi;
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson(obj.get("condition")));
            }
            this.withMaxHeat(JsonUtil.getJsonDouble(obj, "maxHeat", 0.0));
            this.withHeatPerShot(JsonUtil.getJsonDouble(obj, "heatPerShot", 0.0));
            this.withCooldownPerSecond(JsonUtil.getJsonDouble(obj, "cooldownPerSecond", 0.0));
            this.withCooldownDelayMillis(JsonUtil.getJsonInt(obj, "cooldownDelay", 500));
            this.withHeatUi(JsonUtil.getJsonString(obj, "heatUi", "default"));
            return this;
        }

        @Override
        public OverheatFeature build(FeatureProvider featureProvider) {
            return new OverheatFeature(featureProvider, this.condition, this.maxHeat, this.heatPerShot, this.cooldownPerSecond, this.cooldownDelayMillis, this.heatUi);
        }
    }
}

