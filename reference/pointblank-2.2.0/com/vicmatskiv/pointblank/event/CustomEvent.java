/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.EventVisitor;

public interface CustomEvent {
    public <T> T accept(EventVisitor<T> var1);
}

