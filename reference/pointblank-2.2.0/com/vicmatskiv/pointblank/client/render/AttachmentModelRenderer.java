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
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.model.DefaultedItemGeoModel
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoItemRenderer
 *  software.bernie.geckolib.renderer.GeoRenderer
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.render.HierarchicalRenderContext;
import com.vicmatskiv.pointblank.client.render.RenderPass;
import com.vicmatskiv.pointblank.client.render.RenderPassGeoRenderer;
import com.vicmatskiv.pointblank.client.render.layer.AttachmentLayer;
import com.vicmatskiv.pointblank.item.AttachmentItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

public class AttachmentModelRenderer
extends GeoItemRenderer<AttachmentItem>
implements RenderPassGeoRenderer<AttachmentItem> {
    public AttachmentModelRenderer(String resourceName) {
        super((GeoModel)new DefaultedItemGeoModel(ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)resourceName)));
        this.addRenderLayer(new AttachmentLayer<AttachmentItem>(this));
    }

    public void renderByItem(ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        try (HierarchicalRenderContext hrc = HierarchicalRenderContext.push(itemStack, itemDisplayContext);){
            super.renderByItem(itemStack, itemDisplayContext, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (bone.getName().charAt(0) == '_' || bone.getName().equals("scope") || bone.getName().equals("scopepip") || bone.getName().equals("muzzleflash") || bone.getName().equals("muzzle")) {
            return;
        }
        super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public GeoRenderer<AttachmentItem> getRenderer() {
        return this;
    }

    @Override
    public boolean isEffectLayer() {
        return false;
    }

    @Override
    public RenderType getRenderType() {
        return null;
    }

    @Override
    public boolean isSupportedItemDisplayContext(ItemDisplayContext context) {
        return false;
    }

    @Override
    public RenderPass getRenderPass() {
        return null;
    }
}

