/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.packs.PackType
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank;

import com.vicmatskiv.pointblank.BootstrapEventHandler;
import com.vicmatskiv.pointblank.CommonEventHandler;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.event.MenuRegistry;
import com.vicmatskiv.pointblank.network.Network;
import com.vicmatskiv.pointblank.registry.BlockEntityRegistry;
import com.vicmatskiv.pointblank.registry.BlockRegistry;
import com.vicmatskiv.pointblank.registry.DataComponentRegistry;
import com.vicmatskiv.pointblank.registry.EntityRegistry;
import com.vicmatskiv.pointblank.registry.ExtensionRegistry;
import com.vicmatskiv.pointblank.registry.FeatureTypeRegistry;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import com.vicmatskiv.pointblank.registry.ParticleRegistry;
import com.vicmatskiv.pointblank.registry.RecipeTypeRegistry;
import com.vicmatskiv.pointblank.registry.SoundRegistry;
import com.vicmatskiv.pointblank.registry.VillagerRegistry;
import com.vicmatskiv.pointblank.util.ServerTaskScheduler;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PointBlankInitializer {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static final boolean DEBUG_SHELL = Boolean.getBoolean("pointblank.debug.shell");

    public static void init() {
        LOGGER.info("Loading mod {}", (Object)"pointblank");
        Platform platform = Platform.getInstance();
        if (DEBUG_SHELL && platform.isClientSide()) {
            LOGGER.info("[shell-debug] pointblank.debug.shell is enabled");
        }
        platform.registerConfig();
        FeatureTypeRegistry.init();
        ExtensionRegistry extensionRegistry = new ExtensionRegistry();
        extensionRegistry.discoverExtensions(platform.isClientSide() ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA);
        EntityRegistry.init();
        BlockEntityRegistry.init();
        BlockRegistry.init();
        RecipeTypeRegistry.init();
        DataComponentRegistry.init();
        MenuRegistry.init();
        SoundRegistry.init();
        ParticleRegistry.init();
        ItemRegistry.init();
        VillagerRegistry.init();
        ExtensionRegistry.registerItemsFromExtensions(extensionRegistry);
        ItemRegistry.complete();
        platform.onRegistrationCompleted();
        platform.getBootstrapEventBus().register(new BootstrapEventHandler(extensionRegistry));
        platform.getEventBus().register(new CommonEventHandler(new ServerTaskScheduler()));
        Network.setupNetworkChannel();
    }
}

