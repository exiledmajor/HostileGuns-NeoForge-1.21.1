/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.MouseHandler
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Inventory
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package com.vicmatskiv.pointblank.mixin;

import com.vicmatskiv.pointblank.client.ClientEventHandler;
import com.vicmatskiv.pointblank.client.ClientSystem;
import com.vicmatskiv.pointblank.client.GunHandSwapCoordinator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={MouseHandler.class})
public class MouseHandlerMixin {
    @ModifyConstant(method={"turnPlayer(D)V"}, constant={@Constant(doubleValue=0.6000000238418579)}, expect=0)
    private double modifySensitivity(double originalValue) {
        return ClientSystem.modifyMouseSensitivity(originalValue);
    }

    @Redirect(method={"onScroll"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Inventory;swapPaint(D)V"))
    private void pointblank$deferCommittedWheelSelection(Inventory inventory, double scrollDelta) {
        boolean deferred;
        int direction = (int)Math.signum(scrollDelta);
        int targetSlot = Mth.positiveModulo((int)(inventory.selected - direction), (int)9);
        LocalPlayer player = Minecraft.getInstance().player;
        boolean bl = deferred = player != null && ClientEventHandler.runSyncCompute(() -> GunHandSwapCoordinator.getInstance().tryQueueDeferredMainHandSlotChange(player, targetSlot)) != false;
        if (!deferred) {
            inventory.swapPaint(scrollDelta);
        }
    }
}

