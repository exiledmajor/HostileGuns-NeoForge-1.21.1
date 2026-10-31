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
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.network.GunStateRequestPacket;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ReloadRequestPacket
extends GunStateRequestPacket {
    private FireModeInstance fireModeInstance;

    public ReloadRequestPacket() {
    }

    public ReloadRequestPacket(UUID stateId, int slotIndex, FireModeInstance fireModeInstance) {
        super(stateId, slotIndex);
        this.fireModeInstance = fireModeInstance;
    }

    @Override
    protected void doEncode(FriendlyByteBuf buffer) {
        this.fireModeInstance.writeToBuf(buffer);
    }

    public static ReloadRequestPacket decode(FriendlyByteBuf buffer) {
        GunStateRequestPacket header = GunStateRequestPacket.decodeHeader(buffer);
        FireModeInstance fireModeInstance = FireModeInstance.readFromBuf(buffer);
        return new ReloadRequestPacket(header.stateId, header.slotIndex, fireModeInstance);
    }

    @Override
    protected <T extends GunStateRequestPacket> void handleEnqueued(NetworkService.MessageContext ctx) {
        ServerPlayer player = (ServerPlayer)ctx.getSender();
        if (player != null) {
            ItemStack itemStack = GunItem.resolveServerActionStack(player, this.slotIndex, this.stateId);
            if (itemStack != null && itemStack.getItem() instanceof GunItem) {
                ((GunItem)itemStack.getItem()).handleClientReloadRequest(player, itemStack, this.stateId, this.slotIndex, this.fireModeInstance);
            } else {
                System.err.println("Mismatching item in slot " + this.slotIndex);
            }
        }
    }
}

