/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.feature;

import com.vicmatskiv.pointblank.client.GunClientState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record ConditionContext(LivingEntity player, ItemStack rootStack, ItemStack currentItemStack, GunClientState gunClientState, ItemDisplayContext itemDisplayContext, int randomSample1, int randomSample2) {
    public ConditionContext(LivingEntity player, ItemStack rootStack, ItemStack currentItemStack, GunClientState gunClientState, ItemDisplayContext itemDisplayContext) {
        this(player, rootStack, currentItemStack, gunClientState, itemDisplayContext, Integer.MAX_VALUE, 0);
    }

    public ConditionContext(LivingEntity player, ItemStack rootStack, GunClientState gunClientState, ItemDisplayContext itemDisplayContext) {
        this(player, rootStack, rootStack, gunClientState, itemDisplayContext, Integer.MAX_VALUE, 0);
    }

    public ConditionContext(LivingEntity player, GunClientState gunClientState) {
        this(player, null, gunClientState, null);
    }

    public ConditionContext(ItemStack itemStack) {
        this(null, itemStack, itemStack, null, null);
    }
}

