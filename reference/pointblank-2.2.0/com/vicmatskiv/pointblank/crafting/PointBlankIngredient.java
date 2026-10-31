/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.ResourceLocationException
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.util.ExtraCodecs
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.vicmatskiv.pointblank.crafting;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vicmatskiv.pointblank.Platform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface PointBlankIngredient {
    public static final PointBlankIngredient EMPTY = new PointBlankIngredient(){

        @Override
        public List<ItemStack> getItemStacks() {
            return Collections.emptyList();
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf byteBuf) {
        }
    };
    public static final Codec<PointBlankIngredient> CODEC = Codec.xor(ItemIngredient.CODEC, TagIngredient.CODEC).xmap(p_300070_ -> (PointBlankIngredient)p_300070_.map(p_301348_ -> p_301348_, p_298354_ -> p_298354_), p_299608_ -> {
        if (p_299608_ instanceof TagIngredient) {
            TagIngredient tagIngredient = (TagIngredient)p_299608_;
            return Either.right((Object)tagIngredient);
        }
        if (p_299608_ instanceof ItemIngredient) {
            ItemIngredient itemIngredient = (ItemIngredient)p_299608_;
            return Either.left((Object)itemIngredient);
        }
        throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
    });

    public List<ItemStack> getItemStacks();

    public int getCount();

    default public boolean matches(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        return this.getItemStacks().stream().anyMatch(ingredientStack -> ingredientStack.is(itemStack.getItem()));
    }

    public void toNetwork(RegistryFriendlyByteBuf var1);

    public static PointBlankIngredient of(TagKey<Item> tagKey, int count) {
        return new TagIngredient(tagKey, count);
    }

    public static PointBlankIngredient of(ItemLike itemLike, int count) {
        return new ItemIngredient(itemLike, count);
    }

    public static PointBlankIngredient fromNetwork(RegistryFriendlyByteBuf byteBuf) {
        boolean isItem = byteBuf.readBoolean();
        if (isItem) {
            return ItemIngredient.fromNetwork(byteBuf);
        }
        return TagIngredient.fromNetwork((FriendlyByteBuf)byteBuf);
    }

    public static class TagIngredient
    implements PointBlankIngredient {
        private static final Codec<ResourceLocation> RESOURCE_LOCATION_CODEC = Codec.STRING.comapFlatMap(TagIngredient::readResourceLocation, ResourceLocation::toString).stable();
        static final Codec<TagIngredient> CODEC = RecordCodecBuilder.create(p_300241_ -> p_300241_.group((App)TagIngredient.TAG_KEY_CODEC(Registries.ITEM).fieldOf("tag").forGetter(ii -> ii.tagKey), (App)ExtraCodecs.intRange((int)1, (int)99).fieldOf("count").orElse((Object)1).forGetter(TagIngredient::getCount)).apply((Applicative)p_300241_, TagIngredient::new));
        private final TagKey<Item> tagKey;
        private final int count;
        private final List<ItemStack> itemStacks;

        private static DataResult<ResourceLocation> readResourceLocation(String s) {
            try {
                return DataResult.success((Object)Platform.getInstance().parseRecipeResourceLocation(s));
            }
            catch (ResourceLocationException var2) {
                return DataResult.error(() -> "Not a valid resource location: " + s + " " + var2.getMessage());
            }
        }

        public static <T> Codec<TagKey<T>> TAG_KEY_CODEC(ResourceKey<? extends Registry<T>> resourceKey) {
            return RESOURCE_LOCATION_CODEC.xmap(resourceLocation -> TagKey.create((ResourceKey)resourceKey, (ResourceLocation)resourceLocation), TagKey::location);
        }

        private TagIngredient(TagKey<Item> tagKey, int count) {
            this.tagKey = tagKey;
            this.count = count;
            this.itemStacks = new ArrayList<ItemStack>();
        }

        @Override
        public int getCount() {
            return this.count;
        }

        @Override
        public List<ItemStack> getItemStacks() {
            if (this.itemStacks.isEmpty()) {
                for (Holder holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tagKey)) {
                    this.itemStacks.add(new ItemStack((ItemLike)holder.value(), this.count));
                }
            }
            return this.itemStacks;
        }

        @Override
        public final void toNetwork(RegistryFriendlyByteBuf byteBuf) {
            byteBuf.writeBoolean(false);
            byteBuf.writeResourceLocation(this.tagKey.location());
            byteBuf.writeInt(this.count);
        }

        public static PointBlankIngredient fromNetwork(FriendlyByteBuf byteBuf) {
            TagKey tagKey = TagKey.create((ResourceKey)Registries.ITEM, (ResourceLocation)byteBuf.readResourceLocation());
            int count = byteBuf.readInt();
            return new TagIngredient((TagKey<Item>)tagKey, count);
        }
    }

    public static class ItemIngredient
    implements PointBlankIngredient {
        static final Codec<ItemIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ItemStack.ITEM_NON_AIR_CODEC.fieldOf("item").forGetter(ii -> Holder.direct((Object)ii.getItem())), (App)ExtraCodecs.intRange((int)1, (int)99).fieldOf("count").orElse((Object)1).forGetter(ii -> ii.getCount())).apply((Applicative)instance, (holder, count) -> new ItemIngredient((ItemLike)holder.value(), (int)count)));
        private ItemStack itemStack;

        public ItemIngredient(ItemLike itemLike, int count) {
            this.itemStack = new ItemStack(itemLike, count);
        }

        public Item getItem() {
            return this.itemStack.getItem();
        }

        @Override
        public int getCount() {
            return this.itemStack.getCount();
        }

        @Override
        public List<ItemStack> getItemStacks() {
            return Collections.singletonList(this.itemStack);
        }

        @Override
        public final void toNetwork(RegistryFriendlyByteBuf byteBuf) {
            byteBuf.writeBoolean(true);
            ItemStack.OPTIONAL_STREAM_CODEC.encode((Object)byteBuf, (Object)this.itemStack);
        }

        public static PointBlankIngredient fromNetwork(RegistryFriendlyByteBuf byteBuf) {
            ItemStack itemStack = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode((Object)byteBuf);
            return new ItemIngredient((ItemLike)itemStack.getItem(), itemStack.getCount());
        }
    }
}

