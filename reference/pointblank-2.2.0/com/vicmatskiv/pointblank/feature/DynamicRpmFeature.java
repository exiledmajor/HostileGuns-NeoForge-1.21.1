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

public final class DynamicRpmFeature
extends ConditionalFeature {
    private static final int DEFAULT_START_RPM = 0;
    private static final int DEFAULT_SHOTS_TO_MAX = 1;
    private final int startRpm;
    private final int shotsToMax;

    private DynamicRpmFeature(FeatureProvider owner, Predicate<ConditionContext> predicate, int startRpm, int shotsToMax) {
        super(owner, predicate);
        this.startRpm = startRpm;
        this.shotsToMax = shotsToMax;
    }

    public int getStartRpm() {
        return this.startRpm;
    }

    public int getShotsToMax() {
        return this.shotsToMax;
    }

    public int getRpm(int capRpm, int uninterruptedShots) {
        return DynamicRpmFeature.calculateRpm(this.startRpm, capRpm, this.shotsToMax, uninterruptedShots);
    }

    public static int calculateRpm(int startRpm, int capRpm, int shotsToMax, int uninterruptedShots) {
        int totalRampSteps;
        if (capRpm <= 0 || startRpm <= 0 || startRpm >= capRpm || shotsToMax <= 1) {
            return capRpm;
        }
        int completedRampSteps = Math.max(0, uninterruptedShots - 1);
        if (completedRampSteps >= (totalRampSteps = shotsToMax - 1)) {
            return capRpm;
        }
        double progress = (double)completedRampSteps / (double)totalRampSteps;
        int rpm = (int)Math.round((double)startRpm + (double)(capRpm - startRpm) * progress);
        return Math.max(startRpm, Math.min(capRpm, rpm));
    }

    @Nullable
    public static DynamicRpmFeature getEnabled(ItemStack itemStack) {
        for (Features.EnabledFeature enabledFeature : Features.getEnabledFeatures(itemStack, DynamicRpmFeature.class)) {
            Feature feature = enabledFeature.feature();
            if (!(feature instanceof DynamicRpmFeature)) continue;
            DynamicRpmFeature dynamicRpmFeature = (DynamicRpmFeature)feature;
            return dynamicRpmFeature;
        }
        return null;
    }

    public static final class Builder
    implements FeatureBuilder<Builder, DynamicRpmFeature> {
        private Predicate<ConditionContext> condition = context -> true;
        private int startRpm = 0;
        private int shotsToMax = 1;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withStartRpm(int startRpm) {
            this.startRpm = Math.max(0, startRpm);
            return this;
        }

        public Builder withShotsToMax(int shotsToMax) {
            this.shotsToMax = Math.max(1, shotsToMax);
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson(obj.get("condition")));
            }
            this.withStartRpm(JsonUtil.getJsonInt(obj, "startRpm", 0));
            this.withShotsToMax(JsonUtil.getJsonInt(obj, "shotsToMax", 1));
            return this;
        }

        @Override
        public DynamicRpmFeature build(FeatureProvider featureProvider) {
            return new DynamicRpmFeature(featureProvider, this.condition, this.startRpm, this.shotsToMax);
        }
    }
}

