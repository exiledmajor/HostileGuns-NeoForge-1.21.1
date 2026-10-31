/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DestFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SourceFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.resources.ResourceLocation
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vicmatskiv.pointblank.client.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class CrosshairRenderer {
    public static void renderCrosshairOverlay3(GuiGraphics guiGraphics, float partialTick, ResourceLocation targetLockonOverlay, float expansionRatio, float posX, float posY, int renderWidth, int renderHeight) {
        RenderSystem.enableBlend();
        float halfLockWidth = 8.0f;
        float halfLockHeight = 8.0f;
        float centerX = posX + (float)renderWidth * 0.5f;
        float centerY = posY + (float)renderHeight * 0.5f;
        float xOffset = halfLockWidth * 0.4f * expansionRatio;
        float yOffset = halfLockHeight * 0.4f * expansionRatio;
        float ratio = 0.17948718f;
        float uvOffset = (1.0f - ratio) * 0.5f;
        float sq = 30.0f;
        float innerSide = sq * ratio;
        float halfSq = sq * 0.5f;
        float halfInnerSide = innerSide * 0.5f;
        float hairLength = (sq - innerSide) * 0.5f;
        float hairWidth = 0.5f;
        float minU = uvOffset;
        float maxU = 1.0f - minU;
        float minV = uvOffset;
        float maxV = 1.0f - minV;
        float posXStart = centerX - halfInnerSide;
        float posXEnd = centerX + halfInnerSide;
        float posYStart = centerY - halfInnerSide;
        float posYEnd = centerY + halfInnerSide;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart, posXEnd, posYStart, posYEnd, -90.0f, minU, maxU, minV, maxV);
        RenderSystem.blendFuncSeparate((GlStateManager.SourceFactor)GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, (GlStateManager.SourceFactor)GlStateManager.SourceFactor.ONE, (GlStateManager.DestFactor)GlStateManager.DestFactor.ZERO);
        minU = 0.0f;
        maxU = uvOffset;
        minV = 0.0f;
        maxV = 1.0f - minV;
        posXStart = centerX - halfInnerSide - hairLength - xOffset;
        posXEnd = centerX - halfInnerSide + hairWidth - xOffset;
        posYStart = centerY - halfSq;
        posYEnd = centerY + halfSq;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart, posXEnd, posYStart, posYEnd, -90.0f, minU, maxU, minV, maxV);
        minU = 1.0f - uvOffset;
        maxU = 1.0f;
        minV = 0.0f;
        maxV = 1.0f - minV;
        posXStart = centerX + halfInnerSide - hairWidth + xOffset;
        posXEnd = centerX + halfInnerSide + hairLength - hairWidth + xOffset;
        posYStart = centerY - halfSq;
        posYEnd = centerY + halfSq;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart, posXEnd, posYStart, posYEnd, -90.0f, minU, maxU, minV, maxV);
        minU = 0.0f;
        maxU = 1.0f;
        minV = 1.0f - uvOffset;
        maxV = 1.0f;
        posXStart = centerX - halfSq;
        posXEnd = centerX + halfSq;
        posYStart = centerY + halfInnerSide - hairWidth + yOffset;
        posYEnd = posYStart + hairLength - hairWidth;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart, posXEnd, posYStart, posYEnd, -90.0f, minU, maxU, minV, maxV);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderCrosshairOverlay(GuiGraphics guiGraphics, float partialTick, ResourceLocation targetLockonOverlay, float expansionRatio, float posX, float posY, int renderWidth, int renderHeight) {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.blendFuncSeparate((GlStateManager.SourceFactor)GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, (GlStateManager.SourceFactor)GlStateManager.SourceFactor.ONE, (GlStateManager.DestFactor)GlStateManager.DestFactor.ZERO);
        float lockRatio = 0.2f;
        int halfLockWidth = 4;
        int halfLockHeight = 4;
        float centerX = posX + (float)renderWidth * 0.5f;
        float centerY = posY + (float)renderHeight * 0.5f;
        float xOffset = (float)halfLockWidth * 0.4f * expansionRatio;
        float yOffset = (float)halfLockHeight * 0.4f * expansionRatio;
        float minU = 0.0f;
        float minV = 0.0f;
        float maxU = 1.0f;
        float maxV = 0.4f;
        float posXStart = centerX - (float)halfLockWidth;
        float posXEnd = centerX + (float)halfLockWidth;
        float posYStart = centerY - (float)halfLockHeight - yOffset;
        float posYEnd = centerY - yOffset;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart, posXEnd, posYStart, posYEnd, -90.0f, minU, maxU, minV, maxV);
        minU = 0.6f;
        minV = 0.0f;
        maxU = 1.0f;
        maxV = 1.0f;
        posXStart = centerX + xOffset;
        posXEnd = centerX + (float)halfLockWidth + xOffset;
        posYStart = centerY - (float)halfLockHeight;
        posYEnd = centerY + (float)halfLockHeight;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart, posXEnd, posYStart, posYEnd, -90.0f, minU, maxU, minV, maxV);
        minU = 0.0f;
        minV = 0.0f;
        maxU = 1.0f;
        maxV = 0.4f;
        posXStart = centerX - (float)halfLockWidth;
        posXEnd = centerX + (float)halfLockWidth;
        posYStart = centerY + yOffset;
        posYEnd = centerY + (float)halfLockHeight + yOffset;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart, posXEnd, posYStart, posYEnd, -90.0f, minU, maxU, minV, maxV);
        minU = 0.0f;
        minV = 0.0f;
        maxU = 0.4f;
        maxV = 1.0f;
        posXStart = centerX - (float)halfLockWidth - xOffset;
        posXEnd = centerX - xOffset;
        posYStart = centerY - (float)halfLockHeight;
        posYEnd = centerY + (float)halfLockHeight;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart, posXEnd, posYStart, posYEnd, -90.0f, minU, maxU, minV, maxV);
    }

    private void renderCrosshairOverlay2(GuiGraphics guiGraphics, float partialTick, ResourceLocation targetLockonOverlay, float expansionRatio, float posX, float posY, int renderWidth, int renderHeight) {
        expansionRatio += 0.2f;
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.blendFuncSeparate((GlStateManager.SourceFactor)GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, (GlStateManager.SourceFactor)GlStateManager.SourceFactor.ONE, (GlStateManager.DestFactor)GlStateManager.DestFactor.ZERO);
        float halfLockWidth = 5.0f;
        float halfLockHeight = 5.0f;
        float centerX = posX + (float)renderWidth * 0.5f;
        float centerY = posY + (float)renderHeight * 0.5f;
        float xOffset = (float)((int)halfLockWidth) * 0.4f * expansionRatio;
        float yOffset = (float)((int)halfLockHeight) * 0.4f * expansionRatio;
        float minU = 0.0f;
        float minV = 0.0f;
        float maxU = 0.5f;
        float maxV = 0.5f;
        float posXStart = centerX - halfLockWidth - xOffset;
        float posXEnd = centerX - xOffset;
        float posYStart = centerY - halfLockHeight - yOffset;
        float posYEnd = centerY - yOffset;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart, posXEnd, posYStart, posYEnd, -90.0f, minU, maxU, minV, maxV);
        float posXStart2 = centerX + xOffset;
        float posXEnd2 = centerX + halfLockWidth + xOffset;
        float posYStart2 = centerY - halfLockHeight - yOffset;
        float posYEnd2 = centerY - yOffset;
        float minU2 = 0.5f;
        float minV2 = 0.0f;
        float maxU2 = 1.0f;
        float maxV2 = 0.5f;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart2, posXEnd2, posYStart2, posYEnd2, -90.0f, minU2, maxU2, minV2, maxV2);
        posXStart2 = centerX + xOffset;
        posXEnd2 = centerX + halfLockWidth + xOffset;
        posYStart2 = centerY + yOffset;
        posYEnd2 = centerY + halfLockHeight + yOffset;
        minU2 = 0.5f;
        minV2 = 0.5f;
        maxU2 = 1.0f;
        maxV2 = 1.0f;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart2, posXEnd2, posYStart2, posYEnd2, -90.0f, minU2, maxU2, minV2, maxV2);
        posXStart2 = centerX - halfLockWidth - xOffset;
        posXEnd2 = centerX - xOffset;
        posYStart2 = centerY + yOffset;
        posYEnd2 = centerY + halfLockHeight + yOffset;
        minU2 = 0.0f;
        minV2 = 0.5f;
        maxU2 = 0.5f;
        maxV2 = 1.0f;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart2, posXEnd2, posYStart2, posYEnd2, -90.0f, minU2, maxU2, minV2, maxV2);
    }
}

