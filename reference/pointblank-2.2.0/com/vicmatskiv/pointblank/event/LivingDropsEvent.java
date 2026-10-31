/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.LivingEvent;
import java.util.Collection;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

public class LivingDropsEvent
extends LivingEvent {
    private final Collection<ItemEntity> drops;

    public LivingDropsEvent(LivingEntity entity, Collection<ItemEntity> drops) {
        super(entity);
        this.drops = drops;
    }

    public Collection<ItemEntity> getDrops() {
        return this.drops;
    }
}

