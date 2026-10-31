/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 */
package com.vicmatskiv.pointblank.client.render;

import com.vicmatskiv.pointblank.client.render.BufferSourceProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public class DefaultBufferSourceProvider
implements BufferSourceProvider {
    private static final DefaultBufferSourceProvider INSTANCE = new DefaultBufferSourceProvider();

    public static DefaultBufferSourceProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public MultiBufferSource getBufferSource() {
        return Minecraft.getInstance().renderBuffers().bufferSource();
    }
}

