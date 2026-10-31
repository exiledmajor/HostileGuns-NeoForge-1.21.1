/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.irisshaders.iris.Iris
 *  net.irisshaders.iris.pipeline.WorldRenderingPipeline
 *  net.minecraft.Util
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$EmptyTextureStateShard
 *  net.minecraft.client.renderer.RenderStateShard$ShaderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$TextureStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.ResourceManager
 *  org.lwjgl.opengl.GL11
 */
package com.vicmatskiv.pointblank.compat.iris;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vicmatskiv.pointblank.client.ClientSystem;
import com.vicmatskiv.pointblank.client.render.RenderTypeKey;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.compat.iris.IrisAuxShaderProvider;
import com.vicmatskiv.pointblank.compat.iris.WrappedBufferSource;
import java.io.FileNotFoundException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.opengl.GL11;

public class IrisRenderTypeProvider
extends RenderStateShard
implements RenderTypeProvider {
    private final Function<RenderTypeKey, RenderType> pipRenderTypes = Util.memoize(this::createPipRenderType);
    private final Function<RenderTypeKey, RenderType> pipMaskRenderTypes = Util.memoize(key -> this.createPipMaskRenderType((RenderTypeKey)key, this::getPipMaskShader));
    private final Function<RenderTypeKey, RenderType> pipOverlayRenderTypes = Util.memoize(key -> IrisRenderTypeProvider.createPipOverlayRenderType(key, this::getPipOverlayShader));
    private final Function<ResourceLocation, RenderType> reticleRenderTypes = Util.memoize(texture -> IrisRenderTypeProvider.createReticleRenderType(IrisRenderTypeProvider.getIrisTexture(texture)));
    private final Function<ResourceLocation, RenderType> reticleRenderTypesWithParallax = Util.memoize(texture -> IrisRenderTypeProvider.createReticleRenderType(IrisRenderTypeProvider.getIrisTexture(texture), GameRenderer::getPositionTexColorShader));
    private final Function<ResourceLocation, RenderType> glowRenderTypes = Util.memoize(texture -> IrisRenderTypeProvider.createGlowRenderType(IrisRenderTypeProvider.getIrisTexture(texture)));
    private final Function<RenderTypeKey, RenderType> defaultItemRenderTypes = Util.memoize(IrisRenderTypeProvider::createDefaultItemRenderType);
    private final Function<ResourceLocation, RenderType> muzzleFlashRenderTypes = Util.memoize(IrisRenderTypeProvider::createMuzzleFlashRenderType);
    private final Function<RenderTypeKey, RenderType> pipShutterOverlayRenderTypes = Util.memoize(this::createPipShutterOverlayRenderType);

    public IrisRenderTypeProvider() {
        super(null, null, null);
    }

    @Override
    public MultiBufferSource wrapBufferSource(MultiBufferSource source) {
        return new WrappedBufferSource(source);
    }

    @Override
    public RenderType getDefaultItemRenderType(RenderTypeKey key) {
        return this.defaultItemRenderTypes.apply(key);
    }

    @Override
    public RenderType getPipShutterOverlayRenderType(RenderTypeKey renderTypeKey) {
        return this.pipShutterOverlayRenderTypes.apply(renderTypeKey);
    }

    @Override
    public RenderType getPipRenderType(RenderTypeKey key) {
        return this.pipRenderTypes.apply(key);
    }

    private ShaderInstance getPipShader() {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline().orElse(null);
        ShaderInstance shader = null;
        if (pipeline instanceof IrisAuxShaderProvider) {
            IrisAuxShaderProvider auxShaderProvider = (IrisAuxShaderProvider)pipeline;
            shader = auxShaderProvider.getPointblankAuxShader();
        }
        if (shader == null) {
            shader = GameRenderer.getPositionTexColorShader();
        }
        return shader;
    }

    private ShaderInstance getPipOverlayShader() {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline().orElse(null);
        ShaderInstance shader = null;
        if (pipeline instanceof IrisAuxShaderProvider) {
            IrisAuxShaderProvider auxShaderProvider = (IrisAuxShaderProvider)pipeline;
            shader = auxShaderProvider.getPointblankAuxPlainShader();
        }
        if (shader == null) {
            shader = GameRenderer.getPositionTexColorShader();
        }
        return shader;
    }

    private ShaderInstance getPipMaskShader() {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline().orElse(null);
        ShaderInstance shader = null;
        if (pipeline instanceof IrisAuxShaderProvider) {
            IrisAuxShaderProvider auxShaderProvider = (IrisAuxShaderProvider)pipeline;
            shader = auxShaderProvider.getPointblankMaskShader();
        }
        if (shader == null) {
            shader = GameRenderer.getPositionTexColorShader();
        }
        return shader;
    }

    @Override
    public RenderType getPipOverlayRenderType(RenderTypeKey key) {
        return this.pipOverlayRenderTypes.apply(key);
    }

    @Override
    public RenderType getPipMaskRenderType(RenderTypeKey key) {
        return this.pipMaskRenderTypes.apply(key);
    }

    public RenderType getPipMaskInvertedRenderType(ResourceLocation texture) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RenderType getGlowRenderType(ResourceLocation texture) {
        return this.glowRenderTypes.apply(texture);
    }

    @Override
    public RenderType getGlowBlockEntityRenderType(ResourceLocation texture) {
        return RenderType.entityTranslucent((ResourceLocation)texture);
    }

    @Override
    public RenderType getMuzzleFlashRenderType(ResourceLocation texture) {
        return this.muzzleFlashRenderTypes.apply(texture);
    }

    @Override
    public RenderType getReticleRenderType(ResourceLocation texture, boolean isParallaxEnabled) {
        return isParallaxEnabled ? this.reticleRenderTypesWithParallax.apply(texture) : this.reticleRenderTypes.apply(texture);
    }

    private static ResourceLocation getIrisTexture(ResourceLocation originalTexture) {
        Minecraft mc = Minecraft.getInstance();
        ResourceManager resourceManager = mc.getResourceManager();
        String path = originalTexture.getPath();
        if (path.endsWith(".png")) {
            String modifiedPath = path.replace(".png", "_iris.png");
            ResourceLocation irisTexture = ResourceLocation.fromNamespaceAndPath((String)originalTexture.getNamespace(), (String)modifiedPath);
            try {
                resourceManager.getResourceOrThrow(irisTexture);
                return irisTexture;
            }
            catch (FileNotFoundException fileNotFoundException) {
                // empty catch block
            }
        }
        return originalTexture;
    }

    private static RenderType createDefaultItemRenderType(RenderTypeKey key) {
        RenderType baseRenderType = RenderType.entityCutoutNoCull((ResourceLocation)key.texture());
        Consumer<RenderTypeKey> setup = key.isStencilEnabled() ? SETUP_STENCIL_RENDER : k -> {};
        Consumer<RenderTypeKey> clear = key.isStencilEnabled() ? CLEAR_STENCIL_RENDER : k -> {};
        return RenderTypeProvider.wrapAsCompositeRenderType(baseRenderType, () -> setup.accept(key), () -> clear.accept(key));
    }

    private static RenderType createReticleRenderType(ResourceLocation texture, Supplier<ShaderInstance> shaderSupplier) {
        return RenderType.create((String)"pointblank:reticle_iris_with_parallax", (VertexFormat)DefaultVertexFormat.POSITION_TEX_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setLightmapState(NO_LIGHTMAP).setOverlayState(NO_OVERLAY).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(texture, false, false)).setShaderState(new RenderStateShard.ShaderStateShard(shaderSupplier::get)).createCompositeState(false));
    }

    private static RenderType createReticleRenderType(ResourceLocation texture) {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(texture, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).createCompositeState(true);
        return RenderType.create((String)"pointblank:reticle_iris", (VertexFormat)DefaultVertexFormat.POSITION_TEX_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)compositeState);
    }

    private RenderType createPipRenderType(RenderTypeKey key) {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(this::getPipShader)).setTextureState((RenderStateShard.EmptyTextureStateShard)new PipTextureStateShard()).setTransparencyState(NO_TRANSPARENCY).setLightmapState(NO_LIGHTMAP).setOverlayState(NO_OVERLAY).createCompositeState(true);
        Consumer<RenderTypeKey> setup = key.isStencilEnabled() ? SETUP_STENCIL_RENDER : k -> {};
        Consumer<RenderTypeKey> clear = key.isStencilEnabled() ? CLEAR_STENCIL_RENDER : k -> {};
        return RenderTypeProvider.wrapAsCompositeRenderType((RenderType)RenderType.create((String)("pointblank:pip_iris_" + String.valueOf(key)), (VertexFormat)DefaultVertexFormat.POSITION_TEX_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)compositeState), () -> setup.accept(key), () -> clear.accept(key));
    }

    private RenderType createPipShutterOverlayRenderType(RenderTypeKey key) {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader)).setTextureState((RenderStateShard.EmptyTextureStateShard)new PipTextureStateShard()).setTransparencyState(NO_TRANSPARENCY).setLightmapState(NO_LIGHTMAP).setOverlayState(NO_OVERLAY).createCompositeState(true);
        Consumer<RenderTypeKey> setup = key.isStencilEnabled() ? RenderTypeProvider.SETUP_STENCIL_MASK_UPDATE_RENDER : k -> {};
        Consumer<RenderTypeKey> clear = key.isStencilEnabled() ? RenderTypeProvider.CLEAR_STENCIL_MASK_RENDER : k -> {};
        return RenderTypeProvider.wrapAsCompositeRenderType((RenderType)RenderType.create((String)("pointblank:pip_shutter_iris_" + String.valueOf(key)), (VertexFormat)DefaultVertexFormat.POSITION_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)compositeState), () -> setup.accept(key), () -> clear.accept(key));
    }

    private RenderType createPipMaskRenderType(RenderTypeKey key, Supplier<ShaderInstance> shaderSupplier) {
        Consumer<RenderTypeKey> setup = key.stencilFunc() == 519 && key.stencilOp() == 7681 ? RenderTypeProvider.SETUP_STENCIL_MASK_RENDER : RenderTypeProvider.SETUP_STENCIL_MASK_UPDATE_RENDER;
        return RenderTypeProvider.wrapAsCompositeRenderType((RenderType)RenderType.create((String)("pointblank:pip_mask_iris" + String.valueOf(key)), (VertexFormat)DefaultVertexFormat.POSITION_TEX_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setDepthTestState(NO_DEPTH_TEST).setLightmapState(NO_LIGHTMAP).setOverlayState(NO_OVERLAY).setTransparencyState(NO_TRANSPARENCY).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(IrisRenderTypeProvider.getIrisTexture(key.texture()), false, false)).setShaderState(new RenderStateShard.ShaderStateShard(shaderSupplier::get)).createCompositeState(false)), () -> setup.accept(key), () -> RenderTypeProvider.CLEAR_STENCIL_MASK_RENDER.accept(key));
    }

    private static RenderType createPipOverlayRenderType(RenderTypeKey key, Supplier<ShaderInstance> shaderSupplier) {
        Consumer<RenderTypeKey> setup = key.isStencilEnabled() ? SETUP_STENCIL_RENDER : k -> {};
        Consumer<RenderTypeKey> clear = key.isStencilEnabled() ? CLEAR_STENCIL_RENDER : k -> {};
        return RenderTypeProvider.wrapAsCompositeRenderType((RenderType)RenderType.create((String)("pointblank:pip_overlay_iris_" + String.valueOf(key)), (VertexFormat)DefaultVertexFormat.POSITION_TEX_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(NO_TRANSPARENCY).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setLightmapState(NO_LIGHTMAP).setOverlayState(NO_OVERLAY).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(IrisRenderTypeProvider.getIrisTexture(key.texture()), false, false)).setShaderState(new RenderStateShard.ShaderStateShard(shaderSupplier::get)).createCompositeState(false)), () -> setup.accept(key), () -> clear.accept(key));
    }

    private static RenderType createGlowRenderType(ResourceLocation glowTexture) {
        return RenderTypeProvider.wrapAsCompositeRenderType((RenderType)RenderType.create((String)"pointblank:glow_iris", (VertexFormat)DefaultVertexFormat.NEW_ENTITY, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setLightmapState(LIGHTMAP).setOverlayState(NO_OVERLAY).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(glowTexture, false, false)).setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER).createCompositeState(false)), () -> {
            GL11.glEnable((int)2960);
            GL11.glStencilOp((int)7680, (int)7680, (int)7680);
            GL11.glStencilFunc((int)517, (int)2, (int)255);
            GL11.glStencilMask((int)0);
        }, () -> {
            GL11.glStencilMask((int)255);
            GL11.glDisable((int)2960);
        });
    }

    private static RenderType createMuzzleFlashRenderType(ResourceLocation texture) {
        return RenderTypeProvider.wrapAsCompositeRenderType((RenderType)RenderType.create((String)"pointblank:muzzle_flash_iris", (VertexFormat)DefaultVertexFormat.NEW_ENTITY, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(texture, false, false)).setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER).createCompositeState(false)), () -> {
            GL11.glEnable((int)2960);
            GL11.glStencilOp((int)7680, (int)7680, (int)7680);
            GL11.glStencilFunc((int)517, (int)2, (int)255);
            GL11.glStencilMask((int)0);
        }, () -> {
            GL11.glStencilMask((int)255);
            GL11.glDisable((int)2960);
        });
    }

    @Override
    public float getReticleBrightness() {
        return 0.6f;
    }

    @Override
    public float getGlowBrightness() {
        return 0.6f;
    }

    private static class PipTextureStateShard
    extends RenderStateShard.EmptyTextureStateShard {
        public PipTextureStateShard() {
            super(() -> {
                int textureId = ClientSystem.getInstance().getAuxLevelRenderer().getRenderTarget().getColorTextureId();
                RenderSystem.setShaderTexture((int)0, (int)textureId);
            }, () -> {});
        }
    }
}

