/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.Unpooled
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.entity.ExtraSpawnDataHolder;
import com.vicmatskiv.pointblank.util.ClientUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ClientBoundAddSpawnEntityDataPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundAddSpawnEntityDataPacket> STREAM_CODEC = StreamCodec.ofMember(ClientBoundAddSpawnEntityDataPacket::encode, ClientBoundAddSpawnEntityDataPacket::decode);
    private final Entity entity;
    private final int typeId;
    private final int entityId;
    private final UUID uuid;
    private final double posX;
    private final double posY;
    private final double posZ;
    private final byte pitch;
    private final byte yaw;
    private final byte headYaw;
    private final int velX;
    private final int velY;
    private final int velZ;
    private final FriendlyByteBuf buf;

    public ClientBoundAddSpawnEntityDataPacket(Entity e) {
        this.entity = e;
        this.typeId = BuiltInRegistries.ENTITY_TYPE.getId((Object)e.getType());
        this.entityId = e.getId();
        this.uuid = e.getUUID();
        this.posX = e.getX();
        this.posY = e.getY();
        this.posZ = e.getZ();
        this.pitch = (byte)Mth.floor((float)(e.getXRot() * 256.0f / 360.0f));
        this.yaw = (byte)Mth.floor((float)(e.getYRot() * 256.0f / 360.0f));
        this.headYaw = (byte)(e.getYHeadRot() * 256.0f / 360.0f);
        Vec3 vec3d = e.getDeltaMovement();
        double d1 = Mth.clamp((double)vec3d.x, (double)-3.9, (double)3.9);
        double d2 = Mth.clamp((double)vec3d.y, (double)-3.9, (double)3.9);
        double d3 = Mth.clamp((double)vec3d.z, (double)-3.9, (double)3.9);
        this.velX = (int)(d1 * 8000.0);
        this.velY = (int)(d2 * 8000.0);
        this.velZ = (int)(d3 * 8000.0);
        this.buf = null;
    }

    private ClientBoundAddSpawnEntityDataPacket(int typeId, int entityId, UUID uuid, double posX, double posY, double posZ, byte pitch, byte yaw, byte headYaw, int velX, int velY, int velZ, FriendlyByteBuf buf) {
        this.entity = null;
        this.typeId = typeId;
        this.entityId = entityId;
        this.uuid = uuid;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.pitch = pitch;
        this.yaw = yaw;
        this.headYaw = headYaw;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
        this.buf = buf;
    }

    public static void encode(ClientBoundAddSpawnEntityDataPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.typeId);
        buf.writeInt(msg.entityId);
        buf.writeLong(msg.uuid.getMostSignificantBits());
        buf.writeLong(msg.uuid.getLeastSignificantBits());
        buf.writeDouble(msg.posX);
        buf.writeDouble(msg.posY);
        buf.writeDouble(msg.posZ);
        buf.writeByte(msg.pitch);
        buf.writeByte(msg.yaw);
        buf.writeByte(msg.headYaw);
        buf.writeShort(msg.velX);
        buf.writeShort(msg.velY);
        buf.writeShort(msg.velZ);
        Entity entity = msg.entity;
        if (entity instanceof ExtraSpawnDataHolder) {
            ExtraSpawnDataHolder extraSpawnDataHolder = (ExtraSpawnDataHolder)entity;
            FriendlyByteBuf spawnDataBuffer = new FriendlyByteBuf(Unpooled.buffer());
            extraSpawnDataHolder.writeSpawnData(spawnDataBuffer);
            buf.writeVarInt(spawnDataBuffer.readableBytes());
            buf.writeBytes((ByteBuf)spawnDataBuffer);
            spawnDataBuffer.release();
        } else {
            buf.writeVarInt(0);
        }
    }

    public static ClientBoundAddSpawnEntityDataPacket decode(FriendlyByteBuf buf) {
        return new ClientBoundAddSpawnEntityDataPacket(buf.readVarInt(), buf.readInt(), new UUID(buf.readLong(), buf.readLong()), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readByte(), buf.readByte(), buf.readByte(), buf.readShort(), buf.readShort(), buf.readShort(), ClientBoundAddSpawnEntityDataPacket.readSpawnDataPacket(buf));
    }

    private static FriendlyByteBuf readSpawnDataPacket(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count > 0) {
            FriendlyByteBuf spawnDataBuffer = new FriendlyByteBuf(Unpooled.buffer());
            spawnDataBuffer.writeBytes((ByteBuf)buf, count);
            return spawnDataBuffer;
        }
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void handle(ClientBoundAddSpawnEntityDataPacket msg, NetworkService.MessageContext ctx) {
        try {
            EntityType type = (EntityType)BuiltInRegistries.ENTITY_TYPE.byId(msg.typeId);
            Level clientLevel = ClientUtil.getClientLevel();
            Entity entity = type.create(clientLevel);
            if (entity == null) {
                return;
            }
            entity.syncPacketPositionCodec(msg.posX, msg.posY, msg.posZ);
            entity.absMoveTo(msg.posX, msg.posY, msg.posZ, (float)(msg.yaw * 360) / 256.0f, (float)(msg.pitch * 360) / 256.0f);
            entity.setYHeadRot((float)(msg.headYaw * 360) / 256.0f);
            entity.setYBodyRot((float)(msg.headYaw * 360) / 256.0f);
            entity.setId(msg.entityId);
            entity.setUUID(msg.uuid);
            ((ClientLevel)clientLevel).addEntity(entity);
            entity.lerpMotion((double)msg.velX / 8000.0, (double)msg.velY / 8000.0, (double)msg.velZ / 8000.0);
            if (entity instanceof ExtraSpawnDataHolder) {
                ExtraSpawnDataHolder extraSpawnDataHolder = (ExtraSpawnDataHolder)entity;
                extraSpawnDataHolder.readSpawnData(msg.buf);
            }
        }
        finally {
            msg.buf.release();
        }
    }

    public Entity getEntity() {
        return this.entity;
    }

    public int getTypeId() {
        return this.typeId;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public double getPosX() {
        return this.posX;
    }

    public double getPosY() {
        return this.posY;
    }

    public double getPosZ() {
        return this.posZ;
    }

    public byte getPitch() {
        return this.pitch;
    }

    public byte getYaw() {
        return this.yaw;
    }

    public byte getHeadYaw() {
        return this.headYaw;
    }

    public int getVelX() {
        return this.velX;
    }

    public int getVelY() {
        return this.velY;
    }

    public int getVelZ() {
        return this.velZ;
    }

    public FriendlyByteBuf getAdditionalData() {
        return this.buf;
    }
}

