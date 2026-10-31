/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.network.syncher.SynchedEntityData$Builder
 *  net.minecraft.server.level.ServerEntity
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.AbstractHurtingProjectile
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
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
import com.vicmatskiv.pointblank.item.AmmoItem;
import com.vicmatskiv.pointblank.item.EffectBuilderInfo;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.item.HurtingItem;
import com.vicmatskiv.pointblank.network.ClientBoundAddSpawnEntityDataPacket;
import com.vicmatskiv.pointblank.registry.EntityRegistry;
import com.vicmatskiv.pointblank.util.DirectAttackTrajectory;
import com.vicmatskiv.pointblank.util.HitScan;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.TimeUnit;
import com.vicmatskiv.pointblank.util.TopDownAttackTrajectory;
import com.vicmatskiv.pointblank.util.Trajectory;
import com.vicmatskiv.pointblank.util.TrajectoryProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SlowProjectile
extends AbstractHurtingProjectile
implements ProjectileLike,
ExtraSpawnDataHolder {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static final double MAX_RENDER_DISTANCE_SQR = 40000.0;
    private static final float MAX_HIT_SCAN_DISTANCE = 10.0f;
    public static final float DEFAULT_MAX_DISTANCE = 150.0f;
    private static final double DEFAULT_GRAVITY = 0.05;
    private static final EntityDataAccessor<OptionalInt> DATA_ATTACHED_TO_TARGET = SynchedEntityData.defineId(SlowProjectile.class, (EntityDataSerializer)EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
    private static final EntityDataAccessor<Boolean> DATA_SHOT_AT_ANGLE = SynchedEntityData.defineId(SlowProjectile.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private double initialVelocityBlocksPerTick;
    private int life;
    private int lifetime;
    private HitResult hitScanTarget;
    private double gravity;
    private LivingEntity attachedToEntity;
    private float initialAngle;
    private long startTimeMillis;
    private List<ProjectileLike.EffectInfo> trailEffects;
    private List<ProjectileLike.EffectInfo> attachedEffects;
    private List<Effect> activeTrailEffects = Collections.emptyList();
    private List<Effect> activeAttachedEffects = Collections.emptyList();
    private Trajectory<?> trajectory;
    private Entity targetEntity;
    public HurtingItem hurtingItem;
    private ItemStack hurtingItemStack;

    public static Builder builder() {
        return new Builder();
    }

    public SlowProjectile(EntityType<? extends SlowProjectile> entityType, Level level) {
        super(entityType, level);
        this.initialAngle = this.random.nextFloat() * 360.0f;
        this.startTimeMillis = System.currentTimeMillis();
    }

    @Override
    public void launchAtLookTarget(LivingEntity entity, double inaccuracy, long seed) {
        this.hitScanTarget = HitScan.getNearestObjectInCrosshair(entity, 0.0f, 150.0, inaccuracy, seed, block -> false, block -> false, new ArrayList<BlockPos>());
        Vec3 hitLocation = this.hitScanTarget.getLocation();
        Vec3 muzzleWorldPos = this.position();
        Vec3 eyePos = entity.getEyePosition();
        Vec3 viewHitVector = hitLocation.subtract(eyePos);
        Vec3 spawnOffset = muzzleWorldPos.subtract(eyePos);
        Vec3 direction = viewHitVector.subtract(spawnOffset).normalize();
        this.shoot(direction.x, direction.y, direction.z, this.getInitialVelocityBlocksPerTick());
    }

    @Override
    public void launchAtTargetEntity(LivingEntity player, HitResult hitResult, Entity targetEntity) {
        this.hitScanTarget = hitResult;
        this.targetEntity = targetEntity;
        Vec3 hitLocation = this.hitScanTarget.getLocation();
        Vec3 muzzleWorldPos = this.position();
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewHitVector = hitLocation.subtract(eyePos);
        Vec3 spawnOffset = muzzleWorldPos.subtract(eyePos);
        Vec3 direction = viewHitVector.subtract(spawnOffset).normalize();
        this.shoot(direction.x, direction.y, direction.z, this.getInitialVelocityBlocksPerTick());
    }

    public void shoot(double dx, double dy, double dz, double initialSpeed) {
        Vec3 deltaMovement = new Vec3(dx, dy, dz).normalize().scale(initialSpeed);
        this.setDeltaMovement(deltaMovement);
        double horizontalDistance = deltaMovement.horizontalDistance();
        this.setYRot((float)(Mth.atan2((double)deltaMovement.x, (double)deltaMovement.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2((double)deltaMovement.y, (double)horizontalDistance) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        LOGGER.debug("{} initializing projectile trajectory", (Object)(System.currentTimeMillis() % 100000L));
        this.initTrajectory(this.hitScanTarget.getLocation());
        LOGGER.debug("{} performed projectile shot", (Object)(System.currentTimeMillis() % 100000L));
    }

    private void initTrajectory(Vec3 targetLocation) {
        Vec3 startPosition = new Vec3(this.getX(), this.getY(), this.getZ());
        if (this.hurtingItem instanceof TrajectoryProvider) {
            TrajectoryProvider trajectoryProvider = (TrajectoryProvider)((Object)this.hurtingItem);
            this.trajectory = trajectoryProvider.createTrajectory(this.level(), startPosition, targetLocation);
        }
        if (this.trajectory == null) {
            this.trajectory = new DirectAttackTrajectory(startPosition, this.getDeltaMovement(), this.gravity);
        }
    }

    public int getLife() {
        return this.life;
    }

    public int getLifetime() {
        return this.lifetime;
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACHED_TO_TARGET, (Object)OptionalInt.empty());
        builder.define(DATA_SHOT_AT_ANGLE, (Object)false);
    }

    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 40000.0;
    }

    public boolean shouldRender(double p_37083_, double p_37084_, double p_37085_) {
        return super.shouldRender(p_37083_, p_37084_, p_37085_);
    }

    public void tick() {
        Entity entity;
        int entityId;
        Level level = MiscUtil.getLevel((Entity)this);
        if (MiscUtil.isClientSide((Entity)this)) {
            Vec3 dm = this.getDeltaMovement();
            for (Effect trailEffect : this.getActiveTrailEffects()) {
                ((TrailEffect)trailEffect).launchNext((Entity)this, new Vec3(this.getX(), this.getY(), this.getZ()), dm);
            }
        }
        if (level.isClientSide && this.attachedToEntity == null && (entityId = ((OptionalInt)this.entityData.get(DATA_ATTACHED_TO_TARGET)).orElse(-1)) >= 0 && (entity = MiscUtil.getLevel((Entity)this).getEntity(entityId)) instanceof LivingEntity) {
            this.attachedToEntity = (LivingEntity)entity;
        }
        if (this.life > this.getLifetime()) {
            this.discard();
        }
        if (this.trajectory != null) {
            this.trajectory.tick();
            if (!level.isClientSide && this.trajectory.isCompleted()) {
                this.explode();
            }
        }
        if (level.isClientSide) {
            this.activeAttachedEffects = this.attachedEffects.stream().filter(ei -> ei.predicate().test(this)).map(ei -> ei.effect()).toList();
            this.activeTrailEffects = this.trailEffects.stream().filter(ei -> ei.predicate().test(this)).map(ei -> ei.effect()).toList();
        }
        if (this.targetEntity != null) {
            this.trajectory.setTargetPosition(this.targetEntity.getPosition(0.0f));
        }
        Entity entity2 = this.getOwner();
        if (level.isClientSide || (entity2 == null || !entity2.isRemoved()) && MiscUtil.getLevel((Entity)this).hasChunkAt(this.blockPosition())) {
            HitResult hitresult = this.getHitResultOnMoveOrViewVector();
            if (hitresult.getType() != HitResult.Type.MISS) {
                this.onHit(hitresult);
            }
            if (this.trajectory != null) {
                this.setDeltaMovement(this.trajectory.getDeltaMovement());
                this.setPos(this.trajectory.getEndOfTickPosition());
            } else {
                Vec3 deltaMovement = this.getDeltaMovement();
                double d0 = this.getX() + deltaMovement.x;
                double d1 = this.getY() + deltaMovement.y;
                double d2 = this.getZ() + deltaMovement.z;
                this.setDeltaMovement(deltaMovement.x, deltaMovement.y - this.getGravity(), deltaMovement.z);
                this.setPos(d0, d1, d2);
            }
        } else {
            this.discard();
        }
        if (!level.isClientSide && this.life > this.getLifetime()) {
            this.explode();
        }
        ++this.life;
    }

    public void lerpTo(double p_19896_, double p_19897_, double p_19898_, float p_19899_, float p_19900_, int p_19901_) {
    }

    public void setDeltaMovement(double dx, double dy, double dz) {
        super.setDeltaMovement(dx, dy, dz);
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        this.setYRot((float)(Mth.atan2((double)dx, (double)dz) * 57.29577951308232));
        this.setXRot((float)(Mth.atan2((double)dy, (double)horizontalDistance) * 57.29577951308232));
    }

    public void setDeltaMovement(Vec3 dm) {
        super.setDeltaMovement(dm);
        double horizontalDistance = Math.sqrt(dm.x * dm.x + dm.z * dm.z);
        this.setYRot((float)(Mth.atan2((double)dm.x, (double)dm.z) * 57.29577951308232));
        this.setXRot((float)(Mth.atan2((double)dm.y, (double)horizontalDistance) * 57.29577951308232));
    }

    private HitResult getHitResultOnMoveOrViewVector() {
        Entity owner = this.getOwner();
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector((Entity)this, this::canHitEntity);
        if (owner != null) {
            Vec3 ownerPos = owner.position();
            Vec3 originalHitPos = hitResult.getLocation();
            double distanceToOwner = originalHitPos.distanceTo(ownerPos);
            if (distanceToOwner <= 10.0 && this.hitScanTarget != null && this.hitScanTarget.getLocation().distanceTo(ownerPos) <= 10.0) {
                hitResult = this.hitScanTarget;
            }
        }
        return hitResult;
    }

    protected boolean canHitEntity(Entity entity) {
        return entity != this.getOwner() && super.canHitEntity(entity) && !entity.noPhysics;
    }

    protected float getWaterInertia() {
        return 0.6f;
    }

    protected EntityHitResult findHitEntity(Vec3 p_36758_, Vec3 p_36759_) {
        return ProjectileUtil.getEntityHitResult((Level)MiscUtil.getLevel((Entity)this), (Entity)this, (Vec3)p_36758_, (Vec3)p_36759_, (AABB)this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity);
    }

    protected void onHit(HitResult result) {
        super.onHit(result);
        this.discard();
    }

    private void explode() {
        if (this.hurtingItem != null) {
            this.hurtingItem.explodeProjectile((Entity)this);
        }
        this.discard();
    }

    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity owner = this.getOwner();
        HurtingItem hurtingItem = this.hurtingItem;
        if (hurtingItem instanceof AmmoItem) {
            AmmoItem ammoItem = (AmmoItem)hurtingItem;
            if (owner instanceof LivingEntity) {
                this.hurtingItem.hurtEntity((LivingEntity)owner, entityHitResult, (Entity)this, new ItemStack((ItemLike)ammoItem));
            }
        }
    }

    protected void onHitBlock(BlockHitResult blockHitResult) {
        BlockPos blockpos = new BlockPos((Vec3i)blockHitResult.getBlockPos());
        MiscUtil.getLevel((Entity)this).getBlockState(blockpos).entityInside(MiscUtil.getLevel((Entity)this), blockpos, (Entity)this);
        Entity owner = this.getOwner();
        if (this.hurtingItem != null && !MiscUtil.isClientSide((Entity)this) && owner instanceof LivingEntity) {
            this.hurtingItem.handleBlockHit((LivingEntity)((Player)owner), blockHitResult, (Entity)this);
        }
        super.onHitBlock(blockHitResult);
    }

    public boolean isShotAtAngle() {
        return (Boolean)this.entityData.get(DATA_SHOT_AT_ANGLE);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Life", this.life);
        tag.putInt("LifeTime", this.getLifetime());
        tag.putBoolean("ShotAtAngle", ((Boolean)this.entityData.get(DATA_SHOT_AT_ANGLE)).booleanValue());
    }

    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.discard();
    }

    public boolean isAttackable() {
        return false;
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        ClientBoundAddSpawnEntityDataPacket packet = new ClientBoundAddSpawnEntityDataPacket((Entity)this);
        NetworkService networkService = Platform.getInstance().getNetworkService();
        CustomPacketPayload payload = networkService.createCustomPayload(packet);
        return new ClientboundCustomPayloadPacket(payload);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        Vec3 movement = this.getDeltaMovement();
        buffer.writeInt(this.getLifetime());
        buffer.writeDouble(this.getX());
        buffer.writeDouble(this.getY());
        buffer.writeDouble(this.getZ());
        buffer.writeDouble(movement.x);
        buffer.writeDouble(movement.y);
        buffer.writeDouble(movement.z);
        buffer.writeFloat(this.getXRot());
        buffer.writeFloat(this.getYHeadRot());
        Vec3 targetLocation = this.hitScanTarget != null ? this.hitScanTarget.getLocation() : Vec3.ZERO;
        buffer.writeDouble(targetLocation.x);
        buffer.writeDouble(targetLocation.y);
        buffer.writeDouble(targetLocation.z);
        buffer.writeInt(this.targetEntity != null ? this.targetEntity.getId() : -1);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.setLifetime(buffer.readInt());
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        double dx = buffer.readDouble();
        double dy = buffer.readDouble();
        double dz = buffer.readDouble();
        float xRot = buffer.readFloat();
        float yRot = buffer.readFloat();
        double hitLocationX = buffer.readDouble();
        double hitLocationY = buffer.readDouble();
        double hitLocationZ = buffer.readDouble();
        int entityId = buffer.readInt();
        this.absMoveTo(x, y, z, yRot, xRot);
        this.setDeltaMovement(dx, dy, dz);
        this.setYHeadRot(yRot);
        this.setYBodyRot(yRot);
        if (entityId >= 0) {
            this.targetEntity = this.level().getEntity(entityId);
        }
        this.initTrajectory(new Vec3(hitLocationX, hitLocationY, hitLocationZ));
    }

    public void setInitialPosition(double x, double y, double z) {
        this.setPos(x, y, z);
    }

    @Override
    public float getProgress(float partialTick) {
        return ((float)this.life + partialTick) / (float)this.getLifetime();
    }

    @Override
    public long getElapsedTimeMillis() {
        return System.currentTimeMillis() - this.startTimeMillis;
    }

    public float getInitialAngle() {
        return this.initialAngle;
    }

    @Override
    public ItemStack getItem() {
        return this.hurtingItemStack;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public double getDefaultGravity() {
        return this.gravity;
    }

    private void setGravity(double gravity) {
        this.gravity = gravity;
    }

    @Override
    public double getInitialVelocityBlocksPerTick() {
        return this.initialVelocityBlocksPerTick;
    }

    private void setInitialVelocityBlocksPerTick(double initialVelocityBlocksPerTick) {
        this.initialVelocityBlocksPerTick = initialVelocityBlocksPerTick;
    }

    @Override
    public List<Effect> getActiveAttachedEffects() {
        return this.activeAttachedEffects;
    }

    public List<Effect> getActiveTrailEffects() {
        return this.activeTrailEffects;
    }

    @Override
    public void setTrailEffects(List<ProjectileLike.EffectInfo> trailEffects) {
        this.trailEffects = trailEffects;
    }

    @Override
    public void setAttachedEffects(List<ProjectileLike.EffectInfo> attachedEffects) {
        this.attachedEffects = attachedEffects;
    }

    @Override
    public Trajectory<?> getTrajectory() {
        return this.trajectory;
    }

    public static Predicate<ProjectileLike> topDownTrajectoryPhasePredicate(Predicate<TopDownAttackTrajectory> tp) {
        return projectile -> {
            TopDownAttackTrajectory tdat;
            Trajectory<?> patt0$temp;
            return projectile.getTrajectory() != null && (patt0$temp = projectile.getTrajectory()) instanceof TopDownAttackTrajectory && tp.test(tdat = (TopDownAttackTrajectory)patt0$temp);
        };
    }

    public static class Builder
    implements EntityBuilder<Builder, SlowProjectile> {
        private static final float DEFAULT_WIDTH = 0.25f;
        private static final float DEFAULT_HEIGHT = 0.25f;
        private static final int DEFAULT_TRACKING_RANGE = 1024;
        private static final int DEFAULT_CLIENT_TRACKING_RANGE = 1024;
        private static final int DEFAULT_UPDATE_INTERVAL = 1;
        private static final int DEFAULT_LIFETIME_TICKS = 200;
        private String name;
        private float width = 0.25f;
        private float height = 0.25f;
        private int clientTrackingRange = 1024;
        private int trackingRange = 1024;
        private int updateInterval = 1;
        private double initialVelocityBlocksPerSecond;
        private int lifetimeTicks = 200;
        private double gravity;
        private List<EffectBuilderInfo> effectBuilderSuppliers = new ArrayList<EffectBuilderInfo>();
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
            this.lifetimeTicks = (int)TimeUnit.MILLISECOND.toTicks(lifetimeMillis);
            return this;
        }

        public Builder withSize(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        @Override
        public EntityBuilder<?, ?> withRicochet(boolean ricochet) {
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

        public Builder withGravity(boolean isGravityEnabled) {
            this.gravity = isGravityEnabled ? 0.05 : 0.0;
            return this;
        }

        public Builder withGravity(double gravity) {
            this.gravity = Mth.clamp((double)gravity, (double)-1.0, (double)1.0);
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
        public EntityType.Builder<SlowProjectile> getEntityTypeBuilder() {
            return EntityType.Builder.of(this::create, (MobCategory)MobCategory.MISC).sized(this.width, this.height).clientTrackingRange(this.clientTrackingRange).updateInterval(this.updateInterval);
        }

        public SlowProjectile create(EntityType<SlowProjectile> entityType, Level level) {
            SlowProjectile projectile = new SlowProjectile(entityType, level);
            if (level.isClientSide) {
                this.initEffects(projectile);
            }
            projectile.setGravity(this.gravity);
            if (this.hurtingItem != null) {
                projectile.hurtingItem = this.hurtingItem.get();
                projectile.hurtingItemStack = new ItemStack((ItemLike)projectile.hurtingItem);
            }
            return projectile;
        }

        @Override
        public SlowProjectile build(Level level) {
            Supplier<EntityType<?>> entityType = EntityRegistry.getTypeByName(this.name);
            SlowProjectile projectile = new SlowProjectile(entityType.get(), level);
            projectile.setInitialVelocityBlocksPerTick(this.initialVelocityBlocksPerSecond * (double)0.05f);
            projectile.setLifetime(this.lifetimeTicks);
            projectile.setNoGravity(MiscUtil.isNearlyZero(this.gravity));
            projectile.setGravity(this.gravity);
            if (this.hurtingItem != null) {
                projectile.hurtingItem = this.hurtingItem.get();
                projectile.hurtingItemStack = new ItemStack((ItemLike)projectile.hurtingItem);
            }
            if (level.isClientSide) {
                this.initEffects(projectile);
            }
            return projectile;
        }

        public void initEffects(SlowProjectile projectile) {
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

