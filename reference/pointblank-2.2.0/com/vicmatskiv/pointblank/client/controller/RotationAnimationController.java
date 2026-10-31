/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  java.lang.MatchException
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.world.entity.LivingEntity
 *  org.joml.Quaternionf
 *  software.bernie.geckolib.cache.object.GeoBone
 */
package com.vicmatskiv.pointblank.client.controller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.controller.AbstractProceduralAnimationController;
import com.vicmatskiv.pointblank.client.render.GunItemRenderer;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.Locale;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.GeoBone;

public class RotationAnimationController
extends AbstractProceduralAnimationController {
    private static float MODEL_SCALE = 0.0625f;
    private Phase phase = Phase.IDLE;
    private long phaseStartTime;
    private double phaseStartDistance;
    private double rotationsPerSecond = 5.0;
    private double decelerationRate = 5.0;
    private double accelerationRate = 1.0;
    private PhaseMapper phaseMapper;
    private RotationAxis rotationAxis = RotationAxis.Z;
    private RotationDirection rotationDirection = RotationDirection.CLOCKWISE;
    private DistanceFunction phaseDistanceFunction = ZeroDistance.INSTANCE;

    private RotationAnimationController() {
        super(0L);
    }

    @Override
    public void onStateTick(LivingEntity player, GunClientState state) {
        GunClientState.FireState fireState = state.getFireState();
        Phase newPhase = this.phaseMapper.getPhase(fireState);
        if (newPhase != this.phase) {
            double elapsedPhaseTime = (double)(System.nanoTime() - this.phaseStartTime) / 1.0E9;
            double previousVelocity = this.phaseDistanceFunction.getVelocity(elapsedPhaseTime);
            double previousPhaseDurationSeconds = (double)(System.nanoTime() - this.phaseStartTime) / 1.0E9;
            double previousPhaseDistance = this.phaseDistanceFunction.getDistance(previousPhaseDurationSeconds);
            this.phaseStartDistance += previousPhaseDistance;
            this.phaseStartTime = System.nanoTime();
            switch (newPhase.ordinal()) {
                case 0: {
                    this.phaseDistanceFunction = new DecelerationDistance(previousVelocity, this.decelerationRate);
                    break;
                }
                case 1: {
                    this.phaseDistanceFunction = new AccelerationDistance(previousVelocity, this.accelerationRate);
                    break;
                }
                case 2: {
                    this.phaseDistanceFunction = new AccelerationDistance(previousVelocity, this.accelerationRate);
                    break;
                }
                case 3: {
                    this.phaseDistanceFunction = new DecelerationDistance(previousVelocity, this.decelerationRate);
                }
            }
            this.phase = newPhase;
        }
    }

    public void render(GunItemRenderer renderer, GunClientState gunClientState, PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();
        float xPivot = bone.getPivotX() * MODEL_SCALE;
        float yPivot = bone.getPivotY() * MODEL_SCALE;
        float zPivot = bone.getPivotZ() * MODEL_SCALE;
        double elapsedPhaseTime = (double)(System.nanoTime() - this.phaseStartTime) / 1.0E9;
        double elapsedPhaseDistance = this.phaseDistanceFunction.getDistance(elapsedPhaseTime);
        double distance = this.phaseStartDistance + elapsedPhaseDistance;
        double currentDistanceInDegrees = this.rotationsPerSecond * distance * 360.0;
        poseStack.translate(xPivot, yPivot, zPivot);
        poseStack.mulPose(this.getRotationQuaternion(currentDistanceInDegrees));
        poseStack.translate(-xPivot, -yPivot, -zPivot);
        renderer.renderCubesOfBoneParent(poseStack, bone, buffer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }

    public void renderRecursively(GunItemRenderer renderer, PoseStack poseStack, GunItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();
        float xPivot = bone.getPivotX() * MODEL_SCALE;
        float yPivot = bone.getPivotY() * MODEL_SCALE;
        float zPivot = bone.getPivotZ() * MODEL_SCALE;
        double elapsedPhaseTime = (double)(System.nanoTime() - this.phaseStartTime) / 1.0E9;
        double elapsedPhaseDistance = this.phaseDistanceFunction.getDistance(elapsedPhaseTime);
        double distance = this.phaseStartDistance + elapsedPhaseDistance;
        double currentDistanceInDegrees = this.rotationsPerSecond * distance * 360.0;
        poseStack.translate(xPivot, yPivot, zPivot);
        poseStack.mulPose(this.getRotationQuaternion(currentDistanceInDegrees));
        poseStack.translate(-xPivot, -yPivot, -zPivot);
        renderer.renderRecursivelySuper(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
        poseStack.popPose();
    }

    private Quaternionf getRotationQuaternion(double currentDistanceInDegrees) {
        float rotationInRadians = (float)Math.PI / 180 * this.rotationDirection.toSignedDegrees((float)currentDistanceInDegrees);
        return switch (this.rotationAxis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> new Quaternionf().rotationX(rotationInRadians);
            case 1 -> new Quaternionf().rotationY(rotationInRadians);
            case 2 -> new Quaternionf().rotationZ(rotationInRadians);
        };
    }

    private static String normalizeValue(String value) {
        return value == null ? "" : value.trim().replace("-", "").replace("_", "").replace(" ", "").toUpperCase(Locale.ROOT);
    }

    public static enum Phase {
        IDLE,
        PREPARING,
        RUNNING,
        COMPLETING;

    }

    public static enum RotationAxis {
        X,
        Y,
        Z;


        public static RotationAxis fromJsonValue(String value) {
            String normalized = RotationAnimationController.normalizeValue(value);
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("Invalid rotation axis: " + value);
            }
            return switch (normalized.charAt(0)) {
                case 'X' -> X;
                case 'Y' -> Y;
                case 'Z' -> Z;
                default -> throw new IllegalArgumentException("Invalid rotation axis: " + value);
            };
        }
    }

    public static enum RotationDirection {
        CLOCKWISE(-1.0f),
        COUNTERCLOCKWISE(1.0f);

        private final float multiplier;

        private RotationDirection(float multiplier) {
            this.multiplier = multiplier;
        }

        public float toSignedDegrees(float degrees) {
            return this.multiplier * degrees;
        }

        public static RotationDirection fromJsonValue(String value) {
            String normalized;
            return switch (normalized = RotationAnimationController.normalizeValue(value)) {
                case "CLOCKWISE" -> CLOCKWISE;
                case "COUNTERCLOCKWISE" -> COUNTERCLOCKWISE;
                default -> throw new IllegalArgumentException("Invalid rotation direction: " + value);
            };
        }
    }

    private static class ZeroDistance
    implements DistanceFunction {
        static final ZeroDistance INSTANCE = new ZeroDistance();

        private ZeroDistance() {
        }

        @Override
        public double getDistance(double t) {
            return 0.0;
        }

        @Override
        public double getVelocity(double t) {
            return 0.0;
        }
    }

    private static interface DistanceFunction {
        public double getDistance(double var1);

        public double getVelocity(double var1);
    }

    public static interface PhaseMapper {
        public Phase getPhase(GunClientState.FireState var1);
    }

    private static class DecelerationDistance
    implements DistanceFunction {
        private double deceleration;
        private double initialVelocity;

        public DecelerationDistance(double initialVelocity, double deceleration) {
            this.deceleration = deceleration;
            this.initialVelocity = initialVelocity;
        }

        @Override
        public double getDistance(double t) {
            double finalVelocity = this.initialVelocity - t * this.deceleration;
            if (finalVelocity < 0.0) {
                double tStop = this.initialVelocity / this.deceleration;
                return this.initialVelocity * tStop - 0.5 * this.deceleration * tStop * tStop;
            }
            return this.initialVelocity * t - 0.5 * this.deceleration * t * t;
        }

        @Override
        public double getVelocity(double t) {
            double finalVelocity = this.initialVelocity - t * this.deceleration;
            return Math.max(finalVelocity, 0.0);
        }
    }

    private static class AccelerationDistance
    implements DistanceFunction {
        private double acceleration;
        private double initialVelocity;

        public AccelerationDistance(double initialVelocity, double acceleration) {
            this.initialVelocity = initialVelocity;
            this.acceleration = acceleration;
        }

        @Override
        public double getDistance(double t) {
            double tMax;
            double distanceWithConstantSpeed = 0.0;
            double finalVelocity = this.initialVelocity + this.acceleration * t;
            if (finalVelocity > 1.0) {
                tMax = Math.max((1.0 - this.initialVelocity) / this.acceleration, 0.0);
                distanceWithConstantSpeed = t - tMax;
            } else {
                tMax = t;
            }
            return this.initialVelocity * tMax + 0.5 * this.acceleration * tMax * tMax + distanceWithConstantSpeed;
        }

        @Override
        public double getVelocity(double t) {
            double finalVelocity = this.initialVelocity + this.acceleration * t;
            return Math.min(finalVelocity, 1.0);
        }
    }

    public static class Builder {
        private String modelPartName;
        private double rotationsPerSecond = 5.0;
        private double decelerationRate = 5.0;
        private double accelerationRate = 1.0;
        private PhaseMapper phaseMapper = FirePhaseMapper.INSTANCE;
        private RotationAxis rotationAxis = RotationAxis.Z;
        private RotationDirection rotationDirection = RotationDirection.CLOCKWISE;

        public String getModelPartName() {
            return this.modelPartName;
        }

        public Builder withPhase(String phase) {
            switch (phase) {
                case "fire": {
                    this.phaseMapper = FirePhaseMapper.INSTANCE;
                    break;
                }
                case "reload": {
                    this.phaseMapper = ReloadPhaseMapper.INSTANCE;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Invalid phase name: " + phase);
                }
            }
            return this;
        }

        public Builder withPhaseMapper(PhaseMapper phaseMapper) {
            this.phaseMapper = phaseMapper;
            return this;
        }

        public Builder withModelPart(String modelPartName) {
            this.modelPartName = modelPartName;
            return this;
        }

        public Builder withDecelerationRate(double decelerationRate) {
            this.decelerationRate = decelerationRate;
            return this;
        }

        public Builder withAccelerationRate(double accelerationRate) {
            this.accelerationRate = accelerationRate;
            return this;
        }

        public Builder withRotationsPerMinute(double rotationsPerMinute) {
            this.rotationsPerSecond = rotationsPerMinute / 60.0;
            return this;
        }

        public Builder withRotationAxis(RotationAxis rotationAxis) {
            this.rotationAxis = rotationAxis;
            return this;
        }

        public Builder withRotationDirection(RotationDirection rotationDirection) {
            this.rotationDirection = rotationDirection;
            return this;
        }

        public RotationAnimationController build() {
            if (this.modelPartName == null) {
                throw new IllegalStateException("Invalid rotation configutaion, missing model part name");
            }
            RotationAnimationController controller = new RotationAnimationController();
            controller.accelerationRate = this.accelerationRate;
            controller.decelerationRate = this.decelerationRate;
            controller.rotationsPerSecond = this.rotationsPerSecond;
            controller.phaseMapper = this.phaseMapper;
            controller.rotationAxis = this.rotationAxis;
            controller.rotationDirection = this.rotationDirection;
            return controller;
        }
    }

    private static class ConstantDistance
    implements DistanceFunction {
        static final ConstantDistance INSTANCE = new ConstantDistance();

        private ConstantDistance() {
        }

        @Override
        public double getDistance(double t) {
            return t;
        }

        @Override
        public double getVelocity(double t) {
            return t;
        }
    }

    private static class ReloadPhaseMapper
    implements PhaseMapper {
        static final ReloadPhaseMapper INSTANCE = new ReloadPhaseMapper();

        private ReloadPhaseMapper() {
        }

        @Override
        public Phase getPhase(GunClientState.FireState fireState) {
            return switch (fireState) {
                case GunClientState.FireState.PREPARE_RELOAD, GunClientState.FireState.PREPARE_RELOAD_COOLDOWN, GunClientState.FireState.PREPARE_RELOAD_ITER, GunClientState.FireState.PREPARE_RELOAD_COOLDOWN_ITER -> Phase.PREPARING;
                case GunClientState.FireState.RELOAD, GunClientState.FireState.RELOAD_COOLDOWN, GunClientState.FireState.RELOAD_ITER, GunClientState.FireState.RELOAD_COOLDOWN_ITER -> Phase.RUNNING;
                case GunClientState.FireState.COMPLETE_RELOAD, GunClientState.FireState.COMPLETE_RELOAD_COOLDOWN -> Phase.COMPLETING;
                default -> Phase.IDLE;
            };
        }
    }

    private static class FirePhaseMapper
    implements PhaseMapper {
        static final FirePhaseMapper INSTANCE = new FirePhaseMapper();

        private FirePhaseMapper() {
        }

        @Override
        public Phase getPhase(GunClientState.FireState fireState) {
            return switch (fireState) {
                case GunClientState.FireState.PREPARE_FIRE_SINGLE, GunClientState.FireState.PREPARE_FIRE_COOLDOWN_SINGLE, GunClientState.FireState.PREPARE_FIRE_AUTO, GunClientState.FireState.PREPARE_FIRE_COOLDOWN_AUTO, GunClientState.FireState.PREPARE_FIRE_BURST, GunClientState.FireState.PREPARE_FIRE_COOLDOWN_BURST -> Phase.PREPARING;
                case GunClientState.FireState.FIRE_SINGLE, GunClientState.FireState.FIRE_COOLDOWN_SINGLE, GunClientState.FireState.FIRE_AUTO, GunClientState.FireState.FIRE_COOLDOWN_AUTO, GunClientState.FireState.FIRE_BURST, GunClientState.FireState.FIRE_COOLDOWN_BURST -> Phase.RUNNING;
                case GunClientState.FireState.COMPLETE_FIRE, GunClientState.FireState.COMPLETE_FIRE_COOLDOWN -> Phase.COMPLETING;
                default -> Phase.IDLE;
            };
        }
    }
}

