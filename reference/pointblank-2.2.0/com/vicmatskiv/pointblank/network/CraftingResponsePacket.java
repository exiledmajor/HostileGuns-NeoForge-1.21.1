/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.client.ClientEventHandler;
import com.vicmatskiv.pointblank.client.gui.CraftingScreen;
import com.vicmatskiv.pointblank.util.ClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class CraftingResponsePacket {
    private ItemStack itemStack;
    private CraftingResult result;
    private boolean isAddedToInventory;

    public CraftingResponsePacket(ItemStack itemStack, CraftingResult result, boolean isAddedToInventory) {
        this.itemStack = itemStack;
        this.result = result;
        this.isAddedToInventory = isAddedToInventory;
    }

    public CraftingResponsePacket() {
    }

    public static void encode(CraftingResponsePacket packet, RegistryFriendlyByteBuf buf) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode((Object)buf, (Object)packet.itemStack);
        buf.writeEnum((Enum)packet.result);
        buf.writeBoolean(packet.isAddedToInventory);
    }

    public static CraftingResponsePacket decode(RegistryFriendlyByteBuf buf) {
        return new CraftingResponsePacket((ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode((Object)buf), (CraftingResult)buf.readEnum(CraftingResult.class), buf.readBoolean());
    }

    public static void handle(CraftingResponsePacket packet, NetworkService.MessageContext context) {
        context.enqueueWork(() -> ClientEventHandler.runSyncTick(() -> {
            MutableComponent message = null;
            switch (packet.result.ordinal()) {
                case 0: {
                    if (packet.isAddedToInventory) {
                        message = Component.translatable((String)"").append(packet.itemStack.getItem().getName(packet.itemStack)).append(" ").append((Component)Component.translatable((String)"message.pointblank.added_to_the_inventory")).append("!");
                        break;
                    }
                    message = Component.translatable((String)"").append(packet.itemStack.getItem().getName(packet.itemStack)).append(" ").append((Component)Component.translatable((String)"message.pointblank.dropped_on_the_gound")).append("!");
                    break;
                }
                case 1: {
                    Minecraft mc = Minecraft.getInstance();
                    Screen patt0$temp = mc.screen;
                    if (patt0$temp instanceof CraftingScreen) {
                        CraftingScreen craftingScreen = (CraftingScreen)patt0$temp;
                        craftingScreen.cancelCrafting();
                    }
                    message = Component.translatable((String)"message.pointblank.failed_to_craft");
                    break;
                }
                case 2: {
                    message = Component.translatable((String)"").append(packet.itemStack.getItem().getName(packet.itemStack)).append(" ").append((Component)Component.translatable((String)"message.pointblank.cancelled_to_craft"));
                }
            }
            if (message != null) {
                ClientUtil.notifyUser(message);
            }
        }));
        context.setPacketHandled(true);
    }

    public static enum CraftingResult {
        COMPLETED,
        FAILED,
        CANCELLED;

    }
}

