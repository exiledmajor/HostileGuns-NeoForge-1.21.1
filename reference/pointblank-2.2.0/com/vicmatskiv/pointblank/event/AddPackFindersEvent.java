/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.packs.PackType
 *  net.minecraft.server.packs.repository.RepositorySource
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.RepositorySource;

public class AddPackFindersEvent
implements CustomEvent {
    private final PackType packType;
    private final Set<RepositorySource> sources;

    public AddPackFindersEvent(PackType packType) {
        this.packType = packType;
        this.sources = new LinkedHashSet<RepositorySource>();
    }

    public void addRepositorySource(RepositorySource source) {
        this.sources.add(source);
    }

    public Set<RepositorySource> getSources() {
        return Collections.unmodifiableSet(this.sources);
    }

    public PackType getPackType() {
        return this.packType;
    }

    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

