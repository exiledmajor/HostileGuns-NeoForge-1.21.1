/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.RegistryService;
import com.vicmatskiv.pointblank.entity.EntityBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;

public final class EntityRegistry {
    private static RegistryService<EntityType<?>> entityTypeRegistry = Platform.getInstance().getEntityTypeRegistryService();
    private static List<EntityBuilder<?, ?>> entityBuilders = new ArrayList();
    private static Map<EntityKey, Supplier<EntityBuilder<?, ?>>> entityBuildersByNameType = new HashMap();
    private static Map<String, Supplier<EntityType<?>>> typesByName = new HashMap();
    private static Map<Supplier<EntityType<?>>, Supplier<EntityBuilder<?, ?>>> itemEntityBuilders = new HashMap();

    public static Supplier<EntityBuilder<?, ?>> getEntityBuilder(String name, EntityBuilder.EntityTypeExt type) {
        Supplier<EntityBuilder<?, ?>> supplier = entityBuildersByNameType.get(new EntityKey(name, type));
        if (supplier == null) {
            throw new IllegalArgumentException("Entity '" + name + "' of type '" + String.valueOf((Object)type) + "' not found");
        }
        return supplier;
    }

    public static List<EntityBuilder<?, ?>> getEntityBuilders() {
        return Collections.unmodifiableList(entityBuilders);
    }

    public static Supplier<EntityType<?>> getTypeByName(String name) {
        return typesByName.get(name);
    }

    public static Map<Supplier<EntityType<?>>, Supplier<EntityBuilder<?, ?>>> getItemEntityBuilders() {
        return Collections.unmodifiableMap(itemEntityBuilders);
    }

    public static Supplier<EntityType<?>> registerItemEntity(String name, Supplier<EntityBuilder<?, ?>> entityBuilderSupplier) {
        String entityTypeName = name;
        Supplier<EntityType> sup = () -> ((EntityBuilder)entityBuilderSupplier.get()).getEntityTypeBuilder().build(entityTypeName);
        Supplier<EntityType<?>> registeredEntityType = entityTypeRegistry.register(entityTypeName, sup);
        typesByName.put(entityTypeName, registeredEntityType);
        itemEntityBuilders.put(registeredEntityType, entityBuilderSupplier);
        return registeredEntityType;
    }

    public static void init() {
    }

    private static class EntityKey {
        private String name;
        private EntityBuilder.EntityTypeExt type;

        private EntityKey(String name, EntityBuilder.EntityTypeExt type) {
            this.name = name;
            this.type = type;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.name, this.type});
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            EntityKey other = (EntityKey)obj;
            return Objects.equals(this.name, other.name) && this.type == other.type;
        }
    }
}

