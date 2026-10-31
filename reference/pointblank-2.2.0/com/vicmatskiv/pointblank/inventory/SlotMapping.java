/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.entity.player.Player
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.inventory;

import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.inventory.VirtualInventory;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SlotMapping {
    private static final int CURRENT_VERSION = 1;
    private static final long EXPIRATION_DURATION = 604800000L;
    public static final String TAG_SLOT_MAPPING = "pointblank:attachmentSlotMapping";
    private static final Logger logger = LogManager.getLogger((String)"pointblank");
    private Map<String, Map<Integer, AttachmentCategory>> mapping = new HashMap<String, Map<Integer, AttachmentCategory>>();
    private Player player;

    static SlotMapping getOrCreate(Player player) {
        SlotMapping slotMapping = new SlotMapping(player);
        slotMapping.loadSlotMapping();
        return slotMapping;
    }

    SlotMapping(Player player) {
        this.player = player;
    }

    Map<Integer, AttachmentCategory> getOrCreateSlotMapping(VirtualInventory virtualInventory) {
        String key = virtualInventory.getPath();
        Map<Integer, AttachmentCategory> result = null;
        if (key == null) {
            return null;
        }
        result = this.mapping.get(key);
        if (result != null && !virtualInventory.isValidSlotMapping(result)) {
            logger.debug("Invalid slot mapping found for key {} in inventory {}. Here it is: {}", (Object)key, (Object)virtualInventory, result);
            result = null;
        }
        if (result == null) {
            logger.debug("Creating a slot mapping for key {} in inventory {}", (Object)key, (Object)virtualInventory);
            result = virtualInventory.createSlotMapping(key);
            if (key != null) {
                this.mapping.put(key, result);
            }
            this.saveSlotMapping();
            logger.debug("Created a slot mapping for key {} in inventory {}. Here it is: {}", (Object)key, (Object)virtualInventory, result);
        }
        return result;
    }

    Map<Integer, AttachmentCategory> getStackSlotMapping(VirtualInventory virtualInventory) {
        return this.mapping.get(virtualInventory.getPath());
    }

    void saveSlotMapping() {
        CompoundTag persistentData = MiscUtil.getPersistentData(this.player);
        CompoundTag slotMappingTag = this.serializeSlotMapping();
        persistentData.put(TAG_SLOT_MAPPING, (Tag)slotMappingTag);
    }

    private void loadSlotMapping() {
        CompoundTag persistentData = MiscUtil.getPersistentData(this.player);
        CompoundTag slotMappingTag = persistentData.getCompound(TAG_SLOT_MAPPING);
        if (slotMappingTag != null) {
            this.deserializeStackIdSlotMapping(slotMappingTag);
        }
    }

    CompoundTag serializeSlotMapping() {
        CompoundTag rootTag = new CompoundTag();
        long currentTime = System.currentTimeMillis();
        rootTag.putInt("Version", 1);
        rootTag.putLong("Timestamp", currentTime);
        ListTag mappingsList = new ListTag();
        for (Map.Entry<String, Map<Integer, AttachmentCategory>> entry : this.mapping.entrySet()) {
            CompoundTag mappingTag = new CompoundTag();
            mappingTag.putString("id", entry.getKey());
            mappingTag.putLong("CreationTime", currentTime);
            CompoundTag slotMappingsTag = new CompoundTag();
            for (Map.Entry<Integer, AttachmentCategory> slotEntry : entry.getValue().entrySet()) {
                slotMappingsTag.putString(slotEntry.getKey().toString(), slotEntry.getValue().getName());
            }
            mappingTag.put("SlotMappings", (Tag)slotMappingsTag);
            mappingsList.add((Object)mappingTag);
        }
        rootTag.put("Mappings", (Tag)mappingsList);
        return rootTag;
    }

    void deserializeStackIdSlotMapping(CompoundTag rootTag) {
        int storedVersion = rootTag.getInt("Version");
        long storedTimestamp = rootTag.getLong("Timestamp");
        long currentTime = System.currentTimeMillis();
        if (storedVersion != 1 || currentTime - storedTimestamp > 604800000L) {
            logger.warn("Skipping loading of outdated or expired slot mappings.");
            return;
        }
        this.mapping.clear();
        ListTag mappingsList = rootTag.getList("Mappings", 10);
        for (int i = 0; i < mappingsList.size(); ++i) {
            CompoundTag mappingTag = mappingsList.getCompound(i);
            long creationTime = mappingTag.getLong("CreationTime");
            if (currentTime - creationTime > 604800000L) continue;
            String stackId = mappingTag.getString("id");
            CompoundTag slotMappingsTag = mappingTag.getCompound("SlotMappings");
            HashMap<Integer, AttachmentCategory> slotMappings = new HashMap<Integer, AttachmentCategory>();
            for (String slotKey : slotMappingsTag.getAllKeys()) {
                String slotValue = slotMappingsTag.getString(slotKey);
                slotMappings.put(Integer.parseInt(slotKey), AttachmentCategory.fromString(slotValue));
            }
            this.mapping.put(stackId, slotMappings);
        }
    }
}

