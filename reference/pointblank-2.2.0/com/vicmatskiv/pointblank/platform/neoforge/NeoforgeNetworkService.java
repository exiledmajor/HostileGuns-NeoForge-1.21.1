/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.network.PacketDistributor
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 *  net.neoforged.neoforge.network.handling.IPayloadHandler
 *  net.neoforged.neoforge.network.registration.PayloadRegistrar
 */
package com.vicmatskiv.pointblank.platform.neoforge;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.platform.neoforge.NeoforgePayload;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NeoforgeNetworkService
implements NetworkService {
    private final LinkedHashMap<Class<?>, PacketInfo<?>> packetRegistrations = new LinkedHashMap();

    @Override
    public <M, B extends FriendlyByteBuf> void registerPacket(Class<M> packetClass, NetworkService.Direction direction, BiConsumer<M, RegistryFriendlyByteBuf> encoder, Function<RegistryFriendlyByteBuf, M> decoder, BiConsumer<M, NetworkService.MessageContext> handler) {
        StreamCodec mCodec = StreamCodec.ofMember(encoder::accept, decoder::apply);
        StreamCodec codec = StreamCodec.composite((StreamCodec)mCodec, NeoforgePayload::getData, NeoforgePayload::new);
        CustomPacketPayload.Type type = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)packetClass.getSimpleName().toLowerCase()));
        IPayloadHandler handler2 = (payload, context) -> handler.accept(payload.getData(), new NeoforgeMessageContext(context));
        this.packetRegistrations.put(packetClass, new PacketInfo(direction, codec, type, handler2));
    }

    public void registerAll(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        for (Map.Entry<Class<?>, PacketInfo<?>> entry : this.packetRegistrations.entrySet()) {
            PacketInfo<?> packetInfo = entry.getValue();
            this.registerPacket(registrar, packetInfo);
        }
    }

    private <T> void registerPacket(PayloadRegistrar registrar, PacketInfo<T> packetInfo) {
        if (packetInfo.direction == NetworkService.Direction.CLIENT_TO_SERVER) {
            registrar.playToServer(packetInfo.type, packetInfo.codec, packetInfo.handler);
        } else if (packetInfo.direction == NetworkService.Direction.SERVER_TO_CLIENT) {
            registrar.playToClient(packetInfo.type, packetInfo.codec, packetInfo.handler);
        }
    }

    @Override
    public void sendToClient(Object message, Player toPlayer) {
        PacketDistributor.sendToPlayer((ServerPlayer)((ServerPlayer)toPlayer), new NeoforgePayload<Object>(message), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    @Override
    public void sendToServer(Object message) {
        PacketDistributor.sendToServer(new NeoforgePayload<Object>(message), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    @Override
    public CustomPacketPayload createCustomPayload(Object packet) {
        return new NeoforgePayload<Object>(packet);
    }

    private record PacketInfo<T>(NetworkService.Direction direction, StreamCodec<? super RegistryFriendlyByteBuf, NeoforgePayload<T>> codec, CustomPacketPayload.Type<NeoforgePayload<T>> type, IPayloadHandler<NeoforgePayload<T>> handler) {
    }

    static class NeoforgeMessageContext
    implements NetworkService.MessageContext {
        private IPayloadContext delegate;

        NeoforgeMessageContext(IPayloadContext delegate) {
            this.delegate = delegate;
        }

        @Override
        public void enqueueWork(Runnable runnable) {
            this.delegate.enqueueWork(runnable);
        }

        @Override
        public void setPacketHandled(boolean isPacketHandled) {
        }

        @Override
        public Player getSender() {
            return this.delegate.player();
        }

        @Override
        public boolean isClientSide() {
            return MiscUtil.isClientSide((Entity)this.delegate.player());
        }
    }
}

