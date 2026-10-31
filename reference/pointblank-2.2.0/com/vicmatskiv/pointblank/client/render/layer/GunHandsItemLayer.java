/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemDisplayContext
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.renderer.GeoRenderer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package com.vicmatskiv.pointblank.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.render.GunItemRenderer;
import com.vicmatskiv.pointblank.client.render.HierarchicalRenderContext;
import com.vicmatskiv.pointblank.client.render.RenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GunHandsItemLayer<T extends GeoAnimatable>
extends GeoRenderLayer<T> {
    public GunHandsItemLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        ResourceLocation texture = mc.player.getSkin().texture();
        ItemDisplayContext itemDisplayContext = HierarchicalRenderContext.current().getItemDisplayContext();
        if (itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            RenderType handRenderType = this.renderer.getRenderType(animatable, texture, bufferSource, mc.getTimer().getGameTimeDeltaPartialTick(true));
            RenderPass.push(RenderPass.HANDS);
            try {
                poseStack.pushPose();
                GeoRenderer geoRenderer = this.renderer;
                if (geoRenderer instanceof GunItemRenderer) {
                    GunItemRenderer gunItemRenderer = (GunItemRenderer)geoRenderer;
                    gunItemRenderer.applyFirstPersonBodyInertiaForCurrentState(HierarchicalRenderContext.getRootItemStack(), poseStack);
                }
                this.getRenderer().reRender(this.getDefaultBakedModel((GeoAnimatable)animatable), poseStack, bufferSource, animatable, handRenderType, bufferSource.getBuffer(handRenderType), partialTick, packedLight, OverlayTexture.NO_OVERLAY, -1);
                poseStack.popPose();
            }
            finally {
                RenderPass.pop();
            }
        }
    }
}

