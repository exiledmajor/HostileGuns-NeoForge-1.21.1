/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.Util
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package com.vicmatskiv.pointblank.crafting;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vicmatskiv.pointblank.crafting.Codecs;
import com.vicmatskiv.pointblank.crafting.PointBlankIngredient;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.registry.RecipeTypeRegistry;
import com.vicmatskiv.pointblank.util.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class PointBlankRecipe
implements Recipe<CraftingInput> {
    private static final int MAX_INGREDIENTS = 10;
    private static Function<Level, Map<Item, RecipeHolder<PointBlankRecipe>>> levelRecipesByItem = Util.memoize(level -> level.getRecipeManager().getAllRecipesFor(RecipeTypeRegistry.DEFAULT_RECIPE_TYPE.get()).stream().collect(Collectors.toMap(recipeHolder -> ((PointBlankRecipe)recipeHolder.value()).getResultItem(null).getItem(), recipeHolder -> recipeHolder)));
    private static Function<Level, Map<ResourceLocation, PointBlankRecipe>> levelRecipesById = Util.memoize(level -> level.getRecipeManager().getAllRecipesFor(RecipeTypeRegistry.DEFAULT_RECIPE_TYPE.get()).stream().collect(Collectors.toMap(recipeHolder -> recipeHolder.id(), recipeHolder -> (PointBlankRecipe)recipeHolder.value())));
    private final String group;
    private final ItemStack result;
    private final ItemStack initializedItemStack;
    private final NonNullList<PointBlankIngredient> ingredients;
    private static NonNullList<Ingredient> defaultIngredients = NonNullList.of((Object)Ingredient.of((ItemLike[])new ItemLike[]{Items.IRON_INGOT}), (Object[])new Ingredient[0]);

    public static PointBlankRecipe getRecipe(Level level, Item item) {
        RecipeHolder<PointBlankRecipe> holder = levelRecipesByItem.apply(level).get(item);
        return holder != null ? (PointBlankRecipe)holder.value() : null;
    }

    public static PointBlankRecipe getRecipe(Level level, ResourceLocation recipeId) {
        return levelRecipesById.apply(level).get(recipeId);
    }

    public static List<PointBlankRecipe> getRecipes(Level level) {
        return new ArrayList<PointBlankRecipe>(levelRecipesById.apply(level).values());
    }

    public PointBlankRecipe(String group, ItemStack resultItemStack, List<PointBlankIngredient> ingredients) {
        this.group = group;
        this.result = resultItemStack;
        this.initializedItemStack = resultItemStack.copy();
        if (this.initializedItemStack.getItem() instanceof GunItem) {
            GunItem.initStackForCrafting(this.initializedItemStack);
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Recipe ingredients are not set for item " + String.valueOf(resultItemStack));
        }
        if (ingredients.size() > 10) {
            throw new IllegalArgumentException("Recipe ingredients for item " + String.valueOf(resultItemStack) + " exceed maximum allowed count of 10");
        }
        this.ingredients = NonNullList.create();
        this.ingredients.addAll(ingredients);
    }

    public ResourceLocation getId(Level level) {
        RecipeHolder<PointBlankRecipe> holder = levelRecipesByItem.apply(level).get(this.result.getItem());
        return holder != null ? holder.id() : null;
    }

    public RecipeSerializer<?> getSerializer() {
        return RecipeTypeRegistry.DEFAULT_SERIALIZER.get();
    }

    public String getGroup() {
        return this.group;
    }

    public ItemStack getResultItem(HolderLookup.Provider p_331967_) {
        return this.result.copy();
    }

    public ItemStack getInitializedStack() {
        return this.initializedItemStack;
    }

    public NonNullList<Ingredient> getIngredients() {
        return defaultIngredients;
    }

    public List<PointBlankIngredient> getPointBlankIngredients() {
        return this.ingredients;
    }

    public boolean matches(CraftingInput p_343697_, Level p_44003_) {
        throw new UnsupportedOperationException("Implement me!");
    }

    public boolean canBeCrafted(Player player) {
        return this.ingredients.stream().anyMatch(ingredient -> InventoryUtils.hasIngredient(player, ingredient));
    }

    public void removeIngredients(Player player) {
        this.ingredients.stream().forEach(ingredient -> InventoryUtils.removeItem(player, ingredient::matches, ingredient.getCount()));
    }

    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        return this.result.copy();
    }

    public boolean canCraftInDimensions(int m, int n) {
        return false;
    }

    public ItemStack assemble(CraftingInput p_343633_, HolderLookup.Provider p_332698_) {
        throw new UnsupportedOperationException("Implement me!");
    }

    public RecipeType<?> getType() {
        return RecipeTypeRegistry.DEFAULT_RECIPE_TYPE.get();
    }

    public static class Serializer
    implements RecipeSerializer<PointBlankRecipe> {
        public static final MapCodec<PointBlankRecipe> CODEC = RecordCodecBuilder.mapCodec(p_327208_ -> p_327208_.group((App)Codec.STRING.optionalFieldOf("group", (Object)"test").forGetter(recipe -> recipe.group), (App)Codecs.ITEM_STACK_CODEC.fieldOf("result").forGetter(recipe -> recipe.result), (App)PointBlankIngredient.CODEC.listOf().fieldOf("ingredients").flatXmap(ingredients -> {
            Object[] aingredient = (PointBlankIngredient[])ingredients.stream().toArray(PointBlankIngredient[]::new);
            if (aingredient.length == 0) {
                return DataResult.error(() -> "No ingredients for shapeless recipe");
            }
            return aingredient.length > 10 ? DataResult.error(() -> "Too many ingredients for shapeless recipe") : DataResult.success((Object)NonNullList.of((Object)PointBlankIngredient.EMPTY, (Object[])aingredient));
        }, DataResult::success).forGetter(p_298509_ -> p_298509_.ingredients)).apply((Applicative)p_327208_, PointBlankRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, PointBlankRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        private static PointBlankRecipe fromNetwork(RegistryFriendlyByteBuf byteBuf) {
            String recipeGroup = byteBuf.readUtf();
            int ingredientSize = byteBuf.readVarInt();
            NonNullList ingredients = NonNullList.create();
            for (int j = 0; j < ingredientSize; ++j) {
                ingredients.add((Object)PointBlankIngredient.fromNetwork(byteBuf));
            }
            ItemStack itemStack = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode((Object)byteBuf);
            return new PointBlankRecipe(recipeGroup, itemStack, (List<PointBlankIngredient>)ingredients);
        }

        private static void toNetwork(RegistryFriendlyByteBuf byteBuf, PointBlankRecipe recipe) {
            byteBuf.writeUtf(recipe.group);
            byteBuf.writeVarInt(recipe.ingredients.size());
            for (PointBlankIngredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(byteBuf);
            }
            ItemStack.OPTIONAL_STREAM_CODEC.encode((Object)byteBuf, (Object)recipe.result);
        }

        public MapCodec<PointBlankRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, PointBlankRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

