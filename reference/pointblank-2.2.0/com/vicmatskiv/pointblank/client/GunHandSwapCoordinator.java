/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animation.AnimationController
 *  software.bernie.geckolib.animation.AnimationController$State
 */
package com.vicmatskiv.pointblank.client;

import com.vicmatskiv.pointblank.Constants;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.ClientEventHandler;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.item.ItemExtra;
import com.vicmatskiv.pointblank.network.AimingChangeRequestPacket;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;

public final class GunHandSwapCoordinator {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static final long STALE_HIDE_GRACE_NANOS = 2000000000L;
    private static final long STALE_WAITING_NANOS = 10000000000L;
    private static final GunHandSwapCoordinator INSTANCE = new GunHandSwapCoordinator(System::nanoTime);
    private final NanoClock clock;
    private SwapSession activeSession;
    private DualSwapSession dualSwapSession;
    private RendererHandoff rendererHandoff;

    GunHandSwapCoordinator(NanoClock clock) {
        this.clock = clock;
    }

    public static GunHandSwapCoordinator getInstance() {
        return INSTANCE;
    }

    public synchronized boolean tryQueueDeferredMainHandSlotChange(LocalPlayer player, int targetSlot) {
        if (!GunHandSwapCoordinator.isUsablePlayer((Player)player) || !Inventory.isHotbarSlot((int)targetSlot)) {
            return false;
        }
        SwapSession session = this.getSession((Player)player);
        if (session != null) {
            if (targetSlot == session.outgoingSlot) {
                this.cancelToOutgoing(player, session);
            } else {
                session.destinationSlot = targetSlot;
                session.deferredSelection = true;
                session.lastProgressNanos = this.clock.nanoTime();
            }
            return true;
        }
        int outgoingSlot = player.getInventory().selected;
        if (targetSlot == outgoingSlot) {
            return false;
        }
        ItemStack outgoingStack = player.getMainHandItem();
        if (!(outgoingStack.getItem() instanceof GunItem)) {
            return false;
        }
        if (GunHandSwapCoordinator.matchesGunIdentity(outgoingStack, player.getInventory().getItem(targetSlot))) {
            return false;
        }
        GunClientState outgoingState = GunClientState.getState((Player)player, outgoingStack, outgoingSlot, false);
        if (outgoingState == null) {
            return false;
        }
        this.prepareOutgoingState(player, outgoingStack, outgoingSlot, outgoingState);
        long now = this.clock.nanoTime();
        this.activeSession = new SwapSession(player, outgoingSlot, outgoingStack, targetSlot, true, SwapStage.WAITING_FOR_COMMITTED_ACTION, now, outgoingState);
        boolean accepted = outgoingState.requestHideForSwap((LivingEntity)player, outgoingStack);
        if (outgoingState.isHiding() || outgoingState.isHidden()) {
            this.beginHide(this.activeSession, outgoingState, now);
        } else if (!accepted && !GunHandSwapCoordinator.isCommittedAction(outgoingState)) {
            this.activeSession = null;
            return false;
        }
        GunHandSwapCoordinator.logSessionStarted(this.activeSession, outgoingState, accepted, "input-deferred");
        return true;
    }

