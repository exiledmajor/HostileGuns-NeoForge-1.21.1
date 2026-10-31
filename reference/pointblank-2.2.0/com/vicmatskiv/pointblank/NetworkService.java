/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.player.Player
 */
package com.vicmatskiv.pointblank;

import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public interface NetworkService {
    public <M, B extends FriendlyByteBuf> void registerPacket(Class<M> var1, Direction var2, BiConsumer<M, RegistryFriendlyByteBuf> var3, Function<RegistryFriendlyByteBuf, M> var4, BiConsumer<M, MessageContext> var5);

    public void sendToClient(Object var1, Player var2);

    public void sendToServer(Object var1);

    public CustomPacketPayload createCustomPayload(Object var1);

    default public void onRegisteringPacketsCompleted() {
    }

    default public void onRegisteringClientPacketsCompleted() {
    }

    public static interface MessageContext {
        public void enqueueWork(Runnable var1);

        public void setPacketHandled(boolean var1);

        public Player getSender();

        public boolean isClientSide();
    }

    public static enum Direction {
        CLIENT_TO_SERVER,
        SERVER_TO_CLIENT;

    }
}

