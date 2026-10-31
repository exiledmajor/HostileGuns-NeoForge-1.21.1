/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.attachment.Attachments;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.registry.FeatureTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ServerBoundNextAttachmentPacket {
    private AttachmentCategory category;
    private Class<? extends Feature> featureType;

    public ServerBoundNextAttachmentPacket(AttachmentCategory category, Class<? extends Feature> featureType) {
        this.category = category;
        this.featureType = featureType;
    }

    public ServerBoundNextAttachmentPacket() {
    }

    public static void encode(ServerBoundNextAttachmentPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.category.ordinal());
        buf.writeInt(FeatureTypeRegistry.getFeatureTypeId(packet.featureType));
    }

    public static ServerBoundNextAttachmentPacket decode(FriendlyByteBuf buf) {
        return new ServerBoundNextAttachmentPacket(AttachmentCategory.fromOrdinal(buf.readInt()), FeatureTypeRegistry.getFeatureType(buf.readInt()));
    }

    public static void handle(ServerBoundNextAttachmentPacket packet, NetworkService.MessageContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer)ctx.getSender();
            ItemStack mainHeldItem = player.getMainHandItem();
            if (mainHeldItem != null) {
                Attachments.selectNextAttachment(mainHeldItem, packet.category, packet.featureType);
            }
        });
        ctx.setPacketHandled(true);
    }
}

