/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.level.ItemLike
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.client.render.DefaultModelRenderer;
import com.vicmatskiv.pointblank.item.MiscItem;
import com.vicmatskiv.pointblank.item.PrinterItem;
import com.vicmatskiv.pointblank.item.WorkstationItem;
import com.vicmatskiv.pointblank.registry.BlockRegistry;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class MiscItemRegistry {
    public static final Supplier<Item> PROCESSOR = ItemRegistry.ITEMS.register(new MiscItem.MiscItemBuilder().withName("processor").withRenderer(() -> new DefaultModelRenderer("processor")).withTradePrice(100.0, 1));
    public static final Supplier<Item> GUNINTERNALS = ItemRegistry.ITEMS.register(new MiscItem.MiscItemBuilder().withName("guninternals").withRenderer(() -> new DefaultModelRenderer("guninternals")).withTradePrice(500.0, 2));
    public static final Supplier<Item> MOTOR = ItemRegistry.ITEMS.register(new MiscItem.MiscItemBuilder().withName("motor").withRenderer(() -> new DefaultModelRenderer("motor")).withTradePrice(30000.0, 2));
    public static final Supplier<Item> GUNMETAL_MESH = ItemRegistry.itemRegistry.register("gunmetal_mesh", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> GUNMETAL_INGOT = ItemRegistry.itemRegistry.register("gunmetal_ingot", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> GUNMETAL_NUGGET = ItemRegistry.itemRegistry.register("gunmetal_nugget", () -> new Item(new Item.Properties()));
    public static final Supplier<BlockItem> WORKSTATION = ItemRegistry.itemRegistry.register("workstation", () -> new WorkstationItem(BlockRegistry.WORKSTATION.get(), new Item.Properties()));
    public static final Supplier<BlockItem> PRINTER = ItemRegistry.itemRegistry.register("printer", () -> new PrinterItem(BlockRegistry.PRINTER.get(), new Item.Properties()));

    public static void init() {
    }

    public static void registerTabItems(Consumer<ItemLike> entries) {
        entries.accept((ItemLike)PROCESSOR.get());
        entries.accept((ItemLike)GUNINTERNALS.get());
        entries.accept((ItemLike)MOTOR.get());
        entries.accept((ItemLike)GUNMETAL_MESH.get());
        entries.accept((ItemLike)GUNMETAL_INGOT.get());
        entries.accept((ItemLike)GUNMETAL_NUGGET.get());
        entries.accept((ItemLike)WORKSTATION.get());
        entries.accept((ItemLike)PRINTER.get());
    }
}

