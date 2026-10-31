/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.world.entity.player.Player
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.player.Player;

public abstract class TickEvent
implements CustomEvent {
    private Phase phase;

    public TickEvent(Phase phase) {
        this.phase = phase;
    }

    public Phase getPhase() {
        return this.phase;
    }

    public static enum Phase {
        START,
        END;

    }

    public static class PlayerTickEvent
    extends TickEvent {
        public final Player player;

        public PlayerTickEvent(Player player, Phase phase) {
            super(phase);
            this.player = player;
        }

        @Override
        public <T> T accept(EventVisitor<T> visitor) {
            return null;
        }

        public static class Post
        extends PlayerTickEvent {
            public Post(Player player) {
                super(player, Phase.END);
            }
        }

        public static class Pre
        extends PlayerTickEvent {
            public Pre(Player player) {
                super(player, Phase.START);
            }
        }
    }

    public static class ClientTickEvent
    extends TickEvent {
        public ClientTickEvent(Phase phase) {
            super(phase);
        }

        @Override
        public <T> T accept(EventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class RenderTickEvent
    extends TickEvent {
        private DeltaTracker timer;

        public RenderTickEvent(DeltaTracker timer, Phase phase) {
            super(phase);
            this.timer = timer;
        }

        public DeltaTracker getTimer() {
            return this.timer;
        }

        @Override
        public <T> T accept(EventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}

