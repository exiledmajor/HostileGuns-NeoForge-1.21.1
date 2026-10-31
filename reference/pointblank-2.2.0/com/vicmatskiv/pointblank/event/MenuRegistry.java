/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.flag.FeatureFlags
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.MenuType$MenuSupplier
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.RegistryService;
import com.vicmatskiv.pointblank.inventory.AttachmentContainerMenu;
import com.vicmatskiv.pointblank.inventory.CraftingContainerMenu;
import java.util.function.Supplier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class MenuRegistry {
    private static RegistryService<MenuType<?>> menuTypeRegistry = Platform.getInstance().getMenuTypeRegistry();
    public static final Supplier<MenuType<CraftingContainerMenu>> CRAFTING = MenuRegistry.register("crafting", CraftingContainerMenu::new);
    public static final Supplier<MenuType<AttachmentContainerMenu>> ATTACHMENTS = MenuRegistry.register("attachments", AttachmentContainerMenu::new);

    private static <T extends AbstractContainerMenu> Supplier<MenuType<T>> register(String id, MenuType.MenuSupplier<T> factory) {
        return menuTypeRegistry.register(id, () -> new MenuType(factory, FeatureFlags.DEFAULT_FLAGS));
    }

    public static void init() {
    }
}

