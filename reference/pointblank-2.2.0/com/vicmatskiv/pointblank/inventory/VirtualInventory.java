/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.ContainerListener
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.inventory;

import com.vicmatskiv.pointblank.Nameable;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.attachment.Attachment;
import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.attachment.AttachmentHost;
import com.vicmatskiv.pointblank.attachment.Attachments;
import com.vicmatskiv.pointblank.event.AttachmentAddedEvent;
import com.vicmatskiv.pointblank.event.AttachmentRemovedEvent;
import com.vicmatskiv.pointblank.inventory.AttachmentContainerMenu;
import com.vicmatskiv.pointblank.inventory.AttachmentSlot;
import com.vicmatskiv.pointblank.inventory.SimpleAttachmentContainer;
import com.vicmatskiv.pointblank.inventory.SlotMapping;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VirtualInventory
implements ContainerListener {
    private static final Logger logger = LogManager.getLogger((String)"pointblank");
    protected AttachmentCategory category;
    protected ItemStack itemStack;
    protected VirtualInventory parent;
    protected Map<AttachmentCategory, VirtualInventory> elements;
    protected Player owner;

    private VirtualInventory(Player owner, AttachmentCategory category, VirtualInventory parent, ItemStack itemStack) {
        this.owner = owner;
        this.category = category;
        this.parent = parent;
        this.itemStack = itemStack != null ? itemStack : ItemStack.EMPTY;
        this.elements = new LinkedHashMap<AttachmentCategory, VirtualInventory>();
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    private void addElement(VirtualInventory e) {
        AttachmentCategory category = e.category;
        if (category == null) {
            logger.error("Adding an element without a category");
        }
        this.elements.put(e.category, e);
    }

    public Map<AttachmentCategory, VirtualInventory> getElements() {
        return Collections.unmodifiableMap(this.elements);
    }

    public VirtualInventory getElement(AttachmentCategory category) {
        return this.elements.get(category);
    }

    private Collection<AttachmentCategory> getCategories() {
        return this.elements.keySet();
    }

    public boolean mayPlace(ItemStack attachmentStack, AttachmentSlot attachmentSlot) {
        if (attachmentSlot.getSlotIndex() > this.elements.size()) {
            return false;
        }
        if (!this.isCompatibleAttachment(attachmentStack)) {
            return false;
        }
        AttachmentCategory attachmentCategory = ((Attachment)attachmentStack.getItem()).getCategory();
        return !this.hasAttachmentInCategory(attachmentCategory);
    }

    public boolean hasAttachments() {
        return this.elements.values().stream().anyMatch(e -> e.itemStack != null && !e.itemStack.isEmpty());
    }

    private boolean hasAttachmentInCategory(AttachmentCategory category) {
        VirtualInventory e = this.elements.get(category);
        return e != null && e.itemStack != null && !e.itemStack.isEmpty();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean isCompatibleAttachment(ItemStack attachmentStack) {
        Item item = attachmentStack.getItem();
        if (!(item instanceof Attachment)) return false;
        Attachment attachment = (Attachment)item;
        if (!((AttachmentHost)this.itemStack.getItem()).getCompatibleAttachments().contains(attachment)) return false;
        return true;
    }

    public boolean mayPickup(Player player) {
        return false;
    }

    String getPath() {
        if (this.parent != null) {
            Item item = this.itemStack.getItem();
            if (item instanceof Nameable) {
                Nameable nameable = (Nameable)item;
                return this.parent.getPath() + "/" + nameable.getName();
            }
            return this.parent.getPath() + "/" + this.itemStack.getItem().toString();
        }
        return MiscUtil.getItemStackId(this.itemStack).toString();
    }

    private ItemStack getRootStack() {
        if (this.parent != null) {
            return this.parent.getRootStack();
        }
        return this.itemStack;
    }

    public void containerChanged(Container c) {
        if (!(c instanceof SimpleAttachmentContainer)) {
            return;
        }
        SimpleAttachmentContainer container = (SimpleAttachmentContainer)c;
        AttachmentContainerMenu menu = container.getMenu();
        logger.debug("Virtual inventory {} handling changes in container {}, stack tags {}", (Object)this, (Object)container, (Object)MiscUtil.getTag(this.itemStack));
        SlotMapping slotMapping = menu.getSlotMapping();
        Map<Integer, AttachmentCategory> stackSlotMapping = slotMapping.getStackSlotMapping(this);
        if (stackSlotMapping == null) {
            logger.warn("Slot mapping not found for container {}, stack tags {}", (Object)System.identityHashCode(container), (Object)MiscUtil.getTag(this.itemStack));
            return;
        }
        List<Attachment> removedItems = Attachments.removeAllAttachments(this.itemStack);
        for (int i = 1; i < container.getContainerSize(); ++i) {
            Item item;
            ItemStack slotStack = container.getItem(i);
            if (!slotStack.isEmpty() && (item = slotStack.getItem()) instanceof Attachment) {
                Attachment a1 = (Attachment)item;
                logger.debug("Adding attachment '{}' from slot {} with tag {} to stack '{}'", (Object)slotStack, (Object)i, (Object)MiscUtil.getTag(slotStack), (Object)this.itemStack);
                Attachments.addAttachment(this.itemStack, slotStack, true);
                if (!removedItems.contains(a1)) {
                    AttachmentAddedEvent event = new AttachmentAddedEvent(this.owner, this.itemStack, slotStack);
                    Platform.getInstance().getEventBus().postEvent(event);
                    logger.debug("Added new attachment '{}' from slot {} with tag {} to stack '{}' with tag {} and path {}", (Object)slotStack, (Object)i, (Object)MiscUtil.getTag(slotStack), (Object)this.itemStack, (Object)MiscUtil.getTag(this.itemStack));
                } else {
                    logger.debug("Re-added existing attachment '{}' from slot {} with tag {} to stack '{}' with tag {}", (Object)slotStack, (Object)i, (Object)MiscUtil.getTag(slotStack), (Object)this.itemStack, (Object)MiscUtil.getTag(this.itemStack));
                }
                stackSlotMapping.put(i, a1.getCategory());
                VirtualInventory childInventory = VirtualInventory.createInventory(this.owner, this, slotStack);
                if (childInventory == null) continue;
                this.elements.put(a1.getCategory(), childInventory);
                continue;
            }
            AttachmentCategory category = stackSlotMapping.remove(i);
            if (category == null) continue;
            logger.debug("Removing attachment '{}' from slot {}, with tag {} from stack '{}'", (Object)slotStack, (Object)i, (Object)MiscUtil.getTag(slotStack), (Object)this.itemStack);
            VirtualInventory e = this.elements.get(category);
            if (e == null) continue;
            AttachmentRemovedEvent event = new AttachmentRemovedEvent(this.owner, this.getRootStack(), this.itemStack, slotStack);
            Platform.getInstance().getEventBus().postEvent(event);
            e.itemStack = ItemStack.EMPTY;
            e.elements.clear();
        }
        slotMapping.saveSlotMapping();
        if (this.parent != null) {
            this.parent.onContentChange();
        }
        menu.updateAttachmentSlots();
        menu.broadcastChanges();
        logger.debug("Virtual inventory {} handled changes for container {}, stack tags {}", (Object)this, (Object)container, (Object)MiscUtil.getTag(this.itemStack));
    }

    private void onContentChange() {
        logger.debug("Updating tag content for {}, tag: {}", (Object)this, (Object)MiscUtil.getTag(this.itemStack));
        this.updateTag();
        logger.debug("Updating tag content for {}, tag: {}", (Object)this, (Object)MiscUtil.getTag(this.itemStack));
        if (this.parent != null) {
            this.parent.updateTag();
        }
    }

    private CompoundTag updateTag() {
        if (this.itemStack == null) {
            return null;
        }
        if (this.itemStack.isEmpty()) {
            logger.error("Virtual inventory {} attempted to update empty stack {}", (Object)this, (Object)this.itemStack);
            return null;
        }
        CompoundTag tag = MiscUtil.getOrCreateTag(this.itemStack);
        Item item = this.itemStack.getItem();
        if (item instanceof Attachment) {
            Attachment attachment = (Attachment)item;
            String attachmentId = BuiltInRegistries.ITEM.getKey((Object)attachment.asItem()).toString();
            tag.putString("id", attachmentId);
            tag.putBoolean("rmv", tag.getBoolean("rmv"));
        }
        ListTag nestedAttachments = tag.getList("as", 10);
        nestedAttachments.clear();
        for (Map.Entry entry : this.elements.entrySet()) {
            CompoundTag nestedAttachmentTag = ((VirtualInventory)entry.getValue()).updateTag();
            if (nestedAttachmentTag == null) continue;
            nestedAttachments.add((Object)nestedAttachmentTag);
        }
        tag.put("as", (Tag)nestedAttachments);
        MiscUtil.setTag(this.itemStack, tag);
        return tag;
    }

    Map<Integer, AttachmentCategory> createSlotMapping(String stackId) {
        HashMap<Integer, AttachmentCategory> mapping = new HashMap<Integer, AttachmentCategory>();
        int i = 1;
        for (AttachmentCategory category : this.getCategories()) {
            VirtualInventory e = this.getElement(category);
            ItemStack itemStack = e.getItemStack();
            if (itemStack != null && !itemStack.isEmpty()) {
                mapping.put(i, category);
            }
            ++i;
        }
        return mapping;
    }

    boolean isValidSlotMapping(Map<Integer, AttachmentCategory> mapping) {
        if (mapping.isEmpty()) {
            return this.elements.isEmpty();
        }
        return this.elements.keySet().containsAll(mapping.values());
    }

    public String toString() {
        return String.format("{vi: %d, path: %s, elements: %s}", System.identityHashCode(this), this.getPath(), this.elements);
    }

    private static VirtualInventory createInventory(Player owner, VirtualInventory parentInventory, ItemStack currentStack) {
        Item item;
        AttachmentCategory currentCategory = null;
        if (currentStack != null && (item = currentStack.getItem()) instanceof Attachment) {
            Attachment attachment = (Attachment)item;
            currentCategory = attachment.getCategory();
        }
        VirtualInventory currentInventory = new VirtualInventory(owner, currentCategory, parentInventory, currentStack);
        if (currentStack == null) {
            return currentInventory;
        }
        CompoundTag currentTag = MiscUtil.getOrCreateTag(currentStack);
        Item item2 = currentStack.getItem();
        if (!(item2 instanceof AttachmentHost)) {
            return currentInventory;
        }
        AttachmentHost attachmentHost = (AttachmentHost)item2;
        HashMap<AttachmentCategory, ItemStack> attachmentStacks = new HashMap<AttachmentCategory, ItemStack>();
        if (currentTag.contains("as", 9)) {
            ListTag attachmentsList = currentTag.getList("as", 10);
            for (int i = 0; i < attachmentsList.size(); ++i) {
                CompoundTag attachmentTag = attachmentsList.getCompound(i);
                String itemId = attachmentTag.getString("id");
                Item item3 = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.parse((String)itemId));
                if (!(item3 instanceof Attachment)) continue;
                Attachment attachment = (Attachment)item3;
                ItemStack attachmentItemStack = new ItemStack((ItemLike)item3);
                MiscUtil.setTag(attachmentItemStack, attachmentTag);
                attachmentStacks.put(attachment.getCategory(), attachmentItemStack);
            }
        }
        for (AttachmentCategory category : attachmentHost.getCompatibleAttachmentCategories()) {
            ItemStack attachmentStack = (ItemStack)attachmentStacks.get(category);
            VirtualInventory nestedInventory = attachmentStack == null ? new VirtualInventory(owner, category, currentInventory, null) : VirtualInventory.createInventory(owner, currentInventory, attachmentStack);
            if (nestedInventory == null) continue;
            currentInventory.addElement(nestedInventory);
        }
        logger.debug("Created {} with stack: {}, tag {}, elements: {}", (Object)currentInventory, (Object)currentStack, (Object)MiscUtil.getTag(currentStack), currentInventory.elements);
        return currentInventory;
    }

    public static VirtualInventory createInventory(Player owner, ItemStack mainStack) {
        return VirtualInventory.createInventory(owner, null, mainStack);
    }
}

