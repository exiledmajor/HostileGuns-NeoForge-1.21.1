/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.boss.enderdragon.EnderDragon
 *  net.minecraft.world.entity.monster.Creeper
 *  net.minecraft.world.entity.monster.EnderMan
 *  net.minecraft.world.entity.monster.Endermite
 *  net.minecraft.world.entity.monster.Shulker
 *  net.minecraft.world.entity.monster.Skeleton
 *  net.minecraft.world.entity.monster.Slime
 *  net.minecraft.world.entity.monster.warden.Warden
 *  net.minecraft.world.entity.player.Player
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.client.SegmentsProviders;
import com.vicmatskiv.pointblank.client.effect.AbstractEffect;
import com.vicmatskiv.pointblank.client.effect.AttachedProjectileEffect;
import com.vicmatskiv.pointblank.client.effect.DetachedProjectileEffect;
import com.vicmatskiv.pointblank.client.effect.Effect;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.ImpactEffect;
import com.vicmatskiv.pointblank.client.effect.MuzzleFlashEffect;
import com.vicmatskiv.pointblank.client.effect.TrailEffect;
import com.vicmatskiv.pointblank.util.Interpolators;
import com.vicmatskiv.pointblank.util.ParticleValueProviders;
import com.vicmatskiv.pointblank.util.VelocityProviders;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;

