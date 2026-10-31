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

public class PipItemLayer
extends FeaturePassLayer<GunItem> {
    private RenderPass currentRenderPass = RenderPass.PIP;

    public PipItemLayer(GunItemRenderer renderer) {
        super(renderer, PipFeature.class, RenderPass.PIP, Collections.singleton("scopepip"), true, null);
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
        if (isPipFallbackEnabled != null && ((Boolean)isPipFallbackEnabled).booleanValue()) {
            return;
        }
        this.currentRenderPass = RenderPass.PIP;
        ResourceLocation maskTexture = pipFeature.getMaskTexture();
        ResourceLocation overlayTexture = pipFeature.getOverlayTexture();
        boolean isParallaxEnabled = pipFeature.isParallaxEnabled();
        boolean useStencil = maskTexture != null;
        RenderType pipRenderType = RenderTypeProvider.getInstance().getPipRenderType(new RenderTypeKey(maskTexture, useStencil, useStencil ? 514 : 519, 7680, useStencil ? 2 : 1));
        super.render(attachmentModel, poseStack, bufferSource, (GeoAnimatable)animatable, pipRenderType, bufferSource.getBuffer(pipRenderType), partialTick, packedLight, overlay, red, green, blue, alpha);
        if (bufferSource instanceof Flushable) {
            Flushable flushable = (Flushable)bufferSource;
            flushable.flush();
        }
        if (overlayTexture != null) {
            try (HierarchicalRenderContext subHrc = HierarchicalRenderContext.push();){
                subHrc.setAttribute("is_parallax_enabled", isParallaxEnabled);
                RenderPass.push(RenderPass.PIP_OVERLAY);
                this.currentRenderPass = RenderPass.PIP_OVERLAY;
                try {
                    RenderType overlayRenderType = RenderTypeProvider.getInstance().getPipOverlayRenderType(new RenderTypeKey(overlayTexture, useStencil, useStencil ? 514 : 519, 7680, useStencil ? 2 : 1));
                    VertexConsumer overlayBuffer = bufferSource.getBuffer(overlayRenderType);
                    super.render(attachmentModel, poseStack, bufferSource, (GeoAnimatable)animatable, overlayRenderType, overlayBuffer, partialTick, packedLight, overlay, red, green, blue, alpha);
                    if (bufferSource instanceof Flushable) {
                        Flushable flushable = (Flushable)bufferSource;
                        flushable.flush();
                    }
                }
                finally {
                    RenderPass.pop();
                    this.currentRenderPass = RenderPass.PIP;
                }
            }
        }
    }

    @Override
    public void preRender(BakedGeoModel attachmentModel, PoseStack poseStack, MultiBufferSource ignoredBufferSource, GunItem animatable, RenderType renderType, VertexConsumer buffer, float partialTick, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        PipFeature pipFeature = PipFeature.getSelected(HierarchicalRenderContext.getRootItemStack());
        if (pipFeature == null || pipFeature.getMaskTexture() == null) {
            return;
        }
        HierarchicalRenderContext rootContext = HierarchicalRenderContext.getRoot();
        Object isPipFallbackEnabled = rootContext.getAttribute("isPipFallbackEnabled");
        if (isPipFallbackEnabled != null && ((Boolean)isPipFallbackEnabled).booleanValue()) {
            return;
        }
        MultiBufferSource bufferSource = BufferSourceProvider.getInstance().getBufferSource();
        ResourceLocation maskTexture = pipFeature.getMaskTexture();
        this.currentRenderPass = RenderPass.PIP;
        this.renderMaskPass(attachmentModel, poseStack, bufferSource, animatable, maskTexture, RenderPass.PIP_MASK, 519, 7681, partialTick, packedLight, overlay, red, green, blue, alpha);
        this.renderMaskPass(attachmentModel, poseStack, bufferSource, animatable, maskTexture, RenderPass.PIP_MASK_FAR, 514, 7682, partialTick, packedLight, overlay, red, green, blue, alpha);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderMaskPass(BakedGeoModel attachmentModel, PoseStack poseStack, MultiBufferSource bufferSource, GunItem animatable, ResourceLocation maskTexture, RenderPass maskPass, int stencilFunc, int stencilOp, float partialTick, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        RenderPass.push(maskPass);
        this.currentRenderPass = maskPass;
        try {
            RenderType maskRenderType = RenderTypeProvider.getInstance().getPipMaskRenderType(new RenderTypeKey(maskTexture, true, stencilFunc, stencilOp, 1));
            VertexConsumer maskBuffer = bufferSource.getBuffer(maskRenderType);
            poseStack.pushPose();
            try {
                poseStack.translate(-0.5f, -0.51f, -0.5f);
                super.preRender(attachmentModel, poseStack, bufferSource, (GeoAnimatable)animatable, maskRenderType, maskBuffer, partialTick, packedLight, overlay, red, green, blue, alpha);
            }
            finally {
                poseStack.popPose();
            }
            if (bufferSource instanceof Flushable) {
                Flushable flushable = (Flushable)bufferSource;
                flushable.flush();
            }
        }
        finally {
            RenderPass.pop();
            this.currentRenderPass = RenderPass.PIP;
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
}

