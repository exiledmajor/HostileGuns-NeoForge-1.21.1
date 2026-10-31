/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animatable.GeoItem
 *  software.bernie.geckolib.animation.AnimationController
 *  software.bernie.geckolib.animation.AnimationController$State
 *  software.bernie.geckolib.animation.AnimationProcessor$QueuedAnimation
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.animation.RawAnimation$Stage
 */
package com.vicmatskiv.pointblank.client;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.controller.BlendingAnimationController;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.StateMachine;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.RawAnimation;

public class FirstPersonWalkingAnimationHandler {
    public static final String WALKING_CONTROLLER_NAME = "walking";
    private static final float MIN_SPEED_SQUARED = 1.0E-4f;
    private static final long SPRINT_INTENT_GRACE_MILLIS = 120L;
    private static final Map<String, RawAnimation> RAW_ANIMATION_CACHE = new ConcurrentHashMap<String, RawAnimation>();
    private final WalkingTrack mainHandWalkingTrack;
    private final WalkingTrack offhandWalkingTrack;

    public FirstPersonWalkingAnimationHandler() {
        long now = System.currentTimeMillis();
        this.mainHandWalkingTrack = new WalkingTrack(this.createFirstPersonAnimationStateMachine(), now);
        this.offhandWalkingTrack = new WalkingTrack(this.createFirstPersonAnimationStateMachine(), now);
    }

    private static boolean hasCurrentSprintIntent(LocalPlayer player) {
        return player.isSprinting() || Minecraft.getInstance().options.keySprint.isDown() && player.input.up && player.input.getMoveVector().lengthSquared() > 1.0E-4f;
    }

    private static boolean isAnyHeldGunReloading(LocalPlayer player, GunClientState renderedState) {
        if (renderedState != null && renderedState.isReloading()) {
            return true;
        }
        GunClientState mainHandState = GunClientState.getMainHandState((Player)player);
        if (mainHandState != null && mainHandState != renderedState && mainHandState.isReloading()) {
            return true;
        }
        GunClientState offhandState = GunClientState.getOffhandState((Player)player);
        return offhandState != null && offhandState != renderedState && offhandState.isReloading();
    }

