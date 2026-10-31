/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public class GunStateRequestPacket {
    protected UUID stateId;
    protected int slotIndex;
    protected int correlationId;

    public GunStateRequestPacket() {
    }

    public GunStateRequestPacket(UUID stateId, int slotIndex) {
        this.stateId = stateId;
        this.slotIndex = slotIndex;
    }

    public static <T extends GunStateRequestPacket> void encode(T packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.stateId.getMostSignificantBits());
        buffer.writeLong(packet.stateId.getLeastSignificantBits());
        buffer.writeInt(packet.slotIndex);
        packet.doEncode(buffer);
    }

    protected void doEncode(FriendlyByteBuf buffer) {
    }

    protected static GunStateRequestPacket decodeHeader(FriendlyByteBuf buffer) {
        UUID stateId = new UUID(buffer.readLong(), buffer.readLong());
        int slotIndex = buffer.readInt();
        return new GunStateRequestPacket(stateId, slotIndex);
    }

    public <T extends GunStateRequestPacket> void handle(NetworkService.MessageContext ctx) {
        this.handleEnqueued(ctx);
    }

    protected <T extends GunStateRequestPacket> void handleEnqueued(NetworkService.MessageContext ctx) {
    }
}

