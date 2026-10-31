/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.Minecraft
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  software.bernie.geckolib.util.ClientUtil
 */
package com.vicmatskiv.pointblank.client.effect;

import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.GunStateListener;
import com.vicmatskiv.pointblank.client.GunStatePoseProvider;
import com.vicmatskiv.pointblank.client.PoseProvider;
import com.vicmatskiv.pointblank.client.PositionProvider;
import com.vicmatskiv.pointblank.client.VertexConsumers;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.feature.Features;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.network.EffectBroadcastPacket;
import com.vicmatskiv.pointblank.registry.EffectRegistry;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.SimpleHitResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.util.ClientUtil;

public class EffectLauncher
implements GunStateListener {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static final int MAX_DISTANCE_SQR = 22500;
    private Map<GunItem.FirePhase, List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>>> effectBuilders;

    public EffectLauncher(Map<GunItem.FirePhase, List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>>> effectBuilders) {
        this.effectBuilders = effectBuilders;
    }

    @Override
    public void onPrepareFiring(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
        this.applyPhaseEffects(GunItem.FirePhase.PREPARING, player, gunClientState, itemStack, null, 0.0f, false);
    }

    @Override
    public void onStartFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
        this.applyPhaseEffects(GunItem.FirePhase.FIRING, player, state, itemStack, null, 0.0f, false);
    }

    @Override
    public void onCompleteFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
        this.applyPhaseEffects(GunItem.FirePhase.COMPLETETING, player, state, itemStack, null, 0.0f, false);
    }

    @Override
    public void onHitScanTargetAcquired(LivingEntity player, GunClientState state, ItemStack itemStack, HitResult hitResult) {
        this.applyPhaseEffects(GunItem.FirePhase.HIT_SCAN_ACQUIRED, player, state, itemStack, hitResult, 0.0f, false);
    }

    @Override
    public void onHitScanTargetConfirmed(LivingEntity player, GunClientState state, ItemStack itemStack, HitResult hitResult, float damage) {
        this.applyPhaseEffects(GunItem.FirePhase.HIT_TARGET, player, state, itemStack, hitResult, damage, false);
    }

    private static List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> getGlobalEffectBuilders(GunItem.FirePhase phase, LivingEntity player, GunClientState gunClientState, ItemStack itemStack, HitResult hitResult, boolean thirdPersonOnly) {
        Entity entity;
        SimpleHitResult simpleHitResult;
        int entityId;
        if (phase == GunItem.FirePhase.HIT_TARGET && hitResult.getType() == HitResult.Type.ENTITY && hitResult instanceof SimpleHitResult && (entityId = (simpleHitResult = (SimpleHitResult)hitResult).getEntityId()) > 0 && (entity = ClientUtil.getLevel().getEntity(entityId)) instanceof LivingEntity) {
            return EffectRegistry.getEntityHitEffects(entity);
        }
        return Collections.emptyList();
    }

    private void applyPhaseEffects(GunItem.FirePhase phase, LivingEntity player, GunClientState gunClientState, ItemStack itemStack, HitResult hitResult, float damage, boolean thirdPersonOnly) {
        if (!Config.particleEffectsEnabled) {
            return;
        }
        Item item = itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return;
        }
        List mainPhaseEffectBuilders = this.effectBuilders.computeIfAbsent(phase, k -> new ArrayList());
        List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> featurePhaseEffectBuilders = Features.getEnabledPhaseEffects(itemStack, phase);
        ArrayList phaseEffectBuilders = new ArrayList(mainPhaseEffectBuilders);
        phaseEffectBuilders.addAll(featurePhaseEffectBuilders);
        phaseEffectBuilders.addAll(EffectLauncher.getGlobalEffectBuilders(phase, player, gunClientState, itemStack, hitResult, thirdPersonOnly));
        if (phaseEffectBuilders.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        float maxDistance = Math.min((float)(mc.options.getEffectiveRenderDistance() * 16), 200.0f);
        if (mc.options.getCameraType() != CameraType.FIRST_PERSON) {
            thirdPersonOnly = true;
        }
        float distanceToTarget = 0.0f;
        distanceToTarget = hitResult != null ? Mth.clamp((float)((float)Math.sqrt(hitResult.distanceTo((Entity)player)) - 0.5f), (float)0.0f, (float)maxDistance) : maxDistance;
        GunStatePoseProvider gunStatePoseProvider = GunStatePoseProvider.getInstance();
        boolean isAlternatingMuzzle = gunClientState.getSelectedAlternatingMuzzleFlashBone() != null;
        GunStatePoseProvider.PoseContext firstPersonMuzzleFlashContext = gunClientState.isAlternateMuzzleSelected() ? GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE_FLASH_ALT : GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE_FLASH;
        GunStatePoseProvider.PoseContext thirdPersonMuzzleFlashContext = gunClientState.isAlternateMuzzleSelected() ? GunStatePoseProvider.PoseContext.THIRD_PERSON_MUZZLE_FLASH_ALT : GunStatePoseProvider.PoseContext.THIRD_PERSON_MUZZLE_FLASH;
        Vec3 startPosition = null;
        if (!thirdPersonOnly) {
            if (isAlternatingMuzzle) {
                startPosition = gunStatePoseProvider.getPosition(gunClientState, firstPersonMuzzleFlashContext);
            } else {
                startPosition = gunStatePoseProvider.getPosition(gunClientState, GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE);
                if (startPosition == null) {
                    startPosition = gunStatePoseProvider.getPosition(gunClientState, firstPersonMuzzleFlashContext);
                }
                if (startPosition == null) {
                    startPosition = gunStatePoseProvider.getPosition(gunClientState, GunStatePoseProvider.PoseContext.THIRD_PERSON_MUZZLE);
                }
            }
        }
        if (startPosition == null) {
            startPosition = gunStatePoseProvider.getPosition(gunClientState, thirdPersonMuzzleFlashContext);
        }
        if (startPosition == null && isAlternatingMuzzle) {
            startPosition = gunStatePoseProvider.getPosition(gunClientState, GunStatePoseProvider.PoseContext.THIRD_PERSON_MUZZLE);
        }
        for (Supplier supplier : phaseEffectBuilders) {
            EffectBuilder builder = (EffectBuilder)supplier.get();
            if (builder.getCompatiblePhases().contains((Object)phase)) {
                boolean isEffectAttached = builder.isEffectAttached();
                PoseProvider poseProvider = null;
                PositionProvider positionProvider = null;
                if (isEffectAttached) {
                    if (!thirdPersonOnly && gunStatePoseProvider.getPose(gunClientState, firstPersonMuzzleFlashContext) != null) {
                        poseProvider = () -> gunStatePoseProvider.getPose(gunClientState, firstPersonMuzzleFlashContext);
                    } else if (!thirdPersonOnly && gunStatePoseProvider.getPose(gunClientState, GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE) != null) {
                        poseProvider = () -> gunStatePoseProvider.getPose(gunClientState, GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE);
                    } else if (gunStatePoseProvider.getPose(gunClientState, thirdPersonMuzzleFlashContext) != null) {
                        poseProvider = () -> gunStatePoseProvider.getPose(gunClientState, thirdPersonMuzzleFlashContext);
                    } else if (gunStatePoseProvider.getPose(gunClientState, GunStatePoseProvider.PoseContext.THIRD_PERSON_MUZZLE) != null) {
                        poseProvider = () -> gunStatePoseProvider.getPose(gunClientState, GunStatePoseProvider.PoseContext.THIRD_PERSON_MUZZLE);
                    }
                    if (!thirdPersonOnly && gunStatePoseProvider.getPositionAndDirection(gunClientState, firstPersonMuzzleFlashContext) != null) {
                        positionProvider = () -> gunStatePoseProvider.getPositionAndDirection(gunClientState, firstPersonMuzzleFlashContext);
                    } else if (!thirdPersonOnly && gunStatePoseProvider.getPositionAndDirection(gunClientState, GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE) != null) {
                        positionProvider = () -> gunStatePoseProvider.getPositionAndDirection(gunClientState, GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE);
                    }
                } else {
                    if (!thirdPersonOnly && isAlternatingMuzzle && gunStatePoseProvider.getPose(gunClientState, firstPersonMuzzleFlashContext) != null) {
                        poseProvider = () -> gunStatePoseProvider.getPose(gunClientState, firstPersonMuzzleFlashContext);
                    } else if (!thirdPersonOnly && gunStatePoseProvider.getPose(gunClientState, GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE) != null) {
                        poseProvider = () -> gunStatePoseProvider.getPose(gunClientState, GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE);
                    } else if (!thirdPersonOnly && gunStatePoseProvider.getPose(gunClientState, firstPersonMuzzleFlashContext) != null) {
                        poseProvider = () -> gunStatePoseProvider.getPose(gunClientState, firstPersonMuzzleFlashContext);
                    }
                    if (!thirdPersonOnly && isAlternatingMuzzle && gunStatePoseProvider.getPositionAndDirection(gunClientState, firstPersonMuzzleFlashContext) != null) {
                        positionProvider = () -> gunStatePoseProvider.getPositionAndDirection(gunClientState, firstPersonMuzzleFlashContext);
                    } else if (!thirdPersonOnly && gunStatePoseProvider.getPositionAndDirection(gunClientState, GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE) != null) {
                        positionProvider = () -> gunStatePoseProvider.getPositionAndDirection(gunClientState, GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE);
                    } else if (!thirdPersonOnly && gunStatePoseProvider.getPositionAndDirection(gunClientState, firstPersonMuzzleFlashContext) != null) {
                        positionProvider = () -> gunStatePoseProvider.getPositionAndDirection(gunClientState, firstPersonMuzzleFlashContext);
                    }
                }
                EffectBuilder.Context effectBuilderContext = new EffectBuilder.Context().withGunState(gunClientState).withStartPosition(startPosition).withDistance(distanceToTarget).withRandomization(0.0f).withVertexConsumerTransformer(VertexConsumers.PARTICLE).withPoseProvider(poseProvider).withPositionProvider(positionProvider).withDamage(damage).withHitResult(hitResult);
                Object effect = builder.build(effectBuilderContext);
                LOGGER.debug("Launching effect {}", (Object)effect.getName());
                effect.launch((Entity)player);
                continue;
            }
            throw new IllegalStateException("Effect builder " + String.valueOf(builder) + " is not compatible with phase '" + String.valueOf((Object)phase) + "'. Check how you construct item: " + String.valueOf(item.getName(itemStack)));
        }
    }

    public static void broadcast(Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> effectSupplier, Player sourcePlayer, GunClientState state, LivingEntity targetEntity, SimpleHitResult hitResult) {
        Level level = MiscUtil.getLevel((Entity)sourcePlayer);
        if (level.isClientSide) {
            return;
        }
        Vec3 targetPos = targetEntity.getBoundingBox().getCenter();
        for (ServerPlayer nearbyPlayer : ((ServerLevel)level).getPlayers(p -> true)) {
            if (!(nearbyPlayer.distanceToSqr(targetPos) < 22500.0)) continue;
            Platform.getInstance().getNetworkService().sendToClient(new EffectBroadcastPacket(sourcePlayer.getId(), state.getId(), EffectRegistry.getEffectId(effectSupplier.get().getName()), targetPos, hitResult, false), (Player)nearbyPlayer);
        }
    }
}

