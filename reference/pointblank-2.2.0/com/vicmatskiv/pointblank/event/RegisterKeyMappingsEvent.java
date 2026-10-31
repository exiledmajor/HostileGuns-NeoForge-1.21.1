/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;

public class RegisterKeyMappingsEvent
implements CustomEvent {
    private final Consumer<KeyMapping> registry;

    public RegisterKeyMappingsEvent(Consumer<KeyMapping> registry) {
        this.registry = registry;
    }

    public void register(KeyMapping key) {
        this.registry.accept(key);
    }

    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

