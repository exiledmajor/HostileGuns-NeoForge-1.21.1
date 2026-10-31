/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.attachment.AttachmentHost;
import com.vicmatskiv.pointblank.inventory.AttachmentContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

public class ServerBoundOpenScreenPacket {
    private ScreenType screenType;

    public ServerBoundOpenScreenPacket(ScreenType sreenType) {
        this.screenType = sreenType;
    }

    public ServerBoundOpenScreenPacket() {
    }

    public static void encode(ServerBoundOpenScreenPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum((Enum)packet.screenType);
    }

    public static ServerBoundOpenScreenPacket decode(FriendlyByteBuf buf) {
        return new ServerBoundOpenScreenPacket((ScreenType)buf.readEnum(ScreenType.class));
    }

    public static void handle(ServerBoundOpenScreenPacket packet, NetworkService.MessageContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer)ctx.getSender();
            switch (packet.screenType.ordinal()) {
                case 0: {
                    ItemStack heldItem = player.getMainHandItem();
                    if (player == null || !(heldItem.getItem() instanceof AttachmentHost)) break;
                    player.openMenu((MenuProvider)new SimpleMenuProvider((windowId, playerInventory, p) -> new AttachmentContainerMenu(windowId, playerInventory, heldItem), (Component)Component.translatable((String)"screen.pointblank.attachments")));
                    break;
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public static enum ScreenType {
        ATTACHMENTS,
        CRAFTING;

    }
}

