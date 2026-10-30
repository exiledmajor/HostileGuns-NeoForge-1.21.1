package dev.tree.mainframeprogression;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(MainframeProgression.MOD_ID)
public final class MainframeProgression {
    public static final String MOD_ID = "mainframe_progression";

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, MOD_ID);

    public static final DeferredHolder<Attribute, Attribute> CYBERWARE_CAPACITY =
            ATTRIBUTES.register("cyberware_capacity",
                    () -> new RangedAttribute(
                            "attribute.name.mainframe_progression.cyberware_capacity",
                            0.0D, 0.0D, 5000.0D).setSyncable(true));

    public MainframeProgression(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
        modBus.addListener(MainframeProgression::modifyAttributes);
        ProgressionPreset.installOnce();
        NeoForge.EVENT_BUS.addListener(MainframeProgression::playerTick);
    }

    private static void modifyAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, CYBERWARE_CAPACITY);
    }

    private static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 20 != 0) return;

        double investedCapacity = player.getAttributeValue(CYBERWARE_CAPACITY);
        ChromewareBridge.syncCapacity(player, (int) Math.round(investedCapacity));
    }
}