    public synchronized boolean tryQueueDeferredOffhandSwap(LocalPlayer player, Runnable sendSwapPacket) {
        GunClientState offhandState;
        if (!GunHandSwapCoordinator.isUsablePlayer((Player)player) || sendSwapPacket == null) {
            return false;
        }
        if (this.getDualSession((Player)player) != null || this.getSession((Player)player) != null) {
            return true;
        }
        ItemStack mainStack = player.getMainHandItem();
        ItemStack offhandStack = player.getOffhandItem();
        boolean mainGun = mainStack.getItem() instanceof GunItem;
        boolean offhandGun = offhandStack.getItem() instanceof GunItem;
        if (!mainGun && !offhandGun) {
            return false;
        }
        if (mainGun && !GunItem.canBePlacedInOffhand(mainStack)) {
            return true;
        }
        int selectedSlot = player.getInventory().selected;
        GunClientState mainState = mainGun ? GunClientState.getState((Player)player, mainStack, selectedSlot, false) : null;
        GunClientState gunClientState = offhandState = offhandGun ? GunClientState.getState((Player)player, offhandStack, 40, true) : null;
        if (mainState != null) {
            this.prepareOutgoingState(player, mainStack, selectedSlot, mainState);
            mainState.requestHideForSwap((LivingEntity)player, mainStack);
        }
        if (offhandState != null) {
            this.prepareOutgoingState(player, offhandStack, 40, offhandState);
            offhandState.requestHideForSwap((LivingEntity)player, offhandStack);
        }
        long now = this.clock.nanoTime();
        DualSwapStage initialStage = GunHandSwapCoordinator.areDualStatesHidingOrHidden(mainState, offhandState) ? DualSwapStage.HIDING : DualSwapStage.WAITING_FOR_COMMITTED_ACTION;
        this.dualSwapSession = new DualSwapSession(player, mainStack, offhandStack, mainState, offhandState, sendSwapPacket, initialStage, now);
        if (initialStage == DualSwapStage.HIDING) {
            this.beginDualHide(this.dualSwapSession, now);
        }
        return true;
    }

    public synchronized boolean observeAppliedSlotChange(LocalPlayer player, int previousSlot, int selectedSlot, ItemStack outgoingStack) {
        SwapStage stage;
        if (!(GunHandSwapCoordinator.isUsablePlayer((Player)player) && Inventory.isHotbarSlot((int)previousSlot) && Inventory.isHotbarSlot((int)selectedSlot) && previousSlot != selectedSlot)) {
            return false;
        }
        SwapSession existing = this.getSession((Player)player);
        if (existing != null) {
            if (selectedSlot == existing.outgoingSlot) {
                this.cancelToOutgoing(player, existing);
            } else {
                existing.destinationSlot = selectedSlot;
                existing.lastProgressNanos = this.clock.nanoTime();
            }
            return true;
        }
        if (!(outgoingStack.getItem() instanceof GunItem) || GunHandSwapCoordinator.matchesGunIdentity(outgoingStack, player.getMainHandItem())) {
            return false;
        }
        GunClientState outgoingState = GunClientState.getState((Player)player, outgoingStack, previousSlot, false);
        if (outgoingState == null) {
            return false;
        }
        this.prepareOutgoingState(player, outgoingStack, previousSlot, outgoingState);
        boolean accepted = outgoingState.requestHideForSwap((LivingEntity)player, outgoingStack);
        if (outgoingState.isHiding() || outgoingState.isHidden()) {
            stage = SwapStage.HIDING;
        } else if (accepted || GunHandSwapCoordinator.isCommittedAction(outgoingState)) {
            stage = SwapStage.WAITING_FOR_COMMITTED_ACTION;
        } else {
            return false;
        }
        long now = this.clock.nanoTime();
        this.activeSession = new SwapSession(player, previousSlot, outgoingStack, selectedSlot, false, stage, now, outgoingState);
        if (stage == SwapStage.HIDING) {
            this.beginHide(this.activeSession, outgoingState, now);
        }
        GunHandSwapCoordinator.logSessionStarted(this.activeSession, outgoingState, accepted, "applied-observer");
        return true;
    }

    public synchronized boolean blocksMainHandActions(Player player) {
        return this.getSession(player) != null || this.getDualSession(player) != null;
    }

    public synchronized boolean blocksOffhandActions(Player player) {
        return this.getDualSession(player) != null;
    }

    public synchronized boolean ownsMainHandVisual(Player player) {
        return this.getSession(player) != null || this.getDualSession(player) != null;
    }

    public synchronized ItemStack visibleMainHandStack(Player player) {
        SwapSession session = this.getSession(player);
        DualSwapSession dualSession = this.getDualSession(player);
        return session != null ? session.outgoingStack : (dualSession != null ? dualSession.outgoingMainStack : (player != null ? player.getMainHandItem() : ItemStack.EMPTY));
    }

