/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import net.minecraft.world.entity.LivingEntity;

public class LivingEvent
implements CustomEvent {
    private LivingEntity entity;

    public LivingEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return null;
    }

    public static class LivingJumpEvent
    extends LivingEvent {
        public LivingJumpEvent(LivingEntity entity) {
            super(entity);
        }
    }
}

