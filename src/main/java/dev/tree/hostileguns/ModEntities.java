package dev.tree.hostileguns;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HostileGuns.MOD_ID);

    public static final Supplier<EntityType<HostileBulletEntity>> HOSTILE_BULLET =
            ENTITY_TYPES.register("hostile_bullet", () ->
                    EntityType.Builder.<HostileBulletEntity>of(HostileBulletEntity::new, MobCategory.MISC)
                            .sized(0.06F, 0.06F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("hostile_bullet"));

    private ModEntities() {}
}
