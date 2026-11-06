package dev.tree.cyberneticssuppressor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(CyberneticsSuppressor.MOD_ID)
public final class CyberneticsSuppressor {
    public static final String MOD_ID = "cybernetics_suppressor";
    private static final String TARGET_NAMESPACE = "createcybernetics";

    public CyberneticsSuppressor(IEventBus modBus) {
        modBus.addListener(CyberneticsSuppressor::filterCreativeItems);
        NeoForge.EVENT_BUS.addListener(CyberneticsSuppressor::suppressMobItemDrops);
        NeoForge.EVENT_BUS.addListener(CyberneticsSuppressor::suppressRipperdoc);
        NeoForge.EVENT_BUS.addListener(CyberneticsSuppressor::suppressItemUse);
        NeoForge.EVENT_BUS.addListener(CyberneticsSuppressor::suppressBlockPlacement);
    }

    private static void filterCreativeItems(BuildCreativeModeTabContentsEvent event) {
        var hidden = java.util.stream.Stream.concat(
                event.getParentEntries().stream(), event.getSearchEntries().stream())
                .filter(stack -> {
                    var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    return id != null && TARGET_NAMESPACE.equals(id.getNamespace());
                }).distinct().toList();

        for (var stack : hidden) {
            event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private static void suppressMobItemDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        if (id != null && TARGET_NAMESPACE.equals(id.getNamespace())) {
            // Suppress the mod's item loot only. The entity, its AI and XP are untouched.
            event.getDrops().clear();
        }
    }

    private static void suppressRipperdoc(EntityJoinLevelEvent event) {
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        if (id == null || !TARGET_NAMESPACE.equals(id.getNamespace())) return;
        String path = id.getPath().toLowerCase(java.util.Locale.ROOT);
        if (path.equals("ripper") || path.contains("ripperdoc")) {
            event.setCanceled(true);
        }
    }

    private static void suppressItemUse(PlayerInteractEvent.RightClickItem event) {
        var id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (id != null && TARGET_NAMESPACE.equals(id.getNamespace())) event.setCanceled(true);
    }

    private static void suppressBlockPlacement(BlockEvent.EntityPlaceEvent event) {
        var id = BuiltInRegistries.BLOCK.getKey(event.getPlacedBlock().getBlock());
        if (id != null && TARGET_NAMESPACE.equals(id.getNamespace())) event.setCanceled(true);
    }
}
