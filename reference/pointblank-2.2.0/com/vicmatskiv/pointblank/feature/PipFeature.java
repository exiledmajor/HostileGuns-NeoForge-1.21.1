/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.attachment.Attachments;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.ConditionalFeature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.JsonUtil;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class PipFeature
extends ConditionalFeature {
    private static final ResourceLocation DEFAULT_SQUARE_MASK = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/gui/pip_mask_square.png");
    private static final ResourceLocation DEFAULT_ROUND_MASK = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/gui/pip_mask_round.png");
    private final float zoom;
    private final float altZoom;
    private final ResourceLocation overlayTexture;
    private final ResourceLocation maskTexture;
    private final boolean hasCustomMaskTexture;
    private final boolean isParallaxEnabled;

    private PipFeature(FeatureProvider owner, Predicate<ConditionContext> condition, float zoom, float altZoom, boolean isParallaxEnabled, ResourceLocation overlayTexture, ResourceLocation maskTexture, boolean hasCustomMaskTexture) {
        super(owner, condition);
        this.zoom = zoom;
        this.altZoom = altZoom;
        this.isParallaxEnabled = isParallaxEnabled;
        this.overlayTexture = overlayTexture;
        this.maskTexture = maskTexture;
        this.hasCustomMaskTexture = hasCustomMaskTexture;
    }

    public MutableComponent getDescription() {
        return Component.translatable((String)"description.pointblank.enablesPipWithZoom").append((Component)Component.literal((String)String.format(" %.0f%%", Float.valueOf(this.zoom * 100.0f))));
    }

    public float getZoom() {
        return this.zoom;
    }

    public float getAltZoom() {
        return this.altZoom;
    }

    public ResourceLocation getMaskTexture() {
        if (!this.hasCustomMaskTexture) {
            return Config.scopeViewType == Config.ScopeViewType.ROUND ? DEFAULT_ROUND_MASK : DEFAULT_SQUARE_MASK;
        }
        return this.maskTexture;
    }

    public boolean isParallaxEnabled() {
        return this.isParallaxEnabled;
    }

    public ResourceLocation getOverlayTexture() {
        return this.overlayTexture;
    }

    public boolean isFallbackRequired() {
        return Config.isPipFallbackRequired();
    }

    public static Optional<Float> getZoom(ItemStack itemStack) {
        FeatureProvider fp;
        PipFeature feature;
        Pair<String, ItemStack> selected = Attachments.getSelectedAttachment(itemStack, AttachmentCategory.SCOPE);
        ItemStack selectedStack = null;
        selectedStack = selected != null ? (ItemStack)selected.getSecond() : itemStack;
        Item item = selectedStack.getItem();
        if (item instanceof FeatureProvider && (feature = (fp = (FeatureProvider)item).getFeature(PipFeature.class)) != null) {
            return Optional.of(Float.valueOf(feature.getZoom()));
        }
        return Optional.empty();
    }

    public static ResourceLocation getMaskTexture(ItemStack itemStack) {
        FeatureProvider fp;
        PipFeature feature;
        Pair<String, ItemStack> selected = Attachments.getSelectedAttachment(itemStack, AttachmentCategory.SCOPE);
        ItemStack selectedStack = null;
        selectedStack = selected != null ? (ItemStack)selected.getSecond() : itemStack;
        Item item = selectedStack.getItem();
        if (item instanceof FeatureProvider && (feature = (fp = (FeatureProvider)item).getFeature(PipFeature.class)) != null) {
            return feature.getMaskTexture();
        }
        return null;
    }

    public static PipFeature getSelected(ItemStack itemStack) {
        Pair<String, ItemStack> selected = Attachments.getSelectedAttachment(itemStack, AttachmentCategory.SCOPE);
        ItemStack selectedStack = null;
        selectedStack = selected != null ? (ItemStack)selected.getSecond() : itemStack;
        Item item = selectedStack.getItem();
        if (item instanceof FeatureProvider) {
            FeatureProvider fp = (FeatureProvider)item;
            return fp.getFeature(PipFeature.class);
        }
        return null;
    }

    public static class Builder
    implements FeatureBuilder<Builder, PipFeature> {
        private static final float ALT_ZOOM_ADJ = 1.0f;
        private static final float DEFAULT_ZOOM = 0.9f;
        private static final String DEFAULT_MASK_TEXTURE = "textures/gui/pip_mask_square.png";
        private Predicate<ConditionContext> condition = ctx -> true;
        private ResourceLocation overlayTexture;
        private ResourceLocation maskTexture;
        private float zoom;
        private Float altZoom;
        private boolean hasCustomMaskTexture;
        private boolean isParallaxEnabled;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withZoom(double zoom) {
            this.zoom = (float)zoom;
            return this;
        }

        public Builder withAltZoom(double altZoom) {
            this.altZoom = Float.valueOf((float)altZoom);
            return this;
        }

        public Builder withOverlayTexture(String texture) {
            this.overlayTexture = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)texture);
            return this;
        }

        public Builder withMaskTexture(String texture) {
            this.maskTexture = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)texture);
            this.hasCustomMaskTexture = true;
            return this;
        }

        public Builder withParallaxEnabled(boolean isParallaxEnabled) {
            this.isParallaxEnabled = isParallaxEnabled;
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            String maskTextureName;
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson(obj.get("condition")));
            }
            this.isParallaxEnabled = JsonUtil.getJsonBoolean(obj, "parallax", false);
            this.isParallaxEnabled = JsonUtil.getJsonBoolean(obj, "parallax", false);
            String overlayTextureName = JsonUtil.getJsonString(obj, "overlayTexture", null);
            if (overlayTextureName != null) {
                this.withOverlayTexture(overlayTextureName);
            }
            if ((maskTextureName = JsonUtil.getJsonString(obj, "maskTexture", null)) == null) {
                this.maskTexture = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)DEFAULT_MASK_TEXTURE);
                this.hasCustomMaskTexture = false;
            } else {
                this.withMaskTexture(maskTextureName);
            }
            float zoom = JsonUtil.getJsonFloat(obj, "zoom", 0.9f);
            this.withZoom(zoom);
            if (obj.has("altZoom")) {
                this.withAltZoom(JsonUtil.getJsonFloat(obj, "altZoom", 0.9f));
            }
            return this;
        }

        @Override
        public PipFeature build(FeatureProvider featureProvider) {
            ResourceLocation maskTexture = this.maskTexture;
            if (maskTexture == null && this.isParallaxEnabled) {
                maskTexture = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)DEFAULT_MASK_TEXTURE);
                this.hasCustomMaskTexture = false;
            }
            float actualAltZoom = this.altZoom != null ? this.altZoom.floatValue() : this.zoom * 1.0f;
            return new PipFeature(featureProvider, this.condition, this.zoom, actualAltZoom, this.isParallaxEnabled, this.overlayTexture, maskTexture, this.hasCustomMaskTexture);
        }
    }
}

