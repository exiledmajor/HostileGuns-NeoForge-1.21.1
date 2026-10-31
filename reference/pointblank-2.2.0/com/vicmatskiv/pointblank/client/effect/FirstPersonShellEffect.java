/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.math.Axis
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package com.vicmatskiv.pointblank.client.effect;

import com.mojang.math.Axis;
import com.vicmatskiv.pointblank.feature.ShellEjectionFeature;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public final class FirstPersonShellEffect {
    private static final int DEFAULT_LIFETIME_TICKS = 30;
    private static final double DEFAULT_GRAVITY = 0.004;
    private static final double DEFAULT_AIR_DRAG = 1.0;
    private static final float DEFAULT_ANGULAR_DRAG = 0.96f;
    private static final float SHRINK_START_PROGRESS = 0.92f;
    private final ResourceLocation modelResource;
    private final ResourceLocation textureResource;
    private final Vec3 renderOffset;
    private final int lifetimeTicks;
    private final Quaternionf orientation = Axis.YP.rotation((float)Math.PI);
    private final Quaternionf previousOrientation = new Quaternionf((Quaternionfc)this.orientation);
    private final Vector3f angularVelocity;
    private Vec3 position = Vec3.ZERO;
    private Vec3 previousPosition = Vec3.ZERO;
    private Vec3 velocity;
    private int ageTicks;

    public FirstPersonShellEffect(ShellEjectionFeature.Visuals visuals, Vec3 initialVelocity, Vec3 angularVelocityDegrees) {
        this.modelResource = FirstPersonShellEffect.toGeoModelResource(visuals.model());
        this.textureResource = visuals.texture();
        this.renderOffset = visuals.renderOffset();
        this.velocity = initialVelocity;
        this.angularVelocity = new Vector3f((float)angularVelocityDegrees.x, (float)angularVelocityDegrees.y, (float)angularVelocityDegrees.z);
        this.lifetimeTicks = 30;
    }

    public void tick() {
        this.previousPosition = this.position;
        this.previousOrientation.set((Quaternionfc)this.orientation);
        ++this.ageTicks;
        this.position = this.position.add(this.velocity);
        this.velocity = this.velocity.add(0.0, -0.004, 0.0).scale(1.0);
        this.orientation.rotateLocalX(this.angularVelocity.x * ((float)Math.PI / 180)).rotateLocalY(this.angularVelocity.y * ((float)Math.PI / 180)).rotateLocalZ(this.angularVelocity.z * ((float)Math.PI / 180)).normalize();
        this.angularVelocity.mul(0.96f);
    }

    public boolean isExpired() {
        return this.ageTicks >= this.lifetimeTicks;
    }

    public Vec3 getRenderPosition(float partialTick) {
        return this.previousPosition.lerp(this.position, (double)partialTick);
    }

    public Quaternionf getRenderOrientation(float partialTick) {
        return new Quaternionf((Quaternionfc)this.previousOrientation).slerp((Quaternionfc)this.orientation, partialTick);
    }

    public float getRenderScale(float partialTick) {
        float progress = Mth.clamp((float)(((float)this.ageTicks + partialTick) / (float)this.lifetimeTicks), (float)0.0f, (float)1.0f);
        if (progress <= 0.92f) {
            return 1.0f;
        }
        return Mth.clamp((float)(1.0f - (progress - 0.92f) / 0.07999998f), (float)0.0f, (float)1.0f);
    }

    public ResourceLocation getModelResource() {
        return this.modelResource;
    }

    public ResourceLocation getTextureResource() {
        return this.textureResource;
    }

    public Vec3 getRenderOffset() {
        return this.renderOffset;
    }

    private static ResourceLocation toGeoModelResource(ResourceLocation modelResource) {
        Object path = modelResource.getPath();
        if (!((String)path).startsWith("geo/")) {
            path = "geo/" + (String)path;
        }
        if (!((String)path).endsWith(".geo.json")) {
            path = (String)path + ".geo.json";
        }
        return ResourceLocation.fromNamespaceAndPath((String)modelResource.getNamespace(), (String)path);
    }
}

