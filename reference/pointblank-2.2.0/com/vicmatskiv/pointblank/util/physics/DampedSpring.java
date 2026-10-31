/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.util.physics;

public class DampedSpring {
    private final double stiffness;
    private final double damping;
    private final double mass;
    private double position;
    private double velocity;
    private double target;
    private long lastUpdate;

    public DampedSpring(double stiffness, double damping, double mass, double initialValue) {
        this.stiffness = stiffness;
        this.damping = damping;
        this.mass = mass;
        this.position = initialValue;
        this.target = initialValue;
        this.velocity = 0.0;
        this.lastUpdate = System.nanoTime();
    }

    public double update(double newTarget) {
        this.target = newTarget;
        long now = System.nanoTime();
        double deltaTime = (double)(now - this.lastUpdate) / 1.0E9;
        this.lastUpdate = now;
        if (deltaTime > 0.1) {
            deltaTime = 0.1;
        }
        if (deltaTime <= 0.0) {
            return this.position;
        }
        double displacement = this.position - this.target;
        double springForce = -this.stiffness * displacement;
        double dampingForce = -this.damping * this.velocity;
        double acceleration = (springForce + dampingForce) / this.mass;
        this.velocity += acceleration * deltaTime;
        this.position += this.velocity * deltaTime;
        return this.position;
    }

    public double getCurrent() {
        return this.position;
    }

    public double getVelocity() {
        return this.velocity;
    }

    public void reset(double value) {
        this.position = value;
        this.target = value;
        this.velocity = 0.0;
        this.lastUpdate = System.nanoTime();
    }

    public void setTarget(double value) {
        this.target = value;
    }
}

