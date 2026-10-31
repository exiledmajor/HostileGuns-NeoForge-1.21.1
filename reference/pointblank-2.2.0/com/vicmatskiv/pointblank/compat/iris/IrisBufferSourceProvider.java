/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 */
package com.vicmatskiv.pointblank.compat.iris;

import com.vicmatskiv.pointblank.client.render.BufferSourceProvider;
import com.vicmatskiv.pointblank.compat.iris.WrappedBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public class IrisBufferSourceProvider
implements BufferSourceProvider {
    private static final IrisBufferSourceProvider INSTANCE = new IrisBufferSourceProvider();

    public static IrisBufferSourceProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public MultiBufferSource getBufferSource() {
        MultiBufferSource.BufferSource delegate = Minecraft.getInstance().renderBuffers().bufferSource();
        return new WrappedBufferSource((MultiBufferSource)delegate);
    }
}

