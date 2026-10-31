/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.client.uv;

public interface SpriteUVProvider {
    public float[] getSpriteUV(float var1);

    public static float[] getSpriteUV(int spriteIndex, int rows, int columns) {
        if (spriteIndex < 0 || spriteIndex >= rows * columns) {
            throw new IllegalArgumentException("Sprite index is out of bounds");
        }
        int spriteRow = spriteIndex / columns;
        int spriteColumn = spriteIndex % columns;
        float uMin = (float)spriteColumn / (float)columns;
        float vMin = (float)spriteRow / (float)rows;
        float uMax = (float)(spriteColumn + 1) / (float)columns;
        float vMax = (float)(spriteRow + 1) / (float)rows;
        return new float[]{uMin, vMin, uMax, vMax};
    }
}

