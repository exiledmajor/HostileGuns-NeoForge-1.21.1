/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.network.GunStateResponsePacket;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class StopFireResponsePacket
extends GunStateResponsePacket {
    public StopFireResponsePacket() {
    }

    public StopFireResponsePacket(UUID stateId, int slotIndex, int correlationId, boolean isSuccess) {
        super(stateId, slotIndex, correlationId, isSuccess);
    }

    @Override
    protected void doEncode(FriendlyByteBuf buffer) {
    }

    public static StopFireResponsePacket decode(FriendlyByteBuf buffer) {
        GunStateResponsePacket header = GunStateResponsePacket.decodeHeader(buffer);
        return new StopFireResponsePacket(header.stateId, header.slotIndex, header.correlationId, header.isSuccess);
    }

    @Override
    protected <T extends GunStateResponsePacket> void handleEnqueued(NetworkService.MessageContext ctx, ItemStack itemStack, GunClientState gunClientState) {
        ((GunItem)itemStack.getItem()).processStopServerFireResponse(this.stateId, this.correlationId, this.isSuccess, itemStack, gunClientState);
    }
}

