/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.Slot
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.vicmatskiv.pointblank.platform.neoforge.mixin;

import com.vicmatskiv.pointblank.client.SelectedMainHandQuickMoveTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={CreativeModeInventoryScreen.class})
public abstract class CreativeModeInventoryScreenMixin {
    @Inject(method={"slotClicked"}, at={@At(value="HEAD")})
    private void pointblank$trackQuickMoveCandidate(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo callbackInfo) {
        if (type == ClickType.QUICK_MOVE && slot != null && slot.hasItem()) {
            SelectedMainHandQuickMoveTracker.getInstance().recordQuickMoveCandidate(Minecraft.getInstance().player, slot.getItem());
        }
    }
}

