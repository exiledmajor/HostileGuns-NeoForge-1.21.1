/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.world.item.Item
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
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.client.render.layer.FeaturePassLayer;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.feature.ReticleFeature;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class ReticleItemLayer
extends FeaturePassLayer<GunItem> {
    public static final String HRC_ATTRIBUTE_PARALLAX_ENABLED = "is_parallax_enabled";
    public static final String HRC_ATTRIBUTE_MAX_ANGULAR_OFFSET_COS = "max_angular_offset_cos";

    public ReticleItemLayer(GunItemRenderer renderer) {
        super(renderer, ReticleFeature.class, RenderPass.RETICLE, List.of("scope", "reticle"), true, null);
    }

    @Override
    public void render(BakedGeoModel attachmentModel, PoseStack poseStack, MultiBufferSource bufferSource, GunItem animatable, RenderType origRenderType, VertexConsumer origBuffer, float partialTick, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        FeatureProvider fp;
        ReticleFeature feature;
        Item item;
        boolean isParallaxEnabled = false;
        float maxAngularOffsetCos = ReticleFeature.DEFAULT_MAX_ANGULAR_OFFSET_COS;
        RenderType renderType = null;
        HierarchicalRenderContext hrc = HierarchicalRenderContext.current();
        if (hrc != null && (item = hrc.getItemStack().getItem()) instanceof FeatureProvider && (feature = (fp = (FeatureProvider)item).getFeature(ReticleFeature.class)) != null && feature.isEnabled(hrc.getItemStack())) {
            isParallaxEnabled = feature.isParallaxEnabled();
            maxAngularOffsetCos = feature.getMaxAngularOffsetCos();
            RenderTypeProvider renderTypeProvider = RenderTypeProvider.getInstance();
            renderType = renderTypeProvider.getReticleRenderType(feature.getTexture(), isParallaxEnabled);
        }
        if (renderType == null) {
            return;
        }
        VertexConsumer buffer = bufferSource.getBuffer(renderType);
        RenderTypeProvider renderTypeProvider = RenderTypeProvider.getInstance();
        float reticleBrightness = renderTypeProvider.getReticleBrightness();
        try (HierarchicalRenderContext subHrc = HierarchicalRenderContext.push();){
            subHrc.setAttribute(HRC_ATTRIBUTE_PARALLAX_ENABLED, isParallaxEnabled);
            subHrc.setAttribute(HRC_ATTRIBUTE_MAX_ANGULAR_OFFSET_COS, Float.valueOf(maxAngularOffsetCos));
            RenderPass renderPass = RenderPass.current();
            super.render(attachmentModel, poseStack, bufferSource, (GeoAnimatable)animatable, renderType, buffer, partialTick, packedLight, overlay, reticleBrightness, reticleBrightness, reticleBrightness, 1.0f);
        }
    }

    @Override
    public RenderType getRenderType() {
        return RenderTypeProvider.NO_RENDER_TYPE;
    }

    @Override
    public boolean isSupportedItemDisplayContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }

    public static boolean isParallaxEnabled() {
        HierarchicalRenderContext current = HierarchicalRenderContext.current();
        Boolean isParallaxEnabled = (Boolean)current.getAttribute(HRC_ATTRIBUTE_PARALLAX_ENABLED);
        return isParallaxEnabled != null && isParallaxEnabled != false;
    }
}