    private StateMachine<PlayerWalkingState, PlayerWalkingStateContext> createFirstPersonAnimationStateMachine() {
        StateMachine.Builder<PlayerWalkingState, PlayerWalkingStateContext> builder = new StateMachine.Builder<PlayerWalkingState, PlayerWalkingStateContext>();
        Minecraft mc = Minecraft.getInstance();
        builder.withTransition(PlayerWalkingState.NONE, PlayerWalkingState.STANDING, ctx -> mc.options.getCameraType() == CameraType.FIRST_PERSON && ctx.state != null && !ctx.state.isDrawing() && ctx.msSinceReset > 100L, StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition((PlayerWalkingState)((Object)List.of(PlayerWalkingState.STANDING, PlayerWalkingState.WALKING, PlayerWalkingState.WALKING_BACKWARDS, PlayerWalkingState.WALKING_LEFT, PlayerWalkingState.WALKING_RIGHT, PlayerWalkingState.RUNNING, PlayerWalkingState.PREPARE_RUNNING, PlayerWalkingState.COMPLETE_RUNNING, PlayerWalkingState.OFF_GROUND, PlayerWalkingState.OFF_GROUND_SPRINTING)), PlayerWalkingState.NONE, ctx -> mc.options.getCameraType() != CameraType.FIRST_PERSON, StateMachine.TransitionMode.AUTO, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null);
        builder.withTransition((PlayerWalkingState)((Object)List.of(PlayerWalkingState.STANDING, PlayerWalkingState.WALKING, PlayerWalkingState.WALKING_BACKWARDS, PlayerWalkingState.WALKING_LEFT, PlayerWalkingState.WALKING_RIGHT)), PlayerWalkingState.OFF_GROUND, ctx -> !ctx.player.onGround() && !ctx.player.isSprinting(), StateMachine.TransitionMode.AUTO, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null);
        builder.withTransition(PlayerWalkingState.COMPLETE_RUNNING, PlayerWalkingState.OFF_GROUND, ctx -> ctx.speedSquared > 1.0E-4f && !ctx.player.onGround() && (!ctx.player.isSprinting() || ctx.player.isMovingSlowly()) && ctx.isMovingForward() && !ctx.state.isAiming() && !FirstPersonWalkingAnimationHandler.isPlayingAnimation(GunItem.RAW_ANIMATION_COMPLETE_RUNNING, ctx.itemStack), StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition((PlayerWalkingState)((Object)List.of(PlayerWalkingState.RUNNING)), PlayerWalkingState.OFF_GROUND_SPRINTING, ctx -> !ctx.isReloading() && !ctx.player.onGround() && ctx.player.isSprinting(), StateMachine.TransitionMode.AUTO, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null);
        builder.withTransition(PlayerWalkingState.PREPARE_RUNNING, PlayerWalkingState.OFF_GROUND_SPRINTING, ctx -> !ctx.isReloading() && ctx.player.isSprinting() && !ctx.player.onGround() && !ctx.player.isMovingSlowly() && !FirstPersonWalkingAnimationHandler.isPlayingAnimation(GunItem.RAW_ANIMATION_PREPARE_RUNNING, ctx.itemStack), StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition((PlayerWalkingState)((Object)List.of(PlayerWalkingState.STANDING, PlayerWalkingState.WALKING, PlayerWalkingState.WALKING_BACKWARDS, PlayerWalkingState.WALKING_LEFT, PlayerWalkingState.WALKING_RIGHT)), PlayerWalkingState.WALKING_AIMING, ctx -> ctx.speedSquared > 1.0E-4f && ctx.player.onGround() && !ctx.player.isSprinting() && ctx.state.isAiming(), StateMachine.TransitionMode.AUTO, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null);
        builder.withTransition(PlayerWalkingState.COMPLETE_RUNNING, PlayerWalkingState.WALKING_AIMING, ctx -> ctx.speedSquared > 1.0E-4f && ctx.player.onGround() && !ctx.player.isSprinting() && ctx.state.isAiming() && !FirstPersonWalkingAnimationHandler.isPlayingAnimation(GunItem.RAW_ANIMATION_COMPLETE_RUNNING, ctx.itemStack), StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition((PlayerWalkingState)((Object)List.of(PlayerWalkingState.OFF_GROUND, PlayerWalkingState.STANDING, PlayerWalkingState.WALKING_AIMING, PlayerWalkingState.WALKING_BACKWARDS, PlayerWalkingState.WALKING_LEFT, PlayerWalkingState.WALKING_RIGHT)), PlayerWalkingState.WALKING, ctx -> ctx.speedSquared > 1.0E-4f && ctx.player.onGround() && !ctx.player.isSprinting() && ctx.isMovingForward() && !ctx.state.isAiming(), StateMachine.TransitionMode.AUTO, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null);
        builder.withTransition(PlayerWalkingState.COMPLETE_RUNNING, PlayerWalkingState.WALKING, ctx -> ctx.speedSquared > 1.0E-4f && ctx.player.onGround() && (!ctx.player.isSprinting() || ctx.player.isMovingSlowly()) && ctx.isMovingForward() && !ctx.state.isAiming() && !FirstPersonWalkingAnimationHandler.isPlayingAnimation(GunItem.RAW_ANIMATION_COMPLETE_RUNNING, ctx.itemStack), StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition((PlayerWalkingState)((Object)List.of(PlayerWalkingState.OFF_GROUND, PlayerWalkingState.STANDING, PlayerWalkingState.WALKING, PlayerWalkingState.WALKING_AIMING, PlayerWalkingState.WALKING_LEFT, PlayerWalkingState.WALKING_RIGHT)), PlayerWalkingState.WALKING_BACKWARDS, ctx -> ctx.speedSquared > 1.0E-4f && ctx.player.onGround() && (!ctx.player.isSprinting() || ctx.player.isMovingSlowly()) && ctx.isMovingBackward() && !ctx.state.isAiming(), StateMachine.TransitionMode.AUTO, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null);
        builder.withTransition(PlayerWalkingState.COMPLETE_RUNNING, PlayerWalkingState.WALKING_BACKWARDS, ctx -> ctx.speedSquared > 1.0E-4f && (!ctx.player.isSprinting() || ctx.player.isMovingSlowly()) && ctx.isMovingBackward() && !ctx.state.isAiming() && !FirstPersonWalkingAnimationHandler.isPlayingAnimation(GunItem.RAW_ANIMATION_COMPLETE_RUNNING, ctx.itemStack), StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition((PlayerWalkingState)((Object)List.of(PlayerWalkingState.OFF_GROUND, PlayerWalkingState.STANDING, PlayerWalkingState.WALKING, PlayerWalkingState.WALKING_AIMING, PlayerWalkingState.WALKING_BACKWARDS, PlayerWalkingState.WALKING_RIGHT)), PlayerWalkingState.WALKING_LEFT, ctx -> ctx.speedSquared > 1.0E-4f && ctx.player.onGround() && (!ctx.player.isSprinting() || ctx.player.isMovingSlowly()) && ctx.isMovingLeft() && !ctx.state.isAiming(), StateMachine.TransitionMode.AUTO, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null);
        builder.withTransition(PlayerWalkingState.COMPLETE_RUNNING, PlayerWalkingState.WALKING_LEFT, ctx -> ctx.speedSquared > 1.0E-4f && ctx.player.onGround() && (!ctx.player.isSprinting() || ctx.player.isMovingSlowly()) && ctx.isMovingLeft() && !ctx.state.isAiming() && !FirstPersonWalkingAnimationHandler.isPlayingAnimation(GunItem.RAW_ANIMATION_COMPLETE_RUNNING, ctx.itemStack), StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition((PlayerWalkingState)((Object)List.of(PlayerWalkingState.OFF_GROUND, PlayerWalkingState.STANDING, PlayerWalkingState.WALKING, PlayerWalkingState.WALKING_AIMING, PlayerWalkingState.WALKING_BACKWARDS, PlayerWalkingState.WALKING_LEFT)), PlayerWalkingState.WALKING_RIGHT, ctx -> ctx.speedSquared > 1.0E-4f && ctx.player.onGround() && !ctx.player.isSprinting() && !ctx.state.isAiming() && ctx.isMovingRight(), StateMachine.TransitionMode.AUTO, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null);
        builder.withTransition(PlayerWalkingState.COMPLETE_RUNNING, PlayerWalkingState.WALKING_RIGHT, ctx -> ctx.speedSquared > 1.0E-4f && ctx.player.onGround() && (!ctx.player.isSprinting() || ctx.player.isMovingSlowly()) && ctx.isMovingRight() && !ctx.state.isAiming() && !FirstPersonWalkingAnimationHandler.isPlayingAnimation(GunItem.RAW_ANIMATION_COMPLETE_RUNNING, ctx.itemStack), StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition((PlayerWalkingState)((Object)List.of(PlayerWalkingState.OFF_GROUND, PlayerWalkingState.WALKING, PlayerWalkingState.WALKING_AIMING, PlayerWalkingState.WALKING_BACKWARDS, PlayerWalkingState.WALKING_LEFT, PlayerWalkingState.WALKING_RIGHT)), PlayerWalkingState.STANDING, ctx -> ctx.player.onGround() && !ctx.player.isSprinting() && ctx.speedSquared <= 1.0E-4f, StateMachine.TransitionMode.AUTO, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null);
        builder.withTransition(PlayerWalkingState.COMPLETE_RUNNING, PlayerWalkingState.STANDING, ctx -> ctx.player.onGround() && !ctx.player.isSprinting() && ctx.speedSquared <= 1.0E-4f && !FirstPersonWalkingAnimationHandler.isPlayingAnimation(GunItem.RAW_ANIMATION_COMPLETE_RUNNING, ctx.itemStack), StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition((PlayerWalkingState)((Object)List.of(PlayerWalkingState.OFF_GROUND, PlayerWalkingState.STANDING, PlayerWalkingState.WALKING, PlayerWalkingState.WALKING_AIMING, PlayerWalkingState.WALKING_BACKWARDS, PlayerWalkingState.WALKING_LEFT, PlayerWalkingState.WALKING_RIGHT, PlayerWalkingState.COMPLETE_RUNNING)), PlayerWalkingState.PREPARE_RUNNING, ctx -> !ctx.isReloading() && ctx.player.isSprinting() && !ctx.player.isMovingSlowly() && !ctx.state.isFiring(), StateMachine.TransitionMode.AUTO, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null);
        builder.withTransition((PlayerWalkingState)((Object)List.of(PlayerWalkingState.OFF_GROUND_SPRINTING)), PlayerWalkingState.RUNNING, ctx -> !ctx.isReloading() && ctx.player.onGround(), StateMachine.TransitionMode.AUTO, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null);
        builder.withTransition(PlayerWalkingState.PREPARE_RUNNING, PlayerWalkingState.RUNNING, ctx -> !ctx.isReloading() && ctx.player.isSprinting() && ctx.player.onGround() && !ctx.player.isMovingSlowly() && !FirstPersonWalkingAnimationHandler.isPlayingAnimation(GunItem.RAW_ANIMATION_PREPARE_RUNNING, ctx.itemStack), StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition((PlayerWalkingState)((Object)List.of(PlayerWalkingState.PREPARE_RUNNING, PlayerWalkingState.RUNNING, PlayerWalkingState.OFF_GROUND_SPRINTING)), PlayerWalkingState.COMPLETE_RUNNING, ctx -> !ctx.hasSprintIntent() || ctx.player.isMovingSlowly(), StateMachine.TransitionMode.AUTO, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null, (StateMachine.Action<PlayerWalkingState, PlayerWalkingStateContext>)null);
        builder.withOnChangeStateAction((ctx, from, to) -> {
            if (to == PlayerWalkingState.NONE) {
                FirstPersonWalkingAnimationHandler.resetWalkingAnimation(ctx);
            } else {
                FirstPersonWalkingAnimationHandler.applyWalkingAnimation(ctx, to);
            }
        });
        return builder.build(PlayerWalkingState.STANDING);
    }

    private static String getAnimationNameForState(PlayerWalkingState state) {
        return switch (state.ordinal()) {
            default -> throw new MatchException(null, null);
            case 3 -> "animation.model.standing";
            case 8 -> "animation.model.walking_aiming";
            case 4 -> "animation.model.walking";
            case 5 -> "animation.model.walking_backwards";
            case 6 -> "animation.model.walking_left";
            case 7 -> "animation.model.walking_right";
            case 9 -> "animation.model.runningstart";
            case 10 -> "animation.model.running";
            case 11 -> "animation.model.runningend";
            case 1 -> "animation.model.off_ground";
            case 2 -> "animation.model.off_ground_sprinting";
            case 0 -> null;
        };
    }

    private static ItemDisplayContext getPerspective(PlayerWalkingStateContext ctx) {
        return ctx.renderedContext != null ? GunItem.getFirstPersonDisplayContext(ctx.renderedContext) : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }

    private static AnimationController<GeoAnimatable> getWalkingController(PlayerWalkingStateContext ctx) {
        Item item = ctx.itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return null;
        }
        GunItem gunItem = (GunItem)item;
        return gunItem.getGeoAnimationController(WALKING_CONTROLLER_NAME, ctx.itemStack, FirstPersonWalkingAnimationHandler.getPerspective(ctx));
    }

    private static void resetWalkingAnimation(PlayerWalkingStateContext ctx) {
        AnimationController<GeoAnimatable> controller = FirstPersonWalkingAnimationHandler.getWalkingController(ctx);
        if (controller == null) {
            return;
        }
        controller.forceAnimationReset();
        controller.setAnimation(null);
        if (controller instanceof BlendingAnimationController) {
            BlendingAnimationController blendingController = (BlendingAnimationController)controller;
            blendingController.clearAll();
        }
    }

    private static void applyWalkingAnimation(PlayerWalkingStateContext ctx, PlayerWalkingState state) {
        Item item;
        String baseName = FirstPersonWalkingAnimationHandler.getAnimationNameForState(state);
        if (baseName == null || !((item = ctx.itemStack.getItem()) instanceof GunItem)) {
            return;
        }
        GunItem gunItem = (GunItem)item;
        String resolvedName = gunItem.resolveDualWieldAnimationName((LivingEntity)ctx.player, ctx.state, ctx.itemStack, baseName);
        RawAnimation rawAnimation = RAW_ANIMATION_CACHE.computeIfAbsent(resolvedName, name -> RawAnimation.begin().thenPlay(name));
        AnimationController<GeoAnimatable> controller = FirstPersonWalkingAnimationHandler.getWalkingController(ctx);
        if (controller != null) {
            controller.setAnimation(rawAnimation);
            return;
        }
        gunItem.triggerAnim((Entity)ctx.player, GeoItem.getId((ItemStack)ctx.itemStack), WALKING_CONTROLLER_NAME, baseName);
    }

    public void handlePlayerFirstPersonMovement(Player player, ItemStack itemStack) {
        this.handlePlayerFirstPersonMovement(player, itemStack, null);
    }

    public void handlePlayerFirstPersonMovement(Player player, ItemStack itemStack, GunItem.OperableGunContext renderedContext) {
        PlayerWalkingStateContext walkingContext;
        PlayerWalkingState currentState;
        if (!(itemStack.getItem() instanceof GunItem)) {
            return;
        }
        Player mainPlayer = ClientUtil.getClientPlayer();
        if (player != mainPlayer) {
            return;
        }
        LocalPlayer localPlayer = (LocalPlayer)player;
        GunItem.OperableGunContext context = renderedContext != null ? renderedContext : GunItem.resolveRenderableContextForStack((Player)localPlayer, itemStack);
        WalkingTrack walkingTrack = this.resolveWalkingTrack(localPlayer, itemStack, context);
        UUID stackId = GunItem.getItemStackId(itemStack);
        long now = System.currentTimeMillis();
        if (!Objects.equals(walkingTrack.stackId, stackId)) {
            walkingTrack.reset(now, stackId);
        }
        if (FirstPersonWalkingAnimationHandler.hasCurrentSprintIntent(localPlayer)) {
            walkingTrack.lastSprintIntentTs = now;
        }
        if ((currentState = walkingTrack.stateMachine.update(walkingContext = new PlayerWalkingStateContext(localPlayer, itemStack, context, now, walkingTrack.resetTs, walkingTrack.lastSprintIntentTs))) == PlayerWalkingState.NONE) {
            FirstPersonWalkingAnimationHandler.resetWalkingAnimation(walkingContext);
        } else {
            FirstPersonWalkingAnimationHandler.applyWalkingAnimation(walkingContext, currentState);
        }
    }

    public void reset(Player player, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return;
        }
        if (!(player instanceof LocalPlayer)) {
            return;
        }
        LocalPlayer localPlayer = (LocalPlayer)player;
        GunItem.OperableGunContext context = GunItem.resolveRenderableContextForStack(player, itemStack);
        WalkingTrack walkingTrack = this.resolveWalkingTrack(localPlayer, itemStack, context);
        walkingTrack.reset(System.currentTimeMillis(), GunItem.getItemStackId(itemStack));
    }

    private WalkingTrack resolveWalkingTrack(LocalPlayer player, ItemStack itemStack, GunItem.OperableGunContext renderedContext) {
        boolean matchesOffhand;
        if (renderedContext != null) {
            return renderedContext.offhand() ? this.offhandWalkingTrack : this.mainHandWalkingTrack;
        }
        if (itemStack == player.getMainHandItem()) {
            return this.mainHandWalkingTrack;
        }
        if (itemStack == player.getOffhandItem()) {
            return this.offhandWalkingTrack;
        }
        UUID stackId = GunItem.getItemStackId(itemStack);
        boolean matchesMain = stackId != null && Objects.equals(stackId, GunItem.getItemStackId(player.getMainHandItem()));
        boolean bl = matchesOffhand = stackId != null && Objects.equals(stackId, GunItem.getItemStackId(player.getOffhandItem()));
        if (matchesOffhand && !matchesMain) {
            return this.offhandWalkingTrack;
        }
        return this.mainHandWalkingTrack;
    }

    private static AnimationController<GeoAnimatable> getWalkingController(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return null;
        }
        GunItem gunItem = (GunItem)item;
        LocalPlayer player = Minecraft.getInstance().player;
        ItemDisplayContext perspective = player != null ? GunItem.resolveFirstPersonDisplayContext((Player)player, itemStack) : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        return gunItem.getGeoAnimationController(WALKING_CONTROLLER_NAME, itemStack, perspective);
    }

    public static boolean isPlayingAnimation(RawAnimation rawAnimation, ItemStack itemStack) {
        BlendingAnimationController bac;
        Item item = itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return false;
        }
        GunItem gunItem = (GunItem)item;
        AnimationController<GeoAnimatable> controller = FirstPersonWalkingAnimationHandler.getWalkingController(itemStack);
        return controller != null && (FirstPersonWalkingAnimationHandler.sameAnimation(rawAnimation, controller.getCurrentRawAnimation()) || controller instanceof BlendingAnimationController && FirstPersonWalkingAnimationHandler.sameAnimation(rawAnimation, (bac = (BlendingAnimationController)controller).getTriggeredAnimation())) && controller.getAnimationState() != AnimationController.State.STOPPED;
    }

    public static boolean isPlayingBlendingAnimation(Collection<RawAnimation> rawAnimations, ItemStack itemStack, double minProgress, double maxProgress) {
        boolean result;
        Item item = itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return false;
        }
        GunItem gunItem = (GunItem)item;
        AnimationController<GeoAnimatable> controller = FirstPersonWalkingAnimationHandler.getWalkingController(itemStack);
        if (!(controller instanceof BlendingAnimationController)) {
            return false;
        }
        BlendingAnimationController bac = (BlendingAnimationController)controller;
        boolean bl = result = FirstPersonWalkingAnimationHandler.collectionContains(rawAnimations, bac.getTriggeredAnimation()) && bac.getAnimationState() != AnimationController.State.STOPPED;
        if (!result) {
            return false;
        }
        double progress = bac.getAnimationProgress();
        return minProgress <= progress && progress < maxProgress;
    }

    private static boolean collectionContains(Collection<RawAnimation> items, RawAnimation item) {
        return item != null && items.stream().anyMatch(animation -> FirstPersonWalkingAnimationHandler.sameAnimation(animation, item));
    }

    private static boolean sameAnimation(RawAnimation first, RawAnimation second) {
        if (first == null || second == null || first.getAnimationStages().isEmpty() || second.getAnimationStages().isEmpty()) {
            return first == second;
        }
        return Objects.equals(GunItem.stripDualWieldAnimationSuffix(((RawAnimation.Stage)first.getAnimationStages().get(0)).animationName()), GunItem.stripDualWieldAnimationSuffix(((RawAnimation.Stage)second.getAnimationStages().get(0)).animationName()));
    }

    public static boolean isPlayingAnimation(String animationName, ItemStack itemStack, String controllerName) {
        Item item = itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return false;
        }
        GunItem gunItem = (GunItem)item;
        LocalPlayer player = Minecraft.getInstance().player;
        ItemDisplayContext perspective = player != null ? GunItem.resolveFirstPersonDisplayContext((Player)player, itemStack) : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        AnimationController<GeoAnimatable> controller = gunItem.getGeoAnimationController(controllerName, itemStack, perspective);
        if (controller == null) {
            return false;
        }
        AnimationProcessor.QueuedAnimation currentAnimation = controller.getCurrentAnimation();
        return currentAnimation != null && GunItem.stripDualWieldAnimationSuffix(animationName).equals(GunItem.stripDualWieldAnimationSuffix(currentAnimation.animation().name())) && controller.getAnimationState() != AnimationController.State.STOPPED;
    }

    private static final class WalkingTrack {
        final StateMachine<PlayerWalkingState, PlayerWalkingStateContext> stateMachine;
        long resetTs;
        long lastSprintIntentTs;
        UUID stackId;

        WalkingTrack(StateMachine<PlayerWalkingState, PlayerWalkingStateContext> stateMachine, long now) {
            this.stateMachine = stateMachine;
            this.resetTs = now;
            this.lastSprintIntentTs = now;
        }

        void reset(long now, UUID stackId) {
            this.resetTs = now;
            this.lastSprintIntentTs = 0L;
            this.stackId = stackId;
            this.stateMachine.resetToState(PlayerWalkingState.NONE);
        }
    }

    private static enum PlayerWalkingState {
        NONE,
        OFF_GROUND,
        OFF_GROUND_SPRINTING,
        STANDING,
        WALKING,
        WALKING_BACKWARDS,
        WALKING_LEFT,
        WALKING_RIGHT,
        WALKING_AIMING,
        PREPARE_RUNNING,
        RUNNING,
        COMPLETE_RUNNING;

    }

    private static class PlayerWalkingStateContext {
        final LocalPlayer player;
        final ItemStack itemStack;
        final GunItem.OperableGunContext renderedContext;
        final float speedSquared;
        final GunClientState state;
        final long msSinceReset;
        final boolean sprintIntentActive;
        final boolean reloadInProgress;

        public PlayerWalkingStateContext(LocalPlayer player, ItemStack itemStack, GunItem.OperableGunContext renderedContext, long now, long resetTs, long lastSprintIntentTs) {
            this.player = player;
            this.itemStack = itemStack;
            this.renderedContext = renderedContext;
            this.speedSquared = player.input.getMoveVector().lengthSquared();
            this.state = renderedContext != null ? GunClientState.getState((Player)player, itemStack, renderedContext.slotIndex(), renderedContext.offhand()) : GunClientState.getMainHeldState();
            this.msSinceReset = Math.max(0L, now - resetTs);
            boolean aimingForcesWalking = this.state != null && this.state.isAiming();
            this.sprintIntentActive = !aimingForcesWalking && (FirstPersonWalkingAnimationHandler.hasCurrentSprintIntent(player) || now - lastSprintIntentTs <= 120L);
            this.reloadInProgress = FirstPersonWalkingAnimationHandler.isAnyHeldGunReloading(player, this.state);
        }

        boolean isMovingForward() {
            return this.player.input.up;
        }

        boolean isMovingBackward() {
            return this.player.input.down;
        }

        boolean isMovingLeft() {
            return this.player.input.left && !this.player.input.up && !this.player.input.down;
        }

        boolean isMovingRight() {
            return this.player.input.right && !this.player.input.up && !this.player.input.down;
        }

        boolean hasSprintIntent() {
            return !this.reloadInProgress && this.sprintIntentActive;
        }

        boolean isReloading() {
            return this.reloadInProgress;
        }
    }
}

