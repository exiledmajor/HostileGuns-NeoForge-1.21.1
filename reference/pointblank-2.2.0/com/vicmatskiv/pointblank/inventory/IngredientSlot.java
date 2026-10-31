/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.inventory;

import com.vicmatskiv.pointblank.crafting.PointBlankIngredient;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class IngredientSlot
extends Slot {
    private static final int SHOW_ROLLING_INGREDIENT_DURATION_MILLIS = 800;
    private int slotIndex;
    private PointBlankIngredient ingredient;
    boolean isIngredientAvailable;

    public IngredientSlot(Container container, int slotIndex, int posX, int posY) {
        super(container, slotIndex, posX, posY);
        this.slotIndex = slotIndex;
    }

    public boolean isHighlightable() {
        return false;
    }

    public void setIngredient(PointBlankIngredient ingredient, boolean isIngredientAvailable) {
        ItemStack currentStack;
        this.ingredient = ingredient;
        this.isIngredientAvailable = isIngredientAvailable;
        List<Object> itemStacks = ingredient != null ? ingredient.getItemStacks() : Collections.emptyList();
        int itemStackCount = itemStacks.size();
        if (itemStackCount == 1) {
            currentStack = (ItemStack)itemStacks.get(0);
        } else if (itemStackCount > 0) {
            int index = (int)((long)((double)System.currentTimeMillis() / 800.0) % (long)itemStackCount);
            currentStack = (ItemStack)itemStacks.get(index);
        } else {
            currentStack = ItemStack.EMPTY;
        }
        this.set(currentStack);
    }

    public PointBlankIngredient getIngredient() {
        return this.ingredient;
    }

    public boolean mayPickup(Player player) {
        return false;
    }

    public boolean mayPlace(ItemStack itemStack) {
        return false;
    }

    public String toString() {
        return String.format("IngredientSlot {index: %d, container: %s}", this.slotIndex, System.identityHashCode(this.container));
    }

    public boolean isIngredientAvailable() {
        return this.isIngredientAvailable;
    }
}

