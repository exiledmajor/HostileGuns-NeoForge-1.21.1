/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank;

import com.vicmatskiv.pointblank.event.AddPackFindersEvent;
import com.vicmatskiv.pointblank.event.CommonSetupEvent;
import com.vicmatskiv.pointblank.event.GatherDataEvent;
import com.vicmatskiv.pointblank.event.LoadCompleteEvent;
import com.vicmatskiv.pointblank.event.SubscribeEvent2;
import com.vicmatskiv.pointblank.registry.ExtensionRegistry;

public class BootstrapEventHandler {
    private final ExtensionRegistry extensionRegistry;

    public BootstrapEventHandler(ExtensionRegistry extensionRegistry) {
        this.extensionRegistry = extensionRegistry;
    }

    @SubscribeEvent2
    public void commonSetup(CommonSetupEvent event) {
    }

    @SubscribeEvent2
    public void onGatherData(GatherDataEvent event) {
    }

    @SubscribeEvent2
    public void onAddPackFinder(AddPackFindersEvent event) {
        event.addRepositorySource(this.extensionRegistry.getRepositorySource());
    }

    @SubscribeEvent2
    public void loadComplete(LoadCompleteEvent event) {
    }
}

