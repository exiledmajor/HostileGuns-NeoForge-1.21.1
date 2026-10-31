/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.LightTexture
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package com.vicmatskiv.pointblank.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={GameRenderer.class})
public interface GameRendererAccessorMixin {
    @Accessor(value="mainCamera")
    public Camera getMainCamera();

    @Accessor(value="lightTexture")
    public LightTexture getLightTexture();

    @Mutable
    @Accessor(value="renderDistance")
    public void setRenderDistance(float var1);

    @Invoker(value="getFov")
    public double invokeGetFov(Camera var1, float var2, boolean var3);

    @Invoker(value="renderItemInHand")
    public void invokeRenderItemInHand(Camera var1, float var2, Matrix4f var3);
}

