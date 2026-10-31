/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.client.uv;

import com.vicmatskiv.pointblank.client.uv.SpriteUVProvider;

public class StaticSpriteUVProvider
implements SpriteUVProvider {
    public static final StaticSpriteUVProvider INSTANCE = new StaticSpriteUVProvider();

    private StaticSpriteUVProvider() {
    }

    @Override
    public float[] getSpriteUV(float progress) {
        return SpriteUVProvider.getSpriteUV(0, 1, 1);
    }
}

