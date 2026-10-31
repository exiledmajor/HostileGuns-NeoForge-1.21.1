/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.irisshaders.iris.shaderpack.ShaderPack
 *  net.irisshaders.iris.shaderpack.include.AbsolutePackPath
 *  net.irisshaders.iris.shaderpack.programs.ProgramSet
 *  net.irisshaders.iris.shaderpack.properties.ShaderProperties
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.vicmatskiv.pointblank.compat.iris.mixin;

import com.vicmatskiv.pointblank.compat.iris.IrisShaderPropertiesProvider;
import java.util.function.Function;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ProgramSet.class})
public class ProgramSetMixin
implements IrisShaderPropertiesProvider {
    @Unique
    ShaderProperties shaderProperties;

    @Inject(method={"<init>"}, at={@At(value="TAIL")}, remap=false)
    private void onInit(AbsolutePackPath directory, Function<AbsolutePackPath, String> sourceProvider, ShaderProperties shaderProperties, ShaderPack pack, CallbackInfo ci) {
        this.shaderProperties = shaderProperties;
    }

    @Override
    public ShaderProperties getPointblankShaderProperties() {
        return this.shaderProperties;
    }
}

