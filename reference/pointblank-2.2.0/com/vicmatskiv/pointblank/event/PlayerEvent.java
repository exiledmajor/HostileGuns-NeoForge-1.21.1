/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.EventVisitor;
import com.vicmatskiv.pointblank.event.LivingEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public abstract class PlayerEvent
extends LivingEvent {
    private final Player player;

    public PlayerEvent(Player player) {
        super((LivingEntity)player);
        this.player = player;
    }

    public Player getEntity() {
        return this.player;
    }

    public static class PlayerRespawnEvent
    extends PlayerEvent {
        public PlayerRespawnEvent(Player player) {
            super(player);
        }

        @Override
        public <T> T accept(EventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class PlayerLoggedInEvent
    extends PlayerEvent {
        public PlayerLoggedInEvent(Player player) {
            super(player);
        }

        @Override
        public <T> T accept(EventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class PlayerChangedDimensionEvent
    extends PlayerEvent {
        public PlayerChangedDimensionEvent(Player player) {
            super(player);
        }

        @Override
        public <T> T accept(EventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class HarvestCheck
    extends PlayerEvent {
        private final BlockState state;
        private boolean success;

        public HarvestCheck(Player player, BlockState state, boolean success) {
            super(player);
            this.state = state;
            this.success = success;
        }

        public BlockState getTargetBlock() {
            return this.state;
        }

        public boolean canHarvest() {
            return this.success;
        }

        public void setCanHarvest(boolean success) {
            this.success = success;
        }

        @Override
        public <T> T accept(EventVisitor<T> visitor) {
            return null;
        }
    }
}

