/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.data.recipes.RecipeProvider
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package com.vicmatskiv.pointblank.crafting;

import com.vicmatskiv.pointblank.crafting.PointBlankIngredient;
import com.vicmatskiv.pointblank.crafting.PointBlankRecipeBuilder;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.registry.GunRegistry;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import com.vicmatskiv.pointblank.registry.RecipeTypeRegistry;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class PointBlankRecipeProvider
extends RecipeProvider {
    public PointBlankRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    public void buildRecipes(RecipeOutput recipeOutput) {
        for (Map.Entry<String, Supplier<? extends Item>> e : ItemRegistry.ITEMS.getItemsByName().entrySet()) {
            Item item = e.getValue().get();
            if (item != GunRegistry.M4A1.get()) continue;
            int quantity = 0;
            PointBlankRecipeBuilder sampleBuilder = null;
            if (item instanceof GunItem) {
                quantity = 1;
                sampleBuilder = new PointBlankRecipeBuilder(RecipeTypeRegistry.DEFAULT_SERIALIZER.get(), (ItemLike)item, quantity).requires(PointBlankIngredient.of((ItemLike)Items.GOLD_INGOT, 1)).requires(PointBlankIngredient.of((ItemLike)Items.IRON_INGOT, 3));
            }
            if (sampleBuilder == null) continue;
            sampleBuilder.unlockedBy("has_iron", PointBlankRecipeProvider.has((ItemLike)Items.IRON_INGOT));
            sampleBuilder.save(recipeOutput);
        }
    }
}

