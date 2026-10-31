/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.LevelAccessor
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import net.minecraft.world.level.LevelAccessor;

public class LevelEvent
implements CustomEvent {
    private final LevelAccessor level;

    public LevelEvent(LevelAccessor level) {
        this.level = level;
    }

    public LevelAccessor getLevel() {
        return this.level;
    }

    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return null;
    }

    public static class Load
    extends LevelEvent {
        public Load(LevelAccessor level) {
            super(level);
        }
    }
}

