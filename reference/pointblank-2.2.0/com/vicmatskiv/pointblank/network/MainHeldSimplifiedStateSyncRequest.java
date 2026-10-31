/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.network.GunStateRequestPacket;
import com.vicmatskiv.pointblank.network.MainHeldSimplifiedStateBroadcastPacket;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class MainHeldSimplifiedStateSyncRequest
extends GunStateRequestPacket {
    private static final int MAX_STATE_SYNC_DISTANCE_SQR = 10000;
    private GunClientState.FireState simplifiedState;

    public MainHeldSimplifiedStateSyncRequest() {
    }

    public MainHeldSimplifiedStateSyncRequest(UUID stateId, GunClientState.FireState simplifiedState) {
        super(stateId, 0);
        this.simplifiedState = simplifiedState;
    }

    @Override
    protected void doEncode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.simplifiedState.ordinal());
    }

    public static MainHeldSimplifiedStateSyncRequest decode(FriendlyByteBuf buffer) {
        GunStateRequestPacket header = GunStateRequestPacket.decodeHeader(buffer);
        GunClientState.FireState state = GunClientState.FireState.values()[buffer.readInt()];
        return new MainHeldSimplifiedStateSyncRequest(header.stateId, state);
    }

    @Override
    protected <T extends GunStateRequestPacket> void handleEnqueued(NetworkService.MessageContext ctx) {
        ServerPlayer sender = (ServerPlayer)ctx.getSender();
        if (sender == null || this.stateId == null || this.simplifiedState == null || !MainHeldSimplifiedStateSyncRequest.isHeldGunState(sender, this.stateId)) {
            return;
        }
        for (ServerPlayer player : ((ServerLevel)MiscUtil.getLevel((Entity)sender)).getPlayers(p -> true)) {
            if (player == sender || !(player.distanceToSqr((Entity)sender) < 10000.0)) continue;
            Platform.getInstance().getNetworkService().sendToClient(new MainHeldSimplifiedStateBroadcastPacket((Player)sender, this.stateId, this.simplifiedState), (Player)player);
        }
    }

    private static boolean isHeldGunState(ServerPlayer sender, UUID stateId) {
        int matches = 0;
        if (sender.getMainHandItem().getItem() instanceof GunItem && stateId.equals(GunItem.getItemStackId(sender.getMainHandItem()))) {
            ++matches;
        }
        if (sender.getOffhandItem().getItem() instanceof GunItem && GunItem.canBePlacedInOffhand(sender.getOffhandItem()) && stateId.equals(GunItem.getItemStackId(sender.getOffhandItem()))) {
            ++matches;
        }
        return matches == 1;
    }
}

