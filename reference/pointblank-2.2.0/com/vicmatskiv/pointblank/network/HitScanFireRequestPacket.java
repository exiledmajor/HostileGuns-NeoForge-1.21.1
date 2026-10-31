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

public class HitScanFireRequestPacket
extends GunStateRequestPacket {
    private FireModeInstance fireModeInstance;
    private boolean isAiming;
    private boolean alternateMuzzle;
    private long seed;

    public HitScanFireRequestPacket() {
    }

    public HitScanFireRequestPacket(FireModeInstance fireModeInstance, UUID stateId, int slotIndex, boolean isAiming, boolean alternateMuzzle, long seed) {
        super(stateId, slotIndex);
        this.fireModeInstance = fireModeInstance;
        this.isAiming = isAiming;
        this.alternateMuzzle = alternateMuzzle;
        this.seed = seed;
    }

    public static HitScanFireRequestPacket decode(FriendlyByteBuf buffer) {
        GunStateRequestPacket header = GunStateRequestPacket.decodeHeader(buffer);
        FireModeInstance fireModeInstance = FireModeInstance.readFromBuf(buffer);
        boolean isAiming = buffer.readBoolean();
        boolean alternateMuzzle = buffer.readBoolean();
        long seed = buffer.readLong();
        return new HitScanFireRequestPacket(fireModeInstance, header.stateId, header.slotIndex, isAiming, alternateMuzzle, seed);
    }

    @Override
    protected void doEncode(FriendlyByteBuf buffer) {
        this.fireModeInstance.writeToBuf(buffer);
        buffer.writeBoolean(this.isAiming);
        buffer.writeBoolean(this.alternateMuzzle);
        buffer.writeLong(this.seed);
    }

    @Override
    protected <T extends GunStateRequestPacket> void handleEnqueued(NetworkService.MessageContext ctx) {
        ServerPlayer player = (ServerPlayer)ctx.getSender();
        if (player != null) {
            ItemStack itemStack = GunItem.resolveServerActionStack(player, this.slotIndex, this.stateId);
            if (itemStack != null && itemStack.getItem() instanceof GunItem) {
                ((GunItem)itemStack.getItem()).handleClientHitScanFireRequest(player, this.fireModeInstance, this.stateId, this.slotIndex, this.correlationId, this.isAiming, this.alternateMuzzle, this.seed);
            } else {
                System.err.println("Mismatching item in slot " + this.slotIndex);
            }
        }
    }
}