public class EffectRegistry {
    private static Map<String, Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> suppliers = new HashMap();
    private static Map<UUID, Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> effectSuppliersById = new HashMap();
    private static Map<Class<? extends Entity>, List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>>> entityDeathEffects = new HashMap();
    private static Map<Class<? extends Entity>, List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>>> entityHitEffects = new HashMap();
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> MUZZLE_FLASH = EffectRegistry.register("muzzle_flash", () -> ((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)new MuzzleFlashEffect.Builder().withName("muzzle_flash")).withTexture("textures/effect/flashes.png")).withDuration(50L)).withBrightness(1)).withSprites(1, 9, 1, AbstractEffect.SpriteAnimationType.RANDOM)).withGlow(true)).withWidthProvider(new Interpolators.ConstantFloatProvider(1.25f))).withAlphaProvider(new Interpolators.EaseOutFloatProvider(0.6f))).withInitialRollProvider(() -> 0.0f));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> MUZZLE_FLASH_SMALL = EffectRegistry.register("muzzle_flash_small", () -> ((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)new MuzzleFlashEffect.Builder().withName("muzzle_flash_small")).withTexture("textures/effect/flashes.png")).withDuration(50L)).withBrightness(1)).withSprites(1, 9, 1, AbstractEffect.SpriteAnimationType.RANDOM)).withGlow(true)).withWidthProvider(new Interpolators.ConstantFloatProvider(1.0f))).withAlphaProvider(new Interpolators.EaseOutFloatProvider(0.6f))).withInitialRollProvider(() -> 0.0f));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> MUZZLE_FLASH_BIG = EffectRegistry.register("muzzle_flash_big", () -> ((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)new MuzzleFlashEffect.Builder().withName("muzzle_flash_big")).withTexture("textures/effect/flashes.png")).withDuration(50L)).withBrightness(1)).withSprites(1, 9, 1, AbstractEffect.SpriteAnimationType.RANDOM)).withGlow(true)).withWidthProvider(new Interpolators.ConstantFloatProvider(1.5f))).withAlphaProvider(new Interpolators.EaseOutFloatProvider(0.6f))).withInitialRollProvider(() -> 0.0f));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> MUZZLE_FLASH_BIG_EX = EffectRegistry.register("muzzle_flash_big_ex", () -> ((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)((MuzzleFlashEffect.Builder)new MuzzleFlashEffect.Builder().withName("muzzle_flash_big_ex")).withTexture("textures/effect/flashes.png")).withDuration(50L)).withBrightness(1)).withSprites(1, 9, 1, AbstractEffect.SpriteAnimationType.RANDOM)).withGlow(true)).withWidthProvider(new Interpolators.ConstantFloatProvider(2.5f))).withAlphaProvider(new Interpolators.EaseOutFloatProvider(0.6f))).withInitialRollProvider(() -> 0.0f));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> TRACER = EffectRegistry.register("tracer", () -> ((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)new DetachedProjectileEffect.Builder().withName("tracer")).withTexture("textures/effect/tracers2.png")).withBlendMode(Effect.BlendMode.ADDITIVE)).withDepthTest(true)).withDuration(200L)).withBlades(2, 0.0f, 0.75f).withFace(0.75f, 1.0f).withBladeBrightness(2).withFaceBrightness(2).withRotations(3.0)).withGlow(true)).withSegmentsProvider(new SegmentsProviders.MovingSegmentsProvider(300.0f, 0.0f)).withBladeWidthProvider(new Interpolators.ConstantFloatProvider(0.2f)).withFaceWidthProvider(new Interpolators.ConstantFloatProvider(0.1f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> TRACER_MINIGUN = EffectRegistry.register("tracerminigun", () -> ((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)new DetachedProjectileEffect.Builder().withName("tracerminigun")).withTexture("textures/effect/tracers2.png")).withBlendMode(Effect.BlendMode.ADDITIVE)).withDepthTest(true)).withDuration(100L)).withBlades(2, 0.0f, 0.75f).withFace(0.75f, 1.0f).withBrightness(2).withGlow(true)).withSegmentsProvider(new SegmentsProviders.MovingSegmentsProvider(300.0f, 0.0f)).withBladeWidthProvider(new Interpolators.ConstantFloatProvider(0.3f)).withFaceWidthProvider(new Interpolators.ConstantFloatProvider(0.3f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> TRACER_025 = EffectRegistry.register("tracer_025", () -> ((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)((DetachedProjectileEffect.Builder)new DetachedProjectileEffect.Builder().withName("tracer_025")).withTexture("textures/effect/tracers2.png")).withBlendMode(Effect.BlendMode.ADDITIVE)).withDepthTest(true)).withDuration(200L)).withBlades(2, 0.0f, 0.75f).withFace(0.75f, 1.0f).withBladeBrightness(2).withFaceBrightness(2).withRotations(3.0)).withGlow(true)).withSegmentsProvider(new SegmentsProviders.MovingSegmentsProvider(300.0f, 0.0f)).withBladeWidthProvider(new Interpolators.ConstantFloatProvider(0.1f)).withFaceWidthProvider(new Interpolators.ConstantFloatProvider(0.05f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> LASER = EffectRegistry.register("laser", () -> ((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)new AttachedProjectileEffect.Builder().withName("laser")).withTexture("textures/effect/laser_yellow.png")).withBlendMode(Effect.BlendMode.ADDITIVE)).withDuration(200L).withBlades(2, 0.0f, 1.0f).withRotations(3.0)).withBrightness(1).withGlow(true)).withDepthTest(true)).withSegmentsProvider(new SegmentsProviders.StaticBeamSegmentsProvider()).withWidth(0.8));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> LASER_MUZZLE = EffectRegistry.register("laser_muzzle", () -> ((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)new AttachedProjectileEffect.Builder().withName("laser_muzzle")).withTexture("textures/effect/laser_muzzle.png")).withBlendMode(Effect.BlendMode.ADDITIVE)).withDepthTest(true)).withDuration(1000L).withBrightness(1).withFace(0.0f, 1.0f).withSprites(6, 6, 60, AbstractEffect.SpriteAnimationType.PLAY_ONCE)).withGlow(true)).withSegmentsProvider(new SegmentsProviders.StaticBeamSegmentsProvider()).withFaceWidthProvider(new Interpolators.ConstantFloatProvider(2.0f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> LAUNCHER_MUZZLE = EffectRegistry.register("launcher_muzzle", () -> ((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)new AttachedProjectileEffect.Builder().withName("launcher_muzzle")).withTexture("textures/effect/launcher_muzzle.png")).withBlendMode(Effect.BlendMode.ADDITIVE)).withDepthTest(true)).withDuration(1000L).withBrightness(1).withFace(0.0f, 1.0f).withSprites(8, 8, 70, AbstractEffect.SpriteAnimationType.PLAY_ONCE)).withGlow(true)).withSegmentsProvider(new SegmentsProviders.StaticBeamSegmentsProvider()).withFaceWidthProvider(new Interpolators.ConstantFloatProvider(2.0f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> JAVELIN_MUZZLE = EffectRegistry.register("javelin_muzzle", () -> ((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)new AttachedProjectileEffect.Builder().withName("javelin_muzzle")).withTexture("textures/effect/launcher_muzzle.png")).withBlendMode(Effect.BlendMode.ADDITIVE)).withDepthTest(true)).withDuration(1000L).withBrightness(1).withFace(0.0f, 1.0f).withSprites(8, 8, 70, AbstractEffect.SpriteAnimationType.PLAY_ONCE)).withGlow(true)).withSegmentsProvider(new SegmentsProviders.StaticBeamSegmentsProvider()).withFaceWidthProvider(new Interpolators.ConstantFloatProvider(4.0f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> EXPLOSION = EffectRegistry.register("explosion", () -> ((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)new ImpactEffect.Builder().withName("explosion")).withTexture("textures/effect/explosion_new.png")).withBlendMode(Effect.BlendMode.ADDITIVE)).withDuration(1350L)).withSprites(9, 9, 60, AbstractEffect.SpriteAnimationType.PLAY_ONCE)).withGlow(true)).withDepthTest(true)).withWidthProvider(new Interpolators.ConstantFloatProvider(6.0f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> EXPLOSION_BIG = EffectRegistry.register("explosion_big", () -> ((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)new ImpactEffect.Builder().withName("explosion_big")).withTexture("textures/effect/explosion_new.png")).withBlendMode(Effect.BlendMode.ADDITIVE)).withDuration(1400L)).withSprites(9, 9, 60, AbstractEffect.SpriteAnimationType.PLAY_ONCE)).withGlow(true)).withDepthTest(true)).withWidthProvider(new Interpolators.ConstantFloatProvider(9.0f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> DEBRIS = EffectRegistry.register("debris", () -> ((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)new ImpactEffect.Builder().withName("debris")).withTexture("textures/effect/dirt3x3.png")).withBlendMode(Effect.BlendMode.ADDITIVE)).withDuration(1000L)).withRotations(0.5)).withSprites(3, 3, 0, AbstractEffect.SpriteAnimationType.RANDOM)).withCount(10).withVelocityProvider(VelocityProviders.hemisphereVelocityProvider(3.0, VelocityProviders.Distribution.TIGHT))).withGlow(false)).withDepthTest(true)).withGravity(10.0f)).withAlphaProvider(new Interpolators.AnotherEaseInEaseOutFloatProvider(1.0f, 0.01f, 0.5f))).withWidthProvider(new Interpolators.ConstantFloatProvider(0.2f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> CHEM_TRAIL = EffectRegistry.register("chem_trail", () -> ((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)new TrailEffect.Builder().withName("chem_trail")).withTexture("textures/effect/chem_trail.png")).withBlendMode(Effect.BlendMode.NORMAL)).withDuration(1500L)).withGlow(false)).withDepthTest(true)).withLongitudeOffset(2.0).withAlphaProvider(new Interpolators.EaseOutFloatProvider(1.0f))).withWidthProvider(new Interpolators.ConstantFloatProvider(0.8f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> GRENADE_TRAIL = EffectRegistry.register("grenade_trail", () -> ((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)new TrailEffect.Builder().withName("grenade_trail")).withTexture("textures/effect/chem_trail.png")).withBlendMode(Effect.BlendMode.NORMAL)).withDuration(1500L)).withGlow(false)).withDepthTest(true)).withLongitudeOffset(2.0).withAlphaProvider(new Interpolators.EaseOutFloatProvider(0.4f))).withWidthProvider(new Interpolators.ConstantFloatProvider(0.4f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> JAVELIN_TRAIL = EffectRegistry.register("javelin_trail", () -> ((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)((TrailEffect.Builder)new TrailEffect.Builder().withName("javelin_trail")).withTexture("textures/effect/chem_trail.png")).withBlendMode(Effect.BlendMode.NORMAL)).withDuration(1500L)).withGlow(false)).withDepthTest(true)).withLongitudeOffset(2.0).withAlphaProvider(new Interpolators.EaseOutFloatProvider(1.0f))).withWidthProvider(new Interpolators.ConstantFloatProvider(1.2f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> ROCKET_EXHAUST_PLUME = EffectRegistry.register("rocket_exhaust_plume", () -> ((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)((AttachedProjectileEffect.Builder)new AttachedProjectileEffect.Builder().withName("rocket_exhaust_plume")).withTexture("textures/effect/tracers2.png")).withBlendMode(Effect.BlendMode.ADDITIVE)).withDuration(200L).withBlades(2, 0.0f, 0.75f).withFace(0.75f, 1.0f).withBladeBrightness(1).withFaceBrightness(1).withRotations(3.0)).withGlow(true)).withDepthTest(true)).withSegmentsProvider(new SegmentsProviders.SingleSegmentProvider()).withBladeLengthProvider(() -> 1.3f).withBladeWidthProvider(new Interpolators.ConstantFloatProvider(0.7f)).withFaceWidthProvider(new Interpolators.ConstantFloatProvider(0.7f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> BLOOD = EffectRegistry.register("blood", () -> ((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)new ImpactEffect.Builder().withName("blood")).withTexture("textures/effect/blood.png")).withBlendMode(Effect.BlendMode.NORMAL)).withDuration(2000L)).withRotations(0.0)).withSprites(2, 2, 0, AbstractEffect.SpriteAnimationType.RANDOM)).withCount(new ParticleValueProviders.DamageBasedParticleCountProvider(50, 20.0f)).withVelocityProvider(VelocityProviders.hemisphereVelocityProvider(0.3f, VelocityProviders.Distribution.TIGHT))).withGlow(false)).withDepthTest(true)).withGravity(2.0f)).withAlphaProvider(new Interpolators.AnotherEaseInEaseOutFloatProvider(1.0f, 0.01f, 0.5f))).withWidthProvider(new Interpolators.ConstantFloatProvider(0.15f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> BLOODPURPLE = EffectRegistry.register("bloodpurple", () -> ((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)new ImpactEffect.Builder().withName("bloodpurple")).withTexture("textures/effect/bloodpurple.png")).withBlendMode(Effect.BlendMode.NORMAL)).withDuration(2000L)).withRotations(0.0)).withSprites(2, 2, 0, AbstractEffect.SpriteAnimationType.RANDOM)).withCount(new ParticleValueProviders.DamageBasedParticleCountProvider(50, 20.0f)).withVelocityProvider(VelocityProviders.hemisphereVelocityProvider(0.3f, VelocityProviders.Distribution.TIGHT))).withGlow(false)).withDepthTest(true)).withGravity(2.0f)).withAlphaProvider(new Interpolators.AnotherEaseInEaseOutFloatProvider(1.0f, 0.01f, 0.5f))).withWidthProvider(new Interpolators.ConstantFloatProvider(0.15f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> BLOODGREEN = EffectRegistry.register("bloodgreen", () -> ((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)new ImpactEffect.Builder().withName("bloodgreen")).withTexture("textures/effect/bloodgreen.png")).withBlendMode(Effect.BlendMode.NORMAL)).withDuration(2000L)).withRotations(0.0)).withSprites(2, 2, 0, AbstractEffect.SpriteAnimationType.RANDOM)).withCount(new ParticleValueProviders.DamageBasedParticleCountProvider(50, 20.0f)).withVelocityProvider(VelocityProviders.hemisphereVelocityProvider(0.3f, VelocityProviders.Distribution.TIGHT))).withGlow(false)).withDepthTest(true)).withGravity(2.0f)).withAlphaProvider(new Interpolators.AnotherEaseInEaseOutFloatProvider(1.0f, 0.01f, 0.5f))).withWidthProvider(new Interpolators.ConstantFloatProvider(0.15f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> BLOODBLUE = EffectRegistry.register("bloodblue", () -> ((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)new ImpactEffect.Builder().withName("bloodblue")).withTexture("textures/effect/bloodblue.png")).withBlendMode(Effect.BlendMode.NORMAL)).withDuration(2000L)).withRotations(0.0)).withSprites(2, 2, 0, AbstractEffect.SpriteAnimationType.RANDOM)).withCount(new ParticleValueProviders.DamageBasedParticleCountProvider(50, 20.0f)).withVelocityProvider(VelocityProviders.hemisphereVelocityProvider(0.3f, VelocityProviders.Distribution.TIGHT))).withGlow(false)).withDepthTest(true)).withGravity(2.0f)).withAlphaProvider(new Interpolators.AnotherEaseInEaseOutFloatProvider(1.0f, 0.01f, 0.5f))).withWidthProvider(new Interpolators.ConstantFloatProvider(0.15f)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> BLOOD_MIST_RED = EffectRegistry.register("blood_mist", () -> ((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)new ImpactEffect.Builder().withName("blood_mist")).withTexture("textures/effect/blood2.png")).withBlendMode(Effect.BlendMode.NORMAL)).withDuration(3000L)).withRotations(0.01f)).withSprites(1, 1, 0, AbstractEffect.SpriteAnimationType.RANDOM)).withCount(new ParticleValueProviders.RandomParticleCountProvider(1, 10)).withVelocityProvider(VelocityProviders.sphereVelocityProvider(0.03f, VelocityProviders.Distribution.NORMAL))).withGlow(false)).withDepthTest(true)).withGravity(0.01f)).withAlphaProvider(new Interpolators.AnotherEaseInEaseOutFloatProvider(1.0f, 0.01f, 0.5f))).withWidth(new ParticleValueProviders.BoundingBoxBasedParticleWidthProvider(1.5, 1.0)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> BLOOD_MIST_PURPLE = EffectRegistry.register("blood_mist_purple", () -> ((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)new ImpactEffect.Builder().withName("blood_mist_purple")).withTexture("textures/effect/bloodmistpurple.png")).withBlendMode(Effect.BlendMode.NORMAL)).withDuration(3000L)).withRotations(0.01f)).withSprites(1, 1, 0, AbstractEffect.SpriteAnimationType.RANDOM)).withCount(new ParticleValueProviders.RandomParticleCountProvider(1, 10)).withVelocityProvider(VelocityProviders.sphereVelocityProvider(0.03f, VelocityProviders.Distribution.NORMAL))).withGlow(false)).withDepthTest(true)).withGravity(0.01f)).withAlphaProvider(new Interpolators.AnotherEaseInEaseOutFloatProvider(1.0f, 0.01f, 0.5f))).withWidth(new ParticleValueProviders.BoundingBoxBasedParticleWidthProvider(1.5, 1.0)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> BLOOD_MIST_GREEN = EffectRegistry.register("blood_mist_green", () -> ((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)new ImpactEffect.Builder().withName("blood_mist_green")).withTexture("textures/effect/bloodmistgreen.png")).withBlendMode(Effect.BlendMode.NORMAL)).withDuration(3000L)).withRotations(0.01f)).withSprites(1, 1, 0, AbstractEffect.SpriteAnimationType.RANDOM)).withCount(new ParticleValueProviders.RandomParticleCountProvider(1, 10)).withVelocityProvider(VelocityProviders.sphereVelocityProvider(0.03f, VelocityProviders.Distribution.NORMAL))).withGlow(false)).withDepthTest(true)).withGravity(0.01f)).withAlphaProvider(new Interpolators.AnotherEaseInEaseOutFloatProvider(1.0f, 0.01f, 0.5f))).withWidth(new ParticleValueProviders.BoundingBoxBasedParticleWidthProvider(1.5, 1.0)));
    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> BLOOD_MIST_BLUE = EffectRegistry.register("blood_mist_blue", () -> ((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)((ImpactEffect.Builder)new ImpactEffect.Builder().withName("blood_mist_blue")).withTexture("textures/effect/bloodmistblue.png")).withBlendMode(Effect.BlendMode.NORMAL)).withDuration(3000L)).withRotations(0.01f)).withSprites(1, 1, 0, AbstractEffect.SpriteAnimationType.RANDOM)).withCount(new ParticleValueProviders.RandomParticleCountProvider(1, 10)).withVelocityProvider(VelocityProviders.sphereVelocityProvider(0.03f, VelocityProviders.Distribution.NORMAL))).withGlow(false)).withDepthTest(true)).withGravity(0.01f)).withAlphaProvider(new Interpolators.AnotherEaseInEaseOutFloatProvider(1.0f, 0.01f, 0.5f))).withWidth(new ParticleValueProviders.BoundingBoxBasedParticleWidthProvider(1.5, 1.0)));

    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> register(String name, Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> supplier) {
        if (suppliers.put(name, supplier) != null) {
            throw new IllegalArgumentException("Duplicate effect: " + name);
        }
        UUID effectId = EffectRegistry.getEffectId(name);
        Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> existingEffectSupplier = effectSuppliersById.put(effectId, supplier);
        if (existingEffectSupplier != null) {
            throw new IllegalArgumentException("Effect id collision for effect '" + name + "'. Try assigning a different name");
        }
        return supplier;
    }

    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> getEffectBuilderSupplier(UUID effectId) {
        Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> supplier = effectSuppliersById.get(effectId);
        if (supplier == null) {
            throw new IllegalArgumentException("Effect with id " + String.valueOf(effectId) + " not found");
        }
        return supplier;
    }

    public static UUID getEffectId(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes());
    }

    public static Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> getEffectBuilderSupplier(String name) {
        Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> supplier = suppliers.get(name);
        if (supplier == null) {
            throw new IllegalArgumentException("Effect '" + name + "' not found");
        }
        return supplier;
    }

    private static List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> getEntityEffects(Entity entity, Map<Class<? extends Entity>, List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>>> effects) {
        if (!Config.goreEnabled) {
            return Collections.emptyList();
        }
        List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> exactMatchResults = null;
        List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> baseMatchResults = null;
        for (Map.Entry<Class<Entity>, List<Supplier<EffectBuilder<EffectBuilder<?, ?>, ?>>>> e : effects.entrySet()) {
            if (e.getKey() == entity.getClass()) {
                exactMatchResults = e.getValue();
                break;
            }
            if (baseMatchResults != null || !e.getKey().isAssignableFrom(entity.getClass())) continue;
            baseMatchResults = e.getValue();
        }
        if (exactMatchResults != null) {
            return exactMatchResults;
        }
        if (baseMatchResults != null) {
            return baseMatchResults;
        }
        return Collections.emptyList();
    }

    public static List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> getEntityDeathEffects(LivingEntity entity) {
        return EffectRegistry.getEntityEffects((Entity)entity, entityDeathEffects);
    }

    public static List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> getEntityHitEffects(Entity entity) {
        return EffectRegistry.getEntityEffects(entity, entityHitEffects);
    }

    static {
        entityDeathEffects.put(Player.class, List.of(BLOOD_MIST_RED));
        entityDeathEffects.put(EnderMan.class, List.of(BLOOD_MIST_PURPLE));
        entityDeathEffects.put(Endermite.class, List.of(BLOOD_MIST_PURPLE));
        entityDeathEffects.put(Shulker.class, List.of(BLOOD_MIST_PURPLE));
        entityDeathEffects.put(EnderDragon.class, List.of(BLOOD_MIST_PURPLE));
        entityDeathEffects.put(Slime.class, List.of(BLOOD_MIST_GREEN));
        entityDeathEffects.put(Creeper.class, List.of(BLOOD_MIST_GREEN));
        entityDeathEffects.put(Warden.class, List.of(BLOOD_MIST_BLUE));
        entityDeathEffects.put(LivingEntity.class, List.of(BLOOD_MIST_RED));
        entityDeathEffects.put(Skeleton.class, Collections.emptyList());
        entityHitEffects.put(Player.class, List.of(BLOOD));
        entityHitEffects.put(LivingEntity.class, List.of(BLOOD));
        entityHitEffects.put(EnderMan.class, List.of(BLOODPURPLE));
        entityHitEffects.put(Endermite.class, List.of(BLOODPURPLE));
        entityHitEffects.put(Shulker.class, List.of(BLOODPURPLE));
        entityHitEffects.put(EnderDragon.class, List.of(BLOODPURPLE));
        entityHitEffects.put(Slime.class, List.of(BLOODGREEN));
        entityHitEffects.put(Creeper.class, List.of(BLOODGREEN));
        entityHitEffects.put(Warden.class, List.of(BLOODBLUE));
        entityHitEffects.put(Skeleton.class, Collections.emptyList());
    }
}

