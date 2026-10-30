package dev.tree.hostileguns.client;

import dev.tree.hostileguns.HostileBulletEntity;
import dev.tree.hostileguns.HostileGuns;
import dev.tree.hostileguns.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.SnowballRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = HostileGuns.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HOSTILE_BULLET.get(),
                context -> (EntityRenderer<HostileBulletEntity>) (EntityRenderer) new SnowballRenderer(context));
    }
}
