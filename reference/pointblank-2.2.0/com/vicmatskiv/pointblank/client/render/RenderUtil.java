/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.BufferUploader
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  com.mojang.math.Divisor
 *  it.unimi.dsi.fastutil.ints.IntIterator
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  software.bernie.geckolib.cache.object.GeoQuad
 *  software.bernie.geckolib.cache.object.GeoVertex
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Divisor;
import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.client.ClientSystem;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

public class RenderUtil {
    private static final double FOV_NORMAL_DEGREES = 70.0;
    private static Matrix4f projectionMatrix;
    private static Matrix4f projectionMatrixInverted;
    private static Matrix4f modelViewMatrix;
    private static Matrix4f modelViewMatrixInverted;

    public static void renderQuad(PoseStack poseStack, GeoQuad quad, VertexConsumer buffer, float dx, float dy, int color) {
        Matrix4f poseState = poseStack.last().pose();
        float[][] texUV = new float[][]{{1.0f + dx, 0.0f + dy}, {0.0f + dx, 0.0f + dy}, {0.0f + dx, 1.0f + dy}, {1.0f + dx, 1.0f + dy}};
        for (int i = 0; i < 4; ++i) {
            GeoVertex vertex = quad.vertices()[i];
            float u = Mth.clamp((float)texUV[i][0], (float)0.0f, (float)1.0f);
            float v = Mth.clamp((float)texUV[i][1], (float)0.0f, (float)1.0f);
            buffer.addVertex(poseState, vertex.position().x, vertex.position().y, vertex.position().z).setUv(u, v).setUv1(0, 0).setUv2(0, 0).setNormal(0.0f, 1.0f, 0.0f).setColor(color);
        }
    }

    public static void renderColoredQuad(PoseStack poseStack, GeoQuad quad, VertexConsumer buffer, float red, float green, float blue, float alpha) {
        Matrix4f poseState = poseStack.last().pose();
        for (int i = 0; i < 4; ++i) {
            GeoVertex v = quad.vertices()[i];
            buffer.addVertex(poseState, v.position().x, v.position().y, v.position().z).setColor(red, green, blue, alpha);
        }
    }

