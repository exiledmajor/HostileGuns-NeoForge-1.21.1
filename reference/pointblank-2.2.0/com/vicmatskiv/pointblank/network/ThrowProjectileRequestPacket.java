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
import com.vicmatskiv.pointblank.item.ThrowableLike;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ThrowProjectileRequestPacket {
    protected UUID stateId;
    protected int slotIndex;

    public ThrowProjectileRequestPacket() {
    }

    public ThrowProjectileRequestPacket(UUID stateId, int slotIndex) {
        this.stateId = stateId;
        this.slotIndex = slotIndex;
    }

    public static <T extends ThrowProjectileRequestPacket> void encode(T packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.stateId.getMostSignificantBits());
        buffer.writeLong(packet.stateId.getLeastSignificantBits());
        buffer.writeInt(packet.slotIndex);
    }

    public <T extends ThrowProjectileRequestPacket> void handle(NetworkService.MessageContext ctx) {
        this.handleEnqueued(ctx);
    }

    public static ThrowProjectileRequestPacket decode(FriendlyByteBuf buffer) {
        UUID stateId = new UUID(buffer.readLong(), buffer.readLong());
        int slotIndex = buffer.readInt();
        return new ThrowProjectileRequestPacket(stateId, slotIndex);
    }

    protected <T extends ThrowProjectileRequestPacket> void handleEnqueued(NetworkService.MessageContext ctx) {
        ServerPlayer player = (ServerPlayer)ctx.getSender();
        if (player != null) {
            ItemStack itemStack = player.getInventory().getItem(this.slotIndex);
            if (itemStack != null && itemStack.getItem() instanceof ThrowableLike) {
                ((ThrowableLike)itemStack.getItem()).handleClientThrowRequest(player, this.stateId, this.slotIndex);
            } else {
                System.err.println("Mismatching item in slot " + this.slotIndex);
            }
        }
    }
}

