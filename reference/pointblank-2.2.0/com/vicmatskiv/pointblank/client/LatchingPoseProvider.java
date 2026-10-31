/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 */
package com.vicmatskiv.pointblank.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vicmatskiv.pointblank.client.PoseProvider;

public class LatchingPoseProvider
implements PoseProvider {
    private PoseProvider actualPoseProvider;
    private PoseStack.Pose pose;

    public LatchingPoseProvider(PoseProvider actualPoseProvider) {
        this.actualPoseProvider = actualPoseProvider;
        this.pose = actualPoseProvider.getPose();
    }

    @Override
    public PoseStack.Pose getPose() {
        if (this.pose == null) {
            this.pose = this.actualPoseProvider.getPose();
        }
        return this.pose;
    }
}

