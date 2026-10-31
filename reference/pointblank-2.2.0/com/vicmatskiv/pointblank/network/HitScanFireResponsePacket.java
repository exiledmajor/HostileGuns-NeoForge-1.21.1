/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.util.Tuple
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.client.ClientEventHandler;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.network.GunStateResponsePacket;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.InventoryUtils;
import com.vicmatskiv.pointblank.util.SimpleHitResult;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HitScanFireResponsePacket
extends GunStateResponsePacket {
    private SimpleHitResult hitResult;
    private int ownerEntityId;
    private float damage;
    private boolean alternateMuzzle;

    public HitScanFireResponsePacket() {
    }

    public HitScanFireResponsePacket(int ownerEntityId, UUID stateId, int slotIndex, int correlationId, SimpleHitResult hitResult, float damage, boolean alternateMuzzle) {
        super(stateId, slotIndex, correlationId, true);
        this.ownerEntityId = ownerEntityId;
        this.hitResult = hitResult;
        this.damage = damage;
        this.alternateMuzzle = alternateMuzzle;
    }

    @Override
    protected void doEncode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.ownerEntityId);
        FriendlyByteBuf.writeNullable((ByteBuf)buffer, (Object)((Object)this.hitResult), SimpleHitResult.STREAM_CODEC);
        buffer.writeFloat(this.damage);
        buffer.writeBoolean(this.alternateMuzzle);
    }

    public static HitScanFireResponsePacket decode(FriendlyByteBuf buffer) {
        GunStateResponsePacket header = GunStateResponsePacket.decodeHeader(buffer);
        int ownerEntityId = buffer.readInt();
        SimpleHitResult hitResult = (SimpleHitResult)((Object)FriendlyByteBuf.readNullable((ByteBuf)buffer, SimpleHitResult.STREAM_CODEC));
        float damage = buffer.readFloat();
        boolean alternateMuzzle = buffer.readBoolean();
        return new HitScanFireResponsePacket(ownerEntityId, header.stateId, header.slotIndex, header.correlationId, hitResult, damage, alternateMuzzle);
    }

    public static void handle(HitScanFireResponsePacket packet, NetworkService.MessageContext ctx) {
        ctx.enqueueWork(() -> ClientEventHandler.runSyncTick(() -> {
            Player player;
            Tuple<ItemStack, GunClientState> targetTuple;
            Level level = ClientUtil.getClientLevel();
            Entity entity = level.getEntity(packet.ownerEntityId);
            if (entity instanceof Player && (targetTuple = packet.getItemStackAndState(packet, (Entity)(player = (Player)entity))) != null) {
                packet.handleEnqueued(player, (ItemStack)targetTuple.getA(), (GunClientState)targetTuple.getB());
            }
        }));
        ctx.setPacketHandled(true);
    }

    @Override
    protected <T extends GunStateResponsePacket> Tuple<ItemStack, GunClientState> getItemStackAndState(T packet, Entity entity) {
        Player clientPlayer = ClientUtil.getClientPlayer();
        if (entity instanceof Player) {
            Player player = (Player)entity;
            return InventoryUtils.getItemStackByStateId(player, packet.stateId, clientPlayer == player ? packet.slotIndex : 0);
        }
        return null;
    }

    protected <T extends GunStateResponsePacket> void handleEnqueued(Player player, ItemStack itemStack, GunClientState gunClientState) {
        Item item = itemStack.getItem();
        if (item instanceof GunItem) {
            GunItem gunItem = (GunItem)item;
            gunItem.processServerHitScanFireResponse(player, this.stateId, itemStack, gunClientState, this.hitResult, this.damage, this.alternateMuzzle);
        }
    }
}

