/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageSources
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ExplosionDamageCalculator
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.Enableable;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.config.ConfigManager;
import com.vicmatskiv.pointblank.config.ConfigOptionBuilder;
import com.vicmatskiv.pointblank.config.Configurable;
import com.vicmatskiv.pointblank.entity.EntityExt;
import com.vicmatskiv.pointblank.event.BlockHitEvent;
import com.vicmatskiv.pointblank.explosion.CustomExplosion;
import com.vicmatskiv.pointblank.feature.DamageFeature;
import com.vicmatskiv.pointblank.feature.FireModeFeature;
import com.vicmatskiv.pointblank.item.ExplosionDescriptor;
import com.vicmatskiv.pointblank.item.ItemBuilder;
import com.vicmatskiv.pointblank.network.CustomClientBoundExplosionPacket;
import com.vicmatskiv.pointblank.network.SpawnParticlePacket;
import com.vicmatskiv.pointblank.registry.EffectRegistry;
import com.vicmatskiv.pointblank.registry.SoundRegistry;
import com.vicmatskiv.pointblank.util.HitScan;
import com.vicmatskiv.pointblank.util.JsonUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class HurtingItem
extends Item
implements Enableable {
    private static final float DEFAULT_EXPLOSION_SOUND_VOLUME = 4.0f;
    public static final double DEFAULT_MAX_SHOOTING_DISTANCE = 200.0;
    public static final float DEFAULT_DAMAGE = 5.0f;
    public static final float DEFAULT_HEADSHOT_SOUND_VOLUME = 3.0f;
    public static final float DEFAULT_LIGHT_DAMAGE_SOUND_VOLUME = 3.0f;
    public static final float DEFAULT_HEAVY_DAMAGE_SOUND_VOLUME = 3.0f;
    public static final float MAX_LIGHT_DAMAGE = 15.0f;
    public static final long DEFAULT_ENTITY_HIT_SOUND_COOLDOWN = 100L;
    protected ExplosionDescriptor explosionDescriptor;
    protected double maxShootingDistance;
    protected float damage;
    protected boolean isEnabled;
    protected float headshotSoundVolume;
    protected Supplier<SoundEvent> headshotSound;
    protected float lightDamageSoundVolume;
    protected Supplier<SoundEvent> lightDamageSound;
    protected float heavyDamageSoundVolume;
    protected Supplier<SoundEvent> heavyDamageSound;
    protected long entityHitSoundCooldown = 100L;

    public HurtingItem(Item.Properties properties, Builder<?> builder) {
        super(properties);
        if (builder != null) {
            this.explosionDescriptor = builder.explosionDescriptor;
            this.maxShootingDistance = builder.maxShootingDistance;
            this.isEnabled = builder.configOptionEnabled.get();
            this.damage = builder.configOptionDamage.get().floatValue();
            this.headshotSound = builder.headshotSound;
            this.headshotSoundVolume = builder.headshotSoundVolume;
            this.lightDamageSound = builder.lightDamageSound;
            this.lightDamageSoundVolume = builder.lightDamageSoundVolume;
            this.heavyDamageSound = builder.heavyDamageSound;
            this.heavyDamageSoundVolume = builder.heavyDamageSoundVolume;
        }
    }

    public float getDamage() {
        return this.damage;
    }

    public ExplosionDescriptor getExplosion() {
        return this.explosionDescriptor;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public float hurtEntity(LivingEntity player, EntityHitResult entityHitResult, Entity projectile, ItemStack gunStack) {
        float damage = 0.0f;
        if (this.getExplosion() != null) {
            Vec3 hitLocation = entityHitResult.getEntity().getPosition(0.0f);
            if (!MiscUtil.isProtected(entityHitResult.getEntity())) {
                this.explode(MiscUtil.getLevel((Entity)player), projectile, null, hitLocation.x, hitLocation.y, hitLocation.z);
            }
        } else {
            Entity hitEntity = entityHitResult.getEntity();
            if (MiscUtil.isProtected(hitEntity)) {
                Vec3 pos = hitEntity.position();
                MiscUtil.getLevel((Entity)player).playSound(null, pos.x, pos.y, pos.z, SoundEvents.CAT_AMBIENT, SoundSource.AMBIENT, 3.0f, 1.0f);
            } else {
                DamageSource damageSource;
                boolean isHeadshot = false;
                if (hitEntity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity)hitEntity;
                    isHeadshot = HitScan.isHeadshot(livingEntity, entityHitResult.getLocation());
                }
                if (player instanceof Player) {
                    damageSource = player.damageSources().playerAttack((Player)player);
                } else {
                    DamageSources damageSources = MiscUtil.getLevel((Entity)player).damageSources();
                    damageSource = damageSources.generic();
                }
                int origInvulnerableTime = hitEntity.invulnerableTime;
                hitEntity.invulnerableTime = 0;
                double distanceToPlayer = entityHitResult.distanceTo((Entity)player);
                double adjustedDamage = FireModeFeature.getDamage(gunStack);
                if (isHeadshot) {
                    adjustedDamage *= Config.headshotDamageModifier;
                }
                adjustedDamage *= Mth.clamp((double)(1.0 - Math.pow(Math.sqrt(distanceToPlayer) / this.maxShootingDistance * 0.5, 6.0)), (double)0.0, (double)1.0);
                if (gunStack != null) {
                    adjustedDamage *= (double)DamageFeature.getHitScanDamageModifier(gunStack);
                    adjustedDamage *= Config.hitscanDamageModifier;
                }
                hitEntity.hurt(damageSource, (float)adjustedDamage);
                EntityExt entityExt = (EntityExt)hitEntity;
                if (Config.goreSoundsEnabled && System.currentTimeMillis() - entityExt.getLastHitSoundTimestamp() > this.entityHitSoundCooldown) {
                    entityExt.setLastHitSoundTimestamp(System.currentTimeMillis());
                    if (isHeadshot && !MiscUtil.getLevel((Entity)hitEntity).isClientSide) {
                        MiscUtil.getLevel(hitEntity).playSound(null, hitEntity.getX(), hitEntity.getY(), hitEntity.getZ(), this.headshotSound.get(), SoundSource.PLAYERS, this.headshotSoundVolume, 1.0f);
                    } else if (adjustedDamage < 15.0) {
                        MiscUtil.getLevel(hitEntity).playSound(null, hitEntity.getX(), hitEntity.getY(), hitEntity.getZ(), this.lightDamageSound.get(), SoundSource.PLAYERS, this.lightDamageSoundVolume, 1.0f);
                    } else {
                        MiscUtil.getLevel(hitEntity).playSound(null, hitEntity.getX(), hitEntity.getY(), hitEntity.getZ(), this.heavyDamageSound.get(), SoundSource.PLAYERS, this.heavyDamageSoundVolume, 1.0f);
                    }
                }
                hitEntity.invulnerableTime = origInvulnerableTime;
                damage = (float)adjustedDamage;
            }
        }
        if (projectile != null) {
            projectile.discard();
        }
        return damage;
    }

    public void handleBlockHit(LivingEntity player, BlockHitResult blockHitResult, Entity projectile) {
        BlockHitEvent event = new BlockHitEvent(player, blockHitResult, projectile);
        Platform.getInstance().getEventBus().postEvent(event);
        if (!event.isCanceled()) {
            HurtingItem.spawnBlockBreakParticles((ServerPlayer)player, blockHitResult.getBlockPos(), blockHitResult.getLocation());
            if (this.getExplosion() != null) {
                Vec3 hitLocation = blockHitResult.getLocation();
                this.explode(MiscUtil.getLevel((Entity)player), projectile, null, hitLocation.x, hitLocation.y, hitLocation.z);
            }
            if (projectile != null) {
                projectile.discard();
            }
        }
    }

    public void discardProjectile(Entity projectile) {
        this.explodeProjectile(projectile);
    }

    public void explodeProjectile(Entity projectile) {
        this.explode(MiscUtil.getLevel(projectile), projectile, null, projectile.getX(), projectile.getY(), projectile.getZ());
    }

    private static void spawnBlockBreakParticles(ServerPlayer player, BlockPos blockPos, Vec3 hitLocation) {
        Level level = MiscUtil.getLevel((Entity)player);
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState != null) {
            Platform.getInstance().getNetworkService().sendToClient(new SpawnParticlePacket((ParticleType<?>)ParticleTypes.SMOKE, hitLocation.x, hitLocation.y, hitLocation.z, 5), (Player)player);
        }
    }

    private void explode(Level level, Entity entity, DamageSource damageSource, double posX, double posY, double posZ) {
        boolean fire;
        if (level.isClientSide) {
            throw new IllegalArgumentException("Cannot use this method on the client side");
        }
        ExplosionDamageCalculator calc = null;
        float power = this.explosionDescriptor.power();
        CustomExplosion customExplosion = CustomExplosion.explode(level, this, entity, damageSource, calc, posX, posY, posZ, power, fire = this.explosionDescriptor.fire(), this.explosionDescriptor.interaction(), false);
        if (!customExplosion.interactsWithBlocks()) {
            customExplosion.clearToBlow();
        }
        for (ServerPlayer player : ((ServerLevel)level).getPlayers(p -> true)) {
            if (!(player.distanceToSqr(posX, posY, posZ) < 22000.0)) continue;
            Platform.getInstance().getNetworkService().sendToClient(new CustomClientBoundExplosionPacket(this, posX, posY, posZ, power, customExplosion.getToBlow(), (Vec3)customExplosion.getHitPlayers().get(player)), (Player)player);
        }
    }

    public static abstract class Builder<T extends ItemBuilder<T>>
    extends ItemBuilder<T>
    implements Configurable,
    Enableable {
        private ExplosionDescriptor explosionDescriptor;
        private double maxShootingDistance = 200.0;
        private float damage = 5.0f;
        private Supplier<Boolean> configOptionEnabled;
        private Supplier<Double> configOptionDamage;
        private float headshotSoundVolume = 3.0f;
        private Supplier<SoundEvent> headshotSound = SoundRegistry.HIT_HEADSHOT;
        protected float lightDamageSoundVolume = 3.0f;
        protected Supplier<SoundEvent> lightDamageSound = SoundRegistry.HIT_LIGHT;
        protected float heavyDamageSoundVolume = 3.0f;
        protected Supplier<SoundEvent> heavyDamageSound = SoundRegistry.HIT_HEAVY;

        protected T cast(Builder<T> _this) {
            return (T)_this;
        }

        public T withDamage(float damage) {
            this.damage = damage;
            return this.cast(this);
        }

        public T withHeadshotSound(Supplier<SoundEvent> headshotSound, float headshotSoundVolume) {
            this.headshotSound = headshotSound;
            this.headshotSoundVolume = headshotSoundVolume;
            return this.cast(this);
        }

        public T withLightDamageSound(Supplier<SoundEvent> lightDamageSound, float lightDamageSoundVolume) {
            this.lightDamageSound = lightDamageSound;
            this.lightDamageSoundVolume = lightDamageSoundVolume;
            return this.cast(this);
        }

        public T withHeavyDamageSound(Supplier<SoundEvent> heavyDamageSound, float heavyDamageSoundVolume) {
            this.heavyDamageSound = heavyDamageSound;
            this.heavyDamageSoundVolume = heavyDamageSoundVolume;
            return this.cast(this);
        }

        public T withMaxShootingDistance(double distance) {
            this.maxShootingDistance = distance;
            return this.cast(this);
        }

        public T withExplosion(float power, boolean fire, Level.ExplosionInteraction explosionInteraction, List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> effects) {
            return this.withExplosion(power, fire, explosionInteraction, null, 4.0f, effects);
        }

        public T withExplosion(float power, boolean fire, Level.ExplosionInteraction explosionInteraction, String soundName, float soundVolume, List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> effects) {
            this.explosionDescriptor = new ExplosionDescriptor(power, fire, explosionInteraction, soundName, soundVolume, effects);
            return this.cast(this);
        }

        @Override
        public void configure(ConfigManager.Builder builder) {
            this.configOptionEnabled = ((ConfigOptionBuilder)((ConfigOptionBuilder)((ConfigOptionBuilder)builder.createBooleanOption().withName(this.getName() + ".enabled")).withDescription("Set to `false` to remove the item from the game.")).withDefault(true)).getSupplier();
            this.configOptionDamage = ((ConfigOptionBuilder)((ConfigOptionBuilder)((ConfigOptionBuilder)((ConfigOptionBuilder)builder.createDoubleOption().withName(this.getName() + ".damage")).withDescription("Sets this item damage.")).withRange(0.01, 100.0)).withDefault((Double)Double.valueOf(this.damage))).getSupplier();
        }

        @Override
        public T withJsonObject(JsonObject obj, boolean isClientSide) {
            JsonObject jsExplosion;
            this.withMaxShootingDistance(JsonUtil.getJsonDouble(obj, "maxShootingDistance", 200.0));
            this.withDamage(JsonUtil.getJsonFloat(obj, "damage", 5.0f));
            float headshotSoundVolume = JsonUtil.getJsonFloat(obj, "headshotSoundVolume", 3.0f);
            JsonElement headshotSoundElem = obj.get("headshotSound");
            if (headshotSoundElem != null && !headshotSoundElem.isJsonNull()) {
                String headshotSoundName = headshotSoundElem.getAsString();
                this.withHeadshotSound(() -> SoundRegistry.getSoundEvent(headshotSoundName), headshotSoundVolume);
            }
            if ((jsExplosion = obj.getAsJsonObject("explosion")) != null) {
                float power = JsonUtil.getJsonFloat(jsExplosion, "power", 1.0f);
                boolean fire = JsonUtil.getJsonBoolean(jsExplosion, "fire", false);
                String soundName = JsonUtil.getJsonString(jsExplosion, "sound", null);
                float soundVolume = JsonUtil.getJsonFloat(jsExplosion, "soundVolume", 4.0f);
                Level.ExplosionInteraction interaction = JsonUtil.getEnum(jsExplosion, "interaction", Level.ExplosionInteraction.class, Level.ExplosionInteraction.BLOCK, true);
                ArrayList explosionEffects = new ArrayList();
                for (String effectName : JsonUtil.getStrings(jsExplosion, "effects")) {
                    Supplier<EffectBuilder> supplier = () -> EffectRegistry.getEffectBuilderSupplier(effectName).get();
                    explosionEffects.add(supplier);
                }
                this.withExplosion(power, fire, interaction, soundName, soundVolume, explosionEffects);
            }
            return this.cast(this);
        }

        @Override
        public boolean isEnabled() {
            return this.configOptionEnabled == null || this.configOptionEnabled.get() != false;
        }

        public float getDamage() {
            return this.damage;
        }
    }
}

