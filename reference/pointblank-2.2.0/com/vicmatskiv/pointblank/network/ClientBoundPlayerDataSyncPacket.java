/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Player
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class ClientBoundPlayerDataSyncPacket {
    private CompoundTag playerData;
    private List<Integer> enabledItems;

    public ClientBoundPlayerDataSyncPacket(CompoundTag playerData, List<Integer> enabledItems) {
        this.playerData = playerData;
        this.enabledItems = enabledItems;
    }

    public ClientBoundPlayerDataSyncPacket() {
    }

    public static ClientBoundPlayerDataSyncPacket decode(FriendlyByteBuf buf) {
        ClientBoundPlayerDataSyncPacket packet = new ClientBoundPlayerDataSyncPacket();
        packet.playerData = buf.readNbt();
        int itemCount = buf.readInt();
        packet.enabledItems = new ArrayList<Integer>();
        for (int i = 0; i < itemCount; ++i) {
            packet.enabledItems.add(buf.readInt());
        }
        return packet;
    }

    public static void encode(ClientBoundPlayerDataSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeNbt((Tag)packet.playerData);
        buf.writeInt(packet.enabledItems.size());
        for (int itemId : packet.enabledItems) {
            buf.writeInt(itemId);
        }
    }

    public static void handle(ClientBoundPlayerDataSyncPacket packet, NetworkService.MessageContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ClientUtil.getClientPlayer();
            CompoundTag serverSlotMapping = packet.playerData.getCompound("pointblank:attachmentSlotMapping");
            if (serverSlotMapping != null) {
                MiscUtil.getPersistentData(player).put("pointblank:attachmentSlotMapping", (Tag)serverSlotMapping);
            } else {
                MiscUtil.getPersistentData(player).remove(null);
            }
            ItemRegistry.ITEMS.syncEnabledItems(packet.enabledItems);
        });
        ctx.setPacketHandled(true);
    }

    public CompoundTag getPlayerData() {
        return this.playerData;
    }
}

