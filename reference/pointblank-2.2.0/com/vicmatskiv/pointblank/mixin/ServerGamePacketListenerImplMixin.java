/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.server.network.ServerGamePacketListenerImpl
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.vicmatskiv.pointblank.mixin;

import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerGamePacketListenerImpl.class})
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method={"handleSetCreativeModeSlot"}, at={@At(value="HEAD")})
    private void pointblank$refreshCreativeGunIdentity(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo callbackInfo) {
        ItemStack incoming = packet.itemStack();
        if (incoming.isEmpty() || !(incoming.getItem() instanceof GunItem)) {
            return;
        }
        ItemStack replaced = ItemStack.EMPTY;
        short slot = packet.slotNum();
        if (slot >= 0 && slot < this.player.inventoryMenu.slots.size()) {
            replaced = this.player.inventoryMenu.getSlot((int)slot).getItem();
        }
        if (GunItem.hasConflictingItemStackId((Player)this.player, incoming, replaced)) {
            GunItem.assignFreshItemStackId(incoming);
        }
    }
}

