/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.model.DefaultedItemGeoModel
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoItemRenderer
 *  software.bernie.geckolib.renderer.GeoRenderer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 *  software.bernie.geckolib.util.RenderUtil
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vicmatskiv.pointblank.client.effect.Effect;
import com.vicmatskiv.pointblank.client.effect.EffectRenderContext;
import com.vicmatskiv.pointblank.client.render.ProjectileItemEntityRenderer;
import com.vicmatskiv.pointblank.client.uv.StaticSpriteUVProvider;
import com.vicmatskiv.pointblank.entity.ProjectileLike;
import com.vicmatskiv.pointblank.item.AmmoItem;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

public class ProjectileItemRenderer
extends GeoItemRenderer<AmmoItem> {
    public static final String BONE_NOZZLE = "nozzle";

    public ProjectileItemRenderer(String resourceName) {
        super((GeoModel)new DefaultedItemGeoModel(ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)resourceName)));
        this.addRenderLayer(new EffectsLayer((GeoRenderer<AmmoItem>)this));
    }

    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (!BONE_NOZZLE.equals(bone.getName())) {
            super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, color);
        }
    }

    public static class EffectsLayer
    extends GeoRenderLayer<AmmoItem> {
        public EffectsLayer(GeoRenderer<AmmoItem> entityRendererIn) {
            super(entityRendererIn);
        }

        public void render(PoseStack poseStack, AmmoItem animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            GeoBone nozzleBone;
            ProjectileLike projectile = ProjectileItemEntityRenderer.getCurrentProjectile();
            PoseStack.Pose pose = ProjectileItemEntityRenderer.getCurrentPose();
            if (projectile != null && pose != null && (nozzleBone = (GeoBone)bakedModel.getBone(ProjectileItemRenderer.BONE_NOZZLE).orElse(null)) != null) {
                poseStack.pushPose();
                RenderUtil.translateToPivotPoint((PoseStack)poseStack, (GeoBone)nozzleBone);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                List<Effect> attachedEffects = projectile.getActiveAttachedEffects();
                for (Effect attachedEffect : attachedEffects) {
                    EffectRenderContext effectRenderContext = new EffectRenderContext().withPoseStack(poseStack).withProgress(projectile.getProgress(partialTick)).withLightColor(packedLight).withSpriteUVProvider(StaticSpriteUVProvider.INSTANCE).withBufferSource(bufferSource);
                    attachedEffect.render(effectRenderContext);
                }
                poseStack.popPose();
            }
        }
    }
}

