/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.irisshaders.iris.pipeline.FinalPassRenderer
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package com.vicmatskiv.pointblank.compat.iris.mixin;

import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.compat.iris.IrisAuxIndexHolder;
import com.vicmatskiv.pointblank.compat.iris.IrisShaderUtil;
import net.irisshaders.iris.pipeline.FinalPassRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={FinalPassRenderer.class})
public class FinalPassRendererMixin {
    @Unique
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    @Unique
    private static boolean loggedOnce = false;

    @ModifyArgs(method={"createProgram"}, at=@At(value="INVOKE", target="Lnet/irisshaders/iris/gl/program/ProgramBuilder;begin(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/google/common/collect/ImmutableSet;)Lnet/irisshaders/iris/gl/program/ProgramBuilder;"), remap=false)
    private void onCreateProgram(Args args) {
        if (!loggedOnce) {
            LOGGER.info("FinalPassRendererMixin.onCreateProgram: INVOKED! advancedIrisIntegrationEnabled={}", (Object)Config.advancedIrisIntegrationEnabled);
        }
        if (!Config.advancedIrisIntegrationEnabled) {
            if (!loggedOnce) {
                LOGGER.info("FinalPassRendererMixin.onCreateProgram: Disabled by config, skipping");
                loggedOnce = true;
            }
            return;
        }
        Integer auxIndex = IrisAuxIndexHolder.value.get();
        if (!loggedOnce) {
            LOGGER.info("FinalPassRendererMixin.onCreateProgram: auxIndex from holder = {}", (Object)auxIndex);
        }
        if (auxIndex != null) {
            String vsh = (String)args.get(1);
            String fsh = (String)args.get(3);
            String auxPointblankTexCoordVar = "auxPointblankTexCoord";
            vsh = IrisShaderUtil.patchFinalPassVertexShader(vsh, auxPointblankTexCoordVar);
            args.set(1, (Object)vsh);
            fsh = IrisShaderUtil.patchFinalPassFragmentShader(fsh, auxPointblankTexCoordVar, auxIndex);
            args.set(3, (Object)fsh);
            if (!loggedOnce) {
                LOGGER.info("FinalPassRendererMixin.onCreateProgram: Successfully patched final pass shaders for auxIndex={}", (Object)auxIndex);
                loggedOnce = true;
            }
        } else if (!loggedOnce) {
            LOGGER.warn("FinalPassRendererMixin.onCreateProgram: auxIndex is NULL, cannot patch final pass shaders!");
            loggedOnce = true;
        }
    }
}

