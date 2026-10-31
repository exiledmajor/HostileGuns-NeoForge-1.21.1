/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.irisshaders.iris.shadows.ShadowRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.vicmatskiv.pointblank.compat.iris.mixin;

import com.vicmatskiv.pointblank.compat.iris.IrisCompat;
import net.irisshaders.iris.shadows.ShadowRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ShadowRenderer.class})
public class IrisShadowRendererMixin {
    @Inject(method={"renderShadows"}, at={@At(value="HEAD")}, remap=false)
    private void onStartRenderShadows(CallbackInfo callbackInfo) {
        IrisCompat.getInstance().onStartRenderShadows();
    }

    @Inject(method={"renderShadows"}, at={@At(value="TAIL")}, remap=false)
    private void onEndRenderShadows(CallbackInfo callbackInfo) {
        IrisCompat.getInstance().onEndRenderShadows();
    }
}

