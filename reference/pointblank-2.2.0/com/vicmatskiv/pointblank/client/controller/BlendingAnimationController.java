/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animation.AnimationController
 *  software.bernie.geckolib.animation.AnimationController$AnimationStateHandler
 *  software.bernie.geckolib.animation.AnimationController$State
 *  software.bernie.geckolib.animation.AnimationProcessor$QueuedAnimation
 *  software.bernie.geckolib.animation.AnimationState
 *  software.bernie.geckolib.animation.EasingType
 *  software.bernie.geckolib.animation.PlayState
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.animation.keyframe.AnimationPoint
 *  software.bernie.geckolib.animation.keyframe.BoneAnimation
 *  software.bernie.geckolib.animation.keyframe.BoneAnimationQueue
 *  software.bernie.geckolib.animation.keyframe.Keyframe
 *  software.bernie.geckolib.animation.keyframe.KeyframeLocation
 *  software.bernie.geckolib.animation.keyframe.KeyframeStack
 *  software.bernie.geckolib.animation.keyframe.event.CustomInstructionKeyframeEvent
 *  software.bernie.geckolib.animation.keyframe.event.ParticleKeyframeEvent
 *  software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent
 *  software.bernie.geckolib.animation.keyframe.event.data.CustomInstructionKeyframeData
 *  software.bernie.geckolib.animation.keyframe.event.data.KeyFrameData
 *  software.bernie.geckolib.animation.keyframe.event.data.ParticleKeyframeData
 *  software.bernie.geckolib.animation.keyframe.event.data.SoundKeyframeData
 *  software.bernie.geckolib.animation.state.BoneSnapshot
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.loading.math.MathParser
 *  software.bernie.geckolib.loading.math.MathValue
 *  software.bernie.geckolib.loading.math.value.Constant
 *  software.bernie.geckolib.model.GeoModel
 */
package com.vicmatskiv.pointblank.client.controller;

import com.vicmatskiv.pointblank.client.RealtimeLinearEaser;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.ClientUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.AnimationPoint;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.animation.keyframe.BoneAnimationQueue;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.animation.keyframe.KeyframeLocation;
import software.bernie.geckolib.animation.keyframe.KeyframeStack;
import software.bernie.geckolib.animation.keyframe.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib.animation.keyframe.event.ParticleKeyframeEvent;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.animation.keyframe.event.data.CustomInstructionKeyframeData;
import software.bernie.geckolib.animation.keyframe.event.data.KeyFrameData;
import software.bernie.geckolib.animation.keyframe.event.data.ParticleKeyframeData;
import software.bernie.geckolib.animation.keyframe.event.data.SoundKeyframeData;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.math.value.Constant;
import software.bernie.geckolib.model.GeoModel;

