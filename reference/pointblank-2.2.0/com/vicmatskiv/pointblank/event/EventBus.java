/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventListener;

public interface EventBus {
    public void register(Object var1);

    public void unregister(Object var1);

    public <T extends CustomEvent> void addListener(Class<T> var1, EventListener var2);

    public <T extends CustomEvent> void removeListener(Class<T> var1, EventListener var2);

    public void postEvent(CustomEvent var1);
}

