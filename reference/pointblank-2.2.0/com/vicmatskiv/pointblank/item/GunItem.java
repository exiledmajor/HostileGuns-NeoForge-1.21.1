/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonPrimitive
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.util.Tuple
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.BushBlock
 *  net.minecraft.world.level.block.LeavesBlock
 *  net.minecraft.world.level.block.StainedGlassBlock
 *  net.minecraft.world.level.block.StainedGlassPaneBlock
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.jetbrains.annotations.NotNull
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animatable.GeoItem
 *  software.bernie.geckolib.animatable.SingletonGeoAnimatable
 *  software.bernie.geckolib.animatable.client.GeoRenderProvider
 *  software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.animation.AnimatableManager
 *  software.bernie.geckolib.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.animation.Animation
 *  software.bernie.geckolib.animation.AnimationController
 *  software.bernie.geckolib.animation.AnimationController$State
 *  software.bernie.geckolib.animation.ContextAwareAnimatableManager
 *  software.bernie.geckolib.animation.PlayState
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.animation.keyframe.event.data.SoundKeyframeData
 *  software.bernie.geckolib.constant.DataTickets
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package com.vicmatskiv.pointblank.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.Nameable;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.attachment.Attachment;
import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.attachment.AttachmentHost;
import com.vicmatskiv.pointblank.attachment.Attachments;
import com.vicmatskiv.pointblank.client.BiDirectionalInterpolator;
import com.vicmatskiv.pointblank.client.DynamicGeoListener;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.GunHandSwapCoordinator;
import com.vicmatskiv.pointblank.client.GunStatePoseProvider;
import com.vicmatskiv.pointblank.client.LockableTarget;
import com.vicmatskiv.pointblank.client.ShellEjectionLauncher;
import com.vicmatskiv.pointblank.client.controller.AbstractProceduralAnimationController;
import com.vicmatskiv.pointblank.client.controller.BlendingAnimationController;
import com.vicmatskiv.pointblank.client.controller.GlowAnimationController;
import com.vicmatskiv.pointblank.client.controller.GunRandomizingAnimationController;
import com.vicmatskiv.pointblank.client.controller.GunRecoilAnimationController;
import com.vicmatskiv.pointblank.client.controller.GunStateAnimationController;
import com.vicmatskiv.pointblank.client.controller.PlayerRecoilController;
import com.vicmatskiv.pointblank.client.controller.RotationAnimationController;
import com.vicmatskiv.pointblank.client.controller.TimerController;
import com.vicmatskiv.pointblank.client.controller.ViewShakeAnimationController;
import com.vicmatskiv.pointblank.client.controller.ViewShakeAnimationController2;
import com.vicmatskiv.pointblank.client.effect.AbstractEffect;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.EffectLauncher;
import com.vicmatskiv.pointblank.client.effect.MuzzleFlashEffect;
import com.vicmatskiv.pointblank.client.render.GunItemRenderer;
import com.vicmatskiv.pointblank.crafting.Craftable;
import com.vicmatskiv.pointblank.entity.ProjectileLike;
import com.vicmatskiv.pointblank.feature.AccuracyFeature;
import com.vicmatskiv.pointblank.feature.ActiveMuzzleFeature;
import com.vicmatskiv.pointblank.feature.AimingFeature;
import com.vicmatskiv.pointblank.feature.AmmoCapacityFeature;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.DualWieldFeature;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.Features;
import com.vicmatskiv.pointblank.feature.FireModeFeature;
import com.vicmatskiv.pointblank.feature.GlowFeature;
import com.vicmatskiv.pointblank.feature.MuzzleFlashFeature;
import com.vicmatskiv.pointblank.feature.OverheatFeature;
import com.vicmatskiv.pointblank.feature.PipFeature;
import com.vicmatskiv.pointblank.feature.ReloadFeature;
import com.vicmatskiv.pointblank.feature.ReticleFeature;
import com.vicmatskiv.pointblank.feature.SoundFeature;
import com.vicmatskiv.pointblank.item.AmmoItem;
import com.vicmatskiv.pointblank.item.AnimationProvider;
import com.vicmatskiv.pointblank.item.ConditionalAnimationProvider;
import com.vicmatskiv.pointblank.item.FireMode;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.HurtingItem;
import com.vicmatskiv.pointblank.item.ItemExtra;
import com.vicmatskiv.pointblank.network.FireModeRequestPacket;
import com.vicmatskiv.pointblank.network.FireModeResponsePacket;
import com.vicmatskiv.pointblank.network.HitScanFireRequestPacket;
import com.vicmatskiv.pointblank.network.HitScanFireResponsePacket;
import com.vicmatskiv.pointblank.network.ProjectileFireRequestPacket;
import com.vicmatskiv.pointblank.network.ReloadRequestPacket;
import com.vicmatskiv.pointblank.network.ReloadResponsePacket;
import com.vicmatskiv.pointblank.registry.AmmoRegistry;
import com.vicmatskiv.pointblank.registry.EffectRegistry;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import com.vicmatskiv.pointblank.registry.SoundRegistry;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.HitScan;
import com.vicmatskiv.pointblank.util.JsonUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.SimpleHitResult;
import com.vicmatskiv.pointblank.util.TimeUnit;
import com.vicmatskiv.pointblank.util.Tradeable;
import com.vicmatskiv.pointblank.util.TriPredicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.ContextAwareAnimatableManager;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.data.SoundKeyframeData;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GunItem
extends HurtingItem
implements ItemExtra,
Craftable,
AttachmentHost,
Nameable,
GeoItem,
LockableTarget.TargetLocker,
Tradeable {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static final boolean DEBUG_DUAL_WIELD = Boolean.getBoolean("pointblank.debug.dualwield");
    private static final Set<String> DUAL_WIELD_DEBUG_KEYS = Collections.synchronizedSet(new HashSet());
    private static final String DEFAULT_ANIMATION_IDLE = "animation.model.idle";
    private static final String DEFAULT_ANIMATION_RELOAD = "animation.model.reload";
    private static final String DEFAULT_ANIMATION_INSPECT = "animation.model.inspect";
    private static final String DEFAULT_ANIMATION_DRAW = "animation.model.draw";
    public static final String DEFAULT_ANIMATION_HIDE = "animation.model.hide";
    private static final String DEFAULT_ANIMATION_ENABLE_FIRE_MODE = "animation.model.enablefiremode";
    private static final String DEFAULT_ANIMATION_COMPLETE_FIRE = "animation.model.completefire";
    private static final String DEFAULT_ANIMATION_PREPARE_FIRE = "animation.model.preparefire";
    public static final String DEFAULT_ANIMATION_FIRE = "animation.model.fire";
    private static final String DEFAULT_ANIMATION_OVERHEAT = "animation.model.overheat";
    public static final String DUAL_WIELD_LEFT_SUFFIX = "_dwleft";
    public static final String DUAL_WIELD_RIGHT_SUFFIX = "_dwright";
    public static final String DEFAULT_ANIMATION_OFF_GROUND = "animation.model.off_ground";
    public static final String DEFAULT_ANIMATION_OFF_GROUND_SPRINTING = "animation.model.off_ground_sprinting";
    public static final String DEFAULT_ANIMATION_STANDING = "animation.model.standing";
    public static final String DEFAULT_ANIMATION_WALKING = "animation.model.walking";
    public static final String DEFAULT_ANIMATION_CROUCHING = "animation.model.crouching";
    public static final String DEFAULT_ANIMATION_WALKING_AIMING = "animation.model.walking_aiming";
    public static final String DEFAULT_ANIMATION_WALKING_BACKWARDS = "animation.model.walking_backwards";
    public static final String DEFAULT_ANIMATION_WALKING_LEFT = "animation.model.walking_left";
    public static final String DEFAULT_ANIMATION_WALKING_RIGHT = "animation.model.walking_right";
    public static final String DEFAULT_ANIMATION_RUNNING = "animation.model.running";
    public static final String DEFAULT_ANIMATION_PREPARE_RUNNING = "animation.model.runningstart";
    public static final String DEFAULT_ANIMATION_COMPLETE_RUNNING = "animation.model.runningend";
    public static final int INFINITE_AMMO = Integer.MAX_VALUE;
    public static final String DEFAULT_RETICLE_OVERLAY = "textures/item/reticle.png";
    public static final String DEFAULT_RETICLE_OVERLAY_PARALLAX = "textures/item/reticle4.png";
    public static final RawAnimation RAW_ANIMATION_OFF_GROUND = RawAnimation.begin().thenPlay("animation.model.off_ground");
    public static final RawAnimation RAW_ANIMATION_OFF_GROUND_SPRINTING = RawAnimation.begin().thenPlay("animation.model.off_ground_sprinting");
    public static final RawAnimation RAW_ANIMATION_STANDING = RawAnimation.begin().thenPlay("animation.model.standing");
    public static final RawAnimation RAW_ANIMATION_WALKING = RawAnimation.begin().thenPlay("animation.model.walking");
    public static final RawAnimation RAW_ANIMATION_CROUCHING = RawAnimation.begin().thenPlay("animation.model.crouching");
    public static final RawAnimation RAW_ANIMATION_WALKING_AIMING = RawAnimation.begin().thenPlay("animation.model.walking_aiming");
    public static final RawAnimation RAW_ANIMATION_WALKING_BACKWARDS = RawAnimation.begin().thenPlay("animation.model.walking_backwards");
    public static final RawAnimation RAW_ANIMATION_WALKING_LEFT = RawAnimation.begin().thenPlay("animation.model.walking_left");
    public static final RawAnimation RAW_ANIMATION_WALKING_RIGHT = RawAnimation.begin().thenPlay("animation.model.walking_right");
    public static final RawAnimation RAW_ANIMATION_PREPARE_RUNNING = RawAnimation.begin().thenPlay("animation.model.runningstart");
    public static final RawAnimation RAW_ANIMATION_COMPLETE_RUNNING = RawAnimation.begin().thenPlay("animation.model.runningend");
    public static final RawAnimation RAW_ANIMATION_RUNNING = RawAnimation.begin().thenPlay("animation.model.running");
    public static final String CONTROLLER_NAME_IDLE = "idle_controller";
    public static final String CONTROLLER_NAME_FIRE = "fire_controller";
    public static final String CONTROLLER_NAME_WALKING = "walking";
    public static final String CONTROLLER_NAME_DRAW = "draw_controller";
    public static final String CONTROLLER_NAME_HIDE = "hide_controller";
    public static final String CONTROLLER_NAME_OVERHEAT = "overheat_controller";
    private static final List<ResourceLocation> FALLBACK_COMMON_ANIMATIONS = List.of(ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"common"));
    private static final List<ResourceLocation> FALLBACK_PISTOL_ANIMATIONS = List.of(ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"pistol"), ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"common"));
    private static Random random = new Random();
    private static final double MAX_SHOOTING_DISTANCE_WITHOUT_AIMING = 100.0;
    private static final ResourceLocation DEFAULT_SCOPE_OVERLAY = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/gui/scope.png");
    private static final int MAX_ATTACHMENT_CATEGORIES = 11;
    private final String name;
    private float tradePrice;
    private int tradeLevel;
    private int tradeBundleQuantity;
    private int maxAmmoCapacity;
    private boolean requiresPhasedReload;
    private boolean isAimingEnabled;
    private final int rpm;
    private final long prepareIdleCooldownDuration;
    private final long prepareFireCooldownDuration;
    private final long completeFireCooldownDuration;
    private final long enableFireModeCooldownDuration;
    private long targetLockTimeTicks;
    private final Set<Supplier<AmmoItem>> compatibleBullets;
    private double viewRecoilAmplitude;
    private double shakeRecoilAmplitude;
    private int viewRecoilMaxPitch;
    private long viewRecoilDuration;
    private double shakeRecoilSpeed;
    private double shakeDecay;
    private long shakeRecoilDuration;
    private double gunRecoilInitialAmplitude;
    private double gunRecoilRateOfAmplitudeDecay;
    private double gunRecoilInitialAngularFrequency;
    private double gunRecoilRateOfFrequencyIncrease;
    private double gunRecoilPitchMultiplier;
    private long gunRecoilDuration;
    private int shotsPerRecoil;
    private boolean sustainedRecoilEnabled;
    private double sustainedRecoilPitchIncrement;
    private double sustainedRecoilPosYIncrement;
    private double sustainedRecoilPosZIncrement;
    private double sustainedRecoilYawMultiplier;
    private double sustainedRecoilPitchCap;
    private double sustainedRecoilPosYCap;
    private double sustainedRecoilPosZCap;
    private boolean sustainedRecoilOnAim;
    private long sustainedRecoilDecayTime;
    private int shotsPerTrace;
    private long idleRandomizationDuration;
    private long recoilRandomizationDuration;
    private double gunRandomizationAmplitude;
    private int burstShots;
    private SoundEvent fireSound;
    private float fireSoundVolume;
    private SoundEvent targetLockedSound;
    private SoundEvent targetStartLockingSound;
    private long reloadCooldownTime;
    private float bobbing;
    private float bobbingOnAim;
    private float bobbingRollMultiplier;
    private double jumpMultiplier;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private double aimingCurveX;
    private double aimingCurveY;
    private double aimingCurveZ;
    private double aimingCurvePitch;
    private double aimingCurveYaw;
    private double aimingCurveRoll;
    private double aimingZoom;
    private double pipScopeZoom;
    private ResourceLocation scopeOverlay;
    private ResourceLocation targetLockOverlay;
    private String ammoUi;
    private ResourceLocation modelResourceLocation;
    private List<PhasedReload> phasedReloads;
    private AnimationProvider drawAnimationProvider;
    private AnimationProvider hideAnimationProvider;
    private AnimationProvider inspectAnimationProvider;
    private AnimationProvider idleAnimationProvider;
    private volatile GunItemRenderer clientRenderer;
    private final Set<String> missingHideAnimationWarnings = Collections.synchronizedSet(new HashSet());
    private final Set<String> missingOverheatAnimationWarnings = Collections.synchronizedSet(new HashSet());
    private String reloadAnimation;
    private int pelletCount;
    private double pelletSpread;
    private double inaccuracy;
    private double inaccuracyAiming;
    private double inaccuracySprinting;
    private double overheatMaxHeat;
    private double overheatHeatPerShot;
    private double overheatCooldownPerSecond;
    private List<Tuple<Long, AbstractProceduralAnimationController>> reloadEffectControllers;
    private List<GlowAnimationController.Builder> glowEffectBuilders;
    private List<RotationAnimationController.Builder> rotationEffectBuilders;
    private EffectLauncher effectLauncher;
    private float hitScanSpeed;
    private float hitScanAcceleration;
    private float modelScale;
    private float adsZOffset;
    private List<Supplier<Attachment>> compatibleAttachmentSuppliers;
    private List<String> compatibleAttachmentGroups;
    private Set<Attachment> compatibleAttachments;
    private List<Supplier<Attachment>> defaultAttachmentSuppliers;
    private long craftingDuration;
    private Map<Class<? extends Feature>, Feature> features;
    private AnimationType animationType;
    private ResourceLocation firstPersonFallbackAnimations;
    private String thirdPersonFallbackAnimations;

    private GunItem(Builder builder, String namespace) {
        super(new Item.Properties().stacksTo(1), builder);
        ReloadFeature reloadFeature;
        PipFeature pipFeature;
        AimingFeature aimingFeature;
        ReticleFeature reticleFeature;
        MuzzleFlashFeature muzzleFlashFeature;
        FireModeFeature fireModeFeature;
        ActiveMuzzleFeature activeMuzzleFeature;
        this.name = builder.name;
        this.modelResourceLocation = this.name.contains(":") ? ResourceLocation.parse((String)this.name) : ResourceLocation.fromNamespaceAndPath((String)namespace, (String)this.name);
        this.modelScale = builder.modelScale;
        this.tradePrice = builder.tradePrice;
        this.tradeLevel = builder.tradeLevel;
        this.tradeBundleQuantity = builder.tradeBundleQuantity;
        this.maxAmmoCapacity = builder.maxAmmoCapacity;
        this.rpm = builder.rpm;
        this.overheatMaxHeat = builder.overheatMaxHeat;
        this.overheatHeatPerShot = builder.overheatHeatPerShot;
        this.overheatCooldownPerSecond = builder.overheatCooldownPerSecond;
        this.isAimingEnabled = builder.isAimingEnabled;
        this.compatibleBullets = builder.compatibleAmmo != null && builder.compatibleAmmo.size() > 0 ? builder.compatibleAmmo : Collections.emptySet();
        this.targetLockTimeTicks = builder.targetLockTimeTicks;
        this.hitScanSpeed = builder.hitScanSpeed;
        this.hitScanAcceleration = builder.hitScanAcceleration;
        this.viewRecoilAmplitude = builder.viewRecoilAmplitude;
        this.shakeRecoilAmplitude = builder.shakeRecoilAmplitude;
        this.viewRecoilMaxPitch = builder.viewRecoilMaxPitch;
        this.viewRecoilDuration = builder.viewRecoilDuration;
        this.shakeRecoilSpeed = builder.shakeRecoilSpeed;
        this.shakeRecoilDuration = builder.shakeRecoilDuration;
        this.shakeDecay = builder.shakeDecay;
        this.gunRecoilInitialAmplitude = builder.gunRecoilInitialAmplitude;
        this.gunRecoilRateOfAmplitudeDecay = builder.gunRecoilRateOfAmplitudeDecay;
        this.gunRecoilInitialAngularFrequency = builder.gunRecoilInitialAngularFrequency;
        this.gunRecoilRateOfFrequencyIncrease = builder.gunRecoilRateOfFrequencyIncrease;
        this.gunRecoilPitchMultiplier = builder.gunRecoilPitchMultiplier;
        this.gunRecoilDuration = builder.gunRecoilDuration;
        this.shotsPerRecoil = builder.shotsPerRecoil;
        this.sustainedRecoilEnabled = builder.sustainedRecoilEnabled;
        double d = builder.sustainedRecoilEnabled ? (builder.sustainedRecoilPitchIncrement != null ? builder.sustainedRecoilPitchIncrement : 0.0025) : (this.sustainedRecoilPitchIncrement = 0.0);
        double d2 = builder.sustainedRecoilEnabled ? (builder.sustainedRecoilPosYIncrement != null ? builder.sustainedRecoilPosYIncrement : -0.0125) : (this.sustainedRecoilPosYIncrement = 0.0);
        double d3 = builder.sustainedRecoilEnabled ? (builder.sustainedRecoilPosZIncrement != null ? builder.sustainedRecoilPosZIncrement : 0.01) : (this.sustainedRecoilPosZIncrement = 0.0);
        this.sustainedRecoilYawMultiplier = builder.sustainedRecoilEnabled ? (builder.sustainedRecoilYawMultiplier != null ? builder.sustainedRecoilYawMultiplier : 0.05) : 0.0;
        this.sustainedRecoilPitchCap = builder.sustainedRecoilPitchCap != null ? builder.sustainedRecoilPitchCap : 0.01;
        this.sustainedRecoilPosYCap = builder.sustainedRecoilPosYCap != null ? builder.sustainedRecoilPosYCap : -0.05;
        this.sustainedRecoilPosZCap = builder.sustainedRecoilPosZCap != null ? builder.sustainedRecoilPosZCap : 0.08;
        this.sustainedRecoilOnAim = builder.sustainedRecoilOnAim;
        this.sustainedRecoilDecayTime = builder.sustainedRecoilDecayTime != null ? builder.sustainedRecoilDecayTime : 400L;
        this.shotsPerTrace = builder.shotsPerTrace;
        this.gunRandomizationAmplitude = builder.gunRandomizationAmplitude;
        this.idleRandomizationDuration = builder.idleRandomizationDuration;
        this.recoilRandomizationDuration = builder.recoilRandomizationDuration;
        this.jumpMultiplier = builder.jumpMultiplier;
        this.burstShots = builder.burstShots;
        this.fireSound = builder.fireSound != null ? builder.fireSound.get() : null;
        this.fireSoundVolume = builder.fireSoundVolume;
        this.prepareIdleCooldownDuration = builder.prepareIdleCooldownDuration;
        this.prepareFireCooldownDuration = builder.prepareFireCooldownDuration;
        this.completeFireCooldownDuration = builder.completeFireCooldownDuration;
        this.enableFireModeCooldownDuration = builder.enableFireModeCooldownDuration;
        this.craftingDuration = builder.craftingDuration;
        this.aimingCurveX = builder.aimingCurveX;
        this.aimingCurveY = builder.aimingCurveY;
        this.aimingCurveZ = builder.aimingCurveZ;
        this.aimingCurvePitch = builder.aimingCurvePitch;
        this.aimingCurveYaw = builder.aimingCurveYaw;
        this.aimingCurveRoll = builder.aimingCurveRoll;
        this.pipScopeZoom = builder.pipScopeZoom;
        this.aimingZoom = builder.aimingZoom;
        this.scopeOverlay = builder.scopeOverlay != null ? ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)builder.scopeOverlay) : null;
        this.targetLockOverlay = builder.targetLockOverlay != null ? ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)builder.targetLockOverlay) : null;
        this.ammoUi = builder.ammoUi;
        this.adsZOffset = builder.adsZOffset;
        this.targetLockedSound = builder.targetLockedSound != null ? builder.targetLockedSound.get() : null;
        this.targetStartLockingSound = builder.targetStartLockingSound != null ? builder.targetStartLockingSound.get() : null;
        this.bobbing = builder.bobbing;
        this.bobbingOnAim = builder.bobbingOnAim;
        this.bobbingRollMultiplier = builder.bobbingRollMultiplier;
        this.reloadEffectControllers = Collections.unmodifiableList(builder.reloadEffectControllers);
        this.phasedReloads = builder.phasedReloads;
        if (!builder.drawAnimationsBuilder.getAnimations().isEmpty()) {
            this.drawAnimationProvider = builder.drawAnimationsBuilder.build();
        }
        if (!builder.hideAnimationsBuilder.getAnimations().isEmpty()) {
            this.hideAnimationProvider = builder.hideAnimationsBuilder.build();
        }
        if (!builder.inspectAnimationsBuilder.getAnimations().isEmpty()) {
            this.inspectAnimationProvider = builder.inspectAnimationsBuilder.build();
        }
        if (!builder.idleAnimationBuilder.getAnimations().isEmpty()) {
            this.idleAnimationProvider = builder.idleAnimationBuilder.build();
        }
        this.animationType = builder.animationType;
        if (builder.firstPersonFallbackAnimations != null) {
            this.firstPersonFallbackAnimations = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)builder.firstPersonFallbackAnimations);
        }
        this.thirdPersonFallbackAnimations = builder.thirdPersonFallbackAnimations;
        this.pelletSpread = builder.pelletSpread;
        if (builder.pelletCount > 1) {
            this.maxShootingDistance = Math.min(this.maxShootingDistance, 50.0);
        }
        this.inaccuracyAiming = builder.inaccuracyAiming;
        this.inaccuracy = builder.inaccuracy;
        this.inaccuracySprinting = builder.inaccuracySprinting;
        this.reloadCooldownTime = builder.reloadCooldownTime;
        this.reloadAnimation = builder.reloadAnimation;
        if (this.phasedReloads.isEmpty() && this.reloadAnimation != null) {
            this.phasedReloads.add(new PhasedReload(ReloadPhase.RELOADING, this.reloadCooldownTime, this.reloadAnimation));
        } else {
            this.requiresPhasedReload = true;
        }
        this.compatibleAttachmentSuppliers = Collections.unmodifiableList(builder.compatibleAttachments);
        this.compatibleAttachmentGroups = Collections.unmodifiableList(builder.compatibleAttachmentGroups);
        this.defaultAttachmentSuppliers = Collections.unmodifiableList(builder.defaultAttachments);
        HashMap features = new HashMap();
        for (FeatureBuilder<?, ?> featureBuilder : builder.featureBuilders) {
            Object feature = featureBuilder.build(this);
            features.put(feature.getClass(), feature);
        }
        if (!features.containsKey(DualWieldFeature.class) && builder.dualWielding) {
            features.put(DualWieldFeature.class, new DualWieldFeature.Builder().build(this));
        }
        if ((activeMuzzleFeature = (ActiveMuzzleFeature)features.get(ActiveMuzzleFeature.class)) == null) {
            ActiveMuzzleFeature.Builder activeMuzzleFeatureBuilder = new ActiveMuzzleFeature.Builder().withCondition(Conditions.isUsingDefaultMuzzle().and(Conditions.doesNotHaveAttachmentInCategory(AttachmentCategory.MUZZLE)));
            features.put(ActiveMuzzleFeature.class, activeMuzzleFeatureBuilder.build(this));
        }
        if ((fireModeFeature = (FireModeFeature)features.get(FireModeFeature.class)) == null) {
            FireModeFeature.Builder fireModeFeatureBuilder = new FireModeFeature.Builder();
            if (builder.fireModes != null) {
                for (FireMode fireMode : builder.fireModes) {
                    AnimationProvider fireAnimationProvider = builder.fireAnimationsBuilder.build();
                    fireModeFeatureBuilder.withFireMode(new FireModeFeature.FireModeDescriptor.Builder().withName(fireMode.name()).withType(fireMode).withDisplayName((Component)Component.translatable((String)String.format("label.%s.fireMode.%s", "pointblank", fireMode.name().toLowerCase()))).withMaxAmmoCapacity(this.maxAmmoCapacity).withRpm(builder.rpm).withBurstShots(builder.burstShots).withDamage(this.getDamage()).withMaxShootingDistance((int)this.maxShootingDistance).withPelletCount(builder.pelletCount).withPelletSpread(builder.pelletSpread).withIsUsingDefaultMuzzle(true).withFireAnimationProvider(fireAnimationProvider).build());
                }
            }
            features.put(FireModeFeature.class, fireModeFeatureBuilder.build(this));
        }
        if ((muzzleFlashFeature = (MuzzleFlashFeature)features.get(MuzzleFlashFeature.class)) == null) {
            MuzzleFlashFeature.Builder muzzleFlashFeatureBulder = new MuzzleFlashFeature.Builder();
            List<Supplier<EffectBuilder<EffectBuilder<?, ?>, ?>>> fbsl = builder.effectBuilders.get((Object)FirePhase.FIRING);
            if (fbsl != null) {
                for (Supplier<EffectBuilder<EffectBuilder<?, ?>, ?>> s : fbsl) {
                    EffectBuilder<? extends EffectBuilder<?, ?>, ?> eb = s.get();
                    if (!(eb instanceof MuzzleFlashEffect.Builder)) continue;
                    muzzleFlashFeatureBulder.withEffect(FirePhase.FIRING, s);
                }
            }
            muzzleFlashFeature = muzzleFlashFeatureBulder.build(this);
            features.put(MuzzleFlashFeature.class, muzzleFlashFeature);
        }
        if ((reticleFeature = (ReticleFeature)features.get(ReticleFeature.class)) == null && builder.reticleOverlay != null && !MiscUtil.isGreaterThanZero(builder.pipScopeZoom)) {
            features.put(ReticleFeature.class, new ReticleFeature.Builder().withTexture(builder.reticleOverlay).build(this));
        }
        if ((aimingFeature = (AimingFeature)features.get(AimingFeature.class)) == null && builder.isAimingEnabled) {
            features.put(AimingFeature.class, new AimingFeature.Builder().withZoom(this.aimingZoom).build(this));
        }
        if ((pipFeature = (PipFeature)features.get(PipFeature.class)) == null && MiscUtil.isGreaterThanZero(builder.pipScopeZoom)) {
            features.put(PipFeature.class, new PipFeature.Builder().withZoom(builder.pipScopeZoom).withOverlayTexture(builder.reticleOverlay).build(this));
        }
        if ((reloadFeature = (ReloadFeature)features.get(ReloadFeature.class)) == null) {
            features.put(ReloadFeature.class, new ReloadFeature.Builder().withMaxAmmoPerReloadIteration(builder.maxAmmoPerReloadIteration).build(this));
        }
        this.glowEffectBuilders = builder.glowEffectBuilders;
        this.rotationEffectBuilders = builder.rotationEffectBuilders;
        this.effectLauncher = new EffectLauncher(builder.effectBuilders);
        if (!this.glowEffectBuilders.isEmpty() && !features.containsKey(GlowFeature.class)) {
            features.put(GlowFeature.class, new GlowFeature());
        }
        this.features = Collections.unmodifiableMap(features);
        SingletonGeoAnimatable.registerSyncedAnimatable((GeoAnimatable)this);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Component getName(ItemStack itemStack) {
        return Component.translatable((String)this.getDescriptionId(itemStack));
    }

    public float getModelScale() {
        return this.modelScale;
    }

    public float getAdsZOffset() {
        return this.adsZOffset;
    }

    @Override
    public Collection<Feature> getFeatures() {
        return this.features.values();
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component)Component.translatable((String)"label.pointblank.damage").append(": ").append(String.format("%.2f", Float.valueOf(this.damage))).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC));
        tooltip.add((Component)Component.translatable((String)"label.pointblank.rpm").append(": ").append(String.format("%d", this.rpm)).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC));
        MutableComponent ammoDescription = Component.translatable((String)"label.pointblank.ammo").append(": ");
        boolean isFirst = true;
        for (Supplier<AmmoItem> ammoItemSupplier : this.compatibleBullets) {
            AmmoItem ammoItem;
            if (!isFirst) {
                ammoDescription.append(", ");
            }
            if ((ammoItem = ammoItemSupplier.get()) != null) {
                ammoDescription.append((Component)Component.translatable((String)ammoItemSupplier.get().getDescriptionId()));
            } else {
                ammoDescription.append((Component)Component.translatable((String)"missing_ammo"));
            }
            isFirst = false;
        }
        ammoDescription.withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC);
        tooltip.add((Component)ammoDescription);
        if (this.isDualWieldingEnabled(stack)) {
            tooltip.add((Component)Component.translatable((String)"label.pointblank.akimbo").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
            tooltip.add((Component)Component.translatable((String)"label.pointblank.offhandCapable").withStyle(ChatFormatting.DARK_AQUA).withStyle(ChatFormatting.ITALIC));
        }
    }

    public MutableComponent getDisplayName() {
        return Component.translatable((String)(this.getDescriptionId() + ".desc")).withStyle(ChatFormatting.YELLOW);
    }

    public boolean requiresPhasedReload() {
        return this.requiresPhasedReload;
    }

    public List<FireModeInstance> getMainFireModes() {
        FireModeFeature fireModeFeature = this.getFeature(FireModeFeature.class);
        return fireModeFeature.getFireModes();
    }

    public int getRpm() {
        return this.rpm;
    }

    public boolean isAimingEnabled() {
        return this.isAimingEnabled;
    }

    public boolean isDualWieldingEnabled() {
        return this.getFeature(DualWieldFeature.class) != null;
    }

    public boolean isDualWieldingEnabled(ItemStack itemStack) {
        return DualWieldFeature.isEnabledForStack(itemStack);
    }

    public String resolveDualWieldAnimationName(LivingEntity entity, GunClientState state, ItemStack stack, String animationName) {
        Player player;
        String baseName;
        block11: {
            block10: {
                if (animationName == null) {
                    return null;
                }
                baseName = GunItem.stripDualWieldAnimationSuffix(animationName);
                if (!(entity instanceof Player)) break block10;
                player = (Player)entity;
                if (stack != null && !stack.isEmpty() && this.isDualWieldingEnabled(stack)) break block11;
            }
            return baseName;
        }
        OperableGunContext context = GunItem.resolveRenderableContextForStack(player, stack);
        if (context == null && state != null) {
            OperableGunContext main = GunItem.resolveMainHandGunContext(player);
            OperableGunContext offhand = GunItem.resolveOffhandGunContext(player);
            if (offhand != null && GunClientState.getState(player, offhand.itemStack(), offhand.slotIndex(), true) == state) {
                context = offhand;
            } else if (main != null && GunClientState.getState(player, main.itemStack(), main.slotIndex(), false) == state) {
                context = main;
            }
        }
        if (context == null) {
            return baseName;
        }
        if (context.offhand()) {
            return baseName + DUAL_WIELD_LEFT_SUFFIX;
        }
        return GunItem.shouldUseRightHandOnlyMainGrip(player, context) ? baseName + DUAL_WIELD_RIGHT_SUFFIX : baseName;
    }

    public static String stripDualWieldAnimationSuffix(String animationName) {
        if (animationName == null) {
            return null;
        }
        if (animationName.endsWith(DUAL_WIELD_LEFT_SUFFIX)) {
            return animationName.substring(0, animationName.length() - DUAL_WIELD_LEFT_SUFFIX.length());
        }
        if (animationName.endsWith(DUAL_WIELD_RIGHT_SUFFIX)) {
            return animationName.substring(0, animationName.length() - DUAL_WIELD_RIGHT_SUFFIX.length());
        }
        return animationName;
    }

    public long getPrepareFireCooldownDuration() {
        return this.prepareFireCooldownDuration;
    }

    public long getCompleteFireCooldownDuration() {
        return this.completeFireCooldownDuration;
    }

    public long getEnableFireModeCooldownDuration() {
        return this.enableFireModeCooldownDuration;
    }

    public long getPrepareIdleCooldownDuration() {
        return this.prepareIdleCooldownDuration;
    }

    public int getPelletCount() {
        return this.pelletCount;
    }

    public double getPelletSpread() {
        return this.pelletSpread;
    }

    public long getDrawCooldownDuration(LivingEntity player, GunClientState state, ItemStack itemStack) {
        if (this.drawAnimationProvider == null) {
            return 0L;
        }
        AnimationProvider.Descriptor descriptor = this.drawAnimationProvider.getDescriptor(player, itemStack, state);
        return descriptor != null ? descriptor.timeUnit().toMillis(descriptor.duration()) : 0L;
    }

    public AnimationProvider.Descriptor getHideAnimationDescriptor(LivingEntity player, GunClientState state, ItemStack itemStack) {
        return this.hideAnimationProvider != null ? this.hideAnimationProvider.getDescriptor(player, itemStack, state) : null;
    }

    public HideAnimationSelection resolveHideAnimation(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Animation animation;
        AnimationProvider.Descriptor descriptor = this.getHideAnimationDescriptor(player, state, itemStack);
        String animationName = descriptor != null && descriptor.animationName() != null ? descriptor.animationName() : DEFAULT_ANIMATION_HIDE;
        animationName = this.resolveDualWieldAnimationName(player, state, itemStack, animationName);
        if (descriptor != null && descriptor.duration() >= 0L) {
            return new HideAnimationSelection(animationName, Math.max(0L, descriptor.timeUnit().toMillis(descriptor.duration())));
        }
        Animation animation2 = animation = this.clientRenderer != null ? this.clientRenderer.getGeoModel().getAnimation((GeoAnimatable)this, animationName) : null;
        if (animation != null) {
            return new HideAnimationSelection(animationName, Math.max(0L, Math.round(animation.length() * 50.0)));
        }
        if (DEFAULT_ANIMATION_HIDE.equals(animationName)) {
            return new HideAnimationSelection(animationName, 400L);
        }
        if (this.clientRenderer != null && this.missingHideAnimationWarnings.add(animationName)) {
            LOGGER.warn("Missing custom hide animation {} for gun {}; allowing the swap to recover immediately", (Object)animationName, (Object)this.name);
        }
        return new HideAnimationSelection(animationName, 0L);
    }

    public String getHideAnimationName(LivingEntity player, GunClientState state, ItemStack itemStack) {
        return this.resolveHideAnimation(player, state, itemStack).animationName();
    }

    public long getHideCooldownDuration(LivingEntity player, GunClientState state, ItemStack itemStack) {
        return this.resolveHideAnimation(player, state, itemStack).durationMillis();
    }

    public long getInspectCooldownDuration(LivingEntity player, GunClientState state, ItemStack itemStack) {
        if (this.inspectAnimationProvider == null) {
            return 0L;
        }
        AnimationProvider.Descriptor descriptor = this.inspectAnimationProvider.getDescriptor(player, itemStack, state);
        return descriptor != null ? descriptor.timeUnit().toMillis(descriptor.duration()) : 0L;
    }

    public boolean supportsOverheating(ItemStack itemStack) {
        OverheatFeature overheatFeature = OverheatFeature.get(itemStack);
        if (overheatFeature != null) {
            return overheatFeature.isConfigured();
        }
        return this.overheatMaxHeat > 0.0 && this.overheatHeatPerShot > 0.0;
    }

    public double getOverheatMaxHeat(ItemStack itemStack) {
        OverheatFeature overheatFeature = OverheatFeature.get(itemStack);
        if (overheatFeature != null) {
            return overheatFeature.getMaxHeat();
        }
        return this.overheatMaxHeat;
    }

    public double getOverheatHeatPerShot(ItemStack itemStack) {
        OverheatFeature overheatFeature = OverheatFeature.get(itemStack);
        if (overheatFeature != null) {
            return overheatFeature.getHeatPerShot();
        }
        return this.overheatHeatPerShot;
    }

    public double getOverheatCooldownPerSecond(ItemStack itemStack) {
        OverheatFeature overheatFeature = OverheatFeature.get(itemStack);
        if (overheatFeature != null) {
            return overheatFeature.getCooldownPerSecond();
        }
        return this.overheatCooldownPerSecond;
    }

    public long getOverheatCooldownDelayMillis(ItemStack itemStack) {
        OverheatFeature overheatFeature = OverheatFeature.get(itemStack);
        return overheatFeature != null ? overheatFeature.getCooldownDelayMillis() : 500L;
    }

    public long getOverheatAnimationDurationMillis(ItemStack itemStack) {
        Animation animation;
        if (!this.supportsOverheating(itemStack)) {
            return 0L;
        }
        Animation animation2 = animation = this.clientRenderer != null ? this.clientRenderer.getGeoModel().getAnimation((GeoAnimatable)this, DEFAULT_ANIMATION_OVERHEAT) : null;
        if (animation != null) {
            return Math.max(0L, Math.round(animation.length() * 50.0));
        }
        if (this.missingOverheatAnimationWarnings.add(DEFAULT_ANIMATION_OVERHEAT)) {
            LOGGER.warn("Missing overheat animation {} for gun {}; using a 1000ms fallback lockout", (Object)DEFAULT_ANIMATION_OVERHEAT, (Object)this.name);
        }
        return 1000L;
    }

    public long getIdleCooldownDuration(LivingEntity player, GunClientState state, ItemStack itemStack) {
        if (this.idleAnimationProvider == null) {
            return 0L;
        }
        AnimationProvider.Descriptor descriptor = this.idleAnimationProvider.getDescriptor(player, itemStack, state);
        return descriptor != null ? descriptor.timeUnit().toMillis(descriptor.duration()) : 0L;
    }

    public long getReloadingCooldownTime(ReloadPhase phase, LivingEntity player, GunClientState state, ItemStack itemStack) {
        long cooldownTime = 0L;
        for (PhasedReload reload : this.phasedReloads) {
            if (phase != reload.phase || !reload.predicate.test(new ConditionContext(player, itemStack, state, null))) continue;
            cooldownTime = reload.timeUnit.toMillis(reload.cooldownTime);
            break;
        }
        return cooldownTime;
    }

    public int getBurstShots(ItemStack itemStack, FireModeInstance fireModeInstance) {
        FireModeFeature mainFireModeFeature = this.getFeature(FireModeFeature.class);
        if (mainFireModeFeature.getFireModes().contains(fireModeInstance)) {
            int burstShots = fireModeInstance.getBurstShots() == -1 ? this.burstShots : fireModeInstance.getBurstShots();
            return burstShots;
        }
        List<FireModeInstance> allFireModes = GunItem.getFireModes(itemStack);
        if (allFireModes.contains(fireModeInstance)) {
            return fireModeInstance.getBurstShots();
        }
        return this.burstShots;
    }

    public int getMaxAmmoCapacity(ItemStack itemStack, FireModeInstance fireModeInstance) {
        FireModeFeature mainFireModeFeature = this.getFeature(FireModeFeature.class);
        if (mainFireModeFeature.getFireModes().contains(fireModeInstance)) {
            int ammoCapacity = fireModeInstance.getAmmo() == AmmoRegistry.DEFAULT_AMMO_POOL.get() ? this.maxAmmoCapacity : fireModeInstance.getMaxAmmoCapacity();
            return AmmoCapacityFeature.modifyAmmoCapacity(itemStack, ammoCapacity);
        }
        List<FireModeInstance> allFireModes = GunItem.getFireModes(itemStack);
        if (allFireModes.contains(fireModeInstance)) {
            int ammoCapacity = fireModeInstance.getAmmo() == AmmoRegistry.DEFAULT_AMMO_POOL.get() ? this.maxAmmoCapacity : fireModeInstance.getMaxAmmoCapacity();
            return AmmoCapacityFeature.modifyAmmoCapacity(itemStack, ammoCapacity);
        }
        return 0;
    }

    public ResourceLocation getScopeOverlay() {
        if (this.scopeOverlay == null && MiscUtil.isGreaterThanZero(this.pipScopeZoom) && !Config.pipScopesEnabled) {
            return DEFAULT_SCOPE_OVERLAY;
        }
        return this.scopeOverlay;
    }

    public String getAmmoUi() {
        return this.ammoUi;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider(){
            private GunItemRenderer renderer;

            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    List<Object> fallbackAnimations;
                    if (GunItem.this.firstPersonFallbackAnimations != null) {
                        fallbackAnimations = new ArrayList<ResourceLocation>();
                        fallbackAnimations.add(GunItem.this.firstPersonFallbackAnimations);
                        fallbackAnimations.addAll(GunItem.this.animationType.getFallbackFirstPersonAnimations());
                    } else {
                        fallbackAnimations = GunItem.this.animationType.getFallbackFirstPersonAnimations();
                    }
                    GunItem.this.clientRenderer = this.renderer = new GunItemRenderer(GunItem.this.modelResourceLocation, fallbackAnimations, GunItem.this.glowEffectBuilders);
                }
                return this.renderer;
            }
        });
    }

    public int getDefaultMaxStackSize() {
        return 1;
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return true;
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity entity, InteractionHand hand) {
        return InteractionResult.SUCCESS;
    }

    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return true;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new BlendingAnimationController<GunItem>(this, CONTROLLER_NAME_WALKING, 2, false, state -> {
            ItemDisplayContext perspective = (ItemDisplayContext)state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE);
            return perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? PlayState.CONTINUE : PlayState.STOP;
        }).withTransition(DEFAULT_ANIMATION_STANDING, DEFAULT_ANIMATION_WALKING, 2, false).withTransition(DEFAULT_ANIMATION_STANDING, DEFAULT_ANIMATION_WALKING_BACKWARDS, 2, false).withTransition(DEFAULT_ANIMATION_STANDING, DEFAULT_ANIMATION_PREPARE_RUNNING, 2, false).withTransition(DEFAULT_ANIMATION_WALKING, DEFAULT_ANIMATION_PREPARE_RUNNING, 2, false).withTransition(DEFAULT_ANIMATION_PREPARE_RUNNING, DEFAULT_ANIMATION_RUNNING, 2, false).withTransition(DEFAULT_ANIMATION_PREPARE_RUNNING, DEFAULT_ANIMATION_COMPLETE_RUNNING, 3, false).withTransition(DEFAULT_ANIMATION_RUNNING, DEFAULT_ANIMATION_COMPLETE_RUNNING, 2, false).withTransition(DEFAULT_ANIMATION_COMPLETE_RUNNING, DEFAULT_ANIMATION_WALKING, 3, false).withTransition(DEFAULT_ANIMATION_COMPLETE_RUNNING, DEFAULT_ANIMATION_STANDING, 3, false).withTransition(DEFAULT_ANIMATION_COMPLETE_RUNNING, DEFAULT_ANIMATION_PREPARE_RUNNING, 2, false).withSpeedProvider((p, c) -> {
            double baseSpeed;
            double speed = p.getSpeed();
            AttributeInstance attribute = p.getAttribute(Attributes.MOVEMENT_SPEED);
            double d = baseSpeed = attribute != null ? attribute.getBaseValue() : speed;
            if (baseSpeed == 0.0) {
                baseSpeed = speed;
            }
            if (p.isSprinting()) {
                baseSpeed += baseSpeed * 0.3;
            }
            double ratio = speed / baseSpeed;
            if (((LocalPlayer)p).isMovingSlowly() || p.isSwimming() || p.isInWater()) {
                ratio *= 0.6;
            }
            return Math.sqrt(Mth.clamp((double)ratio, (double)0.1, (double)10.0));
        }).triggerableAnim(DEFAULT_ANIMATION_WALKING, RAW_ANIMATION_WALKING).triggerableAnim(DEFAULT_ANIMATION_CROUCHING, RAW_ANIMATION_CROUCHING).triggerableAnim(DEFAULT_ANIMATION_WALKING_AIMING, RAW_ANIMATION_WALKING_AIMING).triggerableAnim(DEFAULT_ANIMATION_WALKING_BACKWARDS, RAW_ANIMATION_WALKING_BACKWARDS).triggerableAnim(DEFAULT_ANIMATION_WALKING_LEFT, RAW_ANIMATION_WALKING_LEFT).triggerableAnim(DEFAULT_ANIMATION_WALKING_RIGHT, RAW_ANIMATION_WALKING_RIGHT).triggerableAnim(DEFAULT_ANIMATION_PREPARE_RUNNING, RAW_ANIMATION_PREPARE_RUNNING).triggerableAnim(DEFAULT_ANIMATION_RUNNING, RAW_ANIMATION_RUNNING).triggerableAnim(DEFAULT_ANIMATION_COMPLETE_RUNNING, RAW_ANIMATION_COMPLETE_RUNNING).triggerableAnim(DEFAULT_ANIMATION_STANDING, RAW_ANIMATION_STANDING).triggerableAnim(DEFAULT_ANIMATION_OFF_GROUND, RAW_ANIMATION_OFF_GROUND).triggerableAnim(DEFAULT_ANIMATION_OFF_GROUND_SPRINTING, RAW_ANIMATION_OFF_GROUND_SPRINTING).setSoundKeyframeHandler(event -> {
            SoundKeyframeData soundKeyframeData;
            String soundName;
            SoundEvent soundEvent;
            Player player = ClientUtil.getClientPlayer();
            if (player != null && (soundEvent = SoundRegistry.getSoundEvent(soundName = (soundKeyframeData = event.getKeyframeData()).getSound())) != null) {
                player.playSound(soundEvent, 1.0f, 1.0f);
            }
        }));
        List<GunStateAnimationController> reloadAnimationControllers = this.createReloadAnimationControllers();
        for (GunStateAnimationController reloadAnimationController : reloadAnimationControllers) {
            controllers.add((AnimationController)reloadAnimationController);
        }
        GunStateAnimationController fireAnimationController = new GunStateAnimationController(this, CONTROLLER_NAME_FIRE, DEFAULT_ANIMATION_FIRE, ctx -> ctx.gunClientState().getFireState() == GunClientState.FireState.FIRE_SINGLE || ctx.gunClientState().getFireState() == GunClientState.FireState.FIRE_AUTO || ctx.gunClientState().getFireState() == GunClientState.FireState.FIRE_BURST || ctx.gunClientState().getFireState() == GunClientState.FireState.FIRE_COOLDOWN_SINGLE || ctx.gunClientState().getFireState() == GunClientState.FireState.FIRE_COOLDOWN_AUTO || ctx.gunClientState().getFireState() == GunClientState.FireState.FIRE_COOLDOWN_BURST || this.completeFireCooldownDuration == 0L && ctx.gunClientState().isIdle() && !ctx.gunClientState().isChangingFireMode() && !this.hasIdleAnimations()){
            private long fireAnimStartNanos;

            @Override
            protected boolean shouldContinueAnimation(GunClientState gunClientState) {
                if (gunClientState.isChangingFireMode() || gunClientState.isReloading() || gunClientState.isInspecting() || gunClientState.isDrawing() || gunClientState.isOverheating()) {
                    return false;
                }
                if (GunItem.this.hasIdleAnimations() && gunClientState.isIdle() && this.getCurrentAnimation() != null) {
                    double animLengthSeconds = this.getCurrentAnimation().animation().length();
                    double elapsedSeconds = (double)(System.nanoTime() - this.fireAnimStartNanos) / 1.0E9;
                    if (elapsedSeconds >= animLengthSeconds) {
                        return false;
                    }
                }
                return this.getAnimationState() == AnimationController.State.RUNNING;
            }

            @Override
            public void onStartFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
                this.fireAnimStartNanos = System.nanoTime();
                if (ClientUtil.isFirstPerson(player)) {
                    this.scheduleReset(player, state, itemStack, FireModeFeature.getFireAnimation(player, state, itemStack));
                }
            }
        };
        fireAnimationController.setSoundKeyframeHandler(event -> {
            SoundKeyframeData soundKeyframeData;
            String soundName;
            SoundEvent soundEvent;
            Player player = ClientUtil.getClientPlayer();
            if (player != null && (soundEvent = SoundRegistry.getSoundEvent(soundName = (soundKeyframeData = event.getKeyframeData()).getSound())) != null) {
                player.playSound(soundEvent, this.fireSoundVolume, 1.0f);
            }
        });
        controllers.add((AnimationController)fireAnimationController);
        GunStateAnimationController prepareFiringAnimationController = new GunStateAnimationController(this, this, "prepare_fire_controller", DEFAULT_ANIMATION_PREPARE_FIRE, ctx -> ctx.gunClientState().isPreparingFiring()){

            @Override
            public void onPrepareFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
                if (ClientUtil.isFirstPerson(player)) {
                    this.scheduleReset(player, state, itemStack, FireModeFeature.getPrepareFireAnimation(player, state, itemStack));
                }
            }
        };
        prepareFiringAnimationController.setSoundKeyframeHandler(event -> {
            SoundKeyframeData soundKeyframeData;
            String soundName;
            SoundEvent soundEvent;
            Player player = ClientUtil.getClientPlayer();
            if (player != null && (soundEvent = SoundRegistry.getSoundEvent(soundName = (soundKeyframeData = event.getKeyframeData()).getSound())) != null) {
                player.playSound(soundEvent, 1.0f, 1.0f);
            }
        });
        controllers.add((AnimationController)prepareFiringAnimationController);
        GunStateAnimationController completeFiringAnimationController = new GunStateAnimationController(this, this, "complete_fire_controller", DEFAULT_ANIMATION_COMPLETE_FIRE, ctx -> ctx.gunClientState().isCompletingFiring()){

            @Override
            public void onCompleteFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
                if (ClientUtil.isFirstPerson(player)) {
                    this.scheduleReset(player, state, itemStack, FireModeFeature.getCompleteFireAnimation(player, state, itemStack));
                }
            }
        };
        completeFiringAnimationController.setSoundKeyframeHandler(event -> {
            SoundKeyframeData soundKeyframeData;
            String soundName;
            SoundEvent soundEvent;
            Player player = ClientUtil.getClientPlayer();
            if (player != null && (soundEvent = SoundRegistry.getSoundEvent(soundName = (soundKeyframeData = event.getKeyframeData()).getSound())) != null) {
                player.playSound(soundEvent, 1.0f, 1.0f);
            }
        });
        controllers.add((AnimationController)completeFiringAnimationController);
        GunStateAnimationController overheatAnimationController = new GunStateAnimationController(this, this, CONTROLLER_NAME_OVERHEAT, DEFAULT_ANIMATION_OVERHEAT, ctx -> ctx.gunClientState().isOverheating()){

            @Override
            public void onOverheating(LivingEntity player, GunClientState state, ItemStack itemStack) {
                if (ClientUtil.isFirstPerson(player)) {
                    this.scheduleReset(player, state, itemStack, GunItem.DEFAULT_ANIMATION_OVERHEAT);
                }
            }
        };
        overheatAnimationController.setSoundKeyframeHandler(event -> {
            SoundKeyframeData soundKeyframeData;
            String soundName;
            SoundEvent soundEvent;
            Player player = ClientUtil.getClientPlayer();
            if (player != null && (soundEvent = SoundRegistry.getSoundEvent(soundName = (soundKeyframeData = event.getKeyframeData()).getSound())) != null) {
                player.playSound(soundEvent, 1.0f, 1.0f);
            }
        });
        controllers.add((AnimationController)overheatAnimationController);
        GunStateAnimationController drawAnimationController = new GunStateAnimationController(this, CONTROLLER_NAME_DRAW, DEFAULT_ANIMATION_DRAW, ctx -> ctx.gunClientState().isDrawing()){

            @Override
            protected boolean shouldContinueAnimation(GunClientState gunClientState) {
                if (gunClientState.isReloading() || gunClientState.isFiring() || gunClientState.isInspecting() || gunClientState.isOverheating() || gunClientState.isChangingFireMode()) {
                    return false;
                }
                return this.getAnimationState() == AnimationController.State.RUNNING;
            }

            private String getAnimationName(LivingEntity player, GunClientState state, ItemStack itemStack) {
                if (GunItem.this.drawAnimationProvider == null) {
                    return null;
                }
                AnimationProvider.Descriptor descriptor = GunItem.this.drawAnimationProvider.getDescriptor(player, itemStack, state);
                return descriptor != null ? descriptor.animationName() : null;
            }

            @Override
            public void onDrawing(LivingEntity player, GunClientState state, ItemStack itemStack) {
                if (ClientUtil.isFirstPerson(player)) {
                    this.scheduleReset(player, state, itemStack, this.getAnimationName(player, state, itemStack));
                }
            }
        };
        drawAnimationController.setSoundKeyframeHandler(event -> {
            SoundKeyframeData soundKeyframeData;
            String soundName;
            SoundEvent soundEvent;
            Player player = ClientUtil.getClientPlayer();
            if (player != null && (soundEvent = SoundRegistry.getSoundEvent(soundName = (soundKeyframeData = event.getKeyframeData()).getSound())) != null) {
                player.playSound(soundEvent, 1.0f, 1.0f);
            }
        });
        controllers.add((AnimationController)drawAnimationController);
        GunStateAnimationController hideAnimationController = new GunStateAnimationController(this, this, CONTROLLER_NAME_HIDE, DEFAULT_ANIMATION_HIDE, ctx -> ctx.gunClientState().isHiding() || ctx.gunClientState().isHidden()){

            @Override
            protected boolean shouldContinueAnimation(GunClientState gunClientState) {
                return gunClientState.isHiding() || gunClientState.isHidden();
            }

            @Override
            public void onHiding(LivingEntity player, GunClientState state, ItemStack itemStack) {
                if (ClientUtil.isFirstPerson(player)) {
                    this.scheduleReset(player, state, itemStack, state.getResolvedHideAnimationName(), state.getResolvedHideTransitionTicks());
                }
            }
        };
        hideAnimationController.setSoundKeyframeHandler(event -> {
            SoundKeyframeData soundKeyframeData;
            SoundEvent soundEvent;
            Player player = ClientUtil.getClientPlayer();
            if (player != null && (soundEvent = SoundRegistry.getSoundEvent((soundKeyframeData = event.getKeyframeData()).getSound())) != null) {
                player.playSound(soundEvent, 1.0f, 1.0f);
            }
        });
        controllers.add((AnimationController)hideAnimationController);
        GunStateAnimationController inspectAnimationController = new GunStateAnimationController(this, "inspect_controller", DEFAULT_ANIMATION_INSPECT, ctx -> ctx.gunClientState().isInspecting()){

            private String getAnimationName(LivingEntity player, GunClientState state, ItemStack itemStack) {
                if (GunItem.this.inspectAnimationProvider == null) {
                    return null;
                }
                AnimationProvider.Descriptor descriptor = GunItem.this.inspectAnimationProvider.getDescriptor(player, itemStack, state);
                return descriptor != null ? descriptor.animationName() : null;
            }

            @Override
            public void onInspecting(LivingEntity player, GunClientState state, ItemStack itemStack) {
                if (ClientUtil.isFirstPerson(player)) {
                    this.scheduleReset(player, state, itemStack, this.getAnimationName(player, state, itemStack));
                }
            }
        };
        inspectAnimationController.setSoundKeyframeHandler(event -> {
            SoundKeyframeData soundKeyframeData;
            String soundName;
            SoundEvent soundEvent;
            Player player = ClientUtil.getClientPlayer();
            if (player != null && (soundEvent = SoundRegistry.getSoundEvent(soundName = (soundKeyframeData = event.getKeyframeData()).getSound())) != null) {
                player.playSound(soundEvent, 1.0f, 1.0f);
            }
        });
        controllers.add((AnimationController)inspectAnimationController);
        GunStateAnimationController idleAnimationController = new GunStateAnimationController(this, CONTROLLER_NAME_IDLE, DEFAULT_ANIMATION_IDLE, ctx -> (ctx.gunClientState().getFireState() == GunClientState.FireState.IDLE || ctx.gunClientState().getFireState() == GunClientState.FireState.IDLE_COOLDOWN) && this.hasIdleAnimations(), true){

            @Override
            protected boolean shouldContinueAnimation(GunClientState gunClientState) {
                if (gunClientState.isChangingFireMode() || gunClientState.isFiring() || gunClientState.isReloading() || gunClientState.isInspecting() || gunClientState.isOverheating() || gunClientState.isDrawing()) {
                    return false;
                }
                return this.getAnimationState() == AnimationController.State.RUNNING;
            }

            private String getAnimationName(LivingEntity player, GunClientState state, ItemStack itemStack) {
                if (GunItem.this.idleAnimationProvider == null) {
                    return null;
                }
                AnimationProvider.Descriptor descriptor = GunItem.this.idleAnimationProvider.getDescriptor(player, itemStack, state);
                return descriptor != null ? descriptor.animationName() : null;
            }

            @Override
            public void onIdle(LivingEntity player, GunClientState state, ItemStack itemStack) {
                if (ClientUtil.isFirstPerson(player)) {
                    String animName = this.getAnimationName(player, state, itemStack);
                    this.scheduleReset(player, state, itemStack, animName);
                }
            }
        };
        idleAnimationController.setSoundKeyframeHandler(event -> {
            SoundKeyframeData soundKeyframeData;
            String soundName;
            SoundEvent soundEvent;
            Player player = ClientUtil.getClientPlayer();
            if (player != null && (soundEvent = SoundRegistry.getSoundEvent(soundName = (soundKeyframeData = event.getKeyframeData()).getSound())) != null) {
                player.playSound(soundEvent, 1.0f, 1.0f);
            }
        });
        controllers.add((AnimationController)idleAnimationController);
        GunStateAnimationController enableFireModeAimationController = new GunStateAnimationController(this, this, "enable_fire_mode_controller", DEFAULT_ANIMATION_ENABLE_FIRE_MODE, ctx -> ctx.gunClientState().isChangingFireMode()){

            @Override
            protected boolean shouldContinueAnimation(GunClientState gunClientState) {
                if (gunClientState.isFiring() || gunClientState.isReloading()) {
                    return false;
                }
                return this.getAnimationState() == AnimationController.State.RUNNING;
            }

            @Override
            public void onEnablingFireMode(LivingEntity player, GunClientState state, ItemStack itemStack) {
                if (ClientUtil.isFirstPerson(player)) {
                    this.scheduleReset(player, state, itemStack, FireModeFeature.getEnableFireModeAnimation(player, state, itemStack));
                }
            }
        };
        enableFireModeAimationController.setSoundKeyframeHandler(event -> {
            SoundKeyframeData soundKeyframeData;
            String soundName;
            SoundEvent soundEvent;
            Player player = ClientUtil.getClientPlayer();
            if (player != null && (soundEvent = SoundRegistry.getSoundEvent(soundName = (soundKeyframeData = event.getKeyframeData()).getSound())) != null) {
                player.playSound(soundEvent, 1.0f, 1.0f);
            }
        });
        controllers.add((AnimationController)enableFireModeAimationController);
    }

    private static boolean isCompatibleBullet(Item ammoItem, ItemStack gunStack, FireModeInstance fireModeInstance) {
        boolean result = false;
        Item item = gunStack.getItem();
        if (!(item instanceof GunItem)) {
            return false;
        }
        GunItem gunItem = (GunItem)item;
        List<FireModeInstance> firModeInstances = GunItem.getFireModes(gunStack);
        if (!firModeInstances.contains(fireModeInstance)) {
            return false;
        }
        if (fireModeInstance.isUsingDefaultAmmoPool()) {
            for (Supplier<AmmoItem> compatibleBullet : gunItem.compatibleBullets) {
                if (!Objects.equals(compatibleBullet.get(), ammoItem)) continue;
                result = true;
                break;
            }
        } else {
            result = ammoItem == fireModeInstance.getAmmo();
        }
        return result;
    }

    public int canReloadGun(ItemStack gunStack, Player player, FireModeInstance fireModeInstance) {
        int currentAmmo;
        GunItem gunItem = (GunItem)gunStack.getItem();
        int maxCapacity = gunItem.getMaxAmmoCapacity(gunStack, fireModeInstance);
        int ammoNeeded = maxCapacity - (currentAmmo = GunItem.getAmmo(gunStack, fireModeInstance));
        if (ammoNeeded <= 0) {
            return 0;
        }
        if (player.isCreative()) {
            return ammoNeeded;
        }
        int availableBullets = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (GunItem.isCompatibleBullet(itemStack.getItem(), gunStack, fireModeInstance)) {
                availableBullets += itemStack.getCount();
            }
            if (availableBullets >= ammoNeeded) break;
        }
        int potentialReloadAmount = Math.min(ammoNeeded, availableBullets);
        return potentialReloadAmount;
    }

    int reloadGun(ItemStack gunStack, Player player, FireModeInstance fireModeInstance) {
        int currentAmmo;
        if (!(gunStack.getItem() instanceof GunItem)) {
            return 0;
        }
        GunItem gunItem = (GunItem)gunStack.getItem();
        if (!gunItem.isEnabled()) {
            return 0;
        }
        int maxCapacity = gunItem.getMaxAmmoCapacity(gunStack, fireModeInstance);
        int neededAmmo = maxCapacity - (currentAmmo = GunItem.getAmmo(gunStack, fireModeInstance));
        if (neededAmmo <= 0) {
            return currentAmmo;
        }
        if (player.isCreative()) {
            int newAmmo = currentAmmo + neededAmmo;
            GunItem.setAmmo(gunStack, fireModeInstance, newAmmo);
            return newAmmo;
        }
        int foundAmmoCount = 0;
        for (int i = 0; i < player.getInventory().items.size(); ++i) {
            ItemStack inventoryItem = (ItemStack)player.getInventory().items.get(i);
            if (!GunItem.isCompatibleBullet(inventoryItem.getItem(), gunStack, fireModeInstance)) continue;
            int availableBullets = inventoryItem.getCount();
            if (availableBullets <= neededAmmo) {
                foundAmmoCount += availableBullets;
                neededAmmo -= availableBullets;
                player.getInventory().items.set(i, (Object)ItemStack.EMPTY);
            } else {
                inventoryItem.shrink(neededAmmo);
                foundAmmoCount += neededAmmo;
                neededAmmo = 0;
            }
            if (neededAmmo == 0) break;
        }
        int newAmmo = currentAmmo + foundAmmoCount;
        GunItem.setAmmo(gunStack, fireModeInstance, newAmmo);
        return newAmmo;
    }

    public void handleClientReloadRequest(ServerPlayer player, ItemStack itemStack, UUID clientStateId, int slotIndex, FireModeInstance fireModeInstance) {
        ItemStack validatedStack = GunItem.resolveServerActionStack(player, slotIndex, clientStateId);
        if (validatedStack != null && validatedStack == itemStack) {
            int ammo = this.reloadGun(itemStack, (Player)player, fireModeInstance);
            UUID itemStackId = GunItem.getItemStackId(itemStack);
            Platform.getInstance().getNetworkService().sendToClient(new ReloadResponsePacket(itemStackId, slotIndex, 0, ammo > 0, ammo, fireModeInstance), (Player)player);
        }
    }

    public static UUID getItemStackId(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return null;
        }
        CompoundTag idTag = MiscUtil.getTag(itemStack);
        return MiscUtil.getTagId(idTag);
    }

    public static void assignFreshItemStackId(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty() || !(itemStack.getItem() instanceof GunItem)) {
            return;
        }
        UUID id = UUID.randomUUID();
        CompoundTag tag = MiscUtil.getOrCreateTag(itemStack);
        tag.putLong("mid", id.getMostSignificantBits());
        tag.putLong("lid", id.getLeastSignificantBits());
        MiscUtil.setTag(itemStack, tag);
    }

    public static boolean hasConflictingItemStackId(Player player, ItemStack candidate, ItemStack excludedStack) {
        UUID candidateId = GunItem.getItemStackId(candidate);
        if (player == null || candidateId == null) {
            return false;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); ++slot) {
            ItemStack existing = player.getInventory().getItem(slot);
            if (existing == candidate || existing == excludedStack || !Objects.equals(candidateId, GunItem.getItemStackId(existing))) continue;
            return true;
        }
        ItemStack offhand = player.getOffhandItem();
        return offhand != candidate && offhand != excludedStack && Objects.equals(candidateId, GunItem.getItemStackId(offhand));
    }

    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        boolean isOffhand;
        boolean bl = isOffhand = entity instanceof Player && ((Player)entity).getOffhandItem() == itemStack;
        if (!level.isClientSide) {
            this.ensureItemStack(itemStack, level, entity, isOffhand);
        } else {
            GunClientState state = GunClientState.getState((Player)entity, itemStack, itemSlot, isOffhand);
            if (state != null && entity instanceof Player) {
                state.inventoryTick((LivingEntity)((Player)entity), itemStack, isSelected);
            }
        }
    }

    private void ensureItemStack(ItemStack itemStack, Level level, Entity entity, boolean isOffhand) {
        GeoItem.getOrAssignId((ItemStack)itemStack, (ServerLevel)((ServerLevel)level));
        GunItem.getOrAssignRandomSeed(itemStack);
        CompoundTag stateTag = MiscUtil.getOrCreateTag(itemStack);
        long mid = stateTag.getLong("mid");
        long lid = stateTag.getLong("lid");
        if (mid == 0L && lid == 0L) {
            UUID newId = UUID.randomUUID();
            stateTag.putLong("mid", newId.getMostSignificantBits());
            stateTag.putLong("lid", newId.getLeastSignificantBits());
            stateTag.putInt("ammo", this.maxAmmoCapacity == Integer.MAX_VALUE ? Integer.MAX_VALUE : 0);
            stateTag.put("ammox", (Tag)new CompoundTag());
            List<FireModeInstance> mainFireModes = this.getMainFireModes();
            if (mainFireModes != null && !mainFireModes.isEmpty()) {
                stateTag.putUUID("fmid", this.getMainFireModes().get(0).getId());
            }
            stateTag.putBoolean("aim", false);
            MiscUtil.setTag(itemStack, stateTag);
            Item item = itemStack.getItem();
            if (item instanceof AttachmentHost) {
                AttachmentHost attachmentHost = (AttachmentHost)item;
                Collection<Attachment> defaultAttachments = attachmentHost.getDefaultAttachments();
                for (Attachment attachment : defaultAttachments) {
                    Attachments.addAttachment(itemStack, new ItemStack((ItemLike)attachment), true);
                }
            }
        } else {
            this.ensureValidFireModeSelected(itemStack);
        }
        Attachments.ensureValidAttachmentsSelected(itemStack);
    }

    public static void initStackForCrafting(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return;
        }
        GunItem gunItem = (GunItem)item;
        CompoundTag stateTag = MiscUtil.getOrCreateTag(itemStack);
        long mid = stateTag.getLong("mid");
        long lid = stateTag.getLong("lid");
        if (mid == 0L && lid == 0L) {
            UUID newId = UUID.randomUUID();
            stateTag.putLong("mid", newId.getMostSignificantBits());
            stateTag.putLong("lid", newId.getLeastSignificantBits());
            stateTag.put("ammox", (Tag)new CompoundTag());
            List<FireModeInstance> mainFireModes = gunItem.getMainFireModes();
            if (mainFireModes != null && !mainFireModes.isEmpty()) {
                stateTag.putUUID("fmid", gunItem.getMainFireModes().get(0).getId());
            }
            stateTag.putBoolean("aim", false);
            MiscUtil.setTag(itemStack, stateTag);
            Item item2 = itemStack.getItem();
            if (item2 instanceof AttachmentHost) {
                AttachmentHost attachmentHost = (AttachmentHost)item2;
                Collection<Attachment> defaultAttachments = attachmentHost.getDefaultAttachments();
                for (Attachment attachment : defaultAttachments) {
                    Attachments.addAttachment(itemStack, new ItemStack((ItemLike)attachment), true);
                }
            }
        }
    }

    private void ensureValidFireModeSelected(ItemStack itemStack) {
        CompoundTag idTag = MiscUtil.getTag(itemStack);
        if (idTag != null) {
            UUID fireModeInstanceId = idTag.hasUUID("fmid") ? idTag.getUUID("fmid") : null;
            FireModeInstance selectedModeInstance = FireModeInstance.getOrElse(fireModeInstanceId, null);
            List<FireModeInstance> fireModes = GunItem.getFireModes(itemStack);
            if (selectedModeInstance != null && !fireModes.contains(selectedModeInstance)) {
                selectedModeInstance = null;
            }
            if (selectedModeInstance == null && !fireModes.isEmpty()) {
                selectedModeInstance = fireModes.get(0);
                GunItem.setFireModeInstance(itemStack, selectedModeInstance);
            }
            if (selectedModeInstance == null) {
                idTag.remove("fmid");
                MiscUtil.setTag(itemStack, idTag);
            }
        }
    }

    private static long getOrAssignRandomSeed(ItemStack stack) {
        CompoundTag tag = MiscUtil.getOrCreateTag(stack);
        long seed = tag.getLong("seed");
        if (tag.contains("seed", 99)) {
            return seed;
        }
        seed = random.nextLong();
        tag.putLong("seed", seed);
        MiscUtil.setTag(stack, tag);
        return seed;
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int ticksRemaining) {
    }

    private Predicate<Block> getDestroyBlockByHitScanPredicate() {
        return block -> Config.bulletsBreakGlassEnabled && (block instanceof StainedGlassBlock || block == Blocks.GLASS || block instanceof StainedGlassPaneBlock || block == Blocks.GLASS_PANE);
    }

    private Predicate<Block> getPassThroughBlocksByHitScanPredicate() {
        return block -> block instanceof BushBlock || block instanceof LeavesBlock;
    }

    public static OperableGunContext resolveMainHandGunContext(Player player) {
        if (player == null) {
            return null;
        }
        ItemStack stack = player.getMainHandItem();
        return stack != null && stack.getItem() instanceof GunItem ? new OperableGunContext(stack, InteractionHand.MAIN_HAND, player.getInventory().selected, false) : null;
    }

    public static OperableGunContext resolveOffhandGunContext(Player player) {
        if (player == null) {
            return null;
        }
        ItemStack stack = player.getOffhandItem();
        return stack != null && stack.getItem() instanceof GunItem && GunItem.canBePlacedInOffhand(stack) ? new OperableGunContext(stack, InteractionHand.OFF_HAND, 40, true) : null;
    }

    public static OperableGunContext resolveOperableGunContext(Player player) {
        OperableGunContext main = GunItem.resolveMainHandGunContext(player);
        return main != null ? main : GunItem.resolveOffhandGunContext(player);
    }

    public static boolean canBePlacedInOffhand(ItemStack stack) {
        GunItem gunItem;
        Item item;
        return stack == null || stack.isEmpty() || !((item = stack.getItem()) instanceof GunItem) || (gunItem = (GunItem)item).isDualWieldingEnabled(stack);
    }

    public static boolean canReloadInCurrentHands(Player player, ItemStack stack) {
        Item item;
        if (player == null || stack == null || stack.isEmpty() || !((item = stack.getItem()) instanceof GunItem)) {
            return false;
        }
        GunItem gunItem = (GunItem)item;
        OperableGunContext context = GunItem.resolveContextForStack(player, stack);
        return context != null && (context.offhand() || gunItem.isDualWieldingEnabled(stack) || player.getOffhandItem().isEmpty());
    }

    public static boolean isReloadBlockedByOccupiedOffhand(Player player, ItemStack stack) {
        GunItem gunItem;
        Item item;
        return player != null && stack != null && !stack.isEmpty() && (item = stack.getItem()) instanceof GunItem && !(gunItem = (GunItem)item).isDualWieldingEnabled(stack) && !player.getOffhandItem().isEmpty();
    }

    public static boolean hasGunsInMainAndAltSlots(Player player) {
        return GunItem.resolveMainHandGunContext(player) != null && GunItem.resolveOffhandGunContext(player) != null;
    }

    public static boolean hasAltSlotGunWithEmptyMainSlot(Player player) {
        return player != null && player.getMainHandItem().isEmpty() && GunItem.resolveOffhandGunContext(player) != null;
    }

    public static boolean shouldUseRightHandOnlyMainGrip(Player player, OperableGunContext context) {
        return player != null && context != null && !context.offhand() && !player.getOffhandItem().isEmpty();
    }

    public static OperableGunContext resolveContextForStack(Player player, ItemStack stack) {
        boolean matchesOffhand;
        if (player == null || stack == null || stack.isEmpty() || !(stack.getItem() instanceof GunItem)) {
            return null;
        }
        OperableGunContext main = GunItem.resolveMainHandGunContext(player);
        OperableGunContext offhand = GunItem.resolveOffhandGunContext(player);
        if (main != null && main.itemStack() == stack) {
            return main;
        }
        if (offhand != null && offhand.itemStack() == stack) {
            return offhand;
        }
        boolean matchesMain = main != null && GunItem.isSameGunStack(main.itemStack(), stack);
        boolean bl = matchesOffhand = offhand != null && GunItem.isSameGunStack(offhand.itemStack(), stack);
        if (matchesMain && matchesOffhand) {
            GunItem.debugDualWieldOnce("ambiguous-context|" + player.getId() + "|" + String.valueOf(GunItem.getItemStackId(stack)), "Rejected ambiguous held gun UUID {} for {}", GunItem.getItemStackId(stack), player.getName().getString());
            return null;
        }
        return matchesMain ? main : (matchesOffhand ? offhand : null);
    }

    public static OperableGunContext resolveRenderableContextForStack(Player player, ItemStack stack) {
        boolean matchesOffhand;
        OperableGunContext offhand;
        if (player == null || stack == null || stack.isEmpty() || !(stack.getItem() instanceof GunItem)) {
            return null;
        }
        GunHandSwapCoordinator coordinator = GunHandSwapCoordinator.getInstance();
        ItemStack visibleMain = coordinator.visibleMainHandStack(player);
        OperableGunContext visibleMainContext = visibleMain != null && visibleMain.getItem() instanceof GunItem ? new OperableGunContext(visibleMain, InteractionHand.MAIN_HAND, coordinator.visibleMainHandSlot(player), false) : null;
        ItemStack visibleOffhand = coordinator.visibleOffhandStack(player);
        OperableGunContext operableGunContext = offhand = visibleOffhand != null && visibleOffhand.getItem() instanceof GunItem && GunItem.canBePlacedInOffhand(visibleOffhand) ? new OperableGunContext(visibleOffhand, InteractionHand.OFF_HAND, 40, true) : null;
        if (visibleMainContext != null && visibleMainContext.itemStack() == stack) {
            return visibleMainContext;
        }
        if (offhand != null && offhand.itemStack() == stack) {
            return offhand;
        }
        boolean matchesMain = visibleMainContext != null && GunItem.isSameGunStack(visibleMainContext.itemStack(), stack);
        boolean bl = matchesOffhand = offhand != null && GunItem.isSameGunStack(offhand.itemStack(), stack);
        if (matchesMain == matchesOffhand) {
            return null;
        }
        return matchesMain ? visibleMainContext : offhand;
    }

    public static int resolveSlotIndex(Player player, ItemStack stack) {
        OperableGunContext context = GunItem.resolveContextForStack(player, stack);
        if (context != null) {
            return context.slotIndex();
        }
        return player != null && stack != null && !stack.isEmpty() ? player.getInventory().findSlotMatchingItem(stack) : -1;
    }

    public static ItemDisplayContext getFirstPersonDisplayContext(OperableGunContext context) {
        Player player = ClientUtil.getClientPlayer();
        if (player == null || context == null) {
            return ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        }
        return GunItem.getFirstPersonDisplayContext(player, context.hand());
    }

    public static ItemDisplayContext getFirstPersonDisplayContext(Player player, InteractionHand hand) {
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        return arm == HumanoidArm.RIGHT ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

    public static ItemDisplayContext resolveFirstPersonDisplayContext(Player player, ItemStack stack) {
        OperableGunContext context = GunItem.resolveRenderableContextForStack(player, stack);
        return context != null ? GunItem.getFirstPersonDisplayContext(player, context.hand()) : null;
    }

    public static boolean shouldApplyOffhandFirstPersonMirror(Player player, ItemStack stack) {
        OperableGunContext context = GunItem.resolveRenderableContextForStack(player, stack);
        return context != null && context.offhand();
    }

    public static boolean isOperableGunStack(Player player, ItemStack stack) {
        return GunItem.resolveContextForStack(player, stack) != null;
    }

    public static ItemStack resolveServerActionStack(ServerPlayer player, int slotIndex, UUID stateId) {
        ItemStack otherHeldStack;
        if (player == null || slotIndex != player.getInventory().selected && slotIndex != 40) {
            GunItem.debugDualWieldOnce("bad-slot|" + String.valueOf(player != null ? player.getUUID() : null) + "|" + slotIndex, "Rejected gun action for non-held slot {}", slotIndex);
            return null;
        }
        ItemStack stack = player.getInventory().getItem(slotIndex);
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof GunItem)) {
            return null;
        }
        if (slotIndex == 40 && !GunItem.canBePlacedInOffhand(stack)) {
            GunItem.debugDualWieldOnce("bad-offhand|" + String.valueOf(player.getUUID()) + "|" + String.valueOf(GunItem.getItemStackId(stack)), "Rejected ineligible offhand gun action for {}", player.getName().getString());
            return null;
        }
        UUID actualId = GunItem.getItemStackId(stack);
        if (stateId == null || !Objects.equals(actualId, stateId)) {
            GunItem.debugDualWieldOnce("stale-id|" + String.valueOf(player.getUUID()) + "|" + String.valueOf(stateId) + "|" + String.valueOf(actualId), "Rejected stale gun state {} (held state {})", stateId, actualId);
            return null;
        }
        ItemStack itemStack = otherHeldStack = slotIndex == 40 ? player.getMainHandItem() : player.getOffhandItem();
        if (otherHeldStack != stack && Objects.equals(actualId, GunItem.getItemStackId(otherHeldStack))) {
            GunItem.debugDualWieldOnce("ambiguous-held-id|" + String.valueOf(player.getUUID()) + "|" + String.valueOf(actualId), "Rejected ambiguous duplicated held gun state {}", actualId);
            return null;
        }
        return stack;
    }

    private static boolean isSameGunStack(ItemStack first, ItemStack second) {
        if (first == second) {
            return true;
        }
        UUID firstId = GunItem.getItemStackId(first);
        return firstId != null && Objects.equals(firstId, GunItem.getItemStackId(second));
    }

    private static void debugDualWieldOnce(String key, String message, Object ... args) {
        if (DEBUG_DUAL_WIELD && DUAL_WIELD_DEBUG_KEYS.add(key)) {
            LOGGER.info("[dual-wield] " + message, args);
        }
    }

    public void requestFireFromServer(GunClientState gunClientState, Player player, ItemStack itemStack, Entity targetEntity) {
        OperableGunContext context = GunItem.resolveContextForStack(player, itemStack);
        if (context == null) {
            return;
        }
        int activeSlot = context.slotIndex();
        LOGGER.debug("{} requesting fire from server", (Object)(System.currentTimeMillis() % 100000L));
        SoundFeature.playFireSound(player, itemStack);
        Pair<Integer, Double> pcs = FireModeFeature.getPelletCountAndSpread((LivingEntity)player, gunClientState, itemStack);
        int shotCount = (Integer)pcs.getFirst() > 0 ? (Integer)pcs.getFirst() : 1;
        long requestSeed = random.nextLong();
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        AmmoItem projectileItem = this.getFirstCompatibleProjectile(itemStack, fireModeInstance);
        if (projectileItem != null) {
            Vec3 direction;
            Vec3 startPos;
            GunStatePoseProvider gunStatePoseProvider = GunStatePoseProvider.getInstance();
            Vec3[] pd = gunStatePoseProvider.getPositionAndDirection(gunClientState, GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE);
            if (pd == null) {
                pd = gunStatePoseProvider.getPositionAndDirection(gunClientState, GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE_FLASH);
            }
            if (pd != null) {
                startPos = pd[0];
                direction = pd[1];
            } else {
                startPos = player.getEyePosition();
                direction = player.getViewVector(0.0f);
                startPos = startPos.add(direction.normalize().multiply(2.0, 2.0, 2.0));
            }
            Platform.getInstance().getNetworkService().sendToServer(new ProjectileFireRequestPacket(fireModeInstance, GunItem.getItemStackId(itemStack), activeSlot, gunClientState.isAiming(), startPos.x, startPos.y, startPos.z, direction.x, direction.y, direction.z, targetEntity != null ? targetEntity.getId() : -1, requestSeed));
            ShellEjectionLauncher.spawnClientShellCasing(player, gunClientState, itemStack);
        } else {
            double adjustedInaccuracy = this.adjustInaccuracy(player, itemStack, gunClientState.isAiming());
            long itemSeed = GunItem.getOrAssignRandomSeed(itemStack);
            long xorSeed = itemSeed ^ requestSeed;
            this.acquireHitScan(player, itemStack, gunClientState, shotCount, xorSeed, adjustedInaccuracy);
            Platform.getInstance().getNetworkService().sendToServer(new HitScanFireRequestPacket(fireModeInstance, GunItem.getItemStackId(itemStack), activeSlot, gunClientState.isAiming(), gunClientState.isAlternateMuzzleSelected(), requestSeed));
            ShellEjectionLauncher.spawnClientShellCasing(player, gunClientState, itemStack);
            LOGGER.debug("{} sent fire request to server", (Object)(System.currentTimeMillis() % 100000L));
        }
    }

    private void acquireHitScan(Player player, ItemStack itemStack, @NotNull GunClientState gunClientState, int shotCount, long seed, double adjustedInaccuracy) {
        double maxDistance = this.getMaxClientShootingDistance(itemStack, gunClientState);
        List<HitResult> hitResults = HitScan.getObjectsInCrosshair((LivingEntity)player, player.getEyePosition(), player.getViewVector(0.0f), 1.0f, maxDistance, shotCount, adjustedInaccuracy, seed, this.getDestroyBlockByHitScanPredicate(), this.getPassThroughBlocksByHitScanPredicate(), new ArrayList<BlockPos>());
        for (HitResult hitResult : hitResults) {
            gunClientState.acquireHitScan((LivingEntity)player, itemStack, hitResult);
        }
    }

    private double getMaxClientShootingDistance(ItemStack itemStack, GunClientState gunClientState) {
        Minecraft mc = Minecraft.getInstance();
        double maxDistance = Math.min(mc.options.getEffectiveRenderDistance() * 16, FireModeFeature.getMaxShootingDistance(itemStack));
        if (!gunClientState.isAiming()) {
            maxDistance = Math.min(maxDistance, 100.0);
        }
        return maxDistance;
    }

    private double adjustInaccuracy(Player player, ItemStack itemStack, boolean isAiming) {
        double adjustedInaccuracy = isAiming ? this.inaccuracyAiming : (player.isSprinting() ? this.inaccuracySprinting : this.inaccuracy);
        Pair<Integer, Double> pcs = FireModeFeature.getPelletCountAndSpread((LivingEntity)player, null, itemStack);
        if ((Integer)pcs.getFirst() > 0) {
            adjustedInaccuracy += ((Double)pcs.getSecond()).doubleValue();
        }
        float accuracyModifier = AccuracyFeature.getAccuracyModifier(itemStack);
        return adjustedInaccuracy / (double)accuracyModifier;
    }

    public static Optional<Integer> getClientSideAmmo(Player player, ItemStack itemStack, int slotIndex) {
        int resolvedSlot = slotIndex >= 0 ? slotIndex : GunItem.resolveSlotIndex(player, itemStack);
        GunClientState state = GunClientState.getState(player, itemStack, resolvedSlot, resolvedSlot == 40);
        return state != null ? Optional.of(state.getAmmoCount(GunItem.getFireModeInstance(itemStack))) : Optional.empty();
    }

    public double getInacuracy() {
        return this.inaccuracy;
    }

    public int getShotsPerTrace() {
        return this.shotsPerTrace;
    }

    public static int getAmmo(ItemStack itemStack, FireModeInstance fireModeInstance) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return 0;
        }
        CompoundTag idTag = MiscUtil.getTag(itemStack);
        if (idTag != null) {
            if (fireModeInstance.isUsingDefaultAmmoPool()) {
                return idTag.getInt("ammo");
            }
            CompoundTag auxAmmoTag = idTag.getCompound("ammox");
            if (auxAmmoTag != null) {
                return auxAmmoTag.getInt(fireModeInstance.getAmmo().getName());
            }
        }
        return 0;
    }

    public static void setAmmo(ItemStack itemStack, FireModeInstance fireModeInstance, int ammo) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return;
        }
        LOGGER.debug("Setting ammo in stack {} to {}", (Object)System.identityHashCode(itemStack), (Object)ammo);
        CompoundTag idTag = MiscUtil.getTag(itemStack);
        if (idTag != null) {
            if (fireModeInstance.isUsingDefaultAmmoPool()) {
                idTag.putInt("ammo", ammo);
            } else {
                CompoundTag auxAmmoTag = idTag.getCompound("ammox");
                auxAmmoTag.putInt(fireModeInstance.getAmmo().getName(), ammo);
                idTag.put("ammox", (Tag)auxAmmoTag);
            }
            MiscUtil.setTag(itemStack, idTag);
        }
    }

    public static int decrementAmmo(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return 0;
        }
        CompoundTag idTag = MiscUtil.getTag(itemStack);
        if (idTag != null) {
            int ammo = idTag.getInt("ammo");
            if (ammo <= 0) {
                return -1;
            }
            idTag.putInt("ammo", --ammo);
            MiscUtil.setTag(itemStack, idTag);
            return ammo;
        }
        return 0;
    }

    public static FireMode getSelectedFireModeType(ItemStack itemStack) {
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        return fireModeInstance != null ? fireModeInstance.getType() : null;
    }

    public static FireModeInstance getFireModeInstance(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return null;
        }
        GunItem gunItem = (GunItem)item;
        CompoundTag idTag = MiscUtil.getTag(itemStack);
        if (idTag != null) {
            UUID fireModeInstanceId = idTag.hasUUID("fmid") ? idTag.getUUID("fmid") : null;
            FireModeInstance fireModeInstance = null;
            if (fireModeInstanceId != null) {
                fireModeInstance = FireModeInstance.getOrElse(fireModeInstanceId, null);
            }
            if (fireModeInstance == null) {
                fireModeInstance = gunItem.getMainFireModes().get(0);
            }
            return fireModeInstance;
        }
        return null;
    }

    private static void setFireModeInstance(ItemStack itemStack, FireModeInstance fireModeInstance) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return;
        }
        CompoundTag idTag = MiscUtil.getTag(itemStack);
        if (idTag != null) {
            idTag.putUUID("fmid", fireModeInstance.getId());
            MiscUtil.setTag(itemStack, idTag);
            LOGGER.debug("Set fire mode instance to {}, tag: {}", (Object)fireModeInstance.getDisplayName(), (Object)idTag);
        }
    }

    public void handleClientProjectileFireRequest(ServerPlayer player, FireModeInstance fireModeInstance, UUID stateId, int slotIndex, int correlationId, boolean isAiming, double spawnPositionX, double spawnPositionY, double spawnPositionZ, double spawnDirectionX, double spawnDirectionY, double spawnDirectionZ, int targetEntityId, long requestSeed) {
        LOGGER.debug("Handling client projectile file request");
        ItemStack itemStack = GunItem.resolveServerActionStack(player, slotIndex, stateId);
        if (itemStack == null) {
            return;
        }
        isAiming = slotIndex != 40 && isAiming;
        AmmoItem projectileItem = this.getFirstCompatibleProjectile(itemStack, fireModeInstance);
        if (projectileItem == null) {
            LOGGER.error("Attempted to handle client projectile fire request with an item that does not support projectiles: " + String.valueOf(this));
            return;
        }
        if (!this.isEnabled() || !projectileItem.isEnabled()) {
            return;
        }
        if (itemStack == null || !(itemStack.getItem() instanceof GunItem)) {
            return;
        }
        int ammo = 0;
        ammo = GunItem.getAmmo(itemStack, fireModeInstance);
        if (ammo > 0) {
            LOGGER.debug("Received client projectile file request");
            Entity targetEntity = null;
            if (targetEntityId >= 0) {
                targetEntity = MiscUtil.getLevel((Entity)player).getEntity(targetEntityId);
            }
            ProjectileLike projectile = null;
            if (targetEntity != null) {
                HitResult hitResult = HitScan.ensureEntityInCrosshair((LivingEntity)player, targetEntity, 0.0f, 400.0, 2.0f);
                if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY && ((EntityHitResult)hitResult).getEntity() == targetEntity) {
                    projectile = projectileItem.createProjectile((LivingEntity)player, spawnPositionX, spawnPositionY, spawnPositionZ);
                    projectile.launchAtTargetEntity((LivingEntity)player, hitResult, targetEntity);
                }
            } else {
                projectile = projectileItem.createProjectile((LivingEntity)player, spawnPositionX, spawnPositionY, spawnPositionZ);
                long xorSeed = GunItem.getOrAssignRandomSeed(itemStack) ^ requestSeed;
                double adjustedInaccuracy = this.adjustInaccuracy((Player)player, itemStack, isAiming);
                projectile.launchAtLookTarget((LivingEntity)player, adjustedInaccuracy, xorSeed);
            }
            if (projectile != null) {
                if (this.getMaxAmmoCapacity(itemStack, fireModeInstance) < Integer.MAX_VALUE) {
                    GunItem.setAmmo(itemStack, fireModeInstance, ammo - 1);
                }
                SoundFeature.playFireSound((Player)player, itemStack);
                MiscUtil.getLevel((Entity)player).addFreshEntity((Entity)projectile);
            } else {
                LOGGER.debug("Did not fire projectile");
            }
        }
    }

    public List<AmmoItem> getCompatibleAmmo() {
        return this.compatibleBullets.stream().map(Supplier::get).toList();
    }

    private AmmoItem getFirstCompatibleProjectile(ItemStack gunStack, FireModeInstance fireModeInstance) {
        Item item = gunStack.getItem();
        if (!(item instanceof GunItem)) {
            return null;
        }
        GunItem gunItem = (GunItem)item;
        List<FireModeInstance> firModeInstances = GunItem.getFireModes(gunStack);
        if (!firModeInstances.contains(fireModeInstance)) {
            return null;
        }
        AmmoItem projectileItem = null;
        if (fireModeInstance.isUsingDefaultAmmoPool()) {
            for (Supplier<AmmoItem> ammoSupplier : gunItem.compatibleBullets) {
                AmmoItem ammoItem = ammoSupplier.get();
                if (!ammoItem.isHasProjectile()) continue;
                projectileItem = ammoItem;
                break;
            }
        } else {
            AmmoItem ammoItem = fireModeInstance.getAmmo();
            if (ammoItem.isHasProjectile()) {
                projectileItem = ammoItem;
            }
        }
        return projectileItem;
    }

    public void handleClientHitScanFireRequest(ServerPlayer player, FireModeInstance fireModeInstance, UUID stateId, int slotIndex, int correlationId, boolean isAiming, boolean alternateMuzzle, long requestSeed) {
        try {
            LOGGER.debug("{} handling client fire request", (Object)(System.currentTimeMillis() % 100000L));
            ItemStack itemStack = GunItem.resolveServerActionStack(player, slotIndex, stateId);
            if (itemStack == null) {
                return;
            }
            isAiming = slotIndex != 40 && isAiming;
            AmmoItem projectileItem = this.getFirstCompatibleProjectile(itemStack, fireModeInstance);
            if (projectileItem != null) {
                LOGGER.error("Attempted to handle client hit scan fire request with an item that fires projectiles: " + String.valueOf(this));
                return;
            }
            if (!this.isEnabled()) {
                return;
            }
            if (itemStack == null || !(itemStack.getItem() instanceof GunItem)) {
                return;
            }
            ArrayList<HitResult> hitResults = new ArrayList<HitResult>();
            int ammo = 0;
            ammo = GunItem.getAmmo(itemStack, fireModeInstance);
            if (ammo > 0) {
                if (this.getMaxAmmoCapacity(itemStack, fireModeInstance) < Integer.MAX_VALUE) {
                    GunItem.setAmmo(itemStack, fireModeInstance, ammo - 1);
                }
                SoundFeature.playFireSound((Player)player, itemStack);
                Pair<Integer, Double> pcs = FireModeFeature.getPelletCountAndSpread((LivingEntity)player, null, itemStack);
                int shotCount = (Integer)pcs.getFirst() > 0 ? (Integer)pcs.getFirst() : 1;
                double adjustedInaccuracy = this.adjustInaccuracy((Player)player, itemStack, isAiming);
                long xorSeed = GunItem.getOrAssignRandomSeed(itemStack) ^ requestSeed;
                Vec3 eyePos = player.getEyePosition();
                Vec3 lookVec = player.getViewVector(0.0f);
                ServerLevel level = (ServerLevel)MiscUtil.getLevel((Entity)player);
                double maxHitScanDistance = this.getMaxServerShootingDistance(itemStack, isAiming, level);
                ArrayList<BlockPos> blockPosToDestroy = new ArrayList<BlockPos>();
                hitResults.addAll(HitScan.getObjectsInCrosshair((LivingEntity)player, eyePos, lookVec, 0.0f, maxHitScanDistance, shotCount, adjustedInaccuracy, xorSeed, this.getDestroyBlockByHitScanPredicate(), this.getPassThroughBlocksByHitScanPredicate(), blockPosToDestroy));
                LOGGER.debug("{} obtained hit results", (Object)(System.currentTimeMillis() % 100000L));
                for (HitResult hitResult : hitResults) {
                    this.hitScanTarget((Player)player, itemStack, slotIndex, correlationId, hitResult, maxHitScanDistance, blockPosToDestroy, alternateMuzzle);
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to handle client hit scan fire request: {}", (Throwable)e);
        }
    }

    private double getMaxServerShootingDistance(ItemStack itemStack, boolean isAiming, ServerLevel level) {
        MinecraftServer server = level.getServer();
        double maxHitScanDistance = Math.min(server.getPlayerList().getViewDistance() * 16, FireModeFeature.getMaxShootingDistance(itemStack));
        if (!isAiming) {
            maxHitScanDistance = Math.min(maxHitScanDistance, 100.0);
        }
        return maxHitScanDistance;
    }

    private void hitScanTarget(Player player, ItemStack itemStack, int slotIndex, int correlationId, HitResult hitResult, double maxHitScanDistance, List<BlockPos> blockPosToDestroy, boolean alternateMuzzle) {
        float entityDamage = 0.0f;
        LOGGER.debug("Executing hit target task for hit result {}", (Object)hitResult);
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            entityDamage = this.hurtEntity((LivingEntity)player, (EntityHitResult)hitResult, null, itemStack);
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            this.handleBlockHit((LivingEntity)player, (BlockHitResult)hitResult, null);
        }
        for (BlockPos bp : blockPosToDestroy) {
            MiscUtil.getLevel((Entity)player).destroyBlock(bp, true, (Entity)player);
        }
        double maxHitScanDistanceSqr = maxHitScanDistance * maxHitScanDistance;
        for (ServerPlayer serverPlayer : ((ServerLevel)MiscUtil.getLevel((Entity)player)).getPlayers(p -> true)) {
            if (serverPlayer != player && !(serverPlayer.distanceToSqr((Entity)player) < maxHitScanDistanceSqr)) continue;
            LOGGER.debug("{} sends hit scan notification to {}", (Object)player, (Object)serverPlayer);
            Platform.getInstance().getNetworkService().sendToClient(new HitScanFireResponsePacket(player.getId(), GunItem.getItemStackId(itemStack), slotIndex, correlationId, SimpleHitResult.fromHitResult(hitResult), entityDamage, alternateMuzzle), (Player)serverPlayer);
        }
    }

    public void handleClientFireModeRequest(ServerPlayer player, UUID stateId, int slotIndex, int correlationId, FireModeInstance fireModeInstance) {
        ItemStack itemStack = GunItem.resolveServerActionStack(player, slotIndex, stateId);
        if (itemStack == null || !(itemStack.getItem() instanceof GunItem)) {
            Platform.getInstance().getNetworkService().sendToClient(new FireModeResponsePacket(stateId, slotIndex, correlationId, false, fireModeInstance), (Player)player);
            return;
        }
        boolean isSuccess = GunItem.getFireModes(itemStack).contains(fireModeInstance);
        if (isSuccess) {
            GunItem.setFireModeInstance(itemStack, fireModeInstance);
        }
        Platform.getInstance().getNetworkService().sendToClient(new FireModeResponsePacket(stateId, slotIndex, correlationId, isSuccess, fireModeInstance), (Player)player);
    }

    public void processServerStateSyncResponse(UUID stateId, int correlationId, boolean isSuccess, ItemStack itemStack, GunClientState gunClientState) {
        if (isSuccess) {
            // empty if block
        }
    }

    public void processServerHitScanFireResponse(Player player, UUID stateId, ItemStack itemStack, GunClientState gunClientState, SimpleHitResult hitResult, float damage, boolean alternateMuzzle) {
        if (gunClientState != null) {
            Item item;
            Player mainPlayer = ClientUtil.getClientPlayer();
            if (player == mainPlayer) {
                if (hitResult.getType() != HitResult.Type.MISS) {
                    gunClientState.confirmHitScanTarget((LivingEntity)mainPlayer, itemStack, hitResult, damage);
                }
            } else if (itemStack != null && (item = itemStack.getItem()) instanceof GunItem) {
                GunItem gunItem = (GunItem)item;
                gunClientState.applyAlternatingMuzzleSelection(itemStack, alternateMuzzle);
                gunItem.effectLauncher.onStartFiring((LivingEntity)mainPlayer, gunClientState, itemStack);
                gunItem.effectLauncher.onHitScanTargetAcquired((LivingEntity)mainPlayer, gunClientState, itemStack, hitResult);
                if (hitResult.getType() != HitResult.Type.MISS) {
                    gunItem.effectLauncher.onHitScanTargetConfirmed((LivingEntity)mainPlayer, gunClientState, itemStack, hitResult, damage);
                }
            }
        }
    }

    public void processServerFireModeResponse(UUID stateId, int correlationId, boolean isSuccess, ItemStack itemStack, GunClientState gunClientState, FireModeInstance fireModeInstance) {
        LOGGER.debug("Process fire mode response: {}", (Object)isSuccess);
        if (isSuccess && GunItem.getFireModes(itemStack).contains(fireModeInstance)) {
            GunItem.setFireModeInstance(itemStack, fireModeInstance);
        }
    }

    public void processServerReloadResponse(int correlationId, boolean isSuccess, ItemStack itemStack, GunClientState gunClientState, int ammo, FireModeInstance fireModeInstance) {
        LOGGER.debug("Process server reload response with ammo count {}", (Object)ammo);
        gunClientState.reloadAmmo(ClientUtil.getClientLevel(), fireModeInstance, ammo);
    }

    public boolean tryReload(Player player, ItemStack itemStack) {
        return this.tryReload(player, GunItem.resolveContextForStack(player, itemStack));
    }

    public boolean tryReload(Player player, OperableGunContext context) {
        ItemStack itemStack;
        GunClientState gunClientState;
        boolean result = false;
        if (context != null && context.itemStack().getItem() == this && (gunClientState = GunClientState.getState(player, itemStack = context.itemStack(), context.slotIndex(), context.offhand())) != null) {
            result = gunClientState.tryReload((LivingEntity)player, itemStack);
        }
        return result;
    }

    public boolean tryFire(Player player, ItemStack itemStack, Entity targetEntity) {
        return this.tryFire(player, GunItem.resolveContextForStack(player, itemStack), targetEntity);
    }

    public boolean tryFire(Player player, OperableGunContext context, Entity targetEntity) {
        ItemStack itemStack;
        GunClientState gunClientState;
        boolean result = false;
        if (context != null && context.itemStack().getItem() == this && (gunClientState = GunClientState.getState(player, itemStack = context.itemStack(), context.slotIndex(), context.offhand())) != null) {
            gunClientState.setTrigger(true);
            result = gunClientState.tryFire((LivingEntity)player, itemStack, targetEntity);
        }
        return result;
    }

    public boolean requestReloadFromServer(Player player, ItemStack itemStack) {
        return this.requestReloadFromServer(player, GunItem.resolveContextForStack(player, itemStack));
    }

    public boolean requestReloadFromServer(Player player, OperableGunContext context) {
        LOGGER.debug("{} Initiating client side reload", (Object)(System.currentTimeMillis() % 100000L));
        boolean result = false;
        if (context != null && context.itemStack().getItem() == this) {
            FireModeInstance fireModeInstance;
            ItemStack itemStack = context.itemStack();
            int ammoToReload = this.canReloadGun(itemStack, player, fireModeInstance = GunItem.getFireModeInstance(itemStack));
            if (ammoToReload > 0) {
                Platform.getInstance().getNetworkService().sendToServer(new ReloadRequestPacket(GunItem.getItemStackId(itemStack), context.slotIndex(), fireModeInstance));
                result = true;
            } else {
                LOGGER.debug("No ammo to reload");
            }
        }
        return result;
    }

    public double getAimingCurveX() {
        return this.aimingCurveX;
    }

    public double getAimingCurveY() {
        return this.aimingCurveY;
    }

    public double getAimingCurveZ() {
        return this.aimingCurveZ;
    }

    public double getAimingCurvePitch() {
        return this.aimingCurvePitch;
    }

    public double getAimingCurveYaw() {
        return this.aimingCurveYaw;
    }

    public double getAimingCurveRoll() {
        return this.aimingCurveRoll;
    }

    public double getAimingZoom() {
        return this.aimingZoom;
    }

    private static FireModeInstance getNextFireModeInstance(ItemStack itemStack, FireModeInstance currentMode) {
        List<FireModeInstance> allFireModes = GunItem.getFireModes(itemStack);
        int currentIndex = allFireModes.indexOf(currentMode);
        int nextIndex = (currentIndex + 1) % allFireModes.size();
        return allFireModes.get(nextIndex);
    }

    public static List<FireModeInstance> getFireModes(ItemStack itemStack) {
        ArrayList<FireModeInstance> allFireModes = new ArrayList<FireModeInstance>();
        List<Features.EnabledFeature> enabledFireModeFeatures = Features.getEnabledFeatures(itemStack, FireModeFeature.class);
        for (Features.EnabledFeature efmf : enabledFireModeFeatures) {
            FireModeFeature fmf = (FireModeFeature)efmf.feature();
            allFireModes.addAll(fmf.getFireModes());
        }
        return allFireModes;
    }

    public void initiateClientSideFireMode(Player player, ItemStack itemStack) {
        FireModeInstance currentFireMode;
        FireModeInstance nextFireMode;
        GunClientState gunClientState;
        OperableGunContext context = GunItem.resolveContextForStack(player, itemStack);
        if (!(context == null || (gunClientState = GunClientState.getState(player, itemStack, context.slotIndex(), context.offhand())) == null || gunClientState.isReloading() || gunClientState.isFiring() || gunClientState.isInspecting() || (nextFireMode = GunItem.getNextFireModeInstance(itemStack, currentFireMode = GunItem.getFireModeInstance(itemStack))) == currentFireMode)) {
            LOGGER.debug("Requesting fire mode change from {} to {}", (Object)currentFireMode.getDisplayName(), (Object)nextFireMode.getDisplayName());
            Platform.getInstance().getNetworkService().sendToServer(new FireModeRequestPacket(GunItem.getItemStackId(itemStack), context.slotIndex(), nextFireMode));
        }
    }

    public void handleClientStopFireRequest(ServerPlayer player, UUID stateId, int slotIndex, int correlationId) {
    }

    public void processStopServerFireResponse(UUID stateId, int correlationId, boolean isSuccess, ItemStack itemStack, GunClientState gunClientState) {
    }

    public void setTriggerOff(Player player, ItemStack itemStack) {
        GunClientState gunClientState;
        OperableGunContext context = GunItem.resolveContextForStack(player, itemStack);
        if (context != null && (gunClientState = GunClientState.getState(player, itemStack, context.slotIndex(), context.offhand())) != null) {
            gunClientState.setTrigger(false);
        }
    }

    public GunClientState createState(UUID stateId) {
        String controllerId;
        GunClientState state = new GunClientState(stateId, this);
        LOGGER.debug("Creating state {}", (Object)stateId);
        state.setAnimationController("playerRecoil", new PlayerRecoilController(this.viewRecoilAmplitude, this.viewRecoilMaxPitch, this.viewRecoilDuration));
        state.setAnimationController("shake", new ViewShakeAnimationController(this, this.shakeRecoilAmplitude, this.shakeRecoilSpeed, this.shakeDecay, this.shakeRecoilDuration){

            @Override
            public void onStartFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
                FireModeInstance.ViewShakeDescriptor viewShakeDescriptor = FireModeFeature.getViewShakeDescriptor(itemStack);
                this.reset(viewShakeDescriptor);
            }
        });
        state.setAnimationController("recoil2", new GunRecoilAnimationController(this.gunRecoilInitialAmplitude, this.gunRecoilRateOfAmplitudeDecay, this.gunRecoilInitialAngularFrequency, this.gunRecoilRateOfFrequencyIncrease, this.gunRecoilPitchMultiplier, this.gunRecoilDuration, this.shotsPerRecoil, this.sustainedRecoilPitchIncrement, this.sustainedRecoilPosYIncrement, this.sustainedRecoilPosZIncrement, this.sustainedRecoilYawMultiplier, this.sustainedRecoilPitchCap, this.sustainedRecoilPosYCap, this.sustainedRecoilPosZCap, this.sustainedRecoilOnAim, this.sustainedRecoilDecayTime));
        state.setAnimationController("randomizer", new GunRandomizingAnimationController(this.gunRandomizationAmplitude, this.idleRandomizationDuration, this.recoilRandomizationDuration));
        state.setAnimationController("reloadTimer", this.createReloadTimerController());
        for (GlowAnimationController.Builder builder : this.glowEffectBuilders) {
            controllerId = "glowEffect" + builder.getEffectId();
            state.setAnimationController(controllerId, builder.build());
        }
        for (RotationAnimationController.Builder builder : this.rotationEffectBuilders) {
            controllerId = "rotation" + builder.getModelPartName();
            state.setAnimationController(controllerId, builder.build());
        }
        state.addListener(new DynamicGeoListener());
        state.addListener(this.effectLauncher);
        state.setAnimationController("aiming", new BiDirectionalInterpolator(this, 400L){

            @Override
            public void onToggleAiming(boolean isAiming) {
                this.set(isAiming ? BiDirectionalInterpolator.Position.END : BiDirectionalInterpolator.Position.START, false);
            }
        });
        return state;
    }

    private List<GunStateAnimationController> createReloadAnimationControllers() {
        ArrayList<GunStateAnimationController> reloadAnimationControllers = new ArrayList<GunStateAnimationController>();
        if (this.phasedReloads.isEmpty()) {
            GunStateAnimationController reloadAnimationController = new GunStateAnimationController(this, this, "reload_controller", DEFAULT_ANIMATION_RELOAD, ctx -> ctx.gunClientState().isReloading()){

                @Override
                public void onStartReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
                    this.scheduleReset(player, state, itemStack);
                }
            };
            reloadAnimationControllers.add(reloadAnimationController);
        } else {
            long maxReloadDuration = 0L;
            for (PhasedReload phasedReload : this.phasedReloads) {
                long conditionalReloadTimeMillis = phasedReload.timeUnit.toMillis(phasedReload.cooldownTime);
                if (conditionalReloadTimeMillis <= maxReloadDuration) continue;
                maxReloadDuration = conditionalReloadTimeMillis;
            }
            int counter = 0;
            for (final PhasedReload phasedReload : this.phasedReloads) {
                ReloadAnimation reloadAnimation = phasedReload.reloadAnimation;
                final Predicate<ConditionContext> combinedPredicate = ctx -> ctx.gunClientState().isReloading() && ctx.gunClientState().getReloadPhase() == phasedReload.phase && phasedReload.predicate.test((ConditionContext)ctx);
                GunStateAnimationController reloadAnimationController = new GunStateAnimationController(this, this, reloadAnimation.animationName + "_" + counter++, reloadAnimation.animationName, combinedPredicate){

                    @Override
                    public void onStartReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
                        if (phasedReload.phase == ReloadPhase.RELOADING && combinedPredicate.test(new ConditionContext((LivingEntity)((Player)player), itemStack, state, null))) {
                            LOGGER.debug("Reset {} on start reloading. Iter: {}", (Object)this.getName(), (Object)state.getReloadIterationIndex());
                            this.scheduleReset(player, state, itemStack);
                        }
                    }

                    @Override
                    public void onCompleteReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
                        if (phasedReload.phase == ReloadPhase.COMPLETETING && combinedPredicate.test(new ConditionContext((LivingEntity)((Player)player), itemStack, state, null))) {
                            LOGGER.debug("Reset {} on complete reloading. Iter: {}", (Object)this.getName(), (Object)state.getReloadIterationIndex());
                            this.scheduleReset(player, state, itemStack);
                        }
                    }

                    @Override
                    public void onPrepareReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
                        if (phasedReload.phase == ReloadPhase.PREPARING && combinedPredicate.test(new ConditionContext(player, itemStack, state, null))) {
                            LOGGER.debug("Reset {} on prepare reloading. Iter: {}", (Object)this.getName(), (Object)state.getReloadIterationIndex());
                            this.scheduleReset(player, state, itemStack);
                        }
                    }
                };
                reloadAnimationController.setSoundKeyframeHandler(event -> {
                    SoundKeyframeData soundKeyframeData;
                    String soundName;
                    SoundEvent soundEvent;
                    Player player = ClientUtil.getClientPlayer();
                    if (player != null && (soundEvent = SoundRegistry.getSoundEvent(soundName = (soundKeyframeData = event.getKeyframeData()).getSound())) != null) {
                        player.playSound(soundEvent, 1.0f, 1.0f);
                    }
                });
                reloadAnimationControllers.add(reloadAnimationController);
            }
        }
        return reloadAnimationControllers;
    }

    private TimerController createReloadTimerController() {
        TimerController reloadTimerController;
        if (this.phasedReloads.isEmpty()) {
            reloadTimerController = new TimerController(this, this.reloadCooldownTime){

                @Override
                public void onStartReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
                    this.reset();
                }
            };
            for (Tuple<Long, AbstractProceduralAnimationController> t : this.reloadEffectControllers) {
                reloadTimerController.schedule(ReloadPhase.RELOADING, (Long)t.getA(), TimeUnit.MILLISECOND, (AbstractProceduralAnimationController)t.getB(), null);
            }
        } else {
            long maxReloadDuration = 0L;
            for (PhasedReload phasedReload : this.phasedReloads) {
                long conditionalReloadTimeMillis = phasedReload.timeUnit.toMillis(phasedReload.cooldownTime);
                if (conditionalReloadTimeMillis <= maxReloadDuration) continue;
                maxReloadDuration = conditionalReloadTimeMillis;
            }
            reloadTimerController = new TimerController(this, maxReloadDuration){

                @Override
                public void onStartReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
                    this.reset();
                }

                @Override
                public void onCompleteReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
                    this.reset();
                }

                @Override
                public void onPrepareReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
                    this.reset();
                }
            };
            for (PhasedReload phasedReload : this.phasedReloads) {
                ReloadAnimation reloadAnimation = phasedReload.reloadAnimation;
                Predicate<ConditionContext> combinedPredicate = ctx -> ctx.gunClientState().isReloading() && phasedReload.predicate.test((ConditionContext)ctx);
                for (ReloadShakeEffect effect : reloadAnimation.shakeEffects) {
                    ViewShakeAnimationController2 controller = new ViewShakeAnimationController2(effect.initialAmplitude, effect.rateOfAmplitudeDecay, effect.initialAngularFrequency, effect.rateOfFrequencyIncrease, effect.duration);
                    reloadTimerController.schedule(phasedReload.phase, phasedReload.timeUnit.toMillis(effect.startTime), TimeUnit.MILLISECOND, controller, combinedPredicate);
                }
            }
        }
        return reloadTimerController;
    }

    public Map<String, AnimationController<GeoAnimatable>> getGeoAnimationControllers(ItemStack itemStack) {
        long geoId = GeoItem.getId((ItemStack)itemStack);
        AnimatableManager animatableManager = this.cache.getManagerForId(geoId);
        return animatableManager.getAnimationControllers();
    }

    public Map<String, AnimationController<GeoAnimatable>> getGeoAnimationControllers(ItemStack itemStack, ItemDisplayContext perspective) {
        long geoId = GeoItem.getId((ItemStack)itemStack);
        AnimatableManager animatableManager = this.cache.getManagerForId(geoId);
        if (perspective != null && animatableManager instanceof ContextAwareAnimatableManager) {
            ContextAwareAnimatableManager contextAware = (ContextAwareAnimatableManager)animatableManager;
            AnimatableManager perspectiveManager = contextAware.getManagerForContext((Object)perspective);
            return perspectiveManager.getAnimationControllers();
        }
        return animatableManager.getAnimationControllers();
    }

    public AnimationController<GeoAnimatable> getGeoAnimationController(String controllerId, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = this.getGeoAnimationControllers(itemStack);
        return controllers.get(controllerId);
    }

    public AnimationController<GeoAnimatable> getGeoAnimationController(String controllerId, ItemStack itemStack, ItemDisplayContext perspective) {
        return this.getGeoAnimationControllers(itemStack, perspective).get(controllerId);
    }

    public boolean isPerspectiveAware() {
        return true;
    }

    public ResourceLocation getTargetLockOverlay() {
        return this.targetLockOverlay;
    }

    @Override
    public long getTargetLockTimeTicks() {
        return this.targetLockTimeTicks;
    }

    @Override
    public void onTargetStartLocking(Entity targetEntity) {
        LOGGER.debug("Locking target: {}", (Object)targetEntity);
        if (this.targetStartLockingSound != null) {
            Player player = ClientUtil.getClientPlayer();
            MiscUtil.getLevel((Entity)player).playSound(player, player.getX(), player.getY(), player.getZ(), this.targetStartLockingSound, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    @Override
    public void onTargetLocked(Entity targetEntity) {
        GunClientState state;
        LOGGER.debug("Target locked: {}", (Object)targetEntity);
        Player player = ClientUtil.getClientPlayer();
        if (this.targetLockedSound != null) {
            MiscUtil.getLevel((Entity)player).playSound(player, player.getX(), player.getY(), player.getZ(), this.targetLockedSound, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        if ((state = GunClientState.getMainHeldState()) != null) {
            state.publishMessage((Component)Component.translatable((String)"message.pointblank.targetAcquired").append(": ").append(targetEntity.getName()).append(". ").append((Component)Component.translatable((String)"message.pointblank.distance").append(": ").append("" + Math.round(targetEntity.distanceTo((Entity)player)))), 1000L, s -> true);
        }
    }

    @Override
    public void onTargetStartUnlocking(Entity targetEntity) {
        LOGGER.debug("Target unlocked: {}", (Object)targetEntity);
    }

    @Override
    public float getPrice() {
        return this.tradePrice;
    }

    @Override
    public int getBundleQuantity() {
        return this.tradeBundleQuantity;
    }

    @Override
    public int getTradeLevel() {
        return this.tradeLevel;
    }

    public float getBobbing() {
        return this.bobbing;
    }

    public float getBobbingOnAim() {
        return this.bobbingOnAim;
    }

    public float getBobbingRollMultiplier() {
        return this.bobbingRollMultiplier;
    }

    public double getJumpMultiplier() {
        return this.jumpMultiplier;
    }

    public double getPipScopeZoom() {
        return this.pipScopeZoom;
    }

    public void handleAimingChangeRequest(Player player, ItemStack itemStack, UUID stateId, int slotIndex, boolean isAiming) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return;
        }
        isAiming = slotIndex != 40 && isAiming;
        CompoundTag idTag = MiscUtil.getTag(itemStack);
        if (idTag != null) {
            idTag.putBoolean("aim", isAiming && this.isAimingEnabled);
            MiscUtil.setTag(itemStack, idTag);
        }
        if (player.isSprinting() && isAiming && this.isAimingEnabled) {
            player.setSprinting(false);
        }
    }

    public static boolean isAiming(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return false;
        }
        GunItem gunItem = (GunItem)item;
        CompoundTag idTag = MiscUtil.getTag(itemStack);
        if (idTag != null) {
            return gunItem.isAimingEnabled && idTag.getBoolean("aim");
        }
        return false;
    }

    @Override
    public int getMaxAttachmentCategories() {
        return 11;
    }

    public List<Attachment> getDefaultAttachments() {
        return this.defaultAttachmentSuppliers.stream().map(Supplier::get).toList();
    }

    @Override
    public Collection<Attachment> getCompatibleAttachments() {
        if (this.compatibleAttachments == null) {
            HashSet<AttachmentCategory> attachmentCategories = new HashSet<AttachmentCategory>();
            LinkedHashSet<Attachment> compatibleAttachments = new LinkedHashSet<Attachment>();
            for (Attachment attachment : this.getDefaultAttachments()) {
                if (attachmentCategories.size() >= this.getMaxAttachmentCategories()) {
                    LOGGER.warn("Cannot add compatible attachment {} with category {} to  item {} because the existing number of compatible categories for this item cannot exceed {}", (Object)attachment.getName(), (Object)attachment.getCategory(), (Object)this.getName(), (Object)this.getMaxAttachmentCategories());
                    break;
                }
                attachmentCategories.add(attachment.getCategory());
                compatibleAttachments.add(attachment);
            }
            for (Supplier supplier : this.compatibleAttachmentSuppliers) {
                Attachment attachment = (Attachment)supplier.get();
                if (attachmentCategories.size() >= this.getMaxAttachmentCategories()) {
                    LOGGER.warn("Cannot add compatible attachment {} with category {} to  item {} because the existing number of compatible categories for this item cannot exceed {}", (Object)attachment.getName(), (Object)attachment.getCategory(), (Object)this.getName(), (Object)this.getMaxAttachmentCategories());
                    break;
                }
                attachmentCategories.add(attachment.getCategory());
                compatibleAttachments.add(attachment);
            }
            block2: for (String string : this.compatibleAttachmentGroups) {
                List<Supplier<? extends Item>> groupAtttachments = ItemRegistry.ITEMS.getAttachmentsForGroup(string);
                for (Supplier<? extends Item> ga : groupAtttachments) {
                    Item item = ga.get();
                    if (!(item instanceof Attachment)) continue;
                    Attachment attachment = (Attachment)item;
                    if (attachmentCategories.size() >= this.getMaxAttachmentCategories()) {
                        LOGGER.warn("Cannot add compatible attachment {} with category {} to  item {} because the existing number of compatible categories for this item cannot exceed {}", (Object)attachment.getName(), (Object)attachment.getCategory(), (Object)this.getName(), (Object)this.getMaxAttachmentCategories());
                        continue block2;
                    }
                    compatibleAttachments.add(attachment);
                }
            }
            this.compatibleAttachments = compatibleAttachments;
        }
        return this.compatibleAttachments;
    }

    @Override
    public <T extends Feature> T getFeature(Class<T> featureClass) {
        return (T)((Feature)featureClass.cast(this.features.get(featureClass)));
    }

    @Override
    public long getCraftingDuration() {
        return this.craftingDuration;
    }

    public SoundEvent getFireSound() {
        return this.fireSound;
    }

    public float getFireSoundVolume() {
        return this.fireSoundVolume;
    }

    public static ItemStack getMainHeldGunItemStack(LivingEntity player) {
        ItemStack itemStack;
        Player visiblePlayer;
        if (player instanceof Player) {
            visiblePlayer = (Player)player;
            v0 = GunHandSwapCoordinator.getInstance().visibleMainHandStack(visiblePlayer);
        } else {
            v0 = itemStack = player.getMainHandItem();
        }
        if (itemStack != null && itemStack.getItem() instanceof GunItem) {
            return itemStack;
        }
        if (player instanceof Player) {
            visiblePlayer = (Player)player;
            OperableGunContext offhand = GunItem.resolveOffhandGunContext(visiblePlayer);
            return offhand != null ? offhand.itemStack() : null;
        }
        return null;
    }

    public boolean hasIdleAnimations() {
        return this.idleAnimationProvider != null;
    }

    public AnimationType getAnimationType() {
        return this.animationType;
    }

    public String getThirdPersonFallbackAnimations() {
        return this.thirdPersonFallbackAnimations;
    }

    public static class Builder
    extends HurtingItem.Builder<Builder>
    implements Nameable {
        private static final float DEFAULT_PRICE = Float.NaN;
        private static final int DEFAULT_TRADE_LEVEL = 0;
        private static final int DEFAULT_TRADE_BUNDLE_QUANTITY = 1;
        private static final int DEFAULT_VIEW_RECOIL_DURATION = 100;
        private static final int DEFAULT_SHAKE_RECOIL_DURATION = 400;
        private static final int DEFAULT_GUN_RECOIL_DURATION = 500;
        private static final int DEFAULT_IDLE_RANDOMIZATION_DURATION = 2500;
        private static final int DEFAULT_RECOIL_RANDOMIZATION_DURATION = 250;
        private static final int DEFAULT_DRAW_COOLDOWN_DURATION = 500;
        private static final int DEFAULT_PREPARE_IDLE_COOLDOWN_DURATION = 0;
        private static final int DEFAULT_INSPECT_COOLDOWN_DURATION = 1000;
        private static final int DEFAULT_CRAFTING_DURATION = 1000;
        private static final float DEFAULT_HIT_SCAN_SPEED = 800.0f;
        private static final float DEFAULT_HIT_SCAN_ACCELERATION = 0.0f;
        private static final double DEFAULT_VIEW_RECOIL_AMPLITUDE = 1.0;
        private static final double DEFAULT_SHAKE_RECOIL_AMPLITUDE = 0.5;
        private static final int DEFAULT_VIEW_RECOIL_MAX_PITCH = 20;
        private static final double DEFAULT_SHAKE_RECOIL_SPEED = 8.0;
        private static final double DEFAULT_SHAKE_DECAY = 0.98;
        private static final double DEFAULT_GUN_RECOIL_INITIAL_AMPLITUDE = 0.3;
        private static final double DEFAULT_GUN_RECOIL_RATE_OF_AMPLITUDE_DECAY = 0.8;
        private static final double DEFAULT_GUN_RECOIL_INITIAL_ANGULAR_FREQUENCY = 1.0;
        private static final double DEFAULT_GUN_RECOIL_RATE_OF_FREQUENCY_INCREASE = 0.05;
        private static final double DEFAULT_GUN_RANDOMIZATION_AMPLITUDE = 0.01;
        private static final double DEFAULT_GUN_RECOIL_PITCH_MULTIPLIER = 1.0;
        private static final double DEFAULT_JUMP_MULTIPLIER = 1.0;
        private static final double DEFAULT_RELOAD_SHAKE_INITIAL_AMPLITUDE = 0.15;
        private static final double DEFAULT_RELOAD_SHAKE_RATE_OF_AMPLITUDE_DECAY = 0.3;
        private static final double DEFAULT_RELOAD_SHAKE_INITIAL_ANGULAR_FREQUENCY = 1.0;
        private static final double DEFAULT_RELOAD_SHAKE_RATE_OF_FREQUENCY_INCREASE = 0.01;
        private static final int DEFAULT_BURST_SHOTS = 3;
        private static final int DEFAULT_RELOAD_COOLDOWN_TIME = 1000;
        public static final double DEFAULT_AIMING_CURVE_X = 0.0;
        public static final double DEFAULT_AIMING_CURVE_Y = -0.07;
        public static final double DEFAULT_AIMING_CURVE_Z = 0.3;
        public static final double DEFAULT_AIMING_CURVE_PITCH = -0.01;
        public static final double DEFAULT_AIMING_CURVE_YAW = -0.01;
        public static final double DEFAULT_AIMING_CURVE_ROLL = -0.01;
        public static final double DEFAULT_AIMING_ZOOM = 0.05;
        public static final double DEFAULT_PELLET_SPREAD = 1.0;
        private static final double DEFAULT_INACCURACY = 0.03;
        private static final double DEFAULT_INACCURACY_AIMING = 0.0;
        private static final double DEFAULT_INACCURACY_SPRINTING = 0.1;
        private static final int DEFAULT_RPM = 600;
        private static final double DEFAULT_OVERHEAT_MAX_HEAT = 0.0;
        private static final double DEFAULT_OVERHEAT_HEAT_PER_SHOT = 0.0;
        private static final double DEFAULT_OVERHEAT_COOLDOWN_PER_SECOND = 0.0;
        private static final float DEFAULT_FIRE_SOUND_VOLUME = 5.0f;
        public static final int DEFAULT_MAX_AMMO_PER_RELOAD_ITERATION = Integer.MAX_VALUE;
        private static final String DEFAULT_RETICLE_OVERLAY = "textures/item/reticle.png";
        public static final int MAX_PELLET_SHOOTING_RANGE = 50;
        private static final float DEFAULT_BOBBING = 1.0f;
        private static final float DEFAULT_BOBBING_ON_AIM = 0.3f;
        private static final float DEFAULT_BOBBING_ROLL_MULTIPLIER = 1.0f;
        private long targetLockTimeTicks;
        private double viewRecoilAmplitude = 1.0;
        private double shakeRecoilAmplitude = 0.5;
        private int viewRecoilMaxPitch = 20;
        private long viewRecoilDuration = 100L;
        private double shakeRecoilSpeed = 8.0;
        private double shakeDecay = 0.98;
        private long shakeRecoilDuration = 400L;
        private double gunRecoilInitialAmplitude = 0.3;
        private double gunRecoilRateOfAmplitudeDecay = 0.8;
        private double gunRecoilInitialAngularFrequency = 1.0;
        private double gunRecoilRateOfFrequencyIncrease = 0.05;
        private double gunRandomizationAmplitude = 0.01;
        private double gunRecoilPitchMultiplier = 1.0;
        private long gunRecoilDuration = 500L;
        private double jumpMultiplier = 1.0;
        private int shotsPerRecoil = 1;
        private boolean sustainedRecoilEnabled = false;
        private Double sustainedRecoilPitchIncrement = null;
        private Double sustainedRecoilPosYIncrement = null;
        private Double sustainedRecoilPosZIncrement = null;
        private Double sustainedRecoilYawMultiplier = null;
        private Double sustainedRecoilPitchCap = null;
        private Double sustainedRecoilPosYCap = null;
        private Double sustainedRecoilPosZCap = null;
        private boolean sustainedRecoilOnAim = false;
        private Long sustainedRecoilDecayTime = null;
        private int shotsPerTrace = 1;
        private long idleRandomizationDuration = 2500L;
        private long recoilRandomizationDuration = 250L;
        private int burstShots = 3;
        private long prepareIdleCooldownDuration = 0L;
        private long prepareFireCooldownDuration = 0L;
        private long completeFireCooldownDuration = 0L;
        private long enableFireModeCooldownDuration = 0L;
        private long craftingDuration = 1000L;
        private String name;
        private float tradePrice = Float.NaN;
        private int tradeBundleQuantity = 1;
        private int tradeLevel = 0;
        private int rpm = 600;
        private double overheatMaxHeat = 0.0;
        private double overheatHeatPerShot = 0.0;
        private double overheatCooldownPerSecond = 0.0;
        private long reloadCooldownTime;
        private String reloadAnimation;
        private int maxAmmoCapacity;
        private int maxAmmoPerReloadIteration = Integer.MAX_VALUE;
        private FireMode[] fireModes;
        private Set<Supplier<AmmoItem>> compatibleAmmo = new LinkedHashSet<Supplier<AmmoItem>>();
        private Supplier<SoundEvent> fireSound;
        private float fireSoundVolume = 5.0f;
        private Supplier<SoundEvent> targetLockedSound;
        private Supplier<SoundEvent> targetStartLockingSound;
        private boolean isAimingEnabled = true;
        private boolean dualWielding;
        private double aimingCurveX = 0.0;
        private double aimingCurveY = -0.07;
        private double aimingCurveZ = 0.3;
        private double aimingCurvePitch = -0.01;
        private double aimingCurveYaw = -0.01;
        private double aimingCurveRoll = -0.01;
        private double aimingZoom = 0.05;
        private double pipScopeZoom = 0.0;
        private List<Tuple<Long, AbstractProceduralAnimationController>> reloadEffectControllers;
        private String scopeOverlay;
        private String reticleOverlay;
        private String targetLockOverlay;
        private String ammoUi;
        private List<PhasedReload> phasedReloads = new ArrayList<PhasedReload>();
        private ConditionalAnimationProvider.Builder drawAnimationsBuilder = new ConditionalAnimationProvider.Builder();
        private ConditionalAnimationProvider.Builder hideAnimationsBuilder = new ConditionalAnimationProvider.Builder();
        private ConditionalAnimationProvider.Builder idleAnimationBuilder = new ConditionalAnimationProvider.Builder();
        private ConditionalAnimationProvider.Builder inspectAnimationsBuilder = new ConditionalAnimationProvider.Builder();
        private ConditionalAnimationProvider.Builder fireAnimationsBuilder = new ConditionalAnimationProvider.Builder();
        private int pelletCount = 0;
        private double pelletSpread = 1.0;
        private double inaccuracy = 0.03;
        private double inaccuracyAiming = 0.0;
        private double inaccuracySprinting = 0.1;
        private List<GlowAnimationController.Builder> glowEffectBuilders = new ArrayList<GlowAnimationController.Builder>();
        private List<RotationAnimationController.Builder> rotationEffectBuilders = new ArrayList<RotationAnimationController.Builder>();
        private Map<FirePhase, List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>>> effectBuilders = new HashMap();
        private float hitScanSpeed = 800.0f;
        private float hitScanAcceleration = 0.0f;
        private float bobbing = 1.0f;
        private float bobbingOnAim = 0.3f;
        private float bobbingRollMultiplier = 1.0f;
        private float modelScale = 1.0f;
        private float adsZOffset = 0.0f;
        private AnimationType animationType = AnimationType.RIFLE;
        private List<Supplier<Attachment>> compatibleAttachments = new ArrayList<Supplier<Attachment>>();
        private List<String> compatibleAttachmentGroups = new ArrayList<String>();
        private List<FeatureBuilder<?, ?>> featureBuilders = new ArrayList();
        private List<Supplier<Attachment>> defaultAttachments = new ArrayList<Supplier<Attachment>>();
        private String playerAnimation = "__DEFAULT_RIFLE_ANIMATIONS__";
        private String firstPersonFallbackAnimations;
        private String thirdPersonFallbackAnimations;

        public Builder() {
            this.reloadEffectControllers = new ArrayList<Tuple<Long, AbstractProceduralAnimationController>>();
        }

        @Override
        public String getName() {
            return this.name;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withAnimationType(AnimationType animationType) {
            this.animationType = animationType;
            return this;
        }

        public Builder withFirstPersonFallbackAnimations(String firstPersonFallbackAnimations) {
            this.firstPersonFallbackAnimations = firstPersonFallbackAnimations;
            return this;
        }

        public Builder withThirdPersonFallbackAnimations(String thirdPersonFallbackAnimations) {
            this.thirdPersonFallbackAnimations = thirdPersonFallbackAnimations;
            return this;
        }

        @SafeVarargs
        public final Builder withDefaultAttachment(Supplier<? extends Attachment> ... attachmentSuppliers) {
            for (Supplier<? extends Attachment> s : attachmentSuppliers) {
                this.defaultAttachments.add(s::get);
            }
            return this;
        }

        @SafeVarargs
        public final Builder withCompatibleAttachment(Supplier<? extends Attachment> ... attachmentSuppliers) {
            for (Supplier<? extends Attachment> s : attachmentSuppliers) {
                this.compatibleAttachments.add(s::get);
            }
            return this;
        }

        public Builder withCompatibleAttachmentGroup(String ... groups) {
            this.compatibleAttachmentGroups.addAll(Set.of(groups));
            return this;
        }

        public Builder withFeature(FeatureBuilder<?, ?> featureBuilder) {
            this.featureBuilders.add(featureBuilder);
            return this;
        }

        public Builder withModelScale(double modelScale) {
            this.modelScale = Mth.clamp((float)((float)modelScale), (float)0.1f, (float)1.0f);
            return this;
        }

        public Builder withAdsZOffset(double adsZOffset) {
            this.adsZOffset = (float)adsZOffset;
            return this;
        }

        public Builder withTradePrice(float price, int tradeBundleQuantity, int tradeLevel) {
            this.tradePrice = price;
            this.tradeLevel = tradeLevel;
            this.tradeBundleQuantity = tradeBundleQuantity;
            return this;
        }

        public Builder withTradePrice(float price, int tradeLevel) {
            return this.withTradePrice(price, 1, tradeLevel);
        }

        public Builder withMaxAmmoCapacity(int maxAmmoCapacity) {
            this.maxAmmoCapacity = maxAmmoCapacity;
            return this;
        }

        public Builder withMaxAmmoPerReloadIteration(int maxAmmoPerReloadIteration) {
            this.maxAmmoPerReloadIteration = maxAmmoPerReloadIteration;
            return this;
        }

        public Builder withRpm(int rpm) {
            this.rpm = rpm;
            return this;
        }

        public Builder withOverheatMaxHeat(double overheatMaxHeat) {
            this.overheatMaxHeat = Math.max(0.0, overheatMaxHeat);
            return this;
        }

        public Builder withOverheatHeatPerShot(double overheatHeatPerShot) {
            this.overheatHeatPerShot = Math.max(0.0, overheatHeatPerShot);
            return this;
        }

        public Builder withOverheatCooldownPerSecond(double overheatCooldownPerSecond) {
            this.overheatCooldownPerSecond = Math.max(0.0, overheatCooldownPerSecond);
            return this;
        }

        public Builder withReloadAnimation(String reloadAnimation) {
            this.reloadAnimation = reloadAnimation;
            return this;
        }

        public Builder withReloadCooldownDuration(long reloadCooldownTime, TimeUnit timeUnit) {
            this.reloadCooldownTime = timeUnit.toMillis(reloadCooldownTime);
            return this;
        }

        public Builder withFireModes(FireMode ... fireModes) {
            this.fireModes = fireModes;
            return this;
        }

        public Builder withCraftingDuration(int duration, TimeUnit timeUnit) {
            this.craftingDuration = timeUnit.toMillis(duration);
            return this;
        }

        @SafeVarargs
        public final Builder withCompatibleAmmo(Supplier<AmmoItem> ... ammo) {
            for (Supplier<AmmoItem> b : ammo) {
                this.compatibleAmmo.add(b);
            }
            return this;
        }

        public final Builder withCompatibleAmmo(List<Supplier<AmmoItem>> ammo) {
            this.compatibleAmmo.addAll(ammo);
            return this;
        }

        public final Builder withTargetLock(int minTargetLockTime, TimeUnit timeUnit) {
            this.targetLockTimeTicks = timeUnit.toTicks(minTargetLockTime);
            return this;
        }

        public Builder withViewRecoilAmplitude(double amplitude) {
            this.viewRecoilAmplitude = amplitude;
            return this;
        }

        public Builder withShakeRecoilAmplitude(double amplitude) {
            this.shakeRecoilAmplitude = amplitude;
            return this;
        }

        public Builder withViewRecoilMaxPitch(int pitch) {
            this.viewRecoilMaxPitch = pitch;
            return this;
        }

        public Builder withViewRecoilDuration(int duration, TimeUnit timeUnit) {
            this.viewRecoilDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withShakeRecoilSpeed(double speed) {
            this.shakeRecoilSpeed = speed;
            return this;
        }

        public Builder withShakeDecay(double shakeDecay) {
            this.shakeDecay = shakeDecay;
            return this;
        }

        public Builder withShakeRecoilDuration(int duration, TimeUnit timeUnit) {
            this.shakeRecoilDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withGunRecoilInitialAmplitude(double amplitude) {
            this.gunRecoilInitialAmplitude = amplitude;
            return this;
        }

        public Builder withGunRecoilRateOfAmplitudeDecay(double decayRate) {
            this.gunRecoilRateOfAmplitudeDecay = decayRate;
            return this;
        }

        public Builder withGunRecoilInitialAngularFrequency(double frequency) {
            this.gunRecoilInitialAngularFrequency = frequency;
            return this;
        }

        public Builder withGunRecoilRateOfFrequencyIncrease(double rate) {
            this.gunRecoilRateOfFrequencyIncrease = rate;
            return this;
        }

        public Builder withGunRecoilPitchMultiplier(double gunRecoilPitchMultiplier) {
            this.gunRecoilPitchMultiplier = gunRecoilPitchMultiplier;
            return this;
        }

        public Builder withGunRecoilDuration(int duration, TimeUnit timeUnit) {
            this.gunRecoilDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withJumpMultiplier(double jumpMultiplier) {
            this.jumpMultiplier = jumpMultiplier;
            return this;
        }

        public Builder withShotsPerRecoil(int shotsPerRecoil) {
            this.shotsPerRecoil = shotsPerRecoil;
            return this;
        }

        public Builder withSustainedRecoil() {
            this.sustainedRecoilEnabled = true;
            return this;
        }

        public Builder withSustainedRecoilPitch(double pitchIncrement) {
            this.sustainedRecoilEnabled = true;
            this.sustainedRecoilPitchIncrement = pitchIncrement;
            return this;
        }

        public Builder withSustainedRecoilPosY(double posYIncrement) {
            this.sustainedRecoilEnabled = true;
            this.sustainedRecoilPosYIncrement = posYIncrement;
            return this;
        }

        public Builder withSustainedRecoilPosZ(double posZIncrement) {
            this.sustainedRecoilEnabled = true;
            this.sustainedRecoilPosZIncrement = posZIncrement;
            return this;
        }

        public Builder withSustainedRecoilYawMultiplier(double yawMultiplier) {
            this.sustainedRecoilEnabled = true;
            this.sustainedRecoilYawMultiplier = yawMultiplier;
            return this;
        }

        public Builder withSustainedRecoilPitchCap(double pitchCap) {
            this.sustainedRecoilPitchCap = pitchCap;
            return this;
        }

        public Builder withSustainedRecoilPosYCap(double posYCap) {
            this.sustainedRecoilPosYCap = posYCap;
            return this;
        }

        public Builder withSustainedRecoilPosZCap(double posZCap) {
            this.sustainedRecoilPosZCap = posZCap;
            return this;
        }

        public Builder withSustainedRecoilOnAim(boolean enabled) {
            this.sustainedRecoilOnAim = enabled;
            return this;
        }

        public Builder withSustainedRecoilDecayTime(long decayTime) {
            this.sustainedRecoilDecayTime = decayTime;
            return this;
        }

        public Builder withShotsPerTrace(int shotsPerTrace) {
            this.shotsPerTrace = shotsPerTrace;
            return this;
        }

        public Builder withIdleRandomizationDuration(int duration, TimeUnit timeUnit) {
            this.idleRandomizationDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withRecoilRandomizationDuration(int duration, TimeUnit timeUnit) {
            this.recoilRandomizationDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withFireSound(Supplier<SoundEvent> fireSound) {
            this.fireSound = fireSound;
            return this;
        }

        public Builder withFireSound(Supplier<SoundEvent> fireSound, float fireSoundVolume) {
            this.fireSound = fireSound;
            this.fireSoundVolume = fireSoundVolume;
            return this;
        }

        public Builder withFireSound(SoundEvent fireSound) {
            this.fireSound = () -> fireSound;
            return this;
        }

        public Builder withFireSound(SoundEvent fireSound, float fireSoundVolume) {
            this.fireSound = () -> fireSound;
            this.fireSoundVolume = fireSoundVolume;
            return this;
        }

        public Builder withReloadSound(Supplier<SoundEvent> reloadSound) {
            return this;
        }

        public Builder withReloadSound(SoundEvent reloadSound) {
            return this;
        }

        public Builder withReloadShakeEffect(long startTime, long duration, TimeUnit timeUnit, double initialAmplitude, double rateOfAmplitudeDecay, double initialAngularFrequency, double rateOfFrequencyIncrease) {
            this.reloadEffectControllers.add((Tuple<Long, AbstractProceduralAnimationController>)new Tuple((Object)timeUnit.toMillis(startTime), (Object)new ViewShakeAnimationController2(initialAmplitude, rateOfAmplitudeDecay, initialAngularFrequency, rateOfFrequencyIncrease, duration)));
            return this;
        }

        public Builder withAimingEnabled(boolean aimingEnabled) {
            this.isAimingEnabled = aimingEnabled;
            return this;
        }

        public Builder withDualWielding(boolean dualWielding) {
            this.dualWielding = dualWielding;
            return this;
        }

        public Builder withAimingCurveX(double aimingCurveX) {
            this.aimingCurveX = aimingCurveX;
            return this;
        }

        public Builder withAimingCurveY(double aimingCurveY) {
            this.aimingCurveY = aimingCurveY;
            return this;
        }

        public Builder withAimingCurveZ(double aimingCurveZ) {
            this.aimingCurveZ = aimingCurveZ;
            return this;
        }

        public Builder withAimingCurvePitch(double aimingCurvePitch) {
            this.aimingCurvePitch = aimingCurvePitch;
            return this;
        }

        public Builder withAimingCurveYaw(double aimingCurveYaw) {
            this.aimingCurveYaw = aimingCurveYaw;
            return this;
        }

        public Builder withAimingCurveRoll(double aimingCurveRoll) {
            this.aimingCurveRoll = aimingCurveRoll;
            return this;
        }

        public Builder withAimingZoom(double aimingZoom) {
            this.aimingZoom = aimingZoom;
            return this;
        }

        public Builder withPipScopeZoom(double pipScopeZoom) {
            this.pipScopeZoom = pipScopeZoom;
            return this;
        }

        public Builder withScopeOverlay(String scopeOverlay) {
            this.scopeOverlay = scopeOverlay;
            return this;
        }

        public Builder withReticleOverlay(String reticleOverlay) {
            this.reticleOverlay = reticleOverlay;
            return this;
        }

        public Builder withTargetLockOverlay(String targetLockOverlay) {
            this.targetLockOverlay = targetLockOverlay;
            return this;
        }

        public Builder withAmmoUi(String ammoUi) {
            this.ammoUi = ammoUi;
            return this;
        }

        public Builder withTargetLockedSound(Supplier<SoundEvent> targetLockedSound) {
            this.targetLockedSound = targetLockedSound;
            return this;
        }

        public Builder withTargetLockedSound(SoundEvent targetLockedSound) {
            this.targetLockedSound = () -> targetLockedSound;
            return this;
        }

        public Builder withTargetStartLockingSound(Supplier<SoundEvent> targetStartLockingSound) {
            this.targetStartLockingSound = targetStartLockingSound;
            return this;
        }

        public Builder withTargetStartLockingSound(SoundEvent targetStartLockingSound) {
            this.targetStartLockingSound = () -> targetStartLockingSound;
            return this;
        }

        public Builder withReticleOverlay() {
            this.reticleOverlay = "textures/item/reticle.png";
            return this;
        }

        public Builder withPrepareFireCooldownDuration(int duration, TimeUnit timeUnit) {
            this.prepareFireCooldownDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withCompleteFireCooldownDuration(int duration, TimeUnit timeUnit) {
            this.completeFireCooldownDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withEnableFireModeCooldownDuration(int duration, TimeUnit timeUnit) {
            this.enableFireModeCooldownDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withPrepareIdleCooldownDuration(int duration, TimeUnit timeUnit) {
            this.prepareIdleCooldownDuration = timeUnit.toMillis(duration);
            return this;
        }

        public Builder withDrawCooldownDuration(int duration, TimeUnit timeUnit) {
            this.withDrawAnimation(GunItem.DEFAULT_ANIMATION_DRAW, ctx -> true, duration, timeUnit);
            return this;
        }

        public Builder withDrawAnimation(String animationName, Predicate<ConditionContext> predicate, int duration, TimeUnit timeUnit) {
            this.drawAnimationsBuilder.withAnimation(animationName, predicate, duration, timeUnit);
            return this;
        }

        public Builder withHideAnimation(String animationName, Predicate<ConditionContext> predicate, int duration, TimeUnit timeUnit) {
            if (animationName == null || animationName.isBlank()) {
                throw new IllegalArgumentException("Hide animation name cannot be blank for " + this.name);
            }
            if (duration < 0) {
                throw new IllegalArgumentException("Hide animation duration cannot be negative for " + this.name);
            }
            this.hideAnimationsBuilder.withAnimation(animationName, predicate, duration, timeUnit);
            return this;
        }

        public Builder withInspectCooldownDuration(int duration, TimeUnit timeUnit) {
            this.withInspectAnimation(GunItem.DEFAULT_ANIMATION_INSPECT, ctx -> true, duration, timeUnit);
            return this;
        }

        public Builder withInspectAnimation(String animationName, Predicate<ConditionContext> predicate, int duration, TimeUnit timeUnit) {
            this.inspectAnimationsBuilder.withAnimation(animationName, predicate, duration, timeUnit);
            return this;
        }

        public Builder withIdleAnimation(String animationName, Predicate<ConditionContext> predicate, int duration, TimeUnit timeUnit) {
            this.idleAnimationBuilder.withAnimation(animationName, predicate, duration, timeUnit);
            return this;
        }

        public Builder withFireAnimation(String animationName, Predicate<ConditionContext> predicate, int duration, TimeUnit timeUnit) {
            this.fireAnimationsBuilder.withAnimation(animationName, predicate, duration, timeUnit);
            return this;
        }

        public Builder withPhasedReload(PhasedReload phasedReload) {
            this.phasedReloads.add(phasedReload);
            return this;
        }

        public Builder withPhasedReload(ReloadPhase phase, long cooldownTime, String animationName) {
            this.phasedReloads.add(new PhasedReload(phase, cooldownTime, animationName));
            return this;
        }

        public Builder withPhasedReload(ReloadPhase phase, long cooldownTime, ReloadAnimation reloadAnimation) {
            this.phasedReloads.add(new PhasedReload(phase, cooldownTime, reloadAnimation));
            return this;
        }

        public Builder withPhasedReload(ReloadPhase phase, TriPredicate<LivingEntity, GunClientState, ItemStack> predicate, long cooldownTime, ReloadAnimation reloadAnimation) {
            this.phasedReloads.add(new PhasedReload(phase, predicate, cooldownTime, reloadAnimation));
            return this;
        }

        public Builder withPhasedReload(ReloadPhase phase, Predicate<ConditionContext> predicate, long cooldownTime, ReloadAnimation reloadAnimation) {
            this.phasedReloads.add(new PhasedReload(phase, predicate, cooldownTime, TimeUnit.MILLISECOND, reloadAnimation));
            return this;
        }

        public Builder withPelletCount(int pelletCount) {
            this.pelletCount = pelletCount;
            return this;
        }

        public Builder withPelletSpread(double pelletSpread) {
            this.pelletSpread = pelletSpread;
            return this;
        }

        public Builder withGunRandomizationAmplitude(double gunRandomizationAmplitude) {
            this.gunRandomizationAmplitude = gunRandomizationAmplitude;
            return this;
        }

        public Builder withBurstShots(int burstShots) {
            this.burstShots = burstShots;
            return this;
        }

        public Builder withInaccuracy(double inaccuracy) {
            this.inaccuracy = inaccuracy;
            return this;
        }

        public Builder withInaccuracyAiming(double inaccuracyAiming) {
            this.inaccuracyAiming = inaccuracyAiming;
            return this;
        }

        public Builder withInaccuracySprinting(double inaccuracySprinting) {
            this.inaccuracySprinting = inaccuracySprinting;
            return this;
        }

        public Builder withGlow(String glowingPartName) {
            return this.withGlow(glowingPartName, null);
        }

        public Builder withGlow(String glowingPartName, String textureName) {
            return this.withGlow(Collections.singleton(FirePhase.ANY), Collections.singleton(glowingPartName), textureName);
        }

        public Builder withGlow(Collection<FirePhase> firePhases, String glowingPartName) {
            return this.withGlow(firePhases, Collections.singleton(glowingPartName), null);
        }

        public Builder withGlow(Collection<FirePhase> firePhases, Collection<String> glowingPartNames, String texture) {
            GlowAnimationController.Builder builder = new GlowAnimationController.Builder().withFirePhases(firePhases);
            if (texture != null) {
                builder.withTexture(ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)texture));
            }
            builder.withGlowingPartNames(glowingPartNames);
            this.glowEffectBuilders.add(builder);
            return this;
        }

        public Builder withGlow(Collection<FirePhase> firePhases, String glowingPartName, String texture, AbstractEffect.SpriteAnimationType spriteAnimationType, int spriteRows, int spriteColumns, int spritesPerSecond, Direction ... directions) {
            GlowAnimationController.Builder builder = new GlowAnimationController.Builder().withFirePhases(firePhases);
            if (texture != null) {
                builder.withTexture(ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)texture));
            }
            builder.withGlowingPartNames(Collections.singleton(glowingPartName));
            builder.withSprites(spriteRows, spriteColumns, spritesPerSecond, spriteAnimationType);
            builder.withDirections(directions);
            this.glowEffectBuilders.add(builder);
            return this;
        }

        public Builder withRotation(String phase, String modelPart, double rpm, double acceleration, double deceleration) {
            return this.withRotation(phase, modelPart, rpm, acceleration, deceleration, RotationAnimationController.RotationAxis.Z, RotationAnimationController.RotationDirection.CLOCKWISE);
        }

        public Builder withRotation(String phase, String modelPart, double rpm, double acceleration, double deceleration, RotationAnimationController.RotationAxis rotationAxis, RotationAnimationController.RotationDirection rotationDirection) {
            RotationAnimationController.Builder builder = new RotationAnimationController.Builder().withPhase(phase).withModelPart(modelPart).withAccelerationRate(acceleration).withDecelerationRate(deceleration).withRotationsPerMinute(rpm).withRotationAxis(rotationAxis).withRotationDirection(rotationDirection);
            this.rotationEffectBuilders.add(builder);
            return this;
        }

        public Builder withRotation(RotationAnimationController.PhaseMapper phaseMapper, String modelPart, double rpm, double acceleration, double deceleration) {
            return this.withRotation(phaseMapper, modelPart, rpm, acceleration, deceleration, RotationAnimationController.RotationAxis.Z, RotationAnimationController.RotationDirection.CLOCKWISE);
        }

        public Builder withRotation(RotationAnimationController.PhaseMapper phaseMapper, String modelPart, double rpm, double acceleration, double deceleration, RotationAnimationController.RotationAxis rotationAxis, RotationAnimationController.RotationDirection rotationDirection) {
            RotationAnimationController.Builder builder = new RotationAnimationController.Builder().withPhaseMapper(phaseMapper).withModelPart(modelPart).withAccelerationRate(acceleration).withDecelerationRate(deceleration).withRotationsPerMinute(rpm).withRotationAxis(rotationAxis).withRotationDirection(rotationDirection);
            this.rotationEffectBuilders.add(builder);
            return this;
        }

        public Builder withEffect(FirePhase firePhase, Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> effectBuilder) {
            List builders = this.effectBuilders.computeIfAbsent(firePhase, k -> new ArrayList());
            builders.add(effectBuilder);
            return this;
        }

        public Builder withHitScanSpeed(float hitScanSpeed) {
            this.hitScanSpeed = hitScanSpeed;
            return this;
        }

        public Builder withHitScanAcceleration(float hitScanAcceleration) {
            this.hitScanAcceleration = hitScanAcceleration;
            return this;
        }

        public Builder withBobbing(double bobbing) {
            this.bobbing = Mth.clamp((float)((float)bobbing), (float)0.0f, (float)2.0f);
            return this;
        }

        public Builder withBobbingOnAim(double bobbingOnAim) {
            this.bobbingOnAim = Mth.clamp((float)((float)bobbingOnAim), (float)0.0f, (float)2.0f);
            return this;
        }

        public Builder withBobbingRollMultiplier(double bobbingRollMultiplier) {
            this.bobbingRollMultiplier = Mth.clamp((float)((float)bobbingRollMultiplier), (float)0.0f, (float)10.0f);
            return this;
        }

        public Builder withPlayerAnimation(String playerAnimation) {
            this.playerAnimation = playerAnimation;
            return this;
        }

        @Override
        public GunItem build() {
            return this.build("pointblank");
        }

        public GunItem build(String namespace) {
            return new GunItem(this, namespace);
        }

        /*
         * WARNING - void declaration
         */
        @Override
        public Builder withJsonObject(JsonObject obj, boolean isClientSide) {
            List<JsonObject> list;
            JsonElement reloadSoundElem;
            JsonElement targetStartLockingSoundElem;
            super.withJsonObject(obj, isClientSide);
            Builder builder = this;
            builder.withName(obj.getAsJsonPrimitive("name").getAsString());
            builder.withAnimationType(JsonUtil.getEnum(obj, "animationType", AnimationType.class, AnimationType.RIFLE, true));
            builder.withFirstPersonFallbackAnimations(JsonUtil.getJsonString(obj, "firstPersonFallbackAnimations", null));
            builder.withThirdPersonFallbackAnimations(JsonUtil.getJsonString(obj, "thirdPersonFallbackAnimations", null));
            builder.withModelScale(JsonUtil.getJsonFloat(obj, "modelScale", 1.0f));
            builder.withAdsZOffset(JsonUtil.getJsonFloat(obj, "adsZOffset", 0.0f));
            builder.withTradePrice(JsonUtil.getJsonFloat(obj, "tradePrice", Float.NaN), JsonUtil.getJsonInt(obj, "traceBundleQuantity", 1), JsonUtil.getJsonInt(obj, "tradeLevel", 0));
            JsonPrimitive jsonMaxAmmoCapacity = obj.getAsJsonPrimitive("maxAmmoCapacity");
            if (jsonMaxAmmoCapacity.isString() && "infinite".equalsIgnoreCase(jsonMaxAmmoCapacity.getAsString())) {
                builder.withMaxAmmoCapacity(Integer.MAX_VALUE);
            } else {
                builder.withMaxAmmoCapacity(obj.getAsJsonPrimitive("maxAmmoCapacity").getAsInt());
            }
            builder.withCraftingDuration(JsonUtil.getJsonInt(obj, "craftingDuration", 1000), TimeUnit.MILLISECOND);
            builder.withMaxAmmoPerReloadIteration(JsonUtil.getJsonInt(obj, "maxAmmoPerReloadIteration", Integer.MAX_VALUE));
            builder.withAimingEnabled(JsonUtil.getJsonBoolean(obj, "aimingEnabled", true));
            builder.withDualWielding(JsonUtil.getJsonBoolean(obj, "dualWielding", false));
            builder.withTargetLock(JsonUtil.getJsonInt(obj, "minTargetLockTime", 0), TimeUnit.MILLISECOND);
            builder.withRpm(JsonUtil.getJsonInt(obj, "rpm", 600));
            if (obj.has("overheating")) {
                JsonObject overheatingObj = obj.getAsJsonObject("overheating");
                builder.withFeature(new OverheatFeature.Builder().withJsonObject(overheatingObj));
            }
            builder.withPrepareIdleCooldownDuration(JsonUtil.getJsonInt(obj, "prepareIdleCooldownDuration", 0), TimeUnit.MILLISECOND);
            builder.withViewRecoilMaxPitch(JsonUtil.getJsonInt(obj, "viewRecoilMaxPitch", 20));
            builder.withViewRecoilDuration(JsonUtil.getJsonInt(obj, "viewRecoilDuration", 100), TimeUnit.MILLISECOND);
            builder.withShakeRecoilDuration(JsonUtil.getJsonInt(obj, "shakeRecoilDuration", 400), TimeUnit.MILLISECOND);
            builder.withGunRecoilDuration(JsonUtil.getJsonInt(obj, "gunRecoilDuration", 500), TimeUnit.MILLISECOND);
            builder.withIdleRandomizationDuration(JsonUtil.getJsonInt(obj, "idleRandomizationDuration", 2500), TimeUnit.MILLISECOND);
            builder.withRecoilRandomizationDuration(JsonUtil.getJsonInt(obj, "recoilRandomizationDuration", 250), TimeUnit.MILLISECOND);
            builder.withBurstShots(JsonUtil.getJsonInt(obj, "burstShots", 3));
            builder.withReloadCooldownDuration(JsonUtil.getJsonInt(obj, "reloadCooldownTime", 1000), TimeUnit.MILLISECOND);
            builder.withPelletCount(JsonUtil.getJsonInt(obj, "pelletCount", 0));
            builder.withPrepareFireCooldownDuration(JsonUtil.getJsonInt(obj, "prepareFireCooldownDuration", 0), TimeUnit.MILLISECOND);
            builder.withCompleteFireCooldownDuration(JsonUtil.getJsonInt(obj, "completeFireCooldownDuration", 0), TimeUnit.MILLISECOND);
            builder.withEnableFireModeCooldownDuration(JsonUtil.getJsonInt(obj, "enableFireModeCooldownDuration", 0), TimeUnit.MILLISECOND);
            builder.withViewRecoilAmplitude(JsonUtil.getJsonDouble(obj, "viewRecoilAmplitude", 1.0));
            builder.withShakeRecoilAmplitude(JsonUtil.getJsonDouble(obj, "shakeRecoilAmplitude", 0.5));
            builder.withShakeRecoilSpeed(JsonUtil.getJsonDouble(obj, "shakeRecoilSpeed", 8.0));
            builder.withShakeDecay(JsonUtil.getJsonDouble(obj, "shakeDecay", 0.98));
            builder.withGunRecoilInitialAmplitude(JsonUtil.getJsonDouble(obj, "gunRecoilInitialAmplitude", 0.3));
            builder.withGunRecoilRateOfAmplitudeDecay(JsonUtil.getJsonDouble(obj, "gunRecoilRateOfAmplitudeDecay", 0.8));
            builder.withGunRecoilInitialAngularFrequency(JsonUtil.getJsonDouble(obj, "gunRecoilInitialAngularFrequency", 1.0));
            builder.withGunRecoilRateOfFrequencyIncrease(JsonUtil.getJsonDouble(obj, "gunRecoilRateOfFrequencyIncrease", 0.05));
            builder.withGunRecoilPitchMultiplier(JsonUtil.getJsonDouble(obj, "gunRecoilPitchMultiplier", 1.0));
            builder.withGunRandomizationAmplitude(JsonUtil.getJsonDouble(obj, "gunRandomizationAmplitude", 0.01));
            builder.withAimingCurveX(JsonUtil.getJsonDouble(obj, "aimingCurveX", 0.0));
            builder.withAimingCurveY(JsonUtil.getJsonDouble(obj, "aimingCurveY", -0.07));
            builder.withAimingCurveZ(JsonUtil.getJsonDouble(obj, "aimingCurveZ", 0.3));
            builder.withAimingCurvePitch(JsonUtil.getJsonDouble(obj, "aimingCurvePitch", -0.01));
            builder.withAimingCurveYaw(JsonUtil.getJsonDouble(obj, "aimingCurveYaw", -0.01));
            builder.withAimingCurveRoll(JsonUtil.getJsonDouble(obj, "aimingCurveRoll", -0.01));
            builder.withAimingZoom(JsonUtil.getJsonDouble(obj, "aimingZoom", 0.05));
            builder.withPipScopeZoom(JsonUtil.getJsonDouble(obj, "pipScopeZoom", 0.0));
            builder.withShotsPerRecoil(JsonUtil.getJsonInt(obj, "shotsPerRecoil", 1));
            builder.withShotsPerTrace(JsonUtil.getJsonInt(obj, "shotsPerTrace", 1));
            if (obj.has("sustainedRecoil")) {
                JsonElement sustainedRecoilElem = obj.get("sustainedRecoil");
                if (sustainedRecoilElem.isJsonPrimitive() && sustainedRecoilElem.getAsJsonPrimitive().isBoolean()) {
                    if (sustainedRecoilElem.getAsBoolean()) {
                        builder.withSustainedRecoil();
                    }
                } else if (sustainedRecoilElem.isJsonObject()) {
                    builder.withSustainedRecoil();
                    JsonObject sustainedRecoilObj = sustainedRecoilElem.getAsJsonObject();
                    if (sustainedRecoilObj.has("pitchIncrement")) {
                        builder.withSustainedRecoilPitch(sustainedRecoilObj.get("pitchIncrement").getAsDouble());
                    }
                    if (sustainedRecoilObj.has("posYIncrement")) {
                        builder.withSustainedRecoilPosY(sustainedRecoilObj.get("posYIncrement").getAsDouble());
                    }
                    if (sustainedRecoilObj.has("posZIncrement")) {
                        builder.withSustainedRecoilPosZ(sustainedRecoilObj.get("posZIncrement").getAsDouble());
                    }
                    if (sustainedRecoilObj.has("yawMultiplier")) {
                        builder.withSustainedRecoilYawMultiplier(sustainedRecoilObj.get("yawMultiplier").getAsDouble());
                    }
                    if (sustainedRecoilObj.has("pitchCap")) {
                        builder.withSustainedRecoilPitchCap(sustainedRecoilObj.get("pitchCap").getAsDouble());
                    }
                    if (sustainedRecoilObj.has("posYCap")) {
                        builder.withSustainedRecoilPosYCap(sustainedRecoilObj.get("posYCap").getAsDouble());
                    }
                    if (sustainedRecoilObj.has("posZCap")) {
                        builder.withSustainedRecoilPosZCap(sustainedRecoilObj.get("posZCap").getAsDouble());
                    }
                    if (sustainedRecoilObj.has("onAim")) {
                        builder.withSustainedRecoilOnAim(sustainedRecoilObj.get("onAim").getAsBoolean());
                    }
                    if (sustainedRecoilObj.has("decayTime")) {
                        builder.withSustainedRecoilDecayTime(sustainedRecoilObj.get("decayTime").getAsLong());
                    }
                }
            }
            builder.withPelletSpread(JsonUtil.getJsonDouble(obj, "pelletSpread", 1.0));
            builder.withInaccuracy(JsonUtil.getJsonDouble(obj, "inaccuracy", 0.03));
            builder.withInaccuracyAiming(JsonUtil.getJsonDouble(obj, "inaccuracyAiming", 0.0));
            builder.withInaccuracySprinting(JsonUtil.getJsonDouble(obj, "inaccuracySprinting", 0.1));
            builder.withJumpMultiplier(JsonUtil.getJsonDouble(obj, "jumpMultiplier", 1.0));
            builder.withScopeOverlay(obj.has("scopeOverlay") ? obj.getAsJsonPrimitive("scopeOverlay").getAsString() : null);
            builder.withReticleOverlay(obj.has("reticleOverlay") ? obj.getAsJsonPrimitive("reticleOverlay").getAsString() : null);
            builder.withTargetLockOverlay(obj.has("targetLockOverlay") ? obj.getAsJsonPrimitive("targetLockOverlay").getAsString() : null);
            builder.withAmmoUi(JsonUtil.getJsonString(obj, "ammoUi", null));
            JsonElement targetLockedSoundElem = obj.get("targetLockedSound");
            if (targetLockedSoundElem != null && !targetLockedSoundElem.isJsonNull()) {
                String targetLockedSoundName = targetLockedSoundElem.getAsString();
                builder.withTargetLockedSound(() -> SoundRegistry.getSoundEvent(targetLockedSoundName));
            }
            if ((targetStartLockingSoundElem = obj.get("targetStartLockingSound")) != null && !targetStartLockingSoundElem.isJsonNull()) {
                String targetStargetLockingdSoundName = targetStartLockingSoundElem.getAsString();
                builder.withTargetStartLockingSound(() -> SoundRegistry.getSoundEvent(targetStargetLockingdSoundName));
            }
            List<String> fireModeNames = JsonUtil.getStrings(obj, "fireModes");
            builder.withFireModes((FireMode[])fireModeNames.stream().map(n -> FireMode.valueOf(n.toUpperCase(Locale.ROOT))).toArray(FireMode[]::new));
            builder.withHitScanSpeed(JsonUtil.getJsonFloat(obj, "hitScanSpeed", 800.0f));
            builder.withHitScanAcceleration(JsonUtil.getJsonFloat(obj, "hitScanAcceleration", 0.0f));
            for (JsonObject jsonObject : JsonUtil.getJsonObjects(obj, "reloadShakeEffects")) {
                builder.withReloadShakeEffect(jsonObject.getAsJsonPrimitive("start").getAsInt(), jsonObject.getAsJsonPrimitive("duration").getAsInt(), TimeUnit.MILLISECOND, JsonUtil.getJsonDouble(jsonObject, "initialAmplitude", 0.15), JsonUtil.getJsonDouble(jsonObject, "rateOfAmplitudeDecay", 0.3), JsonUtil.getJsonDouble(jsonObject, "initialAngularFrequency", 1.0), JsonUtil.getJsonDouble(jsonObject, "rateOfFrequencyIncrease", 0.01));
            }
            List<JsonObject> jsPhasedReloads = JsonUtil.getJsonObjects(obj, "phasedReloads");
            for (JsonObject jsPhasedReload : jsPhasedReloads) {
                ArrayList<ReloadShakeEffect> shakeEffects = new ArrayList<ReloadShakeEffect>();
                for (JsonObject jsReloadShakeEffect : JsonUtil.getJsonObjects(jsPhasedReload, "shakeEffects")) {
                    shakeEffects.add(new ReloadShakeEffect(jsReloadShakeEffect.getAsJsonPrimitive("start").getAsLong(), jsReloadShakeEffect.getAsJsonPrimitive("duration").getAsLong(), TimeUnit.MILLISECOND, JsonUtil.getJsonDouble(jsReloadShakeEffect, "initialAmplitude", 0.15), JsonUtil.getJsonDouble(jsReloadShakeEffect, "rateOfAmplitudeDecay", 0.3), JsonUtil.getJsonDouble(jsReloadShakeEffect, "initialAngularFrequency", 1.0), JsonUtil.getJsonDouble(jsReloadShakeEffect, "rateOfFrequencyIncrease", 0.01)));
                }
                Predicate<ConditionContext> condition = jsPhasedReload.has("condition") ? Conditions.fromJson(jsPhasedReload.get("condition")) : ctx -> true;
                builder.withPhasedReload(new PhasedReload(ReloadPhase.valueOf(jsPhasedReload.getAsJsonPrimitive("phase").getAsString()), condition, jsPhasedReload.getAsJsonPrimitive("duration").getAsInt(), TimeUnit.MILLISECOND, new ReloadAnimation(jsPhasedReload.getAsJsonPrimitive("animation").getAsString(), shakeEffects)));
            }
            JsonElement jsonElement = obj.get("reloadAnimation");
            String reloadAnimation = jsonElement != null ? jsonElement.getAsString() : null;
            builder.withReloadAnimation(reloadAnimation);
            float fireSoundVolume = JsonUtil.getJsonFloat(obj, "fireSoundVolume", 5.0f);
            JsonElement fireSoundElem = obj.get("fireSound");
            if (fireSoundElem != null && !fireSoundElem.isJsonNull()) {
                String fireSoundName = fireSoundElem.getAsString();
                builder.withFireSound(() -> SoundRegistry.getSoundEvent(fireSoundName), fireSoundVolume);
            }
            if ((reloadSoundElem = obj.get("reloadSound")) != null && !reloadSoundElem.isJsonNull()) {
                String reloadSoundName = reloadSoundElem.getAsString();
                builder.withReloadSound(() -> SoundRegistry.getSoundEvent(reloadSoundName));
            }
            List<String> compatibleAmmoNames = JsonUtil.getStrings(obj, "compatibleAmmo");
            ArrayList<Supplier<AmmoItem>> compatibleAmmo = new ArrayList<Supplier<AmmoItem>>();
            for (String string : compatibleAmmoNames) {
                Supplier supplier = ItemRegistry.ITEMS.getDeferredRegisteredObject(string);
                if (supplier == null) continue;
                compatibleAmmo.add(supplier);
            }
            builder.withCompatibleAmmo(compatibleAmmo);
            for (JsonObject jsonObject : JsonUtil.getJsonObjects(obj, "rotations")) {
                builder.withRotation(JsonUtil.getJsonString(jsonObject, "phase", "fire"), JsonUtil.getJsonString(jsonObject, "modelPart", null), JsonUtil.getJsonDouble(jsonObject, "rpm", 180.0), JsonUtil.getJsonDouble(jsonObject, "acceleration", 1.0), JsonUtil.getJsonDouble(jsonObject, "deceleration", 5.0), jsonObject.has("axis") ? RotationAnimationController.RotationAxis.fromJsonValue(JsonUtil.getJsonString(jsonObject, "axis", null)) : RotationAnimationController.RotationAxis.Z, jsonObject.has("direction") ? RotationAnimationController.RotationDirection.fromJsonValue(JsonUtil.getJsonString(jsonObject, "direction", null)) : RotationAnimationController.RotationDirection.CLOCKWISE);
            }
            for (JsonObject jsonObject : JsonUtil.getJsonObjects(obj, "effects")) {
                FirePhase firePhase = JsonUtil.getEnum(jsonObject, "phase", FirePhase.class, null, true);
                String string = JsonUtil.getJsonString(jsonObject, "name");
                Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> supplier = () -> EffectRegistry.getEffectBuilderSupplier(effectName).get();
                builder.withEffect(firePhase, supplier);
            }
            builder.withBobbing(JsonUtil.getJsonDouble(obj, "bobbing", 1.0));
            builder.withBobbingOnAim(JsonUtil.getJsonFloat(obj, "bobbingOnAim", 0.3f));
            builder.withBobbingRollMultiplier(JsonUtil.getJsonDouble(obj, "bobbingRollMultiplier", 1.0));
            for (JsonObject jsonObject : JsonUtil.getJsonObjects(obj, "glowingParts")) {
                void var20_51;
                String string = JsonUtil.getJsonString(jsonObject, "name");
                List<String> list2 = JsonUtil.getStrings(obj, "phases");
                List<FirePhase> list3 = list2.stream().map(n -> FirePhase.valueOf(n.toUpperCase(Locale.ROOT))).toList();
                if (list3.isEmpty()) {
                    List<FirePhase> list4 = Collections.singletonList(FirePhase.ANY);
                }
                String string2 = JsonUtil.getJsonString(jsonObject, "texture", null);
                Direction direction = JsonUtil.getEnum(jsonObject, "direction", Direction.class, null, true);
                JsonObject spritesObj = jsonObject.getAsJsonObject("sprites");
                if (spritesObj != null) {
                    int rows = JsonUtil.getJsonInt(spritesObj, "rows", 1);
                    int columns = JsonUtil.getJsonInt(spritesObj, "columns", 1);
                    int fps = JsonUtil.getJsonInt(spritesObj, "fps", 60);
                    AbstractEffect.SpriteAnimationType spriteAnimationType = JsonUtil.getEnum(spritesObj, "type", AbstractEffect.SpriteAnimationType.class, AbstractEffect.SpriteAnimationType.LOOP, true);
                    if (direction != null) {
                        builder.withGlow((Collection<FirePhase>)var20_51, string, string2, spriteAnimationType, rows, columns, fps, direction);
                        continue;
                    }
                    builder.withGlow((Collection<FirePhase>)var20_51, string, string2, spriteAnimationType, rows, columns, fps, new Direction[0]);
                    continue;
                }
                builder.withGlow((Collection<FirePhase>)var20_51, Collections.singletonList(string), string2);
            }
            List<String> compatibleAttachmentNames = JsonUtil.getStrings(obj, "compatibleAttachments");
            for (String string : compatibleAttachmentNames) {
                Supplier supplier = ItemRegistry.ITEMS.getDeferredRegisteredObject(string);
                if (supplier == null) continue;
                this.withCompatibleAttachment(() -> (Attachment)ri.get());
            }
            List<String> list5 = JsonUtil.getStrings(obj, "compatibleAttachmentGroups");
            this.compatibleAttachmentGroups.addAll(list5);
            for (JsonObject jsonObject : JsonUtil.getJsonObjects(obj, "features")) {
                FeatureBuilder<?, ?> featureBuilder = Features.fromJson(jsonObject);
                if (featureBuilder == null) continue;
                this.withFeature(featureBuilder);
            }
            List<String> list6 = JsonUtil.getStrings(obj, "defaultAttachments");
            for (String string : list6) {
                Supplier supplier = ItemRegistry.ITEMS.getDeferredRegisteredObject(string);
                if (supplier == null) continue;
                this.withDefaultAttachment(() -> (Attachment)ri.get());
            }
            List<JsonObject> list7 = JsonUtil.getJsonObjects(obj, "idleAnimations");
            for (JsonObject jsonObject : list7) {
                Predicate<ConditionContext> predicate = jsonObject.has("condition") ? Conditions.fromJson(jsonObject.get("condition")) : ctx -> true;
                builder.withIdleAnimation(JsonUtil.getJsonString(jsonObject, "name", GunItem.DEFAULT_ANIMATION_IDLE), predicate, jsonObject.getAsJsonPrimitive("duration").getAsInt(), TimeUnit.MILLISECOND);
            }
            List<JsonObject> list8 = JsonUtil.getJsonObjects(obj, "inspectAnimations");
            for (JsonObject jsonObject : list8) {
                Predicate<ConditionContext> condition = jsonObject.has("condition") ? Conditions.fromJson(jsonObject.get("condition")) : ctx -> true;
                builder.withInspectAnimation(JsonUtil.getJsonString(jsonObject, "name", GunItem.DEFAULT_ANIMATION_INSPECT), condition, jsonObject.getAsJsonPrimitive("duration").getAsInt(), TimeUnit.MILLISECOND);
            }
            if (list8.isEmpty()) {
                builder.withInspectCooldownDuration(JsonUtil.getJsonInt(obj, "inspectCooldownDuration", 1000), TimeUnit.MILLISECOND);
            }
            List<JsonObject> list9 = JsonUtil.getJsonObjects(obj, "drawAnimations");
            for (JsonObject jsIdleAnimation : list9) {
                Predicate<ConditionContext> condition = jsIdleAnimation.has("condition") ? Conditions.fromJson(jsIdleAnimation.get("condition")) : ctx -> true;
                builder.withDrawAnimation(JsonUtil.getJsonString(jsIdleAnimation, "name", GunItem.DEFAULT_ANIMATION_DRAW), condition, jsIdleAnimation.getAsJsonPrimitive("duration").getAsInt(), TimeUnit.MILLISECOND);
            }
            if (list9.isEmpty()) {
                builder.withDrawCooldownDuration(JsonUtil.getJsonInt(obj, "drawCooldownDuration", 500), TimeUnit.MILLISECOND);
            }
            try {
                list = JsonUtil.getJsonObjects(obj, "hideAnimations");
            }
            catch (RuntimeException e) {
                throw new IllegalArgumentException("Malformed hideAnimations value for gun " + this.name, e);
            }
            for (JsonObject jsHideAnimation : list) {
                try {
                    int duration;
                    Predicate<ConditionContext> condition = jsHideAnimation.has("condition") ? Conditions.fromJson(jsHideAnimation.get("condition")) : ctx -> true;
                    String animationName = JsonUtil.getJsonString(jsHideAnimation, "name", GunItem.DEFAULT_ANIMATION_HIDE);
                    if (animationName == null || animationName.isBlank()) {
                        throw new IllegalArgumentException("name must not be blank");
                    }
                    int n2 = duration = jsHideAnimation.has("duration") ? jsHideAnimation.getAsJsonPrimitive("duration").getAsInt() : -1;
                    if (jsHideAnimation.has("duration") && duration < 0) {
                        throw new IllegalArgumentException("duration must be non-negative");
                    }
                    this.hideAnimationsBuilder.withAnimation(animationName, condition, duration, TimeUnit.MILLISECOND);
                }
                catch (RuntimeException e) {
                    throw new IllegalArgumentException("Malformed hideAnimations entry for gun " + this.name + ": " + String.valueOf(jsHideAnimation), e);
                }
            }
            List<JsonObject> jsFireAnimations = JsonUtil.getJsonObjects(obj, "fireAnimations");
            for (JsonObject jsFireAnimation : jsFireAnimations) {
                Predicate<ConditionContext> condition = jsFireAnimation.has("condition") ? Conditions.fromJson(jsFireAnimation.get("condition")) : ctx -> true;
                builder.withFireAnimation(JsonUtil.getJsonString(jsFireAnimation, "name", GunItem.DEFAULT_ANIMATION_INSPECT), condition, 0, TimeUnit.MILLISECOND);
            }
            return this;
        }
    }

    public static enum AnimationType {
        RIFLE("__DEFAULT_RIFLE_ANIMATIONS__", FALLBACK_COMMON_ANIMATIONS),
        PISTOL("__DEFAULT_PISTOL_ANIMATIONS__", FALLBACK_PISTOL_ANIMATIONS);

        private final String defaultThirdPersonAnimation;
        private final List<ResourceLocation> fallbackFirstPersonAnimations;

        private AnimationType(String defaultThirdPersonAnimation, List<ResourceLocation> fallbackFirstPersonAnimations) {
            this.defaultThirdPersonAnimation = defaultThirdPersonAnimation;
            this.fallbackFirstPersonAnimations = fallbackFirstPersonAnimations;
        }

        public String getDefaultThirdPersonAnimation() {
            return this.defaultThirdPersonAnimation;
        }

        private List<ResourceLocation> getFallbackFirstPersonAnimations() {
            return this.fallbackFirstPersonAnimations;
        }
    }

    public record PhasedReload(ReloadPhase phase, Predicate<ConditionContext> predicate, long cooldownTime, TimeUnit timeUnit, ReloadAnimation reloadAnimation) {
        public PhasedReload {
            if (cooldownTime == 0L) {
                throw new IllegalArgumentException("cooldownTime cannot be null");
            }
            if (timeUnit == null) {
                throw new IllegalArgumentException("timeUnit cannot be null");
            }
            if (predicate == null) {
                throw new IllegalArgumentException("predicate cannot be null");
            }
        }

        public PhasedReload(ReloadPhase phase, TriPredicate<LivingEntity, GunClientState, ItemStack> predicate, long cooldownTime, ReloadAnimation reloadAnimation) {
            this(phase, ctx -> predicate.test(ctx.player(), ctx.gunClientState(), ctx.currentItemStack()), cooldownTime, TimeUnit.MILLISECOND, reloadAnimation);
        }

        public PhasedReload(ReloadPhase phase, long cooldownTime, ReloadAnimation reloadAnimation) {
            this(phase, ctx -> true, cooldownTime, TimeUnit.MILLISECOND, reloadAnimation);
        }

        public PhasedReload(ReloadPhase phase, long cooldownTime, String animationName) {
            this(phase, ctx -> true, cooldownTime, TimeUnit.MILLISECOND, new ReloadAnimation(animationName));
        }
    }

    public static enum ReloadPhase {
        PREPARING,
        RELOADING,
        COMPLETETING;

    }

    public static enum FirePhase {
        PREPARING,
        FIRING,
        COMPLETETING,
        HIT_SCAN_ACQUIRED,
        HIT_TARGET,
        ANY,
        FLYING;

    }

    public record OperableGunContext(ItemStack itemStack, InteractionHand hand, int slotIndex, boolean offhand) {
    }

    public record HideAnimationSelection(String animationName, long durationMillis) {
    }

    public record ReloadAnimation(String animationName, List<ReloadShakeEffect> shakeEffects) {
        public ReloadAnimation(String animationName) {
            this(animationName, Collections.emptyList());
        }
    }

    public record ReloadShakeEffect(long startTime, long duration, TimeUnit timeUnit, double initialAmplitude, double rateOfAmplitudeDecay, double initialAngularFrequency, double rateOfFrequencyIncrease) {
        private static double DEFAULT_INITIAL_ANGULAR_FREQUENCY = 1.0;
        private static double DEFAULT_RATE_OF_FREQUENCY_INCREASE = 0.01;

        public ReloadShakeEffect(long startTime, long duration, double initialAmplitude, double rateOfAmplitudeDecay) {
            this(startTime, duration, TimeUnit.MILLISECOND, initialAmplitude, rateOfAmplitudeDecay, DEFAULT_INITIAL_ANGULAR_FREQUENCY, DEFAULT_RATE_OF_FREQUENCY_INCREASE);
        }
    }
}