public class BlendingAnimationController<T extends GeoAnimatable>
extends AnimationController<T> {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static final String ANY_ANIMATION = "_any";
    protected final Map<String, BoneSnapshot> topSnapshots = new Object2ObjectOpenHashMap();
    private BiFunction<Player, BlendingAnimationController<T>, Double> speedProvider = (p, c) -> 1.0;
    private boolean justStopped = true;
    private final Set<KeyFrameData> executedKeyFrames = new ObjectOpenHashSet();
    private final Map<String, Map<String, TransitionDescriptor>> transitionDescriptors = new HashMap<String, Map<String, TransitionDescriptor>>();
    protected boolean shouldResetTickOnTransition = true;
    private final TransitionDescriptor defaultTransitionDescriptor;
    protected AnimationProcessor.QueuedAnimation previousAnimation;
    private final RealtimeLinearEaser currentSpeed = new RealtimeLinearEaser(400L);
    private double animationProgress;
    private boolean applySnapshotOnStop = false;

    public BlendingAnimationController(T animatable, String name, int defaultTransitionTickTime, boolean defaultResetTickAfterTransition, AnimationController.AnimationStateHandler<T> animationHandler) {
        super(animatable, name, defaultTransitionTickTime, animationHandler);
        this.defaultTransitionDescriptor = new TransitionDescriptor(defaultTransitionTickTime, defaultResetTickAfterTransition, 1.0);
    }

    public BlendingAnimationController<T> withTransition(String targetAnimation, int tickTime, boolean resetTickAfterTransition) {
        Map fromMap = this.transitionDescriptors.computeIfAbsent(targetAnimation, k -> new HashMap());
        fromMap.put(ANY_ANIMATION, new TransitionDescriptor(tickTime, resetTickAfterTransition, 1.0));
        return this;
    }

    public BlendingAnimationController<T> withTransition(String sourceAnimation, String targetAnimation, int tickTime, boolean resetTickAfterTransition) {
        return this.withTransition(sourceAnimation, targetAnimation, tickTime, resetTickAfterTransition, 1.0);
    }

    public BlendingAnimationController<T> withTransition(String sourceAnimation, String targetAnimation, int tickTime, boolean resetTickAfterTransition, double speed) {
        Map fromMap = this.transitionDescriptors.computeIfAbsent(targetAnimation, k -> new HashMap());
        fromMap.put(sourceAnimation, new TransitionDescriptor(tickTime, resetTickAfterTransition, speed));
        return this;
    }

    public BlendingAnimationController<T> withSpeedProvider(BiFunction<Player, BlendingAnimationController<T>, Double> speedProvider) {
        this.speedProvider = speedProvider;
        return this;
    }

    public BlendingAnimationController<T> withApplySnapshotOnStop(boolean apply) {
        this.applySnapshotOnStop = apply;
        return this;
    }

    public RawAnimation getTriggeredAnimation() {
        return this.triggeredAnimation;
    }

    private TransitionDescriptor getCurrentTransitionDescriptor() {
        if (this.currentAnimation == null) {
            return this.defaultTransitionDescriptor;
        }
        String currentAnimationName = GunItem.stripDualWieldAnimationSuffix(this.currentAnimation.animation().name());
        Map<String, TransitionDescriptor> fromMap = this.transitionDescriptors.get(currentAnimationName);
        if (fromMap == null) {
            return this.defaultTransitionDescriptor;
        }
        if (this.previousAnimation != null) {
            String previousAnimationName = GunItem.stripDualWieldAnimationSuffix(this.previousAnimation.animation().name());
            return fromMap.getOrDefault(previousAnimationName, this.defaultTransitionDescriptor);
        }
        return fromMap.getOrDefault(ANY_ANIMATION, this.defaultTransitionDescriptor);
    }

    protected PlayState handleAnimationState(AnimationState<T> state) {
        if (this.triggeredAnimation != null) {
            if (this.currentRawAnimation != this.triggeredAnimation) {
                this.previousAnimation = this.currentAnimation;
                this.currentAnimation = null;
            }
            this.setAnimation(this.triggeredAnimation);
            if (!(this.hasAnimationFinished() || this.handlingTriggeredAnimations && this.stateHandler.handle(state) != PlayState.CONTINUE)) {
                return PlayState.CONTINUE;
            }
            this.triggeredAnimation = null;
            this.needsAnimationReload = true;
        }
        return this.stateHandler.handle(state);
    }

    public void clearAll() {
        this.topSnapshots.clear();
        this.animationQueue.clear();
        this.currentRawAnimation = null;
        this.currentAnimation = null;
        this.triggeredAnimation = null;
    }

    public boolean tryTriggerAnimation(String animName) {
        Minecraft mc = Minecraft.getInstance();
        long ts = System.currentTimeMillis() % 10000L;
        RawAnimation anim = (RawAnimation)this.triggerableAnimations.get(animName);
        if (anim == null) {
            return false;
        }
        this.triggeredAnimation = anim;
        if (this.animationState == AnimationController.State.STOPPED) {
            this.animationState = AnimationController.State.TRANSITIONING;
            this.shouldResetTick = true;
            this.justStartedTransition = true;
        }
        return true;
    }

    public void setAnimation(RawAnimation rawAnimation) {
        Minecraft mc = Minecraft.getInstance();
        long ts = System.currentTimeMillis() % 10000L;
        if (rawAnimation == null || rawAnimation.getAnimationStages().isEmpty()) {
            this.stop();
            return;
        }
        if (this.needsAnimationReload || !rawAnimation.equals((Object)this.currentRawAnimation)) {
            Queue animations;
            if (this.lastModel != null && (animations = this.lastModel.getAnimationProcessor().buildAnimationQueue(this.animatable, rawAnimation)) != null) {
                this.animationQueue = animations;
                this.currentRawAnimation = rawAnimation;
                this.shouldResetTick = true;
                this.animationState = AnimationController.State.TRANSITIONING;
                this.justStartedTransition = true;
                this.needsAnimationReload = false;
                TransitionDescriptor transitionDescriptor = this.getCurrentTransitionDescriptor();
                this.transitionLength = transitionDescriptor.length;
                this.shouldResetTickOnTransition = transitionDescriptor.resetTickOnTransition;
                return;
            }
            this.stop();
        }
    }

    private void ensureTopSnapshots(Map<String, GeoBone> bones) {
        for (Map.Entry<String, GeoBone> e : bones.entrySet()) {
            String boneName = e.getKey();
            if (this.topSnapshots.containsKey(boneName)) continue;
            this.topSnapshots.put(boneName, BoneSnapshot.copy((BoneSnapshot)e.getValue().getInitialSnapshot()));
        }
    }

    private void applyTopSnapshotsToBones(Map<String, GeoBone> bones) {
        for (Map.Entry<String, BoneSnapshot> entry : this.topSnapshots.entrySet()) {
            GeoBone bone = bones.get(entry.getKey());
            if (bone == null) continue;
            BoneSnapshot snapshot = entry.getValue();
            bone.setRotX(snapshot.getRotX());
            bone.setRotY(snapshot.getRotY());
            bone.setRotZ(snapshot.getRotZ());
            bone.markRotationAsChanged();
            bone.setPosX(snapshot.getOffsetX());
            bone.setPosY(snapshot.getOffsetY());
            bone.setPosZ(snapshot.getOffsetZ());
            bone.markPositionAsChanged();
            bone.setScaleX(snapshot.getScaleX());
            bone.setScaleY(snapshot.getScaleY());
            bone.setScaleZ(snapshot.getScaleZ());
            bone.markScaleAsChanged();
        }
    }

    public void process(GeoModel<T> model, AnimationState<T> state, Map<String, GeoBone> origBones, Map<String, BoneSnapshot> snapshotsIgnored, double seekTime, boolean crashWhenCantFindBone) {
        Minecraft mc = Minecraft.getInstance();
        long ts = System.currentTimeMillis() % 10000L;
        if (this.isJustStarting) {
            this.topSnapshots.clear();
        }
        this.ensureTopSnapshots(origBones);
        this.processInternal(model, state, origBones, this.topSnapshots, seekTime, crashWhenCantFindBone);
        for (BoneAnimationQueue boneAnimationQueue : this.getBoneAnimationQueues().values()) {
            GeoBone bone = boneAnimationQueue.bone();
            BoneSnapshot snapshot = this.topSnapshots.get(bone.getName());
            BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
            AnimationPoint rotXPoint = (AnimationPoint)boneAnimationQueue.rotationXQueue().peek();
            AnimationPoint rotYPoint = (AnimationPoint)boneAnimationQueue.rotationYQueue().peek();
            AnimationPoint rotZPoint = (AnimationPoint)boneAnimationQueue.rotationZQueue().peek();
            AnimationPoint posXPoint = (AnimationPoint)boneAnimationQueue.positionXQueue().peek();
            AnimationPoint posYPoint = (AnimationPoint)boneAnimationQueue.positionYQueue().peek();
            AnimationPoint posZPoint = (AnimationPoint)boneAnimationQueue.positionZQueue().peek();
            AnimationPoint scaleXPoint = (AnimationPoint)boneAnimationQueue.scaleXQueue().peek();
            AnimationPoint scaleYPoint = (AnimationPoint)boneAnimationQueue.scaleYQueue().peek();
            AnimationPoint scaleZPoint = (AnimationPoint)boneAnimationQueue.scaleZQueue().peek();
            EasingType easingType = (EasingType)this.overrideEasingTypeFunction.apply(this.animatable);
            if (rotXPoint != null && rotYPoint != null && rotZPoint != null) {
                float rotX = (float)EasingType.lerpWithOverride((AnimationPoint)rotXPoint, (EasingType)easingType);
                float rotY = (float)EasingType.lerpWithOverride((AnimationPoint)rotYPoint, (EasingType)easingType);
                float rotZ = (float)EasingType.lerpWithOverride((AnimationPoint)rotZPoint, (EasingType)easingType);
                snapshot.updateRotation(rotX + initialSnapshot.getRotX(), rotY + initialSnapshot.getRotY(), rotZ + initialSnapshot.getRotZ());
                snapshot.startRotAnim();
            }
            if (posXPoint != null && posYPoint != null && posZPoint != null) {
                float posX = (float)EasingType.lerpWithOverride((AnimationPoint)posXPoint, (EasingType)easingType);
                float posY = (float)EasingType.lerpWithOverride((AnimationPoint)posYPoint, (EasingType)easingType);
                float posZ = (float)EasingType.lerpWithOverride((AnimationPoint)posZPoint, (EasingType)easingType);
                snapshot.updateOffset(posX, posY, posZ);
                snapshot.startPosAnim();
            }
            if (scaleXPoint == null || scaleYPoint == null || scaleZPoint == null) continue;
            snapshot.updateScale((float)EasingType.lerpWithOverride((AnimationPoint)scaleXPoint, (EasingType)easingType), (float)EasingType.lerpWithOverride((AnimationPoint)scaleYPoint, (EasingType)easingType), (float)EasingType.lerpWithOverride((AnimationPoint)scaleZPoint, (EasingType)easingType));
            snapshot.startScaleAnim();
        }
    }

    public double getAnimationSpeed() {
        double targetSpeed = this.speedProvider.apply(ClientUtil.getClientPlayer(), this);
        return this.currentSpeed.update((float)targetSpeed);
    }

    protected double adjustTick(double tick) {
        Minecraft mc = Minecraft.getInstance();
        long ts = System.currentTimeMillis() % 10000L;
        if (!this.shouldResetTick) {
            return this.getAnimationSpeed() * Math.max(tick - this.tickOffset, 0.0);
        }
        if (this.getAnimationState() != AnimationController.State.STOPPED) {
            this.tickOffset = tick;
        }
        this.shouldResetTick = false;
        return 0.0;
    }

    public void processInternal(GeoModel<T> model, AnimationState<T> state, Map<String, GeoBone> origBones, Map<String, BoneSnapshot> snapshots, double seekTime, boolean crashWhenCantFindBone) {
        PlayState playState;
        double adjustedTick = this.adjustTick(seekTime);
        this.lastModel = model;
        if (this.animationState == AnimationController.State.TRANSITIONING && adjustedTick >= this.transitionLength) {
            this.shouldResetTick = this.shouldResetTickOnTransition;
            this.animationState = AnimationController.State.RUNNING;
            adjustedTick = this.adjustTick(seekTime);
        }
        if ((playState = this.handleAnimationState(state)) == PlayState.STOP || this.currentAnimation == null && this.animationQueue.isEmpty()) {
            this.animationState = AnimationController.State.STOPPED;
            this.justStopped = true;
            if (this.applySnapshotOnStop) {
                this.applyTopSnapshotsToBones(origBones);
            }
            return;
        }
        this.createInitialQueues(origBones.values());
        if (this.justStartedTransition && (this.shouldResetTick || this.justStopped)) {
            this.justStopped = false;
            adjustedTick = this.adjustTick(seekTime);
            if (this.currentAnimation == null) {
                this.animationState = AnimationController.State.TRANSITIONING;
            }
        } else if (this.currentAnimation == null) {
            this.shouldResetTick = true;
            this.animationState = AnimationController.State.TRANSITIONING;
            this.justStartedTransition = true;
            this.needsAnimationReload = false;
            adjustedTick = this.adjustTick(seekTime);
        } else if (this.animationState != AnimationController.State.TRANSITIONING) {
            this.animationState = AnimationController.State.RUNNING;
        }
        if (this.getAnimationState() == AnimationController.State.RUNNING) {
            this.processCurrentAnimation(adjustedTick, seekTime, crashWhenCantFindBone, snapshots);
        } else if (this.animationState == AnimationController.State.TRANSITIONING) {
            if (this.lastPollTime != seekTime && (adjustedTick == 0.0 || this.isJustStarting)) {
                this.justStartedTransition = false;
                this.lastPollTime = seekTime;
                this.currentAnimation = (AnimationProcessor.QueuedAnimation)this.animationQueue.poll();
                this.resetEventKeyFrames();
                if (this.currentAnimation == null) {
                    return;
                }
                TransitionDescriptor transitionDescriptor = this.getCurrentTransitionDescriptor();
                this.transitionLength = transitionDescriptor.length;
                this.shouldResetTickOnTransition = transitionDescriptor.resetTickOnTransition;
                this.saveSnapshotsForAnimation(this.currentAnimation, snapshots);
            }
            if (this.currentAnimation != null) {
                MathParser.setVariable((String)"query.anim_time", () -> 0.0);
                for (BoneAnimation boneAnimation : this.currentAnimation.animation().boneAnimations()) {
                    double startTick;
                    BoneAnimationQueue boneAnimationQueue = (BoneAnimationQueue)this.boneAnimationQueues.get(boneAnimation.boneName());
                    BoneSnapshot boneSnapshot = (BoneSnapshot)this.boneSnapshots.get(boneAnimation.boneName());
                    if (boneSnapshot == null) continue;
                    KeyframeStack rotationKeyFrames = boneAnimation.rotationKeyFrames();
                    KeyframeStack positionKeyFrames = boneAnimation.positionKeyFrames();
                    KeyframeStack scaleKeyFrames = boneAnimation.scaleKeyFrames();
                    double d = startTick = this.shouldResetTickOnTransition ? 0.0 : this.transitionLength;
                    if (!rotationKeyFrames.xKeyframes().isEmpty()) {
                        AnimationPoint xPointNext = BlendingAnimationController.getAnimationPointAtTick(rotationKeyFrames.xKeyframes(), startTick, true, Direction.Axis.X);
                        AnimationPoint yPointNext = BlendingAnimationController.getAnimationPointAtTick(rotationKeyFrames.yKeyframes(), startTick, true, Direction.Axis.Y);
                        AnimationPoint zPointNext = BlendingAnimationController.getAnimationPointAtTick(rotationKeyFrames.zKeyframes(), startTick, true, Direction.Axis.Z);
                        this.addNextRotation(boneAnimationQueue, null, adjustedTick, this.transitionLength, boneSnapshot, boneSnapshot.getBone().getInitialSnapshot(), xPointNext, yPointNext, zPointNext);
                    }
                    if (!positionKeyFrames.xKeyframes().isEmpty()) {
                        this.addNextPosition(boneAnimationQueue, null, adjustedTick, this.transitionLength, boneSnapshot, BlendingAnimationController.getAnimationPointAtTick(positionKeyFrames.xKeyframes(), startTick, false, Direction.Axis.X), BlendingAnimationController.getAnimationPointAtTick(positionKeyFrames.yKeyframes(), startTick, false, Direction.Axis.Y), BlendingAnimationController.getAnimationPointAtTick(positionKeyFrames.zKeyframes(), startTick, false, Direction.Axis.Z));
                    }
                    if (scaleKeyFrames.xKeyframes().isEmpty()) continue;
                    this.addNextScale(boneAnimationQueue, null, adjustedTick, this.transitionLength, boneSnapshot, BlendingAnimationController.getAnimationPointAtTick(scaleKeyFrames.xKeyframes(), startTick, false, Direction.Axis.X), BlendingAnimationController.getAnimationPointAtTick(scaleKeyFrames.yKeyframes(), startTick, false, Direction.Axis.Y), BlendingAnimationController.getAnimationPointAtTick(scaleKeyFrames.zKeyframes(), startTick, false, Direction.Axis.Z));
                }
            }
        }
        this.animationProgress = this.currentAnimation != null ? Mth.clamp((double)(adjustedTick / this.currentAnimation.animation().length()), (double)0.0, (double)1.0) : 0.0;
    }

    public double getAnimationProgress() {
        return this.animationProgress;
    }

    public void addNextRotation(BoneAnimationQueue boneAnimationQueue, Keyframe<?> keyFrame, double lerpedTick, double transitionLength, BoneSnapshot startSnapshot, BoneSnapshot initialSnapshot, AnimationPoint nextXPoint, AnimationPoint nextYPoint, AnimationPoint nextZPoint) {
        EasingType easingType = (EasingType)this.overrideEasingTypeFunction.apply(this.animatable);
        float nextX = (float)EasingType.lerpWithOverride((AnimationPoint)nextXPoint, (EasingType)easingType);
        float nextY = (float)EasingType.lerpWithOverride((AnimationPoint)nextYPoint, (EasingType)easingType);
        float nextZ = (float)EasingType.lerpWithOverride((AnimationPoint)nextZPoint, (EasingType)easingType);
        boneAnimationQueue.addRotationXPoint(keyFrame, lerpedTick, transitionLength, (double)(startSnapshot.getRotX() - initialSnapshot.getRotX()), (double)nextX);
        boneAnimationQueue.addRotationYPoint(keyFrame, lerpedTick, transitionLength, (double)(startSnapshot.getRotY() - initialSnapshot.getRotY()), (double)nextY);
        boneAnimationQueue.addRotationZPoint(keyFrame, lerpedTick, transitionLength, (double)(startSnapshot.getRotZ() - initialSnapshot.getRotZ()), (double)nextZ);
    }

    public void addNextPosition(BoneAnimationQueue boneAnimationQueue, Keyframe<?> keyFrame, double lerpedTick, double transitionLength, BoneSnapshot startSnapshot, AnimationPoint nextXPoint, AnimationPoint nextYPoint, AnimationPoint nextZPoint) {
        EasingType easingType = (EasingType)this.overrideEasingTypeFunction.apply(this.animatable);
        float nextX = (float)EasingType.lerpWithOverride((AnimationPoint)nextXPoint, (EasingType)easingType);
        float nextY = (float)EasingType.lerpWithOverride((AnimationPoint)nextYPoint, (EasingType)easingType);
        float nextZ = (float)EasingType.lerpWithOverride((AnimationPoint)nextZPoint, (EasingType)easingType);
        boneAnimationQueue.addPosXPoint(keyFrame, lerpedTick, transitionLength, (double)startSnapshot.getOffsetX(), (double)nextX);
        boneAnimationQueue.addPosYPoint(keyFrame, lerpedTick, transitionLength, (double)startSnapshot.getOffsetY(), (double)nextY);
        boneAnimationQueue.addPosZPoint(keyFrame, lerpedTick, transitionLength, (double)startSnapshot.getOffsetZ(), (double)nextZ);
    }

    public void addNextScale(BoneAnimationQueue boneAnimationQueue, Keyframe<?> keyFrame, double lerpedTick, double transitionLength, BoneSnapshot startSnapshot, AnimationPoint nextXPoint, AnimationPoint nextYPoint, AnimationPoint nextZPoint) {
        EasingType easingType = (EasingType)this.overrideEasingTypeFunction.apply(this.animatable);
        float nextX = (float)EasingType.lerpWithOverride((AnimationPoint)nextXPoint, (EasingType)easingType);
        float nextY = (float)EasingType.lerpWithOverride((AnimationPoint)nextYPoint, (EasingType)easingType);
        float nextZ = (float)EasingType.lerpWithOverride((AnimationPoint)nextZPoint, (EasingType)easingType);
        boneAnimationQueue.addScaleXPoint(keyFrame, lerpedTick, transitionLength, (double)startSnapshot.getScaleX(), (double)nextX);
        boneAnimationQueue.addScaleYPoint(keyFrame, lerpedTick, transitionLength, (double)startSnapshot.getScaleY(), (double)nextY);
        boneAnimationQueue.addScaleZPoint(keyFrame, lerpedTick, transitionLength, (double)startSnapshot.getScaleZ(), (double)nextZ);
    }

    private void saveSnapshotsForAnimation(AnimationProcessor.QueuedAnimation animation, Map<String, BoneSnapshot> snapshots) {
        block0: for (BoneSnapshot snapshot : snapshots.values()) {
            if (animation.animation().boneAnimations() == null) continue;
            for (BoneAnimation boneAnimation : animation.animation().boneAnimations()) {
                if (!boneAnimation.boneName().equals(snapshot.getBone().getName())) continue;
                this.boneSnapshots.put(boneAnimation.boneName(), BoneSnapshot.copy((BoneSnapshot)snapshot));
                continue block0;
            }
        }
    }

    private void createInitialQueues(Collection<GeoBone> modelRendererList) {
        this.boneAnimationQueues.clear();
        for (GeoBone bone : modelRendererList) {
            this.boneAnimationQueues.put(bone.getName(), new BoneAnimationQueue(bone));
        }
    }

    private void processCurrentAnimation(double adjustedTick, double seekTime, boolean crashWhenCantFindBone, Map<String, BoneSnapshot> snapshots) {
        Minecraft mc = Minecraft.getInstance();
        long ts = System.currentTimeMillis() % 10000L;
        if (adjustedTick >= this.currentAnimation.animation().length()) {
            if (this.currentAnimation.loopType().shouldPlayAgain(this.animatable, (AnimationController)this, this.currentAnimation.animation())) {
                if (this.animationState != AnimationController.State.PAUSED) {
                    this.shouldResetTick = true;
                    adjustedTick = this.adjustTick(seekTime);
                    this.resetEventKeyFrames();
                }
            } else {
                AnimationProcessor.QueuedAnimation nextAnimation = (AnimationProcessor.QueuedAnimation)this.animationQueue.peek();
                this.resetEventKeyFrames();
                if (nextAnimation == null) {
                    this.animationState = AnimationController.State.STOPPED;
                    adjustedTick = this.currentAnimation.animation().length();
                } else {
                    this.animationState = AnimationController.State.TRANSITIONING;
                    this.shouldResetTick = true;
                    adjustedTick = this.adjustTick(seekTime);
                    if (this.currentAnimation != null) {
                        this.previousAnimation = this.currentAnimation;
                    }
                    this.currentAnimation = (AnimationProcessor.QueuedAnimation)this.animationQueue.poll();
                    TransitionDescriptor transitionDescriptor = this.getCurrentTransitionDescriptor();
                    this.transitionLength = transitionDescriptor.length;
                    this.shouldResetTickOnTransition = transitionDescriptor.resetTickOnTransition;
                    this.saveSnapshotsForAnimation(this.currentAnimation, snapshots);
                }
            }
        }
        double finalAdjustedTick = adjustedTick;
        MathParser.setVariable((String)"query.anim_time", () -> finalAdjustedTick / 20.0);
        for (BoneAnimation boneAnimation : this.currentAnimation.animation().boneAnimations()) {
            BoneAnimationQueue boneAnimationQueue = (BoneAnimationQueue)this.boneAnimationQueues.get(boneAnimation.boneName());
            if (boneAnimationQueue == null) {
                if (!crashWhenCantFindBone) continue;
                throw new RuntimeException("Could not find bone: " + boneAnimation.boneName());
            }
            KeyframeStack rotationKeyFrames = boneAnimation.rotationKeyFrames();
            KeyframeStack positionKeyFrames = boneAnimation.positionKeyFrames();
            KeyframeStack scaleKeyFrames = boneAnimation.scaleKeyFrames();
            if (!rotationKeyFrames.xKeyframes().isEmpty()) {
                AnimationPoint rotXPoint = BlendingAnimationController.getAnimationPointAtTick(rotationKeyFrames.xKeyframes(), adjustedTick, true, Direction.Axis.X);
                AnimationPoint rotYPoint = BlendingAnimationController.getAnimationPointAtTick(rotationKeyFrames.yKeyframes(), adjustedTick, true, Direction.Axis.Y);
                AnimationPoint rotZPoint = BlendingAnimationController.getAnimationPointAtTick(rotationKeyFrames.zKeyframes(), adjustedTick, true, Direction.Axis.Z);
                boneAnimationQueue.addRotations(rotXPoint, rotYPoint, rotZPoint);
            }
            if (!positionKeyFrames.xKeyframes().isEmpty()) {
                AnimationPoint xp = BlendingAnimationController.getAnimationPointAtTick(positionKeyFrames.xKeyframes(), adjustedTick, false, Direction.Axis.X);
                AnimationPoint yp = BlendingAnimationController.getAnimationPointAtTick(positionKeyFrames.yKeyframes(), adjustedTick, false, Direction.Axis.Y);
                AnimationPoint zp = BlendingAnimationController.getAnimationPointAtTick(positionKeyFrames.zKeyframes(), adjustedTick, false, Direction.Axis.Z);
                boneAnimationQueue.addPositions(xp, yp, zp);
            }
            if (scaleKeyFrames.xKeyframes().isEmpty()) continue;
            boneAnimationQueue.addScales(BlendingAnimationController.getAnimationPointAtTick(scaleKeyFrames.xKeyframes(), adjustedTick, false, Direction.Axis.X), BlendingAnimationController.getAnimationPointAtTick(scaleKeyFrames.yKeyframes(), adjustedTick, false, Direction.Axis.Y), BlendingAnimationController.getAnimationPointAtTick(scaleKeyFrames.zKeyframes(), adjustedTick, false, Direction.Axis.Z));
        }
        adjustedTick += this.transitionLength;
        for (BoneAnimation boneAnimation : this.currentAnimation.animation().keyFrames().sounds()) {
            if (!(adjustedTick >= boneAnimation.getStartTick()) || !this.executedKeyFrames.add((KeyFrameData)boneAnimation)) continue;
            if (this.soundKeyframeHandler == null) break;
            this.soundKeyframeHandler.handle(new SoundKeyframeEvent(this.animatable, adjustedTick, (AnimationController)this, (SoundKeyframeData)boneAnimation));
        }
        for (BoneAnimation boneAnimation : this.currentAnimation.animation().keyFrames().particles()) {
            if (!(adjustedTick >= boneAnimation.getStartTick()) || !this.executedKeyFrames.add((KeyFrameData)boneAnimation)) continue;
            if (this.particleKeyframeHandler == null) break;
            this.particleKeyframeHandler.handle(new ParticleKeyframeEvent(this.animatable, adjustedTick, (AnimationController)this, (ParticleKeyframeData)boneAnimation));
        }
        for (BoneAnimation boneAnimation : this.currentAnimation.animation().keyFrames().customInstructions()) {
            if (!(adjustedTick >= boneAnimation.getStartTick()) || !this.executedKeyFrames.add((KeyFrameData)boneAnimation)) continue;
            if (this.customKeyframeHandler == null) break;
            this.customKeyframeHandler.handle(new CustomInstructionKeyframeEvent(this.animatable, adjustedTick, (AnimationController)this, (CustomInstructionKeyframeData)boneAnimation));
        }
        if (this.transitionLength == 0.0 && this.shouldResetTick && this.animationState == AnimationController.State.TRANSITIONING) {
            this.currentAnimation = (AnimationProcessor.QueuedAnimation)this.animationQueue.poll();
        }
    }

    private static AnimationPoint getAnimationPointAtTick(List<Keyframe<MathValue>> frames, double tick, boolean isRotation, Direction.Axis axis) {
        KeyframeLocation<Keyframe<MathValue>> location = BlendingAnimationController.getCurrentKeyFrameLocation(frames, tick);
        Keyframe currentFrame = location.keyframe();
        double startValue = currentFrame.startValue().get();
        double endValue = currentFrame.endValue().get();
        if (isRotation) {
            if (!(currentFrame.startValue() instanceof Constant)) {
                startValue = Math.toRadians(startValue);
                if (axis == Direction.Axis.X || axis == Direction.Axis.Y) {
                    startValue *= -1.0;
                }
            }
            if (!(currentFrame.endValue() instanceof Constant)) {
                endValue = Math.toRadians(endValue);
                if (axis == Direction.Axis.X || axis == Direction.Axis.Y) {
                    endValue *= -1.0;
                }
            }
        }
        return new AnimationPoint(currentFrame, location.startTick(), currentFrame.length(), startValue, endValue);
    }

    private static KeyframeLocation<Keyframe<MathValue>> getCurrentKeyFrameLocation(List<Keyframe<MathValue>> frames, double ageInTicks) {
        double totalFrameTime = 0.0;
        for (Keyframe<MathValue> frame : frames) {
            if (!((totalFrameTime += frame.length()) > ageInTicks)) continue;
            return new KeyframeLocation(frame, ageInTicks - (totalFrameTime - frame.length()));
        }
        return new KeyframeLocation(frames.get(frames.size() - 1), ageInTicks);
    }

    private void resetEventKeyFrames() {
        this.executedKeyFrames.clear();
    }

    private record TransitionDescriptor(int length, boolean resetTickOnTransition, double speed) {
    }
}

