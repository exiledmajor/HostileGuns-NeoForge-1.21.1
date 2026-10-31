/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
 *  net.minecraft.client.renderer.entity.EntityRendererProvider
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public abstract class EntityRenderersEvent
implements CustomEvent {
    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public static class RegisterRenderers<T extends Entity, U extends BlockEntity>
    extends EntityRenderersEvent {
        private final BiConsumer<EntityType<T>, EntityRendererProvider<T>> entityRendererRegister;
        private final BiConsumer<BlockEntityType<U>, BlockEntityRendererProvider<U>> blockEntityRendererRegister;

        public RegisterRenderers(BiConsumer<EntityType<T>, EntityRendererProvider<T>> entityRendererRegister, BiConsumer<BlockEntityType<U>, BlockEntityRendererProvider<U>> blockEntityRendererRegister) {
            this.entityRendererRegister = entityRendererRegister;
            this.blockEntityRendererRegister = blockEntityRendererRegister;
        }

        public void registerEntityRenderer(EntityType<T> entityType, EntityRendererProvider<T> entityRendererProvider) {
            this.entityRendererRegister.accept(entityType, entityRendererProvider);
        }

        public void registerBlockEntityRenderer(BlockEntityType<U> blockEntityType, BlockEntityRendererProvider<U> blockEntityRendererProvider) {
            this.blockEntityRendererRegister.accept(blockEntityType, blockEntityRendererProvider);
        }
    }
}

