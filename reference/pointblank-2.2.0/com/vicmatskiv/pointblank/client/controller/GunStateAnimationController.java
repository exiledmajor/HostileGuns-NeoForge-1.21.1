/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animation.Animation
 *  software.bernie.geckolib.animation.Animation$LoopType
 *  software.bernie.geckolib.animation.AnimationController
 *  software.bernie.geckolib.animation.AnimationController$AnimationStateHandler
 *  software.bernie.geckolib.animation.AnimationController$State
 *  software.bernie.geckolib.animation.AnimationState
 *  software.bernie.geckolib.animation.PlayState
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.constant.DataTickets
 *  software.bernie.geckolib.model.GeoModel
 */
package com.vicmatskiv.pointblank.client.controller;

import com.vicmatskiv.pointblank.Constants;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.GunStateListener;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.ClientUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;

public class GunStateAnimationController
extends AnimationController<GunItem>
implements GunStateListener {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private final RawAnimation animation;
    private Predicate<ConditionContext> shouldAnimate;
    private Deque<Action> pendingActions = new ArrayDeque<Action>();
    private String animationName;
    private Map<String, RawAnimation> animations = new HashMap<String, RawAnimation>();
    private final boolean loop;
    private boolean hideTraceArmed;

    public GunStateAnimationController(GunItem animatable, String controllerName, String animationName, Predicate<ConditionContext> shouldAnimate, boolean loop) {
        super((GeoAnimatable)animatable, controllerName, (AnimationController.AnimationStateHandler)new StateHandler());
        this.loop = loop;
        this.animation = RawAnimation.begin().then(animationName, loop ? Animation.LoopType.LOOP : Animation.LoopType.HOLD_ON_LAST_FRAME);
        this.animations.put(animationName, this.animation);
        this.animationName = animationName;
        this.shouldAnimate = shouldAnimate;
    }

    public GunStateAnimationController(GunItem animatable, String controllerName, String animationName, Predicate<ConditionContext> shouldAnimate) {
        this(animatable, controllerName, animationName, shouldAnimate, false);
    }

    RawAnimation getAnimation(String animationName) {
        return this.animations.computeIfAbsent(animationName, n -> RawAnimation.begin().then(animationName, this.loop ? Animation.LoopType.LOOP : Animation.LoopType.HOLD_ON_LAST_FRAME));
    }

    double getTransitionLengthTicks() {
        return this.transitionLength;
    }

    public void scheduleReset(LivingEntity player, GunClientState state, ItemStack itemStack, String animationName) {
        String requestedName = animationName != null ? animationName : this.animationName;
        requestedName = ((GunItem)this.animatable).resolveDualWieldAnimationName(player, state, itemStack, requestedName);
        this.pendingActions.addLast(new ResetAction(requestedName, -1));
    }

    public void scheduleReset(LivingEntity player, GunClientState state, ItemStack itemStack, String animationName, int transitionTicks) {
        if (Constants.DEBUG_HIDE_ANIMATION && "hide_controller".equals(this.getName())) {
            this.hideTraceArmed = true;
            LOGGER.info("[HideSwapTrace] controller-scheduled animation={} transitionTicks={} uuid={}", (Object)animationName, (Object)Math.max(0, transitionTicks), (Object)GunItem.getItemStackId(itemStack));
        }
        String requestedName = animationName != null ? animationName : this.animationName;
        requestedName = ((GunItem)this.animatable).resolveDualWieldAnimationName(player, state, itemStack, requestedName);
        this.pendingActions.addLast(new ResetAction(requestedName, Math.max(0, transitionTicks)));
    }

    public void scheduleReset(LivingEntity player, GunClientState state, ItemStack itemStack) {
        this.scheduleReset(player, state, itemStack, this.animationName);
    }

    protected boolean shouldContinueAnimation(GunClientState gunClientState) {
        return false;
    }

    private static class StateHandler
    implements AnimationController.AnimationStateHandler<GunItem> {
        private PlayState currentPlayState;

        private StateHandler() {
        }

        public PlayState handle(AnimationState<GunItem> state) {
            GunItem.OperableGunContext context;
            GunStateAnimationController controller = this.getThisController(state);
            ItemDisplayContext perspective = (ItemDisplayContext)state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE);
            if (perspective != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && perspective != ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                return PlayState.STOP;
            }
            while (!controller.pendingActions.isEmpty()) {
                Action action = controller.pendingActions.removeFirst();
                action.execute(state);
            }
            Player player = ClientUtil.getClientPlayer();
            ItemStack itemStack = (ItemStack)state.getData(DataTickets.ITEMSTACK);
            GunClientState gunClientState = null;
            if (player != null && itemStack != null && itemStack.getItem() instanceof GunItem && (context = StateHandler.resolveRenderedContext(player, itemStack, perspective)) != null) {
                boolean shouldContinue;
                ItemStack contextStack = context.itemStack();
                gunClientState = GunClientState.getState(player, contextStack, context.slotIndex(), context.offhand());
                boolean shouldAnimate = false;
                if (gunClientState != null && (shouldAnimate = controller.shouldAnimate.test(new ConditionContext((LivingEntity)player, contextStack, gunClientState, GunItem.getFirstPersonDisplayContext(player, context.hand()))))) {
                    return this.setPlayState(PlayState.CONTINUE, controller, gunClientState, player);
                }
                if (gunClientState != null && (shouldContinue = controller.shouldContinueAnimation(gunClientState))) {
                    return this.setPlayState(PlayState.CONTINUE, controller, gunClientState, player);
                }
            }
            return this.setPlayState(PlayState.STOP, controller, gunClientState, player);
        }

        private static GunItem.OperableGunContext resolveRenderedContext(Player player, ItemStack renderedStack, ItemDisplayContext perspective) {
            GunItem.OperableGunContext byPerspective;
            GunItem.OperableGunContext byStack = GunItem.resolveRenderableContextForStack(player, renderedStack);
            if (byStack != null && GunItem.getFirstPersonDisplayContext(player, byStack.hand()) == perspective) {
                return byStack;
            }
            GunItem.OperableGunContext operableGunContext = byPerspective = perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? GunItem.resolveOffhandGunContext(player) : GunItem.resolveMainHandGunContext(player);
            if (byPerspective == null) {
                return null;
            }
            UUID renderedId = GunItem.getItemStackId(renderedStack);
            UUID contextId = GunItem.getItemStackId(byPerspective.itemStack());
            return renderedStack == byPerspective.itemStack() || renderedId != null && Objects.equals(renderedId, contextId) ? byPerspective : null;
        }

        private PlayState setPlayState(PlayState playState, GunStateAnimationController controller, GunClientState gunClientState, Player player) {
            if (controller.hideTraceArmed && this.currentPlayState != playState) {
                LOGGER.info("[HideSwapTrace] controller-play {}->{} gunState={} animation={} controllerState={}", (Object)this.currentPlayState, (Object)playState, (Object)(gunClientState != null ? gunClientState.getFireState() : null), (Object)(controller.getCurrentAnimation() != null ? controller.getCurrentAnimation().animation().name() : null), (Object)controller.getAnimationState());
            }
            this.currentPlayState = playState;
            return this.currentPlayState;
        }

        private GunStateAnimationController getThisController(AnimationState<GunItem> state) {
            return (GunStateAnimationController)state.getController();
        }
    }

    private class ResetAction
    implements Action {
        private String animationName;
        private int transitionTicks;

        ResetAction(String animationName, int transitionTicks) {
            this.animationName = animationName;
            this.transitionTicks = transitionTicks;
        }

        @Override
        public void execute(AnimationState<GunItem> state) {
            GeoModel model = GunStateAnimationController.this.lastModel;
            if (model != null) {
                Animation a = model.getAnimation((GeoAnimatable)((GunItem)GunStateAnimationController.this.animatable), this.animationName);
                if (a != null) {
                    if (this.transitionTicks >= 0) {
                        GunStateAnimationController.this.transitionLength = this.transitionTicks;
                    }
                    String currentAnimName = state.getController().getCurrentAnimation() != null ? state.getController().getCurrentAnimation().animation().name() : null;
                    boolean isSameAnimation = currentAnimName != null && currentAnimName.equals(this.animationName);
                    AnimationController.State controllerState = state.getController().getAnimationState();
                    if (isSameAnimation && GunStateAnimationController.this.loop && controllerState == AnimationController.State.RUNNING) {
                        return;
                    }
                    RawAnimation newAnimation = GunStateAnimationController.this.getAnimation(this.animationName);
                    ((StateHandler)((GunStateAnimationController)GunStateAnimationController.this).stateHandler).currentPlayState = null;
                    state.resetCurrentAnimation();
                    GunStateAnimationController.this.setAnimation(newAnimation);
                    if (GunStateAnimationController.this.hideTraceArmed) {
                        LOGGER.info("[HideSwapTrace] controller-reset animation={} lengthTicks={} transitionTicks={} priorAnimation={} priorState={}", (Object)this.animationName, (Object)a.length(), (Object)this.transitionTicks, (Object)currentAnimName, (Object)controllerState);
                    }
                } else {
                    if (GunStateAnimationController.this.hideTraceArmed) {
                        LOGGER.info("[HideSwapTrace] controller-reset animation-missing={}", (Object)this.animationName);
                    } else {
                        LOGGER.debug("Controller: {}, Animation not found: {}", (Object)GunStateAnimationController.this.getName(), (Object)this.animationName);
                    }
                    state.resetCurrentAnimation();
                }
            } else if (GunStateAnimationController.this.hideTraceArmed) {
                LOGGER.info("[HideSwapTrace] controller-reset model-unavailable animation={}", (Object)this.animationName);
            }
        }
    }

    private static interface Action {
        public void execute(AnimationState<GunItem> var1);
    }
}

