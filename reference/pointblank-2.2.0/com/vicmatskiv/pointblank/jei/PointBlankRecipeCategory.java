/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Lighting
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  mezz.jei.api.constants.VanillaTypes
 *  mezz.jei.api.gui.builder.IRecipeLayoutBuilder
 *  mezz.jei.api.gui.drawable.IDrawable
 *  mezz.jei.api.gui.drawable.IDrawableStatic
 *  mezz.jei.api.gui.ingredient.IRecipeSlotsView
 *  mezz.jei.api.helpers.IGuiHelper
 *  mezz.jei.api.ingredients.IIngredientType
 *  mezz.jei.api.recipe.IFocusGroup
 *  mezz.jei.api.recipe.RecipeIngredientRole
 *  mezz.jei.api.recipe.RecipeType
 *  mezz.jei.api.recipe.category.IRecipeCategory
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package com.vicmatskiv.pointblank.jei;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vicmatskiv.pointblank.crafting.PointBlankIngredient;
import com.vicmatskiv.pointblank.crafting.PointBlankRecipe;
import com.vicmatskiv.pointblank.jei.PointBlankJeiPlugin;
import com.vicmatskiv.pointblank.registry.BlockRegistry;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public class PointBlankRecipeCategory
implements IRecipeCategory<PointBlankRecipe> {
    public static final ResourceLocation BACKGROUND_RESOURCE = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/gui/jei.png");
    private IDrawableStatic backgroundDrawable;
    private IDrawableStatic itemDrawable;
    private IDrawableStatic inventoryDrawable;
    private IDrawable icon;
    private Component title;

    public PointBlankRecipeCategory(IGuiHelper gui) {
        this.backgroundDrawable = gui.createBlankDrawable(180, 124);
        this.itemDrawable = gui.createDrawable(BACKGROUND_RESOURCE, 2, 2, 180, 102);
        this.inventoryDrawable = gui.createDrawable(BACKGROUND_RESOURCE, 2, 108, 180, 19);
        this.icon = gui.createDrawableIngredient((IIngredientType)VanillaTypes.ITEM_STACK, (Object)new ItemStack((ItemLike)BlockRegistry.PRINTER.get()));
        this.title = Component.translatable((String)"block.pointblank.printer");
    }

    public RecipeType<PointBlankRecipe> getRecipeType() {
        return PointBlankJeiPlugin.POINTBLANK_RECIPE_TYPE;
    }

    public Component getTitle() {
        return this.title;
    }

    public IDrawable getBackground() {
        return this.backgroundDrawable;
    }

    public IDrawable getIcon() {
        return this.icon;
    }

    public void setRecipe(IRecipeLayoutBuilder builder, PointBlankRecipe recipe, IFocusGroup focuses) {
        ItemStack resultStack = recipe.getResultItem(null);
        List<PointBlankIngredient> pbi = recipe.getPointBlankIngredients();
        for (int i = 0; i < pbi.size(); ++i) {
            builder.addSlot(RecipeIngredientRole.INPUT, i * 18 + 1, 106).addIngredients((IIngredientType)VanillaTypes.ITEM_STACK, pbi.get(i).getItemStacks());
        }
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(resultStack);
    }

    public void draw(PointBlankRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        this.itemDrawable.draw(guiGraphics, 0, 0);
        this.inventoryDrawable.draw(guiGraphics, 0, this.itemDrawable.getHeight() + 3);
        Font font = mc.font;
        ItemStack itemStack = recipe.getInitializedStack();
        MutableComponent displayName = itemStack.getHoverName().copy();
        if (itemStack.getCount() > 1) {
            displayName.append((Component)Component.literal((String)(" x " + itemStack.getCount())));
        }
        int offsetX = this.itemDrawable.getWidth() >> 1;
        guiGraphics.drawCenteredString(font, (Component)displayName, offsetX, 4, -256);
        Matrix4fStack poseStack = RenderSystem.getModelViewStack();
        float fullTick = (float)mc.player.tickCount + mc.getTimer().getGameTimeDeltaPartialTick(true);
        float yOffset = Mth.cos((float)(fullTick * (float)Math.PI * 0.02f)) * 2.0f;
        poseStack.pushMatrix();
        poseStack.mul((Matrix4fc)guiGraphics.pose().last().pose());
        poseStack.translate(90.0f, 55.0f + yOffset, 1000.0f);
        poseStack.rotate((Quaternionfc)new Quaternionf().rotationXYZ(-0.34906584f, fullTick * ((float)Math.PI / 180) * 2.0f, 0.0f));
        poseStack.scale(60.0f, -60.0f, 60.0f);
        RenderSystem.applyModelViewMatrix();
        BakedModel model = mc.getItemRenderer().getModel(itemStack, MiscUtil.getLevel((Entity)mc.player), (LivingEntity)mc.player, mc.player.getId() + ItemDisplayContext.GROUND.ordinal());
        Lighting.setupFor3DItems();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        mc.getItemRenderer().render(itemStack, ItemDisplayContext.GROUND, false, new PoseStack(), (MultiBufferSource)buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, model);
        buffer.endBatch();
        poseStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }
}