    public synchronized ItemStack visibleOffhandStack(Player player) {
        DualSwapSession session = this.getDualSession(player);
        return session != null ? session.outgoingOffhandStack : (player != null ? player.getOffhandItem() : ItemStack.EMPTY);
    }

    public synchronized int visibleMainHandSlot(Player player) {
        SwapSession session = this.getSession(player);
        return session != null ? session.outgoingSlot : (player != null ? player.getInventory().selected : -1);
    }

    public synchronized GunClientState resolveOutgoingState(Player player) {
        SwapSession session = this.getSession(player);
        return session != null ? session.outgoingState : null;
    }

    public synchronized boolean advance(LocalPlayer player) {
        boolean hideStateComplete;
        if (this.advanceDualSwap(player)) {
            return true;
        }
        SwapSession session = this.getSession((Player)player);
        if (session == null) {
            return false;
        }
        long now = this.clock.nanoTime();
        GunClientState outgoingState = this.resolveOutgoingState((Player)player);
        if (!GunHandSwapCoordinator.isUsablePlayer((Player)player)) {
            this.clear("local player invalidated during swap");
            return true;
        }
        if (!GunHandSwapCoordinator.outgoingStillExists((Player)player, session)) {
            this.complete(player, session, outgoingState, "outgoing stack lost");
            return true;
        }
        if (outgoingState != null && outgoingState.getFireState() != session.lastFireState) {
            session.lastFireState = outgoingState.getFireState();
            session.lastProgressNanos = now;
        }
        if (session.stage == SwapStage.WAITING_FOR_COMMITTED_ACTION) {
            if (outgoingState == null) {
                this.complete(player, session, null, "outgoing state lost");
                return true;
            }
            if (outgoingState.isHiding() || outgoingState.isHidden()) {
                this.beginHide(session, outgoingState, now);
            } else {
                if (GunHandSwapCoordinator.isCommittedAction(outgoingState) || outgoingState.hasPendingHideAfterReload()) {
                    if (now - session.lastProgressNanos > 10000000000L) {
                        this.complete(player, session, outgoingState, "committed action timed out");
                        return true;
                    }
                    return false;
                }
                if (outgoingState.requestHideForSwap((LivingEntity)player, session.outgoingStack) && (outgoingState.isHiding() || outgoingState.isHidden())) {
                    this.beginHide(session, outgoingState, now);
                } else {
                    this.complete(player, session, outgoingState, "hide request rejected");
                    return true;
                }
            }
        }
        if (session.stage != SwapStage.HIDING || now < session.hideDeadlineNanos) {
            return false;
        }
        boolean bl = hideStateComplete = outgoingState != null && outgoingState.getFireState() == GunClientState.FireState.HIDDEN;
        if (!hideStateComplete) {
            if (now - session.hideDeadlineNanos <= 2000000000L) {
                return false;
            }
            this.complete(player, session, outgoingState, "hide state timed out");
            return true;
        }
        this.complete(player, session, outgoingState, "hide complete");
        return true;
    }

    public synchronized RendererHandoff consumeRendererHandoff(Player player) {
        RendererHandoff result = this.rendererHandoff;
        this.rendererHandoff = null;
        return result;
    }

    public synchronized void clear(String reason) {
        SwapSession session = this.activeSession;
        DualSwapSession dualSession = this.dualSwapSession;
        this.activeSession = null;
        this.dualSwapSession = null;
        this.rendererHandoff = null;
        if (session != null) {
            GunClientState state = session.outgoingState;
            if (state != null) {
                state.cancelHideForSwap();
                state.tryDeactivate((LivingEntity)session.player, session.outgoingStack);
            }
            LOGGER.debug("Cleared main-hand swap: {}", (Object)reason);
        }
        if (dualSession != null) {
            GunHandSwapCoordinator.cancelDualState(dualSession.outgoingMainState, dualSession.player, dualSession.outgoingMainStack);
            GunHandSwapCoordinator.cancelDualState(dualSession.outgoingOffhandState, dualSession.player, dualSession.outgoingOffhandStack);
            LOGGER.debug("Cleared dual-hand swap: {}", (Object)reason);
        }
    }

