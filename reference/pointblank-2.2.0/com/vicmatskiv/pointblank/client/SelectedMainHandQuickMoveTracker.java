/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package com.vicmatskiv.pointblank.client;

import com.vicmatskiv.pointblank.item.GunItem;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class SelectedMainHandQuickMoveTracker {
    private static final SelectedMainHandQuickMoveTracker INSTANCE = new SelectedMainHandQuickMoveTracker();
    private int trackedPlayerId = -1;
    private int trackedSelectedSlot = -1;
    @Nullable
    private Item trackedItem;
    @Nullable
    private UUID trackedStateId;
    @Nullable
    private UUID fallbackStateId;

    private SelectedMainHandQuickMoveTracker() {
    }

    public static SelectedMainHandQuickMoveTracker getInstance() {
        return INSTANCE;
    }

    public void clear() {
        this.trackedPlayerId = -1;
        this.trackedSelectedSlot = -1;
        this.trackedItem = null;
        this.trackedStateId = null;
        this.fallbackStateId = null;
    }

    public void recordQuickMoveCandidate(@Nullable LocalPlayer player, ItemStack sourceStack) {
        if (player == null || sourceStack == null || sourceStack.isEmpty() || !(sourceStack.getItem() instanceof GunItem)) {
            return;
        }
        int selectedSlot = player.getInventory().selected;
        ItemStack selectedStack = player.getInventory().getItem(selectedSlot);
        if (selectedStack != null && !selectedStack.isEmpty()) {
            return;
        }
        UUID sourceStateId = GunItem.getItemStackId(sourceStack);
        if (sourceStateId == null) {
            return;
        }
        this.trackedPlayerId = player.getId();
        this.trackedSelectedSlot = selectedSlot;
        this.trackedItem = sourceStack.getItem();
        this.trackedStateId = sourceStateId;
        this.fallbackStateId = sourceStateId;
    }

    public void observeSelectedSlot(@Nullable LocalPlayer player) {
        if (player == null || this.trackedPlayerId != player.getId() || player.getInventory().selected != this.trackedSelectedSlot) {
            this.clear();
            return;
        }
        ItemStack selectedStack = player.getMainHandItem();
        if (selectedStack == null || selectedStack.isEmpty()) {
            if (!(Minecraft.getInstance().screen instanceof AbstractContainerScreen)) {
                this.clear();
            }
            return;
        }
        if (!(selectedStack.getItem() instanceof GunItem) || selectedStack.getItem() != this.trackedItem) {
            this.clear();
            return;
        }
        UUID currentStateId = GunItem.getItemStackId(selectedStack);
        if (currentStateId != null) {
            this.trackedStateId = currentStateId;
        }
    }

    @Nullable
    public UUID resolveRecordedStateId(@Nullable Player player, @Nullable ItemStack itemStack, int slotIndex, boolean isOffhand) {
        if (!this.matchesTrackedSlot(player, itemStack, slotIndex, isOffhand) || this.fallbackStateId == null) {
            return null;
        }
        return this.fallbackStateId;
    }

    @Nullable
    public UUID resolveTrackedStateId(@Nullable Player player, @Nullable ItemStack itemStack, int slotIndex, boolean isOffhand) {
        if (!this.matchesTrackedSlot(player, itemStack, slotIndex, isOffhand) || this.trackedStateId == null) {
            return null;
        }
        UUID currentStateId = GunItem.getItemStackId(itemStack);
        if (currentStateId != null) {
            this.trackedStateId = currentStateId;
        }
        return this.trackedStateId;
    }

    private boolean matchesTrackedSlot(@Nullable Player player, @Nullable ItemStack itemStack, int slotIndex, boolean isOffhand) {
        if (player == null || isOffhand || this.trackedItem == null || this.trackedPlayerId != player.getId() || itemStack == null || itemStack.isEmpty() || itemStack.getItem() != this.trackedItem) {
            return false;
        }
        int effectiveSlotIndex = slotIndex == -1 ? player.getInventory().selected : slotIndex;
        return effectiveSlotIndex == this.trackedSelectedSlot;
    }
}

