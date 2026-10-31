/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  net.minecraft.util.Mth
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animation.AnimatableManager
 *  software.bernie.geckolib.animation.Animation
 *  software.bernie.geckolib.animation.Animation$Keyframes
 *  software.bernie.geckolib.animation.Animation$LoopType
 *  software.bernie.geckolib.animation.AnimationController
 *  software.bernie.geckolib.animation.AnimationController$State
 *  software.bernie.geckolib.animation.AnimationProcessor
 *  software.bernie.geckolib.animation.AnimationProcessor$QueuedAnimation
 *  software.bernie.geckolib.animation.AnimationState
 *  software.bernie.geckolib.animation.ContextAwareAnimatableManager
 *  software.bernie.geckolib.animation.EasingType
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.animation.RawAnimation$Stage
 *  software.bernie.geckolib.animation.keyframe.AnimationPoint
 *  software.bernie.geckolib.animation.keyframe.BoneAnimation
 *  software.bernie.geckolib.animation.keyframe.BoneAnimationQueue
 *  software.bernie.geckolib.animation.keyframe.event.data.CustomInstructionKeyframeData
 *  software.bernie.geckolib.animation.keyframe.event.data.ParticleKeyframeData
 *  software.bernie.geckolib.animation.keyframe.event.data.SoundKeyframeData
 *  software.bernie.geckolib.animation.state.BoneSnapshot
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.model.GeoModel
 */
package com.vicmatskiv.pointblank.client.controller;

