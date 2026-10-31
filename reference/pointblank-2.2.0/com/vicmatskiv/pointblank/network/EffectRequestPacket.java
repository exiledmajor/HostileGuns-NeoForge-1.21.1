/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.phys.Vec3
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.network.EffectBroadcastPacket;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.SimpleHitResult;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EffectRequestPacket {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static final int MAX_DISTANCE_SQR = 22500;
    protected int playerEntityId;
    protected UUID gunStateId;
    protected UUID effectId;
    protected Vec3 startPosition;
    protected SimpleHitResult hitResult;
    protected boolean hasMuzzlePositionProvider;

    public EffectRequestPacket() {
    }

    public EffectRequestPacket(int playerEntityId, UUID gunStateId, UUID effectId, Vec3 startPosition, SimpleHitResult hitResult, boolean hasMuzzlePositionProvider) {
        this.playerEntityId = playerEntityId;
        this.gunStateId = gunStateId;
        this.effectId = effectId;
        this.startPosition = startPosition;
        this.hitResult = hitResult;
        this.hasMuzzlePositionProvider = hasMuzzlePositionProvider;
    }

    public static <T extends EffectRequestPacket> void encode(T packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.playerEntityId);
        buffer.writeLong(packet.gunStateId.getMostSignificantBits());
        buffer.writeLong(packet.gunStateId.getLeastSignificantBits());
        buffer.writeLong(packet.effectId.getMostSignificantBits());
        buffer.writeLong(packet.effectId.getLeastSignificantBits());
        buffer.writeOptional(Optional.ofNullable(packet.startPosition), MiscUtil.VEC3_WRITER);
        buffer.writeOptional(Optional.ofNullable(packet.hitResult), SimpleHitResult.STREAM_CODEC);
        buffer.writeBoolean(packet.hasMuzzlePositionProvider);
        packet.doEncode(buffer);
    }

    protected void doEncode(FriendlyByteBuf buffer) {
    }

    protected static EffectRequestPacket decode(FriendlyByteBuf buffer) {
        int playerEntityId = buffer.readInt();
        UUID gunStateId = new UUID(buffer.readLong(), buffer.readLong());
        UUID effectId = new UUID(buffer.readLong(), buffer.readLong());
        Vec3 startPosition = buffer.readOptional(MiscUtil.VEC3_READER).orElse(null);
        SimpleHitResult hitResult = buffer.readOptional(SimpleHitResult.STREAM_CODEC).orElse(null);
        boolean hasMuzzlePositionProvider = buffer.readBoolean();
        return new EffectRequestPacket(playerEntityId, gunStateId, effectId, startPosition, hitResult, hasMuzzlePositionProvider);
    }

    public static <T extends EffectRequestPacket> void handle(T packet, NetworkService.MessageContext ctx) {
        ctx.enqueueWork(() -> packet.handleEnqueued(ctx));
        ctx.setPacketHandled(true);
    }

    protected <T extends EffectRequestPacket> void handleEnqueued(NetworkService.MessageContext ctx) {
        ServerPlayer sender = (ServerPlayer)ctx.getSender();
        if (sender == null || this.playerEntityId != sender.getId() || this.startPosition == null || this.gunStateId == null || !EffectRequestPacket.isHeldGunState(sender, this.gunStateId)) {
            return;
        }
        LOGGER.debug("Received effect request {} from {}", (Object)this, (Object)sender);
        for (ServerPlayer player : ((ServerLevel)MiscUtil.getLevel((Entity)sender)).getPlayers(p -> true)) {
            if (!(player.distanceToSqr(this.startPosition) < 22500.0)) continue;
            Platform.getInstance().getNetworkService().sendToClient(new EffectBroadcastPacket(this.playerEntityId, this.gunStateId, this.effectId, this.startPosition, this.hitResult, this.hasMuzzlePositionProvider), (Player)player);
        }
    }

    private static boolean isHeldGunState(ServerPlayer sender, UUID stateId) {
        GunItem gunItem;
        Item item;
        int matches = 0;
        if (sender.getMainHandItem().getItem() instanceof GunItem && stateId.equals(GunItem.getItemStackId(sender.getMainHandItem()))) {
            ++matches;
        }
        if ((item = sender.getOffhandItem().getItem()) instanceof GunItem && (gunItem = (GunItem)item).isDualWieldingEnabled(sender.getOffhandItem()) && stateId.equals(GunItem.getItemStackId(sender.getOffhandItem()))) {
            ++matches;
        }
        return matches == 1;
    }
}

