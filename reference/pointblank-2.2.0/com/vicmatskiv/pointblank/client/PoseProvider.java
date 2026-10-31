/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 */
package com.vicmatskiv.pointblank.client;

import com.mojang.blaze3d.vertex.PoseStack;

@FunctionalInterface
public interface PoseProvider {
    public PoseStack.Pose getPose();
}

