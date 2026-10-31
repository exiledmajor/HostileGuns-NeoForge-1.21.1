/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  org.spongepowered.asm.mixin.Mixin
 */
package com.vicmatskiv.pointblank.platform.neoforge.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.vicmatskiv.pointblank.client.render.RenderTargetExt;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={RenderTarget.class})
public class RenderTargetMixin
implements RenderTargetExt {
    @Override
    public boolean isPointblankStencilEnabled() {
        return ((RenderTarget)this).isStencilEnabled();
    }

    @Override
    public void enablePointblankStencil() {
        ((RenderTarget)this).enableStencil();
    }
}

