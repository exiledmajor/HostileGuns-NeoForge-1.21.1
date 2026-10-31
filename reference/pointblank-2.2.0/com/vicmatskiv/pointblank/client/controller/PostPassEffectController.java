/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.client.controller;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.controller.AbstractProceduralAnimationController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PostPassEffectController
extends AbstractProceduralAnimationController {
    public PostPassEffectController(long duration) {
        super(duration);
    }

    @Override
    public void reset() {
        super.reset();
        this.nanoDuration = 300000000L;
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.loadEffect(ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"shaders/post/ripple.json"));
    }

    @Override
    public void onRenderTick(LivingEntity player, GunClientState state, ItemStack itemStack, ItemDisplayContext itemDisplayContext, float partialTicks) {
        super.onRenderTick(player, state, itemStack, itemDisplayContext, partialTicks);
        if (this.isDone) {
            Minecraft mc = Minecraft.getInstance();
            PostChain postChain = mc.gameRenderer.currentEffect();
            if (postChain != null && postChain.getName().startsWith("pointblank:")) {
                mc.gameRenderer.shutdownEffect();
            }
        }
    }

    @Override
    public void onGameTick(LivingEntity player, GunClientState gunClientState) {
    }

    public double getProgress() {
        return super.getProgress(null, 0.0f);
    }
}

