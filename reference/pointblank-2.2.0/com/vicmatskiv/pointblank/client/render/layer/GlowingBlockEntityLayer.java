/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 */
package com.vicmatskiv.pointblank.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.model.BaseBlockModel;
import com.vicmatskiv.pointblank.client.render.BaseModelBlockRenderer;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.client.render.layer.BaseModelBlockLayer;
import java.util.function.Predicate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class GlowingBlockEntityLayer<T extends BlockEntity>
extends BaseModelBlockLayer<T> {
    private BaseBlockModel<T> model;
    private ResourceLocation texture;

    public GlowingBlockEntityLayer(BaseModelBlockRenderer<T> renderer) {
        super(renderer);
        this.renderer = renderer;
        this.model = renderer.getModel();
        this.texture = renderer.getTextureLocation((GeoAnimatable)renderer.getAnimatable());
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderTypeProvider renderTypeProvider = RenderTypeProvider.getInstance();
        RenderType glowRenderType = renderTypeProvider.getGlowBlockEntityRenderType(this.texture);
        packedLight = 240;
        super.render(poseStack, animatable, bakedModel, glowRenderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRender(String boneName, BlockEntity blockEntity) {
        Predicate<BlockEntity> predicate = this.model.getGlowingParts().get(boneName);
        return !this.isRendering || predicate != null && predicate.test(blockEntity);
    }
}