    public static GeoQuad resizeQuad(GeoQuad inputQuad, float size) {
        GeoVertex[] inputVertices;
        Vector3f center = new Vector3f();
        for (GeoVertex vertex : inputVertices = inputQuad.vertices()) {
            center.add((Vector3fc)vertex.position());
        }
        center.div((float)inputVertices.length);
        GeoVertex[] newVertices = new GeoVertex[inputVertices.length];
        for (int i = 0; i < inputVertices.length; ++i) {
            GeoVertex vertex = inputVertices[i];
            Vector3f direction = new Vector3f((Vector3fc)vertex.position()).sub((Vector3fc)center);
            Vector3f newPosition = new Vector3f((Vector3fc)center).add((Vector3fc)direction.mul(size));
            newVertices[i] = new GeoVertex(newPosition, vertex.texU(), vertex.texV());
        }
        return new GeoQuad(newVertices, inputQuad.normal(), inputQuad.direction());
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation textureResource, float posX, float posY, int zLevel, float minU, float minV, int width, int height, int actualWidth, int actualHeight) {
        RenderUtil.blit(guiGraphics, textureResource, posX, posX + (float)width, posY, posY + (float)height, zLevel, width, height, minU, minV, actualWidth, actualHeight);
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation textureResource, float posXStart, float posXEnd, float posYStart, float posYEnd, int zLevel, int width, int height, float minU, float minV, int actualWidth, int actualHeight) {
        RenderUtil.blit(guiGraphics, textureResource, posXStart, posXEnd, posYStart, posYEnd, (float)zLevel, (minU + 0.0f) / (float)actualWidth, (minU + (float)width) / (float)actualWidth, (minV + 0.0f) / (float)actualHeight, (minV + (float)height) / (float)actualHeight);
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation textureResource, float posXStart, float posXEnd, float posYStart, float posYEnd, float zLevel, float minU, float maxU, float minV, float maxV) {
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)textureResource);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(matrix4f, posXStart, posYStart, zLevel).setUv(minU, minV);
        bufferbuilder.addVertex(matrix4f, posXStart, posYEnd, zLevel).setUv(minU, maxV);
        bufferbuilder.addVertex(matrix4f, posXEnd, posYEnd, zLevel).setUv(maxU, maxV);
        bufferbuilder.addVertex(matrix4f, posXEnd, posYStart, zLevel).setUv(maxU, minV);
        BufferUploader.drawWithShader((MeshData)bufferbuilder.buildOrThrow());
    }

    public static void blitMaskedProgress(GuiGraphics guiGraphics, ResourceLocation progressTexture, ResourceLocation maskTexture, float progress, float posX, float posY, int width, int height) {
        RenderUtil.blitMaskedProgress(guiGraphics, progressTexture, maskTexture, progress, posX, posY, width, height, false);
    }

    public static void blitMaskedProgress(GuiGraphics guiGraphics, ResourceLocation progressTexture, ResourceLocation maskTexture, float progress, float posX, float posY, int width, int height, boolean mirrorHorizontally) {
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)progressTexture);
        RenderSystem.setShaderTexture((int)1, (ResourceLocation)maskTexture);
        ShaderInstance shader = ClientSystem.getInstance().getHeatProgressShaderInstance();
        shader.setSampler("Sampler0", (Object)RenderSystem.getShaderTexture((int)0));
        shader.setSampler("Sampler1", (Object)RenderSystem.getShaderTexture((int)1));
        shader.getUniform("HeatProgress").set(Mth.clamp((float)progress, (float)0.0f, (float)1.0f));
        RenderSystem.setShader(() -> shader);
        Matrix4f matrix = guiGraphics.pose().last().pose();
        float minU = mirrorHorizontally ? 1.0f : 0.0f;
        float maxU = mirrorHorizontally ? 0.0f : 1.0f;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, posX, posY, -89.0f).setUv(minU, 0.0f);
        buffer.addVertex(matrix, posX, posY + (float)height, -89.0f).setUv(minU, 1.0f);
        buffer.addVertex(matrix, posX + (float)width, posY + (float)height, -89.0f).setUv(maxU, 1.0f);
        buffer.addVertex(matrix, posX + (float)width, posY, -89.0f).setUv(maxU, 0.0f);
        BufferUploader.drawWithShader((MeshData)buffer.buildOrThrow());
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int startX, int endX, int startY, int endY, int zLevel, float minU, float maxU, float minV, float maxV, float colorR, float colorG, float colorB, float alpha) {
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)resourceLocation);
        RenderSystem.setShader(Config.customShadersEnabled ? ClientSystem.getInstance()::getTexColorShaderInstance : GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.addVertex(matrix4f, (float)startX, (float)startY, (float)zLevel).setUv(minU, minV).setColor(colorR, colorG, colorB, alpha);
        bufferBuilder.addVertex(matrix4f, (float)startX, (float)endY, (float)zLevel).setUv(minU, maxV).setColor(colorR, colorG, colorB, alpha);
        bufferBuilder.addVertex(matrix4f, (float)endX, (float)endY, (float)zLevel).setUv(maxU, maxV).setColor(colorR, colorG, colorB, alpha);
        bufferBuilder.addVertex(matrix4f, (float)endX, (float)startY, (float)zLevel).setUv(maxU, minV).setColor(colorR, colorG, colorB, alpha);
        BufferUploader.drawWithShader((MeshData)bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static Matrix4f getProjectionMatrixInverted() {
        Matrix4f projectionMatrixNew = RenderSystem.getProjectionMatrix();
        if (projectionMatrixNew == null || projectionMatrixNew != projectionMatrix) {
            projectionMatrix = projectionMatrixNew;
            projectionMatrixInverted = new Matrix4f((Matrix4fc)projectionMatrixNew).invert();
        }
        return projectionMatrixInverted;
    }

    public static Matrix4f getModelViewMatrixInverted() {
        Matrix4f modelViewMatrixNew = RenderSystem.getModelViewMatrix();
        if (modelViewMatrixNew == null || modelViewMatrixNew != modelViewMatrix) {
            modelViewMatrix = modelViewMatrixNew;
            modelViewMatrixInverted = new Matrix4f((Matrix4fc)modelViewMatrixNew).invert();
        }
        return modelViewMatrixInverted;
    }

    public static Matrix4f getProjectionMatrixNormalFov() {
        Minecraft mc = Minecraft.getInstance();
        return mc.gameRenderer.getProjectionMatrix(70.0);
    }

    public static void blitNineSliced(GuiGraphics guiGraphics, ResourceLocation p_282543_, int p_281513_, int p_281865_, int p_282482_, int p_282661_, int p_282068_, int p_281294_, int p_281681_, int p_281957_, int p_282300_, int p_282769_) {
        RenderUtil.blitNineSliced(guiGraphics, p_282543_, p_281513_, p_281865_, p_282482_, p_282661_, p_282068_, p_281294_, p_282068_, p_281294_, p_281681_, p_281957_, p_282300_, p_282769_);
    }

    public static void blitNineSliced(GuiGraphics guiGraphics, ResourceLocation p_282712_, int p_283509_, int p_283259_, int p_283273_, int p_282043_, int p_281430_, int p_281412_, int p_282566_, int p_281971_, int p_282879_, int p_281529_, int p_281924_, int p_281407_) {
        p_281430_ = Math.min(p_281430_, p_283273_ / 2);
        p_282566_ = Math.min(p_282566_, p_283273_ / 2);
        p_281412_ = Math.min(p_281412_, p_282043_ / 2);
        p_281971_ = Math.min(p_281971_, p_282043_ / 2);
        if (p_283273_ == p_282879_ && p_282043_ == p_281529_) {
            guiGraphics.blit(p_282712_, p_283509_, p_283259_, p_281924_, p_281407_, p_283273_, p_282043_);
        } else if (p_282043_ == p_281529_) {
            guiGraphics.blit(p_282712_, p_283509_, p_283259_, p_281924_, p_281407_, p_281430_, p_282043_);
            RenderUtil.blitRepeating(guiGraphics, p_282712_, p_283509_ + p_281430_, p_283259_, p_283273_ - p_282566_ - p_281430_, p_282043_, p_281924_ + p_281430_, p_281407_, p_282879_ - p_282566_ - p_281430_, p_281529_);
            guiGraphics.blit(p_282712_, p_283509_ + p_283273_ - p_282566_, p_283259_, p_281924_ + p_282879_ - p_282566_, p_281407_, p_282566_, p_282043_);
        } else if (p_283273_ == p_282879_) {
            guiGraphics.blit(p_282712_, p_283509_, p_283259_, p_281924_, p_281407_, p_283273_, p_281412_);
            RenderUtil.blitRepeating(guiGraphics, p_282712_, p_283509_, p_283259_ + p_281412_, p_283273_, p_282043_ - p_281971_ - p_281412_, p_281924_, p_281407_ + p_281412_, p_282879_, p_281529_ - p_281971_ - p_281412_);
            guiGraphics.blit(p_282712_, p_283509_, p_283259_ + p_282043_ - p_281971_, p_281924_, p_281407_ + p_281529_ - p_281971_, p_283273_, p_281971_);
        } else {
            guiGraphics.blit(p_282712_, p_283509_, p_283259_, p_281924_, p_281407_, p_281430_, p_281412_);
            RenderUtil.blitRepeating(guiGraphics, p_282712_, p_283509_ + p_281430_, p_283259_, p_283273_ - p_282566_ - p_281430_, p_281412_, p_281924_ + p_281430_, p_281407_, p_282879_ - p_282566_ - p_281430_, p_281412_);
            guiGraphics.blit(p_282712_, p_283509_ + p_283273_ - p_282566_, p_283259_, p_281924_ + p_282879_ - p_282566_, p_281407_, p_282566_, p_281412_);
            guiGraphics.blit(p_282712_, p_283509_, p_283259_ + p_282043_ - p_281971_, p_281924_, p_281407_ + p_281529_ - p_281971_, p_281430_, p_281971_);
            RenderUtil.blitRepeating(guiGraphics, p_282712_, p_283509_ + p_281430_, p_283259_ + p_282043_ - p_281971_, p_283273_ - p_282566_ - p_281430_, p_281971_, p_281924_ + p_281430_, p_281407_ + p_281529_ - p_281971_, p_282879_ - p_282566_ - p_281430_, p_281971_);
            guiGraphics.blit(p_282712_, p_283509_ + p_283273_ - p_282566_, p_283259_ + p_282043_ - p_281971_, p_281924_ + p_282879_ - p_282566_, p_281407_ + p_281529_ - p_281971_, p_282566_, p_281971_);
            RenderUtil.blitRepeating(guiGraphics, p_282712_, p_283509_, p_283259_ + p_281412_, p_281430_, p_282043_ - p_281971_ - p_281412_, p_281924_, p_281407_ + p_281412_, p_281430_, p_281529_ - p_281971_ - p_281412_);
            RenderUtil.blitRepeating(guiGraphics, p_282712_, p_283509_ + p_281430_, p_283259_ + p_281412_, p_283273_ - p_282566_ - p_281430_, p_282043_ - p_281971_ - p_281412_, p_281924_ + p_281430_, p_281407_ + p_281412_, p_282879_ - p_282566_ - p_281430_, p_281529_ - p_281971_ - p_281412_);
            RenderUtil.blitRepeating(guiGraphics, p_282712_, p_283509_ + p_283273_ - p_282566_, p_283259_ + p_281412_, p_281430_, p_282043_ - p_281971_ - p_281412_, p_281924_ + p_282879_ - p_282566_, p_281407_ + p_281412_, p_282566_, p_281529_ - p_281971_ - p_281412_);
        }
    }

    public static void blitRepeating(GuiGraphics guiGraphics, ResourceLocation p_283059_, int p_283575_, int p_283192_, int p_281790_, int p_283642_, int p_282691_, int p_281912_, int p_281728_, int p_282324_) {
        RenderUtil.blitRepeating(guiGraphics, p_283059_, p_283575_, p_283192_, p_281790_, p_283642_, p_282691_, p_281912_, p_281728_, p_282324_, 256, 256);
    }

    public static void blitRepeating(GuiGraphics guiGraphics, ResourceLocation p_283059_, int p_283575_, int p_283192_, int p_281790_, int p_283642_, int p_282691_, int p_281912_, int p_281728_, int p_282324_, int textureWidth, int textureHeight) {
        int i = p_283575_;
        IntIterator intiterator = RenderUtil.slices(p_281790_, p_281728_);
        while (intiterator.hasNext()) {
            int j = intiterator.nextInt();
            int k = (p_281728_ - j) / 2;
            int l = p_283192_;
            IntIterator intiterator1 = RenderUtil.slices(p_283642_, p_282324_);
            while (intiterator1.hasNext()) {
                int i1 = intiterator1.nextInt();
                int j1 = (p_282324_ - i1) / 2;
                guiGraphics.blit(p_283059_, i, l, (float)(p_282691_ + k), (float)(p_281912_ + j1), j, i1, textureWidth, textureHeight);
                l += i1;
            }
            i += j;
        }
    }

    private static IntIterator slices(int p_282197_, int p_282161_) {
        int i = Mth.positiveCeilDiv((int)p_282197_, (int)p_282161_);
        return new Divisor(p_282197_, i);
    }

    public static void renderTooltipBackground(GuiGraphics p_282666_, int p_281901_, int p_281846_, int p_281559_, int p_283336_, int p_283422_, int backgroundTop, int backgroundBottom, int borderTop, int borderBottom) {
        int i = p_281901_ - 3;
        int j = p_281846_ - 3;
        int k = p_281559_ + 3 + 3;
        int l = p_283336_ + 3 + 3;
        RenderUtil.renderHorizontalLine(p_282666_, i, j - 1, k, p_283422_, backgroundTop);
        RenderUtil.renderHorizontalLine(p_282666_, i, j + l, k, p_283422_, backgroundBottom);
        RenderUtil.renderRectangleGradient(p_282666_, i, j, k, l, p_283422_, backgroundTop, backgroundBottom);
        RenderUtil.renderVerticalLineGradient(p_282666_, i - 1, j, l, p_283422_, backgroundTop, backgroundBottom);
        RenderUtil.renderVerticalLineGradient(p_282666_, i + k, j, l, p_283422_, backgroundTop, backgroundBottom);
        RenderUtil.renderFrameGradient(p_282666_, i, j + 1, k, l, p_283422_, borderTop, borderBottom);
    }

    public static void renderHorizontalLine(GuiGraphics guiGraphics, int p_282028_, int p_282141_, int p_281771_, int p_282734_, int p_281979_) {
        guiGraphics.fill(p_282028_, p_282141_, p_282028_ + p_281771_, p_282141_ + 1, p_282734_, p_281979_);
    }

    public static void renderVerticalLineGradient(GuiGraphics guiGraphics, int $$1, int $$2, int $$3, int $$4, int $$5, int $$6) {
        guiGraphics.fillGradient($$1, $$2, $$1 + 1, $$2 + $$3, $$4, $$5, $$6);
    }

    public static void renderFrameGradient(GuiGraphics guiGraphics, int $$1, int $$2, int $$3, int $$4, int $$5, int $$6, int $$7) {
        RenderUtil.renderVerticalLineGradient(guiGraphics, $$1, $$2, $$4 - 2, $$5, $$6, $$7);
        RenderUtil.renderVerticalLineGradient(guiGraphics, $$1 + $$3 - 1, $$2, $$4 - 2, $$5, $$6, $$7);
        RenderUtil.renderHorizontalLine(guiGraphics, $$1, $$2 - 1, $$3, $$5, $$6);
        RenderUtil.renderHorizontalLine(guiGraphics, $$1, $$2 - 1 + $$4 - 1, $$3, $$5, $$7);
    }

    private static void renderRectangleGradient(GuiGraphics guiGraphics, int p_282294_, int p_283353_, int p_282640_, int p_281964_, int p_283211_, int p_282349_, int colorTo) {
        guiGraphics.fillGradient(p_282294_, p_283353_, p_282294_ + p_282640_, p_283353_ + p_281964_, p_283211_, p_282349_, colorTo);
    }
}

