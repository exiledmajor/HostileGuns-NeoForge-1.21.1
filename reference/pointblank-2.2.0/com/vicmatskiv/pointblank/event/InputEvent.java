/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CancellableEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import net.minecraft.world.entity.player.Player;

public class InputEvent
extends CancellableEvent {
    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public static class InteractionKeyMappingTriggered
    extends InputEvent {
        private final Player player;

        public InteractionKeyMappingTriggered(Player player) {
            this.player = player;
        }

        public Player getPlayer() {
            return this.player;
        }

        @Override
        public <T> T accept(EventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}

