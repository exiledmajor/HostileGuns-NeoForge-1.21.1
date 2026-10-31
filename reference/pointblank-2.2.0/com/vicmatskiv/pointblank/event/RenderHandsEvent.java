/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 */
package com.vicmatskiv.pointblank.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;

public class RenderHandsEvent
implements CustomEvent {
    private PoseStack poseStack;

    public RenderHandsEvent(PoseStack poseStack) {
        this.poseStack = poseStack;
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return null;
    }

    public static class Pre
    extends RenderHandsEvent {
        public Pre(PoseStack poseStack) {
            super(poseStack);
        }
    }
}

