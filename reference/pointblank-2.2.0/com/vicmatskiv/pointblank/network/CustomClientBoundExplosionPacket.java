/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.network;

import com.google.common.collect.Lists;
import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.client.ClientEventHandler;
import com.vicmatskiv.pointblank.explosion.CustomExplosion;
import com.vicmatskiv.pointblank.util.ClientUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class CustomClientBoundExplosionPacket {
    private Item item;
    private double x;
    private double y;
    private double z;
    private float power;
    private List<BlockPos> toBlow;
    private float knockbackX;
    private float knockbackY;
    private float knockbackZ;

    public CustomClientBoundExplosionPacket(Item item, double posX, double posY, double posZ, float damage, List<BlockPos> toBlow, Vec3 knockback) {
        this.item = item;
        this.x = posX;
        this.y = posY;
        this.z = posZ;
        this.power = damage;
        this.toBlow = Lists.newArrayList(toBlow);
        if (knockback != null) {
            this.knockbackX = (float)knockback.x;
            this.knockbackY = (float)knockback.y;
            this.knockbackZ = (float)knockback.z;
        } else {
            this.knockbackX = 0.0f;
            this.knockbackY = 0.0f;
            this.knockbackZ = 0.0f;
        }
    }

    public CustomClientBoundExplosionPacket() {
    }

    public static CustomClientBoundExplosionPacket decode(FriendlyByteBuf buf) {
        CustomClientBoundExplosionPacket packet = new CustomClientBoundExplosionPacket();
        Item item = (Item)buf.readById(id -> (Item)BuiltInRegistries.ITEM.byId(id));
        if (item != Items.AIR) {
            packet.item = item;
        }
        packet.x = buf.readDouble();
        packet.y = buf.readDouble();
        packet.z = buf.readDouble();
        packet.power = buf.readFloat();
        int i = Mth.floor((double)packet.x);
        int j = Mth.floor((double)packet.y);
        int k = Mth.floor((double)packet.z);
        packet.toBlow = buf.readList(p_178850_ -> {
            int l = p_178850_.readByte() + i;
            int i1 = p_178850_.readByte() + j;
            int j1 = p_178850_.readByte() + k;
            return new BlockPos(l, i1, j1);
        });
        packet.knockbackX = buf.readFloat();
        packet.knockbackY = buf.readFloat();
        packet.knockbackZ = buf.readFloat();
        return packet;
    }

    public static void encode(CustomClientBoundExplosionPacket packet, FriendlyByteBuf buf) {
        buf.writeById(item -> BuiltInRegistries.ITEM.getId(item), (Object)packet.item);
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
        buf.writeFloat(packet.power);
        int i = Mth.floor((double)packet.x);
        int j = Mth.floor((double)packet.y);
        int k = Mth.floor((double)packet.z);
        buf.writeCollection(packet.toBlow, (p_178855_, p_178856_) -> {
            int l = p_178856_.getX() - i;
            int i1 = p_178856_.getY() - j;
            int j1 = p_178856_.getZ() - k;
            p_178855_.writeByte(l);
            p_178855_.writeByte(i1);
            p_178855_.writeByte(j1);
        });
        buf.writeFloat(packet.knockbackX);
        buf.writeFloat(packet.knockbackY);
        buf.writeFloat(packet.knockbackZ);
    }

    public static void handle(CustomClientBoundExplosionPacket packet, NetworkService.MessageContext ctx) {
        ctx.enqueueWork(() -> ClientEventHandler.runSyncTick(() -> CustomClientBoundExplosionPacket.handleClient(packet, ctx)));
        ctx.setPacketHandled(true);
    }

    private static void handleClient(CustomClientBoundExplosionPacket packet, NetworkService.MessageContext ctx) {
        CustomExplosion explosion = new CustomExplosion(ClientUtil.getClientLevel(), packet.item, null, packet.x, packet.y, packet.z, packet.power, packet.toBlow);
        explosion.finalizeClientExplosion();
        Player player = ClientUtil.getClientPlayer();
        player.setDeltaMovement(player.getDeltaMovement().add((double)packet.knockbackX, (double)packet.knockbackY, (double)packet.knockbackZ));
    }

    public Item getItem() {
        return this.item;
    }

    public float getKnockbackX() {
        return this.knockbackX;
    }

    public float getKnockbackY() {
        return this.knockbackY;
    }

    public float getKnockbackZ() {
        return this.knockbackZ;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getPower() {
        return this.power;
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }
}

