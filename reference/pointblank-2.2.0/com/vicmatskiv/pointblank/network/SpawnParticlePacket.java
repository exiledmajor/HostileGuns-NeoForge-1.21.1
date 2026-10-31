/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.level.Level
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.util.ClientUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

public class SpawnParticlePacket {
    private final ParticleType<?> particleType;
    private final double x;
    private final double y;
    private final double z;
    private final int count;

    public SpawnParticlePacket(ParticleType<?> particleType, double x, double y, double z, int count) {
        this.particleType = particleType;
        this.x = x;
        this.y = y;
        this.z = z;
        this.count = count;
    }

    public static void encode(SpawnParticlePacket packet, FriendlyByteBuf buf) {
        buf.writeResourceLocation(BuiltInRegistries.PARTICLE_TYPE.getKey(packet.particleType));
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
        buf.writeInt(packet.count);
    }

    public static SpawnParticlePacket decode(FriendlyByteBuf buf) {
        return new SpawnParticlePacket((ParticleType)BuiltInRegistries.PARTICLE_TYPE.get(buf.readResourceLocation()), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt());
    }

    public static void handle(SpawnParticlePacket packet, NetworkService.MessageContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = ClientUtil.getClientLevel();
            for (int i = 0; i < packet.count; ++i) {
                double offsetX = Math.random() * 0.5 - 0.25;
                double offsetY = Math.random() * 0.5 - 0.25;
                double offsetZ = Math.random() * 0.5 - 0.25;
                double x = packet.x + offsetX;
                double y = packet.y + offsetY;
                double z = packet.z + offsetZ;
                level.addParticle((ParticleOptions)ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
            }
        });
        ctx.setPacketHandled(true);
    }

    public ParticleType<?> getParticleType() {
        return this.particleType;
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

    public int getCount() {
        return this.count;
    }
}

