/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.Tuple
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.vicmatskiv.pointblank.util;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.crafting.PointBlankIngredient;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryUtils {
    public static Tuple<ItemStack, GunClientState> getItemStackByStateId(Player player, UUID gunStateId, int slotIndex) {
        if (player == null || gunStateId == null) {
            return null;
        }
        GunItem.OperableGunContext matched = null;
        GunItem.OperableGunContext main = GunItem.resolveMainHandGunContext(player);
        GunItem.OperableGunContext offhand = GunItem.resolveOffhandGunContext(player);
        if (main != null && Objects.equals(GunItem.getItemStackId(main.itemStack()), gunStateId)) {
            matched = main;
        }
        if (offhand != null && Objects.equals(GunItem.getItemStackId(offhand.itemStack()), gunStateId)) {
            if (matched != null) {
                return null;
            }
            matched = offhand;
        }
        if (matched == null) {
            return null;
        }
        GunClientState state = GunClientState.getState(player, matched.itemStack(), matched.slotIndex(), matched.offhand());
        return state != null ? new Tuple((Object)matched.itemStack(), (Object)state) : null;
    }

    public static boolean hasIngredient(Player player, PointBlankIngredient ingredient) {
        return player.getInventory().items.stream().filter(ingredient::matches).collect(Collectors.summarizingInt(ItemStack::getCount)).getSum() >= (long)ingredient.getCount();
    }

    public static boolean removeItem(Player player, Predicate<ItemStack> matchingPredicate, int count) {
        Inventory inventory = player.getInventory();
        int remainingCount = count;
        for (int i = 0; i < player.getInventory().items.size(); ++i) {
            ItemStack inventoryItem = (ItemStack)inventory.items.get(i);
            if (!matchingPredicate.test(inventoryItem)) continue;
            int availableCount = inventoryItem.getCount();
            if (availableCount <= remainingCount) {
                remainingCount -= availableCount;
                inventory.items.set(i, (Object)ItemStack.EMPTY);
            } else {
                inventoryItem.shrink(remainingCount);
                remainingCount = 0;
            }
            ((ServerPlayer)inventory.player).connection.send((Packet)new ClientboundContainerSetSlotPacket(-2, 0, i, inventory.getItem(i)));
            if (remainingCount == 0) break;
        }
        return remainingCount <= 0;
    }

    public static int addItem(Player player, Item item, int count) {
        int i;
        Inventory inventory = player.getInventory();
        ItemStack stackToAdd = new ItemStack((ItemLike)item, 1);
        int remainingCount = count;
        if (stackToAdd.isStackable()) {
            for (i = 0; i < inventory.items.size(); ++i) {
                ItemStack inventoryStack = (ItemStack)inventory.items.get(i);
                if (inventoryStack.getItem() != item || inventoryStack.getCount() >= inventoryStack.getMaxStackSize()) continue;
                int spaceLeft = inventoryStack.getMaxStackSize() - inventoryStack.getCount();
                int itemsToAdd = Math.min(spaceLeft, remainingCount);
                inventoryStack.grow(itemsToAdd);
                ((ServerPlayer)inventory.player).connection.send((Packet)new ClientboundContainerSetSlotPacket(-2, 0, i, inventory.getItem(i)));
                if ((remainingCount -= itemsToAdd) != 0) continue;
                return 0;
            }
        }
        for (i = 0; i < inventory.items.size(); ++i) {
            if (!inventory.getItem(i).isEmpty()) continue;
            if (stackToAdd.isStackable()) {
                int stackSize = Math.min(stackToAdd.getMaxStackSize(), remainingCount);
                inventory.setItem(i, new ItemStack((ItemLike)item, stackSize));
                ((ServerPlayer)inventory.player).connection.send((Packet)new ClientboundContainerSetSlotPacket(-2, 0, i, inventory.getItem(i)));
                remainingCount -= stackSize;
            } else {
                inventory.setItem(i, stackToAdd.copy());
                ((ServerPlayer)inventory.player).connection.send((Packet)new ClientboundContainerSetSlotPacket(-2, 0, i, inventory.getItem(i)));
                --remainingCount;
            }
            if (remainingCount != 0) continue;
            return 0;
        }
        return remainingCount;
    }
}

