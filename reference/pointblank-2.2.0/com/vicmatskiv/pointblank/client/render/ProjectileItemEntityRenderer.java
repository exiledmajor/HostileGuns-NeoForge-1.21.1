/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.inventory.InventoryMenu
 *  net.minecraft.world.item.ItemDisplayContext
 */
package com.vicmatskiv.pointblank.client.render;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vicmatskiv.pointblank.client.EntityRendererBuilder;
import com.vicmatskiv.pointblank.entity.ProjectileLike;
import com.vicmatskiv.pointblank.util.MiscUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

public class ProjectileItemEntityRenderer<T extends Entity>
extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;
    private static ProjectileLike currentProjectile;
    private static PoseStack.Pose currentPose;

    public ProjectileItemEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    static ProjectileLike getCurrentProjectile() {
        return currentProjectile;
    }

    static PoseStack.Pose getCurrentPose() {
        return currentPose;
    }

    public void render(T projectile, float yRot, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(projectile.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f - projectile.getXRot()));
        currentProjectile = (ProjectileLike)projectile;
        currentPose = poseStack.last();
        this.itemRenderer.renderStatic(((ProjectileLike)projectile).getItem(), ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, MiscUtil.getLevel(projectile), projectile.getId());
        currentProjectile = null;
        currentPose = null;
        poseStack.popPose();
    }

    public ResourceLocation getTextureLocation(Entity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    public static class Builder
    implements EntityRendererBuilder<Builder, Entity, EntityRenderer<Entity>> {
        @Override
        public Builder withJsonObject(JsonObject obj) {
            return null;
        }

        @Override
        public EntityRenderer<Entity> build(EntityRendererProvider.Context context) {
            return new ProjectileItemEntityRenderer<Entity>(context);
        }
    }
}

