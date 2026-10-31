/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.MultiBufferSource
 */
package com.vicmatskiv.pointblank.client.render;

import com.vicmatskiv.pointblank.client.render.DefaultBufferSourceProvider;
import com.vicmatskiv.pointblank.compat.iris.IrisCompat;
import net.minecraft.client.renderer.MultiBufferSource;

public interface BufferSourceProvider {
    public static BufferSourceProvider getInstance() {
        IrisCompat irisCompat = IrisCompat.getInstance();
        if (irisCompat.isShaderPackEnabled()) {
            return irisCompat.getBufferSourceProvider();
        }
        return DefaultBufferSourceProvider.getInstance();
    }

    public MultiBufferSource getBufferSource();
}

