/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.ResourceLocation
 *  org.jetbrains.annotations.NotNull
 */
package com.vicmatskiv.pointblank.platform.neoforge;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class NeoforgePayload<M>
implements CustomPacketPayload {
    private final M data;
    private final CustomPacketPayload.Type<? extends CustomPacketPayload> type;

    NeoforgePayload(M data) {
        this.data = data;
        this.type = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)data.getClass().getSimpleName().toLowerCase()));
    }

    public M getData() {
        return this.data;
    }

    public // Could not load outer class - annotation placement on inner may be incorrect
    @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return this.type;
    }
}

