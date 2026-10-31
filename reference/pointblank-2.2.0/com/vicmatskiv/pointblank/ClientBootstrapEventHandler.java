/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.MenuScreens
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.world.entity.EntityType
 */
package com.vicmatskiv.pointblank;

import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.block.entity.PrinterBlockEntity;
import com.vicmatskiv.pointblank.block.entity.WorkstationBlockEntity;
import com.vicmatskiv.pointblank.client.ClientEventHandler;
import com.vicmatskiv.pointblank.client.gui.AttachmentManagerScreen;
import com.vicmatskiv.pointblank.client.gui.CraftingScreen;
import com.vicmatskiv.pointblank.client.particle.EffectParticles;
import com.vicmatskiv.pointblank.client.render.BaseModelBlockRenderer;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimatorCompat;
import com.vicmatskiv.pointblank.entity.EntityBuilder;
import com.vicmatskiv.pointblank.event.ClientSetupEvent;
import com.vicmatskiv.pointblank.event.EntityRenderersEvent;
import com.vicmatskiv.pointblank.event.MenuRegistry;
import com.vicmatskiv.pointblank.event.RegisterKeyMappingsEvent;
import com.vicmatskiv.pointblank.event.RegisterParticleProvidersEvent;
import com.vicmatskiv.pointblank.event.SubscribeEvent2;
import com.vicmatskiv.pointblank.registry.BlockEntityRegistry;
import com.vicmatskiv.pointblank.registry.BlockModelRegistry;
import com.vicmatskiv.pointblank.registry.EntityRegistry;
import com.vicmatskiv.pointblank.registry.ParticleRegistry;
import com.vicmatskiv.pointblank.registry.ThirdPersonAnimationRegistry;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.EntityType;

public class ClientBootstrapEventHandler {
    @SubscribeEvent2
    public void clientSetup(ClientSetupEvent event) {
        ClientEventHandler clientEventHandler = new ClientEventHandler();
        Platform platform = Platform.getInstance();
        platform.getEventBus().register(clientEventHandler);
        MenuScreens.register(MenuRegistry.ATTACHMENTS.get(), AttachmentManagerScreen::new);
        MenuScreens.register(MenuRegistry.CRAFTING.get(), CraftingScreen::new);
        PlayerAnimatorCompat.getInstance().registerAnimationTypes();
        ThirdPersonAnimationRegistry.init();
    }

    @SubscribeEvent2
    public void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientEventHandler.RELOAD_KEY.get());
        event.register(ClientEventHandler.FIRE_MODE_KEY.get());
        event.register(ClientEventHandler.INSPECT_KEY.get());
        event.register(ClientEventHandler.ATTACHMENT_KEY.get());
        event.register(ClientEventHandler.SCOPE_SWITCH_KEY.get());
        event.register(ClientEventHandler.INTERACT_KEY.get());
        event.register(ClientEventHandler.SETTINGS_KEY.get());
    }

    @SubscribeEvent2
    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityRegistry.WORKSTATION_BLOCK_ENTITY.get(), context -> new BaseModelBlockRenderer<WorkstationBlockEntity>(BlockModelRegistry.WORKSTATION_BLOCK_MODEL.get()));
        event.registerBlockEntityRenderer(BlockEntityRegistry.PRINTER_BLOCK_ENTITY.get(), context -> new BaseModelBlockRenderer<PrinterBlockEntity>(BlockModelRegistry.PRINTER_BLOCK_MODEL.get()));
        for (Map.Entry<Supplier<EntityType<?>>, Supplier<EntityBuilder<?, ?>>> e : EntityRegistry.getItemEntityBuilders().entrySet()) {
            Supplier<EntityBuilder<?, ?>> supplier = e.getValue();
            EntityBuilder<?, ?> builder = supplier.get();
            if (!builder.hasRenderer()) continue;
            EntityType<?> entityType = e.getKey().get();
            event.registerEntityRenderer(entityType, context -> builder.createEntityRenderer(context));
        }
    }

    @SubscribeEvent2
    public void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet((ParticleType)ParticleRegistry.EFFECT_PARTICLE_TYPE.get(), EffectParticles.EffectParticleProvider::new);
    }
}

