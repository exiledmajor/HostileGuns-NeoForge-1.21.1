/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CancellableEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class LivingSwapItemsEvent
extends CancellableEvent {
    private LivingEntity entity;

    public LivingSwapItemsEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public static class Hands
    extends LivingSwapItemsEvent {
        private ItemStack toMainHand;
        private ItemStack toOffHand;

        public Hands(LivingEntity entity) {
            super(entity);
            this.toMainHand = entity.getOffhandItem();
            this.toOffHand = entity.getMainHandItem();
        }

        public ItemStack getItemSwappedToMainHand() {
            return this.toMainHand;
        }

        public ItemStack getItemSwappedToOffHand() {
            return this.toOffHand;
        }

        public void setItemSwappedToMainHand(ItemStack item) {
            this.toMainHand = item;
        }

        public void setItemSwappedToOffHand(ItemStack item) {
            this.toOffHand = item;
        }
    }
}

