/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.BlockHitResult
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CancellableEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;

public class BlockHitEvent
extends CancellableEvent {
    private LivingEntity player;
    private BlockHitResult blockHitResult;
    private Entity projectile;

    public BlockHitEvent(LivingEntity player, BlockHitResult blockHitResult, Entity projectile) {
        this.player = player;
        this.blockHitResult = blockHitResult;
        this.projectile = projectile;
    }

    public LivingEntity getPlayer() {
        return this.player;
    }

    public BlockHitResult getBlockHitResult() {
        return this.blockHitResult;
    }

    public Entity getProjectile() {
        return this.projectile;
    }
}

