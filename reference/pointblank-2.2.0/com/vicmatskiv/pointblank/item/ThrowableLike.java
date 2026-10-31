/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.item;

import com.vicmatskiv.pointblank.client.ThrowableClientState;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ThrowableLike {
    public long getDrawCooldownDuration(LivingEntity var1, ThrowableClientState var2, ItemStack var3);

    public boolean hasIdleAnimations();

    public void requestThrowFromServer(ThrowableClientState var1, Player var2, ItemStack var3, Entity var4);

    public ThrowableClientState createState(UUID var1);

    public long getIdleCooldownDuration(LivingEntity var1, ThrowableClientState var2, ItemStack var3);

    public long getInspectCooldownDuration(LivingEntity var1, ThrowableClientState var2, ItemStack var3);

    public long getPrepareIdleCooldownDuration();

    public long getCompleteThrowCooldownDuration(LivingEntity var1, ThrowableClientState var2, ItemStack var3);

    public long getPrepareThrowCooldownDuration(LivingEntity var1, ThrowableClientState var2, ItemStack var3);

    public long getThrowCooldownDuration(LivingEntity var1, ThrowableClientState var2, ItemStack var3);

    public void prepareThrow(ThrowableClientState var1, Player var2, ItemStack var3, Entity var4);

    public void handleClientThrowRequest(ServerPlayer var1, UUID var2, int var3);
}

