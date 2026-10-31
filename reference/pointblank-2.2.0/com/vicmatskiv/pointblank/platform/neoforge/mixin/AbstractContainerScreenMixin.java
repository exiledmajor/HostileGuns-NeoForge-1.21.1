/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.vicmatskiv.pointblank.platform.neoforge.mixin;

import com.vicmatskiv.pointblank.client.SelectedMainHandQuickMoveTracker;
import com.vicmatskiv.pointblank.client.gui.AttachmentManagerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={AbstractContainerScreen.class})
public abstract class AbstractContainerScreenMixin {
    @Inject(method={"renderSlotContents"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V")})
    public void beforeRenderingSlotItem(GuiGraphics guiGraphics, ItemStack itemstack, Slot slot, String countString, CallbackInfo callbackInfo) {
        AbstractContainerScreenMixin abstractContainerScreenMixin = this;
        if (abstractContainerScreenMixin instanceof AttachmentManagerScreen) {
            AttachmentManagerScreen ams = (AttachmentManagerScreen)((Object)abstractContainerScreenMixin);
            ams.beforeRenderingSlot(guiGraphics, slot);
        }
    }

    @Inject(method={"slotClicked"}, at={@At(value="HEAD")})
    private void pointblank$trackQuickMoveCandidate(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo callbackInfo) {
        if (type == ClickType.QUICK_MOVE && slot != null && slot.hasItem()) {
            SelectedMainHandQuickMoveTracker.getInstance().recordQuickMoveCandidate(Minecraft.getInstance().player, slot.getItem());
        }
    }
}

