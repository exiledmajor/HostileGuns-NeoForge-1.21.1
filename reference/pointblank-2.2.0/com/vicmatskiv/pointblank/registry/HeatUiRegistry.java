/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.registry.HeatUiDefinition;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HeatUiRegistry {
    private static final Map<String, HeatUiDefinition> DEFINITIONS = new LinkedHashMap<String, HeatUiDefinition>();

    private HeatUiRegistry() {
    }

    public static void reset() {
        DEFINITIONS.clear();
        HeatUiRegistry.register(HeatUiDefinition.createDefault());
    }

    public static void register(HeatUiDefinition definition) {
        DEFINITIONS.put(definition.id(), definition);
    }

    public static HeatUiDefinition get(String id) {
        HeatUiDefinition definition = id == null ? null : DEFINITIONS.get(id);
        return definition != null ? definition : DEFINITIONS.get("default");
    }

    public static Collection<HeatUiDefinition> values() {
        return Collections.unmodifiableCollection(DEFINITIONS.values());
    }
}

