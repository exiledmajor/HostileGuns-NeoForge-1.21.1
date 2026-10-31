/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 */
package com.vicmatskiv.pointblank.client.uv;

import com.vicmatskiv.pointblank.client.uv.SpriteUVProvider;
import net.minecraft.util.Mth;

public class PlayOnceSpriteUVProvider
implements SpriteUVProvider {
    private int totalSprites;
    private double spriteFrameDurationMillis;
    private int rows;
    private int columns;
    private long lifetimeMillis;

    public PlayOnceSpriteUVProvider(int rows, int columns, int spritesPerSecond, long lifetimeMillis) {
        this.rows = rows;
        this.columns = columns;
        this.totalSprites = rows * columns;
        this.spriteFrameDurationMillis = 1000.0 / (double)spritesPerSecond;
        this.lifetimeMillis = lifetimeMillis;
    }

    private int getSpriteIndex(float progress) {
        double elapsedTimeMillis = progress * (float)this.lifetimeMillis;
        return Mth.clamp((int)((int)(elapsedTimeMillis / this.spriteFrameDurationMillis)), (int)0, (int)this.totalSprites);
    }

    @Override
    public float[] getSpriteUV(float progress) {
        int spriteIndex = this.getSpriteIndex(progress);
        if (spriteIndex >= this.totalSprites) {
            return null;
        }
        return SpriteUVProvider.getSpriteUV(spriteIndex, this.rows, this.columns);
    }
}

