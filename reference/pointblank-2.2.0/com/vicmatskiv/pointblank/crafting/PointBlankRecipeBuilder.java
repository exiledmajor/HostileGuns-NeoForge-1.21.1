/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.advancements.Advancement$Builder
 *  net.minecraft.advancements.AdvancementRequirements$Strategy
 *  net.minecraft.advancements.AdvancementRewards$Builder
 *  net.minecraft.advancements.Criterion
 *  net.minecraft.advancements.critereon.RecipeUnlockedTrigger
 *  net.minecraft.data.recipes.RecipeBuilder
 *  net.minecraft.data.recipes.RecipeCategory
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.level.ItemLike
 */
package com.vicmatskiv.pointblank.crafting;

import com.google.common.collect.Lists;
import com.vicmatskiv.pointblank.crafting.PointBlankIngredient;
import com.vicmatskiv.pointblank.crafting.PointBlankRecipe;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class PointBlankRecipeBuilder
implements RecipeBuilder {
    private final Item result;
    private final int count;
    private final List<PointBlankIngredient> ingredients = Lists.newArrayList();
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
    private String group = "misc";
    private RecipeSerializer<?> serializer;
    private RecipeCategory category = RecipeCategory.COMBAT;

    public PointBlankRecipeBuilder(RecipeSerializer<?> serializer, ItemLike item, int count) {
        this.serializer = serializer;
        this.result = item.asItem();
        this.count = count;
    }

    public PointBlankRecipeBuilder requires(TagKey<Item> tagKey, int count) {
        return this.requires(PointBlankIngredient.of(tagKey, count));
    }

    public PointBlankRecipeBuilder requires(ItemLike item, int count) {
        return this.requires(PointBlankIngredient.of(item, count));
    }

    public PointBlankRecipeBuilder requires(PointBlankIngredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    public RecipeBuilder unlockedBy(String p_176496_, Criterion<?> criterion) {
        this.advancement.addCriterion(p_176496_, criterion);
        return this;
    }

    public PointBlankRecipeBuilder group(String p_126195_) {
        this.group = p_126195_;
        return this;
    }

    public Item getResult() {
        return this.result;
    }

    public void save(RecipeOutput recipeOutput, ResourceLocation recipeId) {
        this.ensureValid(recipeId);
        Advancement.Builder advancementBuilder = this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked((ResourceLocation)recipeId)).rewards(AdvancementRewards.Builder.recipe((ResourceLocation)recipeId)).requirements(AdvancementRequirements.Strategy.OR);
        ItemStack itemStackResult = new ItemStack((ItemLike)this.result, this.count);
        PointBlankRecipe recipe = new PointBlankRecipe(this.group, itemStackResult, this.ingredients);
        recipeOutput.accept(recipeId, (Recipe)recipe, advancementBuilder.build(recipeId.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation p_126208_) {
    }
}