    private boolean advanceDualSwap(LocalPlayer player) {
        boolean swapped;
        DualSwapSession session = this.getDualSession((Player)player);
        if (session == null) {
            return false;
        }
        if (!GunHandSwapCoordinator.isUsablePlayer((Player)player)) {
            this.clear("local player invalidated during offhand swap");
            return true;
        }
        long now = this.clock.nanoTime();
        GunHandSwapCoordinator.trackDualProgress(session, now);
        if (session.stage == DualSwapStage.WAITING_FOR_COMMITTED_ACTION) {
            boolean mainReady = GunHandSwapCoordinator.prepareDualStateForHide(session.outgoingMainState, player, session.outgoingMainStack);
            boolean offhandReady = GunHandSwapCoordinator.prepareDualStateForHide(session.outgoingOffhandState, player, session.outgoingOffhandStack);
            if (mainReady && offhandReady) {
                this.beginDualHide(session, now);
            } else if (now - session.lastProgressNanos > 10000000000L) {
                this.sendDualSwapPacket(session, now, "committed action timed out");
            } else {
                return false;
            }
        }
        if (session.stage == DualSwapStage.HIDING) {
            if (now < session.deadlineNanos) {
                return false;
            }
            if (!GunHandSwapCoordinator.areDualStatesHidden(session.outgoingMainState, session.outgoingOffhandState) && now - session.deadlineNanos <= 2000000000L) {
                return false;
            }
            this.sendDualSwapPacket(session, now, GunHandSwapCoordinator.areDualStatesHidden(session.outgoingMainState, session.outgoingOffhandState) ? "hide complete" : "hide state timed out");
            return false;
        }
        boolean bl = swapped = Objects.equals(session.outgoingOffhandId, GunItem.getItemStackId(player.getMainHandItem())) && Objects.equals(session.outgoingMainId, GunItem.getItemStackId(player.getOffhandItem()));
        if (!swapped && now - session.packetSentNanos < 2000000000L) {
            return false;
        }
        this.dualSwapSession = null;
        GunHandSwapCoordinator.cancelDualState(session.outgoingMainState, player, session.outgoingMainStack);
        GunHandSwapCoordinator.cancelDualState(session.outgoingOffhandState, player, session.outgoingOffhandStack);
        if (swapped) {
            GunHandSwapCoordinator.drawCurrentContext(player, GunItem.resolveMainHandGunContext((Player)player));
            GunHandSwapCoordinator.drawCurrentContext(player, GunItem.resolveOffhandGunContext((Player)player));
        }
        this.rendererHandoff = new RendererHandoff(player.getMainHandItem(), true);
        return true;
    }

    private void beginDualHide(DualSwapSession session, long now) {
        session.stage = DualSwapStage.HIDING;
        long durationMillis = Math.max(GunHandSwapCoordinator.getHideDurationMillis(session.outgoingMainState), GunHandSwapCoordinator.getHideDurationMillis(session.outgoingOffhandState));
        session.deadlineNanos = now + durationMillis * 1000000L;
        session.lastProgressNanos = now;
    }

    private void sendDualSwapPacket(DualSwapSession session, long now, String reason) {
        session.sendSwapPacket.run();
        session.stage = DualSwapStage.AWAITING_SERVER_SWAP;
        session.packetSentNanos = now;
        LOGGER.debug("Sent deferred offhand swap packet: {}", (Object)reason);
    }

    private static long getHideDurationMillis(GunClientState state) {
        return state != null ? Math.max(0L, state.getResolvedHideDurationMillis()) : 0L;
    }

    private static boolean prepareDualStateForHide(GunClientState state, LocalPlayer player, ItemStack stack) {
        if (state == null || state.isHiding() || state.isHidden()) {
            return true;
        }
        if (GunHandSwapCoordinator.isCommittedAction(state) || state.hasPendingHideAfterReload()) {
            return false;
        }
        state.requestHideForSwap((LivingEntity)player, stack);
        return state.isHiding() || state.isHidden();
    }

