/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.registry.AmmoUiDefinition;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AmmoUiRegistry {
    private static final Map<String, AmmoUiDefinition> DEFINITIONS = new LinkedHashMap<String, AmmoUiDefinition>();

    private AmmoUiRegistry() {
    }

    public static void clear() {
        DEFINITIONS.clear();
    }

    public static void register(AmmoUiDefinition definition) {
        DEFINITIONS.put(definition.id(), definition);
    }

    public static AmmoUiDefinition get(String id) {
        return id == null ? null : DEFINITIONS.get(id);
    }

    public static Collection<AmmoUiDefinition> values() {
        return Collections.unmodifiableCollection(DEFINITIONS.values());
    }
}

