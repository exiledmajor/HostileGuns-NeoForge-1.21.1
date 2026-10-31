/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.attachment.AttachmentModelInfo;
import com.vicmatskiv.pointblank.attachment.Attachments;
import com.vicmatskiv.pointblank.client.GunStateListener;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.ConditionalFeature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.feature.Features;
import com.vicmatskiv.pointblank.feature.PipFeature;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.JsonUtil;
import java.util.NavigableMap;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class AimingFeature
extends ConditionalFeature
implements GunStateListener {
    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();
    private final float zoom;
    private final float viewBobbing;

    public static void applyAimingPosition(ItemStack itemStack, PoseStack poseStack, float rescale, float aimingProgress) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return;
        }
        if (aimingProgress > 0.0f) {
            NavigableMap<String, Pair<ItemStack, Matrix4f>> poseMatrices = AttachmentModelInfo.findInverseBoneMatrices(itemStack, "scope", rescale);
            if (!poseMatrices.isEmpty()) {
                Features.EnabledFeature aimingFeature = Features.getFirstEnabledFeature(itemStack, AimingFeature.class);
                Pair<ItemStack, Matrix4f> attachmentPos = null;
                if (aimingFeature != null) {
                    attachmentPos = (Pair<ItemStack, Matrix4f>)poseMatrices.get(aimingFeature.ownerPath());
                }
                if (attachmentPos == null) {
                    attachmentPos = poseMatrices.firstEntry().getValue();
                }
                Matrix4f scopeMatrix = (Matrix4f)attachmentPos.getSecond();
                Matrix4f poseStackBefore = new Matrix4f((Matrix4fc)poseStack.last().pose());
                if (attachmentPos == null) {
                    return;
                }
                if (aimingProgress < 1.0f) {
                    poseStack.mulPose(AimSwitchAnimation.INSTANCE.update(IDENTITY_MATRIX, (Matrix4f)attachmentPos.getSecond(), aimingProgress));
                } else {
                    poseStack.mulPose(AimSwitchAnimation.INSTANCE.update((Matrix4f)attachmentPos.getSecond()));
                }
                Matrix4f matrix4f = new Matrix4f((Matrix4fc)poseStack.last().pose());
            }
            poseStack.translate(0.0f, aimingProgress * -0.6095f * rescale, aimingProgress * -0.7f * rescale);
            Matrix4f matrix4f = new Matrix4f((Matrix4fc)poseStack.last().pose());
        }
    }

    private AimingFeature(FeatureProvider owner, Predicate<ConditionContext> predicate, float zoom, float viewBobbing) {
        super(owner, predicate);
        this.zoom = zoom;
        this.viewBobbing = viewBobbing;
    }

    public MutableComponent getDescription() {
        return Component.translatable((String)"description.pointblank.enablesAimingWithZoom").append((Component)Component.literal((String)String.format(" %.0f%%", Float.valueOf(this.zoom * 100.0f))));
    }

    public float getZoom() {
        return this.zoom;
    }

    public float getViewBobbing() {
        return this.viewBobbing;
    }

    public static float getZoom(ItemStack itemStack) {
        Pair<String, ItemStack> selected = Attachments.getSelectedAttachment(itemStack, AttachmentCategory.SCOPE);
        ItemStack selectedStack = null;
        selectedStack = selected != null ? (ItemStack)selected.getSecond() : itemStack;
        Item item = selectedStack.getItem();
        if (item instanceof FeatureProvider) {
            FeatureProvider fp = (FeatureProvider)item;
            PipFeature pipFeature = fp.getFeature(PipFeature.class);
            if (pipFeature != null && pipFeature.isFallbackRequired()) {
                return pipFeature.getAltZoom();
            }
            AimingFeature feature = fp.getFeature(AimingFeature.class);
            if (feature != null) {
                return feature.getZoom();
            }
        } else {
            item = selectedStack.getItem();
            if (item instanceof GunItem) {
                GunItem gunItem = (GunItem)item;
                return (float)gunItem.getAimingZoom();
            }
        }
        return 0.0f;
    }

    public static float getBaseZoom(ItemStack itemStack) {
        Pair<String, ItemStack> selected = Attachments.getSelectedAttachment(itemStack, AttachmentCategory.SCOPE);
        ItemStack selectedStack = null;
        selectedStack = selected != null ? (ItemStack)selected.getSecond() : itemStack;
        Item item = selectedStack.getItem();
        if (item instanceof FeatureProvider) {
            FeatureProvider fp = (FeatureProvider)item;
            AimingFeature feature = fp.getFeature(AimingFeature.class);
            if (feature != null) {
                return feature.getZoom();
            }
        } else {
            item = selectedStack.getItem();
            if (item instanceof GunItem) {
                GunItem gunItem = (GunItem)item;
                return (float)gunItem.getAimingZoom();
            }
        }
        return 0.0f;
    }

    public static float getViewBobbing(ItemStack itemStack) {
        FeatureProvider fp;
        AimingFeature feature;
        Pair<String, ItemStack> selected = Attachments.getSelectedAttachment(itemStack, AttachmentCategory.SCOPE);
        ItemStack selectedStack = null;
        selectedStack = selected != null ? (ItemStack)selected.getSecond() : itemStack;
        Item item = selectedStack.getItem();
        if (item instanceof FeatureProvider && (feature = (fp = (FeatureProvider)item).getFeature(AimingFeature.class)) != null) {
            return feature.getViewBobbing();
        }
        return 1.0f;
    }

    private static class AimSwitchAnimation {
        private static final AimSwitchAnimation INSTANCE = new AimSwitchAnimation(200L);
        protected long startTime;
        protected long nanoDuration;
        protected boolean isDone;
        protected Matrix4f fromMatrix;
        protected Matrix4f toMatrix;

        protected AimSwitchAnimation(long durationMillis) {
            this.nanoDuration = durationMillis * 1000000L;
            this.fromMatrix = IDENTITY_MATRIX;
            this.toMatrix = IDENTITY_MATRIX;
        }

        protected float getProgress() {
            double progress = (double)(System.nanoTime() - this.startTime) / (double)this.nanoDuration;
            if (progress > 1.0) {
                progress = 1.0;
            }
            return Mth.clamp((float)((float)progress), (float)0.0f, (float)1.0f);
        }

        public void reset() {
            this.isDone = false;
            this.startTime = System.nanoTime();
        }

        public Matrix4f update(Matrix4f matrix) {
            float progress;
            if (this.toMatrix != matrix) {
                this.fromMatrix = this.toMatrix;
                this.toMatrix = matrix;
                this.reset();
            }
            if ((progress = this.getProgress()) >= 1.0f) {
                this.isDone = true;
            }
            if (this.isDone) {
                return this.toMatrix;
            }
            return new Matrix4f((Matrix4fc)this.fromMatrix).lerp((Matrix4fc)this.toMatrix, progress);
        }

        public Matrix4f update(Matrix4f fromMatrix, Matrix4f toMatrix, float progress) {
            Matrix4f resultMatrix = new Matrix4f((Matrix4fc)fromMatrix).lerp((Matrix4fc)toMatrix, progress);
            AimSwitchAnimation.INSTANCE.fromMatrix = fromMatrix;
            AimSwitchAnimation.INSTANCE.toMatrix = toMatrix;
            return resultMatrix;
        }
    }

    public static class Builder
    implements FeatureBuilder<Builder, AimingFeature> {
        private static final float DEFAULT_ZOOM = 0.1f;
        private Predicate<ConditionContext> condition = ctx -> true;
        private float zoom;
        private float viewBobbing = 1.0f;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withZoom(double zoom) {
            this.zoom = (float)zoom;
            return this;
        }

        public Builder withViewBobbing(double viewBobbing) {
            this.viewBobbing = Mth.clamp((float)((float)viewBobbing), (float)0.0f, (float)1.0f);
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            this.withZoom(JsonUtil.getJsonFloat(obj, "zoom", 0.1f));
            this.withViewBobbing(JsonUtil.getJsonFloat(obj, "viewBobbing", 1.0f));
            return this;
        }

        @Override
        public AimingFeature build(FeatureProvider featureProvider) {
            return new AimingFeature(featureProvider, this.condition, this.zoom, this.viewBobbing);
        }
    }
}

