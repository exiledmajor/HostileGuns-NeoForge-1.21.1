/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.irisshaders.iris.targets.RenderTargets
 *  net.minecraft.client.renderer.ShaderInstance
 */
package com.vicmatskiv.pointblank.compat.iris;

import net.irisshaders.iris.targets.RenderTargets;
import net.minecraft.client.renderer.ShaderInstance;

public interface IrisAuxShaderProvider {
    public ShaderInstance getPointblankAuxShader();

    public ShaderInstance getPointblankAuxPlainShader();

    public ShaderInstance getPointblankMaskShader();

    public RenderTargets getPointblankRenderTargets();
}

