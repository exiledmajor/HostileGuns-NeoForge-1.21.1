/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.math.Axis
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.HumanoidModel$ArmPose
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.tooltip.TooltipComponent
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.BellBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.ButtonBlock
 *  net.minecraft.world.level.block.DoorBlock
 *  net.minecraft.world.level.block.FenceGateBlock
 *  net.minecraft.world.level.block.JukeboxBlock
 *  net.minecraft.world.level.block.LeverBlock
 *  net.minecraft.world.level.block.NoteBlock
 *  net.minecraft.world.level.block.TrapDoorBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.joml.Quaternionf
 *  org.joml.Vector2ic
 *  org.lwjgl.glfw.GLFW
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animation.AnimationController
 */
package com.vicmatskiv.pointblank.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.Enableable;
import com.vicmatskiv.pointblank.Nameable;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.attachment.Attachment;
import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.attachment.AttachmentHost;
import com.vicmatskiv.pointblank.attachment.Attachments;
import com.vicmatskiv.pointblank.client.BiDirectionalInterpolator;
import com.vicmatskiv.pointblank.client.ClientSystem;
import com.vicmatskiv.pointblank.client.FirstPersonWalkingAnimationHandler;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.GunHandSwapCoordinator;
import com.vicmatskiv.pointblank.client.GunStateTicker;
import com.vicmatskiv.pointblank.client.LockableTarget;
import com.vicmatskiv.pointblank.client.RealtimeLinearEaser;
import com.vicmatskiv.pointblank.client.SelectedMainHandQuickMoveTracker;
import com.vicmatskiv.pointblank.client.ThrowableClientState;
import com.vicmatskiv.pointblank.client.controller.AbstractProceduralAnimationController;
import com.vicmatskiv.pointblank.client.controller.GunJumpAnimationController;
import com.vicmatskiv.pointblank.client.controller.GunRandomizingAnimationController;
import com.vicmatskiv.pointblank.client.controller.GunRecoilAnimationController;
import com.vicmatskiv.pointblank.client.controller.InertiaController;
import com.vicmatskiv.pointblank.client.controller.PostPassEffectController;
import com.vicmatskiv.pointblank.client.controller.PryAnimationController;
import com.vicmatskiv.pointblank.client.controller.TimerController;
import com.vicmatskiv.pointblank.client.controller.ViewShakeAnimationController2;
import com.vicmatskiv.pointblank.client.gui.AttachmentManagerScreen;
import com.vicmatskiv.pointblank.client.gui.GunItemOverlay;
import com.vicmatskiv.pointblank.client.gui.SettingsScreen;
import com.vicmatskiv.pointblank.client.render.CrosshairRenderer;
import com.vicmatskiv.pointblank.client.render.FirstPersonHandAnchorCapture;
import com.vicmatskiv.pointblank.client.render.GunItemRenderer;
import com.vicmatskiv.pointblank.client.render.RenderUtil;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimatorCompat;
import com.vicmatskiv.pointblank.event.InputEvent;
import com.vicmatskiv.pointblank.event.LevelEvent;
import com.vicmatskiv.pointblank.event.LivingEvent;
import com.vicmatskiv.pointblank.event.RenderGuiEventPost;
import com.vicmatskiv.pointblank.event.RenderGuiOverlayEvent;
import com.vicmatskiv.pointblank.event.RenderHandEvent;
import com.vicmatskiv.pointblank.event.RenderHandsEvent;
import com.vicmatskiv.pointblank.event.RenderLevelEvent;
import com.vicmatskiv.pointblank.event.RenderLivingEvent;
import com.vicmatskiv.pointblank.event.RenderTooltipEvent;
import com.vicmatskiv.pointblank.event.SubscribeEvent2;
import com.vicmatskiv.pointblank.event.TickEvent;
import com.vicmatskiv.pointblank.event.ViewportEvent;
import com.vicmatskiv.pointblank.explosion.ExplosionEvent;
import com.vicmatskiv.pointblank.feature.AimingFeature;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.feature.PipFeature;
import com.vicmatskiv.pointblank.input.KeyModifierType;
import com.vicmatskiv.pointblank.item.ExplosionDescriptor;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.item.ItemExtra;
import com.vicmatskiv.pointblank.item.ThrowableItem;
import com.vicmatskiv.pointblank.network.AimingChangeRequestPacket;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.HitScan;
import com.vicmatskiv.pointblank.util.LazyOptional;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.UpDownCounter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;

public class ClientEventHandler {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static ClientEventHandler instance;
    public static final Supplier<KeyMapping> RELOAD_KEY;
    public static final Supplier<KeyMapping> FIRE_MODE_KEY;
    public static final Supplier<KeyMapping> INSPECT_KEY;
    public static final Supplier<KeyMapping> ATTACHMENT_KEY;
    public static final Supplier<KeyMapping> SCOPE_SWITCH_KEY;
    public static final Supplier<KeyMapping> INTERACT_KEY;
    public static final Supplier<KeyMapping> SETTINGS_KEY;
    public static final KeyModifierType SETTINGS_KEY_MODIFIER;
    private static ReentrantLock mainLoopLock;
    private static int dedicatedBlockInteractionDepth;
    private static final double HAND_BASE_FOV_DEGREES = 70.0;
    private static final int INTERACTION_PROMPT_X_OFFSET = -12;
    private static final int INTERACTION_PROMPT_Y_OFFSET = 12;
    private static final long RELOAD_BLOCKED_MESSAGE_DURATION_MILLIS = 2000L;
    private static final Component RELOAD_REQUIRES_FREE_OFFHAND_MESSAGE;
    private static final float MAIN_HAND_INERTIA_MAX_ROLL = 0.43633232f;
    private InertiaController scopeInertiaController = new InertiaController(0.06, 0.2, 0.1);
    private InertiaController inertiaController = new InertiaController(0.01, 0.1, 1.2217304706573486, 0.4363323152065277);
    public static InertiaController reticleInertiaController;
    private GunJumpAnimationController jumpController = new GunJumpAnimationController(0.3, 0.8, 1.3, 0.05, 2000L);
    private ViewShakeAnimationController2 sharedViewShakeController = new ViewShakeAnimationController2(0.15, 0.3, 1.0, 0.01, 500L);
    private int currentInventorySlot;
    private int previousInventorySlot;
    private boolean inventorySlotChanged;
    private boolean currentSlotHasGun;
    private boolean currentSlotHasGunChanged;
    private boolean isInitialSlotUpdate = true;
    private LocalPlayer trackedLocalPlayer;
    private boolean leftMouseButtonDown = false;
    private boolean rightMouseButtonDown = false;
    private boolean settingsKeyPressed = false;
    private float previousPlayerXRot;
    private float previousPlayerYRot;
    private float playerDeltaXRot;
    private float playerDeltaYRot;
    private double previousPlayerPosX;
    private double previousPlayerPosY;
    private double previousPlayerPosZ;
    private double playerDeltaX;
    private double playerDeltaY;
    private double playerDeltaZ;
    private static LivingEntity currentEntityLiving;
    private RealtimeLinearEaser bobbingValue = new RealtimeLinearEaser(200L);
    private RealtimeLinearEaser bobbingYawValue = new RealtimeLinearEaser(200L);
    private RealtimeLinearEaser zoomValue = new RealtimeLinearEaser(200L);
    private RealtimeLinearEaser baseZoomValue = new RealtimeLinearEaser(200L);
    private boolean wasPipFallbackActive = false;
    private static boolean isPipFallbackZoomActive;
    private static double gunFov;
    private RealtimeLinearEaser crossHairExp = new RealtimeLinearEaser(100L);
    private LockableTarget lockableTarget = new LockableTarget();
    private static PostPassEffectController postPassEffectController;
    private static final ResourceLocation crossHairOverlay;
    private final FirstPersonWalkingAnimationHandler firstPersonWalkingAnimationHandler;

    public static ClientEventHandler getInstance() {
        return instance;
    }

    public ClientEventHandler() {
        instance = this;
        this.firstPersonWalkingAnimationHandler = new FirstPersonWalkingAnimationHandler();
        this.startGunTicker();
    }

