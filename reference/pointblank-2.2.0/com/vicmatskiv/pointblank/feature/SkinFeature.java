/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
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
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SkinFeature
extends ConditionalFeature {
    private ResourceLocation texture;

    private SkinFeature(FeatureProvider owner, Predicate<ConditionContext> predicate, ResourceLocation texture) {
        super(owner, predicate);
        this.texture = texture;
    }

    public MutableComponent getDescription() {
        return Component.translatable((String)"Changes skin");
    }

    public static ResourceLocation getTexture(ItemStack itemStack) {
        Features.EnabledFeature enabledSkinTexture = Features.getFirstEnabledFeature(itemStack, SkinFeature.class);
        if (enabledSkinTexture != null) {
            return ((SkinFeature)enabledSkinTexture.feature()).texture;
        }
        return null;
    }

    public static class Builder
    implements FeatureBuilder<Builder, SkinFeature> {
        private Predicate<ConditionContext> condition = ctx -> true;
        private ResourceLocation skinResource;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withTexture(String texture) {
            this.skinResource = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)texture);
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            this.withTexture(JsonUtil.getJsonString(obj, "texture"));
            return this;
        }

        @Override
        public SkinFeature build(FeatureProvider featureProvider) {
            return new SkinFeature(featureProvider, this.condition, this.skinResource);
        }
    }
}

