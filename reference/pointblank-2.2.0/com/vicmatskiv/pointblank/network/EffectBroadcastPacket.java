/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.client.ClientEventHandler;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.GunStatePoseProvider;
import com.vicmatskiv.pointblank.client.PoseProvider;
import com.vicmatskiv.pointblank.client.PositionProvider;
import com.vicmatskiv.pointblank.client.VertexConsumers;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.registry.EffectRegistry;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.SimpleHitResult;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EffectBroadcastPacket {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    protected int playerEntityId;
    protected UUID gunStateId;
    protected UUID effectId;
    protected Vec3 startPosition;
    protected SimpleHitResult hitResult;
    protected boolean hasMuzzlePositionProvider;

    public EffectBroadcastPacket() {
    }

    public EffectBroadcastPacket(int playerEntityId, UUID gunStateId, UUID effectId, Vec3 startPosition, SimpleHitResult hitResult, boolean hasMuzzlePositionProvider) {
        this.playerEntityId = playerEntityId;
        this.gunStateId = gunStateId;
        this.effectId = effectId;
        this.startPosition = startPosition;
        this.hitResult = hitResult;
        this.hasMuzzlePositionProvider = hasMuzzlePositionProvider;
    }

    public static <T extends EffectBroadcastPacket> void encode(T packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.playerEntityId);
        buffer.writeLong(packet.gunStateId.getMostSignificantBits());
        buffer.writeLong(packet.gunStateId.getLeastSignificantBits());
        buffer.writeLong(packet.effectId.getMostSignificantBits());
        buffer.writeLong(packet.effectId.getLeastSignificantBits());
        buffer.writeDouble(packet.startPosition.x);
        buffer.writeDouble(packet.startPosition.y);
        buffer.writeDouble(packet.startPosition.z);
        buffer.writeOptional(Optional.ofNullable(packet.hitResult), SimpleHitResult.STREAM_CODEC);
        buffer.writeBoolean(packet.hasMuzzlePositionProvider);
        packet.doEncode(buffer);
    }

    protected void doEncode(FriendlyByteBuf buffer) {
    }

    protected static EffectBroadcastPacket decode(FriendlyByteBuf buffer) {
        int playerEntityId = buffer.readInt();
        UUID gunStateId = new UUID(buffer.readLong(), buffer.readLong());
        UUID effectId = new UUID(buffer.readLong(), buffer.readLong());
        Vec3 startPosition = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        SimpleHitResult hitResult = buffer.readOptional(SimpleHitResult.STREAM_CODEC).orElse(null);
        boolean hasMuzzlePositionProvider = buffer.readBoolean();
        return new EffectBroadcastPacket(playerEntityId, gunStateId, effectId, startPosition, hitResult, hasMuzzlePositionProvider);
    }

    public static <T extends EffectBroadcastPacket> void handle(T packet, NetworkService.MessageContext ctx) {
        ctx.enqueueWork(() -> ClientEventHandler.runSyncTick(() -> {
            if (Config.particleEffectsEnabled) {
                EffectBroadcastPacket.launchEffect(packet);
            }
        }));
        ctx.setPacketHandled(true);
    }

    private static <T extends EffectBroadcastPacket> void launchEffect(T packet) {
        Minecraft mc = Minecraft.getInstance();
        Player clientPlayer = ClientUtil.getClientPlayer();
        Entity effectOwnerEntity = mc.level.getEntity(packet.playerEntityId);
        if (effectOwnerEntity instanceof Player) {
            Supplier<EffectBuilder<EffectBuilder<?, ?>, ?>> effectBuilderSupplier;
            Player effectOwnerPlayer = (Player)effectOwnerEntity;
            if (effectOwnerPlayer == clientPlayer) {
                // empty if block
            }
            if ((effectBuilderSupplier = EffectRegistry.getEffectBuilderSupplier(packet.effectId)) != null) {
                EffectBuilder<EffectBuilder<?, ?>, ?> effectBuilder = effectBuilderSupplier.get();
                SimpleHitResult hitResult = packet.hitResult;
                double distanceToTarget = hitResult != null ? hitResult.getLocation().distanceTo(packet.startPosition) : 400.0;
                GunClientState gunState = GunClientState.getState(packet.gunStateId);
                PositionProvider positionProvider = null;
                PoseProvider poseProvider = null;
                if (packet.hasMuzzlePositionProvider && gunState != null) {
                    positionProvider = () -> GunStatePoseProvider.getInstance().getPositionAndDirection(gunState, GunStatePoseProvider.PoseContext.THIRD_PERSON_MUZZLE);
                    poseProvider = () -> GunStatePoseProvider.getInstance().getPose(gunState, GunStatePoseProvider.PoseContext.THIRD_PERSON_MUZZLE);
                }
                EffectBuilder.Context effectBuilderContext = new EffectBuilder.Context().withGunState(gunState).withStartPosition(packet.startPosition).withDistance((float)distanceToTarget).withRandomization(0.0f).withVertexConsumerTransformer(VertexConsumers.PARTICLE).withPositionProvider(positionProvider).withPoseProvider(poseProvider).withHitResult(packet.hitResult);
                Object effect = effectBuilder.build(effectBuilderContext);
                LOGGER.debug("Launching effect {}", effect);
                effect.launch((Entity)effectOwnerPlayer);
            }
        }
    }
}

