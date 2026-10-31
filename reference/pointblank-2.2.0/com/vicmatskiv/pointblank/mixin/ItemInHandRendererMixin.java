/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.ItemInHandRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.vicmatskiv.pointblank.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.GunHandSwapCoordinator;
import com.vicmatskiv.pointblank.client.render.FirstPersonHandAnchorCapture;
import com.vicmatskiv.pointblank.event.RenderHandEvent;
import com.vicmatskiv.pointblank.event.RenderHandsEvent;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.item.ItemExtra;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemInHandRenderer.class})
public class ItemInHandRendererMixin {
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private ItemStack offHandItem;
    @Shadow
    private float offHandHeight;
    @Shadow
    private float oOffHandHeight;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void pointblank$clearMainHandSwapOnRendererInitialization(CallbackInfo ci) {
        GunHandSwapCoordinator.getInstance().clear("item-in-hand renderer initialized");
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void pointblank$pinOutgoingMainHandAtTickHead(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            this.pointblank$pinOutgoingMainHand(player);
        }
    }

    @Inject(method={"tick"}, at={@At(value="RETURN")})
    private void pointblank$advanceMainHandSwapAtTickTail(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        this.pointblank$advanceHandSwapAndApplyHandoff(player);
    }

    @Unique
    private void pointblank$advanceHandSwapAndApplyHandoff(LocalPlayer player) {
        GunHandSwapCoordinator coordinator = GunHandSwapCoordinator.getInstance();
        coordinator.advance(player);
        if (coordinator.ownsMainHandVisual((Player)player)) {
            this.pointblank$pinOutgoingMainHand(player);
            return;
        }
        GunHandSwapCoordinator.RendererHandoff handoff = coordinator.consumeRendererHandoff((Player)player);
        if (handoff != null) {
            this.mainHandItem = handoff.incomingStack();
            this.oMainHandHeight = this.mainHandHeight = handoff.keepFullHeight() ? 1.0f : 0.0f;
            this.offHandItem = player.getOffhandItem();
            this.oOffHandHeight = this.offHandHeight = handoff.keepFullHeight() ? 1.0f : 0.0f;
        }
    }

    @Unique
    private void pointblank$pinOutgoingMainHand(LocalPlayer player) {
        GunHandSwapCoordinator coordinator = GunHandSwapCoordinator.getInstance();
        if (!coordinator.ownsMainHandVisual((Player)player)) {
            return;
        }
        this.mainHandItem = coordinator.visibleMainHandStack((Player)player);
        this.mainHandHeight = 1.0f;
        this.oMainHandHeight = 1.0f;
        if (coordinator.blocksOffhandActions((Player)player)) {
            this.offHandItem = coordinator.visibleOffhandStack((Player)player);
            this.offHandHeight = 1.0f;
            this.oOffHandHeight = 1.0f;
        }
    }

    @Inject(method={"renderArmWithItem"}, cancellable=true, at={@At(value="HEAD")})
    public void onRenderArmWithItem(AbstractClientPlayer player, float p_109373_, float p_109374_, InteractionHand hand, float p_109376_, ItemStack itemStack, float p_109378_, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo callbackInfo) {
        if (this.pointblank$shouldSuppressVanillaMainHand(player, hand, itemStack)) {
            FirstPersonHandAnchorCapture.clear();
            callbackInfo.cancel();
            return;
        }
        if (itemStack.getItem() instanceof ItemExtra) {
            FirstPersonHandAnchorCapture.clear();
            if (itemStack.getItem() instanceof GunItem) {
                Matrix4f entryMatrix = new Matrix4f((Matrix4fc)poseStack.last().pose());
                Vector3f vector3f = entryMatrix.getTranslation(new Vector3f());
            }
            boolean flag = hand == InteractionHand.MAIN_HAND;
            HumanoidArm humanoidarm = flag ? player.getMainArm() : player.getMainArm().getOpposite();
            boolean isRightHand = humanoidarm == HumanoidArm.RIGHT;
            ItemInHandRenderer itemInHandRenderer = (ItemInHandRenderer)this;
            poseStack.pushPose();
            Platform.getInstance().getEventBus().postEvent(new RenderHandEvent(hand, itemStack, poseStack, p_109373_));
            itemInHandRenderer.renderItem((LivingEntity)player, itemStack, isRightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !isRightHand, poseStack, bufferSource, packedLight);
            poseStack.popPose();
            callbackInfo.cancel();
        }
    }

    @Unique
    private boolean pointblank$shouldSuppressVanillaMainHand(AbstractClientPlayer player, InteractionHand hand, ItemStack itemStack) {
        if (player == null || hand != InteractionHand.MAIN_HAND) {
            return false;
        }
        if (itemStack != null && !itemStack.isEmpty()) {
            return false;
        }
        return GunItem.hasAltSlotGunWithEmptyMainSlot((Player)player);
    }

    @Inject(method={"renderHandsWithItems"}, at={@At(value="HEAD")})
    private void onRenderHandsWithItems(float p_109315_, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer player, int p_109319_, CallbackInfo callbackInfo) {
        GunHandSwapCoordinator coordinator = GunHandSwapCoordinator.getInstance();
        if (player != null && coordinator.blocksOffhandActions((Player)player)) {
            this.pointblank$advanceHandSwapAndApplyHandoff(player);
        }
        RenderHandsEvent.Pre event = new RenderHandsEvent.Pre(poseStack);
        Platform.getInstance().getEventBus().postEvent(event);
    }

    @Inject(method={"renderArmWithItem"}, at={@At(value="RETURN")})
    public void onRenderArmWithItemReturn(AbstractClientPlayer player, float p_109373_, float p_109374_, InteractionHand hand, float p_109376_, ItemStack itemStack, float p_109378_, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo callbackInfo) {
        FirstPersonHandAnchorCapture.clear();
    }
}

