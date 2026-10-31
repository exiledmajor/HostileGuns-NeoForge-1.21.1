/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.common.Mod
 *  net.neoforged.fml.loading.FMLLoader
 */
package com.vicmatskiv.pointblank;

import com.vicmatskiv.pointblank.ClientBootstrapEventHandler;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.PointBlankInitializer;
import com.vicmatskiv.pointblank.event.EventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;

@Mod(value="pointblank")
public class PointBlankMod {
    public PointBlankMod(IEventBus modBus) {
        PointBlankInitializer.init();
        Dist side = FMLLoader.getDist();
        if (side.isClient()) {
            EventBus bootstrapEventBus = Platform.getInstance().getBootstrapEventBus();
            bootstrapEventBus.register(new ClientBootstrapEventHandler());
        }
    }
}

