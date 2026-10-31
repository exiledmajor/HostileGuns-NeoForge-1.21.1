/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class FirstPersonHandAnchorCapture {
    private static final ThreadLocal<Matrix4f> CAPTURED_ANCHOR_POSE = new ThreadLocal();

    private FirstPersonHandAnchorCapture() {
    }

    public static void capture(PoseStack poseStack) {
        CAPTURED_ANCHOR_POSE.set(new Matrix4f((Matrix4fc)poseStack.last().pose()));
    }

    @Nullable
    public static Matrix4f getCapturedPose() {
        return CAPTURED_ANCHOR_POSE.get();
    }

    public static void clear() {
        CAPTURED_ANCHOR_POSE.remove();
    }
}

