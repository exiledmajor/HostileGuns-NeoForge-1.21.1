/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animation.AnimationController
 */
package com.vicmatskiv.pointblank.client;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.GunStateListener;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;

public class DynamicGeoListener
implements GunStateListener {
    private static Map<String, AnimationController<GeoAnimatable>> getControllers(LivingEntity entity, ItemStack itemStack) {
        ItemDisplayContext itemDisplayContext;
        GunItem gunItem = (GunItem)itemStack.getItem();
        if (entity instanceof Player) {
            Player player = (Player)entity;
            itemDisplayContext = GunItem.resolveFirstPersonDisplayContext(player, itemStack);
        } else {
            itemDisplayContext = null;
        }
        ItemDisplayContext perspective = itemDisplayContext;
        return gunItem.getGeoAnimationControllers(itemStack, perspective);
    }

    @Override
    public void onStartFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = DynamicGeoListener.getControllers(player, itemStack);
        for (AnimationController<GeoAnimatable> controller : controllers.values()) {
            if (!(controller instanceof GunStateListener)) continue;
            GunStateListener gunStateListener = (GunStateListener)controller;
            gunStateListener.onStartFiring(player, state, itemStack);
        }
    }

    @Override
    public void onStartReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = DynamicGeoListener.getControllers(player, itemStack);
        for (AnimationController<GeoAnimatable> controller : controllers.values()) {
            if (!(controller instanceof GunStateListener)) continue;
            GunStateListener gunStateListener = (GunStateListener)controller;
            gunStateListener.onStartReloading(player, state, itemStack);
        }
    }

    @Override
    public void onPrepareReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = DynamicGeoListener.getControllers(player, itemStack);
        for (AnimationController<GeoAnimatable> controller : controllers.values()) {
            if (!(controller instanceof GunStateListener)) continue;
            GunStateListener gunStateListener = (GunStateListener)controller;
            gunStateListener.onPrepareReloading(player, state, itemStack);
        }
    }

    @Override
    public void onCompleteReloading(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = DynamicGeoListener.getControllers(player, itemStack);
        for (AnimationController<GeoAnimatable> controller : controllers.values()) {
            if (!(controller instanceof GunStateListener)) continue;
            GunStateListener gunStateListener = (GunStateListener)controller;
            gunStateListener.onCompleteReloading(player, state, itemStack);
        }
    }

    @Override
    public void onPrepareIdle(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = DynamicGeoListener.getControllers(player, itemStack);
        for (AnimationController<GeoAnimatable> controller : controllers.values()) {
            if (!(controller instanceof GunStateListener)) continue;
            GunStateListener gunStateListener = (GunStateListener)controller;
            gunStateListener.onPrepareIdle(player, state, itemStack);
        }
    }

    @Override
    public void onIdle(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = DynamicGeoListener.getControllers(player, itemStack);
        for (AnimationController<GeoAnimatable> controller : controllers.values()) {
            if (!(controller instanceof GunStateListener)) continue;
            GunStateListener gunStateListener = (GunStateListener)controller;
            gunStateListener.onIdle(player, state, itemStack);
        }
    }

    @Override
    public void onDrawing(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = DynamicGeoListener.getControllers(player, itemStack);
        for (AnimationController<GeoAnimatable> controller : controllers.values()) {
            if (!(controller instanceof GunStateListener)) continue;
            GunStateListener gunStateListener = (GunStateListener)controller;
            gunStateListener.onDrawing(player, state, itemStack);
        }
    }

    @Override
    public void onHiding(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = DynamicGeoListener.getControllers(player, itemStack);
        for (AnimationController<GeoAnimatable> controller : controllers.values()) {
            if (!(controller instanceof GunStateListener)) continue;
            GunStateListener gunStateListener = (GunStateListener)controller;
            gunStateListener.onHiding(player, state, itemStack);
        }
    }

    @Override
    public void onInspecting(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = DynamicGeoListener.getControllers(player, itemStack);
        for (AnimationController<GeoAnimatable> controller : controllers.values()) {
            if (!(controller instanceof GunStateListener)) continue;
            GunStateListener gunStateListener = (GunStateListener)controller;
            gunStateListener.onInspecting(player, state, itemStack);
        }
    }

    @Override
    public void onPrepareFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = DynamicGeoListener.getControllers(player, itemStack);
        for (AnimationController<GeoAnimatable> controller : controllers.values()) {
            if (!(controller instanceof GunStateListener)) continue;
            GunStateListener gunStateListener = (GunStateListener)controller;
            gunStateListener.onPrepareFiring(player, state, itemStack);
        }
    }

    @Override
    public void onCompleteFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = DynamicGeoListener.getControllers(player, itemStack);
        for (AnimationController<GeoAnimatable> controller : controllers.values()) {
            if (!(controller instanceof GunStateListener)) continue;
            GunStateListener gunStateListener = (GunStateListener)controller;
            gunStateListener.onCompleteFiring(player, state, itemStack);
        }
    }

    @Override
    public void onOverheating(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = DynamicGeoListener.getControllers(player, itemStack);
        for (AnimationController<GeoAnimatable> controller : controllers.values()) {
            if (!(controller instanceof GunStateListener)) continue;
            GunStateListener gunStateListener = (GunStateListener)controller;
            gunStateListener.onOverheating(player, state, itemStack);
        }
    }

    @Override
    public void onEnablingFireMode(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Map<String, AnimationController<GeoAnimatable>> controllers = DynamicGeoListener.getControllers(player, itemStack);
        for (AnimationController<GeoAnimatable> controller : controllers.values()) {
            if (!(controller instanceof GunStateListener)) continue;
            GunStateListener gunStateListener = (GunStateListener)controller;
            gunStateListener.onEnablingFireMode(player, state, itemStack);
        }
    }
}

