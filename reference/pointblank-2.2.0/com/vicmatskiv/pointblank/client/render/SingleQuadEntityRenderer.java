/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DestFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SourceFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.BufferUploader
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  org.joml.Matrix4f
 *  org.joml.Vector4f
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vicmatskiv.pointblank.entity.SlowProjectile;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class SingleQuadEntityRenderer
extends EntityRenderer<SlowProjectile> {
    private ResourceLocation texture = ResourceLocation.parse((String)"pointblank:textures/effect/laser3.png");

    public SingleQuadEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(SlowProjectile projectile, float p_114657_, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int lightColor) {
        this.renderOrig(projectile, p_114657_, partialTick, poseStack, bufferSource, lightColor);
    }

    public void renderOrig(SlowProjectile projectile, float p_114657_, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int lightColor) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        poseStack.mulPose(camera.rotation());
        float width = 0.5f;
        float length = 0.0f;
        poseStack.pushPose();
        float u = 1.0f;
        float brightness = 1.0f;
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)this.texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        Matrix4f matrix4f = poseStack.last().pose();
        Vector4f t1 = new Vector4f(width, width, 0.0f, 1.0f);
        matrix4f.transform(t1);
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix4f, -width + length, width, 0.0f).setUv(0.0f, 0.0f).setColor(1.0f, 1.0f, 1.0f, brightness);
        bufferbuilder.addVertex(matrix4f, width, width, 0.0f).setUv(u, 0.0f).setColor(1.0f, 1.0f, 1.0f, brightness);
        bufferbuilder.addVertex(matrix4f, width, -width, 0.0f).setUv(u, 1.0f).setColor(1.0f, 1.0f, 1.0f, brightness);
        bufferbuilder.addVertex(matrix4f, -width + length, -width, 0.0f).setUv(0.0f, 1.0f).setColor(1.0f, 1.0f, 1.0f, brightness);
        BufferUploader.drawWithShader((MeshData)bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }

    public ResourceLocation getTextureLocation(SlowProjectile entity) {
        return this.texture;
    }
}

