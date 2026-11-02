package dev.tree.mainframeprogression;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(MainframeProgression.MOD_ID)
public final class MainframeProgression {
    public static final String MOD_ID = "mainframe_progression";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredHolder<Item, Item> CYBERWARE_REPAIR_KIT =
            ITEMS.register("cyberware_repair_kit", () -> new CyberwareRepairKitItem(new Item.Properties().stacksTo(16)));

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, MOD_ID);

    public static final DeferredHolder<Attribute, Attribute> CYBERWARE_VERSATILITY =
            ATTRIBUTES.register("cyberware_versatility",
                    () -> new RangedAttribute(
                            "attribute.name.mainframe_progression.cyberware_versatility",
                            0.0D, 0.0D, 250.0D).setSyncable(true));

    public MainframeProgression(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(MainframeProgression::addCreativeItems);
        modBus.addListener(MainframeProgression::filterCyberneticsCreativeItems);
        modBus.addListener(MainframeProgression::modifyAttributes);
        ProgressionPreset.installOnce();
        NeoForge.EVENT_BUS.addListener(MainframeProgression::playerTick);
        NeoForge.EVENT_BUS.addListener(MainframeProgression::livingDeath);
        NeoForge.EVENT_BUS.addListener(MainframeProgression::removeCyberneticsMobDrops);
        NeoForge.EVENT_BUS.addListener(MainframeProgression::blockRipperdocSpawn);
        NeoForge.EVENT_BUS.addListener(MainframeProgression::blockCyberneticsItemUse);
        NeoForge.EVENT_BUS.addListener(MainframeProgression::blockCyberneticsBlockPlacement);
    }

    private static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) event.accept(CYBERWARE_REPAIR_KIT.get());
    }

    private static void filterCyberneticsCreativeItems(BuildCreativeModeTabContentsEvent event) {
        // Keep Create: Cybernetics' mob entities available through natural spawning,
        // but remove its non-mob items from every creative tab.
        event.getEntries().keySet().removeIf(stack -> {
            var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return id != null && MOD_CREATE_CYBERNETICS.equals(id.getNamespace());
        });
    }

    private static void modifyAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, CYBERWARE_VERSATILITY);
    }

    private static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        double versatility = player.getAttributeValue(CYBERWARE_VERSATILITY);
        ChromewareBridge.tick(player, (int)Math.round(versatility));
    }

    private static void removeCyberneticsMobDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        if (id != null && MOD_CREATE_CYBERNETICS.equals(id.getNamespace())) {
            // Keep the native mob, its AI, equipment and XP; suppress item drops only.
            event.getDrops().clear();
        }
    }

    private static final String MOD_CREATE_CYBERNETICS = "createcybernetics";

    private static void blockCyberneticsItemUse(PlayerInteractEvent.RightClickItem event) {
        var id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (id != null && MOD_CREATE_CYBERNETICS.equals(id.getNamespace())) event.setCanceled(true);
    }

    private static void blockCyberneticsBlockPlacement(BlockEvent.EntityPlaceEvent event) {
        var id = BuiltInRegistries.BLOCK.getKey(event.getPlacedBlock().getBlock());
        if (id != null && MOD_CREATE_CYBERNETICS.equals(id.getNamespace())) event.setCanceled(true);
    }

    private static void blockRipperdocSpawn(EntityJoinLevelEvent event) {
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        if (id != null && MOD_CREATE_CYBERNETICS.equals(id.getNamespace())
                && "ripper".equals(id.getPath())) {
            event.setCanceled(true);
        }
    }

    private static void livingDeath(LivingDeathEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()) return;
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            ChromewareBridge.onDeath(player, event.getSource());
        }
    }
}
