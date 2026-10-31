/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.network.GunStateRequestPacket;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class StopFireRequestPacket
extends GunStateRequestPacket {
    public StopFireRequestPacket() {
    }

    public StopFireRequestPacket(UUID stateId, int slotIndex) {
        super(stateId, slotIndex);
    }

    public static StopFireRequestPacket decode(FriendlyByteBuf buffer) {
        GunStateRequestPacket header = GunStateRequestPacket.decodeHeader(buffer);
        return new StopFireRequestPacket(header.stateId, header.slotIndex);
    }

    @Override
    protected <T extends GunStateRequestPacket> void handleEnqueued(NetworkService.MessageContext ctx) {
        ServerPlayer player = (ServerPlayer)ctx.getSender();
        if (player != null) {
            ItemStack itemStack = GunItem.resolveServerActionStack(player, this.slotIndex, this.stateId);
            if (itemStack != null && itemStack.getItem() instanceof GunItem) {
                ((GunItem)itemStack.getItem()).handleClientStopFireRequest(player, this.stateId, this.slotIndex, this.correlationId);
            } else {
                System.err.println("Mismatching item in slot " + this.slotIndex);
            }
        }
    }
}

