/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.irisshaders.iris.targets.RenderTargets
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package com.vicmatskiv.pointblank.compat.iris.mixin;

import com.vicmatskiv.pointblank.compat.iris.RenderTargetsExt;
import net.irisshaders.iris.targets.RenderTargets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={RenderTargets.class})
public class RenderTargetsMixin
implements RenderTargetsExt {
    @Shadow
    private boolean fullClearRequired;

    @Override
    public void setPointblankRenderFullClearRequired(boolean fullClearRequired) {
        this.fullClearRequired = fullClearRequired;
    }
}

