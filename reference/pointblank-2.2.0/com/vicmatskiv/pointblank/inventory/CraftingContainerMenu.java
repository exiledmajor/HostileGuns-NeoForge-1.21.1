/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.searchtree.FullTextSearchTree
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.util.Mth
 *  net.minecraft.world.Container
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerData
 *  net.minecraft.world.inventory.SimpleContainerData
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.TooltipFlag$Default
 *  net.minecraft.world.level.Level
 */
package com.vicmatskiv.pointblank.inventory;

import com.vicmatskiv.pointblank.block.entity.PrinterBlockEntity;
import com.vicmatskiv.pointblank.crafting.PointBlankIngredient;
import com.vicmatskiv.pointblank.crafting.PointBlankRecipe;
import com.vicmatskiv.pointblank.event.MenuRegistry;
import com.vicmatskiv.pointblank.inventory.IngredientSlot;
import com.vicmatskiv.pointblank.inventory.SearchSlot;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.InventoryUtils;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class CraftingContainerMenu
extends AbstractContainerMenu {
    private static final int SEARCH_COLS = 6;
    private static final int SEARCH_ROWS = 7;
    private static final int INGREDIENT_COLS = 5;
    private static final int INGREDIENT_ROWS = 2;
    public static final SimpleContainer INGREDIENT_CONTAINER = new SimpleContainer(10);
    public static final SimpleContainer SEARCH_CONTAINER = new SimpleContainer(42);
    private final NonNullList<ItemStack> items = NonNullList.create();
    private final AbstractContainerMenu inventoryMenu;
    private List<ItemStack> displayItems = new ArrayList<ItemStack>();
    private PrinterBlockEntity craftingBlockEntity;
    private ContainerData containerData;
    private FullTextSearchTree<ItemStack> searchTree;
    private Player player;

    public CraftingContainerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, (ContainerData)new SimpleContainerData(2));
    }

    public CraftingContainerMenu(int containerId, Inventory playerInventory, PrinterBlockEntity craftingBlockEntity, ContainerData dataAccess) {
        super(MenuRegistry.CRAFTING.get(), containerId);
        Slot slot;
        int j;
        int i;
        this.craftingBlockEntity = craftingBlockEntity;
        this.containerData = dataAccess;
        this.player = playerInventory.player;
        this.inventoryMenu = playerInventory.player.inventoryMenu;
        this.addDataSlots(dataAccess);
        for (i = 0; i < 7; ++i) {
            for (j = 0; j < 6; ++j) {
                slot = new SearchSlot((Container)SEARCH_CONTAINER, i * 6 + j, 9 + j * 18, 18 + i * 18, true);
                this.addSlot(slot);
            }
        }
        for (i = 0; i < 2; ++i) {
            for (j = 0; j < 5; ++j) {
                slot = new IngredientSlot((Container)INGREDIENT_CONTAINER, i * 5 + j, 151 + j * 18, 108 + i * 18);
                this.addSlot(slot);
            }
        }
        for (Supplier<? extends Item> itemSupplier : ItemRegistry.ITEMS.getItemsByName().values()) {
            Item item = itemSupplier.get();
            PointBlankRecipe recipe = PointBlankRecipe.getRecipe(playerInventory.player.level(), item);
            if (recipe == null) continue;
            this.displayItems.add(recipe.getInitializedStack());
        }
        if (MiscUtil.isClientSide((Entity)playerInventory.player)) {
            this.searchTree = new FullTextSearchTree(itemStack -> itemStack.getTooltipLines(Item.TooltipContext.of((Level)ClientUtil.getClientLevel()), playerInventory.player, (TooltipFlag)TooltipFlag.Default.NORMAL.asCreative()).stream().map(component -> ChatFormatting.stripFormatting((String)component.getString()).trim()).filter(p_210809_ -> !p_210809_.isEmpty()), itemStack -> Stream.of(BuiltInRegistries.ITEM.getKey((Object)itemStack.getItem())), this.displayItems);
        }
        this.scrollTo(0.0f);
    }

    public PrinterBlockEntity getWorkstationBlockEntity() {
        return this.craftingBlockEntity;
    }

    public void setItem(int slotIndex, int stateId, ItemStack itemStack) {
        super.setItem(slotIndex, stateId, itemStack);
    }

    public void initializeContents(int stateId, List<ItemStack> itemStacks, ItemStack carriedStack) {
    }

    public boolean stillValid(Player player) {
        return true;
    }

    protected int calculateRowCount() {
        return Mth.positiveCeilDiv((int)this.items.size(), (int)6) - 7;
    }

    public int getRowIndexForScroll(float scroll) {
        return Math.max((int)((double)(scroll * (float)this.calculateRowCount()) + 0.5), 0);
    }

    public float getScrollForRowIndex(int rowIndex) {
        return Mth.clamp((float)((float)rowIndex / (float)this.calculateRowCount()), (float)0.0f, (float)1.0f);
    }

    public float subtractInputFromScroll(float scrolloffs, double scroll) {
        return Mth.clamp((float)(scrolloffs - (float)(scroll / (double)this.calculateRowCount())), (float)0.0f, (float)1.0f);
    }

    public void scrollTo(float scroll) {
        int i = this.getRowIndexForScroll(scroll);
        for (int j = 0; j < 7; ++j) {
            for (int k = 0; k < 6; ++k) {
                int l = k + (j + i) * 6;
                if (l >= 0 && l < this.items.size()) {
                    SEARCH_CONTAINER.setItem(k + j * 6, (ItemStack)this.items.get(l));
                    continue;
                }
                SEARCH_CONTAINER.setItem(k + j * 6, ItemStack.EMPTY);
            }
        }
    }

    public boolean canScroll() {
        return this.items.size() > 42;
    }

    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot;
        if (slotIndex >= this.slots.size() - 6 && slotIndex < this.slots.size() && (slot = (Slot)this.slots.get(slotIndex)) != null && slot.hasItem()) {
            slot.setByPlayer(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.container != SEARCH_CONTAINER;
    }

    public boolean canDragTo(Slot p_98653_) {
        return p_98653_.container != SEARCH_CONTAINER;
    }

    public ItemStack getCarried() {
        return this.inventoryMenu.getCarried();
    }

    public void setCarried(ItemStack itemStack) {
        this.inventoryMenu.setCarried(itemStack);
    }

    public void refreshSearchResults(String s) {
        this.items.clear();
        if (s.isEmpty()) {
            this.items.addAll(this.displayItems);
        } else {
            this.items.addAll((Collection)this.searchTree.search(s.toLowerCase(Locale.ROOT)));
        }
        this.scrollTo(0.0f);
    }

    public boolean updateIngredientSlots(PointBlankRecipe selectedItemRecipe) {
        boolean isCraftable = true;
        List<PointBlankIngredient> ingredients = selectedItemRecipe.getPointBlankIngredients();
        if (ingredients.size() <= INGREDIENT_CONTAINER.getContainerSize()) {
            int ingredientSlotOffset = SEARCH_CONTAINER.getContainerSize();
            for (int i = 0; i < ingredients.size(); ++i) {
                PointBlankIngredient ingredient = ingredients.get(i);
                IngredientSlot ingredientSlot = (IngredientSlot)((Object)this.slots.get(ingredientSlotOffset + i));
                boolean hasIngredient = InventoryUtils.hasIngredient(ClientUtil.getClientPlayer(), ingredient);
                ingredientSlot.setIngredient(ingredient, hasIngredient);
                isCraftable &= hasIngredient;
            }
        }
        return isCraftable;
    }

    public void clearIngredientSlots() {
        int ingredientSlotOffset = SEARCH_CONTAINER.getContainerSize();
        for (int i = 0; i < INGREDIENT_CONTAINER.getContainerSize(); ++i) {
            IngredientSlot ingredientSlot = (IngredientSlot)((Object)this.slots.get(ingredientSlotOffset + i));
            ingredientSlot.setIngredient(null, true);
        }
        INGREDIENT_CONTAINER.clearContent();
    }

    public boolean isCreativeSlot(Slot slot) {
        return slot != null && slot.container == SEARCH_CONTAINER;
    }

    public boolean isIdle() {
        return PrinterBlockEntity.State.values()[this.containerData.get(0)] == PrinterBlockEntity.State.IDLE;
    }

    public boolean isCrafting() {
        boolean result = false;
        PrinterBlockEntity.State state = PrinterBlockEntity.State.values()[this.containerData.get(0)];
        if (state == PrinterBlockEntity.State.CRAFTING) {
            Level level = MiscUtil.getLevel((Entity)this.player);
            int playerEntityId = this.containerData.get(1);
            if (playerEntityId >= 0) {
                result = this.player == level.getEntity(playerEntityId);
            }
        }
        return result;
    }
}

