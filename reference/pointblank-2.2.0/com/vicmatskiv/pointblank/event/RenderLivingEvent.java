/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.world.entity.LivingEntity
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

public abstract class RenderLivingEvent<T extends LivingEntity, M extends EntityModel<T>>
implements CustomEvent {
    private final LivingEntity entity;
    private LivingEntityRenderer<T, M> renderer;

    public RenderLivingEvent(LivingEntity entity, LivingEntityRenderer<T, M> renderer) {
        this.entity = entity;
        this.renderer = renderer;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public LivingEntityRenderer<T, M> getRenderer() {
        return this.renderer;
    }

    public static class Post<T extends LivingEntity, M extends EntityModel<T>>
    extends RenderLivingEvent<T, M> {
        public Post(LivingEntity entity, LivingEntityRenderer<T, M> renderer) {
            super(entity, renderer);
        }

        public <R> R accept(EventVisitor<R> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Pre<T extends LivingEntity, M extends EntityModel<T>>
    extends RenderLivingEvent<T, M> {
        public Pre(LivingEntity entity, LivingEntityRenderer<T, M> renderer) {
            super(entity, renderer);
        }

        public <R> R accept(EventVisitor<R> visitor) {
            return visitor.visit(this);
        }
    }
}

