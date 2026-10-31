/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.world.item.ItemDisplayContext
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.renderer.GeoRenderer
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.render.HierarchicalRenderContext;
import com.vicmatskiv.pointblank.client.render.RenderPass;
import com.vicmatskiv.pointblank.client.render.RenderPassProvider;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.util.MiscUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public interface RenderPassRenderer<T extends GeoAnimatable>
extends RenderPassProvider {
    public GeoRenderer<T> getRenderer();

    public boolean isEffectLayer();

    default public RenderType getPreRenderType() {
        return RenderTypeProvider.NO_RENDER_TYPE;
    }

    public RenderType getRenderType();

    public boolean isSupportedItemDisplayContext(ItemDisplayContext var1);

    default public void renderPass(Runnable runnable) {
        ItemDisplayContext itemDisplayContext = HierarchicalRenderContext.current().getItemDisplayContext();
        if (!this.isSupportedItemDisplayContext(itemDisplayContext)) {
            return;
        }
        RenderPass.push(this.getRenderPass());
        try {
            runnable.run();
        }
        finally {
            RenderPass.pop();
        }
    }

    default public void render(BakedGeoModel attachmentModel, PoseStack poseStack, MultiBufferSource bufferSource, T animatable, RenderType renderType, VertexConsumer buffer, float partialTick, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        int color = MiscUtil.toColor(red, green, blue, alpha);
        this.getRenderer().reRender(attachmentModel, poseStack, bufferSource, animatable, renderType, buffer, partialTick, packedLight, overlay, color);
    }

    default public void preRender(BakedGeoModel attachmentModel, PoseStack poseStack, MultiBufferSource bufferSource, T animatable, RenderType renderType, VertexConsumer buffer, float partialTick, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        int color = MiscUtil.toColor(red, green, blue, alpha);
        this.getRenderer().preRender(poseStack, animatable, attachmentModel, bufferSource, buffer, false, partialTick, packedLight, overlay, color);
    }

    default public void postRender(BakedGeoModel attachmentModel, PoseStack poseStack, MultiBufferSource bufferSource, T animatable, RenderType renderType, VertexConsumer buffer, float partialTick, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        int color = MiscUtil.toColor(red, green, blue, alpha);
        this.getRenderer().postRender(poseStack, animatable, attachmentModel, bufferSource, buffer, false, partialTick, packedLight, overlay, color);
    }
}

