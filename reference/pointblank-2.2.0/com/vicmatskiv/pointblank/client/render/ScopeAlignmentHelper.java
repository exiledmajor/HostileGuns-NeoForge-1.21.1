/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public final class ScopeAlignmentHelper {
    private ScopeAlignmentHelper() {
    }

    @Nullable
    public static Matrix4f computeAimingTransform(PoseStack poseStack, Matrix4f inverseScopeBoneMatrix, Vector3f scopeAimPointLocal, float adsZTarget) {
        Matrix4f combined = new Matrix4f((Matrix4fc)RenderSystem.getModelViewMatrix()).mul((Matrix4fc)poseStack.last().pose());
        if (Math.abs(combined.determinant()) < 1.0E-10f) {
            return null;
        }
        Matrix4f targetCamera = new Matrix4f().translation(-scopeAimPointLocal.x, -scopeAimPointLocal.y, adsZTarget - scopeAimPointLocal.z);
        return combined.invert().mul((Matrix4fc)targetCamera).mul((Matrix4fc)inverseScopeBoneMatrix);
    }

    public static Matrix4f lerpAimingTransform(Matrix4f aimingTransform, float progress) {
        return new Matrix4f().lerp((Matrix4fc)aimingTransform, progress);
    }
}