    private void startGunTicker() {
        GunStateTicker gunStateTicker = new GunStateTicker(this);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> gunStateTicker.shutdown()));
        gunStateTicker.start();
    }

    @SubscribeEvent2
    public void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            GunHandSwapCoordinator.getInstance().clear("world load");
            SelectedMainHandQuickMoveTracker.getInstance().clear();
            this.currentInventorySlot = -1;
            this.isInitialSlotUpdate = true;
            this.trackedLocalPlayer = null;
        }
    }

    @SubscribeEvent2
    public void onExplosion(ExplosionEvent event) {
        double distanceToPlayer = event.getLocation().distanceTo(ClientUtil.getClientPlayer().position());
        ExplosionDescriptor descriptor = event.getExplosionDescriptor();
        distanceToPlayer = Mth.clamp((double)distanceToPlayer, (double)1.0, (double)Double.MAX_VALUE);
        double lambda = 0.2;
        double adjustedPower = (double)descriptor.power() * Math.exp(-lambda * distanceToPlayer);
        this.sharedViewShakeController.reset(adjustedPower, 0.5, 1.5, 0.01);
    }

    @SubscribeEvent2
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        Player player = ClientUtil.getClientPlayer();
        if (event.getPhase() == TickEvent.Phase.START) {
            List<ActiveGunStateEntry> activeStates;
            mainLoopLock.lock();
            if (player != null && !(activeStates = this.getActiveGunStateEntries(player)).isEmpty()) {
                Minecraft mc = Minecraft.getInstance();
                float renderTickTime = MiscUtil.getRenderTickTime(event.getTimer());
                for (ActiveGunStateEntry entry : activeStates) {
                    entry.state().renderTick((LivingEntity)player, entry.context().itemStack(), renderTickTime);
                }
                if (!mc.options.getCameraType().isFirstPerson()) {
                    return;
                }
                ActiveGunStateEntry sharedEntry = ClientEventHandler.selectSharedEffectState(activeStates);
                if (sharedEntry == null) {
                    return;
                }
                GunClientState state = sharedEntry.state();
                ItemStack gunStack = sharedEntry.context().itemStack();
                GunItem gunItem = state.getGunItem();
                ItemDisplayContext itemDisplayContext = GunItem.getFirstPersonDisplayContext(player, sharedEntry.context().hand());
                this.inertiaController.onRenderTick((LivingEntity)player, state, gunStack, itemDisplayContext, renderTickTime);
                if (gunItem.getScopeOverlay() != null && state.isAiming() && state.isFiring()) {
                    this.scopeInertiaController.setDynamicModifier(0.01f);
                } else {
                    this.scopeInertiaController.setDynamicModifier(1.0f);
                }
                this.scopeInertiaController.onRenderTick((LivingEntity)player, state, gunStack, itemDisplayContext, renderTickTime);
                this.jumpController.onRenderTick((LivingEntity)player, state, gunStack, itemDisplayContext, renderTickTime);
                postPassEffectController.onRenderTick((LivingEntity)player, state, gunStack, itemDisplayContext, renderTickTime);
                this.sharedViewShakeController.onRenderTick((LivingEntity)player, state, gunStack, itemDisplayContext, renderTickTime);
            }
        } else if (event.getPhase() == TickEvent.Phase.END && mainLoopLock.isHeldByCurrentThread()) {
            mainLoopLock.unlock();
        }
    }

    public static PostPassEffectController getPostPassEffectController() {
        return postPassEffectController;
    }

    private boolean autoReloadEnabled(Player player) {
        Config.AutoReload autoReload = Config.autoReload;
        return autoReload == Config.AutoReload.ENABLED || autoReload == Config.AutoReload.CREATIVE && player.isCreative() || autoReload == Config.AutoReload.SURVIVAL && !player.isCreative();
    }

    @SubscribeEvent2
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != this.trackedLocalPlayer) {
            GunClientState.clearPlayerStates((Player)this.trackedLocalPlayer);
            GunHandSwapCoordinator.getInstance().clear("local player changed");
            SelectedMainHandQuickMoveTracker.getInstance().clear();
            this.trackedLocalPlayer = player;
            this.currentInventorySlot = -1;
            this.isInitialSlotUpdate = true;
            this.leftMouseButtonDown = false;
            this.rightMouseButtonDown = false;
        }
        if (event.getPhase() == TickEvent.Phase.START) {
            mainLoopLock.lock();
        } else if (event.getPhase() == TickEvent.Phase.END) {
            if (player != null) {
                boolean rightMouseButtonDown;
                boolean leftMouseButtonDown;
                Enableable e;
                boolean updatedSlotHasGun;
                if (!player.isAlive() || player.isRemoved()) {
                    GunHandSwapCoordinator.getInstance().clear("local player invalidated");
                }
                this.playerDeltaX = player.getX() - this.previousPlayerPosX;
                this.playerDeltaY = player.getY() - this.previousPlayerPosY;
                this.playerDeltaZ = player.getZ() - this.previousPlayerPosZ;
                this.previousPlayerPosX = player.getX();
                this.previousPlayerPosY = player.getY();
                this.previousPlayerPosZ = player.getZ();
                this.playerDeltaXRot = player.getXRot() - this.previousPlayerXRot;
                this.playerDeltaYRot = player.getYRot() - this.previousPlayerYRot;
                this.previousPlayerXRot = player.getXRot();
                this.previousPlayerYRot = player.getYRot();
                ItemStack heldItem = player.getMainHandItem();
                SelectedMainHandQuickMoveTracker.getInstance().observeSelectedSlot(player);
                GunHandSwapCoordinator swapCoordinator = GunHandSwapCoordinator.getInstance();
                GunItem.OperableGunContext operableContext = GunItem.resolveOperableGunContext((Player)player);
                ItemStack outgoingStackForChange = ItemStack.EMPTY;
                boolean sameGunIdentityForChange = false;
                boolean hideArmedForChange = false;
                int activeSlot = player.getInventory().selected;
                if (activeSlot != this.currentInventorySlot) {
                    this.inventorySlotChanged = true;
                    this.previousInventorySlot = this.currentInventorySlot;
                    if (this.previousInventorySlot >= 0 && this.previousInventorySlot < player.getInventory().getContainerSize()) {
                        outgoingStackForChange = player.getInventory().getItem(this.previousInventorySlot).copy();
                        sameGunIdentityForChange = GunHandSwapCoordinator.matchesGunIdentity(outgoingStackForChange, heldItem);
                        if (!this.isInitialSlotUpdate && !sameGunIdentityForChange) {
                            hideArmedForChange = swapCoordinator.observeAppliedSlotChange(player, this.previousInventorySlot, activeSlot, outgoingStackForChange);
                        }
                    }
                    this.currentInventorySlot = activeSlot;
                } else {
                    this.inventorySlotChanged = false;
                }
                boolean bl = updatedSlotHasGun = heldItem != null && heldItem.getItem() instanceof GunItem;
                if (updatedSlotHasGun != this.currentSlotHasGun) {
                    this.currentSlotHasGun = updatedSlotHasGun;
                    this.currentSlotHasGunChanged = true;
                } else {
                    this.currentSlotHasGunChanged = false;
                }
                Item item = heldItem.getItem();
                boolean isEnabled = !(item instanceof Enableable) || (e = (Enableable)item).isEnabled();
                boolean swapBlocksActions = swapCoordinator.blocksMainHandActions((Player)player);
                boolean dualWielding = GunItem.hasGunsInMainAndAltSlots((Player)player);
                if (dualWielding) {
                    this.toggleAiming(player, GunItem.resolveMainHandGunContext((Player)player), false);
                    this.toggleAiming(player, GunItem.resolveOffhandGunContext((Player)player), false);
                }
                if (!swapBlocksActions && this.autoReloadEnabled((Player)player) && !this.inventorySlotChanged) {
                    if (dualWielding) {
                        this.tryAutoReload(player, GunItem.resolveOffhandGunContext((Player)player));
                        this.tryAutoReload(player, GunItem.resolveMainHandGunContext((Player)player));
                    } else {
                        this.tryAutoReload(player, operableContext);
                    }
                }
                while (isEnabled && RELOAD_KEY.get().consumeClick() && !this.inventorySlotChanged) {
                    if (swapBlocksActions) continue;
                    if (dualWielding) {
                        this.tryReload(player, GunItem.resolveOffhandGunContext((Player)player), true);
                        this.tryReload(player, GunItem.resolveMainHandGunContext((Player)player), true);
                        continue;
                    }
                    this.tryReload(player, operableContext, true);
                }
                while (FIRE_MODE_KEY.get().consumeClick()) {
                    GunClientState state = swapBlocksActions ? null : GunClientState.getMainHeldState();
                    if (state == null || operableContext == null) continue;
                    state.tryChangeFireMode((LivingEntity)player, operableContext.itemStack());
                }
                while (INSPECT_KEY.get().consumeClick() && !this.inventorySlotChanged) {
                    GunClientState state;
                    GunItem.OperableGunContext inspectContext = GunItem.resolveMainHandGunContext((Player)player);
                    if (swapBlocksActions || inspectContext == null || !player.getOffhandItem().isEmpty() || (state = GunClientState.getState((Player)player, inspectContext.itemStack(), inspectContext.slotIndex(), false)) == null) continue;
                    state.tryInspect((LivingEntity)player, inspectContext.itemStack());
                }
                boolean settingsKeyTriggered = false;
                long windowHandle = mc.getWindow().getWindow();
                boolean altKeyDown = KeyModifierType.ALT.isDown();
                if (Platform.getInstance().supportsKeyModifiers()) {
                    if (Platform.getInstance().isKeyWithModifierDown(SETTINGS_KEY.get(), SETTINGS_KEY_MODIFIER)) {
                        settingsKeyTriggered = true;
                    }
                } else {
                    boolean isKeyUnbound = SETTINGS_KEY.get().isUnbound();
                    if (isKeyUnbound) {
                        if (altKeyDown && GLFW.glfwGetKey((long)windowHandle, (int)89) == 1) {
                            settingsKeyTriggered = true;
                        }
                    } else if (SETTINGS_KEY.get().isDown()) {
                        settingsKeyTriggered = true;
                    }
                }
                if (settingsKeyTriggered) {
                    if (!this.settingsKeyPressed) {
                        this.settingsKeyPressed = true;
                        this.openSettingsScreen();
                    }
                } else {
                    this.settingsKeyPressed = false;
                }
                while (!altKeyDown && ATTACHMENT_KEY.get().consumeClick()) {
                    if (swapBlocksActions || heldItem == null || !(heldItem.getItem() instanceof AttachmentHost)) continue;
                    Attachments.tryAttachmentMode((Player)player, heldItem);
                }
                this.triggerDedicatedBlockInteraction(mc, player);
                boolean bl2 = leftMouseButtonDown = mc.screen == null && mc.options.keyAttack.isDown();
                if (leftMouseButtonDown && !this.leftMouseButtonDown) {
                    this.leftMouseDown();
                } else if (!leftMouseButtonDown && this.leftMouseButtonDown) {
                    this.leftMouseButtonRelease();
                }
                this.leftMouseButtonDown = leftMouseButtonDown;
                boolean bl3 = rightMouseButtonDown = mc.screen == null && mc.options.keyUse.isDown();
                if (rightMouseButtonDown && !this.rightMouseButtonDown) {
                    this.rightMouseButtonDown();
                } else if (!rightMouseButtonDown && this.rightMouseButtonDown) {
                    this.rightMouseButtonRelease();
                }
                GunClientState state = GunClientState.getMainHeldState();
                while (SCOPE_SWITCH_KEY.get().consumeClick()) {
                    if (swapBlocksActions || heldItem == null || !(heldItem.getItem() instanceof GunItem) || state == null || !state.isAiming()) continue;
                    Attachments.tryNextAttachment((Player)player, heldItem, AttachmentCategory.SCOPE, AimingFeature.class);
                }
                if (heldItem.getItem() instanceof LockableTarget.TargetLocker) {
                    this.lockableTarget.setLocker((LockableTarget.TargetLocker)heldItem.getItem());
                } else {
                    this.lockableTarget.setLocker(null);
                }
                if (state != null && state.isAiming() && state.isIdle() && this.lockableTarget.getLockTimeTicks() > 0L) {
                    HitResult hitResult = HitScan.getNearestObjectInCrosshair((LivingEntity)player, 0.0f, 400.0, b -> false, b -> false, new ArrayList<BlockPos>());
                    if (hitResult.getType() == HitResult.Type.ENTITY) {
                        Entity hitScanEntity = ((EntityHitResult)hitResult).getEntity();
                        if (MiscUtil.isProtected(hitScanEntity) || !this.lockableTarget.tryLock(hitScanEntity)) {
                            this.lockableTarget.unlock(hitScanEntity);
                        }
                    } else {
                        this.lockableTarget.unlock(null);
                    }
                } else {
                    this.lockableTarget.unlock(null);
                }
                if (this.inventorySlotChanged) {
                    AnimationController<GeoAnimatable> walkingController;
                    this.lockableTarget.unlock(null);
                    this.inertiaController.reset((Player)player);
                    this.scopeInertiaController.reset();
                    reticleInertiaController.reset();
                    if (hideArmedForChange) {
                        this.isInitialSlotUpdate = false;
                    } else if (!sameGunIdentityForChange && this.previousInventorySlot >= 0) {
                        Object previousState;
                        Item item2;
                        ItemStack previousStack;
                        ItemStack itemStack = previousStack = outgoingStackForChange.isEmpty() ? player.getInventory().getItem(this.previousInventorySlot) : outgoingStackForChange;
                        if (previousStack != null && (item2 = previousStack.getItem()) instanceof GunItem) {
                            GunItem previousGunItem = (GunItem)item2;
                            previousState = GunClientState.getState((Player)player, previousStack, this.previousInventorySlot, false);
                            if (previousState != null) {
                                ((GunClientState)previousState).tryDeactivate((LivingEntity)player, previousStack);
                            }
                            if ((walkingController = previousGunItem.getGeoAnimationController("walking", previousStack, GunItem.getFirstPersonDisplayContext((Player)player, InteractionHand.MAIN_HAND))) != null) {
                                walkingController.tryTriggerAnimation("animation.model.standing");
                            }
                        } else if (previousStack != null && previousStack.getItem() instanceof ThrowableItem && (previousState = ThrowableClientState.getState((Player)player, previousStack, this.previousInventorySlot, false)) != null) {
                            ((ThrowableClientState)previousState).tryDeactivate((LivingEntity)player, previousStack);
                        }
                    }
                    ItemStack mainHeldItem = player.getMainHandItem();
                    boolean skipDraw = this.isInitialSlotUpdate;
                    this.isInitialSlotUpdate = false;
                    if (!hideArmedForChange && !sameGunIdentityForChange && state != null) {
                        if (!skipDraw) {
                            state.tryDraw((LivingEntity)player, mainHeldItem);
                        }
                        if (heldItem.getItem() instanceof GunItem) {
                            this.firstPersonWalkingAnimationHandler.reset((Player)player, heldItem);
                        }
                    } else if (!hideArmedForChange && !sameGunIdentityForChange && (walkingController = mainHeldItem.getItem()) instanceof ThrowableItem) {
                        ThrowableItem throwableItem = (ThrowableItem)walkingController;
                        ThrowableClientState throwableState = ThrowableClientState.getMainHeldState();
                        if (throwableState != null && !skipDraw) {
                            throwableState.tryDraw((LivingEntity)player, mainHeldItem);
                        }
                    }
                }
                if (this.inventorySlotChanged || this.currentSlotHasGunChanged) {
                    this.jumpController.reset();
                }
                this.leftMouseButtonDown = leftMouseButtonDown;
                this.rightMouseButtonDown = rightMouseButtonDown;
            } else {
                GunHandSwapCoordinator.getInstance().clear("no local player");
                SelectedMainHandQuickMoveTracker.getInstance().clear();
            }
            if (mainLoopLock.isHeldByCurrentThread()) {
                mainLoopLock.unlock();
            }
        }
    }

    private List<ActiveGunStateEntry> getActiveGunStateEntries(Player player) {
        ArrayList<ActiveGunStateEntry> result = new ArrayList<ActiveGunStateEntry>(3);
        if (player == null) {
            return result;
        }
        Set<GunClientState> seen = Collections.newSetFromMap(new IdentityHashMap());
        GunHandSwapCoordinator coordinator = GunHandSwapCoordinator.getInstance();
        ItemStack visibleMain = coordinator.visibleMainHandStack(player);
        if (visibleMain != null && visibleMain.getItem() instanceof GunItem) {
            ClientEventHandler.addActiveGunState(result, seen, player, new GunItem.OperableGunContext(visibleMain, InteractionHand.MAIN_HAND, coordinator.visibleMainHandSlot(player), false));
        } else {
            ClientEventHandler.addActiveGunState(result, seen, player, GunItem.resolveMainHandGunContext(player));
        }
        ItemStack visibleOffhand = coordinator.visibleOffhandStack(player);
        if (visibleOffhand != null && visibleOffhand.getItem() instanceof GunItem && GunItem.canBePlacedInOffhand(visibleOffhand)) {
            ClientEventHandler.addActiveGunState(result, seen, player, new GunItem.OperableGunContext(visibleOffhand, InteractionHand.OFF_HAND, 40, true));
        }
        return result;
    }

    private static void addActiveGunState(List<ActiveGunStateEntry> entries, Set<GunClientState> seen, Player player, GunItem.OperableGunContext context) {
        if (context == null) {
            return;
        }
        GunClientState state = GunClientState.getState(player, context.itemStack(), context.slotIndex(), context.offhand());
        if (state != null && seen.add(state)) {
            entries.add(new ActiveGunStateEntry(context, state));
        }
    }

    private static ActiveGunStateEntry selectSharedEffectState(List<ActiveGunStateEntry> entries) {
        GunClientState state;
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        for (ActiveGunStateEntry entry : entries) {
            state = entry.state();
            if (!state.isFiring() && !state.isPreparingFiring() && !state.isCompletingFiring()) continue;
            return entry;
        }
        for (ActiveGunStateEntry entry : entries) {
            state = entry.state();
            if (!state.isReloading() && !state.isDrawing() && !state.isHiding() && !state.isInspecting() && !state.isChangingFireMode() && !state.isOverheating()) continue;
            return entry;
        }
        for (ActiveGunStateEntry entry : entries) {
            if (entry.context().offhand()) continue;
            return entry;
        }
        return entries.get(0);
    }

    public void onMainHandSwapCompleted(LocalPlayer player, int outgoingSlot, int destinationSlot, ItemStack outgoingStack) {
        Object incomingState;
        ItemStack incomingStack;
        Item item = outgoingStack.getItem();
        if (item instanceof GunItem) {
            AnimationController<GeoAnimatable> walkingController;
            GunItem outgoingGun = (GunItem)item;
            GunClientState outgoingState = GunClientState.getState((Player)player, outgoingStack, outgoingSlot, false);
            if (outgoingState != null) {
                outgoingState.tryDeactivate((LivingEntity)player, outgoingStack);
            }
            if ((walkingController = outgoingGun.getGeoAnimationController("walking", outgoingStack, GunItem.getFirstPersonDisplayContext((Player)player, InteractionHand.MAIN_HAND))) != null) {
                walkingController.tryTriggerAnimation("animation.model.standing");
            }
        }
        if ((incomingStack = player.getMainHandItem()).getItem() instanceof GunItem) {
            incomingState = GunClientState.getState((Player)player, incomingStack, destinationSlot, false);
            if (incomingState != null) {
                ((GunClientState)incomingState).tryDraw((LivingEntity)player, incomingStack);
            }
            this.firstPersonWalkingAnimationHandler.reset((Player)player, incomingStack);
        } else if (incomingStack.getItem() instanceof ThrowableItem && (incomingState = ThrowableClientState.getState((Player)player, incomingStack, destinationSlot, false)) != null) {
            ((ThrowableClientState)incomingState).tryDraw((LivingEntity)player, incomingStack);
        }
        this.previousInventorySlot = outgoingSlot;
        this.currentInventorySlot = destinationSlot;
        this.inventorySlotChanged = false;
        this.currentSlotHasGun = incomingStack.getItem() instanceof GunItem;
        this.currentSlotHasGunChanged = false;
        this.isInitialSlotUpdate = false;
        this.jumpController.reset();
    }

    public void onMainHandSwapCanceled(LocalPlayer player, int outgoingSlot) {
        this.previousInventorySlot = outgoingSlot;
        this.currentInventorySlot = outgoingSlot;
        this.inventorySlotChanged = false;
        this.currentSlotHasGun = player.getMainHandItem().getItem() instanceof GunItem;
        this.currentSlotHasGunChanged = false;
        this.isInitialSlotUpdate = false;
        if (player.getMainHandItem().getItem() instanceof GunItem) {
            this.firstPersonWalkingAnimationHandler.reset((Player)player, player.getMainHandItem());
        }
    }

    @SubscribeEvent2
    public void onRenderLivingEvent(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> e) {
        currentEntityLiving = e.getEntity();
        if (e.getEntity() instanceof Player) {
            Player player = (Player)e.getEntity();
            ItemStack itemStack = player.getMainHandItem();
            int activeSlot = player.getInventory().selected;
            if (itemStack != null && itemStack.getItem() instanceof GunItem && !PlayerAnimatorCompat.getInstance().isEnabled()) {
                LivingEntityRenderer r;
                EntityModel model;
                GunClientState gunClientState = GunClientState.getState(player, itemStack, activeSlot, false);
                if ((Config.thirdPersonArmPoseAlwaysOn || gunClientState != null && (gunClientState.isAiming() || gunClientState.isFiring())) && (model = (r = e.getRenderer()).getModel()) instanceof PlayerModel) {
                    ((PlayerModel)model).rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
                }
            }
            Minecraft mc = Minecraft.getInstance();
            this.handlePlayerFirstPersonMovement(player, itemStack);
            if (Config.thirdPersonAnimationsEnabled) {
                PlayerAnimatorCompat.getInstance().handlePlayerThirdPersonMovement(player, mc.getTimer().getGameTimeDeltaPartialTick(true));
            } else {
                PlayerAnimatorCompat.getInstance().clearAll(player);
            }
        }
    }

    public static LivingEntity getCurrentEntityLiving() {
        return currentEntityLiving;
    }

    @SubscribeEvent2
    public void onRenderGameOverlay(RenderGuiEventPost event) {
        ItemStack offhandStack;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (mc.screen instanceof AttachmentManagerScreen) {
            return;
        }
        GuiGraphics guiGraphics = event.getGuiGraphics();
        GunItemOverlay.beginOverlayRenderFrame();
        GunItem.OperableGunContext mainContext = GunItem.resolveMainHandGunContext((Player)mc.player);
        GunItem.OperableGunContext offhandContext = GunItem.resolveOffhandGunContext((Player)mc.player);
        ItemStack mainStack = mainContext != null ? mainContext.itemStack() : ItemStack.EMPTY;
        ItemStack itemStack = offhandStack = offhandContext != null ? offhandContext.itemStack() : ItemStack.EMPTY;
        if (!mainStack.isEmpty() || !offhandStack.isEmpty()) {
            if (!mainStack.isEmpty() && !offhandStack.isEmpty()) {
                GunItemOverlay.renderGunOverlayAtPlacement(guiGraphics, offhandStack, Config.AmmoIndicatorPlacement.LEFT_OF_HOTBAR);
                GunItemOverlay.renderGunOverlayAtPlacement(guiGraphics, mainStack, Config.AmmoIndicatorPlacement.RIGHT_OF_HOTBAR);
            } else if (!mainStack.isEmpty()) {
                GunItemOverlay.renderGunOverlay2(guiGraphics, mainStack);
            } else {
                GunItemOverlay.renderGunOverlayAtPlacement(guiGraphics, offhandStack, Config.AmmoIndicatorPlacement.LEFT_OF_HOTBAR);
            }
            guiGraphics.flush();
        }
        this.renderInteractionPrompt(guiGraphics, mc);
    }

    @SubscribeEvent2
    public void onFovUpdate(ViewportEvent.ComputeFov event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.getMainHandItem().isEmpty() || minecraft.options.getCameraType() != CameraType.FIRST_PERSON) {
            isPipFallbackZoomActive = false;
            return;
        }
        ItemStack itemStack = minecraft.player.getMainHandItem();
        if (!(itemStack.getItem() instanceof GunItem)) {
            isPipFallbackZoomActive = false;
            return;
        }
        int activeSlot = minecraft.player.getInventory().selected;
        GunClientState gunClientState = GunClientState.getState((Player)minecraft.player, itemStack, activeSlot, false);
        if (gunClientState != null) {
            boolean isFallbackActive = this.isPipFallbackActive(itemStack);
            boolean wasPipFallbackActiveBefore = this.wasPipFallbackActive;
            float rawFullZoom = AimingFeature.getZoom(itemStack);
            float rawBaseZoom = AimingFeature.getBaseZoom(itemStack);
            boolean transitioningFromPipFallback = wasPipFallbackActiveBefore && !isFallbackActive;
            float fullZoom = this.zoomValue.update(rawFullZoom);
            BiDirectionalInterpolator aimingController = (BiDirectionalInterpolator)gunClientState.getAnimationController("aiming");
            float aimingProgress = (float)aimingController.getValue();
            float fullZoomAdj = fullZoom * aimingProgress;
            float baseZoom = this.baseZoomValue.update(rawBaseZoom);
            float baseZoomAdj = baseZoom * aimingProgress;
            double worldBaseFov = event.getFOV();
            double worldFov = worldBaseFov - worldBaseFov * (double)fullZoomAdj - (double)((float)this.sharedViewShakeController.getRoll());
            double handBaseFov = 70.0;
            gunFov = handBaseFov - handBaseFov * (double)baseZoomAdj - (double)((float)this.sharedViewShakeController.getRoll());
            boolean zoomStillInterpolating = Math.abs(fullZoom - rawFullZoom) > 0.01f || Math.abs(baseZoom - rawBaseZoom) > 0.01f;
            boolean hasFallbackZoomDelta = rawFullZoom > rawBaseZoom + 1.0E-4f;
            boolean aimingActive = gunClientState.isAiming() || aimingProgress > 1.0E-4f;
            isPipFallbackZoomActive = isFallbackActive && hasFallbackZoomDelta && aimingActive || transitioningFromPipFallback && zoomStillInterpolating && aimingActive;
            this.wasPipFallbackActive = isFallbackActive && aimingActive || wasPipFallbackActiveBefore && zoomStillInterpolating && aimingActive;
            event.setFOV(worldFov);
        } else {
            isPipFallbackZoomActive = false;
        }
    }

    private boolean isPipFallbackActive(ItemStack itemStack) {
        Pair<String, ItemStack> selected = Attachments.getSelectedAttachment(itemStack, AttachmentCategory.SCOPE);
        ItemStack selectedStack = selected != null ? (ItemStack)selected.getSecond() : itemStack;
        Item item = selectedStack.getItem();
        if (item instanceof FeatureProvider) {
            FeatureProvider fp = (FeatureProvider)item;
            PipFeature pipFeature = fp.getFeature(PipFeature.class);
            return pipFeature != null && pipFeature.isFallbackRequired();
        }
        return false;
    }

    public static boolean isPipFallbackZoomActive() {
        return isPipFallbackZoomActive;
    }

    public static double getGunFov() {
        return gunFov;
    }

    public float getCurrentGunBodyInertiaYaw() {
        return this.inertiaController != null ? (float)this.inertiaController.getYaw() : 0.0f;
    }

    public float getCurrentGunBodyInertiaPitch() {
        return this.inertiaController != null ? (float)this.inertiaController.getPitch() : 0.0f;
    }

    private void undoVanillaTurnBob(PoseStack poseStack, LocalPlayer player, float partialTick, float strength) {
        if (strength <= 1.0E-4f) {
            return;
        }
        float xBob = Mth.lerp((float)partialTick, (float)player.xBobO, (float)player.xBob);
        float yBob = Mth.lerp((float)partialTick, (float)player.yBobO, (float)player.yBob);
        float deltaX = (player.getViewXRot(partialTick) - xBob) * 0.1f;
        float deltaY = (player.getViewYRot(partialTick) - yBob) * 0.1f;
        float clampedStrength = Mth.clamp((float)strength, (float)0.0f, (float)1.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-deltaY * clampedStrength));
        poseStack.mulPose(Axis.XP.rotationDegrees(-deltaX * clampedStrength));
    }

    private void undoVanillaWalkBob(PoseStack poseStack, float walkDistance, float bobbing) {
        poseStack.mulPose(Axis.XP.rotationDegrees(-Math.abs(Mth.cos((float)(walkDistance * (float)Math.PI - 0.2f)) * bobbing) * 5.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-Mth.sin((float)(walkDistance * (float)Math.PI)) * bobbing * 3.0f));
        poseStack.translate(-Mth.sin((float)(walkDistance * (float)Math.PI)) * bobbing * 0.5f, Math.abs(Mth.cos((float)(walkDistance * (float)Math.PI)) * bobbing), 0.0f);
    }

    @SubscribeEvent2
    public void onRenderHandEvent(RenderHandEvent event) {
        ItemStack heldItem = event.getItemStack();
        Item item = heldItem.getItem();
        if (item instanceof GunItem) {
            int slot;
            GunItem gunItem = (GunItem)item;
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer localPlayer = minecraft.player;
            if (localPlayer == null) {
                return;
            }
            GunHandSwapCoordinator coordinator = GunHandSwapCoordinator.getInstance();
            ItemStack contextStack = event.getHand() == InteractionHand.MAIN_HAND ? coordinator.visibleMainHandStack((Player)localPlayer) : coordinator.visibleOffhandStack((Player)localPlayer);
            boolean offhand = event.getHand() == InteractionHand.OFF_HAND;
            int n = slot = offhand ? 40 : coordinator.visibleMainHandSlot((Player)localPlayer);
            if (!(contextStack.getItem() instanceof GunItem) || !GunHandSwapCoordinator.matchesGunIdentity(heldItem, contextStack)) {
                return;
            }
            GunClientState gunClientState = GunClientState.getState((Player)localPlayer, contextStack, slot, offhand);
            if (gunClientState != null) {
                GunRecoilAnimationController recoilController;
                GunRandomizingAnimationController randomizerController;
                if (((Boolean)minecraft.options.bobView().get()).booleanValue() && minecraft.getCameraEntity() instanceof Player) {
                    Player player = (Player)minecraft.getCameraEntity();
                    PoseStack poseStack = event.getPoseStack();
                    float f = player.walkDist - player.walkDistO;
                    float partialTick = event.getPartialTick();
                    float walkDistance = -(player.walkDist + f * partialTick);
                    float bobbing = Mth.lerp((float)partialTick, (float)player.oBob, (float)player.bob);
                    if (player instanceof LocalPlayer) {
                        LocalPlayer cameraLocalPlayer = (LocalPlayer)player;
                        this.undoVanillaTurnBob(poseStack, cameraLocalPlayer, partialTick, 1.0f);
                    }
                    this.undoVanillaWalkBob(poseStack, walkDistance, bobbing);
                    if (!Config.firstPersonAnimationsEnabled) {
                        float targetYaw;
                        float randomPitch = 0.0f;
                        float randomYaw = 0.0f;
                        float targetBobbing = 1.0f;
                        if (player.isSprinting()) {
                            targetYaw = 10.0f;
                            targetBobbing = gunItem.getBobbing();
                        } else if (gunClientState.isAiming()) {
                            targetBobbing = gunItem.getBobbingOnAim();
                            float afc = AimingFeature.getViewBobbing(heldItem);
                            targetYaw = 5.0f * afc;
                            targetBobbing *= afc;
                        } else {
                            targetBobbing = gunItem.getBobbing();
                            targetYaw = 5.0f;
                        }
                        float bobbingRoll = (bobbing *= this.bobbingValue.update(targetBobbing)) * 10.0f * gunItem.getBobbingRollMultiplier();
                        float bobbingPitch = bobbing * 5.0f;
                        float bobbingYaw = bobbing * this.bobbingYawValue.update(targetYaw);
                        poseStack.translate((double)(-Mth.sin((float)(walkDistance * (float)Math.PI)) * bobbing * 0.5f), (double)Math.abs(Mth.cos((float)(walkDistance * (float)Math.PI)) * bobbing) * 0.35 + (double)(randomPitch * 0.01f), (double)(randomYaw * 0.0f));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin((float)(walkDistance * (float)Math.PI)) * bobbingRoll));
                        poseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos((float)(walkDistance * (float)Math.PI - 0.2f))) * bobbingPitch));
                        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin((float)(walkDistance * (float)Math.PI)) * bobbingYaw));
                    }
                }
                FirstPersonHandAnchorCapture.capture(event.getPoseStack());
                if (Config.firstPersonAnimationsEnabled) {
                    this.handlePlayerFirstPersonMovement((Player)localPlayer, contextStack, new GunItem.OperableGunContext(contextStack, event.getHand(), slot, offhand));
                }
                if ((randomizerController = (GunRandomizingAnimationController)gunClientState.getAnimationController("randomizer")) != null) {
                    if (this.inventorySlotChanged || this.currentSlotHasGunChanged) {
                        randomizerController.reset();
                    }
                    double posX = randomizerController.getPosX();
                    double posY = randomizerController.getPosY();
                    double posZ = randomizerController.getPosZ();
                    double roll = randomizerController.getRoll();
                    double yaw = randomizerController.getYaw();
                    double pitch = randomizerController.getPitch();
                    PoseStack poseStack = event.getPoseStack();
                    Quaternionf q = new Quaternionf(pitch, yaw, roll, 1.0);
                    poseStack.translate(posX, posY, posZ);
                    poseStack.mulPose(q);
                }
                if ((recoilController = (GunRecoilAnimationController)gunClientState.getAnimationController("recoil2")) != null) {
                    if (this.inventorySlotChanged || this.currentSlotHasGunChanged) {
                        recoilController.reset();
                    }
                    double posX = recoilController.getPosX();
                    double posY = recoilController.getPosY();
                    double posZ = recoilController.getPosZ();
                    double roll = recoilController.getRoll();
                    double pitch = recoilController.getPitch();
                    PoseStack poseStack = event.getPoseStack();
                    Quaternionf q = new Quaternionf(pitch, 0.0, roll, 1.0);
                    poseStack.translate(posX, posY, posZ);
                    poseStack.mulPose(q);
                }
                if (this.jumpController != null) {
                    double jumpMultiplier = gunItem.getJumpMultiplier();
                    double posX = this.jumpController.getPosX() * jumpMultiplier;
                    double posY = this.jumpController.getPosY() * jumpMultiplier;
                    double posZ = this.jumpController.getPosZ() * jumpMultiplier;
                    double roll = this.jumpController.getRoll() * jumpMultiplier;
                    double pitch = this.jumpController.getPitch() * jumpMultiplier;
                    double yaw = this.jumpController.getYaw() * jumpMultiplier;
                    PoseStack poseStack = event.getPoseStack();
                    Quaternionf q = new Quaternionf(pitch, yaw, roll, 1.0);
                    poseStack.translate(posX, posY, posZ);
                    poseStack.mulPose(q);
                }
                if (this.inertiaController != null) {
                    double inertiaRoll = this.inertiaController.getRoll();
                    PoseStack poseStack = event.getPoseStack();
                    poseStack.mulPose(Axis.ZP.rotation((float)inertiaRoll));
                }
            }
        }
    }

    private void handlePlayerFirstPersonMovement(Player player, ItemStack itemStack) {
        this.firstPersonWalkingAnimationHandler.handlePlayerFirstPersonMovement(player, itemStack);
    }

    private void handlePlayerFirstPersonMovement(Player player, ItemStack itemStack, GunItem.OperableGunContext renderedContext) {
        this.firstPersonWalkingAnimationHandler.handlePlayerFirstPersonMovement(player, itemStack, renderedContext);
    }

    private static boolean suppressesVanillaBlockInteraction(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack heldItem = player.getMainHandItem();
        return heldItem.getItem() instanceof ItemExtra || GunItem.getMainHeldGunItemStack((LivingEntity)player) != null;
    }

    private boolean shouldSuppressVanillaAttack(Player player) {
        return GunHandSwapCoordinator.getInstance().blocksMainHandActions(player) || ClientEventHandler.suppressesVanillaBlockInteraction(player);
    }

    private static boolean usesDedicatedBlockInteraction(Player player) {
        return ClientEventHandler.suppressesVanillaBlockInteraction(player) && !INTERACT_KEY.get().isUnbound();
    }

    public static boolean shouldBlockHeldWeaponBlockInteraction(Player player) {
        return GunHandSwapCoordinator.getInstance().blocksMainHandActions(player) || dedicatedBlockInteractionDepth == 0 && ClientEventHandler.usesDedicatedBlockInteraction(player);
    }

    private static void withDedicatedBlockInteractionOverride(Runnable action) {
        ++dedicatedBlockInteractionDepth;
        try {
            action.run();
        }
        finally {
            --dedicatedBlockInteractionDepth;
        }
    }

    private void triggerDedicatedBlockInteraction(Minecraft mc, LocalPlayer player) {
        if (GunHandSwapCoordinator.getInstance().blocksMainHandActions((Player)player) || !ClientEventHandler.usesDedicatedBlockInteraction((Player)player) || mc.gameMode == null || mc.screen != null || mc.getOverlay() != null) {
            return;
        }
        while (INTERACT_KEY.get().consumeClick()) {
            HitResult hitResult = mc.hitResult;
            if (!(hitResult instanceof BlockHitResult)) continue;
            BlockHitResult blockHitResult = (BlockHitResult)hitResult;
            ClientEventHandler.withDedicatedBlockInteractionOverride(() -> mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, blockHitResult));
        }
    }

    private void renderInteractionPrompt(GuiGraphics guiGraphics, Minecraft mc) {
        BlockHitResult blockHitResult;
        if (!Config.interactionPromptEnabled || mc.player == null || mc.level == null || mc.screen != null || mc.getOverlay() != null || !mc.options.getCameraType().isFirstPerson() || INTERACT_KEY.get().isUnbound()) {
            return;
        }
        if (GunItem.getMainHeldGunItemStack((LivingEntity)mc.player) == null) {
            return;
        }
        HitResult hitResult = mc.hitResult;
        if (!(hitResult instanceof BlockHitResult) || !ClientEventHandler.isInteractableBlock((Level)mc.level, blockHitResult = (BlockHitResult)hitResult)) {
            return;
        }
        String keyName = INTERACT_KEY.get().getTranslatedKeyMessage().getString();
        MutableComponent message = Component.literal((String)("[" + keyName + "]"));
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int x = (screenWidth - mc.font.width((FormattedText)message)) / 2 + -12;
        int y = screenHeight / 2 + 12;
        guiGraphics.drawString(mc.font, (Component)message, x, y, -1, true);
    }

    private static boolean isInteractableBlock(Level level, BlockHitResult blockHitResult) {
        BlockState blockState = level.getBlockState(blockHitResult.getBlockPos());
        if (blockState.isAir()) {
            return false;
        }
        if (blockState.getMenuProvider(level, blockHitResult.getBlockPos()) != null) {
            return true;
        }
        Block block = blockState.getBlock();
        return block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock || block instanceof LeverBlock || block instanceof ButtonBlock || block instanceof NoteBlock || block instanceof BellBlock || block instanceof JukeboxBlock;
    }

    @SubscribeEvent2
    public void onClickEvent(InputEvent.InteractionKeyMappingTriggered event) {
        Player player = event.getPlayer();
        if (player == null && Platform.getInstance().isClientThread()) {
            player = ClientUtil.getClientPlayer();
        }
        if (player == null) {
            return;
        }
        if (GunHandSwapCoordinator.getInstance().blocksMainHandActions(player)) {
            event.setCanceled(true);
            return;
        }
        if (this.shouldSuppressVanillaAttack(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent2
    public void onPreRenderHandEvent(RenderHandsEvent.Pre event) {
        Player clientPlayer = ClientUtil.getClientPlayer();
        if (clientPlayer != null && (!clientPlayer.getOffhandItem().isEmpty() || GunItem.hasGunsInMainAndAltSlots(clientPlayer))) {
            return;
        }
        ItemStack itemStack = GunItem.getMainHeldGunItemStack((LivingEntity)clientPlayer);
        if (itemStack != null && itemStack.getItem() instanceof Nameable) {
            PoseStack poseStack = event.getPoseStack();
            float rotX = GunItemRenderer.getCameraBoneRotX();
            float rotY = GunItemRenderer.getCameraBoneRotY();
            float rotZ = GunItemRenderer.getCameraBoneRotZ();
            if (rotY != 0.0f) {
                poseStack.mulPose(Axis.YP.rotation(-rotY));
            }
            if (rotX != 0.0f) {
                poseStack.mulPose(Axis.XP.rotation(-rotX));
            }
            if (rotZ != 0.0f) {
                poseStack.mulPose(Axis.ZP.rotation(-rotZ));
            }
        }
    }

    @SubscribeEvent2
    public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        ItemDisplayContext cameraPerspective;
        GunClientState state;
        Minecraft mc = Minecraft.getInstance();
        Player clientPlayer = ClientUtil.getClientPlayer();
        if (!mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        List<ActiveGunStateEntry> activeGunStates = this.getActiveGunStateEntries(clientPlayer);
        ActiveGunStateEntry sharedEffectState = ClientEventHandler.selectSharedEffectState(activeGunStates);
        GunClientState gunClientState = state = sharedEffectState != null ? sharedEffectState.state() : null;
        if (!activeGunStates.isEmpty()) {
            for (ActiveGunStateEntry entry : activeGunStates) {
                TimerController reloadTimer;
                GunClientState entryState = entry.state();
                PryAnimationController shakeAnimationController = (PryAnimationController)entryState.getAnimationController("shake");
                if (shakeAnimationController != null) {
                    float yawModifier = entryState.isAiming() ? 0.1f : 1.0f;
                    float rollModifier = entryState.isAiming() ? 1.5f : 1.0f;
                    event.setRoll(event.getRoll() + (float)shakeAnimationController.getRoll() * rollModifier);
                    event.setYaw(event.getYaw() + (float)shakeAnimationController.getYaw() * yawModifier);
                }
                if ((reloadTimer = (TimerController)entryState.getAnimationController("reloadTimer")) == null || reloadTimer.isDone()) continue;
                ItemStack gunStack = entry.context().itemStack();
                for (AbstractProceduralAnimationController activeHandler : reloadTimer.getActiveHandlers(clientPlayer, entryState, gunStack)) {
                    event.setRoll(event.getRoll() + (float)activeHandler.getRoll());
                    event.setPitch(event.getPitch() + (float)activeHandler.getPitch());
                }
            }
            if (this.sharedViewShakeController != null) {
                event.setRoll(event.getRoll() + (float)this.sharedViewShakeController.getRoll() * 0.5f);
            }
            if (state != null && this.inertiaController != null) {
                double roll = this.inertiaController.getRoll();
                double yaw = this.inertiaController.getYaw();
                event.setRoll(event.getRoll() + (float)roll * 15.0f);
                event.setYaw(event.getYaw() + (float)yaw * 15.0f);
            }
        }
        ItemStack itemStack = sharedEffectState != null ? sharedEffectState.context().itemStack() : (clientPlayer != null ? GunItem.getMainHeldGunItemStack((LivingEntity)clientPlayer) : null);
        ItemDisplayContext itemDisplayContext = cameraPerspective = clientPlayer != null && sharedEffectState != null ? GunItem.getFirstPersonDisplayContext(clientPlayer, sharedEffectState.context().hand()) : null;
        if (itemStack != null && itemStack.getItem() instanceof Nameable) {
            float rotX = cameraPerspective != null ? GunItemRenderer.getCameraBoneRotX(cameraPerspective) : GunItemRenderer.getCameraBoneRotX();
            float rotY = cameraPerspective != null ? GunItemRenderer.getCameraBoneRotY(cameraPerspective) : GunItemRenderer.getCameraBoneRotY();
            float rotZ = cameraPerspective != null ? GunItemRenderer.getCameraBoneRotZ(cameraPerspective) : GunItemRenderer.getCameraBoneRotZ();
            event.setPitch(event.getPitch() - rotX * 57.295776f);
            event.setYaw(event.getYaw() - rotY * 57.295776f);
            event.setRoll(event.getRoll() - rotZ * 57.295776f);
        }
    }

    @SubscribeEvent2
    public void onRenderOverlay(RenderGuiOverlayEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        GunItem.OperableGunContext operableGunContext = GunItem.resolveOperableGunContext((Player)minecraft.player);
        if (operableGunContext == null) {
            return;
        }
        ItemStack itemStack = operableGunContext.itemStack();
        if (event.isHotbarType() && minecraft.screen instanceof AttachmentManagerScreen) {
            event.setCanceled(true);
            return;
        }
        if (!minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        GunClientState gunClientState = GunClientState.getState((Player)minecraft.player, itemStack, operableGunContext.slotIndex(), operableGunContext.offhand());
        if (gunClientState == null) {
            return;
        }
        GunItem item = (GunItem)itemStack.getItem();
        if (event.isCrosshairType()) {
            if (Config.crosshairType == Config.CrosshairType.DEFAULT && !gunClientState.isAiming() && (gunClientState.isFiring() || gunClientState.isIdle())) {
                int height;
                double scaleFactor;
                float crossHairExpansionRate = this.getCrosshairExpansionRatio((Player)minecraft.player, gunClientState);
                double originalAspectRatio = 1.0;
                int width = event.getWindow().getGuiScaledWidth();
                int renderWidth = (int)((double)width * (scaleFactor = 3.3));
                int renderHeight = (int)((double)renderWidth / originalAspectRatio);
                if (renderHeight > (height = event.getWindow().getGuiScaledHeight())) {
                    renderHeight = (int)((double)height * scaleFactor);
                    renderWidth = (int)((double)renderHeight * originalAspectRatio);
                }
                renderWidth = 80;
                renderHeight = 80;
                float posX = (float)(width - renderWidth) / 2.0f;
                float posY = (float)(height - renderHeight) / 2.0f;
                CrosshairRenderer.renderCrosshairOverlay3(event.getGuiGraphics(), event.getPartialTick(), crossHairOverlay, crossHairExpansionRate - 1.0f, posX, posY, renderWidth, renderHeight);
            }
            if (Config.crosshairType == Config.CrosshairType.DEFAULT || Config.crosshairType == Config.CrosshairType.DISABLED) {
                event.setCanceled(true);
            }
        }
        ResourceLocation scopeOverlay = item.getScopeOverlay();
        if (gunClientState.isAiming()) {
            boolean pipZoomEnabled = MiscUtil.isGreaterThanZero(gunClientState.getGunItem().getPipScopeZoom());
            if (scopeOverlay != null && !pipZoomEnabled) {
                event.setCanceled(true);
            }
            if (!pipZoomEnabled || !Config.pipScopesEnabled) {
                BiDirectionalInterpolator aimingController = (BiDirectionalInterpolator)gunClientState.getAnimationController("aiming");
                double aimingProgress = aimingController.getValue();
                if (aimingController != null && scopeOverlay != null && !gunClientState.isReloading()) {
                    this.renderTextureOverlay(item, gunClientState, event.getGuiGraphics(), event.getPartialTick(), item.getScopeOverlay(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), (float)aimingProgress);
                }
            }
        }
    }

    protected void renderTextureOverlay(GunItem gunItem, GunClientState gunClientState, GuiGraphics guiGraphics, float partialTick, ResourceLocation textureLocation, int width, int height, float alpha) {
        GunRecoilAnimationController recoilController;
        double scaleFactor = 3.3;
        int renderWidth = (int)((double)width * scaleFactor);
        double originalAspectRatio = 1.0;
        int renderHeight = (int)((double)renderWidth / originalAspectRatio);
        if (renderHeight > height) {
            renderHeight = (int)((double)height * scaleFactor);
            renderWidth = (int)((double)renderHeight * originalAspectRatio);
        }
        float posX = (float)(width - renderWidth) / 2.0f;
        float posY = (float)(height - renderHeight) / 2.0f;
        if (this.scopeInertiaController != null) {
            double yaw = this.scopeInertiaController.getYaw();
            double pitch = this.scopeInertiaController.getPitch();
            posX = (float)((double)posX - yaw * 5000.0);
            posY = (float)((double)posY - pitch * 5000.0);
        }
        if ((recoilController = (GunRecoilAnimationController)gunClientState.getAnimationController("recoil2")) != null) {
            if (this.inventorySlotChanged || this.currentSlotHasGunChanged) {
                recoilController.reset();
            }
            double posZ1 = recoilController.getPosZ();
            posY = (float)((double)posY + posZ1 * 50.0);
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, alpha);
        RenderUtil.blit(guiGraphics, textureLocation, posX, posY, -90, 0.0f, 0.0f, renderWidth, renderHeight, renderWidth, renderHeight);
        ResourceLocation targetLockOverlay = gunItem.getTargetLockOverlay();
        if (targetLockOverlay != null) {
            this.renderTargetLockOverlay(guiGraphics, partialTick, targetLockOverlay, posX, posY, renderWidth, renderHeight);
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderTargetLockOverlay(GuiGraphics guiGraphics, float partialTick, ResourceLocation targetLockonOverlay, float posX, float posY, int renderWidth, int renderHeight) {
        UpDownCounter lockCounter = this.lockableTarget.getLockCounter();
        float targetLockCount = Mth.lerp((float)partialTick, (float)lockCounter.getPreviousValue(), (float)lockCounter.getCurrentValue()) / (float)lockCounter.getMaxValue();
        float lockProgress2 = 1.0f - targetLockCount;
        float lockRatio = 0.2f;
        int halfLockWidth = (int)((float)renderWidth * lockRatio * 0.5f);
        int halfLockHeight = (int)((float)renderHeight * lockRatio * 0.5f);
        float centerX = posX + (float)renderWidth * 0.5f;
        float centerY = posY + (float)renderHeight * 0.5f;
        float xOffset = (float)halfLockWidth * 0.4f * lockProgress2;
        float yOffset = (float)halfLockHeight * 0.4f * lockProgress2;
        float minU = 0.0f;
        float minV = 0.0f;
        float maxU = 0.5f;
        float maxV = 0.5f;
        float posXStart = centerX - (float)halfLockWidth - xOffset;
        float posXEnd = centerX - xOffset;
        float posYStart = centerY - (float)halfLockHeight - yOffset;
        float posYEnd = centerY - yOffset;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart, posXEnd, posYStart, posYEnd, -90.0f, minU, maxU, minV, maxV);
        float posXStart2 = centerX + xOffset;
        float posXEnd2 = centerX + (float)halfLockWidth + xOffset;
        float posYStart2 = centerY - (float)halfLockHeight - yOffset;
        float posYEnd2 = centerY - yOffset;
        float minU2 = 0.5f;
        float minV2 = 0.0f;
        float maxU2 = 1.0f;
        float maxV2 = 0.5f;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart2, posXEnd2, posYStart2, posYEnd2, -90.0f, minU2, maxU2, minV2, maxV2);
        posXStart2 = centerX + xOffset;
        posXEnd2 = centerX + (float)halfLockWidth + xOffset;
        posYStart2 = centerY + yOffset;
        posYEnd2 = centerY + (float)halfLockHeight + yOffset;
        minU2 = 0.5f;
        minV2 = 0.5f;
        maxU2 = 1.0f;
        maxV2 = 1.0f;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart2, posXEnd2, posYStart2, posYEnd2, -90.0f, minU2, maxU2, minV2, maxV2);
        posXStart2 = centerX - (float)halfLockWidth - xOffset;
        posXEnd2 = centerX - xOffset;
        posYStart2 = centerY + yOffset;
        posYEnd2 = centerY + (float)halfLockHeight + yOffset;
        minU2 = 0.0f;
        minV2 = 0.5f;
        maxU2 = 0.5f;
        maxV2 = 1.0f;
        RenderUtil.blit(guiGraphics, targetLockonOverlay, posXStart2, posXEnd2, posYStart2, posYEnd2, -90.0f, minU2, maxU2, minV2, maxV2);
    }

    private void setTriggerOff(LocalPlayer player) {
        if (player == null) {
            return;
        }
        ItemStack heldItem = player.getMainHandItem();
        if (this.setTriggerOff(player, GunItem.resolveOperableGunContext((Player)player))) {
            return;
        }
        Item item = heldItem.getItem();
        if (item instanceof GunItem) {
            GunItem gunItem = (GunItem)item;
            if (player.isSprinting()) {
                player.setSprinting(false);
            } else {
                gunItem.setTriggerOff((Player)player, heldItem);
            }
        } else {
            item = heldItem.getItem();
            if (item instanceof ThrowableItem) {
                ThrowableItem throwableItem = (ThrowableItem)item;
                throwableItem.setTriggerOff((Player)player, heldItem);
            }
        }
    }

    private boolean setTriggerOff(LocalPlayer player, GunItem.OperableGunContext context) {
        if (player == null || context == null) {
            return false;
        }
        GunClientState state = GunClientState.getState((Player)player, context.itemStack(), context.slotIndex(), context.offhand());
        if (state == null) {
            return false;
        }
        state.setTrigger(false);
        return true;
    }

    private void leftMouseDown() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || GunHandSwapCoordinator.getInstance().blocksMainHandActions((Player)player)) {
            return;
        }
        GunItem.OperableGunContext fireContext = GunItem.hasGunsInMainAndAltSlots((Player)player) ? GunItem.resolveOffhandGunContext((Player)player) : GunItem.resolveOperableGunContext((Player)player);
        this.tryFire(player, fireContext);
    }

    private void rightMouseButtonDown() {
        GunItem gunItem;
        Item item;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || GunHandSwapCoordinator.getInstance().blocksMainHandActions((Player)player)) {
            return;
        }
        if (GunItem.hasGunsInMainAndAltSlots((Player)player)) {
            this.tryFire(player, GunItem.resolveMainHandGunContext((Player)player));
            return;
        }
        GunItem.OperableGunContext mainContext = GunItem.resolveMainHandGunContext((Player)player);
        if (mainContext != null && (item = mainContext.itemStack().getItem()) instanceof GunItem && (gunItem = (GunItem)item).isAimingEnabled()) {
            if (player.isSprinting()) {
                player.setSprinting(false);
            }
            this.toggleAiming(player, mainContext, true);
        }
    }

    private void rightMouseButtonRelease() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && GunItem.hasGunsInMainAndAltSlots((Player)player)) {
            this.setTriggerOff(player, GunItem.resolveMainHandGunContext((Player)player));
        } else if (player != null) {
            this.toggleAiming(player, GunItem.resolveMainHandGunContext((Player)player), false);
        }
    }

    private void leftMouseButtonRelease() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && GunItem.hasGunsInMainAndAltSlots((Player)player)) {
            this.setTriggerOff(player, GunItem.resolveOffhandGunContext((Player)player));
        } else {
            this.setTriggerOff(player);
        }
    }

    public void tickMainHeldGun() {
        Player player = ClientUtil.getClientPlayer();
        if (player != null) {
            List<ActiveGunStateEntry> entries = this.getActiveGunStateEntries(player);
            boolean hasMain = entries.stream().anyMatch(entry -> !entry.context().offhand());
            for (ActiveGunStateEntry entry2 : entries) {
                entry2.state().stateTick((LivingEntity)player, entry2.context().itemStack(), !entry2.context().offhand() || !hasMain);
            }
            ActiveGunStateEntry shared = ClientEventHandler.selectSharedEffectState(entries);
            if (shared != null) {
                this.scopeInertiaController.onUpdateState((LivingEntity)player, shared.state());
                reticleInertiaController.onUpdateState((LivingEntity)player, shared.state());
                this.inertiaController.onUpdateState((LivingEntity)player, shared.state());
            }
        }
    }

    public static void runSyncTick(Runnable runnable) {
        mainLoopLock.lock();
        try {
            runnable.run();
        }
        catch (Exception e) {
            LOGGER.error("Client sync tick failed: {}", (Throwable)e);
        }
        finally {
            if (mainLoopLock.isHeldByCurrentThread()) {
                mainLoopLock.unlock();
            }
        }
    }

    public static <T> T runSyncCompute(Supplier<T> resultSupplier) {
        T result;
        mainLoopLock.lock();
        try {
            result = resultSupplier.get();
        }
        catch (Exception e) {
            LOGGER.error("Run sync compute failed: {}", (Throwable)e);
            throw e;
        }
        finally {
            if (mainLoopLock.isHeldByCurrentThread()) {
                mainLoopLock.unlock();
            }
        }
        return result;
    }

    private boolean toggleAiming(LocalPlayer player, boolean isAiming) {
        return this.toggleAiming(player, GunItem.resolveMainHandGunContext((Player)player), isAiming);
    }

    private boolean toggleAiming(LocalPlayer player, GunItem.OperableGunContext context, boolean isAiming) {
        GunClientState gunClientState;
        if (player == null || context == null || !context.offhand() && GunHandSwapCoordinator.getInstance().blocksMainHandActions((Player)player) || context.offhand() && GunHandSwapCoordinator.getInstance().blocksOffhandActions((Player)player) || context.offhand() && isAiming) {
            return false;
        }
        boolean toggled = false;
        ItemStack itemStack = context.itemStack();
        if (itemStack != null && itemStack.getItem() instanceof GunItem && (gunClientState = GunClientState.getState((Player)player, itemStack, context.slotIndex(), context.offhand())) != null) {
            toggled = gunClientState.isAiming() != isAiming;
            gunClientState.setAiming(isAiming);
            if (toggled) {
                Platform.getInstance().getNetworkService().sendToServer(new AimingChangeRequestPacket(gunClientState.getId(), context.slotIndex(), isAiming));
            }
        }
        return toggled;
    }

    private boolean tryFire(LocalPlayer player) {
        return this.tryFire(player, GunItem.resolveOperableGunContext((Player)player));
    }

    private boolean tryFire(LocalPlayer player, GunItem.OperableGunContext context) {
        GunItem gunItem;
        Item item;
        ItemStack heldItemStack;
        if (GunHandSwapCoordinator.getInstance().blocksMainHandActions((Player)player)) {
            return false;
        }
        boolean result = false;
        ItemStack itemStack = heldItemStack = context != null ? context.itemStack() : ItemStack.EMPTY;
        if (heldItemStack != null && (item = heldItemStack.getItem()) instanceof GunItem && (gunItem = (GunItem)item).isEnabled()) {
            long minTargetLockTime = gunItem.getTargetLockTimeTicks();
            if (minTargetLockTime == 0L || this.lockableTarget.getLockCounter().isAtMax()) {
                result = gunItem.tryFire((Player)player, context, this.lockableTarget.getTargetEntity());
            }
        } else if (context == null && (item = player.getMainHandItem().getItem()) instanceof ThrowableItem) {
            ThrowableItem throwableItem = (ThrowableItem)item;
            heldItemStack = player.getMainHandItem();
            if (heldItemStack != null && player.getOffhandItem() != heldItemStack && throwableItem.isEnabled()) {
                result = throwableItem.tryThrow((Player)player, heldItemStack, this.lockableTarget.getTargetEntity());
            }
        }
        return result;
    }

    private void tryReload(LocalPlayer player, GunItem.OperableGunContext context, boolean showBlockedMessage) {
        GunItem gunItem;
        Item item;
        if (player == null || context == null || !((item = context.itemStack().getItem()) instanceof GunItem) || !(gunItem = (GunItem)item).isEnabled()) {
            return;
        }
        GunClientState state = GunClientState.getState((Player)player, context.itemStack(), context.slotIndex(), context.offhand());
        if (!context.offhand() && GunItem.isReloadBlockedByOccupiedOffhand((Player)player, context.itemStack())) {
            if (showBlockedMessage && state != null) {
                state.publishMessage(RELOAD_REQUIRES_FREE_OFFHAND_MESSAGE, 2000L, current -> true);
            }
            return;
        }
        gunItem.tryReload((Player)player, context);
    }

    private void tryAutoReload(LocalPlayer player, GunItem.OperableGunContext context) {
        if (context == null) {
            return;
        }
        Optional<Integer> ammo = GunItem.getClientSideAmmo((Player)player, context.itemStack(), context.slotIndex());
        if (ammo.isPresent() && ammo.get() <= 0) {
            this.tryReload(player, context, false);
        }
    }

    @SubscribeEvent2
    public void onJump(LivingEvent.LivingJumpEvent event) {
        Level level = MiscUtil.getLevel((Entity)event.getEntity());
        if (level.isClientSide && event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            ItemStack heldItem = player.getMainHandItem();
            if (player == ClientUtil.getClientPlayer() && heldItem.getItem() instanceof GunItem) {
                GunClientState state = GunClientState.getState(player, heldItem, this.currentInventorySlot, false);
                if (state != null) {
                    state.jump((LivingEntity)player, heldItem);
                }
                if (this.jumpController != null) {
                    this.jumpController.onJumping((LivingEntity)player, state, heldItem);
                }
            }
        }
    }

    @SubscribeEvent2
    public void onRenderTooltip(RenderTooltipEvent.Pre event) {
        ClientTooltipComponent component;
        ItemStack itemStack = event.getItemStack();
        if (itemStack == null || !(itemStack.getItem() instanceof AttachmentHost)) {
            return;
        }
        event.setCanceled(true);
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGraphics();
        List<ClientTooltipComponent> tooltipComponents = event.getComponents();
        int i = 0;
        int j = tooltipComponents.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent clienttooltipcomponent : tooltipComponents) {
            int k = clienttooltipcomponent.getWidth(event.getFont());
            if (k > i) {
                i = k;
            }
            j += clienttooltipcomponent.getHeight();
        }
        int i2 = i;
        int j2 = j;
        Vector2ic vector2ic = event.getTooltipPositioner().positionTooltip(guiGraphics.guiWidth(), guiGraphics.guiHeight(), event.getX(), event.getY(), i2, j2);
        int l = vector2ic.x();
        int i1 = vector2ic.y();
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        guiGraphics.drawManaged(() -> {
            int background = 0x50101010;
            int borderStart = 1342218495;
            int borderEnd = 0x50005077;
            if (!(mc.screen instanceof AttachmentManagerScreen)) {
                background = -267382768;
            }
            RenderUtil.renderTooltipBackground(guiGraphics, l, i1, i2, j2, 400, background, background, borderStart, borderEnd);
        });
        poseStack.translate(0.0f, 0.0f, 400.0f);
        int k1 = i1;
        for (int l1 = 0; l1 < tooltipComponents.size(); ++l1) {
            component = tooltipComponents.get(l1);
            component.renderText(event.getFont(), l, k1, poseStack.last().pose(), guiGraphics.bufferSource());
            k1 += component.getHeight() + (l1 == 0 ? 2 : 0);
        }
        k1 = i1;
        for (int k2 = 0; k2 < tooltipComponents.size(); ++k2) {
            component = tooltipComponents.get(k2);
            component.renderImage(event.getFont(), l, k1, guiGraphics);
            k1 += component.getHeight() + (k2 == 0 ? 2 : 0);
        }
        poseStack.popPose();
    }

    @SubscribeEvent2
    public void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack itemStack = event.getItemStack();
        Item item = itemStack.getItem();
        if (item instanceof FeatureProvider) {
            AttachmentHost attachmentHost;
            Collection<Attachment> collection;
            FeatureProvider featureProvider = (FeatureProvider)item;
            List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
            ArrayList<Object> components = new ArrayList<Object>();
            components.addAll(featureProvider.getDescriptionTooltipLines());
            components.addAll(featureProvider.getFeatureTooltipLines());
            if (item instanceof AttachmentHost && !(collection = (attachmentHost = (AttachmentHost)item).getCompatibleAttachments()).isEmpty()) {
                if (Screen.hasShiftDown()) {
                    components.addAll(attachmentHost.getCompatibleAttachmentTooltipLines(itemStack));
                } else {
                    components.add(Component.empty());
                    components.add(Component.translatable((String)"message.pointblank.holdShiftForCompatibleAttachments").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.DARK_AQUA));
                }
            }
            for (Component component : components) {
                elements.add((Either<FormattedText, TooltipComponent>)Either.left((Object)component));
            }
        }
    }

    private float getCrosshairExpansionRatio(Player player, GunClientState gunClientState) {
        float inaccuracy = 2.0f;
        float baseExpansion = 1.0f;
        float speedMultiplier = 4.0f * inaccuracy;
        float rotationMultiplier = 0.05f * inaccuracy;
        float sprintMultiplier = 1.5f * inaccuracy;
        float jumpMultiplier = 1.2f * inaccuracy;
        float sneakMultiplier = 0.8f;
        float crouchMultiplier = 0.7f;
        float fireMultiplier = 2.0f * inaccuracy;
        double horizontalSpeed = Mth.sqrt((float)((float)(this.playerDeltaX * this.playerDeltaX + this.playerDeltaY * this.playerDeltaY + this.playerDeltaZ * this.playerDeltaZ)));
        float expansionRatio = baseExpansion + (float)horizontalSpeed * speedMultiplier;
        if (player.isSprinting()) {
            expansionRatio *= sprintMultiplier;
        }
        if (!player.onGround()) {
            expansionRatio *= jumpMultiplier;
        }
        if (player.isVisuallyCrawling()) {
            expansionRatio *= sneakMultiplier;
        }
        if (player.isCrouching()) {
            expansionRatio *= crouchMultiplier;
        }
        float yawChangeRate = Math.abs(this.playerDeltaYRot);
        float pitchChangeRate = Math.abs(this.playerDeltaXRot);
        expansionRatio += yawChangeRate * rotationMultiplier;
        expansionRatio += pitchChangeRate * rotationMultiplier;
        if (gunClientState.getTotalUninterruptedShots() > 0) {
            expansionRatio *= fireMultiplier;
        }
        return Mth.clamp((float)this.crossHairExp.update(expansionRatio), (float)1.0f, (float)7.0f);
    }

    @SubscribeEvent2
    public void onRenderLevel(RenderLevelEvent.Pre event) {
        ClientSystem.getInstance().setInverseCameraMatrix(event.getInverseCameraMatrix());
    }

    private void openSettingsScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            mc.setScreen((Screen)new SettingsScreen());
        }
    }

    private PipFeature getActivePipFeature(ItemStack itemStack) {
        Item item;
        ItemStack selectedStack;
        Pair<String, ItemStack> selected = Attachments.getSelectedAttachment(itemStack, AttachmentCategory.SCOPE);
        ItemStack itemStack2 = selectedStack = selected != null ? (ItemStack)selected.getSecond() : itemStack;
        if (selectedStack != null && (item = selectedStack.getItem()) instanceof FeatureProvider) {
            FeatureProvider fp = (FeatureProvider)item;
            return fp.getFeature(PipFeature.class);
        }
        return null;
    }

    static {
        RELOAD_KEY = LazyOptional.of(() -> new KeyMapping("key.pointblack.reload", InputConstants.Type.KEYSYM, 82, "key.categories.pointblank"));
        FIRE_MODE_KEY = LazyOptional.of(() -> new KeyMapping("key.pointblack.firemode", InputConstants.Type.KEYSYM, 66, "key.categories.pointblank"));
        INSPECT_KEY = LazyOptional.of(() -> new KeyMapping("key.pointblack.inspect", InputConstants.Type.KEYSYM, 73, "key.categories.pointblank"));
        ATTACHMENT_KEY = LazyOptional.of(() -> new KeyMapping("key.pointblack.attachments", InputConstants.Type.KEYSYM, 89, "key.categories.pointblank"));
        SCOPE_SWITCH_KEY = LazyOptional.of(() -> new KeyMapping("key.pointblack.scope_switch", InputConstants.Type.KEYSYM, 86, "key.categories.pointblank"));
        INTERACT_KEY = LazyOptional.of(() -> new KeyMapping("key.pointblack.interact", InputConstants.Type.KEYSYM, 71, "key.categories.pointblank"));
        SETTINGS_KEY = LazyOptional.of(() -> Platform.getInstance().createKeyMappingWithModifier("key.pointblack.settings", 89, "key.categories.pointblank", KeyModifierType.ALT));
        SETTINGS_KEY_MODIFIER = KeyModifierType.ALT;
        mainLoopLock = new ReentrantLock();
        dedicatedBlockInteractionDepth = 0;
        RELOAD_REQUIRES_FREE_OFFHAND_MESSAGE = Component.translatable((String)"message.pointblank.reload_requires_free_offhand");
        reticleInertiaController = new InertiaController(0.005, 0.05, 1.0);
        isPipFallbackZoomActive = false;
        gunFov = 0.0;
        postPassEffectController = new PostPassEffectController(2000L);
        crossHairOverlay = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/gui/crosshair.png");
    }

    private record ActiveGunStateEntry(GunItem.OperableGunContext context, GunClientState state) {
    }
}

