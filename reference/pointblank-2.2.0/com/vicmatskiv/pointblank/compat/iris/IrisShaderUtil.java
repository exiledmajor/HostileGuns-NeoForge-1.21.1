/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.irisshaders.iris.gl.blending.BlendModeOverride
 *  net.irisshaders.iris.gl.texture.InternalTextureFormat
 *  net.irisshaders.iris.shaderpack.programs.ProgramSet
 *  net.irisshaders.iris.shaderpack.programs.ProgramSource
 *  net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives$RenderTargetSettings
 *  net.irisshaders.iris.shaderpack.properties.ShaderProperties
 *  net.irisshaders.iris.targets.RenderTargets
 *  org.apache.commons.io.IOUtils
 *  org.jetbrains.annotations.NotNull
 */
package com.vicmatskiv.pointblank.compat.iris;

import com.vicmatskiv.pointblank.compat.iris.GlslPatcher;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import net.irisshaders.iris.targets.RenderTargets;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

public class IrisShaderUtil {
    public static final String GLSL_VAR_AUX_POINTBLANK_TEX_COORD = "auxPointblankTexCoord";

    public static ProgramSource createProgramSource(String name, String vertexShaderSource, String fragmentShaderSource, ProgramSet parent, Supplier<ShaderProperties> shaderPropertiesSupplier, BlendModeOverride defaultBlendModeOverride) {
        return new ProgramSource(name, vertexShaderSource, null, null, null, fragmentShaderSource, parent, shaderPropertiesSupplier.get(), defaultBlendModeOverride);
    }

    public static String getResource(String resourceName) throws IOException {
        return new String(IOUtils.toByteArray((InputStream)Objects.requireNonNull(IrisShaderUtil.class.getResourceAsStream(resourceName))), StandardCharsets.UTF_8);
    }

    public static String replaceRenderTargets(String template, int value) {
        String pattern = "/* RENDERTARGETS: {} */";
        String replacement = "/* RENDERTARGETS: " + value + " */";
        return template.replace(pattern, replacement);
    }

    public static int findAvailableRenderTarget(ProgramSet programSet, RenderTargets renderTargets) {
        boolean targetExists;
        InternalTextureFormat format;
        boolean shouldClear;
        PackRenderTargetDirectives.RenderTargetSettings settings;
        int i;
        Map renderTargetSettings = programSet.getPackDirectives().getRenderTargetDirectives().getRenderTargetSettings();
        int result = -1;
        for (i = 4; i < 16; ++i) {
            settings = (PackRenderTargetDirectives.RenderTargetSettings)renderTargetSettings.get(i);
            if (settings == null) continue;
            shouldClear = settings.shouldClear();
            format = settings.getInternalFormat();
            boolean bl = targetExists = renderTargets.get(i) != null;
            if (!shouldClear || format != InternalTextureFormat.RGBA || targetExists) continue;
            renderTargets.getOrCreate(i);
            result = i;
            break;
        }
        if (result == -1) {
            for (i = 4; i < 16; ++i) {
                boolean hasAlpha;
                settings = (PackRenderTargetDirectives.RenderTargetSettings)renderTargetSettings.get(i);
                if (settings == null) continue;
                shouldClear = settings.shouldClear();
                format = settings.getInternalFormat();
                targetExists = renderTargets.get(i) != null;
                boolean bl = hasAlpha = format != null && format.name().contains("RGBA");
                if (!shouldClear || !hasAlpha || targetExists) continue;
                renderTargets.getOrCreate(i);
                result = i;
                break;
            }
        }
        return result;
    }

    @NotNull
    public static String patchFinalPassVertexShader(String vsh, String auxPointblankTexCoordVar) {
        vsh = GlslPatcher.appendBeforeUniformDeclaration(vsh, "out vec2 " + auxPointblankTexCoordVar + ";\n");
        vsh = GlslPatcher.appendToMain(vsh, auxPointblankTexCoordVar + " = (mat4(1.0f) * vec4(UV0, 0.0f, 1.0f)).xy;\n");
        return vsh;
    }

    @NotNull
    public static String patchFinalPassFragmentShader(String fsh, String auxPointblankTexCoordVar, int auxIndex) {
        String colorTexIdentifierName;
        if (!GlslPatcher.containsIdentifier(fsh = GlslPatcher.appendBeforeUniformDeclaration(fsh, "in vec2 " + auxPointblankTexCoordVar + ";\n"), colorTexIdentifierName = "colortex" + auxIndex)) {
            fsh = GlslPatcher.appendBeforeSamplerDeclaration(fsh, "uniform sampler2D " + colorTexIdentifierName + ";\n");
        }
        String outIdentifier = "iris_FragData0";
        String color = null;
        if (GlslPatcher.containsIdentifier(fsh, outIdentifier)) {
            color = "vec4 pointblankAuxColor = texture(colortex" + auxIndex + ", " + auxPointblankTexCoordVar + ");\nif (pointblankAuxColor.a > 0.0) " + outIdentifier + " = pointblankAuxColor;\n";
        } else {
            List<String> layoutVars = GlslPatcher.extractVec3LayoutVariables(fsh);
            if (!layoutVars.isEmpty()) {
                outIdentifier = layoutVars.get(0);
                color = "vec4 pointblankAuxColor = texture(colortex" + auxIndex + ", " + auxPointblankTexCoordVar + ");\nif (pointblankAuxColor.a > 0.0) " + outIdentifier + " = pointblankAuxColor.xyz;\n";
            }
        }
        if (color != null) {
            fsh = GlslPatcher.appendToMain(fsh, color);
        }
        return fsh;
    }
}

