/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.block.entity.PrinterBlockEntity;
import com.vicmatskiv.pointblank.inventory.CraftingContainerMenu;
import com.vicmatskiv.pointblank.network.CraftingResponsePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class CraftingRequestPacket {
    private RequestType requestType;
    private ResourceLocation recipeId;

    public CraftingRequestPacket(RequestType requestType, ResourceLocation recipeId) {
        this.requestType = requestType;
        this.recipeId = recipeId;
    }

    public CraftingRequestPacket() {
    }

    public static void encode(CraftingRequestPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum((Enum)packet.requestType);
        buf.writeResourceLocation(packet.recipeId);
    }

    public static CraftingRequestPacket decode(FriendlyByteBuf buf) {
        return new CraftingRequestPacket((RequestType)buf.readEnum(RequestType.class), buf.readResourceLocation());
    }

    public static void handle(CraftingRequestPacket packet, NetworkService.MessageContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer)ctx.getSender();
            AbstractContainerMenu patt0$temp = player.containerMenu;
            if (patt0$temp instanceof CraftingContainerMenu) {
                CraftingContainerMenu containerMenu = (CraftingContainerMenu)patt0$temp;
                PrinterBlockEntity craftingBlockEntity = containerMenu.getWorkstationBlockEntity();
                if (packet.requestType == RequestType.START_CRAFTING) {
                    PrinterBlockEntity.CraftingEventHandler eventHandler = new PrinterBlockEntity.CraftingEventHandler(){

                        @Override
                        public void onCraftingCompleted(Player player, ItemStack craftingItemStack, boolean isAddedToInventory) {
                            Platform.getInstance().getNetworkService().sendToClient(new CraftingResponsePacket(craftingItemStack, CraftingResponsePacket.CraftingResult.COMPLETED, isAddedToInventory), player);
                        }

                        @Override
                        public void onCraftingFailed(Player player, ItemStack craftingItemStack, Exception craftingException) {
                            Platform.getInstance().getNetworkService().sendToClient(new CraftingResponsePacket(ItemStack.EMPTY, CraftingResponsePacket.CraftingResult.FAILED, false), player);
                        }
                    };
                    if (!craftingBlockEntity.tryCrafting((Player)player, packet.recipeId, eventHandler)) {
                        Platform.getInstance().getNetworkService().sendToClient(new CraftingResponsePacket(ItemStack.EMPTY, CraftingResponsePacket.CraftingResult.FAILED, false), (Player)player);
                    }
                } else if (packet.requestType == RequestType.CANCEL_CRAFTING) {
                    craftingBlockEntity.cancelCrafting((Player)player, packet.recipeId);
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public static enum RequestType {
        START_CRAFTING,
        CANCEL_CRAFTING;

    }
}

