/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class RenderHandEvent
implements CustomEvent {
    private final InteractionHand hand;
    private final ItemStack itemStack;
    private final PoseStack poseStack;
    private final float partialTick;

    public RenderHandEvent(InteractionHand hand, ItemStack itemStack, PoseStack poseStack, float partialTick) {
        this.hand = hand;
        this.itemStack = itemStack;
        this.poseStack = poseStack;
        this.partialTick = partialTick;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

    public float getPartialTick() {
        return this.partialTick;
    }

    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

