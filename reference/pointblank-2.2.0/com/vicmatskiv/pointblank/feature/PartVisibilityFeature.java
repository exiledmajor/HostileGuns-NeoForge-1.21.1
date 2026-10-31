/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.world.level.ItemLike
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.JsonUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.world.level.ItemLike;

public class PartVisibilityFeature
implements Feature {
    private FeatureProvider owner;
    private Map<String, Predicate<ConditionContext>> predicates = new HashMap<String, Predicate<ConditionContext>>();

    private PartVisibilityFeature(FeatureProvider owner, Map<String, Predicate<ConditionContext>> partPredicates) {
        this.owner = owner;
        this.predicates = Collections.unmodifiableMap(partPredicates);
    }

    @Override
    public FeatureProvider getOwner() {
        return this.owner;
    }

    public boolean isPartVisible(ItemLike partOwner, String partName, ConditionContext conditionContext) {
        if (partOwner != this.owner) {
            return true;
        }
        Predicate<ConditionContext> bonePredicate = this.predicates.get(partName);
        if (bonePredicate == null) {
            return true;
        }
        boolean result = bonePredicate.test(conditionContext);
        if (partName.contains("muzzle")) {
            // empty if block
        }
        return result;
    }

    public static class Builder
    implements FeatureBuilder<Builder, PartVisibilityFeature> {
        private Map<String, Predicate<ConditionContext>> partPredicates = new HashMap<String, Predicate<ConditionContext>>();

        public Builder withShownPart(String partName, Predicate<ConditionContext> condition) {
            if (this.partPredicates.put(partName, condition) != null) {
                throw new IllegalArgumentException("Duplicate part: " + partName);
            }
            return this;
        }

        public Builder withHiddenPart(String partName, Predicate<ConditionContext> condition) {
            if (this.partPredicates.put(partName, condition.negate()) != null) {
                throw new IllegalArgumentException("Duplicate part: " + partName);
            }
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            for (JsonObject partObj : JsonUtil.getJsonObjects(obj, "parts")) {
                String partName = JsonUtil.getJsonString(partObj, "name");
                boolean isVisible = JsonUtil.getJsonBoolean(partObj, "visible", true);
                JsonElement conditionObj = partObj.get("condition");
                Predicate<ConditionContext> condition = Conditions.fromJson(conditionObj);
                this.withShownPart(partName, isVisible ? condition : condition.negate());
            }
            return this;
        }

        @Override
        public PartVisibilityFeature build(FeatureProvider featureProvider) {
            return new PartVisibilityFeature(featureProvider, this.partPredicates);
        }
    }
}

