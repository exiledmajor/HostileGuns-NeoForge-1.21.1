/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemDisplayContext
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 */
package com.vicmatskiv.pointblank.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.render.BufferSourceProvider;
import com.vicmatskiv.pointblank.client.render.Flushable;
import com.vicmatskiv.pointblank.client.render.GunItemRenderer;
import com.vicmatskiv.pointblank.client.render.HierarchicalRenderContext;
import com.vicmatskiv.pointblank.client.render.RenderPass;
import com.vicmatskiv.pointblank.client.render.RenderTypeKey;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.client.render.layer.FeaturePassLayer;
import com.vicmatskiv.pointblank.feature.PipFeature;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.Collections;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class SeeThroughItemLayer
extends FeaturePassLayer<GunItem> {
    private RenderPass currentRenderPass = RenderPass.SEE_THROUGH;

    public SeeThroughItemLayer(GunItemRenderer renderer) {
        super(renderer, PipFeature.class, RenderPass.SEE_THROUGH, Collections.singleton("scopepip"), true, null);
    }

    @Override
    public RenderType getRenderType() {
        return RenderTypeProvider.NO_RENDER_TYPE;
    }

    @Override
    public RenderPass getRenderPass() {
        return this.currentRenderPass;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void render(BakedGeoModel attachmentModel, PoseStack poseStack, MultiBufferSource bufferSource, GunItem animatable, RenderType renderType, VertexConsumer ignoredBuffer, float partialTick, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        PipFeature pipFeature = PipFeature.getSelected(HierarchicalRenderContext.getRootItemStack());
        if (pipFeature == null) {
            return;
        }
        HierarchicalRenderContext rootContext = HierarchicalRenderContext.getRoot();
        Object isPipFallbackEnabled = rootContext.getAttribute("isPipFallbackEnabled");
        if (isPipFallbackEnabled == null || !((Boolean)isPipFallbackEnabled).booleanValue()) {
            return;
        }
        this.currentRenderPass = RenderPass.SEE_THROUGH;
        ResourceLocation maskTexture = pipFeature.getMaskTexture();
        ResourceLocation overlayTexture = pipFeature.getOverlayTexture();
        boolean isParallaxEnabled = pipFeature.isParallaxEnabled();
        if (overlayTexture != null) {
            try (HierarchicalRenderContext subHrc = HierarchicalRenderContext.push();){
                subHrc.setAttribute("is_parallax_enabled", isParallaxEnabled);
                RenderPass.push(RenderPass.SEE_THROUGH_OVERLAY);
                this.currentRenderPass = RenderPass.SEE_THROUGH_OVERLAY;
                try {
                    RenderType overlayRenderType = RenderTypeProvider.getInstance().getPipOverlayRenderType(new RenderTypeKey(overlayTexture, maskTexture != null, 514, 7680, 2));
                    VertexConsumer overlayBuffer = bufferSource.getBuffer(overlayRenderType);
                    super.render(attachmentModel, poseStack, bufferSource, (GeoAnimatable)animatable, overlayRenderType, overlayBuffer, partialTick, packedLight, overlay, red, green, blue, alpha);
                    if (bufferSource instanceof Flushable) {
                        Flushable flushable = (Flushable)bufferSource;
                        flushable.flush();
                    }
                }
                finally {
                    RenderPass.pop();
                    this.currentRenderPass = RenderPass.SEE_THROUGH;
                }
            }
        }
    }

    @Override
    public boolean isSupportedItemDisplayContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }

    public static boolean isParallaxEnabled() {
        HierarchicalRenderContext current = HierarchicalRenderContext.current();
        Boolean isParallaxEnabled = (Boolean)current.getAttribute("is_parallax_enabled");
        return isParallaxEnabled != null && isParallaxEnabled != false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void preRender(BakedGeoModel attachmentModel, PoseStack poseStack, MultiBufferSource bufferSourceOrig, GunItem animatable, RenderType renderType, VertexConsumer buffer, float partialTick, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        Flushable flushable;
        VertexConsumer maskBuffer;
        RenderType maskRenderType;
        MultiBufferSource bufferSource = BufferSourceProvider.getInstance().getBufferSource();
        PipFeature pipFeature = PipFeature.getSelected(HierarchicalRenderContext.getRootItemStack());
        if (pipFeature == null) {
            return;
        }
        HierarchicalRenderContext rootContext = HierarchicalRenderContext.getRoot();
        Object isPipFallbackEnabled = rootContext.getAttribute("isPipFallbackEnabled");
        if (isPipFallbackEnabled == null || !((Boolean)isPipFallbackEnabled).booleanValue()) {
            return;
        }
        ResourceLocation maskTexture = pipFeature.getMaskTexture();
        if (maskTexture == null) {
            return;
        }
        this.currentRenderPass = RenderPass.SEE_THROUGH;
        RenderPass.push(RenderPass.SEE_THROUGH_MASK);
        this.currentRenderPass = RenderPass.SEE_THROUGH_MASK;
        try {
            maskRenderType = RenderTypeProvider.getInstance().getPipMaskRenderType(new RenderTypeKey(maskTexture, true, 519, 7681, 1));
            maskBuffer = bufferSource.getBuffer(maskRenderType);
            poseStack.pushPose();
            poseStack.translate(-0.5f, -0.51f, -0.5f);
            super.preRender(attachmentModel, poseStack, bufferSource, (GeoAnimatable)animatable, maskRenderType, maskBuffer, partialTick, packedLight, overlay, red, green, blue, alpha);
            poseStack.popPose();
            if (bufferSource instanceof Flushable) {
                flushable = (Flushable)bufferSource;
                flushable.flush();
            }
        }
        finally {
            RenderPass.pop();
            this.currentRenderPass = RenderPass.SEE_THROUGH;
        }
        RenderPass.push(RenderPass.SEE_THROUGH_MASK_FRONT);
        this.currentRenderPass = RenderPass.SEE_THROUGH_MASK_FRONT;
        try {
            maskRenderType = RenderTypeProvider.getInstance().getPipMaskRenderType(new RenderTypeKey(maskTexture, true, 514, 7682, 1));
            maskBuffer = bufferSource.getBuffer(maskRenderType);
            poseStack.pushPose();
            poseStack.translate(-0.5f, -0.51f, -0.5f);
            super.preRender(attachmentModel, poseStack, bufferSource, (GeoAnimatable)animatable, maskRenderType, maskBuffer, partialTick, packedLight, overlay, red, green, blue, alpha);
            poseStack.popPose();
            if (bufferSource instanceof Flushable) {
                flushable = (Flushable)bufferSource;
                flushable.flush();
            }
        }
        finally {
            RenderPass.pop();
            this.currentRenderPass = RenderPass.SEE_THROUGH;
        }
    }
}

