/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.Util
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.RenderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$EmptyTextureStateShard
 *  net.minecraft.client.renderer.RenderStateShard$ShaderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$TextureStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeRenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.ResourceProvider
 *  org.lwjgl.opengl.GL11
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vicmatskiv.pointblank.client.ClientSystem;
import com.vicmatskiv.pointblank.client.render.RenderTypeKey;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.compat.iris.IrisCompat;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.lwjgl.opengl.GL11;

public class DefaultRenderTypeProvider
extends RenderStateShard
implements RenderTypeProvider {
    private static DefaultRenderTypeProvider instance;
    private static Optional<ShaderInstance> posTexColorShader;
    private static Optional<ShaderInstance> auxShader;
    private Function<RenderTypeKey, RenderType> pipRenderTypes = Util.memoize(key -> DefaultRenderTypeProvider.createPipRenderType(key));
    private final Function<RenderTypeKey, RenderType> pipOverlayRenderTypes = Util.memoize(DefaultRenderTypeProvider::createPipOverlayRenderType);
    private final Function<RenderTypeKey, RenderType> pipShutterOverlayRenderTypes = Util.memoize(DefaultRenderTypeProvider::createPipShutterOverlayRenderType);
    private final Function<RenderTypeKey, RenderType> pipMaskRenderTypes = Util.memoize(DefaultRenderTypeProvider::createPipMaskRenderType);
    private final Function<ResourceLocation, RenderType> muzzleFlashRenderTypes = Util.memoize(DefaultRenderTypeProvider::createMuzzleFlashRenderType);
    private Function<ResourceLocation, RenderType> reticleRenderTypes = Util.memoize(texture -> DefaultRenderTypeProvider.createReticleRenderType(texture));
    private Function<ResourceLocation, RenderType> reticleRenderTypesWithParallax = Util.memoize(texture -> DefaultRenderTypeProvider.createReticleRenderType(texture, ClientSystem.getInstance()::getTexColorShaderInstance));
    private Function<ResourceLocation, RenderType> glowEntityRenderTypes = Util.memoize(texture -> GlowEntityRenderType.createRenderType(texture));
    private final Function<ResourceLocation, RenderType> glowRenderTypes = Util.memoize(DefaultRenderTypeProvider::createGlowRenderType);
    private final Function<RenderTypeKey, RenderType> defaultItemRenderTypes = Util.memoize(DefaultRenderTypeProvider::createDefaultItemRenderType);
    private static final PipTextureStateShard PIP_TEXTURE_STATE_SHARD;

    public static DefaultRenderTypeProvider getInstance() {
        if (instance == null) {
            instance = new DefaultRenderTypeProvider();
        }
        return instance;
    }

    public DefaultRenderTypeProvider() {
        super(null, null, null);
    }

    @Override
    public RenderType getDefaultItemRenderType(RenderTypeKey key) {
        return this.defaultItemRenderTypes.apply(key);
    }

    @Override
    public RenderType getPipRenderType(RenderTypeKey renderTypeKey) {
        return this.pipRenderTypes.apply(renderTypeKey);
    }

    @Override
    public RenderType getPipOverlayRenderType(RenderTypeKey renderTypeKey) {
        return this.pipOverlayRenderTypes.apply(renderTypeKey);
    }

    @Override
    public RenderType getPipShutterOverlayRenderType(RenderTypeKey renderTypeKey) {
        return this.pipShutterOverlayRenderTypes.apply(renderTypeKey);
    }

    @Override
    public RenderType getPipMaskRenderType(RenderTypeKey renderTypeKey) {
        return this.pipMaskRenderTypes.apply(renderTypeKey);
    }

    @Override
    public RenderType getGlowRenderType(ResourceLocation texture) {
        return this.glowRenderTypes.apply(texture);
    }

    @Override
    public RenderType getMuzzleFlashRenderType(ResourceLocation texture) {
        return this.muzzleFlashRenderTypes.apply(texture);
    }

    @Override
    public RenderType getReticleRenderType(ResourceLocation texture, boolean isParallaxEnabled) {
        return isParallaxEnabled ? this.reticleRenderTypesWithParallax.apply(texture) : this.reticleRenderTypes.apply(texture);
    }

    @Override
    public RenderType getGlowBlockEntityRenderType(ResourceLocation texture) {
        if (IrisCompat.getInstance().isIrisLoaded()) {
            return RenderType.entityTranslucent((ResourceLocation)texture);
        }
        return this.glowEntityRenderTypes.apply(texture);
    }

    private static ShaderInstance getPosTexColorShader() {
        if (posTexColorShader != null) {
            return posTexColorShader.orElse(null);
        }
        String shaderName = "pointblank_position_tex_color";
        try {
            Minecraft mc = Minecraft.getInstance();
            posTexColorShader = Optional.of(new ShaderInstance((ResourceProvider)mc.getResourceManager(), shaderName, DefaultVertexFormat.POSITION_TEX_COLOR));
        }
        catch (Exception e) {
            e.printStackTrace();
            posTexColorShader = Optional.empty();
        }
        return posTexColorShader.get();
    }

    private static ShaderInstance getAuxShader() {
        if (auxShader != null) {
            return auxShader.orElse(null);
        }
        String shaderName = "pointblank_aux";
        try {
            Minecraft mc = Minecraft.getInstance();
            auxShader = Optional.of(new ShaderInstance((ResourceProvider)mc.getResourceManager(), shaderName, DefaultVertexFormat.POSITION_TEX_COLOR));
        }
        catch (Exception e) {
            e.printStackTrace();
            auxShader = Optional.empty();
        }
        return auxShader.get();
    }

    private static RenderType createDefaultItemRenderType(RenderTypeKey key) {
        Consumer<RenderTypeKey> setup = key.isStencilEnabled() ? SETUP_STENCIL_RENDER : k -> {};
        Consumer<RenderTypeKey> clear = key.isStencilEnabled() ? CLEAR_STENCIL_RENDER : k -> {};
        return RenderTypeProvider.wrapAsCompositeRenderType(RenderType.entityCutoutNoCull((ResourceLocation)key.texture()), () -> setup.accept(key), () -> clear.accept(key));
    }

    private static RenderType createPipRenderType(RenderTypeKey key) {
        Consumer<RenderTypeKey> setup = key.isStencilEnabled() ? SETUP_STENCIL_RENDER : k -> {};
        Consumer<RenderTypeKey> clear = key.isStencilEnabled() ? CLEAR_STENCIL_RENDER : k -> {};
        return RenderTypeProvider.wrapAsCompositeRenderType((RenderType)RenderType.create((String)("pointblank:pip_" + String.valueOf(key)), (VertexFormat)DefaultVertexFormat.POSITION_TEX_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(DefaultRenderTypeProvider::getAuxShader)).setTextureState((RenderStateShard.EmptyTextureStateShard)PIP_TEXTURE_STATE_SHARD).setTransparencyState(NO_TRANSPARENCY).setDepthTestState(LEQUAL_DEPTH_TEST).setLightmapState(NO_LIGHTMAP).setOverlayState(NO_OVERLAY).setCullState(NO_CULL).createCompositeState(true)), () -> setup.accept(key), () -> clear.accept(key));
    }

    private static RenderType createPipMaskRenderType(RenderTypeKey key) {
        Consumer<RenderTypeKey> setup = key.stencilFunc() == 519 && key.stencilOp() == 7681 ? RenderTypeProvider.SETUP_STENCIL_MASK_RENDER : RenderTypeProvider.SETUP_STENCIL_MASK_UPDATE_RENDER;
        return RenderTypeProvider.wrapAsCompositeRenderType((RenderType)RenderType.CompositeRenderType.create((String)("pointblank:pip_mask_" + String.valueOf(key)), (VertexFormat)DefaultVertexFormat.POSITION_TEX_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)0x200000, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(NO_TRANSPARENCY).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(key.texture(), false, false)).setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader)).createCompositeState(false)), () -> setup.accept(key), () -> RenderTypeProvider.CLEAR_STENCIL_MASK_RENDER.accept(key));
    }

    private static RenderType createPipOverlayRenderType(RenderTypeKey key) {
        Consumer<RenderTypeKey> setup = key.isStencilEnabled() ? SETUP_STENCIL_RENDER : k -> {};
        Consumer<RenderTypeKey> clear = key.isStencilEnabled() ? CLEAR_STENCIL_RENDER : k -> {};
        return RenderTypeProvider.wrapAsCompositeRenderType((RenderType)RenderType.create((String)("pointblank:pip_overlay_" + String.valueOf(key)), (VertexFormat)DefaultVertexFormat.POSITION_TEX_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setLightmapState(NO_LIGHTMAP).setOverlayState(NO_OVERLAY).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(key.texture(), false, false)).setShaderState(new RenderStateShard.ShaderStateShard(DefaultRenderTypeProvider::getPosTexColorShader)).createCompositeState(false)), () -> setup.accept(key), () -> clear.accept(key));
    }

    private static RenderType createPipShutterOverlayRenderType(RenderTypeKey key) {
        return RenderTypeProvider.wrapAsCompositeRenderType((RenderType)RenderType.CompositeRenderType.create((String)("pointblank:pip_shutter_" + String.valueOf(key)), (VertexFormat)DefaultVertexFormat.POSITION_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)0x200000, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(NO_TRANSPARENCY).setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader)).createCompositeState(false)), () -> RenderTypeProvider.SETUP_STENCIL_MASK_UPDATE_RENDER.accept(key), () -> RenderTypeProvider.CLEAR_STENCIL_MASK_RENDER.accept(key));
    }

    private static RenderType createMuzzleFlashRenderType(ResourceLocation texture) {
        return RenderType.create((String)"pointblank:muzzle_flash", (VertexFormat)DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(LIGHTNING_TRANSPARENCY).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(texture, false, false)).setShaderState(new RenderStateShard.ShaderStateShard(ClientSystem.getInstance()::getColorTexLightmapShaderInstance)).createCompositeState(false));
    }

    private static RenderType createReticleRenderType(ResourceLocation texture, Supplier<ShaderInstance> shaderSupplier) {
        return RenderType.create((String)"pointblank:reticle_parallax", (VertexFormat)DefaultVertexFormat.POSITION_TEX_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setLightmapState(NO_LIGHTMAP).setOverlayState(NO_OVERLAY).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(texture, false, false)).setShaderState(new RenderStateShard.ShaderStateShard(shaderSupplier::get)).createCompositeState(false));
    }

    private static RenderType createReticleRenderType(ResourceLocation reticleResource) {
        return RenderType.create((String)"pointblank:reticle", (VertexFormat)DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(reticleResource, false, false)).setShaderState(new RenderStateShard.ShaderStateShard(ClientSystem.getInstance()::getColorTexLightmapShaderInstance)).createCompositeState(false));
    }

    private static RenderType createGlowRenderType(ResourceLocation glowTexture) {
        return RenderTypeProvider.wrapAsCompositeRenderType((RenderType)RenderType.create((String)"pointblank:glow", (VertexFormat)DefaultVertexFormat.NEW_ENTITY, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setLightmapState(LIGHTMAP).setOverlayState(NO_OVERLAY).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(glowTexture, false, false)).setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER).createCompositeState(false)), () -> {
            GL11.glEnable((int)2960);
            RenderSystem.stencilOp((int)7680, (int)7680, (int)7680);
            RenderSystem.stencilFunc((int)517, (int)2, (int)255);
        }, () -> GL11.glDisable((int)2960));
    }

    static {
        PIP_TEXTURE_STATE_SHARD = new PipTextureStateShard();
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

    private static final class GlowEntityRenderType
    extends RenderType {
        public GlowEntityRenderType(String renderTypeName, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
            super(renderTypeName, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
        }

        private static RenderType createRenderType(ResourceLocation texture) {
            return RenderType.create((String)"pointblank:glowy_entity_block", (VertexFormat)DefaultVertexFormat.POSITION_TEX_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(LIGHTNING_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(texture, false, false)).setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader)).createCompositeState(false));
        }
    }
}

