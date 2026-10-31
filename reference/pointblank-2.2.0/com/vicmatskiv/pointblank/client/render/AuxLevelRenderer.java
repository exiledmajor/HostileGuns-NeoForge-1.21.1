/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.pipeline.TextureTarget
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.joml.Matrix4f
 *  org.lwjgl.opengl.GL11
 *  software.bernie.geckolib.cache.object.GeoQuad
 *  software.bernie.geckolib.cache.object.GeoVertex
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.client.render.RenderTargetExt;
import com.vicmatskiv.pointblank.compat.iris.IrisCompat;
import com.vicmatskiv.pointblank.feature.PipFeature;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.mixin.GameRendererAccessorMixin;
import com.vicmatskiv.pointblank.mixin.MinecraftAccessorMixin;
import com.vicmatskiv.pointblank.util.ClientUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

public class AuxLevelRenderer {
    private RenderTarget renderTarget;
    private int textureWidth;
    private int textureHeight;
    private long frameCount = 0L;
    private boolean isRendering;
    private boolean isRenderingPip;
    private double fov;
    private double cullFrustrumFov;
    private boolean isStencilEnabled;

    public AuxLevelRenderer(int textureWidth, int textureHeight) {
        this.renderTarget = new TextureTarget(textureWidth, textureHeight, true, Minecraft.ON_OSX);
        this.renderTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public RenderTarget getRenderTarget() {
        return this.renderTarget;
    }

    public boolean isRenderingPip() {
        return this.isRenderingPip;
    }

    public double getFov() {
        return this.fov;
    }

    public double getCullFrustrumFov() {
        return this.cullFrustrumFov;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderToTarget(DeltaTracker deltaTracker, float zoom) {
        IrisCompat irisCompat;
        ItemStack itemStack;
        Player player;
        Minecraft mc = Minecraft.getInstance();
        if (mc.noRender || mc.gameMode == null || mc.player == null) {
            return;
        }
        if (Config.isPipFallbackRequired() || this.frameCount % (long)Config.pipScopeRefreshFrame != 0L) {
            return;
        }
        this.isRenderingPip = true;
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        double d0 = ((GameRendererAccessorMixin)mc.gameRenderer).invokeGetFov(mc.gameRenderer.getMainCamera(), partialTick, true);
        this.fov = d0 * (double)(1.0f - zoom);
        this.cullFrustrumFov = 110.0;
        RenderTarget origTarget = mc.getMainRenderTarget();
        MinecraftAccessorMixin mm = (MinecraftAccessorMixin)mc;
        int originalWidth = mc.getWindow().getWidth();
        int originalHeight = mc.getWindow().getHeight();
        RenderTargetExt origTargetExt = (RenderTargetExt)origTarget;
        RenderTargetExt renderTargetExt = (RenderTargetExt)this.renderTarget;
        if (this.renderTarget.width != origTarget.width || this.renderTarget.height != origTarget.height || this.isStencilEnabled != origTargetExt.isPointblankStencilEnabled()) {
            this.renderTarget.resize(origTarget.width, origTarget.height, true);
            this.textureWidth = originalWidth;
            this.textureHeight = originalHeight;
            if (origTargetExt.isPointblankStencilEnabled()) {
                renderTargetExt.enablePointblankStencil();
            }
        }
        this.isStencilEnabled = origTargetExt.isPointblankStencilEnabled();
        mc.getMainRenderTarget().unbindWrite();
        mc.getMainRenderTarget().clear(false);
        mm.setMainRenderTarget(this.renderTarget);
        this.renderTarget.bindWrite(true);
        boolean bobViewEnabled = (Boolean)mc.options.bobView().get();
        try {
            if (bobViewEnabled) {
                mc.options.bobView().set((Object)false);
            }
            mc.gameRenderer.setPanoramicMode(true);
            mc.gameRenderer.setRenderBlockOutline(false);
            mc.gameRenderer.setRenderHand(false);
            RenderSystem.clear((int)0, (boolean)Minecraft.ON_OSX);
            this.renderTarget.clear(false);
            this.renderTarget.bindWrite(false);
            mc.gameRenderer.renderLevel(deltaTracker);
        }
        finally {
            if ((Boolean)mc.options.bobView().get() != bobViewEnabled) {
                mc.options.bobView().set((Object)bobViewEnabled);
            }
            mc.gameRenderer.setPanoramicMode(false);
            mc.gameRenderer.setRenderBlockOutline(true);
            mc.gameRenderer.setRenderHand(true);
            mc.getMainRenderTarget().unbindWrite();
            mm.setMainRenderTarget(origTarget);
            RenderSystem.clear((int)0, (boolean)Minecraft.ON_OSX);
            mc.getMainRenderTarget().clear(true);
            mc.getMainRenderTarget().bindWrite(true);
            this.isRenderingPip = false;
        }
        if (!origTargetExt.isPointblankStencilEnabled() && (player = ClientUtil.getClientPlayer()) != null && (itemStack = GunItem.getMainHeldGunItemStack((LivingEntity)player)) != null && PipFeature.getMaskTexture(itemStack) != null) {
            origTargetExt.enablePointblankStencil();
        }
        if ((irisCompat = IrisCompat.getInstance()).isIrisLoaded() && irisCompat.isShaderPackEnabled()) {
            GL11.glDepthMask((boolean)true);
            GL11.glClear((int)17664);
            if (ClientUtil.getClientLevel().dimension() != Level.NETHER) {
                GL11.glDepthMask((boolean)false);
            }
        }
    }

    public boolean isRendering() {
        return this.isRendering;
    }

    public void renderToBuffer(PoseStack poseStack, GeoQuad quad, VertexConsumer buffer, float red, float green, float blue, float alpha) {
        Matrix4f poseState = poseStack.last().pose();
        float[][] texUV = new float[][]{{1.0f, 1.0f}, {0.0f, 1.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}};
        for (int i = 0; i < 4; ++i) {
            GeoVertex v = quad.vertices()[i];
            buffer.addVertex(poseState, v.position().x, v.position().y, v.position().z).setUv(texUV[i][0], texUV[i][1]).setColor(red, green, blue, alpha);
        }
    }

    public void renderToBuffer(PoseStack poseStack, GeoQuad quad, VertexConsumer buffer, int packedLight) {
        int alpha;
        int blue;
        int green;
        int red;
        Matrix4f poseState = poseStack.last().pose();
        float aspectRatio = Mth.clamp((float)((float)this.textureHeight / (float)this.textureWidth), (float)0.0f, (float)1.0f);
        float arH = (1.0f - aspectRatio) * 0.5f;
        float minU = 0.0f + arH;
        float maxU = 1.0f - arH;
        float minV = 0.0f;
        float maxV = 1.0f;
        float[][] texUV = new float[][]{{maxU, maxV}, {minU, maxV}, {minU, minV}, {maxU, minV}};
        IrisCompat irisCompat = IrisCompat.getInstance();
        if (irisCompat.isShaderPackEnabled()) {
            int colorBalance = irisCompat.getColorBalance();
            red = colorBalance >> 24 & 0xFF;
            green = colorBalance >> 16 & 0xFF;
            blue = colorBalance >> 8 & 0xFF;
            alpha = colorBalance & 0xFF;
        } else {
            alpha = 255;
            blue = 255;
            green = 255;
            red = 255;
        }
        for (int i = 0; i < 4; ++i) {
            GeoVertex v = quad.vertices()[i];
            buffer.addVertex(poseState, v.position().x, v.position().y, v.position().z).setUv(texUV[i][0], texUV[i][1]).setColor(red, green, blue, alpha);
        }
    }
}

