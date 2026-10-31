/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.DataGenerator
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;

public class GatherDataEvent
implements CustomEvent {
    private final DataGenerator dataGenerator;
    private CompletableFuture<HolderLookup.Provider> lookupProvider;
    private boolean isServerIncluded;

    public GatherDataEvent(DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> lookupProvider, boolean isServerIncluded) {
        this.dataGenerator = dataGenerator;
        this.lookupProvider = lookupProvider;
        this.isServerIncluded = isServerIncluded;
    }

    public DataGenerator getGenerator() {
        return this.dataGenerator;
    }

    public CompletableFuture<HolderLookup.Provider> getLookupProvider() {
        return this.lookupProvider;
    }

    public boolean includeServer() {
        return this.isServerIncluded;
    }

    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

