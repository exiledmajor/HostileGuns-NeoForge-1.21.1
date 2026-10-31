/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.vicmatskiv.pointblank.client.render;

import net.minecraft.resources.ResourceLocation;

public record RenderTypeKey(ResourceLocation texture, boolean isStencilEnabled, int stencilFunc, int stencilOp, int stencilValue) {
}

