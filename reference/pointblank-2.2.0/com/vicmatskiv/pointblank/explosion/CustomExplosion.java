/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  net.minecraft.Util
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Vec3i
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.item.PrimedTnt
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.EntityBasedExplosionDamageCalculator
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.Explosion$BlockInteraction
 *  net.minecraft.world.level.ExplosionDamageCalculator
 *  net.minecraft.world.level.GameRules
 *  net.minecraft.world.level.GameRules$BooleanValue
 *  net.minecraft.world.level.GameRules$Key
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.level.block.BaseFireBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.gameevent.GameEvent
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.storage.loot.LootParams$Builder
 *  net.minecraft.world.level.storage.loot.parameters.LootContextParams
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package com.vicmatskiv.pointblank.explosion;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.explosion.ExplosionEvent;
import com.vicmatskiv.pointblank.item.ExplosionDescriptor;
import com.vicmatskiv.pointblank.item.ExplosionProvider;
import com.vicmatskiv.pointblank.registry.SoundRegistry;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.SimpleHitResult;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CustomExplosion
extends Explosion {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private static final float DEFAULT_SOUND_VOLUME = 4.0f;
    private Item item;
    private Level level;
    private float radius;
    private double x;
    private double y;
    private double z;
    private boolean fire;
    private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList();
    private Explosion.BlockInteraction blockInteraction;
    private Random random;
    private Entity source;
    private ExplosionDamageCalculator damageCalculator;
    private final Map<Player, Vec3> hitPlayers = Maps.newHashMap();
    private DamageSource damageSource;
    private Vec3 position;

    public CustomExplosion(Level level, Item item, Entity entity, DamageSource damageSource, ExplosionDamageCalculator calc, double posX, double posY, double posZ, float power, boolean fire, Explosion.BlockInteraction blockInteraction) {
        super(level, entity, damageSource, calc, posX, posY, posZ, power, fire, blockInteraction, null, null, null);
        this.level = level;
        this.item = item;
        this.source = entity;
        this.x = posX;
        this.y = posY;
        this.z = posZ;
        this.radius = power;
        this.fire = fire;
        this.blockInteraction = blockInteraction;
        this.random = new Random();
        this.damageSource = damageSource;
        this.damageSource = damageSource != null ? damageSource : CustomExplosion.getDefaultDamageSource((Level)level, (Entity)entity);
        this.damageCalculator = calc == null ? this.makeDamageCalculator(entity) : calc;
        this.position = new Vec3(this.x, this.y, this.z);
    }

    public CustomExplosion(Level level, Item gunItem, Entity entity, double x, double y, double z, float power, List<BlockPos> toBlow) {
        super(level, entity, x, y, z, power, false, null, toBlow);
        this.level = level;
        this.item = gunItem;
        this.source = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = power;
        this.toBlow.addAll(toBlow);
        this.position = new Vec3(this.x, this.y, this.z);
    }

    private ExplosionDamageCalculator makeDamageCalculator(Entity entity) {
        return entity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(entity);
    }

    public Item getItem() {
        return this.item;
    }

    public void finalizeClientExplosion() {
        Item item = this.item;
        if (item instanceof ExplosionProvider) {
            ExplosionProvider explosionProvider = (ExplosionProvider)item;
            ExplosionDescriptor explosionDescriptor = explosionProvider.getExplosion();
            Platform.getInstance().getEventBus().postEvent(new ExplosionEvent(new Vec3(this.x, this.y, this.z), explosionDescriptor));
            SoundEvent soundEvent = null;
            float soundVolume = 4.0f;
            if (explosionDescriptor != null) {
                if (explosionDescriptor.soundName() != null) {
                    soundEvent = SoundRegistry.getSoundEvent(explosionDescriptor.soundName());
                }
                soundVolume = explosionDescriptor.soundVolume();
                this.applyExplosionEffects(explosionDescriptor);
            }
            if (soundEvent == null) {
                soundEvent = (SoundEvent)SoundEvents.GENERIC_EXPLODE.value();
            }
            if (!MiscUtil.isNearlyZero(soundVolume)) {
                this.playSound(soundEvent, soundVolume);
            }
        }
        this.finalizeExplosion(false);
    }

    private void playSound(SoundEvent soundEvent, float volume) {
        this.level.playLocalSound(this.x, this.y, this.z, soundEvent, SoundSource.BLOCKS, volume, (1.0f + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2f) * 0.7f, false);
    }

    private void applyExplosionEffects(ExplosionDescriptor explosionDescriptor) {
        List<Supplier<EffectBuilder<EffectBuilder<?, ?>, ?>>> effects = explosionDescriptor.effects();
        for (Supplier<EffectBuilder<EffectBuilder<?, ?>, ?>> effectBuilderSupplier : effects) {
            EffectBuilder<EffectBuilder<?, ?>, ?> effectBuilder = effectBuilderSupplier.get();
            EffectBuilder.Context context = new EffectBuilder.Context().withHitResult(new SimpleHitResult(this.position, HitResult.Type.BLOCK, Direction.UP, -1));
            Object effect = effectBuilder.build(context);
            effect.launch((Entity)ClientUtil.getClientPlayer());
        }
    }

    public void finalizeExplosion(boolean ignored) {
        boolean flag = this.interactsWithBlocks();
        if (flag) {
            ObjectArrayList objectarraylist = new ObjectArrayList();
            boolean flag1 = this.getIndirectSourceEntity() instanceof Player;
            Util.shuffle(this.toBlow, (RandomSource)this.level.random);
            for (BlockPos blockpos : this.toBlow) {
                Level $$9;
                BlockState blockstate = this.level.getBlockState(blockpos);
                if (blockstate.isAir()) continue;
                BlockPos blockpos1 = blockpos.immutable();
                this.level.getProfiler().push("explosion_blocks");
                if (blockstate.getBlock().dropFromExplosion((Explosion)this) && ($$9 = this.level) instanceof ServerLevel) {
                    ServerLevel serverlevel = (ServerLevel)$$9;
                    BlockEntity blockentity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
                    LootParams.Builder lootparams$builder = new LootParams.Builder(serverlevel).withParameter(LootContextParams.ORIGIN, (Object)Vec3.atCenterOf((Vec3i)blockpos)).withParameter(LootContextParams.TOOL, (Object)ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, (Object)blockentity).withOptionalParameter(LootContextParams.THIS_ENTITY, (Object)this.source);
                    if (this.blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
                        lootparams$builder.withParameter(LootContextParams.EXPLOSION_RADIUS, (Object)Float.valueOf(this.radius));
                    }
                    blockstate.spawnAfterBreak(serverlevel, blockpos, ItemStack.EMPTY, flag1);
                    blockstate.getDrops(lootparams$builder).forEach(p_46074_ -> CustomExplosion.addBlockDrops((ObjectArrayList<Pair<ItemStack, BlockPos>>)objectarraylist, p_46074_, blockpos1));
                }
                MiscUtil.onBlockExploded(blockstate, this.level, blockpos, this);
                this.level.getProfiler().pop();
            }
            for (Pair pair : objectarraylist) {
                Block.popResource((Level)this.level, (BlockPos)((BlockPos)pair.getSecond()), (ItemStack)((ItemStack)pair.getFirst()));
            }
        }
        if (this.fire) {
            for (BlockPos blockpos2 : this.toBlow) {
                if (this.random.nextInt(3) != 0 || !this.level.getBlockState(blockpos2).isAir() || !this.level.getBlockState(blockpos2.below()).isSolidRender((BlockGetter)this.level, blockpos2.below())) continue;
                this.level.setBlockAndUpdate(blockpos2, BaseFireBlock.getState((BlockGetter)this.level, (BlockPos)blockpos2));
            }
        }
    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> blockPosToBlow, ItemStack itemStack, BlockPos blockPos) {
        int i = blockPosToBlow.size();
        for (int j = 0; j < i; ++j) {
            Pair pair = (Pair)blockPosToBlow.get(j);
            ItemStack itemstack = (ItemStack)pair.getFirst();
            if (!ItemEntity.areMergable((ItemStack)itemstack, (ItemStack)itemStack)) continue;
            ItemStack itemstack1 = ItemEntity.merge((ItemStack)itemstack, (ItemStack)itemStack, (int)16);
            blockPosToBlow.set(j, (Object)Pair.of((Object)itemstack1, (Object)((BlockPos)pair.getSecond())));
            if (!itemStack.isEmpty()) continue;
            return;
        }
        blockPosToBlow.add((Object)Pair.of((Object)itemStack, (Object)blockPos));
    }

    private static Explosion.BlockInteraction getDestroyType(Level level, GameRules.Key<GameRules.BooleanValue> gameRulesKey) {
        return level.getGameRules().getBoolean(gameRulesKey) ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.DESTROY;
    }

    public static CustomExplosion explode(Level level, Item item, Entity entity, DamageSource damageSource, ExplosionDamageCalculator calc, double posX, double posY, double posZ, float power, boolean fire, Level.ExplosionInteraction interaction, boolean particlesEnabled) {
        if (!Config.explosionDestroyBlocksEnabled) {
            interaction = Level.ExplosionInteraction.NONE;
        }
        CustomExplosion explosion = new CustomExplosion(level, item, entity, damageSource, calc, posX, posY, posZ, power, fire, switch (interaction) {
            case Level.ExplosionInteraction.NONE -> Explosion.BlockInteraction.KEEP;
            case Level.ExplosionInteraction.BLOCK -> CustomExplosion.getDestroyType(level, (GameRules.Key<GameRules.BooleanValue>)GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY);
            case Level.ExplosionInteraction.TNT -> CustomExplosion.getDestroyType(level, (GameRules.Key<GameRules.BooleanValue>)GameRules.RULE_TNT_EXPLOSION_DROP_DECAY);
            default -> throw new IncompatibleClassChangeError();
        });
        explosion.explode();
        explosion.finalizeExplosion(false);
        return explosion;
    }

    public void explode() {
        this.level.gameEvent(this.source, (Holder)GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
        HashSet set = Sets.newHashSet();
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                block2: for (int l = 0; l < 16; ++l) {
                    if (j != 0 && j != 15 && k != 0 && k != 15 && l != 0 && l != 15) continue;
                    double d0 = (float)j / 15.0f * 2.0f - 1.0f;
                    double d1 = (float)k / 15.0f * 2.0f - 1.0f;
                    double d2 = (float)l / 15.0f * 2.0f - 1.0f;
                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    d0 /= d3;
                    d1 /= d3;
                    d2 /= d3;
                    double d4 = this.x;
                    double d6 = this.y;
                    double d8 = this.z;
                    float f1 = 0.3f;
                    for (float f = this.radius * (0.7f + this.level.random.nextFloat() * 0.6f); f > 0.0f; f -= 0.22500001f) {
                        BlockPos blockpos = BlockPos.containing((double)d4, (double)d6, (double)d8);
                        BlockState blockstate = this.level.getBlockState(blockpos);
                        FluidState fluidstate = this.level.getFluidState(blockpos);
                        if (!this.level.isInWorldBounds(blockpos)) continue block2;
                        Optional optional = this.damageCalculator.getBlockExplosionResistance((Explosion)this, (BlockGetter)this.level, blockpos, blockstate, fluidstate);
                        if (optional.isPresent()) {
                            f -= (((Float)optional.get()).floatValue() + 0.3f) * 0.3f;
                        }
                        if (f > 0.0f && this.damageCalculator.shouldBlockExplode((Explosion)this, (BlockGetter)this.level, blockpos, blockstate, f)) {
                            set.add(blockpos);
                        }
                        d4 += d0 * (double)0.3f;
                        d6 += d1 * (double)0.3f;
                        d8 += d2 * (double)0.3f;
                    }
                }
            }
        }
        this.toBlow.addAll((Collection)set);
        float adjustedRadius = this.radius * 2.0f;
        int bbXMin = Mth.floor((double)(this.x - (double)adjustedRadius - 1.0));
        int bbXMax = Mth.floor((double)(this.x + (double)adjustedRadius + 1.0));
        int bbYMin = Mth.floor((double)(this.y - (double)adjustedRadius - 1.0));
        int bbYMax = Mth.floor((double)(this.y + (double)adjustedRadius + 1.0));
        int bbZMin = Mth.floor((double)(this.z - (double)adjustedRadius - 1.0));
        int bbZMax = Mth.floor((double)(this.z + (double)adjustedRadius + 1.0));
        List list = this.level.getEntities(this.source, new AABB((double)bbXMin, (double)bbYMin, (double)bbZMin, (double)bbXMax, (double)bbYMax, (double)bbZMax));
        Vec3 thisPos = new Vec3(this.x, this.y, this.z);
        for (int k2 = 0; k2 < list.size(); ++k2) {
            Player player;
            double d11;
            double zOffset;
            double yOffset;
            double xOffset;
            double adjustedDistanceToEntity;
            double normalizedDistanceToEntity;
            Entity entity = (Entity)list.get(k2);
            if (entity.ignoreExplosion((Explosion)this) || MiscUtil.isProtected(entity) || !((normalizedDistanceToEntity = Math.sqrt(entity.distanceToSqr(thisPos)) / (double)adjustedRadius) <= 1.0) || (adjustedDistanceToEntity = Math.sqrt((xOffset = entity.getX() - this.x) * xOffset + (yOffset = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y) * yOffset + (zOffset = entity.getZ() - this.z) * zOffset)) == 0.0) continue;
            xOffset /= adjustedDistanceToEntity;
            yOffset /= adjustedDistanceToEntity;
            zOffset /= adjustedDistanceToEntity;
            double seenPercent = CustomExplosion.getSeenPercent((Vec3)thisPos, (Entity)entity);
            double damage = (1.0 - normalizedDistanceToEntity) * seenPercent;
            entity.hurt(this.damageSource, (float)((int)((damage * damage + damage) / 2.0 * 7.5 * (double)adjustedRadius + 1.0)));
            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity)entity;
                d11 = damage * (1.0 - livingentity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE));
            } else {
                d11 = damage;
            }
            Vec3 knockbackMovement = new Vec3(xOffset *= d11, yOffset *= d11, zOffset *= d11);
            entity.setDeltaMovement(entity.getDeltaMovement().add(knockbackMovement));
            if (!(entity instanceof Player) || (player = (Player)entity).isSpectator() || player.isCreative() && player.getAbilities().flying) continue;
            this.hitPlayers.put(player, knockbackMovement);
        }
    }
}

