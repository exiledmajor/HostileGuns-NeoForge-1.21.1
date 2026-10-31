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
 *  net.minecraft.world.item.ItemStack
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
import com.vicmatskiv.pointblank.feature.GlowFeature;
import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class GlowingItemLayer
extends FeaturePassLayer<GunItem> {
    public static final String HRC_ATTRIBUTE_GLOW_ENABLED = "isGlowEnabled";
    private RenderType renderType;

    public GlowingItemLayer(GunItemRenderer renderer, int effectId, ResourceLocation texture) {
        super(renderer, GlowFeature.class, RenderPass.GLOW, ALL_PARTS, true, effectId);
        RenderTypeProvider renderTypeProvider = RenderTypeProvider.getInstance();
        this.renderType = renderTypeProvider.getGlowRenderType(texture);
    }

    protected RenderType getRenderType(GunItem animatable) {
        return this.renderType;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void render(BakedGeoModel attachmentModel, PoseStack poseStack, MultiBufferSource bufferSource, GunItem animatable, RenderType renderType, VertexConsumer buffer, float partialTick, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        block7: {
            RenderPass.push(this.getRenderPass());
            try {
                RenderPass.setEffectId(this.effectId);
                RenderTypeProvider renderTypeProvider = RenderTypeProvider.getInstance();
                float glowBrightness = renderTypeProvider.getGlowBrightness();
                ItemDisplayContext itemDisplayContext = HierarchicalRenderContext.current().getItemDisplayContext();
                if (itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                    poseStack.pushPose();
                    ((GunItemRenderer)this.renderer).applyFirstPersonBodyInertiaForCurrentState(HierarchicalRenderContext.getRootItemStack(), poseStack);
                    try {
                        super.render(attachmentModel, poseStack, bufferSource, (GeoAnimatable)animatable, renderType, buffer, partialTick, packedLight, overlay, glowBrightness, glowBrightness, glowBrightness, 1.0f);
                        break block7;
                    }
                    finally {
                        poseStack.popPose();
                    }
                }
                super.render(attachmentModel, poseStack, bufferSource, (GeoAnimatable)animatable, renderType, buffer, partialTick, packedLight, overlay, glowBrightness, glowBrightness, glowBrightness, 1.0f);
            }
            finally {
                RenderPass.pop();
            }
        }
    }

    @Override
    public RenderType getRenderType() {
        return this.renderType;
    }

    @Override
    public boolean isSupportedItemDisplayContext(ItemDisplayContext context) {
        return true;
    }

    @Override
    public boolean approveRendering(RenderPass renderPass, String partName, ItemStack rootStack, ItemStack currentStack, String path, ItemDisplayContext itemDisplayContext) {
        if (GlowingItemLayer.isGlowEnabled()) {
            return true;
        }
        return super.approveRendering(renderPass, partName, rootStack, currentStack, path, itemDisplayContext);
    }

    public static boolean isGlowEnabled() {
        HierarchicalRenderContext current = HierarchicalRenderContext.current();
        Boolean isGlowEnabled = (Boolean)current.getAttribute(HRC_ATTRIBUTE_GLOW_ENABLED);
        return isGlowEnabled != null && isGlowEnabled != false;
    }

    public static void setGlowEnabled(boolean isGlowEnabled) {
        HierarchicalRenderContext current = HierarchicalRenderContext.current();
        current.setAttribute(HRC_ATTRIBUTE_GLOW_ENABLED, isGlowEnabled);
    }
}

