/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package com.vicmatskiv.pointblank.client.effect;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.SegmentsProviders;
import com.vicmatskiv.pointblank.client.VertexConsumers;
import com.vicmatskiv.pointblank.client.effect.AbstractEffect;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.EffectRenderContext;
import com.vicmatskiv.pointblank.client.particle.EffectParticles;
import com.vicmatskiv.pointblank.client.uv.SpriteUVProvider;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.Interpolators;
import com.vicmatskiv.pointblank.util.JsonUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class DetachedProjectileEffect
extends AbstractEffect {
    private float distanceToTarget;
    private Vec3 targetPosition;
    private int numBlades;
    private boolean isFaceEnabled;
    private float rCol = 1.0f;
    private float gCol = 1.0f;
    private float bCol = 1.0f;
    private int bladeBrightness;
    private int faceBrightness;
    private float bladeTextureMinU;
    private float bladeTextureMaxU;
    private float faceTextureMinU;
    private float faceTextureMaxU;
    private Quaternionf initialRotation;
    private SegmentsProviders.SegmentsProvider segmentsProvider;
    private Interpolators.FloatInterpolator faceWidthProvider;
    private Interpolators.FloatInterpolator bladeWidthProvider;
    private Vec3 startPosition;
    private Function<VertexConsumer, VertexConsumer> vertexConsumerTransformer;

    private DetachedProjectileEffect() {
    }

    @Override
    public boolean hasInfiniteBounds() {
        return true;
    }

    @Override
    public void render(EffectRenderContext effectRenderContext) {
        float progress = effectRenderContext.getProgress();
        if (progress < 0.0f) {
            return;
        }
        VertexConsumer vertexBuffer = this.vertexConsumerTransformer.apply(effectRenderContext.getVertexBuffer());
        float[][] segments = this.segmentsProvider.getSegments(this.distanceToTarget, progress, this.lifetimeNanos);
        float distanceSoFar = segments[0][0] + (segments[0][1] - segments[0][0]) * 0.5f;
        if (distanceSoFar >= this.distanceToTarget) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        if (this.initialRotation == null) {
            Vec3 direction = this.targetPosition.subtract(this.startPosition);
            this.initialRotation = new Quaternionf().rotateTo(0.0f, 0.0f, -1.0f, (float)direction.x, (float)direction.y, (float)direction.z);
        }
        Quaternionf faceRotation = new Quaternionf((Quaternionfc)camera.rotation());
        float distanceProgress = distanceSoFar / this.distanceToTarget;
        float currentPosX = (float)Mth.lerp((double)distanceProgress, (double)this.startPosition.x, (double)this.targetPosition.x);
        float currentPosY = (float)Mth.lerp((double)distanceProgress, (double)this.startPosition.y, (double)this.targetPosition.y);
        float currentPosZ = (float)Mth.lerp((double)distanceProgress, (double)this.startPosition.z, (double)this.targetPosition.z);
        Vec3 cameraPosition = camera.getPosition();
        float dx = (float)((double)currentPosX - cameraPosition.x());
        float dy = (float)((double)currentPosY - cameraPosition.y());
        float dz = (float)((double)currentPosZ - cameraPosition.z());
        int lightColor = this.isGlowEnabled ? 240 : effectRenderContext.getLightColor();
        Quaternionf rotation = new Quaternionf((Quaternionfc)this.initialRotation);
        float roll = -(effectRenderContext.getInitialAngle() + this.numRotations * 360.0f * progress);
        rotation.rotateZ(-roll * ((float)Math.PI / 180));
        SpriteUVProvider spriteUVProvider = effectRenderContext.getSpriteUVProvider();
        if (this.isFaceEnabled) {
            faceRotation.rotateZ(-roll * ((float)Math.PI / 180));
            this.renderFace(dx, dy, dz, faceRotation, vertexBuffer, progress, lightColor, spriteUVProvider);
        }
        if (this.numBlades > 0) {
            float bladeStep = 180.0f / (float)this.numBlades;
            for (int k = 0; k < this.numBlades; ++k) {
                Quaternionf bladeQuaternion = new Quaternionf((Quaternionfc)rotation);
                float bladeRoll = (float)k * bladeStep;
                bladeQuaternion.rotateZ(bladeRoll * ((float)Math.PI / 180));
                this.renderBlade(dx, dy, dz, bladeQuaternion, segments, vertexBuffer, progress, lightColor, spriteUVProvider);
            }
        }
    }

    private void renderFace(float dx, float dy, float dz, Quaternionf rotation, VertexConsumer vertexConsumer, float progress, int lightColor, SpriteUVProvider spriteUVProvider) {
        float halfWidth = this.faceWidthProvider.getValue(progress) * 0.5f;
        float alpha = this.alphaProvider.getValue(progress);
        float[] uv = spriteUVProvider.getSpriteUV(progress);
        float spriteWidth = uv[2] - uv[0];
        float u0 = uv[0] + spriteWidth * this.faceTextureMinU;
        float v0 = uv[1];
        float u1 = uv[0] + spriteWidth * this.faceTextureMaxU;
        float v1 = uv[3];
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-halfWidth, -halfWidth, 0.0f), new Vector3f(-halfWidth, halfWidth, 0.0f), new Vector3f(halfWidth, halfWidth, 0.0f), new Vector3f(halfWidth, -halfWidth, 0.0f)};
        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate((Quaternionfc)rotation);
            vector3f.add(dx, dy, dz);
        }
        for (int k = 0; k < this.faceBrightness; ++k) {
            vertexConsumer.addVertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, alpha).setLight(lightColor);
            vertexConsumer.addVertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, alpha).setLight(lightColor);
            vertexConsumer.addVertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, alpha).setLight(lightColor);
            vertexConsumer.addVertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, alpha).setLight(lightColor);
        }
    }

    private void renderBlade(float dx, float dy, float dz, Quaternionf rotation, float[][] beamSegments, VertexConsumer vertexConsumer, float progress, int lightColor, SpriteUVProvider spriteUVProvider) {
        float beamSegmentWidth = this.bladeWidthProvider.getValue(progress) * 0.5f;
        float alpha = this.alphaProvider.getValue(progress);
        int numBeamSpriteSegments = beamSegments.length;
        float[] uv = spriteUVProvider.getSpriteUV(progress * (float)this.lifetimeNanos);
        float spriteWidth = uv[2] - uv[0];
        float spriteSegmentWidth = spriteWidth * (this.bladeTextureMaxU - this.bladeTextureMinU) / (float)numBeamSpriteSegments;
        for (int beamSegmentIndex = 0; beamSegmentIndex < numBeamSpriteSegments; ++beamSegmentIndex) {
            float u0 = uv[0] + spriteWidth * this.bladeTextureMinU + spriteSegmentWidth * (float)beamSegmentIndex;
            float v0 = uv[1];
            float u1 = u0 + spriteSegmentWidth;
            float v1 = uv[3];
            float beamSegmentStart = beamSegments[beamSegmentIndex][0];
            float beamSegmentEnd = beamSegments[beamSegmentIndex][1];
            float beamSegmentLengthHalf = (beamSegmentEnd - beamSegmentStart) * 0.5f;
            Vector3f[] avector3f = new Vector3f[]{new Vector3f(0.0f, beamSegmentWidth, beamSegmentLengthHalf), new Vector3f(0.0f, beamSegmentWidth, -beamSegmentLengthHalf), new Vector3f(0.0f, -beamSegmentWidth, -beamSegmentLengthHalf), new Vector3f(0.0f, -beamSegmentWidth, beamSegmentLengthHalf)};
            for (int i = 0; i < 4; ++i) {
                Vector3f vector3f = avector3f[i];
                vector3f.rotate((Quaternionfc)rotation);
                vector3f.add(dx, dy, dz);
            }
            for (int k = 0; k < this.bladeBrightness; ++k) {
                vertexConsumer.addVertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, alpha).setLight(lightColor);
                vertexConsumer.addVertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, alpha).setLight(lightColor);
                vertexConsumer.addVertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, alpha).setLight(lightColor);
                vertexConsumer.addVertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, alpha).setLight(lightColor);
            }
        }
    }

    @Override
    public void launch(Entity player) {
        EffectParticles.EffectParticle particle = new EffectParticles.EffectParticle(player, this);
        Minecraft mc = Minecraft.getInstance();
        mc.particleEngine.add((Particle)particle);
    }

    public static class Builder
    extends AbstractEffect.AbstractEffectBuilder<Builder, DetachedProjectileEffect> {
        private static final Set<GunItem.FirePhase> COMPATIBLE_PHASES = Set.of(GunItem.FirePhase.PREPARING, GunItem.FirePhase.FIRING, GunItem.FirePhase.HIT_SCAN_ACQUIRED, GunItem.FirePhase.COMPLETETING);
        public static final long DEFAULT_DURATION = 250L;
        public static final float MAX_DISTANCE = 200.0f;
        public static final int DEFAULT_NUM_BLADES = 0;
        public static final int DEFAULT_NUM_SPRITES = 1;
        public static final int DEFAULT_FACE_BRIGHTNESS = 2;
        public static final int DEFAULT_BLADE_BRIGHTNESS = 2;
        public static final float DEFAULT_DETACH_AFTER = 1.0f;
        public static final int MAX_SPRITES = 200;
        public static final int MAX_BRIGHTNESS = 5;
        public static final int DEFAULT_SHOTS_PER_TRACE = 1;
        public static final SegmentsProviders.SegmentsProvider DEFAULT_SEGMENTS_PROVIDER = new SegmentsProviders.MovingSegmentsProvider(200.0f, -10.0f);
        private Interpolators.FloatInterpolator bladeWidthProvider;
        private Interpolators.FloatInterpolator faceWidthProvider;
        private int bladeBrightness;
        private int faceBrightness;
        private SegmentsProviders.SegmentsProvider segmentsProvider;
        private int numBlades;
        private boolean isFaceEnabled;
        private float faceTextureMinU;
        private float faceTextureMaxU;
        private float bladeTextureMinU;
        private float bladeTextureMaxU;

        public Builder() {
            this.duration = 250L;
            this.numBlades = 0;
            this.faceBrightness = 2;
            this.bladeBrightness = 2;
            this.segmentsProvider = DEFAULT_SEGMENTS_PROVIDER;
            this.faceTextureMinU = 0.0f;
            this.faceTextureMaxU = 1.0f;
            this.bladeTextureMinU = 0.0f;
            this.bladeTextureMaxU = 1.0f;
        }

        public Builder withWidth(double width) {
            this.withWidthProvider(new Interpolators.EaseInEaseOutFloatProvider((float)width));
            return this;
        }

        @Override
        public Builder withWidthProvider(Interpolators.FloatInterpolator widthProvider) {
            this.withBladeWidthProvider(widthProvider);
            this.withFaceWidthProvider(widthProvider);
            return this;
        }

        public Builder withBladeWidthProvider(Interpolators.FloatInterpolator widthProvider) {
            this.bladeWidthProvider = widthProvider;
            return this;
        }

        public Builder withFaceWidthProvider(Interpolators.FloatInterpolator widthProvider) {
            this.faceWidthProvider = widthProvider;
            return this;
        }

        public Builder withBlades(int numBlades, float bladeTextureMinU, float bladeTextureMaxU) {
            this.numBlades = numBlades;
            this.bladeTextureMinU = bladeTextureMinU;
            this.bladeTextureMaxU = bladeTextureMaxU;
            return this;
        }

        public Builder withBlade(float bladeTextureMinU, float bladeTextureMaxU) {
            return this.withBlades(1, bladeTextureMinU, bladeTextureMaxU);
        }

        public Builder withBlades(int numBlades) {
            return this.withBlades(numBlades, 0.0f, 1.0f);
        }

        public Builder withFace(float faceTextureMinU, float faceTextureMaxU) {
            this.faceTextureMinU = faceTextureMinU;
            this.faceTextureMaxU = faceTextureMaxU;
            this.isFaceEnabled = true;
            return this;
        }

        @Override
        public Builder withBrightness(int brightness) {
            this.faceBrightness = brightness;
            this.bladeBrightness = brightness;
            return this;
        }

        public Builder withFaceBrightness(int brightness) {
            this.faceBrightness = Mth.clamp((int)brightness, (int)0, (int)5);
            return this;
        }

        public Builder withBladeBrightness(int brightness) {
            this.bladeBrightness = Mth.clamp((int)brightness, (int)0, (int)5);
            return this;
        }

        public Builder withSegmentsProvider(SegmentsProviders.SegmentsProvider segmentsProvider) {
            this.segmentsProvider = segmentsProvider;
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            JsonObject bladeObj;
            super.withJsonObject(obj);
            JsonObject faceObj = obj.getAsJsonObject("face");
            if (faceObj != null) {
                float minU = JsonUtil.getJsonFloat(faceObj, "minU", 0.0f);
                float maxU = JsonUtil.getJsonFloat(faceObj, "maxU", 1.0f);
                this.withFace(minU, maxU);
                int brightness = JsonUtil.getJsonInt(faceObj, "brightness", 1);
                this.withFaceBrightness(brightness);
                Interpolators.FloatInterpolator widthInt = JsonUtil.getJsonInterpolator(faceObj, "width");
                if (widthInt != null) {
                    this.withFaceWidthProvider(widthInt);
                }
            }
            if ((bladeObj = obj.getAsJsonObject("blade")) != null) {
                int count = JsonUtil.getJsonInt(bladeObj, "count", 1);
                float minU = JsonUtil.getJsonFloat(bladeObj, "minU", 0.0f);
                float maxU = JsonUtil.getJsonFloat(bladeObj, "maxU", 1.0f);
                this.withBlades(count, minU, maxU);
                int brightness = JsonUtil.getJsonInt(bladeObj, "brightness", 1);
                this.withBladeBrightness(brightness);
                Interpolators.FloatInterpolator widthInt = JsonUtil.getJsonInterpolator(bladeObj, "width");
                if (widthInt != null) {
                    this.withBladeWidthProvider(widthInt);
                }
            }
            float initialSpeed = JsonUtil.getJsonFloat(obj, "initialSpeed", 0.0f);
            float acceleration = JsonUtil.getJsonFloat(obj, "acceleration", 0.0f);
            if (this.numBlades > 0) {
                if (!MiscUtil.isNearlyZero(initialSpeed) || !MiscUtil.isNearlyZero(acceleration)) {
                    this.withSegmentsProvider(new SegmentsProviders.MovingSegmentsProvider(initialSpeed, acceleration));
                } else {
                    this.withSegmentsProvider(new SegmentsProviders.StaticBeamSegmentsProvider());
                }
            } else if (!MiscUtil.isNearlyZero(initialSpeed) || !MiscUtil.isNearlyZero(acceleration)) {
                this.withSegmentsProvider(new SegmentsProviders.MovingPointProvider(initialSpeed, acceleration));
            } else {
                this.withSegmentsProvider(SegmentsProviders.ZERO_PROVIDER);
            }
            return this;
        }

        @Override
        public boolean isEffectAttached() {
            return false;
        }

        @Override
        public DetachedProjectileEffect build(EffectBuilder.Context context) {
            DetachedProjectileEffect effect = new DetachedProjectileEffect();
            super.apply(effect, context);
            effect.startPosition = (Vec3)effect.startPositionProvider.get();
            effect.targetPosition = context.getHitResult().getLocation();
            effect.distanceToTarget = (float)effect.startPosition.distanceTo(effect.targetPosition);
            effect.faceWidthProvider = this.faceWidthProvider;
            effect.bladeWidthProvider = this.bladeWidthProvider;
            effect.isFaceEnabled = this.isFaceEnabled;
            effect.numBlades = this.numBlades;
            effect.bladeTextureMinU = this.bladeTextureMinU;
            effect.bladeTextureMaxU = this.bladeTextureMaxU;
            effect.faceTextureMinU = this.faceTextureMinU;
            effect.faceTextureMaxU = this.faceTextureMaxU;
            effect.faceBrightness = this.faceBrightness;
            effect.bladeBrightness = this.bladeBrightness;
            effect.segmentsProvider = this.segmentsProvider;
            Function<VertexConsumer, VertexConsumer> contextVertexConsumerTransformer = context.getVertexConsumerTransformer();
            effect.vertexConsumerTransformer = contextVertexConsumerTransformer != null ? contextVertexConsumerTransformer : VertexConsumers.PARTICLE;
            effect.alphaProvider = this.alphaProvider;
            return effect;
        }

        @Override
        public Collection<GunItem.FirePhase> getCompatiblePhases() {
            return COMPATIBLE_PHASES;
        }
    }
}

