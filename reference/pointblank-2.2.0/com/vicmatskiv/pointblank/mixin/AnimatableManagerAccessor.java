/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  software.bernie.geckolib.animation.AnimatableManager
 */
package com.vicmatskiv.pointblank.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import software.bernie.geckolib.animation.AnimatableManager;

@Mixin(value={AnimatableManager.class})
public interface AnimatableManagerAccessor {
    @Mutable
    @Accessor(value="isFirstTick")
    public void setIsFirstTick(boolean var1);
}

