/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.HitResult
 */
package com.vicmatskiv.pointblank.client;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.render.RenderListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public interface GunStateListener
extends RenderListener {
    default public void onStateTick(LivingEntity player, GunClientState state) {
    }

    default public void onUpdateState(LivingEntity player, GunClientState state) {
    }

    @Override
    default public void onRenderTick(LivingEntity player, GunClientState state, ItemStack itemStack, ItemDisplayContext context, float partialTicks) {
    }

    default public void onGameTick(LivingEntity player, GunClientState gunClientState) {
    }

    default public void onStartFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
    }

    default public void onStopFiring() {
    }

    default public void onStartReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
    }

    default public void onPrepareReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
    }

    default public void onCompleteReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
    }

    default public void onToggleAiming(boolean isAiming) {
    }

    default public void onDrawing(LivingEntity player, GunClientState state, ItemStack itemStack) {
    }

    default public void onHiding(LivingEntity player, GunClientState state, ItemStack itemStack) {
    }

    default public void onInspecting(LivingEntity player, GunClientState state, ItemStack itemStack) {
    }

    default public void onJumping(LivingEntity player, GunClientState state, ItemStack itemStack) {
    }

    default public void onStartAutoFiring(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
    }

    default public void onStopAutoFiring(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
    }

    default public void onPrepareFiring(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
    }

    default public void onCompleteFiring(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
    }

    default public void onOverheating(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
    }

    default public void onHitScanTargetAcquired(LivingEntity player, GunClientState gunClientState, ItemStack itemStack, HitResult hitResult) {
    }

    default public void onHitScanTargetConfirmed(LivingEntity player, GunClientState gunClientState, ItemStack itemStack, HitResult hitResult, float damage) {
    }

    default public void onPrepareIdle(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
    }

    default public void onIdle(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
    }

    default public void onEnablingFireMode(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
    }
}

