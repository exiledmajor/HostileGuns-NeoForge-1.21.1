/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.syncher.SynchedEntityData$Builder
 *  net.minecraft.server.level.ServerEntity
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.entity.projectile.ThrowableProjectile
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.entity;

import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.EntityRendererBuilder;
import com.vicmatskiv.pointblank.client.effect.AbstractEffect;
import com.vicmatskiv.pointblank.client.effect.AttachedProjectileEffect;
import com.vicmatskiv.pointblank.client.effect.Effect;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.TrailEffect;
import com.vicmatskiv.pointblank.entity.EntityBuilder;
import com.vicmatskiv.pointblank.entity.ExtraSpawnDataHolder;
import com.vicmatskiv.pointblank.entity.ProjectileLike;
import com.vicmatskiv.pointblank.item.EffectBuilderInfo;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.item.HurtingItem;
import com.vicmatskiv.pointblank.network.ClientBoundAddSpawnEntityDataPacket;
import com.vicmatskiv.pointblank.registry.EntityRegistry;
import com.vicmatskiv.pointblank.util.HitScan;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.TimeUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class GenericThrowableProjectile
extends ThrowableProjectile
implements ProjectileLike,
ExtraSpawnDataHolder {
    private static final float DEFAULT_GRAVITY = 0.05f;
    private static final float DEFAULT_WIDTH = 0.25f;
    private static final float DEFAULT_HEIGHT = 0.25f;
    private static final int DEFAULT_CLIENT_TRACKING_RANGE = 1024;
    private static final int DEFAULT_UPDATE_INTERVAL = 1;
    private static final int DEFAULT_LIFETIME_TICKS = 200;
    private static final double MIN_SPEED_THRESHOLD = 0.01;
    private HurtingItem throwableItem;
    private ItemStack throwableItemStack;
    private double initialVelocityBlocksPerTick;
    private boolean isRicochet;
    private float gravity;
    private int maxLifetimeTicks;
    private List<ProjectileLike.EffectInfo> trailEffects;
    private List<ProjectileLike.EffectInfo> attachedEffects;
    private List<Effect> activeTrailEffects = Collections.emptyList();
    private List<Effect> activeAttachedEffects = Collections.emptyList();

    public static Builder builder() {
        return new Builder();
    }

    public GenericThrowableProjectile(EntityType<? extends GenericThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public GenericThrowableProjectile(EntityType<? extends GenericThrowableProjectile> entityType, LivingEntity owner, Level level) {
        super(entityType, owner, level);
    }

    private void setInitialVelocityBlocksPerTick(double initialVelocityBlocksPerTick) {
        this.initialVelocityBlocksPerTick = initialVelocityBlocksPerTick;
    }

    private void setGravity(float gravity) {
        this.gravity = gravity;
    }

    protected double getDefaultGravity() {
        return this.gravity;
    }

    public void setMaxLifetimeTicks(int maxLifetimeTicks) {
        this.maxLifetimeTicks = maxLifetimeTicks;
    }

    @Override
    public ItemStack getItem() {
        return this.throwableItemStack;
    }

    @Override
    public List<Effect> getActiveAttachedEffects() {
        return this.activeTrailEffects;
    }

    @Override
    public float getProgress(float partialTick) {
        return 0.0f;
    }

    @Override
    public long getElapsedTimeMillis() {
        return 0L;
    }

    @Override
    public double getInitialVelocityBlocksPerTick() {
        return this.initialVelocityBlocksPerTick;
    }

    @Override
    public void launchAtTargetEntity(LivingEntity player, HitResult hitResult, Entity targetEntity) {
        Vec3 hitLocation = hitResult.getLocation();
        Vec3 muzzleWorldPos = this.position();
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewHitVector = hitLocation.subtract(eyePos);
        Vec3 spawnOffset = muzzleWorldPos.subtract(eyePos);
        Vec3 direction = viewHitVector.subtract(spawnOffset).normalize();
        this.shoot(direction.x, direction.y, direction.z, (float)this.getInitialVelocityBlocksPerTick(), 0.0f);
    }

    @Override
    public void launchAtLookTarget(LivingEntity player, double inaccuracy, long seed) {
        HitResult hitScanTarget = HitScan.getNearestObjectInCrosshair(player, 0.0f, 150.0, inaccuracy, seed, block -> false, block -> false, new ArrayList<BlockPos>());
        Vec3 hitLocation = hitScanTarget.getLocation();
        Vec3 muzzleWorldPos = this.position();
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewHitVector = hitLocation.subtract(eyePos);
        Vec3 spawnOffset = muzzleWorldPos.subtract(eyePos);
        Vec3 direction = viewHitVector.subtract(spawnOffset).normalize();
        this.shoot(direction.x, direction.y, direction.z, (float)this.getInitialVelocityBlocksPerTick(), 0.0f);
    }

    private double getSpeedSqr() {
        return this.getDeltaMovement().lengthSqr();
    }

    protected void onHitBlock(BlockHitResult blockHitResult) {
        if (this.isRicochet) {
            BlockPos resultPos = blockHitResult.getBlockPos();
            Level level = MiscUtil.getLevel((Entity)this);
            BlockState state = level.getBlockState(resultPos);
            SoundEvent event = state.getBlock().getSoundType(state).getStepSound();
            if (this.getSpeedSqr() > 0.01) {
                level.playSound(null, blockHitResult.getLocation().x, blockHitResult.getLocation().y, blockHitResult.getLocation().z, event, SoundSource.AMBIENT, 1.0f, 1.0f);
            }
            this.ricochet(blockHitResult.getDirection());
        } else {
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity) {
                LivingEntity player = (LivingEntity)owner;
                this.throwableItem.handleBlockHit(player, blockHitResult, (Entity)this);
            }
        }
    }

    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity owner = this.getOwner();
        if (this.isRicochet) {
            if (owner instanceof LivingEntity) {
                Entity entity;
                LivingEntity player = (LivingEntity)owner;
                if (this.getSpeedSqr() > 0.01 && !MiscUtil.isProtected(entity = entityHitResult.getEntity())) {
                    entity.hurt(entity.damageSources().thrown((Entity)this, this.getOwner()), 0.5f);
                }
            }
            this.ricochet(Direction.getNearest((double)this.getDeltaMovement().x(), (double)this.getDeltaMovement().y(), (double)this.getDeltaMovement().z()).getOpposite(), 0.3, 1.0, 0.3);
        } else if (owner instanceof LivingEntity) {
            LivingEntity player = (LivingEntity)owner;
            this.throwableItem.hurtEntity(player, entityHitResult, (Entity)this, this.throwableItemStack);
        }
    }

    private void ricochet(Direction direction) {
        this.ricochet(direction, 1.0, 1.0, 1.0);
    }

    private void ricochet(Direction direction, double mx, double my, double mz) {
        Direction.Axis axis = direction.getAxis();
        Vec3 delta = this.getDeltaMovement();
        delta = delta.multiply(axis == Direction.Axis.X ? -0.5 : 0.7, axis == Direction.Axis.Y ? -0.2 : 0.7, axis == Direction.Axis.Z ? -0.5 : 0.7);
        if (axis == Direction.Axis.Y && delta.y < this.getGravity()) {
            delta = new Vec3(delta.x, 0.0, delta.z);
        }
        this.setDeltaMovement(delta.multiply(mx, my, mz));
    }

    public boolean isNoGravity() {
        return false;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(BuiltInRegistries.ITEM.getId((Object)this.throwableItemStack.getItem()));
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        int itemId = buffer.readInt();
        this.throwableItemStack = new ItemStack((ItemLike)BuiltInRegistries.ITEM.byId(itemId), 1);
    }

    public void tick() {
        super.tick();
        if (this.tickCount >= this.maxLifetimeTicks) {
            this.doDiscard();
        }
        if (MiscUtil.isClientSide((Entity)this)) {
            this.activeAttachedEffects = this.attachedEffects.stream().filter(ei -> ei.predicate().test(this)).map(ei -> ei.effect()).toList();
            this.activeTrailEffects = this.trailEffects.stream().filter(ei -> ei.predicate().test(this)).map(ei -> ei.effect()).toList();
            Vec3 dm = this.getDeltaMovement();
            for (Effect trailEffect : this.activeTrailEffects) {
                ((TrailEffect)trailEffect).launchNext((Entity)this, new Vec3(this.getX(), this.getY(), this.getZ()), dm);
            }
        }
    }

    public void doDiscard() {
        if (!MiscUtil.isClientSide((Entity)this)) {
            if (this.throwableItem != null) {
                this.throwableItem.discardProjectile((Entity)this);
            }
            this.discard();
        }
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        ClientBoundAddSpawnEntityDataPacket packet = new ClientBoundAddSpawnEntityDataPacket((Entity)this);
        NetworkService networkService = Platform.getInstance().getNetworkService();
        CustomPacketPayload payload = networkService.createCustomPayload(packet);
        return new ClientboundCustomPayloadPacket(payload);
    }

    public static class Builder
    implements EntityBuilder<Builder, GenericThrowableProjectile> {
        private String name;
        private float width = 0.25f;
        private float height = 0.25f;
        private double initialVelocityBlocksPerSecond;
        private int maxLifetimeTicks = 200;
        private float gravity;
        private boolean isRicochet;
        private final List<EffectBuilderInfo> effectBuilderSuppliers = new ArrayList<EffectBuilderInfo>();
        private Supplier<EntityRendererBuilder<?, Entity, EntityRenderer<Entity>>> rendererBuilder;
        private Supplier<HurtingItem> hurtingItem;

        private Builder() {
        }

        @Override
        public String getName() {
            return this.name;
        }

        public Builder withItem(Supplier<Item> hurtingItem) {
            this.hurtingItem = () -> (HurtingItem)hurtingItem.get();
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withMaxLifetime(long lifetimeMillis) {
            this.maxLifetimeTicks = (int)TimeUnit.MILLISECOND.toTicks(lifetimeMillis);
            return this;
        }

        public Builder withSize(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder withInitialVelocity(double initialVelocityBlocksPerSecond) {
            this.initialVelocityBlocksPerSecond = initialVelocityBlocksPerSecond;
            return this;
        }

        public Builder withRenderer(Supplier<EntityRendererBuilder<?, Entity, EntityRenderer<Entity>>> rendererBuilder2) {
            this.rendererBuilder = rendererBuilder2;
            return this;
        }

        public Builder withRicochet(boolean isRicochet) {
            this.isRicochet = isRicochet;
            return this;
        }

        public Builder withGravity(boolean isGravityEnabled) {
            this.gravity = isGravityEnabled ? 0.05f : 0.0f;
            return this;
        }

        public Builder withGravity(double gravity) {
            this.gravity = Mth.clamp((float)((float)gravity), (float)-1.0f, (float)1.0f);
            return this;
        }

        public Builder withEffect(EffectBuilderInfo effectInfo) {
            this.effectBuilderSuppliers.add(effectInfo);
            return this;
        }

        @Override
        public EntityBuilder.EntityTypeExt getEntityTypeExt() {
            return EntityBuilder.EntityTypeExt.PROJECTILE;
        }

        @Override
        public EntityType.Builder<GenericThrowableProjectile> getEntityTypeBuilder() {
            return EntityType.Builder.of(this::build, (MobCategory)MobCategory.MISC).sized(this.width, this.height).clientTrackingRange(1024).noSummon().fireImmune().updateInterval(1);
        }

        public GenericThrowableProjectile build(EntityType<?> entityType, Level level) {
            GenericThrowableProjectile projectile = new GenericThrowableProjectile(entityType, level);
            if (level.isClientSide) {
                this.initEffects(projectile);
            }
            if (this.hurtingItem != null) {
                projectile.throwableItem = this.hurtingItem.get();
                projectile.throwableItemStack = new ItemStack((ItemLike)projectile.throwableItem);
            }
            projectile.maxLifetimeTicks = this.maxLifetimeTicks;
            projectile.isRicochet = this.isRicochet;
            projectile.setInitialVelocityBlocksPerTick(this.initialVelocityBlocksPerSecond * (double)0.05f);
            projectile.setMaxLifetimeTicks(this.maxLifetimeTicks);
            projectile.setNoGravity(MiscUtil.isNearlyZero(this.gravity));
            projectile.setGravity(this.gravity);
            return projectile;
        }

        @Override
        public GenericThrowableProjectile build(Level level) {
            Supplier<EntityType<?>> entityTypeSupplier = EntityRegistry.getTypeByName(this.name);
            return this.build(entityTypeSupplier.get(), level);
        }

        public void initEffects(GenericThrowableProjectile projectile) {
            ArrayList<ProjectileLike.EffectInfo> trailEffects = new ArrayList<ProjectileLike.EffectInfo>();
            ArrayList<ProjectileLike.EffectInfo> attachedEffects = new ArrayList<ProjectileLike.EffectInfo>();
            GunItem.FirePhase phase = GunItem.FirePhase.FLYING;
            for (EffectBuilderInfo effectBuilderInfo : this.effectBuilderSuppliers) {
                AbstractEffect effect;
                EffectBuilder.Context context;
                EffectBuilder<EffectBuilder<?, ?>, ?> effectBuilder = effectBuilderInfo.effectSupplier().get();
                if (effectBuilder.getCompatiblePhases().contains((Object)GunItem.FirePhase.FLYING)) {
                    context = new EffectBuilder.Context();
                    effect = (TrailEffect)effectBuilder.build(context);
                    trailEffects.add(new ProjectileLike.EffectInfo(effect, effectBuilderInfo.predicate()));
                    continue;
                }
                if (effectBuilder instanceof AttachedProjectileEffect.Builder) {
                    context = new EffectBuilder.Context();
                    effect = (AttachedProjectileEffect)effectBuilder.build(context);
                    attachedEffects.add(new ProjectileLike.EffectInfo(effect, effectBuilderInfo.predicate()));
                    continue;
                }
                throw new IllegalStateException("Effect builder " + String.valueOf(effectBuilder) + " is not compatible with phase '" + String.valueOf((Object)phase) + "'. Check how you construct projectile: " + this.getName());
            }
            projectile.trailEffects = Collections.unmodifiableList(trailEffects);
            projectile.attachedEffects = Collections.unmodifiableList(attachedEffects);
        }

        @Override
        public boolean hasRenderer() {
            return this.rendererBuilder != null;
        }

        @Override
        public EntityRenderer<Entity> createEntityRenderer(EntityRendererProvider.Context context) {
            return this.rendererBuilder.get().build(context);
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            throw new UnsupportedOperationException();
        }
    }
}

