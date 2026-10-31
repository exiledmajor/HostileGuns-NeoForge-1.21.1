/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.model.BakedModel
 *  net.neoforged.neoforge.client.model.SeparateTransformsModel$Baked
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package com.vicmatskiv.pointblank.platform.neoforge.mixin;

import net.minecraft.client.resources.model.BakedModel;
import net.neoforged.neoforge.client.model.SeparateTransformsModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={SeparateTransformsModel.Baked.class})
public interface BakedModelMixin {
    @Accessor(value="baseModel", remap=false)
    public BakedModel getBaseModel();
}

