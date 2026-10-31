/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.inventory;

import com.vicmatskiv.pointblank.Nameable;
import com.vicmatskiv.pointblank.attachment.Attachment;
import com.vicmatskiv.pointblank.attachment.Attachments;
import com.vicmatskiv.pointblank.inventory.Activatable;
import com.vicmatskiv.pointblank.inventory.AttachmentContainerMenu;
import com.vicmatskiv.pointblank.inventory.HierarchicalSlot;
import com.vicmatskiv.pointblank.inventory.SimpleAttachmentContainer;
import com.vicmatskiv.pointblank.inventory.SlotExt;
import com.vicmatskiv.pointblank.inventory.VirtualInventory;
import com.vicmatskiv.pointblank.util.MiscUtil;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AttachmentSlot
extends Slot
implements SlotExt,
Activatable,
HierarchicalSlot {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private AttachmentContainerMenu menu;
    private boolean isActive;
    private SimpleAttachmentContainer container;
    private int slotIndexInContainer;
    private SimpleAttachmentContainer childContainer;
    private HierarchicalSlot parentSlot;

    public AttachmentSlot(Player player, AttachmentContainerMenu menu, SimpleAttachmentContainer container, int index, int x, int y) {
        super((Container)container, index, x, y);
        this.slotIndexInContainer = index;
        this.container = container;
        this.menu = menu;
    }

    public SimpleAttachmentContainer getContainer() {
        return this.container;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    void clear() {
        this.childContainer = null;
        this.parentSlot = null;
    }

    @Override
    public String getPath() {
        String string;
        Item item;
        String parentPath = this.getParentSlot() != null ? this.getParentSlot().getPath() : "/";
        ItemStack itemStack = this.getItem();
        if (itemStack != null && (item = itemStack.getItem()) instanceof Nameable) {
            Nameable n = (Nameable)item;
            string = n.getName();
        } else {
            string = "?";
        }
        String name = string;
        return parentPath + "/" + name;
    }

    @Override
    public HierarchicalSlot getParentSlot() {
        return this.parentSlot;
    }

    @Override
    public void setParentSlot(HierarchicalSlot parentSlot) {
        this.parentSlot = parentSlot;
    }

    void setChildContainer(SimpleAttachmentContainer childContainer) {
        this.childContainer = childContainer;
    }

    public SimpleAttachmentContainer getChildContainer() {
        return this.childContainer;
    }

    public boolean mayPlace(ItemStack newAttachmentStack) {
        if (!this.isActive) {
            return false;
        }
        if (!(newAttachmentStack.getItem() instanceof Attachment)) {
            return false;
        }
        ItemStack currentItemStack = this.getItem();
        if (currentItemStack != null && currentItemStack.getItem() instanceof Attachment) {
            return false;
        }
        VirtualInventory e = this.container.getVirtualInventory();
        return e != null ? e.mayPlace(newAttachmentStack, this) : false;
    }

    public int getMaxStackSize() {
        return 1;
    }

    public boolean mayPickup(Player player) {
        if (!this.isActive) {
            return false;
        }
        ItemStack currentItemStack = this.getItem();
        if (currentItemStack == null || currentItemStack.isEmpty() || player.isCreative()) {
            return true;
        }
        if (!(currentItemStack.getItem() instanceof Attachment)) {
            return true;
        }
        return Attachments.isRemoveable(MiscUtil.getTag(currentItemStack));
    }

    @Override
    public void setActive(boolean isActive) {
        if (isActive != this.isActive) {
            LOGGER.debug("Changing status for slot {} in container {} to {},", (Object)this.slotIndexInContainer, (Object)this.container, (Object)isActive);
        }
        this.isActive = isActive;
    }

    public void setByPlayer(ItemStack itemStack) {
        this.doSet(itemStack);
    }

    private void doSet(ItemStack itemStack) {
        LOGGER.debug("Setting attachment slot {} for container {} to stack {} with tag {}", (Object)this.slotIndexInContainer, (Object)this.container, (Object)itemStack, (Object)MiscUtil.getTag(itemStack));
        super.set(itemStack);
    }

    public void set(ItemStack itemStack) {
        this.doSet(itemStack);
    }

    public void setChanged() {
    }
}

