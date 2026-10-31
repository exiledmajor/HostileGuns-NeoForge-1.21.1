/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;

public class CancellableEvent
implements CustomEvent {
    private boolean isCancelled;

    public boolean isCanceled() {
        return this.isCancelled;
    }

    public void setCanceled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return null;
    }
}

