/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package com.vicmatskiv.pointblank.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.render.BaseModelBlockRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public abstract class BaseModelBlockLayer<T extends BlockEntity>
extends GeoRenderLayer<T> {
    protected BaseModelBlockRenderer<T> renderer;
    protected boolean isRendering;

    public BaseModelBlockLayer(BaseModelBlockRenderer<T> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.isRendering = true;
        try {
            this.renderer.reRender(bakedModel, poseStack, bufferSource, (GeoAnimatable)animatable, renderType, bufferSource.getBuffer(renderType), partialTick, packedLight, OverlayTexture.NO_OVERLAY, -1);
        }
        finally {
            this.isRendering = false;
        }
    }

    public abstract boolean shouldRender(String var1, BlockEntity var2);
}