    private static boolean areDualStatesHidingOrHidden(GunClientState mainState, GunClientState offhandState) {
        return GunHandSwapCoordinator.isHidingOrHidden(mainState) && GunHandSwapCoordinator.isHidingOrHidden(offhandState);
    }

    private static boolean areDualStatesHidden(GunClientState mainState, GunClientState offhandState) {
        return !(mainState != null && !mainState.isHidden() || offhandState != null && !offhandState.isHidden());
    }

    private static boolean isHidingOrHidden(GunClientState state) {
        return state == null || state.isHiding() || state.isHidden();
    }

    private static void trackDualProgress(DualSwapSession session, long now) {
        GunClientState.FireState offhandState;
        GunClientState.FireState mainState = session.outgoingMainState != null ? session.outgoingMainState.getFireState() : null;
        GunClientState.FireState fireState = offhandState = session.outgoingOffhandState != null ? session.outgoingOffhandState.getFireState() : null;
        if (mainState != session.lastMainFireState || offhandState != session.lastOffhandFireState) {
            session.lastMainFireState = mainState;
            session.lastOffhandFireState = offhandState;
            session.lastProgressNanos = now;
        }
    }

    private static void cancelDualState(GunClientState state, LocalPlayer player, ItemStack stack) {
        if (state != null) {
            state.cancelHideForSwap();
            state.tryDeactivate((LivingEntity)player, stack);
        }
    }

    private static void drawCurrentContext(LocalPlayer player, GunItem.OperableGunContext context) {
        if (context == null) {
            return;
        }
        GunClientState state = GunClientState.getState((Player)player, context.itemStack(), context.slotIndex(), context.offhand());
        if (state != null) {
            state.tryDraw((LivingEntity)player, context.itemStack());
        }
    }

    private void beginHide(SwapSession session, GunClientState state, long now) {
        session.stage = SwapStage.HIDING;
        session.hideStartNanos = now;
        session.hideDeadlineNanos = now + Math.max(0L, state.getResolvedHideDurationMillis()) * 1000000L;
        session.lastProgressNanos = now;
        if (Constants.DEBUG_HIDE_ANIMATION) {
            LOGGER.info("[HideSwapTrace] begin-hide gun={} uuid={} state={} animation={} durationMs={}", (Object)session.outgoingStack.getDescriptionId(), (Object)session.outgoingGunUuid, (Object)state.getFireState(), (Object)state.getResolvedHideAnimationName(), (Object)state.getResolvedHideDurationMillis());
        }
    }

    private void complete(LocalPlayer player, SwapSession session, GunClientState outgoingState, String reason) {
        long elapsedMillis;
        int destination = Inventory.isHotbarSlot((int)session.destinationSlot) ? session.destinationSlot : player.getInventory().selected;
        GunClientState.FireState completedFireState = outgoingState != null ? outgoingState.getFireState() : null;
        boolean completedHidden = outgoingState != null && outgoingState.getFireState() == GunClientState.FireState.HIDDEN;
        AnimationController.State completedControllerState = Constants.DEBUG_HIDE_ANIMATION ? GunHandSwapCoordinator.getHideControllerState(session) : null;
        this.activeSession = null;
        if (session.deferredSelection || player.getInventory().selected != destination) {
            player.getInventory().selected = destination;
        }
        if (outgoingState != null) {
            outgoingState.cancelHideForSwap();
            outgoingState.tryDeactivate((LivingEntity)player, session.outgoingStack);
        }
        ItemStack incoming = player.getMainHandItem();
        this.rendererHandoff = new RendererHandoff(incoming, incoming.getItem() instanceof ItemExtra);
        ClientEventHandler handler = ClientEventHandler.getInstance();
        if (handler != null) {
            handler.onMainHandSwapCompleted(player, session.outgoingSlot, destination, session.outgoingStack);
        }
        long l = elapsedMillis = session.hideStartNanos > 0L ? Math.max(0L, (this.clock.nanoTime() - session.hideStartNanos) / 1000000L) : -1L;
        if (Constants.DEBUG_HIDE_ANIMATION) {
            LOGGER.info("[HideSwapTrace] complete source={} destination={} reason={} elapsedMs={} state={} hidden={} controllerState={}", (Object)session.outgoingSlot, (Object)destination, (Object)reason, (Object)elapsedMillis, (Object)completedFireState, (Object)completedHidden, (Object)completedControllerState);
        }
    }

