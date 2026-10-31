/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.irisshaders.iris.pipeline.IrisRenderingPipeline
 *  net.irisshaders.iris.pipeline.programs.ShaderKey
 *  net.irisshaders.iris.shaderpack.programs.ProgramSet
 *  net.irisshaders.iris.shaderpack.programs.ProgramSource
 *  net.irisshaders.iris.targets.RenderTargets
 *  net.minecraft.client.renderer.ShaderInstance
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.vicmatskiv.pointblank.compat.iris.mixin;

import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.compat.iris.IrisAuxIndexHolder;
import com.vicmatskiv.pointblank.compat.iris.IrisAuxShaderProvider;
import com.vicmatskiv.pointblank.compat.iris.IrisShaderPropertiesProvider;
import com.vicmatskiv.pointblank.compat.iris.IrisShaderUtil;
import java.io.IOException;
import java.util.Optional;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.targets.RenderTargets;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={IrisRenderingPipeline.class})
public abstract class IrisRenderingPipelineMixin
implements IrisAuxShaderProvider {
    @Shadow
    @Final
    private RenderTargets renderTargets;
    @Unique
    private ShaderInstance auxShader;
    @Unique
    private ShaderInstance auxPlainShader;
    @Unique
    private ShaderInstance maskShader;
    @Unique
    private ProgramSet programSet;

    @Shadow
    protected abstract void destroyShaders();

    @Shadow
    protected abstract ShaderInstance createShader(String var1, Optional<ProgramSource> var2, ShaderKey var3) throws IOException;

    @Inject(method={"<init>"}, at={@At(value="INVOKE", target="Lnet/irisshaders/iris/targets/RenderTargets;<init>(IIIILnet/irisshaders/iris/gl/texture/DepthBufferFormat;Ljava/util/Map;Lnet/irisshaders/iris/shaderpack/properties/PackDirectives;)V")})
    private void captureContext(ProgramSet programSet, CallbackInfo ci) {
        this.programSet = programSet;
    }

    @Inject(method={"<init>"}, at={@At(value="INVOKE", target="Lnet/irisshaders/iris/pipeline/FinalPassRenderer;<init>(Lnet/irisshaders/iris/pipeline/WorldRenderingPipeline;Lnet/irisshaders/iris/shaderpack/programs/ProgramSet;Lnet/irisshaders/iris/targets/RenderTargets;Lnet/irisshaders/iris/gl/texture/TextureAccess;Lnet/irisshaders/iris/gl/buffer/ShaderStorageBufferHolder;Lnet/irisshaders/iris/uniforms/FrameUpdateNotifier;Lcom/google/common/collect/ImmutableSet;Lnet/irisshaders/iris/pathways/CenterDepthSampler;Ljava/util/function/Supplier;Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;Ljava/util/Set;Lcom/google/common/collect/ImmutableSet;Lnet/irisshaders/iris/uniforms/custom/CustomUniforms;)V")})
    private void beforeFinalPassRendererInit(ProgramSet programSet, CallbackInfo ci) {
        if (!Config.advancedIrisIntegrationEnabled) {
            return;
        }
        int auxRenderTargetIndex = IrisShaderUtil.findAvailableRenderTarget(programSet, this.renderTargets);
        if (auxRenderTargetIndex > 0) {
            IrisAuxIndexHolder.value.set(auxRenderTargetIndex);
        } else {
            IrisAuxIndexHolder.value.remove();
        }
    }

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void onInit(ProgramSet programSet, CallbackInfo ci) {
        ProgramSource programSource;
        String vsh;
        String fsh;
        if (!Config.advancedIrisIntegrationEnabled) {
            return;
        }
        if (!(programSet instanceof IrisShaderPropertiesProvider)) {
            return;
        }
        IrisShaderPropertiesProvider aux = (IrisShaderPropertiesProvider)programSet;
        Integer auxIndex = IrisAuxIndexHolder.value.get();
        if (auxIndex == null) {
            return;
        }
        try {
            fsh = IrisShaderUtil.getResource("/iris_aux_shader.fsh");
            fsh = IrisShaderUtil.replaceRenderTargets(fsh, auxIndex);
            vsh = IrisShaderUtil.getResource("/iris_aux_shader.vsh");
            programSource = IrisShaderUtil.createProgramSource("pointblank_iris_aux", vsh, fsh, this.programSet, aux::getPointblankShaderProperties, ShaderKey.TEXTURED.getProgram().getBlendModeOverride());
            this.auxShader = this.createShader(programSource.getName(), Optional.of(programSource), ShaderKey.TEXTURED);
        }
        catch (IOException e) {
            this.destroyShaders();
            throw new RuntimeException(e);
        }
        finally {
            IrisAuxIndexHolder.value.remove();
        }
        try {
            fsh = IrisShaderUtil.getResource("/iris_aux_shader_plain.fsh");
            fsh = IrisShaderUtil.replaceRenderTargets(fsh, auxIndex);
            vsh = IrisShaderUtil.getResource("/iris_aux_shader_plain.vsh");
            programSource = IrisShaderUtil.createProgramSource("pointblank_iris_aux_plain", vsh, fsh, this.programSet, aux::getPointblankShaderProperties, ShaderKey.TEXTURED.getProgram().getBlendModeOverride());
            this.auxPlainShader = this.createShader(programSource.getName(), Optional.of(programSource), ShaderKey.TEXTURED);
        }
        catch (IOException e) {
            this.destroyShaders();
            throw new RuntimeException(e);
        }
        finally {
            IrisAuxIndexHolder.value.remove();
        }
        try {
            fsh = IrisShaderUtil.getResource("/iris_mask_shader.fsh");
            fsh = IrisShaderUtil.replaceRenderTargets(fsh, auxIndex);
            vsh = IrisShaderUtil.getResource("/iris_mask_shader.vsh");
            programSource = IrisShaderUtil.createProgramSource("pointblank_iris_mask", vsh, fsh, programSet, aux::getPointblankShaderProperties, ShaderKey.TEXTURED_COLOR.getProgram().getBlendModeOverride());
            this.maskShader = this.createShader(programSource.getName(), Optional.of(programSource), ShaderKey.TEXTURED_COLOR);
        }
        catch (IOException e) {
            this.destroyShaders();
            throw new RuntimeException(e);
        }
        finally {
            IrisAuxIndexHolder.value.remove();
        }
    }

    @Override
    @Unique
    public ShaderInstance getPointblankAuxShader() {
        return this.auxShader;
    }

    @Override
    @Unique
    public ShaderInstance getPointblankAuxPlainShader() {
        return this.auxPlainShader;
    }

    @Override
    public ShaderInstance getPointblankMaskShader() {
        return this.maskShader;
    }

    @Override
    public RenderTargets getPointblankRenderTargets() {
        return this.renderTargets;
    }
}

