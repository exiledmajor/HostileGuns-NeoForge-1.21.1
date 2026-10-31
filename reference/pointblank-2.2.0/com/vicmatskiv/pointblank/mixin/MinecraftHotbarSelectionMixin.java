/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package com.vicmatskiv.pointblank.mixin;

import com.vicmatskiv.pointblank.client.ClientEventHandler;
import com.vicmatskiv.pointblank.client.GunHandSwapCoordinator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={Minecraft.class})
public class MinecraftHotbarSelectionMixin {
    @Redirect(method={"handleKeybinds"}, at=@At(value="FIELD", target="Lnet/minecraft/world/entity/player/Inventory;selected:I", opcode=181))
    private void pointblank$deferCommittedHotbarSelection(Inventory inventory, int selected) {
        boolean deferred;
        LocalPlayer player = Minecraft.getInstance().player;
        boolean bl = deferred = player != null && ClientEventHandler.runSyncCompute(() -> GunHandSwapCoordinator.getInstance().tryQueueDeferredMainHandSlotChange(player, selected)) != false;
        if (!deferred) {
            inventory.selected = selected;
        }
    }
}

