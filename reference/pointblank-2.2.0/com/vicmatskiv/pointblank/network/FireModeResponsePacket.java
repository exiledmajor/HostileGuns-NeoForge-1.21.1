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
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.network.GunStateResponsePacket;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class FireModeResponsePacket
extends GunStateResponsePacket {
    private FireModeInstance fireModeInstance;

    public FireModeResponsePacket() {
    }

    public FireModeResponsePacket(UUID stateId, int slotIndex, int correlationId, boolean isSuccess, FireModeInstance fireModeInstance) {
        super(stateId, slotIndex, correlationId, isSuccess);
        this.fireModeInstance = fireModeInstance;
    }

    @Override
    protected void doEncode(FriendlyByteBuf buffer) {
        this.fireModeInstance.writeToBuf(buffer);
    }

    public static FireModeResponsePacket decode(FriendlyByteBuf buffer) {
        GunStateResponsePacket header = GunStateResponsePacket.decodeHeader(buffer);
        FireModeInstance fireModeInstance = FireModeInstance.readFromBuf(buffer);
        return new FireModeResponsePacket(header.stateId, header.slotIndex, header.correlationId, header.isSuccess, fireModeInstance);
    }

    @Override
    protected <T extends GunStateResponsePacket> void handleEnqueued(NetworkService.MessageContext ctx, ItemStack itemStack, GunClientState gunClientState) {
        ((GunItem)itemStack.getItem()).processServerFireModeResponse(this.stateId, this.correlationId, this.isSuccess, itemStack, gunClientState, this.fireModeInstance);
    }
}

