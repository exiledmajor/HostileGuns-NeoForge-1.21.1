/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.inventory;

import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.attachment.AttachmentHost;
import com.vicmatskiv.pointblank.event.MenuRegistry;
import com.vicmatskiv.pointblank.inventory.Activatable;
import com.vicmatskiv.pointblank.inventory.AttachmentHeadingSlot;
import com.vicmatskiv.pointblank.inventory.AttachmentSlot;
import com.vicmatskiv.pointblank.inventory.HierarchicalSlot;
import com.vicmatskiv.pointblank.inventory.SimpleAttachmentContainer;
import com.vicmatskiv.pointblank.inventory.SlotMapping;
import com.vicmatskiv.pointblank.inventory.VirtualInventory;
import java.util.Map;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AttachmentContainerMenu
extends AbstractContainerMenu {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static final int MAX_TOP_LEVEL_ATTACHMENTS = 11;
    private static final int ATTACHMENT_CONTAINERS = 10;
    private static final int ATTACHMENTS_PER_CONTAINER = 6;
    private SlotMapping slotMapping;
    private Inventory playerInventory;
    private VirtualInventory virtualInventory;
    private SimpleAttachmentContainer[] attachmentContainers;
    private int totalAttachmentSlots;
    private int slotWidth;
    private int slotHeight;
    private int mainInventoryLeftOffset;
    private int mainInventoryTopOffset;
    private int hotBarTopOffset;
    private int attachmentsLeftOffset;
    private int attachmentsTopOffset;
    private int attachmentsSlotRightPadding;
    private int attachmentsHeaderBottomPadding;

    public AttachmentContainerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, playerInventory.getSelected());
    }

    public AttachmentContainerMenu(int containerId, Inventory playerInventory, ItemStack itemStack) {
        super(MenuRegistry.ATTACHMENTS.get(), containerId);
        AttachmentHost attachmentHost = (AttachmentHost)itemStack.getItem();
        this.slotWidth = 18;
        this.slotHeight = 18;
        this.hotBarTopOffset = 220;
        this.mainInventoryLeftOffset = 105;
        this.mainInventoryTopOffset = 162;
        this.attachmentsSlotRightPadding = 4;
        this.attachmentsHeaderBottomPadding = 2;
        this.attachmentsLeftOffset = 6;
        this.attachmentsTopOffset = 9;
        this.playerInventory = playerInventory;
        this.slotMapping = SlotMapping.getOrCreate(playerInventory.player);
        this.virtualInventory = VirtualInventory.createInventory(playerInventory.player, playerInventory.getSelected());
        this.attachmentContainers = new SimpleAttachmentContainer[10];
        this.totalAttachmentSlots = Math.min(attachmentHost.getMaxAttachmentCategories(), 11) + 63;
        try {
            this.initAttachmentContainers();
            this.updateAttachmentSlots();
            this.addInventorySlots();
            this.addHotbarSlots();
        }
        catch (Exception e) {
            LOGGER.error("Failed to initialize attachment container menu", (Throwable)e);
        }
    }

    public Inventory getPlayerInventory() {
        return this.playerInventory;
    }

    public SimpleAttachmentContainer[] getAttachmentContainers() {
        return this.attachmentContainers;
    }

    public int getTotalAttachmentSlots() {
        return this.totalAttachmentSlots;
    }

    private void addInventorySlots() {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot((Container)this.playerInventory, j + i * 9 + 9, this.mainInventoryLeftOffset + j * this.slotWidth, this.mainInventoryTopOffset + i * this.slotHeight));
            }
        }
    }

    private void addHotbarSlots() {
        for (int i = 0; i < 9; ++i) {
            if (i == this.playerInventory.selected) {
                this.addSlot(new Slot(this, (Container)this.playerInventory, i, this.mainInventoryLeftOffset + i * this.slotWidth, this.hotBarTopOffset){

                    public boolean mayPickup(Player playerIn) {
                        return false;
                    }
                });
                continue;
            }
            this.addSlot(new Slot((Container)this.playerInventory, i, this.mainInventoryLeftOffset + i * this.slotWidth, this.hotBarTopOffset));
        }
    }

    private void initAttachmentContainers() {
        for (int containerIndex = 0; containerIndex < 10; ++containerIndex) {
            this.attachmentContainers[containerIndex] = this.initAttachmentContainer(containerIndex);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private SimpleAttachmentContainer initAttachmentContainer(int containerIndex) {
        int attachmentSlotCount = containerIndex == 0 ? 11 : 6;
        SimpleAttachmentContainer attachmentContainer = new SimpleAttachmentContainer(containerIndex, this, attachmentSlotCount + 1);
        LOGGER.debug("Created container {}", (Object)containerIndex);
        AttachmentHeadingSlot attachmentHeadingSlot = new AttachmentHeadingSlot(attachmentContainer, 0, this.attachmentsLeftOffset + containerIndex * (this.slotWidth + this.attachmentsSlotRightPadding), this.attachmentsTopOffset, attachmentContainer);
        this.suppressRemoteUpdates();
        try {
            this.addSlot(attachmentHeadingSlot);
            for (int attachmentInContainerIndex = 1; attachmentInContainerIndex < attachmentSlotCount + 1; ++attachmentInContainerIndex) {
                AttachmentSlot attachmentSlot = new AttachmentSlot(this.playerInventory.player, this, attachmentContainer, attachmentInContainerIndex, this.attachmentsLeftOffset + containerIndex * (this.slotWidth + this.attachmentsSlotRightPadding), this.attachmentsTopOffset + this.attachmentsHeaderBottomPadding + attachmentInContainerIndex * this.slotHeight);
                attachmentSlot.setActive(false);
                this.addSlot(attachmentSlot);
                LOGGER.debug("Added attachment slot {} to container {}", (Object)attachmentInContainerIndex, (Object)attachmentContainer);
            }
        }
        finally {
            this.resumeRemoteUpdates();
        }
        return attachmentContainer;
    }

    private void clearContainer(int i, SimpleAttachmentContainer container) {
        container.setParentContainer(container);
        container.removeAllListeners();
        container.clearContent();
        container.setVirtualInventory(null);
        int startSlotIndex = SimpleAttachmentContainer.getContainerStartIndex(this.attachmentContainers, i);
        for (int attachmentSlotIndex = 1; attachmentSlotIndex < container.getContainerSize(); ++attachmentSlotIndex) {
            int absoluteSlotIndex = startSlotIndex + attachmentSlotIndex;
            ((Activatable)this.getSlot(absoluteSlotIndex)).setActive(false);
        }
    }

    void updateAttachmentSlots() {
        this.suppressRemoteUpdates();
        try {
            int lastContainerIndex = this.updateAttachmentSlots(null, null, this.virtualInventory, 0);
            for (int i = lastContainerIndex + 1; i < this.attachmentContainers.length; ++i) {
                this.clearContainer(i, this.attachmentContainers[i]);
            }
        }
        finally {
            this.resumeRemoteUpdates();
        }
    }

    private int updateAttachmentSlots(AttachmentSlot parentSlot, SimpleAttachmentContainer parentContainer, VirtualInventory virtualInventory, int containerIndex) {
        if (virtualInventory == null) {
            return -1;
        }
        if (containerIndex >= this.attachmentContainers.length) {
            LOGGER.error("Requested container index {} exceeds the max {}", (Object)containerIndex, (Object)(this.attachmentContainers.length - 1));
            return -1;
        }
        int maxContainerIndex = containerIndex;
        SimpleAttachmentContainer container = this.attachmentContainers[containerIndex];
        container.removeAllListeners();
        container.clearContent();
        container.setVirtualInventory(virtualInventory);
        container.setParentContainer(parentContainer);
        if (parentSlot != null) {
            parentSlot.setChildContainer(container);
        }
        LOGGER.debug("Updating attachment slots for inventory {} for container {}", (Object)virtualInventory, (Object)containerIndex);
        Map<Integer, AttachmentCategory> mapping = this.slotMapping.getOrCreateSlotMapping(virtualInventory);
        int startSlotIndex = SimpleAttachmentContainer.getContainerStartIndex(this.attachmentContainers, containerIndex);
        ItemStack headerStack = virtualInventory.getItemStack().copy();
        container.setItem(0, headerStack);
        HierarchicalSlot headerSlot = (HierarchicalSlot)this.getSlot(startSlotIndex);
        headerSlot.setParentSlot(parentSlot);
        int activeAttachmentCount = 0;
        for (int attachmentSlotIndex = 1; attachmentSlotIndex < container.getContainerSize(); ++attachmentSlotIndex) {
            int absoluteSlotIndex = startSlotIndex + attachmentSlotIndex;
            AttachmentSlot slot = (AttachmentSlot)this.getSlot(absoluteSlotIndex);
            slot.clear();
            slot.setParentSlot(parentSlot);
            if (attachmentSlotIndex - 1 < virtualInventory.getElements().size()) {
                AttachmentCategory category = mapping.get(attachmentSlotIndex);
                if (category != null) {
                    VirtualInventory mappedElement = virtualInventory.getElement(category);
                    if (mappedElement != null) {
                        int subIndex;
                        container.setItem(attachmentSlotIndex, mappedElement.getItemStack().copy());
                        LOGGER.debug("Updated slot #{} in container {} with item {}", (Object)attachmentSlotIndex, (Object)container, (Object)container.getItem(attachmentSlotIndex));
                        if (!mappedElement.getElements().isEmpty() && (subIndex = this.updateAttachmentSlots(slot, container, mappedElement, maxContainerIndex + 1)) > maxContainerIndex) {
                            maxContainerIndex = subIndex;
                        }
                    }
                } else {
                    container.setItem(attachmentSlotIndex, ItemStack.EMPTY);
                    LOGGER.debug("Updated slot #{} in container {} with empty item {}", (Object)attachmentSlotIndex, (Object)container, (Object)container.getItem(attachmentSlotIndex));
                }
                slot.setActive(true);
                ++activeAttachmentCount;
                continue;
            }
            slot.setActive(false);
        }
        LOGGER.debug("Container active attachment count: {} in container {}", (Object)activeAttachmentCount, (Object)container);
        ((Activatable)this.getSlot(startSlotIndex)).setActive(activeAttachmentCount > 0);
        container.addListener(virtualInventory);
        LOGGER.debug("Updated attachment slots for container {}", (Object)container);
        return maxContainerIndex;
    }

    SlotMapping getSlotMapping() {
        return this.slotMapping;
    }

    public boolean stillValid(Player player) {
        return true;
    }

    public void slotsChanged(Container inventory) {
        LOGGER.debug("Slots changed for container {}", (Object)inventory);
        super.broadcastChanges();
    }

    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack copyStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            copyStack = slotStack.copy();
            if (slotIndex < this.totalAttachmentSlots ? !this.moveItemStackTo(slotStack, this.totalAttachmentSlots, this.slots.size(), true) : !this.moveItemStackTo(slotStack, 0, this.totalAttachmentSlots, false)) {
                return ItemStack.EMPTY;
            }
            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return copyStack;
    }
}

