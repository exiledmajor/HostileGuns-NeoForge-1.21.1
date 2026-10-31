/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import org.joml.Matrix4f;

public class RenderLevelEvent
implements CustomEvent {
    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return null;
    }

    public static class Pre
    extends RenderLevelEvent {
        private final Matrix4f inverseCameraMatrix;

        public Pre(Matrix4f inverseCameraMatrix) {
            this.inverseCameraMatrix = inverseCameraMatrix;
        }

        public Matrix4f getInverseCameraMatrix() {
            return this.inverseCameraMatrix;
        }
    }
}

