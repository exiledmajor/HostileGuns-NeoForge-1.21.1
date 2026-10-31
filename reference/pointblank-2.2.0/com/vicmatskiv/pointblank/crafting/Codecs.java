/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponentPatch
 *  net.minecraft.util.ExtraCodecs
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.crafting;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class Codecs {
    static Function<?, Either<Holder<Item>, Holder<Item>>> getter;
    public static final Codec<ItemStack> ITEM_STACK_CODEC;
    public static final Codec<ItemStack> ITEM_STACK_CODEC2;

    static {
        ITEM_STACK_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(p_341560_ -> p_341560_.group((App)Codec.mapEither((MapCodec)ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id"), (MapCodec)ItemStack.ITEM_NON_AIR_CODEC.fieldOf("item")).forGetter(itemStack -> Either.left((Object)itemStack.getItemHolder())), (App)ExtraCodecs.intRange((int)1, (int)99).fieldOf("count").orElse((Object)1).forGetter(ItemStack::getCount), (App)DataComponentPatch.CODEC.optionalFieldOf("components", (Object)DataComponentPatch.EMPTY).forGetter(p_327171_ -> p_327171_.getComponentsPatch())).apply((Applicative)p_341560_, (eitherId, count, components) -> new ItemStack(eitherId.left().orElse(eitherId.right().orElse(null)), count.intValue(), components))));
        ITEM_STACK_CODEC2 = Codec.lazyInitialized(() -> RecordCodecBuilder.create(p_341560_ -> p_341560_.group((App)ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(itemStack -> itemStack.getItemHolder()), (App)ExtraCodecs.intRange((int)1, (int)99).fieldOf("count").orElse((Object)1).forGetter(ItemStack::getCount), (App)DataComponentPatch.CODEC.optionalFieldOf("components", (Object)DataComponentPatch.EMPTY).forGetter(p_327171_ -> p_327171_.getComponentsPatch())).apply((Applicative)p_341560_, (holder, count, components) -> new ItemStack(holder, count.intValue(), components))));
    }
}

