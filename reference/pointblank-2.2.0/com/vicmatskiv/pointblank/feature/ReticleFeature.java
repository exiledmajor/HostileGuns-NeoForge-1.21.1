/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
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
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class ReticleFeature
extends ConditionalFeature {
    public static final float DEFAULT_MAX_ANGULAR_OFFSET_DEGREES = 5.0f;
    public static final float DEFAULT_MAX_ANGULAR_OFFSET_COS = Mth.cos((float)0.08726646f);
    private final ResourceLocation texture;
    private final boolean isParallaxEnabled;
    private final float maxAngularOffsetCos;

    private ReticleFeature(FeatureProvider owner, Predicate<ConditionContext> condition, ResourceLocation texture, boolean isParallaxEnabled, float maxAngularOffsetCos) {
        super(owner, condition);
        this.texture = texture;
        this.isParallaxEnabled = isParallaxEnabled;
        this.maxAngularOffsetCos = maxAngularOffsetCos;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    public boolean isParallaxEnabled() {
        return this.isParallaxEnabled;
    }

    public float getMaxAngularOffsetCos() {
        return this.maxAngularOffsetCos;
    }

    public static class Builder
    implements FeatureBuilder<Builder, ReticleFeature> {
        private Predicate<ConditionContext> condition = ctx -> true;
        private boolean isParallaxEnabled;
        private float maxAngularOffsetCos = DEFAULT_MAX_ANGULAR_OFFSET_COS;
        private ResourceLocation texture;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withTexture(String texture) {
            this.texture = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)texture);
            return this;
        }

        public Builder withParallaxEnabled(boolean isParallaxEnabled) {
            this.isParallaxEnabled = isParallaxEnabled;
            return this;
        }

        public Builder withMaxAngularOffset(float maxAngularOffsetDegrees) {
            this.maxAngularOffsetCos = Mth.cos((float)((float)Math.PI / 180 * Mth.clamp((float)maxAngularOffsetDegrees, (float)0.0f, (float)45.0f)));
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            this.isParallaxEnabled = JsonUtil.getJsonBoolean(obj, "parallax", false);
            if (obj.has("texture")) {
                this.withTexture(JsonUtil.getJsonString(obj, "texture"));
            } else if (this.isParallaxEnabled) {
                this.withTexture("textures/item/reticle4.png");
            }
            this.withMaxAngularOffset(JsonUtil.getJsonFloat(obj, "maxAngularOffset", 5.0f));
            return this;
        }

        @Override
        public ReticleFeature build(FeatureProvider featureProvider) {
            ResourceLocation texture = this.texture;
            if (texture == null) {
                texture = this.isParallaxEnabled ? ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/item/reticle4.png") : ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/item/reticle.png");
            }
            return new ReticleFeature(featureProvider, this.condition, texture, this.isParallaxEnabled, this.maxAngularOffsetCos);
        }
    }
}

