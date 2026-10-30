package dev.tree.hostileguns;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(HostileGuns.MOD_ID)
public final class HostileGuns {
    public static final String MOD_ID = "hostileguns";

    public HostileGuns(IEventBus modBus) {
        HostileGunsConfig.load();
        ModEntities.ENTITY_TYPES.register(modBus);
        NeoForge.EVENT_BUS.addListener(HostileGunEvents::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(HostileGunEvents::onEntityTick);
    }
}
