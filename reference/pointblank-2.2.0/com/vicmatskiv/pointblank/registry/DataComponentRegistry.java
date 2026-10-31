/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.component.DataComponentType$Builder
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.codec.ByteBufCodecs
 */
package com.vicmatskiv.pointblank.registry;

import com.mojang.serialization.Codec;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.RegistryService;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;

public class DataComponentRegistry {
    private static RegistryService<DataComponentType<?>> dataComponentTypeRegistry = Platform.getInstance().getDataComponentTypeRegistry();
    public static final Supplier<DataComponentType<Long>> TIMESTAMP_COMPONENT = DataComponentRegistry.registerDataComponent("ts", builder -> builder.persistent((Codec)Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));
    public static final Supplier<DataComponentType<CompoundTag>> CUSTOM_TAG_COMPONENT = DataComponentRegistry.registerDataComponent("custom_tag", builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    public static <T> Supplier<DataComponentType<T>> registerDataComponent(String id, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return dataComponentTypeRegistry.register(id, () -> ((DataComponentType.Builder)builder.apply(new DataComponentType.Builder())).build());
    }

    public static void init() {
    }
}

