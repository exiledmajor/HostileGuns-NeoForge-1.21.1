/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 */
package com.vicmatskiv.pointblank.compat.iris;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.render.Flushable;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

class WrappedBufferSource
implements MultiBufferSource,
Flushable {
    private final MultiBufferSource delegate;

    WrappedBufferSource(MultiBufferSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public void flush() {
        MultiBufferSource multiBufferSource = this.delegate;
        if (multiBufferSource instanceof FullyBufferedMultiBufferSource) {
            FullyBufferedMultiBufferSource fbmbs = (FullyBufferedMultiBufferSource)multiBufferSource;
            fbmbs.endBatch();
        }
    }

    public VertexConsumer getBuffer(RenderType renderType) {
        if (renderType == RenderTypeProvider.NO_RENDER_TYPE) {
            return null;
        }
        return this.delegate.getBuffer(renderType);
    }
}

