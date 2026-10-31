/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package com.vicmatskiv.pointblank.mixin;

import com.vicmatskiv.pointblank.client.GunHandSwapCoordinator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={Minecraft.class})
public abstract class MinecraftDualSwapMixin {
    @Redirect(method={"handleKeybinds"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void pointblank$delayOffhandSwap(ClientPacketListener connection, Packet<?> packet) {
        ServerboundPlayerActionPacket actionPacket;
        if (packet instanceof ServerboundPlayerActionPacket && (actionPacket = (ServerboundPlayerActionPacket)packet).getAction() == ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (GunHandSwapCoordinator.getInstance().tryQueueDeferredOffhandSwap(player, () -> connection.send(packet))) {
                return;
            }
        }
        connection.send(packet);
    }
}

