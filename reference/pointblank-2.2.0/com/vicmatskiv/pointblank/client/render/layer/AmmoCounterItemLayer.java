/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.world.item.ItemDisplayContext
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 */
package com.vicmatskiv.pointblank.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.render.GunItemRenderer;
import com.vicmatskiv.pointblank.client.render.HierarchicalRenderContext;
import com.vicmatskiv.pointblank.client.render.RenderPass;
import com.vicmatskiv.pointblank.client.render.RenderTypeKey;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.client.render.layer.FeaturePassLayer;
import com.vicmatskiv.pointblank.feature.AmmoCounterFeature;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.Collections;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class AmmoCounterItemLayer
extends FeaturePassLayer<GunItem> {
    public AmmoCounterItemLayer(GunItemRenderer renderer) {
        super(renderer, AmmoCounterFeature.class, RenderPass.AMMO_COUNTER, Collections.singleton("_ammo_"), false, null);
    }

    @Override
    public void render(PoseStack poseStack, GunItem animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.renderPass(() -> {
            GunItemRenderer renderer1 = (GunItemRenderer)this.getRenderer();
            RenderType ammoRenderType = RenderTypeProvider.getInstance().getDefaultItemRenderType(new RenderTypeKey(renderer1.getTextureLocation(animatable), false, 0, 0, 0));
            poseStack.pushPose();
            renderer1.applyFirstPersonBodyInertiaForCurrentState(HierarchicalRenderContext.getRootItemStack(), poseStack);
            try {
                this.render(this.getDefaultBakedModel((GeoAnimatable)animatable), poseStack, bufferSource, (GeoAnimatable)animatable, ammoRenderType, bufferSource.getBuffer(ammoRenderType), partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
            }
            finally {
                poseStack.popPose();
            }
        });
    }

    @Override
    public RenderType getRenderType() {
        return RenderTypeProvider.NO_RENDER_TYPE;
    }

    @Override
    public boolean isSupportedItemDisplayContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }
}