    private static void logSessionStarted(SwapSession session, GunClientState state, boolean accepted, String source) {
        if (!Constants.DEBUG_HIDE_ANIMATION) {
            return;
        }
        LOGGER.info("[HideSwapTrace] session source={} gun={} uuid={} slots={}->{} deferred={} accepted={} stage={} state={}", (Object)source, (Object)session.outgoingStack.getDescriptionId(), (Object)session.outgoingGunUuid, (Object)session.outgoingSlot, (Object)session.destinationSlot, (Object)session.deferredSelection, (Object)accepted, (Object)session.stage, (Object)(state != null ? state.getFireState() : null));
    }

    private void cancelToOutgoing(LocalPlayer player, SwapSession session) {
        GunClientState outgoingState = this.resolveOutgoingState((Player)player);
        this.activeSession = null;
        player.getInventory().selected = session.outgoingSlot;
        if (outgoingState != null) {
            outgoingState.cancelHideForSwap();
            if (outgoingState.isHiding() || outgoingState.isHidden()) {
                outgoingState.tryDraw((LivingEntity)player, session.outgoingStack);
            }
        }
        this.rendererHandoff = new RendererHandoff(session.outgoingStack, true);
        ClientEventHandler handler = ClientEventHandler.getInstance();
        if (handler != null) {
            handler.onMainHandSwapCanceled(player, session.outgoingSlot);
        }
    }

    private void prepareOutgoingState(LocalPlayer player, ItemStack outgoingStack, int outgoingSlot, GunClientState outgoingState) {
        if (outgoingState == null) {
            return;
        }
        outgoingState.setTrigger(false);
        if (outgoingState.isAiming()) {
            outgoingState.setAiming(false);
            Platform.getInstance().getNetworkService().sendToServer(new AimingChangeRequestPacket(outgoingState.getId(), outgoingSlot, false));
        }
    }

    private SwapSession getSession(Player player) {
        SwapSession session = this.activeSession;
        if (session == null || player == null || session.player != player || !Objects.equals(session.playerUuid, player.getUUID())) {
            return null;
        }
        return session;
    }

    private DualSwapSession getDualSession(Player player) {
        DualSwapSession session = this.dualSwapSession;
        if (session == null || player == null || session.player != player || !Objects.equals(session.playerUuid, player.getUUID())) {
            return null;
        }
        return session;
    }

    private static boolean isUsablePlayer(Player player) {
        return player != null && player.isAlive() && !player.isRemoved();
    }

    private static boolean outgoingStillExists(Player player, SwapSession session) {
        if (session.outgoingGunUuid == null) {
            return false;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            if (!Objects.equals(session.outgoingGunUuid, GunItem.getItemStackId(player.getInventory().getItem(i)))) continue;
            return true;
        }
        return false;
    }

    private static boolean isCommittedAction(GunClientState state) {
        if (state == null) {
            return false;
        }
        return state.isFiring() || GunHandSwapCoordinator.isCommittedReload(state.getFireState());
    }

    private static boolean isCommittedReload(GunClientState.FireState state) {
        return switch (state) {
            case GunClientState.FireState.RELOAD, GunClientState.FireState.RELOAD_COOLDOWN, GunClientState.FireState.RELOAD_ITER, GunClientState.FireState.RELOAD_COOLDOWN_ITER, GunClientState.FireState.COMPLETE_RELOAD, GunClientState.FireState.COMPLETE_RELOAD_COOLDOWN -> true;
            default -> false;
        };
    }

