/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.ItemLike
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public final class GunRegistry {
    private static final List<String> GUN_ITEM_NAMES = List.of("glock17", "glock18", "m9", "m1911a1", "tti_viper", "p30l", "usp45", "deserteagle", "rhino", "m4a1", "m4a1mod1", "star15", "m4sopmodii", "m16a1", "hk416", "scarl", "xm7", "g36c", "g36k", "aug", "g41", "ak47", "ak74", "ak12", "an94", "ar57", "xm29", "mp5", "mp7", "ro635", "ump45", "vector", "p90", "m950", "tmp", "sl8", "mk14ebr", "uar10", "g3", "wa2000", "xm3", "c14", "l96a1", "ballista", "gm6lynx", "m590", "m870", "spas12", "m1014", "aa12", "citoricxs", "hs12", "aughbar", "lamg", "mk48", "m249", "m32mgl", "smaw", "at4", "javelin", "m134minigun");
    public static final Supplier<GunItem> M4A1 = ItemRegistry.ITEMS.getDeferredRegisteredObject("m4a1");

    private GunRegistry() {
    }

    public static void registerTabItems(Consumer<ItemLike> entries) {
        for (String gunItemName : GUN_ITEM_NAMES) {
            Supplier gunSupplier = ItemRegistry.ITEMS.getDeferredRegisteredObject(gunItemName);
            Item item = (Item)gunSupplier.get();
            if (item == null) continue;
            entries.accept((ItemLike)item);
        }
    }

    public static void init() {
    }
}

