/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.HitResult
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.client;

import com.vicmatskiv.pointblank.Constants;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.FirstPersonWalkingAnimationHandler;
import com.vicmatskiv.pointblank.client.GunHandSwapCoordinator;
import com.vicmatskiv.pointblank.client.GunStateListener;
import com.vicmatskiv.pointblank.client.SelectedMainHandQuickMoveTracker;
import com.vicmatskiv.pointblank.client.effect.FirstPersonShellEffect;
import com.vicmatskiv.pointblank.client.effect.MuzzleFlashEffect;
import com.vicmatskiv.pointblank.feature.AlternatingMuzzleFeature;
import com.vicmatskiv.pointblank.feature.DynamicRpmFeature;
import com.vicmatskiv.pointblank.feature.FireModeFeature;
import com.vicmatskiv.pointblank.feature.ReloadFeature;
import com.vicmatskiv.pointblank.item.AmmoCount;
import com.vicmatskiv.pointblank.item.FireMode;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.network.MainHeldSimplifiedStateSyncRequest;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.StateMachine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GunClientState {
    private static final int RELOAD_HIDE_TRANSITION_TICKS = 1;
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static final Map<PlayerSlot, GunClientState> localSlotStates = new ConcurrentHashMap<PlayerSlot, GunClientState>();
    private static final Map<UUID, GunClientState> noSlotStates = new ConcurrentHashMap<UUID, GunClientState>();
    private UUID id;
    private GunItem gunItem;
    protected int totalUninterruptedFireTime;
    private long reloadCooldownStartTime;
    private long reloadCooldownDuration;
    private long prepareFireCooldownStartTime;
    private long completeFireCooldownStartTime;
    private long drawCooldownDuration;
    private long drawCooldownStartTime;
    private long hideCooldownDuration;
    private long hideCooldownStartTime;
    private long inspectCooldownStartTime;
    private long inspectCooldownDuration;
    private long overheatCooldownStartTime;
    private long overheatCooldownDuration;
    private long prepareIdleCooldownStartTime;
    private long prepareIdleCooldownDuration = 0L;
    private long idleCooldownStartTime;
    private long idleCooldownDuration;
    private long enableFireModeCooldownStartTime;
    protected long fireCooldownStartTime;
    protected long lastSyncTime;
    protected long lastShotFiredTime;
    protected AmmoCount ammoCount = new AmmoCount();
    protected boolean isTriggerOn;
    protected int totalUninterruptedShots;
    private long alternatingMuzzleShotSequence;
    private String selectedAlternatingMuzzleFlashBone;
    private double currentHeat;
    private double heatAtOverheatStart;
    private long lastHeatUpdateTime;
    private long lastHeatAddedTime;
    private boolean pendingOverheat;
    private Map<String, GunStateListener> animationControllers = new HashMap<String, GunStateListener>();
    private List<GunStateListener> stateListeners = new ArrayList<GunStateListener>();
    private List<MuzzleFlashEffect> muzzleFlashEffects = new ArrayList<MuzzleFlashEffect>();
    private List<FirstPersonShellEffect> shellEffects = new ArrayList<FirstPersonShellEffect>();
    private long clientSyncTimeout = 100L;
    private boolean isAiming;
    private int remainingAmmoToReload;
    private AmmoCount reloadAmmoCount = new AmmoCount();
    private int reloadIterationIndex;
    private boolean pendingHideAfterReload;
    private String resolvedHideAnimationName = "animation.model.hide";
    private int resolvedHideTransitionTicks;
    private Component temporaryMessage;
    private long messageEndTime;
    private Predicate<GunClientState> messagePredicate;
    private Predicate<Context> hasAmmo = context -> this.ammoCount.getAmmoCount(context.getFireModeInstance()) > 0;
    private Predicate<Context> isValidGameMode = context -> context.player != null && !context.player.isSpectator();
    private FireState simplifiedFireState;
    private StateMachine<FireState, Context> stateMachine;
    private static final Map<UUID, GunClientState> statesById = new HashMap<UUID, GunClientState>();

    private boolean isOperableStack(Context context) {
        LivingEntity livingEntity = context.player;
        if (!(livingEntity instanceof Player)) {
            return false;
        }
        Player player = (Player)livingEntity;
        GunItem.OperableGunContext heldContext = GunItem.resolveContextForStack(player, context.itemStack);
        return heldContext != null && Objects.equals(GunItem.getItemStackId(heldContext.itemStack()), this.id);
    }

    public GunClientState(UUID id, GunItem item) {
        this.id = id;
        this.gunItem = item;
        this.stateMachine = this.createStateMachine();
        this.prepareIdleCooldownDuration = 1000000L * this.gunItem.getPrepareIdleCooldownDuration();
        this.lastHeatUpdateTime = System.nanoTime();
        statesById.put(id, this);
    }

    private StateMachine<FireState, Context> createStateMachine() {
        StateMachine.Builder<FireState, Context> builder = new StateMachine.Builder<FireState, Context>();
        builder.withTransition((FireState)((Object)List.of(FireState.PREPARE_IDLE, FireState.IDLE, FireState.IDLE_COOLDOWN, FireState.HIDING, FireState.HIDDEN)), FireState.DRAW, this.isValidGameMode, StateMachine.TransitionMode.EVENT, (StateMachine.Action<FireState, Context>)null, this::actionDraw);
        builder.withTransition(FireState.DRAW, FireState.DRAW_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.drawCooldownStartTime = System.nanoTime();
            this.drawCooldownDuration = this.gunItem.getDrawCooldownDuration(ctx.player, this, ctx.itemStack) * 1000000L;
        });
        builder.withTransition(FireState.DRAW_COOLDOWN, FireState.PREPARE_IDLE, Predicate.not(this::isDrawCooldownInProgress));
        builder.withTransition((FireState)((Object)List.of(FireState.PREPARE_IDLE, FireState.IDLE, FireState.IDLE_COOLDOWN, FireState.DRAW, FireState.DRAW_COOLDOWN, FireState.PREPARE_RELOAD, FireState.PREPARE_RELOAD_COOLDOWN, FireState.PREPARE_RELOAD_ITER, FireState.PREPARE_RELOAD_COOLDOWN_ITER, FireState.OVERHEAT, FireState.OVERHEAT_COOLDOWN, FireState.INSPECT, FireState.INSPECT_COOLDOWN, FireState.CHANGE_FIRE_MODE, FireState.CHANGE_FIRE_MODE_COOLDOWN)), FireState.HIDING, ctx -> true, StateMachine.TransitionMode.EVENT, (StateMachine.Action<FireState, Context>)null, this::actionHide);
        builder.withTransition(FireState.HIDING, FireState.HIDDEN, Predicate.not(this::isHideCooldownInProgress));
        builder.withTransition((FireState)((Object)List.of(FireState.HIDING, FireState.HIDDEN)), FireState.PREPARE_IDLE, ctx -> true, StateMachine.TransitionMode.EVENT, (StateMachine.Action<FireState, Context>)null, (StateMachine.Action<FireState, Context>)null);
        builder.withTransition(FireState.PREPARE_IDLE, FireState.IDLE, ctx -> !this.isPrepareIdleCooldownInProgress((Context)ctx), StateMachine.TransitionMode.AUTO, null, this::actionIdle);
        builder.withTransition(FireState.IDLE, FireState.IDLE_COOLDOWN, ctx -> this.gunItem.hasIdleAnimations(), StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.idleCooldownStartTime = System.nanoTime();
            this.idleCooldownDuration = this.gunItem.getIdleCooldownDuration(ctx.player, this, ctx.itemStack) * 1000000L;
        });
        builder.withTransition(FireState.IDLE_COOLDOWN, FireState.IDLE, Predicate.not(this::isIdleCooldownInProgress), StateMachine.TransitionMode.AUTO, null, this::actionIdle);
        builder.withTransition((FireState)((Object)List.of(FireState.IDLE, FireState.IDLE_COOLDOWN)), FireState.PREPARE_IDLE, ctx -> true, StateMachine.TransitionMode.EVENT, (StateMachine.Action<FireState, Context>)null, (StateMachine.Action<FireState, Context>)null);
        builder.withTransition((FireState)((Object)List.of(FireState.PREPARE_IDLE, FireState.IDLE, FireState.IDLE_COOLDOWN)), FireState.INSPECT, this.isValidGameMode, StateMachine.TransitionMode.EVENT, (StateMachine.Action<FireState, Context>)null, this::actionInspect);
        builder.withTransition(FireState.INSPECT, FireState.INSPECT_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.inspectCooldownStartTime = System.nanoTime();
            this.inspectCooldownDuration = this.gunItem.getInspectCooldownDuration(ctx.player, this, ctx.itemStack) * 1000000L;
        });
        builder.withTransition(FireState.INSPECT_COOLDOWN, FireState.PREPARE_IDLE, Predicate.not(this::inspectCooldownInProgress));
        builder.withTransition(FireState.PREPARE_IDLE, FireState.CHANGE_FIRE_MODE, this.isValidGameMode, StateMachine.TransitionMode.EVENT, null, this::actionEnableFireMode);
        builder.withTransition((FireState)((Object)List.of(FireState.PREPARE_IDLE, FireState.IDLE, FireState.IDLE_COOLDOWN)), FireState.CHANGE_FIRE_MODE, this.isValidGameMode, StateMachine.TransitionMode.EVENT, (StateMachine.Action<FireState, Context>)null, this::actionEnableFireMode);
        builder.withTransition(FireState.CHANGE_FIRE_MODE, FireState.CHANGE_FIRE_MODE_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.enableFireModeCooldownStartTime = System.nanoTime();
        });
        builder.withTransition(FireState.CHANGE_FIRE_MODE_COOLDOWN, FireState.PREPARE_IDLE, Predicate.not(this::isEnableFireModeCooldownInProgress));
        builder.withTransition(FireState.PREPARE_IDLE, FireState.PREPARE_RELOAD, this.isValidGameMode.and(Predicate.not(this::requiresPhasedReload)), StateMachine.TransitionMode.EVENT, null, this::actionPrepareReload);
        builder.withTransition((FireState)((Object)List.of(FireState.PREPARE_IDLE, FireState.IDLE, FireState.IDLE_COOLDOWN)), FireState.PREPARE_RELOAD, this.isValidGameMode.and(Predicate.not(this::requiresPhasedReload)), StateMachine.TransitionMode.EVENT, (StateMachine.Action<FireState, Context>)null, this::actionPrepareReload);
        builder.withTransition(FireState.PREPARE_RELOAD, FireState.PREPARE_RELOAD_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.reloadCooldownStartTime = System.nanoTime();
            this.reloadCooldownDuration = this.gunItem.getReloadingCooldownTime(GunItem.ReloadPhase.PREPARING, ctx.player, this, ctx.itemStack) * 1000000L;
        });
        builder.withTransition(FireState.PREPARE_RELOAD_COOLDOWN, FireState.RELOAD, Predicate.not(this::isReloadCooldownInProgress), StateMachine.TransitionMode.AUTO, null, this::actionReload);
        builder.withTransition(FireState.RELOAD, FireState.RELOAD_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.reloadCooldownStartTime = System.nanoTime();
            this.reloadCooldownDuration = this.gunItem.getReloadingCooldownTime(GunItem.ReloadPhase.RELOADING, ctx.player, this, ctx.itemStack) * 1000000L;
        });
        builder.withTransition(FireState.RELOAD_COOLDOWN, FireState.COMPLETE_RELOAD, Predicate.not(this::isReloadCooldownInProgress), StateMachine.TransitionMode.AUTO, null, this::actionCompleteReload);
        builder.withTransition(FireState.PREPARE_IDLE, FireState.PREPARE_RELOAD_ITER, context -> this.isValidGameMode.test((Context)context) && this.canReload((Context)context) && this.requiresPhasedReload((Context)context), StateMachine.TransitionMode.EVENT, null, this::actionPrepareReload);
        builder.withTransition((FireState)((Object)List.of(FireState.PREPARE_IDLE, FireState.IDLE, FireState.IDLE_COOLDOWN)), FireState.PREPARE_RELOAD_ITER, context -> this.isValidGameMode.test((Context)context) && this.canReload((Context)context) && this.requiresPhasedReload((Context)context), StateMachine.TransitionMode.EVENT, (StateMachine.Action<FireState, Context>)null, this::actionPrepareReload);
        builder.withTransition(FireState.PREPARE_RELOAD_ITER, FireState.PREPARE_RELOAD_COOLDOWN_ITER, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.reloadCooldownStartTime = System.nanoTime();
            this.reloadCooldownDuration = this.gunItem.getReloadingCooldownTime(GunItem.ReloadPhase.PREPARING, ctx.player, this, ctx.itemStack) * 1000000L;
        });
        builder.withTransition(FireState.PREPARE_RELOAD_COOLDOWN_ITER, FireState.RELOAD_ITER, Predicate.not(this::isReloadCooldownInProgress), StateMachine.TransitionMode.AUTO, null, this::actionReload);
        builder.withTransition(FireState.RELOAD_ITER, FireState.RELOAD_COOLDOWN_ITER, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.reloadCooldownStartTime = System.nanoTime();
            this.reloadCooldownDuration = this.gunItem.getReloadingCooldownTime(GunItem.ReloadPhase.RELOADING, ctx.player, this, ctx.itemStack) * 1000000L;
        });
        builder.withTransition(FireState.RELOAD_COOLDOWN_ITER, FireState.RELOAD_ITER, context -> !this.isReloadCooldownInProgress((Context)context) && this.remainingAmmoToReload > 0, StateMachine.TransitionMode.AUTO, null, this::actionReload);
        builder.withTransition(FireState.RELOAD_COOLDOWN_ITER, FireState.COMPLETE_RELOAD, context -> !this.isReloadCooldownInProgress((Context)context) && this.remainingAmmoToReload <= 0, StateMachine.TransitionMode.AUTO, null, this::actionCompleteReload);
        builder.withTransition(FireState.COMPLETE_RELOAD, FireState.COMPLETE_RELOAD_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.reloadCooldownStartTime = System.nanoTime();
            this.reloadCooldownDuration = this.gunItem.getReloadingCooldownTime(GunItem.ReloadPhase.COMPLETETING, ctx.player, this, ctx.itemStack) * 1000000L;
        });
        builder.withTransition(FireState.COMPLETE_RELOAD_COOLDOWN, FireState.HIDING, Predicate.not(this::isReloadCooldownInProgress).and(this::canCompleteReload).and(context -> this.pendingHideAfterReload), StateMachine.TransitionMode.AUTO, this::actionApplyReloadAmmo, this::actionHide);
        builder.withTransition(FireState.COMPLETE_RELOAD_COOLDOWN, FireState.PREPARE_IDLE, Predicate.not(this::isReloadCooldownInProgress).and(this::canCompleteReload).and(context -> !this.pendingHideAfterReload), StateMachine.TransitionMode.AUTO, this::actionApplyReloadAmmo, null);
        builder.withTransition((FireState)((Object)List.of(FireState.PREPARE_IDLE, FireState.IDLE, FireState.IDLE_COOLDOWN, FireState.INSPECT_COOLDOWN)), FireState.PREPARE_FIRE_SINGLE, this.isValidGameMode.and(this.hasAmmo).and(this::canStartFiring).and(context -> GunItem.getSelectedFireModeType(context.itemStack) == FireMode.SINGLE).and(context -> this.isTriggerOn && this.isOperableStack((Context)context) && !context.player.isSprinting() && !FirstPersonWalkingAnimationHandler.isPlayingBlendingAnimation(Set.of(GunItem.RAW_ANIMATION_PREPARE_RUNNING, GunItem.RAW_ANIMATION_COMPLETE_RUNNING, GunItem.RAW_ANIMATION_RUNNING), context.itemStack, 0.0, 0.25)), StateMachine.TransitionMode.EVENT, (StateMachine.Action<FireState, Context>)null, this::actionPrepareFire);
        builder.withTransition(FireState.PREPARE_FIRE_SINGLE, FireState.PREPARE_FIRE_COOLDOWN_SINGLE, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.prepareFireCooldownStartTime = System.nanoTime();
        });
        builder.withTransition(FireState.PREPARE_FIRE_COOLDOWN_SINGLE, FireState.PREPARE_IDLE, context -> !this.isTriggerOn, StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition(FireState.PREPARE_FIRE_COOLDOWN_SINGLE, FireState.FIRE_SINGLE, context -> this.isTriggerOn && !this.isPrepareFireCooldownInProgress((Context)context), StateMachine.TransitionMode.AUTO, null, this::actionFire);
        builder.withTransition(FireState.FIRE_SINGLE, FireState.FIRE_COOLDOWN_SINGLE, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.fireCooldownStartTime = System.nanoTime();
        });
        builder.withTransition(FireState.FIRE_COOLDOWN_SINGLE, FireState.OVERHEAT, Predicate.not(this::isFireCooldownInProgress).and(this::hasReachedOverheatThreshold), StateMachine.TransitionMode.AUTO, null, this::actionOverheat);
        builder.withTransition(FireState.FIRE_COOLDOWN_SINGLE, FireState.COMPLETE_FIRE, Predicate.not(this::isFireCooldownInProgress), StateMachine.TransitionMode.AUTO, null, this::actionCompleteFire);
        builder.withTransition((FireState)((Object)List.of(FireState.PREPARE_IDLE, FireState.IDLE, FireState.IDLE_COOLDOWN, FireState.INSPECT_COOLDOWN)), FireState.PREPARE_FIRE_BURST, this.isValidGameMode.and(this.hasAmmo).and(this::canStartFiring).and(context -> GunItem.getSelectedFireModeType(context.itemStack) == FireMode.BURST).and(context -> this.isTriggerOn && this.isOperableStack((Context)context) && !context.player.isSprinting() && !FirstPersonWalkingAnimationHandler.isPlayingBlendingAnimation(Set.of(GunItem.RAW_ANIMATION_PREPARE_RUNNING, GunItem.RAW_ANIMATION_COMPLETE_RUNNING, GunItem.RAW_ANIMATION_RUNNING), context.itemStack, 0.0, 0.25)), StateMachine.TransitionMode.EVENT, (StateMachine.Action<FireState, Context>)null, this::actionPrepareFire);
        builder.withTransition(FireState.PREPARE_FIRE_BURST, FireState.PREPARE_FIRE_COOLDOWN_BURST, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.prepareFireCooldownStartTime = System.nanoTime();
        });
        builder.withTransition(FireState.PREPARE_FIRE_COOLDOWN_BURST, FireState.PREPARE_IDLE, context -> !this.isTriggerOn, StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition(FireState.PREPARE_FIRE_COOLDOWN_BURST, FireState.FIRE_BURST, context -> this.isTriggerOn && !this.isPrepareFireCooldownInProgress((Context)context), StateMachine.TransitionMode.AUTO, null, this::actionFire);
        builder.withTransition(FireState.FIRE_BURST, FireState.FIRE_COOLDOWN_BURST, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.fireCooldownStartTime = System.nanoTime();
        });
        builder.withTransition(FireState.FIRE_COOLDOWN_BURST, FireState.OVERHEAT, Predicate.not(this::isFireCooldownInProgress).and(this::hasReachedOverheatThreshold), StateMachine.TransitionMode.AUTO, null, this::actionOverheat);
        builder.withTransition(FireState.FIRE_COOLDOWN_BURST, FireState.FIRE_BURST, Predicate.not(this::isFireCooldownInProgress).and(this.hasAmmo).and(Predicate.not(this::hasReachedOverheatThreshold)).and(context -> GunItem.getSelectedFireModeType(context.itemStack) == FireMode.BURST).and(context -> this.isTriggerOn && this.isOperableStack((Context)context)).and(context -> this.totalUninterruptedShots < this.gunItem.getBurstShots(context.itemStack, context.getFireModeInstance())), StateMachine.TransitionMode.AUTO, null, this::actionFire);
        builder.withTransition(FireState.FIRE_COOLDOWN_BURST, FireState.COMPLETE_FIRE, Predicate.not(this::isFireCooldownInProgress).and(this.hasAmmo.negate().or(context -> !this.isTriggerOn).or(context -> !this.isOperableStack((Context)context)).or(context -> this.totalUninterruptedShots >= this.gunItem.getBurstShots(context.itemStack, context.getFireModeInstance()))), StateMachine.TransitionMode.AUTO, null, this::actionCompleteFire);
        builder.withTransition((FireState)((Object)List.of(FireState.PREPARE_IDLE, FireState.IDLE, FireState.IDLE_COOLDOWN, FireState.INSPECT_COOLDOWN)), FireState.PREPARE_FIRE_AUTO, this.isValidGameMode.and(this.hasAmmo).and(this::canStartFiring).and(context -> GunItem.getSelectedFireModeType(context.itemStack) == FireMode.AUTOMATIC).and(context -> this.isTriggerOn && this.isOperableStack((Context)context) && !context.player.isSprinting() && !FirstPersonWalkingAnimationHandler.isPlayingBlendingAnimation(Set.of(GunItem.RAW_ANIMATION_PREPARE_RUNNING, GunItem.RAW_ANIMATION_COMPLETE_RUNNING, GunItem.RAW_ANIMATION_RUNNING), context.itemStack, 0.0, 0.25)), StateMachine.TransitionMode.EVENT, (StateMachine.Action<FireState, Context>)null, this::actionPrepareFire);
        builder.withTransition(FireState.PREPARE_FIRE_AUTO, FireState.PREPARE_FIRE_COOLDOWN_AUTO, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.prepareFireCooldownStartTime = System.nanoTime();
        });
        builder.withTransition(FireState.PREPARE_FIRE_COOLDOWN_AUTO, FireState.PREPARE_IDLE, context -> !this.isTriggerOn, StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition(FireState.PREPARE_FIRE_COOLDOWN_AUTO, FireState.FIRE_AUTO, context -> this.isTriggerOn && !this.isPrepareFireCooldownInProgress((Context)context), StateMachine.TransitionMode.AUTO, null, this::actionFire);
        builder.withTransition(FireState.FIRE_AUTO, FireState.FIRE_COOLDOWN_AUTO, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.fireCooldownStartTime = System.nanoTime();
        });
        builder.withTransition(FireState.FIRE_COOLDOWN_AUTO, FireState.OVERHEAT, Predicate.not(this::isFireCooldownInProgress).and(this::hasReachedOverheatThreshold), StateMachine.TransitionMode.AUTO, null, this::actionOverheat);
        builder.withTransition(FireState.FIRE_COOLDOWN_AUTO, FireState.FIRE_AUTO, Predicate.not(this::isFireCooldownInProgress).and(this.hasAmmo).and(Predicate.not(this::hasReachedOverheatThreshold)).and(context -> GunItem.getSelectedFireModeType(context.itemStack) == FireMode.AUTOMATIC).and(context -> this.isTriggerOn && this.isOperableStack((Context)context)), StateMachine.TransitionMode.AUTO, null, this::actionFire);
        builder.withTransition(FireState.FIRE_COOLDOWN_AUTO, FireState.COMPLETE_FIRE, Predicate.not(this::isFireCooldownInProgress).and(this.hasAmmo.negate().or(context -> !this.isTriggerOn).or(context -> !this.isOperableStack((Context)context))), StateMachine.TransitionMode.AUTO, null, this::actionCompleteFire);
        builder.withTransition(FireState.COMPLETE_FIRE, FireState.COMPLETE_FIRE_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.completeFireCooldownStartTime = System.nanoTime();
        });
        builder.withTransition(FireState.COMPLETE_FIRE_COOLDOWN, FireState.PREPARE_IDLE, Predicate.not(this::isCompleteFireCooldownInProgress), StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition(FireState.OVERHEAT, FireState.OVERHEAT_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition(FireState.OVERHEAT_COOLDOWN, FireState.PREPARE_IDLE, Predicate.not(this::isOverheatCooldownInProgress), StateMachine.TransitionMode.AUTO, null, null);
        builder.withOnSetStateAction(FireState.PREPARE_IDLE, (ctx, f, t) -> this.actionPrepareIdle((Context)ctx));
        builder.withOnSetStateAction(FireState.IDLE, (ctx, f, t) -> this.actionFiddle((Context)ctx));
        builder.withOnChangeStateAction((ctx, f, t) -> {
            boolean wasFiring = GunClientState.isFiring(f);
            boolean isFiring = GunClientState.isFiring(t);
            if (isFiring && !wasFiring) {
                this.actionStartAutoFiring((Context)ctx);
            } else if (!isFiring && wasFiring) {
                this.actionStopAutoFiring((Context)ctx);
            }
        });
        return builder.build(FireState.PREPARE_IDLE);
    }

    private boolean isPrepareIdleCooldownInProgress(Context context) {
        return System.nanoTime() - this.prepareIdleCooldownStartTime <= this.prepareIdleCooldownDuration;
    }

    private boolean isIdleCooldownInProgress(Context context) {
        return System.nanoTime() - this.idleCooldownStartTime <= this.idleCooldownDuration;
    }

    private boolean isDrawCooldownInProgress(Context context) {
        return System.nanoTime() - this.drawCooldownStartTime <= this.drawCooldownDuration;
    }

    private boolean isHideCooldownInProgress(Context context) {
        return System.nanoTime() - this.hideCooldownStartTime <= this.hideCooldownDuration;
    }

    private boolean inspectCooldownInProgress(Context context) {
        return System.nanoTime() - this.inspectCooldownStartTime <= this.inspectCooldownDuration;
    }

    private boolean isEnableFireModeCooldownInProgress(Context context) {
        return (double)(System.nanoTime() - this.enableFireModeCooldownStartTime) <= 1000000.0 * (double)FireModeFeature.getEnableFireModeCooldownDuration(context.player, this, context.itemStack);
    }

    private boolean isCompleteFireCooldownInProgress(Context context) {
        return (double)(System.nanoTime() - this.completeFireCooldownStartTime) <= 1000000.0 * (double)FireModeFeature.getCompleteFireCooldownDuration(context.player, this, context.itemStack);
    }

    private boolean isFireCooldownInProgress(Context context) {
        return System.nanoTime() - this.fireCooldownStartTime <= this.getFireCooldownDuration(context);
    }

    private long getFireCooldownDuration(Context context) {
        DynamicRpmFeature dynamicRpmFeature;
        int rpm = FireModeFeature.getRpm(context.itemStack);
        if (GunItem.getSelectedFireModeType(context.itemStack) == FireMode.AUTOMATIC && (dynamicRpmFeature = DynamicRpmFeature.getEnabled(context.itemStack)) != null) {
            rpm = dynamicRpmFeature.getRpm(rpm, this.totalUninterruptedShots);
        }
        return (long)(6.0E10 / (double)rpm);
    }

    private boolean isPrepareFireCooldownInProgress(Context context) {
        return (double)(System.nanoTime() - this.prepareFireCooldownStartTime) <= 1000000.0 * (double)FireModeFeature.getPrepareFireCooldownDuration(context.player, this, context.itemStack);
    }

    private boolean isReloadCooldownInProgress(Context context) {
        return System.nanoTime() - this.reloadCooldownStartTime <= this.reloadCooldownDuration;
    }

    private boolean isOverheatCooldownInProgress(Context context) {
        return this.isOverheatLockoutInProgress();
    }

    private boolean isOverheatLockoutInProgress() {
        return this.overheatCooldownDuration > 0L && System.nanoTime() - this.overheatCooldownStartTime <= this.overheatCooldownDuration;
    }

    private boolean canStartFiring(Context context) {
        return !this.isOverheatLockoutInProgress();
    }

    private boolean hasReachedOverheatThreshold(Context context) {
        return this.gunItem.supportsOverheating(context.itemStack) && (this.pendingOverheat || this.currentHeat + 1.0E-6 >= this.gunItem.getOverheatMaxHeat(context.itemStack));
    }

    private void updateHeat(ItemStack itemStack) {
        long now = System.nanoTime();
        long elapsedNanos = Math.max(0L, now - this.lastHeatUpdateTime);
        this.lastHeatUpdateTime = now;
        if (!this.gunItem.supportsOverheating(itemStack)) {
            this.currentHeat = 0.0;
            this.heatAtOverheatStart = 0.0;
            this.pendingOverheat = false;
            this.overheatCooldownDuration = 0L;
            return;
        }
        if (elapsedNanos <= 0L) {
            return;
        }
        if (this.isOverheatLockoutInProgress()) {
            if (this.overheatCooldownDuration <= 0L) {
                this.currentHeat = 0.0;
                this.heatAtOverheatStart = 0.0;
                return;
            }
            long delayNanos = Math.min(Math.max(0L, this.overheatCooldownDuration - 1L), this.gunItem.getOverheatCooldownDelayMillis(itemStack) * 1000000L);
            long coolingDuration = Math.max(1L, this.overheatCooldownDuration - delayNanos);
            long recoveryNanos = Math.min(Math.max(0L, now - this.overheatCooldownStartTime - delayNanos), coolingDuration);
            double progress = (double)recoveryNanos / (double)coolingDuration;
            this.currentHeat = Math.max(0.0, this.heatAtOverheatStart * (1.0 - progress));
            if (progress >= 1.0) {
                this.currentHeat = 0.0;
                this.heatAtOverheatStart = 0.0;
            }
            return;
        }
        long cooldownDelayNanos = this.gunItem.getOverheatCooldownDelayMillis(itemStack) * 1000000L;
        if (now - this.lastHeatAddedTime < cooldownDelayNanos) {
            return;
        }
        double cooledHeat = this.gunItem.getOverheatCooldownPerSecond(itemStack) * ((double)elapsedNanos / 1.0E9);
        this.currentHeat = Math.max(0.0, this.currentHeat - cooledHeat);
    }

    private void addHeatForShot(ItemStack itemStack) {
        if (!this.gunItem.supportsOverheating(itemStack)) {
            return;
        }
        double maxHeat = this.gunItem.getOverheatMaxHeat(itemStack);
        this.currentHeat = Math.min(maxHeat, this.currentHeat + this.gunItem.getOverheatHeatPerShot(itemStack));
        this.lastHeatAddedTime = System.nanoTime();
        if (this.currentHeat + 1.0E-6 >= maxHeat) {
            this.pendingOverheat = true;
        }
    }

    private boolean requiresPhasedReload(Context context) {
        return this.gunItem.requiresPhasedReload();
    }

    private boolean canReload(Context context) {
        int ammoToReload = this.gunItem.canReloadGun(context.itemStack, (Player)context.player, context.getFireModeInstance());
        return ammoToReload > 0;
    }

    private boolean canCompleteReload(Context context) {
        return this.reloadAmmoCount.getAmmoCount(context.getFireModeInstance()) > 0 || this.isClientSyncTimeoutExpired(MiscUtil.getLevel((Entity)context.player));
    }

    private void actionPrepareReload(Context context, FireState fromState, FireState toState) {
        this.clearPendingHideAfterReload();
        this.reloadIterationIndex = this.ammoCount.getAmmoCount(context.getFireModeInstance()) - 1;
        this.remainingAmmoToReload = this.gunItem.canReloadGun(context.itemStack, (Player)context.player, context.getFireModeInstance());
        LOGGER.debug("Preparing to reload ammo: {}", (Object)this.remainingAmmoToReload);
        this.publishMessage((Component)Component.translatable((String)"message.pointblank.reloading").append("..."), 10000L, state -> state.isReloading());
        LOGGER.debug("{} Set reload iteration index to {}", (Object)(System.currentTimeMillis() % 100000L), (Object)this.reloadIterationIndex);
        for (GunStateListener listener : this.stateListeners) {
            listener.onPrepareReloading(context.player, this, context.itemStack);
        }
    }

    private void actionReload(Context context, FireState fromState, FireState toState) {
        ++this.reloadIterationIndex;
        int ammoToReloadPerIteration = Math.min(this.remainingAmmoToReload, ReloadFeature.getMaxAmmoPerReloadIteration(context.itemStack));
        this.remainingAmmoToReload -= ammoToReloadPerIteration;
        this.ammoCount.incrementAmmoCount(context.getFireModeInstance(), ammoToReloadPerIteration);
        LOGGER.debug("Ammo to reload per iteration: {}, remaining ammo to reload: {}, current ammo: {}, reload iteration index: {}", (Object)ammoToReloadPerIteration, (Object)this.remainingAmmoToReload, (Object)this.ammoCount, (Object)this.reloadIterationIndex);
        this.gunItem.requestReloadFromServer((Player)context.player, context.itemStack);
        for (GunStateListener listener : this.stateListeners) {
            listener.onStartReloading(context.player, this, context.itemStack);
        }
    }

    private void actionCompleteReload(Context context, FireState fromState, FireState toState) {
        this.reloadIterationIndex = 0;
        LOGGER.debug("Completing reload");
        for (GunStateListener listener : this.stateListeners) {
            listener.onCompleteReloading(context.player, this, context.itemStack);
        }
    }

    private void actionPrepareFire(Context context, FireState fromState, FireState toState) {
        this.publishMessage((Component)Component.translatable((String)"message.pointblank.preparing").append("..."), 3500L, state -> state.isPreparingFiring());
        for (GunStateListener listener : this.stateListeners) {
            listener.onPrepareFiring(context.player, this, context.itemStack);
        }
    }

    private void actionFire(Context context, FireState fromState, FireState toState) {
        this.selectAlternatingMuzzleForShot(context.itemStack);
        this.gunItem.requestFireFromServer(this, (Player)context.player, context.itemStack, context.targetEntity);
        if (this.gunItem.getMaxAmmoCapacity(context.itemStack, context.getFireModeInstance()) < Integer.MAX_VALUE) {
            this.ammoCount.incrementAmmoCount(context.getFireModeInstance(), -1);
        }
        this.addHeatForShot(context.itemStack);
        ++this.totalUninterruptedShots;
        this.lastShotFiredTime = MiscUtil.getLevel((Entity)context.player).getGameTime();
        for (GunStateListener listener : this.stateListeners) {
            listener.onStartFiring(context.player, this, context.itemStack);
        }
    }

    private void actionDraw(Context context, FireState fromState, FireState toState) {
        for (GunStateListener listener : this.stateListeners) {
            listener.onDrawing(context.player, this, context.itemStack);
        }
    }

    private void actionHide(Context context, FireState fromState, FireState toState) {
        this.clearPendingHideAfterReload();
        GunItem.HideAnimationSelection selection = this.gunItem.resolveHideAnimation(context.player, this, context.itemStack);
        this.resolvedHideAnimationName = selection.animationName();
        long durationMillis = Math.max(0L, selection.durationMillis());
        this.resolvedHideTransitionTicks = fromState == FireState.COMPLETE_RELOAD_COOLDOWN ? 1 : 0;
        this.hideCooldownStartTime = System.nanoTime();
        this.hideCooldownDuration = durationMillis * 1000000L;
        if (Constants.DEBUG_HIDE_ANIMATION) {
            LOGGER.info("[HideSwapTrace] enter-state uuid={} from={} animation={} durationMs={} transitionTicks={} listeners={}", (Object)this.id, (Object)fromState, (Object)this.resolvedHideAnimationName, (Object)durationMillis, (Object)this.resolvedHideTransitionTicks, (Object)this.stateListeners.size());
        }
        this.setTrigger(false);
        this.setAiming(false);
        for (GunStateListener listener : this.stateListeners) {
            listener.onHiding(context.player, this, context.itemStack);
        }
    }

    private void actionInspect(Context context, FireState fromState, FireState toState) {
        for (GunStateListener listener : this.stateListeners) {
            listener.onInspecting(context.player, this, context.itemStack);
        }
    }

    private void actionEnableFireMode(Context context, FireState fromState, FireState toState) {
        ((GunItem)context.itemStack.getItem()).initiateClientSideFireMode((Player)context.player, context.itemStack);
        for (GunStateListener listener : this.stateListeners) {
            listener.onEnablingFireMode(context.player, this, context.itemStack);
        }
    }

    private void actionIdle(Context context, FireState fromState, FireState toState) {
        for (GunStateListener listener : this.stateListeners) {
            listener.onIdle(context.player, this, context.itemStack);
        }
    }

    private void actionCompleteFire(Context context, FireState fromState, FireState toState) {
        this.publishMessage((Component)Component.translatable((String)"message.pointblank.completing").append("..."), 3500L, state -> state.isCompletingFiring());
        LOGGER.debug("{} Completing firing in state {}", (Object)(System.currentTimeMillis() % 100000L), (Object)toState);
        for (GunStateListener listener : this.stateListeners) {
            listener.onCompleteFiring(context.player, this, context.itemStack);
        }
    }

    private void actionOverheat(Context context, FireState fromState, FireState toState) {
        this.setTrigger(false);
        this.setAiming(false);
        this.pendingOverheat = false;
        this.heatAtOverheatStart = Math.max(this.currentHeat, this.gunItem.getOverheatMaxHeat(context.itemStack));
        this.overheatCooldownDuration = Math.max(0L, this.gunItem.getOverheatAnimationDurationMillis(context.itemStack) * 1000000L);
        this.overheatCooldownStartTime = System.nanoTime();
        if (this.overheatCooldownDuration <= 0L) {
            this.currentHeat = 0.0;
            this.heatAtOverheatStart = 0.0;
        }
        for (GunStateListener listener : this.stateListeners) {
            listener.onOverheating(context.player, this, context.itemStack);
        }
    }

    private void actionPrepareIdle(Context context) {
        this.clearPendingHideAfterReload();
        this.prepareIdleCooldownStartTime = System.nanoTime();
        this.totalUninterruptedShots = 0;
        if (!this.isOverheating()) {
            this.pendingOverheat = false;
        }
        for (GunStateListener listener : this.stateListeners) {
            listener.onPrepareIdle(context.player, this, context.itemStack);
        }
    }

    private void actionFiddle(Context context) {
        this.totalUninterruptedShots = 0;
        for (GunStateListener listener : this.stateListeners) {
            listener.onIdle(context.player, this, context.itemStack);
        }
    }

    private void actionStartAutoFiring(Context context) {
        for (GunStateListener listener : this.stateListeners) {
            listener.onStartAutoFiring(context.player, this, context.itemStack);
        }
    }

    private void actionStopAutoFiring(Context context) {
        for (GunStateListener listener : this.stateListeners) {
            listener.onStopAutoFiring(context.player, this, context.itemStack);
        }
    }

    private void actionApplyReloadAmmo(Context context, FireState fromState, FireState toState) {
        FireModeInstance fireModeInstance = context.getFireModeInstance();
        if (this.reloadAmmoCount.getAmmoCount(fireModeInstance) > 0) {
            LOGGER.debug("Applying reload ammo count: {}", (Object)this.reloadAmmoCount);
            this.ammoCount.setAmmoCount(context.getFireModeInstance(), this.reloadAmmoCount.getAmmoCount(fireModeInstance));
            this.reloadAmmoCount.setAmmoCount(fireModeInstance, 0);
        }
    }

    public boolean tryReload(LivingEntity player, ItemStack itemStack) {
        Context context;
        if (player instanceof Player) {
            Player swapPlayer = (Player)player;
            if (GunHandSwapCoordinator.getInstance().blocksMainHandActions(swapPlayer)) {
                return false;
            }
        }
        return this.stateMachine.setStateToAnyOf(context = new Context(this, player, itemStack), List.of(FireState.PREPARE_RELOAD, FireState.PREPARE_RELOAD_ITER)) != null;
    }

    public boolean tryFire(LivingEntity player, ItemStack itemStack, Entity targetEntity) {
        Context context;
        if (player instanceof Player) {
            Player swapPlayer = (Player)player;
            if (GunHandSwapCoordinator.getInstance().blocksMainHandActions(swapPlayer)) {
                return false;
            }
        }
        return this.stateMachine.setStateToAnyOf(context = new Context(this, player, itemStack, targetEntity), List.of(FireState.PREPARE_FIRE_SINGLE, FireState.PREPARE_FIRE_BURST, FireState.PREPARE_FIRE_AUTO)) != null;
    }

    public boolean tryDraw(LivingEntity player, ItemStack itemStack) {
        Context context = new Context(this, player, itemStack);
        return this.stateMachine.setState(context, FireState.DRAW) != null;
    }

    public boolean tryDeactivate(LivingEntity player, ItemStack itemStack) {
        Context context = new Context(this, player, itemStack);
        return this.stateMachine.setState(context, FireState.PREPARE_IDLE) != null;
    }

    public boolean tryHide(LivingEntity player, ItemStack itemStack) {
        Context context = new Context(this, player, itemStack);
        return this.stateMachine.setState(context, FireState.HIDING) != null;
    }

    public boolean requestHideForSwap(LivingEntity player, ItemStack itemStack) {
        FireState fireState = this.stateMachine.getCurrentState();
        if (fireState == FireState.HIDING || fireState == FireState.HIDDEN) {
            return true;
        }
        Context context = new Context(this, player, itemStack);
        if (GunClientState.isPreRequestReloadState(fireState)) {
            this.clearPendingHideAfterReload();
            return this.stateMachine.setState(context, FireState.HIDING) != null;
        }
        if (GunClientState.isCommittedReloadState(fireState)) {
            this.pendingHideAfterReload = true;
            return true;
        }
        return this.stateMachine.setState(context, FireState.HIDING) != null;
    }

    public void cancelHideForSwap() {
        this.clearPendingHideAfterReload();
    }

    public boolean hasPendingHideAfterReload() {
        return this.pendingHideAfterReload;
    }

    public boolean tryInspect(LivingEntity player, ItemStack itemStack) {
        Context context;
        if (player instanceof Player) {
            Player swapPlayer = (Player)player;
            if (GunHandSwapCoordinator.getInstance().blocksMainHandActions(swapPlayer)) {
                return false;
            }
        }
        return this.stateMachine.setState(context = new Context(this, player, itemStack), FireState.INSPECT) != null;
    }

    public boolean tryChangeFireMode(LivingEntity player, ItemStack itemStack) {
        Context context;
        if (player instanceof Player) {
            Player swapPlayer = (Player)player;
            if (GunHandSwapCoordinator.getInstance().blocksMainHandActions(swapPlayer)) {
                return false;
            }
        }
        return this.stateMachine.setState(context = new Context(this, player, itemStack), FireState.CHANGE_FIRE_MODE) != null;
    }

    public UUID getId() {
        return this.id;
    }

    public GunItem getGunItem() {
        return this.gunItem;
    }

    public void setTrigger(boolean isTriggerOn) {
        this.isTriggerOn = isTriggerOn;
    }

    public boolean isIdle() {
        FireState fireState = this.stateMachine.getCurrentState();
        return this.simplifiedFireState == FireState.IDLE || fireState == FireState.PREPARE_IDLE || fireState == FireState.IDLE || fireState == FireState.IDLE_COOLDOWN;
    }

    public boolean isPreparingReload() {
        FireState fireState = this.stateMachine.getCurrentState();
        return fireState == FireState.PREPARE_RELOAD || fireState == FireState.PREPARE_RELOAD_ITER || fireState == FireState.PREPARE_RELOAD_COOLDOWN || fireState == FireState.PREPARE_RELOAD_COOLDOWN_ITER;
    }

    public boolean isReloading() {
        FireState fireState = this.stateMachine.getCurrentState();
        return this.simplifiedFireState == FireState.RELOAD || fireState == FireState.PREPARE_RELOAD || fireState == FireState.PREPARE_RELOAD_COOLDOWN || fireState == FireState.PREPARE_RELOAD_ITER || fireState == FireState.PREPARE_RELOAD_COOLDOWN_ITER || fireState == FireState.RELOAD || fireState == FireState.RELOAD_ITER || fireState == FireState.RELOAD_COOLDOWN || fireState == FireState.RELOAD_COOLDOWN_ITER || fireState == FireState.COMPLETE_RELOAD || fireState == FireState.COMPLETE_RELOAD_COOLDOWN;
    }

    public boolean isFiring() {
        FireState fireState = this.stateMachine.getCurrentState();
        return GunClientState.isFiring(fireState) || this.simplifiedFireState == FireState.FIRE_SINGLE;
    }

    private static boolean isFiring(FireState fireState) {
        return fireState == FireState.PREPARE_FIRE_SINGLE || fireState == FireState.PREPARE_FIRE_COOLDOWN_SINGLE || fireState == FireState.PREPARE_FIRE_AUTO || fireState == FireState.PREPARE_FIRE_COOLDOWN_AUTO || fireState == FireState.PREPARE_FIRE_BURST || fireState == FireState.PREPARE_FIRE_COOLDOWN_BURST || fireState == FireState.FIRE_SINGLE || fireState == FireState.FIRE_BURST || fireState == FireState.FIRE_AUTO || fireState == FireState.FIRE_COOLDOWN_SINGLE || fireState == FireState.FIRE_COOLDOWN_BURST || fireState == FireState.FIRE_COOLDOWN_AUTO || fireState == FireState.COMPLETE_FIRE || fireState == FireState.COMPLETE_FIRE_COOLDOWN;
    }

    public boolean isPreparingFiring() {
        FireState fireState = this.stateMachine.getCurrentState();
        return fireState == FireState.PREPARE_FIRE_SINGLE || fireState == FireState.PREPARE_FIRE_COOLDOWN_SINGLE || fireState == FireState.PREPARE_FIRE_AUTO || fireState == FireState.PREPARE_FIRE_COOLDOWN_AUTO || fireState == FireState.PREPARE_FIRE_BURST || fireState == FireState.PREPARE_FIRE_COOLDOWN_BURST;
    }

    public boolean isCompletingFiring() {
        FireState fireState = this.stateMachine.getCurrentState();
        return fireState == FireState.COMPLETE_FIRE || fireState == FireState.COMPLETE_FIRE_COOLDOWN;
    }

    public boolean isOverheating() {
        FireState fireState = this.stateMachine.getCurrentState();
        return this.simplifiedFireState == FireState.OVERHEAT || fireState == FireState.OVERHEAT || fireState == FireState.OVERHEAT_COOLDOWN || this.isOverheatLockoutInProgress();
    }

    public boolean isDrawing() {
        FireState fireState = this.stateMachine.getCurrentState();
        return this.simplifiedFireState == FireState.DRAW || fireState == FireState.DRAW || fireState == FireState.DRAW_COOLDOWN;
    }

    public double getCurrentHeat() {
        return this.currentHeat;
    }

    public double getCurrentHeatRatio(ItemStack itemStack) {
        if (!this.gunItem.supportsOverheating(itemStack)) {
            return 0.0;
        }
        return Mth.clamp((double)(this.currentHeat / this.gunItem.getOverheatMaxHeat(itemStack)), (double)0.0, (double)1.0);
    }

    public boolean isHiding() {
        return this.stateMachine.getCurrentState() == FireState.HIDING;
    }

    public boolean isHidden() {
        return this.stateMachine.getCurrentState() == FireState.HIDDEN || this.simplifiedFireState == FireState.HIDDEN;
    }

    public String getResolvedHideAnimationName() {
        return this.resolvedHideAnimationName;
    }

    public long getResolvedHideDurationMillis() {
        return this.hideCooldownDuration / 1000000L;
    }

    public int getResolvedHideTransitionTicks() {
        return this.resolvedHideTransitionTicks;
    }

    public boolean isInspecting() {
        FireState fireState = this.stateMachine.getCurrentState();
        return this.simplifiedFireState == FireState.INSPECT || fireState == FireState.INSPECT || fireState == FireState.INSPECT_COOLDOWN;
    }

    public boolean isChangingFireMode() {
        FireState fireState = this.stateMachine.getCurrentState();
        return fireState == FireState.CHANGE_FIRE_MODE || fireState == FireState.CHANGE_FIRE_MODE_COOLDOWN;
    }

    public void inventoryTick(LivingEntity player, ItemStack itemStack, boolean isSelected) {
        this.updateState(player, itemStack, isSelected);
        for (GunStateListener listener : this.stateListeners) {
            listener.onGameTick(player, this);
        }
    }

    public void stateTick(LivingEntity player, ItemStack itemStack, boolean isSelected) {
        Object effect;
        this.updateState(player, itemStack, isSelected);
        for (GunStateListener listener : this.stateListeners) {
            listener.onStateTick(player, this);
        }
        Iterator<Object> it = this.muzzleFlashEffects.iterator();
        while (it.hasNext()) {
            effect = it.next();
            if (!((MuzzleFlashEffect)effect).isExpired()) continue;
            LOGGER.debug("Effect {} expired, removing", effect);
            it.remove();
        }
        it = this.shellEffects.iterator();
        while (it.hasNext()) {
            effect = (FirstPersonShellEffect)it.next();
            ((FirstPersonShellEffect)effect).tick();
            if (!((FirstPersonShellEffect)effect).isExpired()) continue;
            it.remove();
        }
    }

    private boolean isClientSyncTimeoutExpired(Level level) {
        return level.getGameTime() - Math.max(this.lastSyncTime, this.lastShotFiredTime) > this.clientSyncTimeout;
    }

    public void updateState(LivingEntity player, ItemStack itemStack, boolean isSelected) {
        if (player != ClientUtil.getClientPlayer()) {
            return;
        }
        if (!isSelected && this.isAiming) {
            this.setAiming(false);
            LOGGER.debug("Turning the aiming off");
        }
        Context context = new Context(this, player, itemStack);
        this.updateHeat(itemStack);
        this.stateMachine.update(context);
        Level level = MiscUtil.getLevel((Entity)player);
        if (!this.isFiring() && !this.isReloading() && this.isClientSyncTimeoutExpired(level)) {
            this.syncAmmo(level, itemStack);
        }
        for (GunStateListener listener : this.stateListeners) {
            listener.onUpdateState(player, this);
        }
        FireState updatedSimplifiedFireState = FireState.getSimplifiedFireState(this.stateMachine.getCurrentState());
        if (updatedSimplifiedFireState != this.simplifiedFireState) {
            this.simplifiedFireState = updatedSimplifiedFireState;
            Platform.getInstance().getNetworkService().sendToServer(new MainHeldSimplifiedStateSyncRequest(this.id, updatedSimplifiedFireState));
        }
    }

    private void syncAmmo(Level level, ItemStack itemStack) {
        List<FireModeInstance> fireModeInstances = GunItem.getFireModes(itemStack);
        for (FireModeInstance fireModeInstance : fireModeInstances) {
            int localStateAmmo;
            int stackAmmo = GunItem.getAmmo(itemStack, fireModeInstance);
            if (stackAmmo != (localStateAmmo = this.ammoCount.getAmmoCount(fireModeInstance))) {
                LOGGER.debug("Current ammo {} is out of sync with server count {} ", (Object)localStateAmmo, (Object)stackAmmo);
            }
            this.ammoCount.setAmmoCount(fireModeInstance, stackAmmo);
        }
        this.lastSyncTime = level.getGameTime();
    }

    public int getTotalUninterruptedShots() {
        return this.totalUninterruptedShots;
    }

    public void selectAlternatingMuzzleForShot(ItemStack itemStack) {
        if (!AlternatingMuzzleFeature.isEnabledFor(itemStack)) {
            this.selectedAlternatingMuzzleFlashBone = null;
            return;
        }
        this.selectedAlternatingMuzzleFlashBone = AlternatingMuzzleFeature.getMuzzleFlashBone(this.alternatingMuzzleShotSequence++);
    }

    public void applyAlternatingMuzzleSelection(ItemStack itemStack, boolean alternate) {
        if (!AlternatingMuzzleFeature.isEnabledFor(itemStack)) {
            this.selectedAlternatingMuzzleFlashBone = null;
            return;
        }
        this.selectedAlternatingMuzzleFlashBone = alternate ? "muzzleflashalt" : "muzzleflash";
    }

    public String getSelectedAlternatingMuzzleFlashBone() {
        return this.selectedAlternatingMuzzleFlashBone;
    }

    public boolean isAlternateMuzzleSelected() {
        return "muzzleflashalt".equals(this.selectedAlternatingMuzzleFlashBone);
    }

    public void jump(LivingEntity player, ItemStack itemStack) {
        for (GunStateListener listener : this.stateListeners) {
            listener.onJumping(player, this, itemStack);
        }
    }

    public void confirmHitScanTarget(LivingEntity player, ItemStack itemStack, HitResult hitResult, float damage) {
        for (GunStateListener listener : this.stateListeners) {
            listener.onHitScanTargetConfirmed(player, this, itemStack, hitResult, damage);
        }
    }

    public void acquireHitScan(LivingEntity player, ItemStack itemStack, HitResult hitResult) {
        for (GunStateListener listener : this.stateListeners) {
            listener.onHitScanTargetAcquired(player, this, itemStack, hitResult);
        }
    }

    public void renderTick(LivingEntity player, ItemStack itemStack, float partialTicks) {
        for (GunStateListener listener : this.stateListeners) {
            listener.onRenderTick(player, this, itemStack, null, partialTicks);
        }
    }

    public GunStateListener getAnimationController(String controllerId) {
        return this.animationControllers.get(controllerId);
    }

    public void setAnimationController(String controllerId, GunStateListener controller) {
        this.animationControllers.put(controllerId, controller);
        this.addListener(controller);
    }

    public void addListener(GunStateListener listener) {
        this.stateListeners.add(listener);
    }

    public int getAmmoCount(FireModeInstance fireModeInstance) {
        return this.ammoCount.getAmmoCount(fireModeInstance);
    }

    public FireState getFireState() {
        return this.stateMachine.getCurrentState();
    }

    public boolean isAiming() {
        return this.isAiming;
    }

    public void setAiming(boolean isAiming) {
        if (isAiming != this.isAiming) {
            this.isAiming = isAiming;
            this.toggleAiming(isAiming);
        }
    }

    private void toggleAiming(boolean isAiming) {
        for (GunStateListener listener : this.stateListeners) {
            listener.onToggleAiming(isAiming);
        }
    }

    public void reloadAmmo(Level level, FireModeInstance fireModeInstance, int reloadAmmoCount) {
        this.lastSyncTime = level.getGameTime();
        this.reloadAmmoCount.setAmmoCount(fireModeInstance, reloadAmmoCount);
    }

    public int getReloadIterationIndex() {
        return this.reloadIterationIndex;
    }

    public GunItem.ReloadPhase getReloadPhase() {
        return switch (this.stateMachine.getCurrentState().ordinal()) {
            case 23, 24, 25, 26 -> GunItem.ReloadPhase.PREPARING;
            case 27, 28, 29, 30 -> GunItem.ReloadPhase.RELOADING;
            case 31, 32 -> GunItem.ReloadPhase.COMPLETETING;
            default -> null;
        };
    }

    private void clearPendingHideAfterReload() {
        this.pendingHideAfterReload = false;
    }

    private static boolean isPreRequestReloadState(FireState fireState) {
        return fireState == FireState.PREPARE_RELOAD || fireState == FireState.PREPARE_RELOAD_COOLDOWN || fireState == FireState.PREPARE_RELOAD_ITER || fireState == FireState.PREPARE_RELOAD_COOLDOWN_ITER;
    }

    private static boolean isCommittedReloadState(FireState fireState) {
        return fireState == FireState.RELOAD || fireState == FireState.RELOAD_COOLDOWN || fireState == FireState.RELOAD_ITER || fireState == FireState.RELOAD_COOLDOWN_ITER || fireState == FireState.COMPLETE_RELOAD || fireState == FireState.COMPLETE_RELOAD_COOLDOWN;
    }

    public void publishMessage(Component message, long displayDurationMillis, Predicate<GunClientState> messagePredicate) {
        this.temporaryMessage = message;
        this.messageEndTime = System.currentTimeMillis() + displayDurationMillis;
        this.messagePredicate = messagePredicate;
    }

    public Component getCurrentMessage() {
        if (this.temporaryMessage != null && System.currentTimeMillis() < this.messageEndTime) {
            return this.messagePredicate == null || this.messagePredicate.test(this) ? this.temporaryMessage : null;
        }
        return null;
    }

    public String toString() {
        return String.format("GunClientState[sid=%s,id=%s]", System.identityHashCode(this), this.id);
    }

    public static GunClientState getMainHeldState() {
        Player player = ClientUtil.getClientPlayer();
        return GunClientState.getMainHeldState(player);
    }

    public static GunClientState getMainHeldState(Player player) {
        if (player == null) {
            return null;
        }
        GunItem.OperableGunContext context = GunItem.resolveOperableGunContext(player);
        return context != null ? GunClientState.getState(player, context.itemStack(), context.slotIndex(), context.offhand()) : null;
    }

    public static GunClientState getMainHandState(Player player) {
        GunItem.OperableGunContext context = GunItem.resolveMainHandGunContext(player);
        return context != null ? GunClientState.getState(player, context.itemStack(), context.slotIndex(), false) : null;
    }

    public static GunClientState getOffhandState(Player player) {
        GunItem.OperableGunContext context = GunItem.resolveOffhandGunContext(player);
        return context != null ? GunClientState.getState(player, context.itemStack(), 40, true) : null;
    }

    public static void clearPlayerStates(Player player) {
        if (player == null) {
            return;
        }
        boolean clientSide = MiscUtil.getLevel((Entity)player).isClientSide;
        localSlotStates.entrySet().removeIf(entry -> ((PlayerSlot)entry.getKey()).playerEntityId == player.getId() && ((PlayerSlot)entry.getKey()).isClientSide == clientSide);
    }

    public static GunClientState getState(Player player, ItemStack itemStack, int slotIndex, boolean isOffhand) {
        UUID offhandStackId;
        UUID mainHandStackId;
        Level level = MiscUtil.getLevel((Entity)player);
        UUID stackId = GunClientState.resolveEffectiveStackId(player, itemStack, slotIndex, isOffhand);
        if (stackId == null) {
            return null;
        }
        if (GunClientState.hasAmbiguousHeldIdentity(player, stackId)) {
            return null;
        }
        if (slotIndex == -1 && !isOffhand && Objects.equals(mainHandStackId = GunClientState.resolveEffectiveStackId(player, player.getMainHandItem(), player.getInventory().selected, false), stackId)) {
            return GunClientState.getState(player, itemStack, player.getInventory().selected, false);
        }
        if (slotIndex == -1 && isOffhand && Objects.equals(offhandStackId = GunClientState.resolveEffectiveStackId(player, player.getOffhandItem(), 40, true), stackId)) {
            return GunClientState.getState(player, itemStack, 40, true);
        }
        if (slotIndex == -1) {
            GunClientState state = null;
            for (Map.Entry<PlayerSlot, GunClientState> entry : localSlotStates.entrySet()) {
                boolean offhandSlot;
                ItemStack stackAtSlotIndex;
                if (entry.getKey().isClientSide != level.isClientSide || !Objects.equals(entry.getValue().getId(), stackId)) continue;
                PlayerSlot playerSlot = entry.getKey();
                int playerSlotId = playerSlot.slotId;
                if (playerSlotId < 0 && playerSlotId != -5 || playerSlot.playerEntityId != player.getId() || (stackAtSlotIndex = (offhandSlot = playerSlotId == -5) ? player.getOffhandItem() : player.getInventory().getItem(playerSlotId)) == null || !(stackAtSlotIndex.getItem() instanceof GunItem) || !Objects.equals(GunClientState.resolveEffectiveStackId(player, stackAtSlotIndex, offhandSlot ? 40 : playerSlotId, offhandSlot), stackId)) continue;
                state = entry.getValue();
                break;
            }
            if (state == null) {
                state = GunClientState.getOrCreateCanonicalState(itemStack, stackId, level);
            }
            return state;
        }
        boolean offhandSlot = isOffhand || slotIndex == 40;
        int adjustedSlotIndex = offhandSlot ? -5 : slotIndex;
        PlayerSlot playerSlot = new PlayerSlot(player.getId(), adjustedSlotIndex, level.isClientSide);
        GunClientState slotState = localSlotStates.get(playerSlot);
        if (slotState == null || !Objects.equals(slotState.getId(), stackId)) {
            if ((slotState = GunClientState.tryPromoteTrackedQuickMoveState(player, itemStack, slotIndex, offhandSlot, stackId, level, playerSlot, slotState)) == null || !Objects.equals(slotState.getId(), stackId)) {
                slotState = GunClientState.findExistingLocalState(playerSlot, stackId);
            }
            if (slotState == null) {
                slotState = GunClientState.getOrCreateCanonicalState(itemStack, stackId, level);
            }
            GunClientState.removeOtherLocalBindings(playerSlot, stackId);
            localSlotStates.put(playerSlot, slotState);
        }
        return slotState;
    }

    private static UUID resolveEffectiveStackId(Player player, ItemStack itemStack, int slotIndex, boolean isOffhand) {
        UUID stackId = GunItem.getItemStackId(itemStack);
        return stackId != null ? stackId : SelectedMainHandQuickMoveTracker.getInstance().resolveTrackedStateId(player, itemStack, slotIndex, isOffhand);
    }

    private static boolean hasAmbiguousHeldIdentity(Player player, UUID stackId) {
        ItemStack offhandStack;
        ItemStack mainStack = player.getMainHandItem();
        return mainStack != (offhandStack = player.getOffhandItem()) && Objects.equals(stackId, GunItem.getItemStackId(mainStack)) && Objects.equals(stackId, GunItem.getItemStackId(offhandStack));
    }

    private static GunClientState tryPromoteTrackedQuickMoveState(Player player, ItemStack itemStack, int slotIndex, boolean isOffhand, UUID stackId, Level level, PlayerSlot playerSlot, GunClientState slotState) {
        UUID recordedStateId = SelectedMainHandQuickMoveTracker.getInstance().resolveRecordedStateId(player, itemStack, slotIndex, isOffhand);
        if (recordedStateId == null || Objects.equals(recordedStateId, stackId) || isOffhand || slotIndex != player.getInventory().selected) {
            return slotState;
        }
        GunClientState trackedState = slotState;
        if (trackedState == null || !Objects.equals(trackedState.getId(), recordedStateId)) {
            trackedState = statesById.get(recordedStateId);
        }
        if (trackedState == null || trackedState.getGunItem() != itemStack.getItem()) {
            return slotState;
        }
        GunClientState replacedState = statesById.get(stackId);
        if (replacedState != null && replacedState != trackedState) {
            noSlotStates.remove(stackId, replacedState);
        }
        statesById.remove(recordedStateId, trackedState);
        noSlotStates.remove(recordedStateId, trackedState);
        trackedState.id = stackId;
        trackedState.syncAmmo(level, itemStack);
        statesById.put(stackId, trackedState);
        noSlotStates.put(stackId, trackedState);
        LOGGER.debug("Promoted selected quick-move gun state {} -> {} for player {} slot {}", (Object)recordedStateId, (Object)stackId, (Object)player.getId(), (Object)playerSlot.slotId);
        return trackedState;
    }

    private static GunClientState getOrCreateCanonicalState(ItemStack itemStack, UUID stackId, Level level) {
        return noSlotStates.computeIfAbsent(stackId, id -> {
            GunClientState state = ((GunItem)itemStack.getItem()).createState((UUID)id);
            state.syncAmmo(level, itemStack);
            return state;
        });
    }

    private static GunClientState findExistingLocalState(PlayerSlot playerSlot, UUID stackId) {
        for (Map.Entry<PlayerSlot, GunClientState> entry : localSlotStates.entrySet()) {
            PlayerSlot existingSlot = entry.getKey();
            if (existingSlot.playerEntityId != playerSlot.playerEntityId || existingSlot.isClientSide != playerSlot.isClientSide || !Objects.equals(entry.getValue().getId(), stackId)) continue;
            return entry.getValue();
        }
        return null;
    }

    private static void removeOtherLocalBindings(PlayerSlot playerSlot, UUID stackId) {
        localSlotStates.entrySet().removeIf(entry -> {
            PlayerSlot existingSlot = (PlayerSlot)entry.getKey();
            return existingSlot.playerEntityId == playerSlot.playerEntityId && existingSlot.isClientSide == playerSlot.isClientSide && Objects.equals(((GunClientState)entry.getValue()).getId(), stackId);
        });
    }

    public static GunClientState getState(UUID stateId) {
        return statesById.get(stateId);
    }

    public void addMuzzleEffect(MuzzleFlashEffect muzzleFlashEffect) {
        this.muzzleFlashEffects.add(muzzleFlashEffect);
    }

    public List<MuzzleFlashEffect> getMuzzleFlashEffects() {
        return Collections.unmodifiableList(this.muzzleFlashEffects);
    }

    public void addShellEffect(FirstPersonShellEffect shellEffect) {
        this.shellEffects.add(shellEffect);
    }

    public List<FirstPersonShellEffect> getShellEffects() {
        return Collections.unmodifiableList(this.shellEffects);
    }

    public void setSimplifiedState(FireState simplifiedFireState) {
        this.simplifiedFireState = simplifiedFireState;
    }

    private class Context {
        LivingEntity player;
        ItemStack itemStack;
        Entity targetEntity;

        public Context(GunClientState gunClientState, LivingEntity player, ItemStack itemStack) {
            this.player = player;
            this.itemStack = itemStack;
        }

        public Context(GunClientState gunClientState, LivingEntity player, ItemStack itemStack, Entity targetEntity) {
            this.player = player;
            this.itemStack = itemStack;
            this.targetEntity = targetEntity;
        }

        public FireModeInstance getFireModeInstance() {
            return GunItem.getFireModeInstance(this.itemStack);
        }
    }

    public static enum FireState {
        PREPARE_IDLE,
        IDLE,
        IDLE_COOLDOWN,
        DRAW,
        DRAW_COOLDOWN,
        CHANGE_FIRE_MODE,
        CHANGE_FIRE_MODE_COOLDOWN,
        PREPARE_FIRE_SINGLE,
        PREPARE_FIRE_COOLDOWN_SINGLE,
        PREPARE_FIRE_AUTO,
        PREPARE_FIRE_COOLDOWN_AUTO,
        PREPARE_FIRE_BURST,
        PREPARE_FIRE_COOLDOWN_BURST,
        FIRE_SINGLE,
        FIRE_COOLDOWN_SINGLE,
        FIRE_AUTO,
        FIRE_COOLDOWN_AUTO,
        FIRE_BURST,
        FIRE_COOLDOWN_BURST,
        COMPLETE_FIRE,
        COMPLETE_FIRE_COOLDOWN,
        OVERHEAT,
        OVERHEAT_COOLDOWN,
        PREPARE_RELOAD,
        PREPARE_RELOAD_COOLDOWN,
        PREPARE_RELOAD_ITER,
        PREPARE_RELOAD_COOLDOWN_ITER,
        RELOAD,
        RELOAD_COOLDOWN,
        RELOAD_ITER,
        RELOAD_COOLDOWN_ITER,
        COMPLETE_RELOAD,
        COMPLETE_RELOAD_COOLDOWN,
        INSPECT,
        INSPECT_COOLDOWN,
        HIDING,
        HIDDEN;


        public static FireState getSimplifiedFireState(FireState fireState) {
            return switch (fireState.ordinal()) {
                case 3, 4 -> DRAW;
                case 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 -> FIRE_SINGLE;
                case 21, 22 -> OVERHEAT;
                case 23, 24, 25, 26, 27, 28, 29, 30, 31, 32 -> RELOAD;
                case 33, 34 -> INSPECT;
                case 35, 36 -> HIDDEN;
                default -> IDLE;
            };
        }
    }

    private static class PlayerSlot {
        int playerEntityId;
        int slotId;
        boolean isClientSide;

        PlayerSlot(int playerEntityId, int slotId, boolean isClientSide) {
            this.playerEntityId = playerEntityId;
            this.slotId = slotId;
            this.isClientSide = isClientSide;
        }

        public int hashCode() {
            return Objects.hash(this.isClientSide, this.playerEntityId, this.slotId);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            PlayerSlot other = (PlayerSlot)obj;
            return this.isClientSide == other.isClientSide && this.playerEntityId == other.playerEntityId && this.slotId == other.slotId;
        }
    }
}

