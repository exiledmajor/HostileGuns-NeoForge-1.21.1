/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.client;

import com.vicmatskiv.pointblank.client.GunStateListener;
import com.vicmatskiv.pointblank.item.Drawable;
import com.vicmatskiv.pointblank.item.ItemExtra;
import com.vicmatskiv.pointblank.item.ThrowableItem;
import com.vicmatskiv.pointblank.item.ThrowableLike;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.StateMachine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThrowableClientState {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static final Map<PlayerSlot, ThrowableClientState> localSlotStates = new HashMap<PlayerSlot, ThrowableClientState>();
    private static final Map<UUID, ThrowableClientState> noSlotStates = new WeakHashMap<UUID, ThrowableClientState>();
    private UUID id;
    private ThrowableLike item;
    private long prepareThrowCooldownStartTime;
    private long completeThrowCooldownStartTime;
    private long drawCooldownDuration;
    private long drawCooldownStartTime;
    private long inspectCooldownStartTime;
    private long inspectCooldownDuration;
    private long throwCooldownDuration;
    private long prepareIdleCooldownDuration;
    private long prepareIdleCooldownStartTime;
    private long idleCooldownStartTime;
    private long idleCooldownDuration;
    protected long throwCooldownStartTime;
    protected long lastThrowTime;
    protected boolean isTriggerOn;
    private Map<String, GunStateListener> animationControllers = new HashMap<String, GunStateListener>();
    private List<GunStateListener> stateListeners = new ArrayList<GunStateListener>();
    private Component temporaryMessage;
    private long messageEndTime;
    private Predicate<ThrowableClientState> messagePredicate;
    private Predicate<Context> isValidGameMode = context -> context.player != null && !context.player.isSpectator();
    private State simplifiedThrowState;
    private StateMachine<State, Context> stateMachine;
    private static final Map<UUID, ThrowableClientState> statesById = new HashMap<UUID, ThrowableClientState>();

    public ThrowableClientState(UUID id, ThrowableLike item) {
        this.id = id;
        this.item = item;
        this.stateMachine = this.createStateMachine();
        statesById.put(id, this);
    }

    private StateMachine<State, Context> createStateMachine() {
        StateMachine.Builder<State, Context> builder = new StateMachine.Builder<State, Context>();
        builder.withTransition((State)((Object)List.of(State.PREPARE_IDLE, State.IDLE, State.IDLE_COOLDOWN)), State.DRAW, this.isValidGameMode, StateMachine.TransitionMode.EVENT, (StateMachine.Action<State, Context>)null, this::actionDraw);
        builder.withTransition(State.DRAW, State.DRAW_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.drawCooldownStartTime = System.nanoTime();
            this.drawCooldownDuration = this.item.getDrawCooldownDuration(ctx.player, this, ctx.itemStack) * 1000000L;
        });
        builder.withTransition(State.DRAW_COOLDOWN, State.PREPARE_IDLE, Predicate.not(this::isDrawCooldownInProgress));
        builder.withTransition(State.PREPARE_IDLE, State.IDLE, ctx -> !this.isPrepareIdleCooldownInProgress((Context)ctx) && this.item.hasIdleAnimations(), StateMachine.TransitionMode.AUTO, null, null);
        builder.withTransition(State.IDLE, State.IDLE_COOLDOWN, ctx -> this.item.hasIdleAnimations(), StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.idleCooldownStartTime = System.nanoTime();
            this.idleCooldownDuration = this.item.getIdleCooldownDuration(ctx.player, this, ctx.itemStack) * 1000000L;
        });
        builder.withTransition(State.IDLE_COOLDOWN, State.IDLE, Predicate.not(this::isIdleCooldownInProgress));
        builder.withTransition((State)((Object)List.of(State.IDLE, State.IDLE_COOLDOWN)), State.PREPARE_IDLE, ctx -> true, StateMachine.TransitionMode.EVENT, (StateMachine.Action<State, Context>)null, (StateMachine.Action<State, Context>)null);
        builder.withTransition((State)((Object)List.of(State.PREPARE_IDLE, State.IDLE, State.IDLE_COOLDOWN)), State.INSPECT, this.isValidGameMode, StateMachine.TransitionMode.EVENT, (StateMachine.Action<State, Context>)null, this::actionInspect);
        builder.withTransition(State.INSPECT, State.INSPECT_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.inspectCooldownStartTime = System.nanoTime();
            this.inspectCooldownDuration = this.item.getInspectCooldownDuration(ctx.player, this, ctx.itemStack) * 1000000L;
        });
        builder.withTransition(State.INSPECT_COOLDOWN, State.PREPARE_IDLE, Predicate.not(this::inspectCooldownInProgress));
        builder.withTransition(State.PREPARE_IDLE, State.PREPARE_THROW, this.isValidGameMode.and(context -> this.isTriggerOn && context.itemStack == ((Player)context.player).getMainHandItem()), StateMachine.TransitionMode.EVENT, null, this::actionPrepareThrow);
        builder.withTransition((State)((Object)List.of(State.PREPARE_IDLE, State.IDLE, State.IDLE_COOLDOWN)), State.PREPARE_THROW, this.isValidGameMode.and(context -> this.isTriggerOn && context.itemStack == ((Player)context.player).getMainHandItem()), StateMachine.TransitionMode.EVENT, (StateMachine.Action<State, Context>)null, this::actionPrepareThrow);
        builder.withTransition((State)((Object)List.of(State.PREPARE_IDLE, State.IDLE, State.IDLE_COOLDOWN)), State.PREPARE_THROW, this.isValidGameMode.and(context -> this.isTriggerOn && context.itemStack == ((Player)context.player).getMainHandItem()), StateMachine.TransitionMode.EVENT, (StateMachine.Action<State, Context>)null, this::actionPrepareThrow);
        builder.withTransition(State.INSPECT_COOLDOWN, State.PREPARE_THROW, context -> this.isTriggerOn && context.itemStack == ((Player)context.player).getMainHandItem(), StateMachine.TransitionMode.EVENT, null, this::actionPrepareThrow);
        builder.withTransition(State.PREPARE_THROW, State.PREPARE_THROW_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.prepareThrowCooldownStartTime = System.nanoTime();
        });
        builder.withTransition(State.PREPARE_THROW_COOLDOWN, State.THROW, context -> !this.isPrepareThrowCooldownInProgress((Context)context), StateMachine.TransitionMode.AUTO, null, this::actionThrow);
        builder.withTransition(State.THROW, State.THROW_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.throwCooldownStartTime = System.nanoTime();
            this.throwCooldownDuration = this.item.getThrowCooldownDuration(ctx.player, this, ctx.itemStack) * 1000000L;
        });
        builder.withTransition(State.THROW_COOLDOWN, State.COMPLETE_THROW, Predicate.not(this::isThrowCooldownInProgress), StateMachine.TransitionMode.AUTO, null, this::actionCompleteThrow);
        builder.withTransition(State.COMPLETE_THROW, State.COMPLETE_THROW_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.completeThrowCooldownStartTime = System.nanoTime();
        });
        builder.withTransition(State.COMPLETE_THROW_COOLDOWN, State.PREPARE_IDLE, Predicate.not(this::isCompleteFireCooldownInProgress), StateMachine.TransitionMode.AUTO, null, null);
        builder.withOnSetStateAction(State.PREPARE_IDLE, (ctx, f, t) -> this.actionPrepareIdle((Context)ctx));
        builder.withOnSetStateAction(State.IDLE, (ctx, f, t) -> this.actionFiddle((Context)ctx));
        builder.withOnChangeStateAction((ctx, f, t) -> LOGGER.info("Throwable state changed from {} to {}", (Object)f, (Object)t));
        return builder.build(State.PREPARE_IDLE);
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

    private boolean inspectCooldownInProgress(Context context) {
        return System.nanoTime() - this.inspectCooldownStartTime <= this.inspectCooldownDuration;
    }

    private boolean isCompleteFireCooldownInProgress(Context context) {
        return (double)(System.nanoTime() - this.completeThrowCooldownStartTime) <= 1000000.0 * (double)this.item.getCompleteThrowCooldownDuration(context.player, this, context.itemStack);
    }

    private boolean isThrowCooldownInProgress(Context context) {
        return System.nanoTime() - this.throwCooldownStartTime <= this.throwCooldownDuration;
    }

    private boolean isPrepareThrowCooldownInProgress(Context context) {
        return (double)(System.nanoTime() - this.prepareThrowCooldownStartTime) <= 1000000.0 * (double)this.item.getPrepareThrowCooldownDuration(context.player, this, context.itemStack);
    }

    private void actionPrepareThrow(Context context, State fromState, State toState) {
        this.item.prepareThrow(this, (Player)context.player, context.itemStack, context.targetEntity);
    }

    private void actionThrow(Context context, State fromState, State toState) {
        this.item.requestThrowFromServer(this, (Player)context.player, context.itemStack, context.targetEntity);
        this.lastThrowTime = MiscUtil.getLevel((Entity)context.player).getGameTime();
    }

    private void actionDraw(Context context, State fromState, State toState) {
        ThrowableLike throwableLike = this.item;
        if (throwableLike instanceof Drawable) {
            Drawable drawable = (Drawable)((Object)throwableLike);
            drawable.draw((Player)context.player, context.itemStack);
        }
    }

    private void actionInspect(Context context, State fromState, State toState) {
    }

    private void actionCompleteThrow(Context context, State fromState, State toState) {
        LOGGER.debug("{} Completing firing in state {}", (Object)(System.currentTimeMillis() % 100000L), (Object)toState);
    }

    private void actionPrepareIdle(Context context) {
        this.prepareIdleCooldownStartTime = System.nanoTime();
        this.prepareIdleCooldownDuration = this.item.getPrepareIdleCooldownDuration();
    }

    private void actionFiddle(Context context) {
    }

    public boolean tryThrow(LivingEntity player, ItemStack itemStack, Entity targetEntity) {
        Context context = new Context(this, player, itemStack, targetEntity);
        return this.stateMachine.setStateToAnyOf(context, List.of(State.PREPARE_THROW)) != null;
    }

    public boolean tryDraw(LivingEntity player, ItemStack itemStack) {
        Context context = new Context(this, player, itemStack);
        return this.stateMachine.setState(context, State.DRAW) != null;
    }

    public boolean tryDeactivate(LivingEntity player, ItemStack itemStack) {
        Context context = new Context(this, player, itemStack);
        return this.stateMachine.setState(context, State.PREPARE_IDLE) != null;
    }

    public boolean tryInspect(LivingEntity player, ItemStack itemStack) {
        Context context = new Context(this, player, itemStack);
        return this.stateMachine.setState(context, State.INSPECT) != null;
    }

    public UUID getId() {
        return this.id;
    }

    public void setTrigger(boolean isTriggerOn) {
        this.isTriggerOn = isTriggerOn;
    }

    public boolean isIdle() {
        State fireState = this.stateMachine.getCurrentState();
        return this.simplifiedThrowState == State.IDLE || fireState == State.PREPARE_IDLE || fireState == State.IDLE || fireState == State.IDLE_COOLDOWN;
    }

    public boolean isThrowing() {
        State fireState = this.stateMachine.getCurrentState();
        return ThrowableClientState.isThrowing(fireState) || this.simplifiedThrowState == State.THROW;
    }

    private static boolean isThrowing(State fireState) {
        return fireState == State.PREPARE_THROW || fireState == State.PREPARE_THROW_COOLDOWN || fireState == State.THROW || fireState == State.THROW_COOLDOWN || fireState == State.COMPLETE_THROW || fireState == State.COMPLETE_THROW_COOLDOWN;
    }

    public boolean isPreparingThrowing() {
        State fireState = this.stateMachine.getCurrentState();
        return fireState == State.PREPARE_THROW || fireState == State.PREPARE_THROW_COOLDOWN;
    }

    public boolean isCompletingThrowing() {
        State state = this.stateMachine.getCurrentState();
        return state == State.COMPLETE_THROW || state == State.COMPLETE_THROW_COOLDOWN;
    }

    public boolean isDrawing() {
        State state = this.stateMachine.getCurrentState();
        return state == State.DRAW || state == State.DRAW_COOLDOWN;
    }

    public boolean isInspecting() {
        State state = this.stateMachine.getCurrentState();
        return state == State.INSPECT || state == State.INSPECT_COOLDOWN;
    }

    public void updateState(LivingEntity player, ItemStack itemStack, boolean isSelected) {
        if (player != ClientUtil.getClientPlayer()) {
            return;
        }
        Context context = new Context(this, player, itemStack);
        this.stateMachine.update(context);
        State updatedSimplifiedFireState = State.getSimplifiedState(this.stateMachine.getCurrentState());
        if (updatedSimplifiedFireState != this.simplifiedThrowState) {
            this.simplifiedThrowState = updatedSimplifiedFireState;
        }
    }

    public String toString() {
        return String.format("ThrowableClientState[sid=%s,id=%s]", System.identityHashCode(this), this.id);
    }

    public static ThrowableClientState getMainHeldState() {
        Player player = ClientUtil.getClientPlayer();
        return ThrowableClientState.getMainHeldState(player);
    }

    public static ThrowableClientState getMainHeldState(Player player) {
        if (player == null) {
            return null;
        }
        ItemStack itemStack = player.getMainHandItem();
        int activeSlot = player.getInventory().selected;
        if (itemStack != null && itemStack.getItem() instanceof ThrowableItem) {
            return ThrowableClientState.getState(player, itemStack, activeSlot, false);
        }
        return null;
    }

    public static ThrowableClientState getState(Player player, ItemStack itemStack, int slotIndex, boolean isOffhand) {
        Level level = MiscUtil.getLevel((Entity)player);
        UUID stackId = ItemExtra.getItemStackId(itemStack);
        if (stackId == null) {
            return null;
        }
        if (slotIndex == -1) {
            ThrowableClientState state = null;
            for (Map.Entry<PlayerSlot, ThrowableClientState> entry : localSlotStates.entrySet()) {
                ItemStack stackAtSlotIndex;
                if (entry.getKey().isClientSide != level.isClientSide || !Objects.equals(entry.getValue().getId(), stackId)) continue;
                PlayerSlot playerSlot = entry.getKey();
                slotIndex = playerSlot.slotId;
                if (slotIndex < 0 || playerSlot.playerEntityId != player.getId() || (stackAtSlotIndex = player.getInventory().getItem(slotIndex)) == null || !(stackAtSlotIndex.getItem() instanceof ThrowableLike)) continue;
                state = entry.getValue();
                break;
            }
            if (state == null) {
                state = noSlotStates.computeIfAbsent(stackId, ((ThrowableLike)itemStack.getItem())::createState);
            }
            return state;
        }
        int adjustedSlotIndex = isOffhand ? -5 : slotIndex;
        PlayerSlot playerSlot = new PlayerSlot(player.getId(), adjustedSlotIndex, level.isClientSide);
        ThrowableClientState slotState = localSlotStates.get(playerSlot);
        if (slotState == null || !Objects.equals(slotState.getId(), stackId)) {
            slotState = ((ThrowableLike)itemStack.getItem()).createState(stackId);
            localSlotStates.put(playerSlot, slotState);
        }
        return slotState;
    }

    public static ThrowableClientState getState(UUID stateId) {
        return statesById.get(stateId);
    }

    public void setSimplifiedState(State state) {
        this.simplifiedThrowState = state;
    }

    public static enum State {
        PREPARE_IDLE,
        IDLE,
        IDLE_COOLDOWN,
        DRAW,
        DRAW_COOLDOWN,
        PREPARE_THROW,
        PREPARE_THROW_COOLDOWN,
        THROW,
        THROW_COOLDOWN,
        COMPLETE_THROW,
        COMPLETE_THROW_COOLDOWN,
        INSPECT,
        INSPECT_COOLDOWN;


        public static State getSimplifiedState(State state) {
            return switch (state.ordinal()) {
                case 3, 4 -> DRAW;
                case 5, 6, 7, 8, 9, 10 -> THROW;
                case 11, 12 -> INSPECT;
                default -> IDLE;
            };
        }
    }

    private class Context {
        LivingEntity player;
        ItemStack itemStack;
        Entity targetEntity;

        public Context(ThrowableClientState throwableClientState, LivingEntity player, ItemStack itemStack) {
            this.player = player;
            this.itemStack = itemStack;
        }

        public Context(ThrowableClientState throwableClientState, LivingEntity player, ItemStack itemStack, Entity targetEntity) {
            this.player = player;
            this.itemStack = itemStack;
            this.targetEntity = targetEntity;
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

