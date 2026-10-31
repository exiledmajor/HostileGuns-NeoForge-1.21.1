/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.ConditionalFeature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.JsonUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ActiveMuzzleFeature
extends ConditionalFeature {
    private Map<String, Predicate<ConditionContext>> muzzleParts;

    private ActiveMuzzleFeature(FeatureProvider owner, Predicate<ConditionContext> predicate, Map<String, Predicate<ConditionContext>> muzzleParts) {
        super(owner, predicate);
        this.muzzleParts = muzzleParts;
    }

    public MutableComponent getDescription() {
        return Component.empty();
    }

    public static boolean isActiveMuzzle(ItemStack rootStack, ItemStack currentStack, ItemDisplayContext itemDisplayContext, String partName) {
        Item item = currentStack.getItem();
        if (!(item instanceof FeatureProvider)) {
            return rootStack == currentStack;
        }
        FeatureProvider featureProvider = (FeatureProvider)item;
        ActiveMuzzleFeature feature = featureProvider.getFeature(ActiveMuzzleFeature.class);
        if (feature == null) {
            return rootStack == currentStack;
        }
        Predicate predicate = feature.muzzleParts.get(partName);
        if (predicate == null) {
            predicate = feature.predicate;
        }
        return feature.predicate.test(new ConditionContext(null, rootStack, currentStack, null, itemDisplayContext));
    }

    public static class Builder
    implements FeatureBuilder<Builder, ActiveMuzzleFeature> {
        private Predicate<ConditionContext> condition = ctx -> true;
        private Map<String, Predicate<ConditionContext>> muzzleParts = new HashMap<String, Predicate<ConditionContext>>();

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withPart(String partName, Predicate<ConditionContext> condition) {
            this.muzzleParts.put(partName, condition);
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            for (JsonObject partObj : JsonUtil.getJsonObjects(obj, "parts")) {
                Predicate<ConditionContext> condition;
                String partName = JsonUtil.getJsonString(partObj, "name");
                if (partObj.has("condition")) {
                    JsonObject conditionObj = partObj.getAsJsonObject("condition");
                    condition = Conditions.fromJson((JsonElement)conditionObj);
                } else {
                    condition = ctx -> true;
                }
                this.withPart(partName, condition);
            }
            return this;
        }

        @Override
        public ActiveMuzzleFeature build(FeatureProvider featureProvider) {
            HashMap<String, Predicate<ConditionContext>> muzzleParts = new HashMap<String, Predicate<ConditionContext>>(this.muzzleParts);
            if (muzzleParts.isEmpty()) {
                muzzleParts.put("muzzleflash", this.condition);
                muzzleParts.put("muzzle", this.condition);
            }
            return new ActiveMuzzleFeature(featureProvider, this.condition, Collections.unmodifiableMap(muzzleParts));
        }
    }
}

