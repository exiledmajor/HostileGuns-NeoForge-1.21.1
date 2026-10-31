/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animation.AnimationController
 *  software.bernie.geckolib.animation.EasingType
 */
package com.vicmatskiv.pointblank.mixin;

import java.util.function.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.EasingType;

@Mixin(value={AnimationController.class})
public interface AnimationControllerAccessor {
    @Mutable
    @Accessor(value="isJustStarting")
    public void setIsJustStarting(boolean var1);

    @Accessor(value="overrideEasingTypeFunction")
    public <T extends GeoAnimatable> Function<T, EasingType> getOverrideEasingTypeFunction();

    @Accessor(value="justStartedTransition")
    public boolean justStartedTransition();
}

