/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.Container
 *  net.minecraft.world.ContainerHelper
 *  net.minecraft.world.ContainerListener
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.player.StackedContents
 *  net.minecraft.world.inventory.StackedContentsCompatible
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.inventory;

import com.google.common.collect.Lists;
import com.vicmatskiv.pointblank.inventory.AttachmentContainerMenu;
import com.vicmatskiv.pointblank.inventory.VirtualInventory;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleAttachmentContainer
implements Container,
StackedContentsCompatible {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private int containerIndex;
    private final int size;
    private final NonNullList<ItemStack> items;
    private List<ContainerListener> listeners;
    private AttachmentContainerMenu menu;
    private VirtualInventory virtualInventory;
    private SimpleAttachmentContainer parentContainer;

    public SimpleAttachmentContainer(int containerIndex, AttachmentContainerMenu menu, int size) {
        this.containerIndex = containerIndex;
        this.menu = menu;
        this.size = size;
        this.items = NonNullList.withSize((int)size, (Object)ItemStack.EMPTY);
    }

    public SimpleAttachmentContainer(ItemStack ... itemStacks) {
        this.size = itemStacks.length;
        this.items = NonNullList.of((Object)ItemStack.EMPTY, (Object[])itemStacks);
    }

    public int getContainerIndex() {
        return this.containerIndex;
    }

    void setParentContainer(SimpleAttachmentContainer parentContainer) {
        this.parentContainer = parentContainer;
    }

    public SimpleAttachmentContainer getParentContainer() {
        return this.parentContainer;
    }

    void setVirtualInventory(VirtualInventory virtualInventory) {
        this.virtualInventory = virtualInventory;
    }

    public VirtualInventory getVirtualInventory() {
        return this.virtualInventory;
    }

    public void addListener(ContainerListener listener) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }
        this.listeners.add(listener);
    }

    public void removeListener(ContainerListener listeners) {
        if (this.listeners != null) {
            this.listeners.remove(listeners);
        }
    }

    public void removeAllListeners() {
        this.listeners = null;
    }

    public ItemStack getItem(int index) {
        return index >= 0 && index < this.items.size() ? (ItemStack)this.items.get(index) : ItemStack.EMPTY;
    }

    public List<ItemStack> removeAllItems() {
        List<ItemStack> list = this.items.stream().filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList());
        this.clearContent();
        return list;
    }

    public ItemStack removeItem(int p_19159_, int p_19160_) {
        ItemStack itemstack = ContainerHelper.removeItem(this.items, (int)p_19159_, (int)p_19160_);
        if (!itemstack.isEmpty()) {
            this.setChanged();
        }
        return itemstack;
    }

    public ItemStack removeItemType(Item item, int p_19172_) {
        ItemStack itemstack = new ItemStack((ItemLike)item, 0);
        for (int i = this.size - 1; i >= 0; --i) {
            ItemStack itemstack1 = this.getItem(i);
            if (!itemstack1.getItem().equals(item)) continue;
            int j = p_19172_ - itemstack.getCount();
            ItemStack itemstack2 = itemstack1.split(j);
            itemstack.grow(itemstack2.getCount());
            if (itemstack.getCount() == p_19172_) break;
        }
        if (!itemstack.isEmpty()) {
            this.setChanged();
        }
        return itemstack;
    }

    public ItemStack addItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStackCopy = itemStack.copy();
        this.moveItemToOccupiedSlotsWithSameType(itemStackCopy);
        if (itemStackCopy.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.moveItemToEmptySlots(itemStackCopy);
        return itemStackCopy.isEmpty() ? ItemStack.EMPTY : itemStackCopy;
    }

    public boolean canAddItem(ItemStack item) {
        boolean flag = false;
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty() && (!ItemStack.isSameItemSameComponents((ItemStack)itemstack, (ItemStack)item) || itemstack.getCount() >= itemstack.getMaxStackSize())) continue;
            flag = true;
            break;
        }
        return flag;
    }

    public ItemStack removeItemNoUpdate(int itemIndex) {
        ItemStack itemstack = (ItemStack)this.items.get(itemIndex);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.items.set(itemIndex, (Object)ItemStack.EMPTY);
        return itemstack;
    }

    public void setItem(int index, ItemStack itemStack) {
        LOGGER.debug("Setting item {} in container {} to stack {} with tag {}", (Object)index, (Object)this, (Object)itemStack, (Object)MiscUtil.getTag(itemStack));
        this.items.set(index, (Object)itemStack);
        if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    public int getContainerSize() {
        return this.size;
    }

    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (itemstack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public void setChanged() {
        if (this.listeners != null) {
            for (ContainerListener containerlistener : this.listeners) {
                containerlistener.containerChanged((Container)this);
            }
        }
    }

    public boolean stillValid(Player p_19167_) {
        return true;
    }

    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    public void fillStackedContents(StackedContents p_19169_) {
        for (ItemStack itemstack : this.items) {
            p_19169_.accountStack(itemstack);
        }
    }

    public String toString() {
        return String.format("{Container #%d id: %d, items: %s }", this.containerIndex, System.identityHashCode(this), this.items);
    }

    private void moveItemToEmptySlots(ItemStack p_19190_) {
        for (int i = 0; i < this.size; ++i) {
            ItemStack itemstack = this.getItem(i);
            if (!itemstack.isEmpty()) continue;
            this.setItem(i, p_19190_.copyAndClear());
            return;
        }
    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack p_19192_) {
        for (int i = 0; i < this.size; ++i) {
            ItemStack itemstack = this.getItem(i);
            if (!ItemStack.isSameItemSameComponents((ItemStack)itemstack, (ItemStack)p_19192_)) continue;
            this.moveItemsBetweenStacks(p_19192_, itemstack);
            if (!p_19192_.isEmpty()) continue;
            return;
        }
    }

    private void moveItemsBetweenStacks(ItemStack fromStack, ItemStack toStack) {
        int i = Math.min(this.getMaxStackSize(), toStack.getMaxStackSize());
        int j = Math.min(fromStack.getCount(), i - toStack.getCount());
        if (j > 0) {
            toStack.grow(j);
            fromStack.shrink(j);
            this.setChanged();
        }
    }

    AttachmentContainerMenu getMenu() {
        return this.menu;
    }

    public static int getContainerStartIndex(SimpleAttachmentContainer[] attachmentContainers, int containerIndex) {
        int startIndex = 0;
        for (int i = 0; i < containerIndex; ++i) {
            SimpleAttachmentContainer container = attachmentContainers[i];
            startIndex += container.getContainerSize();
        }
        return startIndex;
    }
}

