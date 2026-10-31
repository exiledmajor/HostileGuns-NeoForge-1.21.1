/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.attachment;

import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.attachment.Attachment;
import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.attachment.AttachmentHost;
import com.vicmatskiv.pointblank.feature.AimingFeature;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.item.AttachmentItem;
import com.vicmatskiv.pointblank.network.ServerBoundNextAttachmentPacket;
import com.vicmatskiv.pointblank.network.ServerBoundOpenScreenPacket;
import com.vicmatskiv.pointblank.util.LRUCache;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Attachments {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static final LRUCache<Pair<CompoundTag, Long>, NavigableMap<AttachmentCategory, String>> selectedAttachmentsCache = new LRUCache(100);
    private static final LRUCache<Pair<CompoundTag, Long>, NavigableMap<String, ItemStack>> tagAttachmentsCache = new LRUCache(100);
    private static final LRUCache<Pair<CompoundTag, Long>, NavigableMap<String, List<ItemStack>>> tagAttachmentGroupsCache = new LRUCache(100);
    private static final LRUCache<Pair<CompoundTag, Long>, NavigableMap<String, ItemStack>> recursiveAttachmentsCache = new LRUCache(100);
    public static final String ROOT_PATH = "/";

    public static void ensureValidAttachmentsSelected(ItemStack itemStack) {
        Attachments.ensureValidAttachmentSelected(itemStack, AttachmentCategory.SCOPE, AimingFeature.class);
    }

    private static void ensureValidAttachmentSelected(ItemStack itemStack, AttachmentCategory category, Class<? extends Feature> featureType) {
        FeatureProvider fp;
        Feature feature;
        Item item;
        boolean isValid = false;
        Pair<String, ItemStack> selected = Attachments.getSelectedAttachment(itemStack, category);
        if (selected != null && (item = ((ItemStack)selected.getSecond()).getItem()) instanceof FeatureProvider && (feature = (fp = (FeatureProvider)item).getFeature(featureType)) != null) {
            isValid = feature.isEnabled((ItemStack)selected.getSecond());
        }
        if (!isValid) {
            Attachments.selectNextAttachment(itemStack, category, featureType);
        }
    }

    public static Collection<ItemStack> getAttachments(ItemStack itemStack) {
        return Attachments.getAttachments(itemStack, false).values();
    }

    public static Pair<String, ItemStack> getSelectedAttachment(ItemStack rootItemStack, AttachmentCategory category) {
        NavigableMap<AttachmentCategory, String> selectedAttachments = Attachments.getSelectedAttachments(rootItemStack);
        String selectedAttachmentPath = (String)selectedAttachments.get(category);
        if (selectedAttachmentPath == null) {
            return null;
        }
        if (ROOT_PATH.equals(selectedAttachmentPath)) {
            return Pair.of((Object)selectedAttachmentPath, (Object)rootItemStack);
        }
        NavigableMap<String, ItemStack> allAttachments = Attachments.getAttachments(rootItemStack, true);
        ItemStack attachmentStack = (ItemStack)allAttachments.get(selectedAttachmentPath);
        if (attachmentStack == null) {
            return Pair.of((Object)selectedAttachmentPath, (Object)ItemStack.EMPTY);
        }
        return Pair.of((Object)selectedAttachmentPath, (Object)attachmentStack);
    }

    public static NavigableMap<AttachmentCategory, String> getSelectedAttachments(ItemStack itemStack) {
        if (itemStack == null || !(itemStack.getItem() instanceof AttachmentHost) || MiscUtil.getTag(itemStack) == null) {
            return Collections.emptyNavigableMap();
        }
        return selectedAttachmentsCache.computeIfAbsent(MiscUtil.getTagAndTimestamp(itemStack), p -> Attachments.getSelectedAttachments((CompoundTag)p.getFirst()));
    }

    private static NavigableMap<AttachmentCategory, String> getSelectedAttachments(CompoundTag tag) {
        if (tag != null && tag.contains("sa")) {
            CompoundTag attachmentsTag = tag.getCompound("sa");
            TreeMap<AttachmentCategory, String> attachments = new TreeMap<AttachmentCategory, String>();
            for (AttachmentCategory category : AttachmentCategory.values()) {
                if (!attachmentsTag.contains(category.getName())) continue;
                attachments.put(category, attachmentsTag.getString(category.getName()));
            }
            return attachments;
        }
        return Collections.emptyNavigableMap();
    }

    private static void saveSelectedAttachments(ItemStack rootItemStack, Map<AttachmentCategory, String> attachments) {
        CompoundTag nbt = MiscUtil.getOrCreateTag(rootItemStack);
        CompoundTag attachmentsTag = new CompoundTag();
        for (Map.Entry<AttachmentCategory, String> entry : attachments.entrySet()) {
            attachmentsTag.putString(entry.getKey().getName(), entry.getValue());
        }
        nbt.put("sa", (Tag)attachmentsTag);
        MiscUtil.setTag(rootItemStack, nbt);
    }

    public static Pair<String, ItemStack> selectNextAttachment(ItemStack rootItemStack, AttachmentCategory category, Class<? extends Feature> featureType) {
        FeatureProvider fp;
        Object feature;
        Item item = rootItemStack.getItem();
        if (!(item instanceof AttachmentHost)) {
            return null;
        }
        AttachmentHost attachmentHost = (AttachmentHost)item;
        if (attachmentHost.getCompatibleAttachments().isEmpty()) {
            return null;
        }
        NavigableMap<String, ItemStack> attachmentsForCategory = Attachments.getAttachmentsForCategory(rootItemStack, category);
        NavigableMap<AttachmentCategory, String> selectedAttachments = Attachments.getSelectedAttachments(rootItemStack);
        String activeAttachmentPath = (String)selectedAttachments.get(category);
        Map.Entry<String, Object> next = null;
        if (activeAttachmentPath != null) {
            for (Map.Entry entry : attachmentsForCategory.tailMap(activeAttachmentPath, false).entrySet()) {
                Item item2 = ((ItemStack)entry.getValue()).getItem();
                if (!(item2 instanceof FeatureProvider) || (feature = (fp = (FeatureProvider)item2).getFeature(featureType)) == null || !feature.isEnabled((ItemStack)entry.getValue())) continue;
                next = entry;
                break;
            }
        }
        if (next == null) {
            FeatureProvider fp2;
            Feature feature2;
            Item item3 = rootItemStack.getItem();
            if (item3 instanceof FeatureProvider && (feature2 = (fp2 = (FeatureProvider)item3).getFeature(featureType)) != null && feature2.isEnabled(rootItemStack)) {
                next = new AbstractMap.SimpleImmutableEntry<String, ItemStack>(ROOT_PATH, rootItemStack);
            }
            if (next == null) {
                for (Map.Entry entry : attachmentsForCategory.entrySet()) {
                    feature = ((ItemStack)entry.getValue()).getItem();
                    if (!(feature instanceof FeatureProvider) || (feature = (fp = (FeatureProvider)feature).getFeature(featureType)) == null || !feature.isEnabled((ItemStack)entry.getValue())) continue;
                    next = entry;
                    break;
                }
            }
        }
        selectedAttachments = new TreeMap<AttachmentCategory, String>((SortedMap<AttachmentCategory, String>)selectedAttachments);
        if (next != null) {
            selectedAttachments.put(category, (String)next.getKey());
        } else {
            selectedAttachments.remove(category);
        }
        LOGGER.debug("Active attachments for stack {} updated: {}", (Object)rootItemStack, selectedAttachments);
        Attachments.saveSelectedAttachments(rootItemStack, selectedAttachments);
        return next != null ? Pair.of((Object)((String)next.getKey()), (Object)((ItemStack)next.getValue())) : null;
    }

    public static NavigableMap<String, ItemStack> getAttachmentsForCategory(ItemStack itemStack, AttachmentCategory category) {
        NavigableMap<String, ItemStack> attachments = Attachments.getAttachments(itemStack, true);
        TreeMap<String, ItemStack> attachmentsForCategory = new TreeMap<String, ItemStack>();
        for (Map.Entry e : attachments.entrySet()) {
            Attachment attachment;
            ItemStack attachmentStack = (ItemStack)e.getValue();
            Item item = attachmentStack.getItem();
            if (!(item instanceof Attachment) || !Objects.equals(category, (attachment = (Attachment)item).getCategory())) continue;
            attachmentsForCategory.put((String)e.getKey(), (ItemStack)e.getValue());
        }
        return Collections.unmodifiableNavigableMap(attachmentsForCategory);
    }

    public static NavigableMap<String, List<ItemStack>> getAttachmentGroups(ItemStack itemStack) {
        if (itemStack == null || !(itemStack.getItem() instanceof AttachmentHost) || MiscUtil.getTag(itemStack) == null) {
            return Collections.emptyNavigableMap();
        }
        return tagAttachmentGroupsCache.computeIfAbsent(MiscUtil.getTagAndTimestamp(itemStack), p -> Attachments.getAttachmentGroups((CompoundTag)p.getFirst()));
    }

    private static NavigableMap<String, List<ItemStack>> getAttachmentGroups(CompoundTag tag) {
        NavigableMap<String, ItemStack> attachments = Attachments.getAttachments(tag, ROOT_PATH, false, new TreeMap<String, ItemStack>());
        TreeMap<String, List<ItemStack>> attachmentGroups = new TreeMap<String, List<ItemStack>>();
        for (Map.Entry e : attachments.entrySet()) {
            for (String group : ((Attachment)((ItemStack)e.getValue()).getItem()).getGroups()) {
                List attachmentList = attachmentGroups.computeIfAbsent(group, g -> new ArrayList());
                attachmentList.add((ItemStack)e.getValue());
            }
        }
        return attachmentGroups;
    }

    public static NavigableMap<String, ItemStack> getAttachments(ItemStack itemStack, boolean recursive) {
        if (itemStack == null || !(itemStack.getItem() instanceof AttachmentHost) || MiscUtil.getTag(itemStack) == null) {
            return Collections.emptyNavigableMap();
        }
        if (recursive) {
            return recursiveAttachmentsCache.computeIfAbsent(MiscUtil.getTagAndTimestamp(itemStack), p -> Attachments.getAttachments((CompoundTag)p.getFirst(), ROOT_PATH, true, new TreeMap<String, ItemStack>()));
        }
        return tagAttachmentsCache.computeIfAbsent(MiscUtil.getTagAndTimestamp(itemStack), p -> Attachments.getAttachments((CompoundTag)p.getFirst(), ROOT_PATH, false, new TreeMap<String, ItemStack>()));
    }

    static NavigableMap<String, ItemStack> getAttachments(CompoundTag itemTag, String parentPath, boolean recursive, NavigableMap<String, ItemStack> attachmentStacks) {
        if (itemTag != null && itemTag.contains("as", 9)) {
            ListTag attachmentsList = itemTag.getList("as", 10);
            for (int i = 0; i < attachmentsList.size(); ++i) {
                CompoundTag attachmentTag = attachmentsList.getCompound(i);
                String attachmentItemId = attachmentTag.getString("id");
                Item item = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.parse((String)attachmentItemId));
                if (!(item instanceof Attachment)) continue;
                Attachment attachment = (Attachment)item;
                ItemStack attachmentStack = new ItemStack((ItemLike)item);
                MiscUtil.setTag(attachmentStack, attachmentTag);
                String path = parentPath + ROOT_PATH + attachment.getName();
                attachmentStacks.put(path, attachmentStack);
                if (!recursive || !(item instanceof AttachmentHost)) continue;
                Attachments.getAttachments(attachmentTag, path, recursive, attachmentStacks);
            }
        }
        return attachmentStacks;
    }

    public static CompoundTag addAttachment(ItemStack containerStack, ItemStack attachmentStack, boolean isRemoveable) {
        String attachmentId = BuiltInRegistries.ITEM.getKey((Object)attachmentStack.getItem()).toString();
        Item item = attachmentStack.getItem();
        if (item instanceof AttachmentItem) {
            AttachmentItem attachmentItem = (AttachmentItem)item;
            attachmentItem.ensureItemStack(attachmentStack);
        }
        CompoundTag containerTag = MiscUtil.getOrCreateTag(containerStack);
        ListTag attachmentsList = containerTag.getList("as", 10);
        CompoundTag attachmentTag = MiscUtil.hasTag(attachmentStack) ? MiscUtil.copyTag(attachmentStack) : new CompoundTag();
        Attachments.initAttachmentTag(attachmentTag, attachmentId, isRemoveable);
        attachmentsList.add((Object)attachmentTag);
        containerTag.put("as", (Tag)attachmentsList);
        MiscUtil.setTag(containerStack, containerTag);
        return attachmentTag;
    }

    public static List<Attachment> removeAllAttachments(ItemStack containerStack) {
        CompoundTag containerTag = MiscUtil.getOrCreateTag(containerStack);
        ListTag attachmentsList1 = containerTag.getList("as", 10);
        ListTag removedList = attachmentsList1.copy();
        attachmentsList1.clear();
        ArrayList<Attachment> removedItems = new ArrayList<Attachment>();
        for (int i = 0; i < removedList.size(); ++i) {
            CompoundTag attachmentTag = removedList.getCompound(i);
            String attachmentItemId = attachmentTag.getString("id");
            Item item = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.parse((String)attachmentItemId));
            if (!(item instanceof Attachment)) continue;
            Attachment attachment = (Attachment)item;
            removedItems.add(attachment);
        }
        MiscUtil.setTag(containerStack, containerTag);
        return removedItems;
    }

    public static void initAttachmentTag(CompoundTag attachmentTag, String attachmentId, boolean isRemoveable) {
        attachmentTag.putString("id", attachmentId);
        attachmentTag.putBoolean("rmv", isRemoveable);
    }

    public static boolean isRemoveable(CompoundTag containerTag) {
        return true;
    }

    public static void tryAttachmentMode(Player player, ItemStack heldItem) {
        Platform.getInstance().getNetworkService().sendToServer(new ServerBoundOpenScreenPacket(ServerBoundOpenScreenPacket.ScreenType.ATTACHMENTS));
    }

    public static void tryNextAttachment(Player player, ItemStack heldItem, AttachmentCategory category, Class<? extends Feature> feature) {
        Platform.getInstance().getNetworkService().sendToServer(new ServerBoundNextAttachmentPacket(category, feature));
    }
}

