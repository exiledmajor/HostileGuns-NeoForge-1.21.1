/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.client.ClientEventHandler;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.ClientUtil;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class MainHeldSimplifiedStateBroadcastPacket {
    protected Player owner;
    protected UUID stateId;
    protected GunClientState.FireState simplifiedState;

    public MainHeldSimplifiedStateBroadcastPacket() {
    }

    public MainHeldSimplifiedStateBroadcastPacket(Player owner, UUID stateId, GunClientState.FireState simplifiedState) {
        this.owner = owner;
        this.stateId = stateId;
        this.simplifiedState = simplifiedState;
    }

    public static <T extends MainHeldSimplifiedStateBroadcastPacket> void encode(T packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.owner.getId());
        buffer.writeUUID(packet.stateId);
        buffer.writeInt(packet.simplifiedState.ordinal());
    }

    public static MainHeldSimplifiedStateBroadcastPacket decode(FriendlyByteBuf buffer) {
        Player player;
        Entity effectOwnerEntity = ClientUtil.getClientLevel().getEntity(buffer.readInt());
        UUID stateId = buffer.readUUID();
        GunClientState.FireState fireState = GunClientState.FireState.values()[buffer.readInt()];
        return new MainHeldSimplifiedStateBroadcastPacket(effectOwnerEntity instanceof Player ? (player = (Player)effectOwnerEntity) : null, stateId, fireState);
    }

    public static <T extends MainHeldSimplifiedStateBroadcastPacket> void handle(T packet, NetworkService.MessageContext ctx) {
        ctx.enqueueWork(() -> ClientEventHandler.runSyncTick(() -> {
            GunClientState otherPlayerClientState;
            if (packet.owner == null || packet.stateId == null || packet.owner == ClientUtil.getClientPlayer()) {
                return;
            }
            GunItem.OperableGunContext context = MainHeldSimplifiedStateBroadcastPacket.resolveHeldContext(packet.owner, packet.stateId);
            GunClientState gunClientState = otherPlayerClientState = context != null ? GunClientState.getState(packet.owner, context.itemStack(), context.slotIndex(), context.offhand()) : null;
            if (otherPlayerClientState != null) {
                otherPlayerClientState.setSimplifiedState(packet.simplifiedState);
            }
        }));
        ctx.setPacketHandled(true);
    }

    private static GunItem.OperableGunContext resolveHeldContext(Player player, UUID stateId) {
        GunItem.OperableGunContext result = null;
        GunItem.OperableGunContext main = GunItem.resolveMainHandGunContext(player);
        GunItem.OperableGunContext offhand = GunItem.resolveOffhandGunContext(player);
        if (main != null && stateId.equals(GunItem.getItemStackId(main.itemStack()))) {
            result = main;
        }
        if (offhand != null && stateId.equals(GunItem.getItemStackId(offhand.itemStack()))) {
            if (result != null) {
                return null;
            }
            result = offhand;
        }
        return result;
    }
}

