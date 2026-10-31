/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.inventory;

import com.vicmatskiv.pointblank.inventory.Activatable;
import com.vicmatskiv.pointblank.inventory.HierarchicalSlot;
import com.vicmatskiv.pointblank.inventory.SimpleAttachmentContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class AttachmentHeadingSlot
extends Slot
implements Activatable,
HierarchicalSlot {
    private final SimpleAttachmentContainer attachmentContainer;
    private boolean isActive;
    private HierarchicalSlot parentSlot;

    AttachmentHeadingSlot(Container container, int index, int x, int y, SimpleAttachmentContainer attachmentContainer) {
        super(container, index, x, y);
        this.attachmentContainer = attachmentContainer;
    }

    public ItemStack getItem() {
        return this.attachmentContainer.getVirtualInventory() != null ? this.attachmentContainer.getVirtualInventory().getItemStack() : ItemStack.EMPTY;
    }

    public boolean isHighlightable() {
        return false;
    }

    public boolean mayPickup(Player player) {
        return false;
    }

    public boolean mayPlace(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    void clear() {
        this.parentSlot = null;
    }

    @Override
    public String getPath() {
        String parentPath = this.getParentSlot() != null ? this.getParentSlot().getPath() : "/";
        return parentPath + "/";
    }

    @Override
    public HierarchicalSlot getParentSlot() {
        return this.parentSlot;
    }

    @Override
    public void setParentSlot(HierarchicalSlot parentSlot) {
        this.parentSlot = parentSlot;
    }
}

