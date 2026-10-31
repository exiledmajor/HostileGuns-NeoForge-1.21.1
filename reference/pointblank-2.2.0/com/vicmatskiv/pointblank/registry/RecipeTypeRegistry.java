/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.RegistryService;
import com.vicmatskiv.pointblank.crafting.PointBlankRecipe;
import java.util.function.Supplier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeTypeRegistry {
    private static RegistryService<RecipeType<?>> recipeTypeRegistryService = Platform.getInstance().getRecipeTypeRegistryService();
    private static RegistryService<RecipeSerializer<?>> recipeSeriailzerService = Platform.getInstance().getRecipeSerializerService();
    public static final Supplier<PointBlankRecipe.Serializer> DEFAULT_SERIALIZER = recipeSeriailzerService.register("default", () -> new PointBlankRecipe.Serializer());
    public static final Supplier<RecipeType<PointBlankRecipe>> DEFAULT_RECIPE_TYPE = recipeTypeRegistryService.register("default", () -> new RecipeType<PointBlankRecipe>(){

        public String toString() {
            return "default";
        }
    });

    public static void init() {
    }
}