import com.vicmatskiv.pointblank.Constants;
import com.vicmatskiv.pointblank.client.controller.BlendingAnimationController;
import com.vicmatskiv.pointblank.client.controller.GunStateAnimationController;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.mixin.AnimatableManagerAccessor;
import com.vicmatskiv.pointblank.mixin.AnimationControllerAccessor;
import com.vicmatskiv.pointblank.util.AnimationPointInfo2;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.ContextAwareAnimatableManager;
import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.AnimationPoint;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.animation.keyframe.BoneAnimationQueue;
import software.bernie.geckolib.animation.keyframe.event.data.CustomInstructionKeyframeData;
import software.bernie.geckolib.animation.keyframe.event.data.ParticleKeyframeData;
import software.bernie.geckolib.animation.keyframe.event.data.SoundKeyframeData;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class BlendingAnimationProcessor<T extends GeoAnimatable>
extends AnimationProcessor<T> {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    public static final String BONE_ANIMATION_POINT_DEFAULT = "_default";
    private final Map<String, GeoBone> bones = new Object2ObjectOpenHashMap();
    private final GeoModel<T> model;
    private final Map<AnimatableManager<T>, HideRenderState> hideRenderStates = new WeakHashMap<AnimatableManager<T>, HideRenderState>();
    public boolean reloadAnimations = false;
    static final String WAIT = "internal.wait";

    public BlendingAnimationProcessor(GeoModel<T> model) {
        super(null);
        this.model = model;
    }

    public Queue<AnimationProcessor.QueuedAnimation> buildAnimationQueue(T animatable, RawAnimation rawAnimation) {
        LinkedList<AnimationProcessor.QueuedAnimation> animations = new LinkedList<AnimationProcessor.QueuedAnimation>();
        boolean error = false;
        for (RawAnimation.Stage stage : rawAnimation.getAnimationStages()) {
            Animation animation = stage.animationName().equals(WAIT) ? BlendingAnimationProcessor.generateWaitAnimation(stage.additionalTicks()) : this.model.getAnimation(animatable, stage.animationName());
            if (animation == null) {
                System.out.println("Unable to find animation: " + stage.animationName() + " for " + animatable.getClass().getSimpleName());
                error = true;
                continue;
            }
            animations.add(new AnimationProcessor.QueuedAnimation(animation, stage.loopType()));
        }
        return error ? null : animations;
    }

    static Animation generateWaitAnimation(double length) {
        return new Animation(WAIT, length, Animation.LoopType.PLAY_ONCE, new BoneAnimation[0], new Animation.Keyframes(new SoundKeyframeData[0], new ParticleKeyframeData[0], new CustomInstructionKeyframeData[0]));
    }

    private static boolean isDualWieldAnimation(String animationName) {
        return animationName != null && (animationName.endsWith("_dwleft") || animationName.endsWith("_dwright"));
    }

    private static boolean isArmBone(String boneName) {
        return "leftarm".equalsIgnoreCase(boneName) || "rightarm".equalsIgnoreCase(boneName);
    }

    public void tickAnimation(T animatable, GeoModel<T> model, AnimatableManager<T> animatableManager, double animTime, AnimationState<T> state, boolean crashWhenCantFindBone) {
        boolean hideRunStarted;
        AnimatableManager<T> activeManager = BlendingAnimationProcessor.getActiveManager(animatableManager);
        if (this.reloadAnimations) {
            this.hideRenderStates.clear();
        }
        HideRenderState hideRenderState = this.hideRenderStates.computeIfAbsent(activeManager, ignored -> new HideRenderState());
        LinkedHashMap<GeoBone, Map> animationPoints = new LinkedHashMap<GeoBone, Map>();
        Map<String, BoneSnapshot> globalBoneSnapshots = this.updateBoneSnapshots(activeManager.getBoneSnapshotCollection());
        Set snapshotSets = Collections.newSetFromMap(new IdentityHashMap());
        snapshotSets.add(globalBoneSnapshots);
        HashMap<String, AnimationController.State> defaultLayerControllerStates = new HashMap<String, AnimationController.State>();
        boolean hideProducedBoneData = false;
        AnimationController.State hideControllerState = AnimationController.State.STOPPED;
        AnimationController.State walkingControllerState = AnimationController.State.STOPPED;
        double hideControllerTransitionLength = 0.0;
        String walkingAnimationName = null;
        HashSet<String> hideOwnedBoneNames = new HashSet<String>();
        if (this.reloadAnimations) {
            hideRenderState.heldAnimationPoints.clear();
            hideRenderState.previousControllerState = AnimationController.State.STOPPED;
            hideRenderState.clearRebase();
        }
        for (AnimationController animationController : activeManager.getAnimationControllers().values()) {
            boolean hideControllerOwnsPose;
            if (this.reloadAnimations) {
                animationController.forceAnimationReset();
                animationController.getBoneAnimationQueues().clear();
            }
            AnimationControllerAccessor animationControllerAccessor = (AnimationControllerAccessor)animationController;
            animationControllerAccessor.setIsJustStarting(activeManager.isFirstTick());
            state.withController(animationController);
            animationController.process(model, state, this.bones, globalBoneSnapshots, animTime, crashWhenCantFindBone);
            boolean isDefaultLayerController = !(animationController instanceof BlendingAnimationController);
            boolean isHideController = "hide_controller".equals(animationController.getName());
            boolean isWalkingController = "walking".equals(animationController.getName());
            AnimationController.State controllerState = animationController.getAnimationState();
            boolean bl = hideControllerOwnsPose = isHideController && (controllerState == AnimationController.State.RUNNING || controllerState == AnimationController.State.TRANSITIONING || controllerState == AnimationController.State.PAUSED);
            if (isWalkingController) {
                walkingControllerState = controllerState;
                AnimationProcessor.QueuedAnimation walkingAnimation = animationController.getCurrentAnimation();
                String string = walkingAnimationName = walkingAnimation != null ? walkingAnimation.animation().name() : null;
            }
            if (isHideController) {
                hideControllerState = controllerState;
                if (animationController instanceof GunStateAnimationController) {
                    GunStateAnimationController gunStateController = (GunStateAnimationController)animationController;
                    hideControllerTransitionLength = gunStateController.getTransitionLengthTicks();
                }
                if (hideControllerOwnsPose && (hideRenderState.previousControllerState == AnimationController.State.STOPPED || hideRenderState.previousControllerState == AnimationController.State.PAUSED && controllerState != AnimationController.State.PAUSED)) {
                    hideRenderState.heldAnimationPoints.clear();
                }
            }
            for (BoneAnimationQueue boneAnimation : animationController.getBoneAnimationQueues().values()) {
                GeoBone bone = boneAnimation.bone();
                AnimationPoint rotXPoint = (AnimationPoint)boneAnimation.rotationXQueue().poll();
                AnimationPoint rotYPoint = (AnimationPoint)boneAnimation.rotationYQueue().poll();
                AnimationPoint rotZPoint = (AnimationPoint)boneAnimation.rotationZQueue().poll();
                AnimationPoint posXPoint = (AnimationPoint)boneAnimation.positionXQueue().poll();
                AnimationPoint posYPoint = (AnimationPoint)boneAnimation.positionYQueue().poll();
                AnimationPoint posZPoint = (AnimationPoint)boneAnimation.positionZQueue().poll();
                AnimationPoint scaleXPoint = (AnimationPoint)boneAnimation.scaleXQueue().poll();
                AnimationPoint scaleYPoint = (AnimationPoint)boneAnimation.scaleYQueue().poll();
                AnimationPoint scaleZPoint = (AnimationPoint)boneAnimation.scaleZQueue().poll();
                if (isWalkingController && BlendingAnimationProcessor.isDualWieldAnimation(walkingAnimationName) && BlendingAnimationProcessor.isArmBone(bone.getName())) continue;
                EasingType easingType = animationControllerAccessor.getOverrideEasingTypeFunction().apply(animatable);
                AnimationPointInfo2 info = new AnimationPointInfo2(easingType);
                if (rotXPoint != null && rotYPoint != null && rotZPoint != null) {
                    info.setRotXPoint(rotXPoint);
                    info.setRotYPoint(rotYPoint);
                    info.setRotZPoint(rotZPoint);
                }
                if (posXPoint != null && posYPoint != null && posZPoint != null) {
                    info.setPosXPoint(posXPoint);
                    info.setPosYPoint(posYPoint);
                    info.setPosZPoint(posZPoint);
                }
                if (scaleXPoint != null && scaleYPoint != null && scaleZPoint != null) {
                    info.setScaleXPoint(scaleXPoint);
                    info.setScaleYPoint(scaleYPoint);
                    info.setScaleZPoint(scaleZPoint);
                }
                if (!info.isPositionChanged() && !info.isRotationChanged() && !info.isScaleChanged()) continue;
                if (hideControllerOwnsPose) {
                    hideProducedBoneData = true;
                    hideOwnedBoneNames.add(bone.getName());
                    hideRenderState.heldAnimationPoints.put(bone.getName(), info);
                }
                Map boneAnimationPoints = animationPoints.computeIfAbsent(bone, b -> new HashMap());
                Object controllerKey = isDefaultLayerController ? "_default:" + animationController.getName() : animationController.getName();
                boneAnimationPoints.put(controllerKey, info);
                if (!isDefaultLayerController) continue;
                defaultLayerControllerStates.put(animationController.getName(), animationController.getAnimationState());
            }
        }
        if (hideControllerState == AnimationController.State.PAUSED) {
            defaultLayerControllerStates.put("hide_controller", hideControllerState);
            for (Map.Entry entry : hideRenderState.heldAnimationPoints.entrySet()) {
                GeoBone geoBone = this.bones.get(entry.getKey());
                if (geoBone == null) continue;
                animationPoints.computeIfAbsent(geoBone, ignored -> new HashMap()).putIfAbsent("_default:hide_controller", (AnimationPointInfo2)entry.getValue());
                hideProducedBoneData = true;
                hideOwnedBoneNames.add(geoBone.getName());
            }
        } else if (hideControllerState == AnimationController.State.STOPPED) {
            hideRenderState.heldAnimationPoints.clear();
            hideRenderState.clearRebase();
        }
        boolean bl = hideRunStarted = hideControllerState != AnimationController.State.STOPPED && hideControllerState != AnimationController.State.PAUSED && (hideRenderState.previousControllerState == AnimationController.State.STOPPED || hideRenderState.previousControllerState == AnimationController.State.PAUSED);
        if (hideRunStarted) {
            hideRenderState.clearRebase();
            if (hideControllerTransitionLength <= 0.0 && BlendingAnimationProcessor.isActiveLocomotion(walkingControllerState, walkingAnimationName)) {
                for (Map.Entry<String, BoneSnapshot> entry : globalBoneSnapshots.entrySet()) {
                    hideRenderState.rebaseSourceSnapshots.put(entry.getKey(), BoneSnapshot.copy((BoneSnapshot)entry.getValue()));
                }
                hideRenderState.rebasingFromLocomotion = true;
            }
        }
        if (Constants.DEBUG_HIDE_ANIMATION && hideControllerState != hideRenderState.previousControllerState) {
            LOGGER.info("[HideSwapTrace] processor-state {}->{} producedBoneData={} heldBones={} walkingAnimation={} locomotionRebase={} transitionTicks={}", (Object)hideRenderState.previousControllerState, (Object)hideControllerState, (Object)hideProducedBoneData, hideRenderState.heldAnimationPoints.keySet(), walkingAnimationName, (Object)hideRenderState.rebasingFromLocomotion, (Object)hideControllerTransitionLength);
        }
        hideRenderState.previousControllerState = hideControllerState;
        if (hideProducedBoneData) {
            for (Map map : animationPoints.values()) {
                map.remove("walking");
            }
        }
        for (Map.Entry entry : animationPoints.entrySet()) {
            Map bonePoints = (Map)entry.getValue();
            AnimationPointInfo2 bestDefaultInfo = null;
            AnimationController.State bestState = null;
            ArrayList<String> defaultLayerKeys = new ArrayList<String>();
            for (String key : bonePoints.keySet()) {
                if (!key.startsWith("_default:")) continue;
                defaultLayerKeys.add(key);
                String controllerName = key.substring(BONE_ANIMATION_POINT_DEFAULT.length() + 1);
                AnimationController.State controllerState = (AnimationController.State)defaultLayerControllerStates.get(controllerName);
                if (hideProducedBoneData && "hide_controller".equals(controllerName)) {
                    bestDefaultInfo = (AnimationPointInfo2)bonePoints.get(key);
                    bestState = controllerState;
                    continue;
                }
                if (hideProducedBoneData && bestDefaultInfo != null && bonePoints.containsKey("_default:hide_controller")) continue;
                boolean isBetter = false;
                if (bestState == null) {
                    isBetter = true;
                } else if (controllerState == AnimationController.State.RUNNING && bestState != AnimationController.State.RUNNING) {
                    isBetter = true;
                } else if (controllerState == AnimationController.State.TRANSITIONING && bestState != AnimationController.State.RUNNING && bestState != AnimationController.State.TRANSITIONING) {
                    isBetter = true;
                }
                if (!isBetter) continue;
                bestDefaultInfo = (AnimationPointInfo2)bonePoints.get(key);
                bestState = controllerState;
            }
            for (String key : defaultLayerKeys) {
                bonePoints.remove(key);
            }
            if (bestDefaultInfo == null) continue;
            bonePoints.put(BONE_ANIMATION_POINT_DEFAULT, bestDefaultInfo);
        }
        this.reloadAnimations = false;
        double d = animatable.getBoneResetTime();
        for (Map.Entry e : animationPoints.entrySet()) {
            HideStartPose hideStartPose;
            Map pointInfoByControllers = (Map)e.getValue();
            if (pointInfoByControllers == null) continue;
            double rotXPoint = 0.0;
            double rotYPoint = 0.0;
            double rotZPoint = 0.0;
            double posXPoint = 0.0;
            double posYPoint = 0.0;
            double posZPoint = 0.0;
            double scaleXPoint = 0.0;
            double scaleYPoint = 0.0;
            double scaleZPoint = 0.0;
            boolean rotationChanged = false;
            boolean positionChanged = false;
            boolean scaleChanged = false;
            for (Map.Entry e2 : pointInfoByControllers.entrySet()) {
                AnimationPointInfo2 pointInfo = (AnimationPointInfo2)e2.getValue();
                if (pointInfo.isRotationChanged()) {
                    rotXPoint += (double)((float)EasingType.lerpWithOverride((AnimationPoint)pointInfo.getRotXPoint(), (EasingType)pointInfo.getEasingType()));
                    rotYPoint += (double)((float)EasingType.lerpWithOverride((AnimationPoint)pointInfo.getRotYPoint(), (EasingType)pointInfo.getEasingType()));
                    rotZPoint += (double)((float)EasingType.lerpWithOverride((AnimationPoint)pointInfo.getRotZPoint(), (EasingType)pointInfo.getEasingType()));
                    rotationChanged = true;
                }
                if (pointInfo.isPositionChanged()) {
                    posXPoint += (double)((float)EasingType.lerpWithOverride((AnimationPoint)pointInfo.getPosXPoint(), (EasingType)pointInfo.getEasingType()));
                    posYPoint += (double)((float)EasingType.lerpWithOverride((AnimationPoint)pointInfo.getPosYPoint(), (EasingType)pointInfo.getEasingType()));
                    posZPoint += (double)((float)EasingType.lerpWithOverride((AnimationPoint)pointInfo.getPosZPoint(), (EasingType)pointInfo.getEasingType()));
                    positionChanged = true;
                }
                if (!pointInfo.isScaleChanged()) continue;
                scaleXPoint += (double)((float)EasingType.lerpWithOverride((AnimationPoint)pointInfo.getScaleXPoint(), (EasingType)pointInfo.getEasingType()));
                scaleYPoint += (double)((float)EasingType.lerpWithOverride((AnimationPoint)pointInfo.getScaleYPoint(), (EasingType)pointInfo.getEasingType()));
                scaleZPoint += (double)((float)EasingType.lerpWithOverride((AnimationPoint)pointInfo.getScaleZPoint(), (EasingType)pointInfo.getEasingType()));
                scaleChanged = true;
            }
            GeoBone bone = this.bones.get(((GeoBone)e.getKey()).getName());
            BoneSnapshot snapshot = globalBoneSnapshots.get(bone.getName());
            BoneSnapshot hideRebaseSource = hideRenderState.rebasingFromLocomotion && hideOwnedBoneNames.contains(bone.getName()) ? hideRenderState.rebaseSourceSnapshots.get(bone.getName()) : null;
            HideStartPose hideStartPose2 = hideStartPose = hideRebaseSource != null ? hideRenderState.hideStartPoses.computeIfAbsent(bone.getName(), ignored -> new HideStartPose()) : null;
            if (rotationChanged) {
                BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
                float targetRotX = (float)rotXPoint + initialSnapshot.getRotX();
                float targetRotY = (float)rotYPoint + initialSnapshot.getRotY();
                float targetRotZ = (float)rotZPoint + initialSnapshot.getRotZ();
                if (hideStartPose != null) {
                    if (!hideStartPose.hasRotation) {
                        hideStartPose.rotX = targetRotX;
                        hideStartPose.rotY = targetRotY;
                        hideStartPose.rotZ = targetRotZ;
                        hideStartPose.hasRotation = true;
                    }
                    targetRotX = hideRebaseSource.getRotX() + targetRotX - hideStartPose.rotX;
                    targetRotY = hideRebaseSource.getRotY() + targetRotY - hideStartPose.rotY;
                    targetRotZ = hideRebaseSource.getRotZ() + targetRotZ - hideStartPose.rotZ;
                }
                bone.setRotX(targetRotX);
                bone.setRotY(targetRotY);
                bone.setRotZ(targetRotZ);
                snapshot.updateRotation(bone.getRotX(), bone.getRotY(), bone.getRotZ());
                snapshot.startRotAnim();
                bone.markRotationAsChanged();
            }
            if (positionChanged) {
                float targetPosX = (float)posXPoint;
                float targetPosY = (float)posYPoint;
                float targetPosZ = (float)posZPoint;
                if (hideStartPose != null) {
                    if (!hideStartPose.hasPosition) {
                        hideStartPose.posX = targetPosX;
                        hideStartPose.posY = targetPosY;
                        hideStartPose.posZ = targetPosZ;
                        hideStartPose.hasPosition = true;
                    }
                    targetPosX = hideRebaseSource.getOffsetX() + targetPosX - hideStartPose.posX;
                    targetPosY = hideRebaseSource.getOffsetY() + targetPosY - hideStartPose.posY;
                    targetPosZ = hideRebaseSource.getOffsetZ() + targetPosZ - hideStartPose.posZ;
                }
                bone.setPosX(targetPosX);
                bone.setPosY(targetPosY);
                bone.setPosZ(targetPosZ);
                snapshot.updateOffset(bone.getPosX(), bone.getPosY(), bone.getPosZ());
                snapshot.startPosAnim();
                bone.markPositionAsChanged();
            }
            if (!scaleChanged) continue;
            float targetScaleX = (float)scaleXPoint;
            float targetScaleY = (float)scaleYPoint;
            float targetScaleZ = (float)scaleZPoint;
            if (hideStartPose != null) {
                if (!hideStartPose.hasScale) {
                    hideStartPose.scaleX = targetScaleX;
                    hideStartPose.scaleY = targetScaleY;
                    hideStartPose.scaleZ = targetScaleZ;
                    hideStartPose.hasScale = true;
                }
                targetScaleX = hideRebaseSource.getScaleX() + targetScaleX - hideStartPose.scaleX;
                targetScaleY = hideRebaseSource.getScaleY() + targetScaleY - hideStartPose.scaleY;
                targetScaleZ = hideRebaseSource.getScaleZ() + targetScaleZ - hideStartPose.scaleZ;
            }
            bone.setScaleX(targetScaleX);
            bone.setScaleY(targetScaleY);
            bone.setScaleZ(targetScaleZ);
            snapshot.updateScale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
            snapshot.startScaleAnim();
            bone.markScaleAsChanged();
        }
        this.resetBones(this.getRegisteredBones(), globalBoneSnapshots, activeManager, animTime, d);
        ((AnimatableManagerAccessor)activeManager).setIsFirstTick(false);
    }

    private static <A extends GeoAnimatable> AnimatableManager<A> getActiveManager(AnimatableManager<A> manager) {
        ContextAwareAnimatableManager contextAware;
        ContextAwareAnimatableManager typedManager;
        AnimatableManager contextManager;
        if (manager instanceof ContextAwareAnimatableManager && (contextManager = (typedManager = (contextAware = (ContextAwareAnimatableManager)manager)).getManagerForContext(typedManager.getCurrentContext())) != null) {
            return contextManager;
        }
        return manager;
    }

    private static boolean isActiveLocomotion(AnimationController.State controllerState, String animationName) {
        if (controllerState == AnimationController.State.STOPPED || animationName == null) {
            return false;
        }
        String baseAnimationName = GunItem.stripDualWieldAnimationSuffix(animationName);
        return controllerState == AnimationController.State.TRANSITIONING || !"animation.model.standing".equals(baseAnimationName);
    }

    private void resetBones(Collection<GeoBone> bones, Map<String, BoneSnapshot> boneSnapshots, AnimatableManager<T> animatableManager, double animTime, double resetTickLength) {
        for (GeoBone bone : bones) {
            double percentageReset;
            BoneSnapshot saveSnapshot;
            BoneSnapshot initialSnapshot;
            if (!bone.hasRotationChanged()) {
                initialSnapshot = bone.getInitialSnapshot();
                saveSnapshot = boneSnapshots.get(bone.getName());
                if (saveSnapshot == null) continue;
                if (saveSnapshot.isRotAnimInProgress()) {
                    saveSnapshot.stopRotAnim(animTime);
                }
                percentageReset = resetTickLength == 0.0 ? 1.0 : Math.min((animTime - saveSnapshot.getLastResetRotationTick()) / resetTickLength, 1.0);
                float initialRotX = initialSnapshot.getRotX();
                float initialRotY = initialSnapshot.getRotY();
                float initialRotZ = initialSnapshot.getRotZ();
                float lastRotX = saveSnapshot.getRotX();
                float lastRotY = saveSnapshot.getRotY();
                float lastRotZ = saveSnapshot.getRotZ();
                if (percentageReset == 0.0) {
                    if (lastRotX != initialRotX && BlendingAnimationProcessor.isSuspectedCompletedRotation(lastRotX)) {
                        lastRotX = initialRotX;
                        percentageReset = 1.0;
                    }
                    if (lastRotY != initialRotY && BlendingAnimationProcessor.isSuspectedCompletedRotation(lastRotY)) {
                        lastRotY = initialRotY;
                        percentageReset = 1.0;
                    }
                    if (lastRotZ != initialRotZ && BlendingAnimationProcessor.isSuspectedCompletedRotation(lastRotZ)) {
                        lastRotZ = initialRotZ;
                        percentageReset = 1.0;
                    }
                }
                bone.setRotX((float)Mth.lerp((double)percentageReset, (double)lastRotX, (double)initialRotX));
                bone.setRotY((float)Mth.lerp((double)percentageReset, (double)lastRotY, (double)initialRotY));
                bone.setRotZ((float)Mth.lerp((double)percentageReset, (double)lastRotZ, (double)initialRotZ));
                if (percentageReset >= 1.0) {
                    saveSnapshot.updateRotation(bone.getRotX(), bone.getRotY(), bone.getRotZ());
                }
            }
            if (!bone.hasPositionChanged()) {
                initialSnapshot = bone.getInitialSnapshot();
                saveSnapshot = boneSnapshots.get(bone.getName());
                if (saveSnapshot.isPosAnimInProgress()) {
                    saveSnapshot.stopPosAnim(animTime);
                }
                percentageReset = resetTickLength == 0.0 ? 1.0 : Math.min((animTime - saveSnapshot.getLastResetPositionTick()) / resetTickLength, 1.0);
                bone.setPosX((float)Mth.lerp((double)percentageReset, (double)saveSnapshot.getOffsetX(), (double)initialSnapshot.getOffsetX()));
                bone.setPosY((float)Mth.lerp((double)percentageReset, (double)saveSnapshot.getOffsetY(), (double)initialSnapshot.getOffsetY()));
                bone.setPosZ((float)Mth.lerp((double)percentageReset, (double)saveSnapshot.getOffsetZ(), (double)initialSnapshot.getOffsetZ()));
                if (percentageReset >= 1.0) {
                    saveSnapshot.updateOffset(bone.getPosX(), bone.getPosY(), bone.getPosZ());
                }
            }
            if (bone.hasScaleChanged()) continue;
            initialSnapshot = bone.getInitialSnapshot();
            saveSnapshot = boneSnapshots.get(bone.getName());
            if (saveSnapshot.isScaleAnimInProgress()) {
                saveSnapshot.stopScaleAnim(animTime);
            }
            percentageReset = resetTickLength == 0.0 ? 1.0 : Math.min((animTime - saveSnapshot.getLastResetScaleTick()) / resetTickLength, 1.0);
            bone.setScaleX((float)Mth.lerp((double)percentageReset, (double)saveSnapshot.getScaleX(), (double)initialSnapshot.getScaleX()));
            bone.setScaleY((float)Mth.lerp((double)percentageReset, (double)saveSnapshot.getScaleY(), (double)initialSnapshot.getScaleY()));
            bone.setScaleZ((float)Mth.lerp((double)percentageReset, (double)saveSnapshot.getScaleZ(), (double)initialSnapshot.getScaleZ()));
            if (!(percentageReset >= 1.0)) continue;
            saveSnapshot.updateScale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
        }
        BlendingAnimationProcessor.resetBoneTransformationMarkers(bones);
    }

    private static boolean isSuspectedCompletedRotation(float lastRotation) {
        float rotations = Mth.abs((float)(lastRotation / ((float)Math.PI * 2)));
        float partialRotation = 1.0f - (rotations - (float)((int)rotations));
        return partialRotation == 1.0f || (double)partialRotation < 0.026 * (double)rotations;
    }

    private static void resetBoneTransformationMarkers(Collection<GeoBone> bones) {
        bones.forEach(GeoBone::resetStateChanges);
    }

    private Map<String, BoneSnapshot> updateBoneSnapshots(Map<String, BoneSnapshot> snapshots) {
        for (GeoBone bone : this.getRegisteredBones()) {
            if (snapshots.containsKey(bone.getName())) continue;
            snapshots.put(bone.getName(), BoneSnapshot.copy((BoneSnapshot)bone.getInitialSnapshot()));
        }
        return snapshots;
    }

    public GeoBone getBone(String boneName) {
        return this.bones.get(boneName);
    }

    public void registerGeoBone(GeoBone bone) {
        bone.saveInitialSnapshot();
        this.bones.put(bone.getName(), bone);
        for (GeoBone child : bone.getChildBones()) {
            this.registerGeoBone(child);
        }
    }

    public void setActiveModel(BakedGeoModel model) {
        this.bones.clear();
        this.hideRenderStates.clear();
        for (GeoBone bone : model.topLevelBones()) {
            this.registerGeoBone(bone);
        }
    }

    public Collection<GeoBone> getRegisteredBones() {
        return this.bones.values();
    }

    public void preAnimationSetup(AnimationState<T> animationState, double animTime) {
        this.model.applyMolangQueries(animationState, animTime);
    }

    private static final class HideRenderState {
        private final Map<String, AnimationPointInfo2> heldAnimationPoints = new HashMap<String, AnimationPointInfo2>();
        private final Map<String, BoneSnapshot> rebaseSourceSnapshots = new HashMap<String, BoneSnapshot>();
        private final Map<String, HideStartPose> hideStartPoses = new HashMap<String, HideStartPose>();
        private AnimationController.State previousControllerState = AnimationController.State.STOPPED;
        private boolean rebasingFromLocomotion;

        private HideRenderState() {
        }

        private void clearRebase() {
            this.rebaseSourceSnapshots.clear();
            this.hideStartPoses.clear();
            this.rebasingFromLocomotion = false;
        }
    }

    private static final class HideStartPose {
        private float rotX;
        private float rotY;
        private float rotZ;
        private float posX;
        private float posY;
        private float posZ;
        private float scaleX;
        private float scaleY;
        private float scaleZ;
        private boolean hasRotation;
        private boolean hasPosition;
        private boolean hasScale;

        private HideStartPose() {
        }
    }
}

