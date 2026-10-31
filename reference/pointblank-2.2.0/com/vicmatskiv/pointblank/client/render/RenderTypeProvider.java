/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeRenderType
 *  net.minecraft.resources.ResourceLocation
 *  org.lwjgl.opengl.GL11
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vicmatskiv.pointblank.client.render.DefaultRenderTypeProvider;
import com.vicmatskiv.pointblank.client.render.RenderTypeKey;
import com.vicmatskiv.pointblank.client.render.WrappedCompositeRenderType;
import com.vicmatskiv.pointblank.compat.iris.IrisCompat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public interface RenderTypeProvider {
    public static final RenderType NO_RENDER_TYPE = new RenderType(null, null, null, 0, false, false, null, null){};
    public static final AtomicInteger wrappedCounter = new AtomicInteger();
    public static final Consumer<RenderTypeKey> SETUP_STENCIL_MASK_RENDER = key -> {
        GL11.glEnable((int)2960);
        RenderSystem.clear((int)1024, (boolean)Minecraft.ON_OSX);
        RenderSystem.clearStencil((int)0);
        RenderSystem.stencilMask((int)255);
        RenderSystem.colorMask((boolean)false, (boolean)false, (boolean)false, (boolean)false);
        RenderSystem.depthMask((boolean)false);
        RenderSystem.stencilFunc((int)key.stencilFunc(), (int)key.stencilValue(), (int)255);
        RenderSystem.stencilOp((int)7680, (int)7680, (int)key.stencilOp());
    };
    public static final Consumer<RenderTypeKey> SETUP_STENCIL_MASK_UPDATE_RENDER = key -> {
        GL11.glEnable((int)2960);
        RenderSystem.stencilMask((int)255);
        RenderSystem.colorMask((boolean)false, (boolean)false, (boolean)false, (boolean)false);
        RenderSystem.depthMask((boolean)false);
        RenderSystem.stencilFunc((int)key.stencilFunc(), (int)key.stencilValue(), (int)255);
        RenderSystem.stencilOp((int)7680, (int)7680, (int)key.stencilOp());
    };
    public static final Consumer<RenderTypeKey> CLEAR_STENCIL_MASK_RENDER = key -> {
        RenderSystem.depthMask((boolean)true);
        RenderSystem.colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        RenderSystem.stencilMask((int)0);
        GL11.glDisable((int)2960);
    };
    public static final Consumer<RenderTypeKey> SETUP_STENCIL_RENDER = key -> {
        GL11.glEnable((int)2960);
        RenderSystem.stencilOp((int)7680, (int)7680, (int)key.stencilOp());
        GL11.glStencilFunc((int)key.stencilFunc(), (int)key.stencilValue(), (int)255);
    };
    public static final Consumer<RenderTypeKey> CLEAR_STENCIL_RENDER = key -> {
        GL11.glStencilMask((int)255);
        GL11.glDisable((int)2960);
    };

    public static RenderTypeProvider getInstance() {
        IrisCompat irisCompat = IrisCompat.getInstance();
        if (irisCompat.isShaderPackEnabled()) {
            return irisCompat.getRenderTypeProvider();
        }
        return DefaultRenderTypeProvider.getInstance();
    }

    public RenderType getDefaultItemRenderType(RenderTypeKey var1);

    public RenderType getPipRenderType(RenderTypeKey var1);

    public RenderType getPipOverlayRenderType(RenderTypeKey var1);

    public RenderType getPipMaskRenderType(RenderTypeKey var1);

    public RenderType getPipShutterOverlayRenderType(RenderTypeKey var1);

    public RenderType getGlowRenderType(ResourceLocation var1);

    public RenderType getMuzzleFlashRenderType(ResourceLocation var1);

    public RenderType getReticleRenderType(ResourceLocation var1, boolean var2);

    public RenderType getGlowBlockEntityRenderType(ResourceLocation var1);

    public static RenderType wrapAsBaseRenderType(RenderType renderType, Runnable setupRenderState, Runnable clearRenderState) {
        return new RenderType("pointblank:wrapped:" + String.valueOf(renderType) + ":" + wrappedCounter.incrementAndGet(), renderType.format(), renderType.mode(), renderType.bufferSize(), renderType.affectsCrumbling(), false, () -> {
            renderType.setupRenderState();
            setupRenderState.run();
        }, () -> {
            clearRenderState.run();
            renderType.clearRenderState();
        }){};
    }

    public static RenderType wrapAsCompositeRenderType(RenderType renderType, Runnable setupRenderState, Runnable clearRenderState) {
        if (!(renderType instanceof RenderType.CompositeRenderType)) {
            throw new IllegalArgumentException("wrapAsCompositeRenderType only supports CompositeRenderType, got: " + String.valueOf(renderType.getClass()));
        }
        RenderType.CompositeRenderType compositeRenderType = (RenderType.CompositeRenderType)renderType;
        return new WrappedCompositeRenderType(compositeRenderType, setupRenderState, clearRenderState);
    }

    default public MultiBufferSource wrapBufferSource(MultiBufferSource source) {
        return renderType -> {
            if (renderType == NO_RENDER_TYPE) {
                return null;
            }
            return source.getBuffer(renderType);
        };
    }

    default public float getReticleBrightness() {
        return 1.0f;
    }

    default public float getGlowBrightness() {
        return 1.0f;
    }

    public static enum Key {
        MUZZLE_FLASH,
        PIP,
        PIP_OVERLAY,
        RETICLE;

    }
}

