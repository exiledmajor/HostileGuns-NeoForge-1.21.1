/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 */
package com.vicmatskiv.pointblank.client.effect;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vicmatskiv.pointblank.client.ClientSystem;
import com.vicmatskiv.pointblank.client.PoseProvider;
import com.vicmatskiv.pointblank.client.SegmentsProviders;
import com.vicmatskiv.pointblank.client.VertexConsumers;
import com.vicmatskiv.pointblank.client.effect.AbstractEffect;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.EffectRenderContext;
import com.vicmatskiv.pointblank.client.particle.EffectParticles;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.client.render.RenderUtil;
import com.vicmatskiv.pointblank.client.uv.SpriteUVProvider;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.Interpolators;
import com.vicmatskiv.pointblank.util.JsonUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

public class AttachedProjectileEffect
extends AbstractEffect {
    private static Random random = new Random();
    private PoseProvider poseProvider;
    private Interpolators.FloatProvider distanceProvider;
    private int numBlades;
    private boolean isFaceEnabled;
    private float randomization;
    private double randomPitch = (random.nextDouble() - 0.5) * 2.0;
    private double randomYaw = (random.nextDouble() - 0.5) * 2.0;
    private int bladeBrightness;
    private int faceBrightness;
    private float bladeTextureMinU;
    private float bladeTextureMaxU;
    private float faceTextureMinU;
    private float faceTextureMaxU;
    private SegmentsProviders.SegmentsProvider segmentsProvider;
    private Interpolators.FloatInterpolator faceWidthProvider;
    private Interpolators.FloatInterpolator bladeWidthProvider;
    private Function<VertexConsumer, VertexConsumer> vertexConsumerTransformer;
    private RenderType renderType;

    private AttachedProjectileEffect() {
    }

    @Override
    public boolean hasInfiniteBounds() {
        return true;
    }

    private VertexConsumer getVertextBuffer(EffectRenderContext effectRenderContext) {
        VertexConsumer vertexBuffer = effectRenderContext.getVertexBuffer();
        if (vertexBuffer == null) {
            MultiBufferSource bufferSource = effectRenderContext.getBufferSource();
            if (this.renderType == null) {
                this.renderType = RenderTypeProvider.getInstance().getMuzzleFlashRenderType(this.texture);
            }
            vertexBuffer = bufferSource.getBuffer(this.renderType);
            vertexBuffer = VertexConsumers.ENTITY.apply(vertexBuffer);
        }
        return vertexBuffer;
    }

    @Override
    public void render(EffectRenderContext effectRenderContext) {
        if (ClientSystem.getInstance().getAuxLevelRenderer().isRendering()) {
            return;
        }
        PoseStack poseStack = effectRenderContext.getPoseStack();
        if (poseStack != null) {
            this.renderWithPose(effectRenderContext);
        } else {
            this.renderWithoutPose(effectRenderContext);
        }
    }

    private void renderWithoutPose(EffectRenderContext effectRenderContext) {
        float progress = effectRenderContext.getProgress();
        if (progress < 0.0f) {
            return;
        }
        if (this.poseProvider != null) {
            PoseStack poseStack = new PoseStack();
            poseStack.pushPose();
            this.adjustPose(poseStack, effectRenderContext);
            this.render(effectRenderContext, poseStack, this.getVertextBuffer(effectRenderContext), progress, effectRenderContext.getLightColor());
            poseStack.popPose();
        }
    }

    private void renderWithPose(EffectRenderContext effectRenderContext) {
        float progress = effectRenderContext.getProgress();
        if (progress < 0.0f) {
            return;
        }
        this.render(effectRenderContext, effectRenderContext.getPoseStack(), this.getVertextBuffer(effectRenderContext), progress, effectRenderContext.getLightColor());
    }

    private void render(EffectRenderContext effectRenderContext, PoseStack poseStack, VertexConsumer vertexBuffer, float progress, int lightColor) {
        float numRotations = 0.0f;
        float distance = this.distanceProvider.getValue();
        float[][] beamSegments = this.segmentsProvider.getSegments(distance, progress, this.lifetimeNanos);
        float distanceSoFar = beamSegments[0][0] + (beamSegments[0][1] - beamSegments[0][0]) * 0.5f;
        if (distanceSoFar >= distance) {
            return;
        }
        vertexBuffer = this.vertexConsumerTransformer.apply(vertexBuffer);
        float roll = -(effectRenderContext.getInitialAngle() + numRotations * 360.0f * progress);
        if (this.isGlowEnabled) {
            lightColor = 240;
        }
        SpriteUVProvider spriteUVProvider = effectRenderContext.getSpriteUVProvider();
        this.renderBladesWithRotations(poseStack, beamSegments, roll, vertexBuffer, progress, lightColor, spriteUVProvider);
        this.renderFaceWithRotations(poseStack, beamSegments, roll, vertexBuffer, progress, lightColor, spriteUVProvider);
    }

    private void adjustPose(PoseStack poseStack, EffectRenderContext context) {
        PoseStack.Pose currentItemPose = this.poseProvider.getPose();
        if (currentItemPose != null) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float)this.randomPitch * this.randomization));
            poseStack.mulPose(Axis.YP.rotationDegrees((float)this.randomYaw * this.randomization));
            Matrix4f modelMatrixAdjusted = new Matrix4f().mul((Matrix4fc)new Matrix4f((Matrix4fc)RenderUtil.getModelViewMatrixInverted())).mul((Matrix4fc)new Matrix4f((Matrix4fc)RenderUtil.getProjectionMatrixInverted())).mul((Matrix4fc)RenderUtil.getProjectionMatrixNormalFov()).mul((Matrix4fc)new Matrix4f((Matrix4fc)RenderSystem.getModelViewMatrix()));
            modelMatrixAdjusted = modelMatrixAdjusted.mul((Matrix4fc)currentItemPose.pose());
            poseStack.mulPose(modelMatrixAdjusted);
        }
    }

    private void renderFaceWithRotations(PoseStack poseStack, float[][] beamSegments, float rot, VertexConsumer vertexBuffer, float progress, int lightColor, SpriteUVProvider spriteUVProvider) {
        poseStack.pushPose();
        Quaternionf yQuaternion = Axis.YP.rotationDegrees(0.0f);
        poseStack.mulPose(yQuaternion);
        Quaternionf rollQ = Axis.XP.rotationDegrees(0.0f);
        poseStack.mulPose(rollQ);
        Quaternionf zQuaternion = Axis.ZP.rotationDegrees(-rot);
        poseStack.mulPose(zQuaternion);
        Matrix4f poseState = poseStack.last().pose();
        if (this.isFaceEnabled) {
            float faceZOffset = beamSegments[0][0] + (beamSegments[0][1] - beamSegments[0][0]) * 0.5f;
            this.renderFace(poseState, faceZOffset, vertexBuffer, progress, lightColor, spriteUVProvider);
        }
        poseStack.popPose();
    }

    private void renderBladesWithRotations(PoseStack poseStack, float[][] beamSegments, float rot, VertexConsumer vertexBuffer, float progress, int lightColor, SpriteUVProvider spriteUVProvider) {
        float bladeStep = 180.0f / (float)this.numBlades;
        for (int k = 0; k < this.numBlades; ++k) {
            poseStack.pushPose();
            Quaternionf yQuaternion = Axis.YP.rotationDegrees(90.0f);
            poseStack.mulPose(yQuaternion);
            float xRot = (float)k * bladeStep + rot;
            Quaternionf rollQ = Axis.XP.rotationDegrees(xRot);
            poseStack.mulPose(rollQ);
            Quaternionf zQuaternion = Axis.ZP.rotationDegrees(0.0f);
            poseStack.mulPose(zQuaternion);
            Matrix4f poseState = poseStack.last().pose();
            this.renderBlade(poseState, beamSegments, vertexBuffer, progress, lightColor, spriteUVProvider);
            poseStack.popPose();
        }
    }

    private void renderFace(Matrix4f transform, float zOffset, VertexConsumer vertexBuffer, float progress, int lightColor, SpriteUVProvider spriteUVProvider) {
        float[] uv = spriteUVProvider.getSpriteUV(progress);
        if (uv == null) {
            return;
        }
        float offset = 0.5f * this.faceWidthProvider.getValue(progress);
        float dynamicAlpha = this.alphaProvider.getValue(progress);
        float spriteWidth = uv[2] - uv[0];
        float minU = uv[0] + spriteWidth * this.faceTextureMinU;
        float minV = uv[1];
        float maxU = uv[0] + spriteWidth * this.faceTextureMaxU;
        float maxV = uv[3];
        float[][] texUV = new float[][]{{maxU, maxV}, {minU, maxV}, {minU, minV}, {maxU, minV}};
        float[][] positionOffsets = new float[][]{{offset, -offset, -zOffset}, {-offset, -offset, -zOffset}, {-offset, offset, -zOffset}, {offset, offset, -zOffset}};
        for (int k = 0; k < this.faceBrightness; ++k) {
            for (int i = 0; i < 4; ++i) {
                vertexBuffer.addVertex(transform, positionOffsets[i][0], positionOffsets[i][1], positionOffsets[i][2]).setUv(texUV[i][0], texUV[i][1]).setColor(1.0f, 1.0f, 1.0f, dynamicAlpha).setOverlay(0).setNormal(0.0f, 1.0f, 0.0f).setLight(lightColor);
            }
        }
    }

    private void renderBlade(Matrix4f transform, float[][] beamSegments, VertexConsumer vertexBuffer, float progress, int lightColor, SpriteUVProvider spriteUVProvider) {
        float beamSegmentWidth = 0.5f * this.bladeWidthProvider.getValue(progress);
        float dynamicAlpha = this.alphaProvider.getValue(progress);
        int numBeamSpriteSegments = beamSegments.length;
        float[] uv = spriteUVProvider.getSpriteUV(progress * (float)this.lifetimeNanos);
        float spriteWidth = uv[2] - uv[0];
        float spriteSegmentWidth = spriteWidth * (this.bladeTextureMaxU - this.bladeTextureMinU) / (float)numBeamSpriteSegments;
        for (int beamSegmentIndex = 0; beamSegmentIndex < numBeamSpriteSegments; ++beamSegmentIndex) {
            float minU = uv[0] + spriteWidth * this.bladeTextureMinU + spriteSegmentWidth * (float)beamSegmentIndex;
            float minV = uv[1];
            float maxU = minU + spriteSegmentWidth;
            float maxV = uv[3];
            float[][] texUV = new float[][]{{maxU, maxV}, {minU, maxV}, {minU, minV}, {maxU, minV}};
            float beamSegmentStart = beamSegments[beamSegmentIndex][0];
            float beamSegmentEnd = beamSegments[beamSegmentIndex][1];
            float[][] positionOffsets = new float[][]{{beamSegmentEnd, beamSegmentWidth, 0.0f}, {beamSegmentStart, beamSegmentWidth, 0.0f}, {beamSegmentStart, -beamSegmentWidth, 0.0f}, {beamSegmentEnd, -beamSegmentWidth, 0.0f}};
            for (int k = 0; k < this.bladeBrightness; ++k) {
                for (int i = 0; i < 4; ++i) {
                    vertexBuffer.addVertex(transform, positionOffsets[i][0], positionOffsets[i][1], positionOffsets[i][2]).setUv(texUV[i][0], texUV[i][1]).setColor(1.0f, 1.0f, 1.0f, dynamicAlpha).setNormal(0.0f, 1.0f, 0.0f).setOverlay(0).setLight(lightColor);
                }
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
    extends AbstractEffect.AbstractEffectBuilder<Builder, AttachedProjectileEffect> {
        private static final Set<GunItem.FirePhase> COMPATIBLE_PHASES = Set.of(GunItem.FirePhase.PREPARING, GunItem.FirePhase.FIRING, GunItem.FirePhase.HIT_SCAN_ACQUIRED, GunItem.FirePhase.COMPLETETING);
        public static final long DEFAULT_DURATION = 250L;
        public static final float MAX_DISTANCE = 200.0f;
        public static final int DEFAULT_NUM_BLADES = 0;
        public static final int DEFAULT_NUM_SPRITES = 1;
        public static final int DEFAULT_FACE_BRIGHTNESS = 2;
        public static final int DEFAULT_BLADE_BRIGHTNESS = 2;
        public static final int MAX_SPRITES = 200;
        public static final int MAX_BRIGHTNESS = 5;
        public static final int DEFAULT_SHOTS_PER_TRACE = 1;
        public static final SegmentsProviders.SegmentsProvider DEFAULT_SEGMENTS_PROVIDER = new SegmentsProviders.MovingSegmentsProvider(200.0f, -10.0f);
        private Interpolators.FloatInterpolator bladeWidthProvider;
        private Interpolators.FloatInterpolator faceWidthProvider;
        private Interpolators.FloatProvider bladeLengthProvider;
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
        }

        @Override
        public Builder withDuration(long durationMillis) {
            this.duration = durationMillis;
            return this;
        }

        public Builder withWidth(double width) {
            this.bladeWidthProvider = new Interpolators.EaseInEaseOutFloatProvider((float)width);
            this.faceWidthProvider = new Interpolators.EaseInEaseOutFloatProvider((float)width);
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

        public Builder withBladeLengthProvider(Interpolators.FloatProvider bladeLengthProvider) {
            this.bladeLengthProvider = bladeLengthProvider;
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
            this.withRotations(JsonUtil.getJsonFloat(obj, "numRotations", 0.0f));
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
            return true;
        }

        @Override
        public AttachedProjectileEffect build(EffectBuilder.Context context) {
            AttachedProjectileEffect effect = new AttachedProjectileEffect();
            super.apply(effect, context);
            effect.randomization = 0.0f;
            effect.faceWidthProvider = this.faceWidthProvider;
            effect.bladeWidthProvider = this.bladeWidthProvider;
            effect.distanceProvider = !MiscUtil.isNearlyZero(context.getDistance()) ? () -> context.getDistance() : this.bladeLengthProvider;
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
            PoseProvider poseProvider = context.getPoseProvider();
            if (poseProvider != null && effect.poseProvider == null) {
                effect.poseProvider = poseProvider;
            }
            return effect;
        }

        @Override
        public Collection<GunItem.FirePhase> getCompatiblePhases() {
            return COMPATIBLE_PHASES;
        }
    }
}

