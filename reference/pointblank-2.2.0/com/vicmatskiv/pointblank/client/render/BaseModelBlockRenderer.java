/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.renderer.GeoBlockRenderer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.model.BaseBlockModel;
import com.vicmatskiv.pointblank.client.render.layer.BaseModelBlockLayer;
import com.vicmatskiv.pointblank.client.render.layer.GlowingBlockEntityLayer;
import com.vicmatskiv.pointblank.compat.iris.IrisCompat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class BaseModelBlockRenderer<T extends BlockEntity>
extends GeoBlockRenderer<T> {
    private BaseBlockModel<T> baseBlockModel;
    private BlockEntity currentBlockEntity;

    public BaseModelBlockRenderer(BaseBlockModel<T> baseBlockModel) {
        super(baseBlockModel);
        this.baseBlockModel = baseBlockModel;
        if (!baseBlockModel.getGlowingParts().isEmpty()) {
            this.addRenderLayer(new GlowingBlockEntityLayer(this));
        }
    }

    public BaseBlockModel<T> getModel() {
        return this.baseBlockModel;
    }

    private boolean shouldRender(String boneName) {
        boolean shouldRender = true;
        for (GeoRenderLayer layer : this.getRenderLayers()) {
            BaseModelBlockLayer baseModelBlockLayer;
            if (!(layer instanceof BaseModelBlockLayer) || (baseModelBlockLayer = (BaseModelBlockLayer)layer).shouldRender(boneName, this.currentBlockEntity)) continue;
            shouldRender = false;
            break;
        }
        return shouldRender;
    }

    public void render(T animatable, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (IrisCompat.getInstance().isRenderingShadows()) {
            return;
        }
        this.currentBlockEntity = animatable;
        super.render(animatable, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (this.shouldRender(bone.getName())) {
            super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, color);
        }
    }

    public record RenderInfo(BaseModelBlockLayer<?> layer, BoneRenderVote mode) {
    }

    public static enum BoneRenderVote {
        ALLOW,
        DENY;

    }
}