    private static AnimationController.State getHideControllerState(SwapSession session) {
        Item item = session.outgoingStack.getItem();
        if (!(item instanceof GunItem)) {
            return null;
        }
        GunItem gunItem = (GunItem)item;
        AnimationController<GeoAnimatable> controller = gunItem.getGeoAnimationController("hide_controller", session.outgoingStack, GunItem.getFirstPersonDisplayContext((Player)session.player, InteractionHand.MAIN_HAND));
        return controller != null ? controller.getAnimationState() : null;
    }

    public static boolean matchesGunIdentity(ItemStack first, ItemStack second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null || first.isEmpty() || second.isEmpty()) {
            return false;
        }
        UUID firstId = GunItem.getItemStackId(first);
        UUID secondId = GunItem.getItemStackId(second);
        return firstId != null && secondId != null && Objects.equals(firstId, secondId);
    }

    @FunctionalInterface
    static interface NanoClock {
        public long nanoTime();
    }

    private static final class SwapSession {
        private final LocalPlayer player;
        private final UUID playerUuid;
        private final int outgoingSlot;
        private final ItemStack outgoingStack;
        private final UUID outgoingGunUuid;
        private final GunClientState outgoingState;
        private boolean deferredSelection;
        private int destinationSlot;
        private SwapStage stage;
        private long hideStartNanos;
        private long hideDeadlineNanos;
        private long lastProgressNanos;
        private GunClientState.FireState lastFireState;

        private SwapSession(LocalPlayer player, int outgoingSlot, ItemStack outgoingStack, int destinationSlot, boolean deferredSelection, SwapStage stage, long now, GunClientState outgoingState) {
            this.player = player;
            this.playerUuid = player.getUUID();
            this.outgoingSlot = outgoingSlot;
            this.outgoingStack = outgoingStack.copy();
            this.outgoingGunUuid = GunItem.getItemStackId(outgoingStack);
            this.outgoingState = outgoingState;
            this.destinationSlot = destinationSlot;
            this.deferredSelection = deferredSelection;
            this.stage = stage;
            this.lastProgressNanos = now;
            this.lastFireState = outgoingState.getFireState();
        }
    }

    static enum SwapStage {
        WAITING_FOR_COMMITTED_ACTION,
        HIDING;

    }

    private static final class DualSwapSession {
        private final LocalPlayer player;
        private final UUID playerUuid;
        private final ItemStack outgoingMainStack;
        private final ItemStack outgoingOffhandStack;
        private final UUID outgoingMainId;
        private final UUID outgoingOffhandId;
        private final GunClientState outgoingMainState;
        private final GunClientState outgoingOffhandState;
        private final Runnable sendSwapPacket;
        private DualSwapStage stage;
        private long deadlineNanos;
        private long packetSentNanos;
        private long lastProgressNanos;
        private GunClientState.FireState lastMainFireState;
        private GunClientState.FireState lastOffhandFireState;

        private DualSwapSession(LocalPlayer player, ItemStack mainStack, ItemStack offhandStack, GunClientState mainState, GunClientState offhandState, Runnable sendSwapPacket, DualSwapStage stage, long now) {
            this.player = player;
            this.playerUuid = player.getUUID();
            this.outgoingMainStack = mainStack.copy();
            this.outgoingOffhandStack = offhandStack.copy();
            this.outgoingMainId = GunItem.getItemStackId(mainStack);
            this.outgoingOffhandId = GunItem.getItemStackId(offhandStack);
            this.outgoingMainState = mainState;
            this.outgoingOffhandState = offhandState;
            this.sendSwapPacket = sendSwapPacket;
            this.stage = stage;
            this.lastProgressNanos = now;
            this.lastMainFireState = mainState != null ? mainState.getFireState() : null;
            this.lastOffhandFireState = offhandState != null ? offhandState.getFireState() : null;
        }
    }

    static enum DualSwapStage {
        WAITING_FOR_COMMITTED_ACTION,
        HIDING,
        AWAITING_SERVER_SWAP;

    }

    public record RendererHandoff(ItemStack incomingStack, boolean keepFullHeight) {
    }
}

