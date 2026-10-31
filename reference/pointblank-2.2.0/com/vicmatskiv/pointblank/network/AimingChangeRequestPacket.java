/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.network.GunStateRequestPacket;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AimingChangeRequestPacket
extends GunStateRequestPacket {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private boolean isAimingEnabled;

    public AimingChangeRequestPacket() {
    }

    public AimingChangeRequestPacket(UUID stateId, int slotIndex, boolean isAimingEnabled) {
        super(stateId, slotIndex);
        this.isAimingEnabled = isAimingEnabled;
    }

    public static AimingChangeRequestPacket decode(FriendlyByteBuf buffer) {
        GunStateRequestPacket header = GunStateRequestPacket.decodeHeader(buffer);
        return new AimingChangeRequestPacket(header.stateId, header.slotIndex, buffer.readBoolean());
    }

    @Override
    protected void doEncode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.isAimingEnabled);
    }

    @Override
    protected <T extends GunStateRequestPacket> void handleEnqueued(NetworkService.MessageContext ctx) {
        ServerPlayer player = (ServerPlayer)ctx.getSender();
        if (player != null) {
            Item item;
            ItemStack itemStack = GunItem.resolveServerActionStack(player, this.slotIndex, this.stateId);
            if (itemStack != null && (item = itemStack.getItem()) instanceof GunItem) {
                GunItem gunItem = (GunItem)item;
                gunItem.handleAimingChangeRequest((Player)player, itemStack, this.stateId, this.slotIndex, this.isAimingEnabled);
            } else {
                LOGGER.warn("Mismatching item in slot {}", (Object)this.slotIndex);
            }
        }
    }
}

