/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.GameRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.vicmatskiv.pointblank.mixin;

import com.vicmatskiv.pointblank.client.ClientEventHandler;
import com.vicmatskiv.pointblank.client.ClientSystem;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.render.AuxLevelRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={GameRenderer.class})
public class GameRendererMixin {
    @Unique
    private boolean pointblank$isRenderingWorld = true;

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V")})
    public void render(DeltaTracker deltaTracker, boolean p_109096_, CallbackInfo callbackInfo) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        GunClientState state = GunClientState.getMainHeldState();
        if (state != null) {
            ClientSystem.getInstance().preRender(state, deltaTracker);
        }
    }

    @ModifyArg(method={"renderLevel"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/GameRenderer;getProjectionMatrix (D)Lorg/joml/Matrix4f;", ordinal=0), index=0)
    private double modifyFov(double fov) {
        AuxLevelRenderer auxRenderer = ClientSystem.getInstance().getAuxLevelRenderer();
        if (auxRenderer.isRenderingPip()) {
            fov = auxRenderer.getFov();
        }
        return fov;
    }

    @ModifyArg(method={"renderLevel"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/GameRenderer;getProjectionMatrix (D)Lorg/joml/Matrix4f;", ordinal=1), index=0)
    private double modifyCullFrustrumFov(double fov) {
        AuxLevelRenderer auxRenderer = ClientSystem.getInstance().getAuxLevelRenderer();
        if (auxRenderer.isRenderingPip()) {
            fov = auxRenderer.getCullFrustrumFov();
        }
        return fov;
    }

    @Inject(method={"getFov"}, at={@At(value="HEAD")})
    private void onGetFovHead(Camera camera, float partialTicks, boolean useFovSetting, CallbackInfoReturnable<Double> cir) {
        this.pointblank$isRenderingWorld = useFovSetting;
    }

    @Inject(method={"getFov"}, at={@At(value="RETURN")}, cancellable=true)
    private void onGetFovReturn(Camera camera, float partialTicks, boolean useFovSetting, CallbackInfoReturnable<Double> cir) {
        double gunFov;
        if (!useFovSetting && ClientEventHandler.isPipFallbackZoomActive() && (gunFov = ClientEventHandler.getGunFov()) > 0.0) {
            cir.setReturnValue((Object)gunFov);
        }
    }
}

