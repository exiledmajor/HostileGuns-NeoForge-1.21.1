/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;

public abstract class ViewportEvent
implements CustomEvent {

    public static class ComputeFov
    extends ViewportEvent {
        private double fov;
        private boolean useFovSetting = true;

        public double getFOV() {
            return this.fov;
        }

        public void setFOV(double fov) {
            this.fov = fov;
        }

        public boolean useFovSetting() {
            return this.useFovSetting;
        }

        public void setUseFovSetting(boolean useFovSetting) {
            this.useFovSetting = useFovSetting;
        }

        @Override
        public <T> T accept(EventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class ComputeCameraAngles
    extends ViewportEvent {
        private float yaw;
        private float pitch;
        private float roll;

        public ComputeCameraAngles(float yaw, float pitch, float roll) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
        }

        public float getPitch() {
            return this.pitch;
        }

        public float getYaw() {
            return this.yaw;
        }

        public float getRoll() {
            return this.roll;
        }

        public void setYaw(float yaw) {
            this.yaw = yaw;
        }

        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

        public void setRoll(float roll) {
            this.roll = roll;
        }

        @Override
        public <T> T accept(EventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}

