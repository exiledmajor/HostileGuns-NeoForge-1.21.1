/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.LightTexture
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.vicmatskiv.pointblank.mixin;

import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.event.RenderLevelEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelRenderer.class})
public class LevelRenderMixin {
    @Inject(method={"renderLevel"}, at={@At(value="HEAD")})
    private void onRenderLevel(DeltaTracker deltaTracker, boolean $$1, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f inverseCameraMatrix, Matrix4f $$6, CallbackInfo ci) {
        Platform.getInstance().getEventBus().postEvent(new RenderLevelEvent.Pre(inverseCameraMatrix));
    }
}

