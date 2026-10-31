/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 */
package com.vicmatskiv.pointblank.block.entity;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

public interface PrinterBlockEntityExt {
    public void onDataPacket(Connection var1, ClientboundBlockEntityDataPacket var2, HolderLookup.Provider var3);
}

