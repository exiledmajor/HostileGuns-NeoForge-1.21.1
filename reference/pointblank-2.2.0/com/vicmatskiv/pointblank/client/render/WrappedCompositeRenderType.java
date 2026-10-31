/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.RenderType$CompositeRenderType
 */
package com.vicmatskiv.pointblank.client.render;

import com.vicmatskiv.pointblank.mixin.CompositeRenderTypeAccessor;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.renderer.RenderType;

public class WrappedCompositeRenderType
extends RenderType.CompositeRenderType {
    private static final AtomicInteger counter = new AtomicInteger();
    private final Runnable customSetup;
    private final Runnable customClear;

    public WrappedCompositeRenderType(RenderType.CompositeRenderType original, Runnable customSetup, Runnable customClear) {
        super("pointblank:wrapped:" + original.toString() + ":" + counter.incrementAndGet(), original.format(), original.mode(), original.bufferSize(), original.affectsCrumbling(), false, ((CompositeRenderTypeAccessor)original).getState());
        this.customSetup = customSetup;
        this.customClear = customClear;
    }

    public void setupRenderState() {
        super.setupRenderState();
        this.customSetup.run();
    }

    public void clearRenderState() {
        this.customClear.run();
        super.clearRenderState();
    }
}

