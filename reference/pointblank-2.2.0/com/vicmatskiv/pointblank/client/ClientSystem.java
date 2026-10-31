/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ReloadableResourceManager
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.server.packs.resources.ResourceProvider
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Matrix4f
 */
package com.vicmatskiv.pointblank.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.client.ClientEventHandler;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.render.AuxLevelRenderer;
import com.vicmatskiv.pointblank.client.render.RenderTargetExt;
import com.vicmatskiv.pointblank.feature.PipFeature;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.ReloadableMemoize;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class ClientSystem {
    private static ClientSystem instance;
    private AuxLevelRenderer auxLevelRenderer;
    private ShaderInstance texColorShaderInstance;
    private ShaderInstance colorTexLightmapShaderInstance;
    private ShaderInstance heatProgressShaderInstance;
    private List<ResourceManagerReloadListener> resourceManagerReloadListeners;
    private Matrix4f inverseCameraMatrix;

    public static ClientSystem getInstance() {
        if (RenderSystem.isOnRenderThreadOrInit()) {
            if (instance == null) {
                instance = new ClientSystem();
            }
        } else {
            new IllegalStateException("getInstance() called from wrong thread");
        }
        return instance;
    }

    private ClientSystem() {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget mainRenderTarget = mc.getMainRenderTarget();
        this.auxLevelRenderer = new AuxLevelRenderer(mainRenderTarget.width, mainRenderTarget.height);
        String texColorShaderName = "pointblank_position_tex_color";
        try {
            this.texColorShaderInstance = new ShaderInstance((ResourceProvider)mc.getResourceManager(), texColorShaderName, DefaultVertexFormat.POSITION_TEX_COLOR);
        }
        catch (Exception exception) {
            throw new IllegalStateException("could not preload shader " + texColorShaderName, exception);
        }
        String colorTexLightmapShaderName = "pointblank_position_color_tex_lightmap";
        try {
            this.colorTexLightmapShaderInstance = new ShaderInstance((ResourceProvider)mc.getResourceManager(), colorTexLightmapShaderName, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        }
        catch (Exception exception) {
            throw new IllegalStateException("could not preload shader " + colorTexLightmapShaderName, exception);
        }
        String heatProgressShaderName = "pointblank_heat_progress";
        try {
            this.heatProgressShaderInstance = new ShaderInstance((ResourceProvider)mc.getResourceManager(), heatProgressShaderName, DefaultVertexFormat.POSITION_TEX);
        }
        catch (Exception exception) {
            throw new IllegalStateException("could not preload shader " + heatProgressShaderName, exception);
        }
        this.resourceManagerReloadListeners = new ArrayList<ResourceManagerReloadListener>();
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        if (resourceManager instanceof ReloadableResourceManager) {
            ReloadableResourceManager rrm = (ReloadableResourceManager)resourceManager;
            rrm.registerReloadListener((PreparableReloadListener)this.resourceManagerReloadListener());
        }
    }

    private ResourceManagerReloadListener resourceManagerReloadListener() {
        return this::onResourceManagerReload;
    }

    private void onResourceManagerReload(ResourceManager resourceManager) {
        for (ResourceManagerReloadListener reloadListener : this.resourceManagerReloadListeners) {
            reloadListener.onResourceManagerReload(resourceManager);
        }
    }

    public <T, U, R> BiFunction<T, U, R> createReloadableMemoize(BiFunction<T, U, R> f) {
        ReloadableMemoize<T, U, R> reloadable = new ReloadableMemoize<T, U, R>(f);
        this.resourceManagerReloadListeners.add(reloadable);
        return reloadable.getMemoizedFunction();
    }

    public AuxLevelRenderer getAuxLevelRenderer() {
        return this.auxLevelRenderer;
    }

    public ShaderInstance getTexColorShaderInstance() {
        if (Config.customShadersEnabled) {
            return this.texColorShaderInstance;
        }
        return GameRenderer.getPositionTexColorShader();
    }

    public ShaderInstance getColorTexLightmapShaderInstance() {
        if (Config.customShadersEnabled) {
            return this.colorTexLightmapShaderInstance;
        }
        return GameRenderer.getPositionColorTexLightmapShader();
    }

    public ShaderInstance getHeatProgressShaderInstance() {
        return this.heatProgressShaderInstance;
    }

    public void preRender(GunClientState state, DeltaTracker deltaTracker) {
        Optional<Float> pipZoom;
        Minecraft mc = Minecraft.getInstance();
        if (Config.isPipFallbackRequired()) {
            this.preparePipFallback(mc.player.getMainHandItem());
        }
        if ((pipZoom = PipFeature.getZoom(mc.player.getMainHandItem())).isPresent() && state.isAiming()) {
            if (mc.noRender || mc.gameMode == null || mc.player == null) {
                return;
            }
            if (!Config.isPipFallbackRequired() && Config.pipScopesEnabled) {
                this.renderAux(state, deltaTracker);
            }
        }
        this.clearStencil();
    }

    private void preparePipFallback(ItemStack itemStack) {
        Minecraft mc;
        RenderTargetExt origTargetExt;
        if (itemStack != null && PipFeature.getMaskTexture(itemStack) != null && !(origTargetExt = (RenderTargetExt)(mc = Minecraft.getInstance()).getMainRenderTarget()).isPointblankStencilEnabled()) {
            origTargetExt.enablePointblankStencil();
        }
    }

    public void renderAux(GunClientState state, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Optional<Float> pipZoom = PipFeature.getZoom(mc.player.getMainHandItem());
        if (pipZoom.isPresent() && state.isAiming()) {
            ClientSystem.getInstance().getAuxLevelRenderer().renderToTarget(deltaTracker, pipZoom.get().floatValue());
        }
    }

    public void clearStencil() {
        RenderSystem.clearStencil((int)0);
        RenderSystem.clear((int)1024, (boolean)Minecraft.ON_OSX);
    }

    public static double modifyMouseSensitivity(double originalValue) {
        Item item;
        GunClientState state = GunClientState.getMainHeldState();
        ItemStack mainHeldItem = ClientUtil.getClientPlayer().getMainHandItem();
        if (mainHeldItem != null && (item = mainHeldItem.getItem()) instanceof GunItem) {
            GunItem gunItem = (GunItem)item;
            if (state != null && state.isAiming() && (gunItem.getScopeOverlay() != null || MiscUtil.isGreaterThanZero(gunItem.getPipScopeZoom()) || ClientEventHandler.runSyncCompute(() -> !PipFeature.getZoom(mainHeldItem).isEmpty()).booleanValue())) {
                return originalValue * Config.scopeAimingMouseSensitivity;
            }
        }
        return originalValue;
    }

    public void setInverseCameraMatrix(Matrix4f inverseCameraMatrix) {
        this.inverseCameraMatrix = inverseCameraMatrix;
    }

    public Matrix4f getInverseCameraMatrix() {
        return this.inverseCameraMatrix;
    }
}

