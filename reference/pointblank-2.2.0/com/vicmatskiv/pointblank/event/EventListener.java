/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;

@FunctionalInterface
public interface EventListener {
    public void handleEvent(CustomEvent var1);
}

