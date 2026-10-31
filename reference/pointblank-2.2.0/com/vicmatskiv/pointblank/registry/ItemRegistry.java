/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.CreativeModeTab$Row
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.Enableable;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.RegistryService;
import com.vicmatskiv.pointblank.config.ConfigManager;
import com.vicmatskiv.pointblank.config.Configurable;
import com.vicmatskiv.pointblank.entity.EntityBuilderProvider;
import com.vicmatskiv.pointblank.item.AttachmentItem;
import com.vicmatskiv.pointblank.item.ItemBuilder;
import com.vicmatskiv.pointblank.registry.AmmoRegistry;
import com.vicmatskiv.pointblank.registry.AttachmentRegistry;
import com.vicmatskiv.pointblank.registry.EntityRegistry;
import com.vicmatskiv.pointblank.registry.GunRegistry;
import com.vicmatskiv.pointblank.registry.MiscItemRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ItemRegistry {
    public static RegistryService<Item> itemRegistry = Platform.getInstance().getItemRegistry();
    private static RegistryService<CreativeModeTab> tabRegistry = Platform.getInstance().getCreativeModeTabRegistry();
    private Map<String, Supplier<? extends Item>> itemsByName = new LinkedHashMap<String, Supplier<? extends Item>>();
    private Map<String, List<Supplier<? extends Item>>> itemsByGroup = new LinkedHashMap<String, List<Supplier<? extends Item>>>();
    public static final ItemRegistry ITEMS = new ItemRegistry();
    private final ConfigManager.Builder itemConfigBuilder = new ConfigManager.Builder("pointblank-items");
    public static final Supplier<CreativeModeTab> POINTBLANK_TAB = tabRegistry.register("pointblank", () -> CreativeModeTab.builder((CreativeModeTab.Row)CreativeModeTab.Row.TOP, (int)0).title((Component)Component.translatable((String)"itemGroup.pointblank.items")).icon(() -> new ItemStack((ItemLike)GunRegistry.M4A1.get())).displayItems((enabledFeatures, entries) -> {
        Consumer<ItemLike> output = new Consumer<ItemLike>(){

            @Override
            public void accept(ItemLike itemLike) {
                Enableable e;
                if (itemLike != null && (!(itemLike instanceof Enableable) || (e = (Enableable)itemLike).isEnabled())) {
                    entries.accept(itemLike);
                }
            }
        };
        GunRegistry.registerTabItems(output);
        AttachmentRegistry.registerTabItems(output);
        AmmoRegistry.registerTabItems(output);
        MiscItemRegistry.registerTabItems(output);
    }).build());

    public Map<String, Supplier<? extends Item>> getItemsByName() {
        return Collections.unmodifiableMap(this.itemsByName);
    }

    public List<Supplier<? extends Item>> getAttachmentsForGroup(String group) {
        List<Supplier<? extends Item>> groupAttachments = this.itemsByGroup.get(group);
        return groupAttachments != null ? Collections.unmodifiableList(groupAttachments) : Collections.emptyList();
    }

    public <I extends Item, T extends ItemBuilder<T>> Supplier<I> register(ItemBuilder<?> itemBuilder) {
        EntityBuilderProvider entityBuilderProvider;
        String name = itemBuilder.getName();
        if (itemBuilder instanceof Configurable) {
            Configurable configurable = (Configurable)((Object)itemBuilder);
            configurable.configure(this.itemConfigBuilder);
        }
        Supplier<Item> ro = itemRegistry.register(name, new ItemSupplier(itemBuilder));
        this.itemsByName.put(name, ro);
        if (itemBuilder instanceof AttachmentItem.Builder) {
            AttachmentItem.Builder attachmentBuilder = (AttachmentItem.Builder)itemBuilder;
            for (String group : attachmentBuilder.getGroups()) {
                List groupMembers = this.itemsByGroup.computeIfAbsent(group, g -> new ArrayList());
                groupMembers.add(ro);
            }
        }
        if ((entityBuilderProvider = itemBuilder.getEntityBuilderProvider()) != null) {
            EntityRegistry.registerItemEntity(name, () -> entityBuilderProvider.getEntityBuilder());
        }
        return ro;
    }

    public <I extends Item> Supplier<I> getDeferredRegisteredObject(String name) {
        String normalizedName = name;
        return () -> {
            Supplier<? extends Item> registryObject = this.itemsByName.get(normalizedName);
            return registryObject != null ? registryObject.get() : null;
        };
    }

    public static void complete() {
        ItemRegistry.ITEMS.itemConfigBuilder.build();
    }

    public static void init() {
        MiscItemRegistry.init();
        AmmoRegistry.init();
        AttachmentRegistry.init();
    }

    public void syncEnabledItems(List<Integer> enabledItemIds) {
        Item item;
        HashSet<Item> enabledItemSet = new HashSet<Item>();
        for (Integer n : enabledItemIds) {
            item = Item.byId((int)n);
            enabledItemSet.add(item);
        }
        for (Supplier supplier : this.itemsByName.values()) {
            item = (Item)supplier.get();
            if (enabledItemSet.contains(item) || !(item instanceof Enableable)) continue;
            Enableable e = (Enableable)item;
            e.setEnabled(false);
        }
    }

    public static class ItemSupplier
    implements Supplier<Item> {
        private final ItemBuilder<?> itemBuilder;

        ItemSupplier(ItemBuilder<?> itemBuilder) {
            this.itemBuilder = itemBuilder;
        }

        public boolean isEnabled() {
            Enableable e;
            ItemBuilder<?> itemBuilder = this.itemBuilder;
            return !(itemBuilder instanceof Enableable) || (e = (Enableable)((Object)itemBuilder)).isEnabled();
        }

        @Override
        public Item get() {
            if (this.isEnabled()) {
                return this.itemBuilder.build();
            }
            return null;
        }
    }
}

