/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package com.vicmatskiv.pointblank.entity;

import net.minecraft.network.FriendlyByteBuf;

public interface ExtraSpawnDataHolder {
    public void writeSpawnData(FriendlyByteBuf var1);

    public void readSpawnData(FriendlyByteBuf var1);
}

