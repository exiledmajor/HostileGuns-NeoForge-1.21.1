/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.item;

import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface ItemExtra {
    public static UUID getItemStackId(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ItemExtra)) {
            return null;
        }
        CompoundTag idTag = MiscUtil.getTag(itemStack);
        return MiscUtil.getTagId(idTag);
    }
}

