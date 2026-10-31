/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Objects
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.math.Axis
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.ItemTransform
 *  net.minecraft.client.renderer.block.model.ItemTransforms
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.cache.GeckoLibCache
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.cache.object.GeoCube
 *  software.bernie.geckolib.cache.object.GeoQuad
 *  software.bernie.geckolib.cache.object.GeoVertex
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoItemRenderer
 *  software.bernie.geckolib.renderer.GeoRenderer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 *  software.bernie.geckolib.util.RenderUtil
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.attachment.AttachmentModelInfo;
import com.vicmatskiv.pointblank.client.BiDirectionalInterpolator;
import com.vicmatskiv.pointblank.client.ClientEventHandler;
import com.vicmatskiv.pointblank.client.ClientSystem;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.GunStatePoseProvider;
import com.vicmatskiv.pointblank.client.RealtimeLinearEaser;
import com.vicmatskiv.pointblank.client.controller.GlowAnimationController;
import com.vicmatskiv.pointblank.client.controller.RotationAnimationController;
import com.vicmatskiv.pointblank.client.effect.EffectRenderContext;
import com.vicmatskiv.pointblank.client.effect.FirstPersonShellEffect;
import com.vicmatskiv.pointblank.client.effect.MuzzleFlashEffect;
import com.vicmatskiv.pointblank.client.gui.AttachmentManagerScreen;
import com.vicmatskiv.pointblank.client.model.GunGeoModel;
import com.vicmatskiv.pointblank.client.render.FirstPersonHandAnchorCapture;
import com.vicmatskiv.pointblank.client.render.Flushable;
import com.vicmatskiv.pointblank.client.render.HierarchicalRenderContext;
import com.vicmatskiv.pointblank.client.render.RenderApprover;
import com.vicmatskiv.pointblank.client.render.RenderPass;
import com.vicmatskiv.pointblank.client.render.RenderPassGeoRenderer;
import com.vicmatskiv.pointblank.client.render.RenderPassProvider;
import com.vicmatskiv.pointblank.client.render.RenderTypeKey;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.client.render.RenderUtil;
import com.vicmatskiv.pointblank.client.render.ScopeAlignmentHelper;
import com.vicmatskiv.pointblank.client.render.layer.AmmoCounterItemLayer;
import com.vicmatskiv.pointblank.client.render.layer.AttachmentLayer;
import com.vicmatskiv.pointblank.client.render.layer.GlowingItemLayer;
import com.vicmatskiv.pointblank.client.render.layer.GunHandsItemLayer;
import com.vicmatskiv.pointblank.client.render.layer.MuzzleFlashItemLayer;
import com.vicmatskiv.pointblank.client.render.layer.PipItemLayer;
import com.vicmatskiv.pointblank.client.render.layer.ReticleItemLayer;
import com.vicmatskiv.pointblank.client.render.layer.SeeThroughItemLayer;
import com.vicmatskiv.pointblank.client.uv.SpriteUVProvider;
import com.vicmatskiv.pointblank.compat.iris.IrisCompat;
import com.vicmatskiv.pointblank.feature.ActiveMuzzleFeature;
import com.vicmatskiv.pointblank.feature.AimingFeature;
import com.vicmatskiv.pointblank.feature.AlternatingMuzzleFeature;
import com.vicmatskiv.pointblank.feature.AmmoCounterFeature;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.Features;
import com.vicmatskiv.pointblank.feature.PartVisibilityFeature;
import com.vicmatskiv.pointblank.feature.PipFeature;
import com.vicmatskiv.pointblank.feature.SkinFeature;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.registry.EffectRegistry;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GunItemRenderer
extends GeoItemRenderer<GunItem>
implements RenderPassGeoRenderer<GunItem>,
RenderApprover {
    public static final String BONE_SCOPE = "scope";
    public static final String BONE_RETICLE = "reticle";
    public static final String BONE_SCOPE_PIP = "scopepip";
    public static final String BONE_RIGHTARM = "rightarm";
    public static final String BONE_LEFTARM = "leftarm";
    public static final String BONE_MUZZLE = "muzzle";
    public static final String BONE_MUZZLE2 = "muzzle2";
    public static final String BONE_MUZZLE3 = "muzzle3";
    public static final String BONE_MUZZLEFLASH = "muzzleflash";
    public static final String BONE_MUZZLEFLASH2 = "muzzleflash2";
    public static final String BONE_MUZZLEFLASH3 = "muzzleflash3";
    public static final String BONE_MUZZLEFLASH_ALT = "muzzleflashalt";
    public static final String BONE_CAMERA = "_camera_";
    public static final String BONE_AMMO_SCREEN = "_ammo_";
    public static final float DEFAULT_MAX_ANGULAR_RETICLE_OFFSET = Mth.cos((float)0.08726646f);
    public static final float DEFAULT_MAX_ANGULAR_RETICLE_OFFSET_NOT_AIMED = Mth.cos((float)((float)Math.PI / 90));
    private static final float ADS_Z_DEPTH_TARGET = -0.7f;
    private static final float PIP_FAR_MASK_SCALE = 5.0f;
    private static final float PIP_FAR_MASK_Z_OFFSET = -2.0f;
    private static final Vector3f SCOPE_AIM_POINT_OFFSET = new Vector3f(0.00625f, 0.0f, 0.0f);
    private static final boolean TRACE_FP_TRANSFORM = Boolean.parseBoolean(System.getProperty("pointblank.debug.fptransform", "false"));
    private static final String TRACE_FP_GUN_FILTER = System.getProperty("pointblank.debug.fptransform.filter", "").toLowerCase(Locale.ROOT);
    private static final int TRACE_FP_RATE = Integer.getInteger("pointblank.debug.fptransform.rate", 20);
    private static final float TRACE_FP_MIN_AIMING = Float.parseFloat(System.getProperty("pointblank.debug.fptransform.minAiming", "0.99"));
    private static final boolean DEBUG_SHELL = Boolean.getBoolean("pointblank.debug.shell");
    private static final Set<ResourceLocation> MISSING_SHELL_MODEL_WARNINGS = ConcurrentHashMap.newKeySet();
    private static int traceFpFrameCounter = 0;
    private static long traceFpSampleCounter = 0L;
    private static final String[] PRIMARY_MUZZLE_PIVOT_BONES = new String[]{"muzzle", "muzzle2", "muzzle3"};
    private static final String[] FALLBACK_MUZZLE_PIVOT_BONES = new String[]{"muzzleflash", "muzzleflash2", "muzzleflash3"};
    private static final float FIRST_PERSON_BODY_INERTIA_PITCH_DIRECTION = -1.0f;
    private static final float FIRST_PERSON_BODY_INERTIA_PITCH_SCALE = 1.0f;
    private static final float FIRST_PERSON_BODY_INERTIA_YAW_DIRECTION = -1.0f;
    private static final float FIRST_PERSON_BODY_INERTIA_YAW_SCALE = 1.5f;
    private static final float FIRST_PERSON_BODY_INERTIA_AIMING_MULTIPLIER = 0.0f;
    private static final float FIRST_PERSON_BODY_INERTIA_MAX_PITCH = 0.010471975f;
    private static final float FIRST_PERSON_BODY_INERTIA_MAX_YAW = 0.02268928f;
    private static final float FIRST_PERSON_BODY_INERTIA_MAX_PIVOT_DEPTH = 5.0f;
    private static final float FIRST_PERSON_BODY_INERTIA_PITCH_EPSILON = 1.0E-4f;
    private static final float FIRST_PERSON_BODY_INERTIA_YAW_EPSILON = 1.0E-4f;
    private static final Vector3f FIRST_PERSON_BODY_INERTIA_FALLBACK_PIVOT = new Vector3f(0.0f, 0.0f, -2.0f);
    private static final float DUAL_WIELD_SPREAD_X = 0.2f;
    private static final float DUAL_WIELD_MIN_CONVERGENCE_DEPTH = 1.25f;
    private static final float DUAL_WIELD_MAX_INWARD_YAW_DEGREES = 4.0f;
    private static final float DUAL_WIELD_MANUAL_YAW_BIAS_DEGREES = -5.0f;
    private static final float DUAL_WIELD_SPREAD_EPSILON = 1.0E-4f;
    private static volatile float cameraBoneRotX;
    private static volatile float cameraBoneRotY;
    private static volatile float cameraBoneRotZ;
    private static volatile float cameraBoneRotRightX;
    private static volatile float cameraBoneRotRightY;
    private static volatile float cameraBoneRotRightZ;
    private static volatile float cameraBoneRotLeftX;
    private static volatile float cameraBoneRotLeftY;
    private static volatile float cameraBoneRotLeftZ;
    private GunClientState gunClientState;
    private boolean hasScopeOverlay;
    private ItemTransforms transforms;
    private MultiBufferSource currentBufferSource;
    private float currentPartialTick;
    private boolean traceCurrentFirstPersonFrame;
    private long traceCurrentFirstPersonSample;
    private Matrix4f firstPersonBasePose;
    private ItemStack firstPersonItemStack;
    private ItemTransform firstPersonAppliedItemTransform;
    private ItemTransform firstPersonTargetItemTransform;
    private Matrix4f firstPersonHandAnchorPose;
    private float firstPersonAimingProgress;
    private float firstPersonModelScale = 1.0f;
    private ItemDisplayContext firstPersonDisplayContext;
    private final RealtimeLinearEaser dualWieldSpreadEaser = new RealtimeLinearEaser(180L);
    private final ScopeSwitchAimingInterpolator scopeSwitchAimingInterpolator = new ScopeSwitchAimingInterpolator(200L);
    private ResourceLocation leftHandModelResource = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"geo/item/left_arm.geo.json");
    private ResourceLocation rightHandModelResource = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"geo/item/right_arm.geo.json");
    private ResourceLocation reticleModelResource = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"geo/item/reticle.geo.json");
    private boolean useCustomGlowingTexture;
    private Set<Direction> glowDirections;
    private Supplier<SpriteUVProvider> glowingSpriteUVProviderSupplier;
    private float glowingProgress;

    public static float getCameraBoneRotX() {
        return cameraBoneRotX;
    }

    public static float getCameraBoneRotY() {
        return cameraBoneRotY;
    }

    public static float getCameraBoneRotZ() {
        return cameraBoneRotZ;
    }

    public static float getCameraBoneRotX(@Nullable ItemDisplayContext context) {
        if (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            return cameraBoneRotLeftX;
        }
        if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            return cameraBoneRotRightX;
        }
        return cameraBoneRotX;
    }

    public static float getCameraBoneRotY(@Nullable ItemDisplayContext context) {
        if (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            return cameraBoneRotLeftY;
        }
        if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            return cameraBoneRotRightY;
        }
        return cameraBoneRotY;
    }

    public static float getCameraBoneRotZ(@Nullable ItemDisplayContext context) {
        if (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            return cameraBoneRotLeftZ;
        }
        if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            return cameraBoneRotRightZ;
        }
        return cameraBoneRotZ;
    }

    public GunItemRenderer(ResourceLocation modelResource, List<ResourceLocation> fallbackAnimations, List<GlowAnimationController.Builder> glowEffectBuilders) {
        super((GeoModel)new GunGeoModel(modelResource, fallbackAnimations));
        this.addRenderLayer(new AttachmentLayer<GunItem>(this));
        for (GlowAnimationController.Builder glowEffectBuilder : glowEffectBuilders) {
            ResourceLocation glowTexture = glowEffectBuilder.getTexture();
            if (glowTexture == null) {
                glowTexture = this.getGeoModel().getTextureResource((GeoAnimatable)((GunItem)this.animatable));
            }
            this.addRenderLayer(new GlowingItemLayer(this, glowEffectBuilder.getEffectId(), glowTexture));
        }
        this.addRenderLayer(new GunHandsItemLayer<GunItem>(this));
        this.addRenderLayer(new PipItemLayer(this));
        this.addRenderLayer(new AmmoCounterItemLayer(this));
        this.addRenderLayer(new SeeThroughItemLayer(this));
        this.addRenderLayer(new ReticleItemLayer(this));
        this.addRenderLayer(new MuzzleFlashItemLayer(this));
    }

    private boolean shouldTraceFirstPersonTransform(GunItem gunItem, float aimingProgress) {
        if (!TRACE_FP_TRANSFORM || gunItem == null || aimingProgress < TRACE_FP_MIN_AIMING) {
            return false;
        }
        if (!TRACE_FP_GUN_FILTER.isEmpty() && !gunItem.getName().toLowerCase(Locale.ROOT).contains(TRACE_FP_GUN_FILTER)) {
            return false;
        }
        int frame = ++traceFpFrameCounter;
        return TRACE_FP_RATE <= 1 || frame % TRACE_FP_RATE == 0;
    }

    private static String fmt(float value) {
        return String.format(Locale.ROOT, "%.4f", Float.valueOf(value));
    }

    private void logFirstPersonTransformPose(String phase, GunItem gunItem, float aimingProgress, float modelScale, ItemTransform fprt, PoseStack poseStack) {
        this.logFirstPersonTransformMatrix(phase, gunItem, aimingProgress, modelScale, fprt, new Matrix4f((Matrix4fc)poseStack.last().pose()));
    }

    private void logFirstPersonTransformMatrix(String phase, GunItem gunItem, float aimingProgress, float modelScale, ItemTransform fprt, Matrix4f matrix) {
        Vector3f translation = matrix.getTranslation(new Vector3f());
        Quaternionf rotationQuat = matrix.getNormalizedRotation(new Quaternionf());
        Vector3f euler = rotationQuat.getEulerAnglesXYZ(new Vector3f());
        float sx = (float)Math.sqrt(matrix.m00() * matrix.m00() + matrix.m01() * matrix.m01() + matrix.m02() * matrix.m02());
        float sy = (float)Math.sqrt(matrix.m10() * matrix.m10() + matrix.m11() * matrix.m11() + matrix.m12() * matrix.m12());
        float sz = (float)Math.sqrt(matrix.m20() * matrix.m20() + matrix.m21() * matrix.m21() + matrix.m22() * matrix.m22());
        Platform.LOGGER.info("[fp-trace-1.21] sample=" + this.traceCurrentFirstPersonSample + " phase=" + phase + " gun=" + gunItem.getName() + " aiming=" + GunItemRenderer.fmt(aimingProgress) + " modelScale=" + GunItemRenderer.fmt(modelScale) + " itemTransform.t=(" + GunItemRenderer.fmt(fprt.translation.x) + "," + GunItemRenderer.fmt(fprt.translation.y) + "," + GunItemRenderer.fmt(fprt.translation.z) + ") itemTransform.r=(" + GunItemRenderer.fmt(fprt.rotation.x) + "," + GunItemRenderer.fmt(fprt.rotation.y) + "," + GunItemRenderer.fmt(fprt.rotation.z) + ") itemTransform.s=(" + GunItemRenderer.fmt(fprt.scale.x) + "," + GunItemRenderer.fmt(fprt.scale.y) + "," + GunItemRenderer.fmt(fprt.scale.z) + ") pose.t=(" + GunItemRenderer.fmt(translation.x) + "," + GunItemRenderer.fmt(translation.y) + "," + GunItemRenderer.fmt(translation.z) + ") pose.eulerDeg=(" + GunItemRenderer.fmt((float)Math.toDegrees(euler.x)) + "," + GunItemRenderer.fmt((float)Math.toDegrees(euler.y)) + "," + GunItemRenderer.fmt((float)Math.toDegrees(euler.z)) + ") pose.scale=(" + GunItemRenderer.fmt(sx) + "," + GunItemRenderer.fmt(sy) + "," + GunItemRenderer.fmt(sz) + ")");
    }

    private void logFirstPersonScopeSelection(ItemStack itemStack, GunItem gunItem, float modelScale, float aimingProgress) {
        NavigableMap<String, Pair<ItemStack, Matrix4f>> poseMatrices = AttachmentModelInfo.findInverseBoneMatrices(itemStack, BONE_SCOPE, 1.0f);
        Features.EnabledFeature aimingFeature = Features.getFirstEnabledFeature(itemStack, AimingFeature.class);
        String requestedPath = aimingFeature != null ? aimingFeature.ownerPath() : null;
        Pair<ItemStack, Matrix4f> selected = requestedPath != null ? (Pair<ItemStack, Matrix4f>)poseMatrices.get(requestedPath) : null;
        String selectedPath = requestedPath;
        if (selected == null && !poseMatrices.isEmpty()) {
            selectedPath = (String)poseMatrices.firstKey();
            selected = poseMatrices.firstEntry().getValue();
        }
        Platform.LOGGER.info("[fp-scope-trace-1.21] sample=" + this.traceCurrentFirstPersonSample + " gun=" + gunItem.getName() + " aiming=" + GunItemRenderer.fmt(aimingProgress) + " modelScale=" + GunItemRenderer.fmt(modelScale) + " boneLookupScale=1.0000 requestedPath=" + requestedPath + " availablePaths=" + String.valueOf(poseMatrices.keySet()) + " selectedPath=" + selectedPath + " inverse=" + (selected != null ? GunItemRenderer.formatMatrixSummary((Matrix4f)selected.getSecond()) : "null"));
    }

    private void logFirstPersonPipGeometry(PoseStack poseStack, GeoBone bone, double aimingProgress) {
        List cubes = bone.getCubes();
        if (cubes == null || cubes.isEmpty()) {
            Platform.LOGGER.info("[fp-pip-trace-1.21] sample=" + this.traceCurrentFirstPersonSample + " bone=" + bone.getName() + " cubes=empty");
            return;
        }
        GeoQuad quad = ((GeoCube)cubes.get(0)).quads()[3];
        Matrix4f pose = new Matrix4f((Matrix4fc)poseStack.last().pose());
        Matrix4f modelView = new Matrix4f((Matrix4fc)RenderSystem.getModelViewMatrix());
        Matrix4f projection = new Matrix4f((Matrix4fc)RenderSystem.getProjectionMatrix());
        Matrix4f poseProjection = new Matrix4f((Matrix4fc)projection).mul((Matrix4fc)pose);
        Matrix4f fullProjection = new Matrix4f((Matrix4fc)projection).mul((Matrix4fc)modelView).mul((Matrix4fc)pose);
        Minecraft minecraft = Minecraft.getInstance();
        double projectionFov = projection.m11() != 0.0f ? Math.toDegrees(2.0 * Math.atan(1.0 / (double)Math.abs(projection.m11()))) : Double.NaN;
        Platform.LOGGER.info("[fp-pip-trace-1.21] sample=" + this.traceCurrentFirstPersonSample + " gun=" + (this.gunClientState != null ? this.gunClientState.getGunItem().getName() : "null") + " aiming=" + GunItemRenderer.fmt((float)aimingProgress) + " pass=" + String.valueOf((Object)RenderPass.current()) + " shaders=" + IrisCompat.getInstance().isShaderPackEnabled() + " configuredFov=" + String.valueOf(minecraft.options.fov().get()) + " gunFov=" + GunItemRenderer.fmt((float)ClientEventHandler.getGunFov()) + " projectionFov=" + GunItemRenderer.fmt((float)projectionFov) + " fallbackZoom=" + ClientEventHandler.isPipFallbackZoomActive() + " window=" + minecraft.getWindow().getWidth() + "x" + minecraft.getWindow().getHeight() + " pose=" + GunItemRenderer.formatMatrixSummary(pose) + " modelView=" + GunItemRenderer.formatMatrixSummary(modelView) + " projection.diag=(" + GunItemRenderer.fmt(projection.m00()) + "," + GunItemRenderer.fmt(projection.m11()) + "," + GunItemRenderer.fmt(projection.m22()) + "," + GunItemRenderer.fmt(projection.m33()) + ") rawQuad=" + GunItemRenderer.formatTransformedQuadBounds(new Matrix4f(), quad, false) + " poseQuad=" + GunItemRenderer.formatTransformedQuadBounds(pose, quad, false) + " ndcProjectionPose=" + GunItemRenderer.formatTransformedQuadBounds(poseProjection, quad, true) + " ndcProjectionModelViewPose=" + GunItemRenderer.formatTransformedQuadBounds(fullProjection, quad, true));
    }

    private static String formatMatrixSummary(Matrix4f matrix) {
        Vector3f translation = matrix.getTranslation(new Vector3f());
        float sx = (float)Math.sqrt(matrix.m00() * matrix.m00() + matrix.m01() * matrix.m01() + matrix.m02() * matrix.m02());
        float sy = (float)Math.sqrt(matrix.m10() * matrix.m10() + matrix.m11() * matrix.m11() + matrix.m12() * matrix.m12());
        float sz = (float)Math.sqrt(matrix.m20() * matrix.m20() + matrix.m21() * matrix.m21() + matrix.m22() * matrix.m22());
        return "t=(" + GunItemRenderer.fmt(translation.x) + "," + GunItemRenderer.fmt(translation.y) + "," + GunItemRenderer.fmt(translation.z) + ") s=(" + GunItemRenderer.fmt(sx) + "," + GunItemRenderer.fmt(sy) + "," + GunItemRenderer.fmt(sz) + ")";
    }

    private static String formatTransformedQuadBounds(Matrix4f transform, GeoQuad quad, boolean divideByW) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        float minW = Float.POSITIVE_INFINITY;
        float maxW = Float.NEGATIVE_INFINITY;
        for (GeoVertex vertex : quad.vertices()) {
            Vector3f position = vertex.position();
            Vector4f transformed = transform.transform(new Vector4f(position.x(), position.y(), position.z(), 1.0f));
            minW = Math.min(minW, transformed.w);
            maxW = Math.max(maxW, transformed.w);
            if (divideByW && Math.abs(transformed.w) > 1.0E-6f) {
                transformed.div(transformed.w);
            }
            minX = Math.min(minX, transformed.x);
            minY = Math.min(minY, transformed.y);
            minZ = Math.min(minZ, transformed.z);
            maxX = Math.max(maxX, transformed.x);
            maxY = Math.max(maxY, transformed.y);
            maxZ = Math.max(maxZ, transformed.z);
        }
        return "min=(" + GunItemRenderer.fmt(minX) + "," + GunItemRenderer.fmt(minY) + "," + GunItemRenderer.fmt(minZ) + ") max=(" + GunItemRenderer.fmt(maxX) + "," + GunItemRenderer.fmt(maxY) + "," + GunItemRenderer.fmt(maxZ) + ") size=(" + GunItemRenderer.fmt(maxX - minX) + "," + GunItemRenderer.fmt(maxY - minY) + ") w=(" + GunItemRenderer.fmt(minW) + "," + GunItemRenderer.fmt(maxW) + ")";
    }

    public ResourceLocation getTextureLocation(GunItem animatable) {
        ResourceLocation texture = null;
        HierarchicalRenderContext hrc = HierarchicalRenderContext.getRoot();
        if (hrc != null) {
            ItemStack itemStack = hrc.getItemStack();
            texture = SkinFeature.getTexture(itemStack);
        }
        if (texture == null) {
            texture = super.getTextureLocation((Item)animatable);
        }
        return texture;
    }

    @Override
    public GeoRenderer<GunItem> getRenderer() {
        return this;
    }

    public GunClientState getGunClientState() {
        return this.gunClientState;
    }

    private BakedGeoModel getLeftHandModel() {
        return (BakedGeoModel)GeckoLibCache.getBakedModels().get(this.leftHandModelResource);
    }

    private BakedGeoModel getRightHandModel() {
        return (BakedGeoModel)GeckoLibCache.getBakedModels().get(this.rightHandModelResource);
    }

    private BakedGeoModel getReticleModel() {
        return (BakedGeoModel)GeckoLibCache.getBakedModels().get(this.reticleModelResource);
    }

    private Player getPlayer(ItemDisplayContext itemDisplayContext) {
        if (itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || itemDisplayContext == ItemDisplayContext.GROUND) {
            return ClientUtil.getClientPlayer();
        }
        LivingEntity renderedEntity = ClientEventHandler.getCurrentEntityLiving();
        if (renderedEntity instanceof Player) {
            return (Player)renderedEntity;
        }
        return null;
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource bufferSourceOrig, int packedLight, int packedOverlay) {
        if (IrisCompat.getInstance().isRenderingShadows()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AttachmentManagerScreen && itemDisplayContext != ItemDisplayContext.GROUND) {
            return;
        }
        Player player = this.getPlayer(itemDisplayContext);
        if (player == null) {
            return;
        }
        MultiBufferSource wrappedBufferSource = RenderTypeProvider.getInstance().wrapBufferSource(bufferSourceOrig);
        try (HierarchicalRenderContext hrc = HierarchicalRenderContext.push(stack, itemDisplayContext);){
            this.renderPass(() -> {
                int slotIndex;
                boolean isOffhand;
                PipFeature pipFeature = PipFeature.getSelected(HierarchicalRenderContext.getRootItemStack());
                if (pipFeature != null) {
                    hrc.setAttribute("isPipFallbackEnabled", pipFeature.isFallbackRequired());
                }
                boolean bl = isOffhand = player != null && player.getOffhandItem() == stack;
                if (itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                    slotIndex = player.getInventory().selected;
                    isOffhand = false;
                } else if (itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                    slotIndex = 40;
                    isOffhand = true;
                } else {
                    slotIndex = player.getInventory().findSlotMatchingItem(stack);
                }
                GunClientState state = GunClientState.getState(player, stack, slotIndex, isOffhand);
                if (state != null) {
                    GunStatePoseProvider.getInstance().clear(state.getId());
                }
                boolean isFirstPerson = itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
                poseStack.pushPose();
                GunItem gunItem = (GunItem)stack.getItem();
                this.hasScopeOverlay = gunItem.getScopeOverlay() != null;
                GeoModel geoModel = this.getGeoModel();
                if (isFirstPerson) {
                    GeoBone scopeBone = geoModel.getBone(BONE_SCOPE).orElse(null);
                    this.initTransforms(player, stack, itemDisplayContext);
                    this.adjustFirstPersonPose(stack, gunItem, state, poseStack, (GeoModel<GunItem>)geoModel, scopeBone);
                }
                this.gunClientState = state;
                this.currentBufferSource = wrappedBufferSource;
                this.currentPartialTick = mc.getTimer().getGameTimeDeltaPartialTick(true);
                try {
                    super.renderByItem(stack, itemDisplayContext, poseStack, wrappedBufferSource, packedLight, packedOverlay);
                }
                finally {
                    this.currentBufferSource = null;
                    this.currentPartialTick = 0.0f;
                    this.clearFirstPersonAlignmentState();
                    poseStack.popPose();
                }
            });
        }
    }

    @Override
    public void renderPass(Runnable runnablePass) {
        RenderPass.push(this.getRenderPass());
        try {
            runnablePass.run();
        }
        finally {
            RenderPass.pop();
        }
    }

    private void adjustFirstPersonPose(ItemStack itemStack, GunItem gunItem, GunClientState state, PoseStack poseStack, GeoModel<GunItem> geoModel, GeoBone scopeBone) {
        boolean traceFirstPersonTransform;
        boolean shouldMirrorOffhandTransform;
        ItemTransform appliedTransform;
        if (this.transforms == null) {
            return;
        }
        ItemDisplayContext displayContext = HierarchicalRenderContext.current().getItemDisplayContext();
        ItemTransform itemTransform = appliedTransform = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? this.transforms.firstPersonLeftHand : this.transforms.firstPersonRightHand;
        if (appliedTransform == null) {
            return;
        }
        ItemTransform targetTransform = appliedTransform;
        Player player = ClientUtil.getClientPlayer();
        ItemDisplayContext offhandDisplayContext = player != null ? GunItem.getFirstPersonDisplayContext(player, InteractionHand.OFF_HAND) : null;
        boolean bl = shouldMirrorOffhandTransform = player != null && displayContext == offhandDisplayContext && GunItem.shouldApplyOffhandFirstPersonMirror(player, itemStack);
        if (shouldMirrorOffhandTransform) {
            ItemTransform oppositeTransform;
            ItemTransform itemTransform2 = oppositeTransform = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? this.transforms.firstPersonRightHand : this.transforms.firstPersonLeftHand;
            if (oppositeTransform != null) {
                targetTransform = oppositeTransform;
            }
        }
        float aimingProgress = this.getAimingProgress(state);
        float rescale = gunItem.getModelScale();
        Matrix4f handAnchorPose = FirstPersonHandAnchorCapture.getCapturedPose();
        this.firstPersonBasePose = new Matrix4f((Matrix4fc)poseStack.last().pose());
        this.firstPersonItemStack = itemStack;
        this.firstPersonAppliedItemTransform = appliedTransform;
        this.firstPersonTargetItemTransform = targetTransform;
        this.firstPersonHandAnchorPose = handAnchorPose != null ? new Matrix4f((Matrix4fc)handAnchorPose) : null;
        this.firstPersonAimingProgress = aimingProgress;
        this.firstPersonModelScale = rescale;
        this.firstPersonDisplayContext = displayContext;
        this.traceCurrentFirstPersonFrame = traceFirstPersonTransform = this.shouldTraceFirstPersonTransform(gunItem, aimingProgress);
        if (traceFirstPersonTransform) {
            this.traceCurrentFirstPersonSample = ++traceFpSampleCounter;
        }
        if (traceFirstPersonTransform) {
            this.logFirstPersonTransformPose("entry", gunItem, aimingProgress, rescale, appliedTransform, poseStack);
            if (handAnchorPose != null) {
                this.logFirstPersonTransformMatrix("hand_anchor", gunItem, aimingProgress, rescale, appliedTransform, new Matrix4f((Matrix4fc)handAnchorPose));
            } else {
                Platform.LOGGER.info("[fp-trace-1.21] sample=" + this.traceCurrentFirstPersonSample + " phase=hand_anchor gun=" + gunItem.getName() + " pose=null");
            }
        }
        this.applyFirstPersonPreScopePose(poseStack, gunItem, appliedTransform, targetTransform, aimingProgress, rescale, handAnchorPose, traceFirstPersonTransform);
        if (traceFirstPersonTransform) {
            this.logFirstPersonTransformPose("before_geckolib_center", gunItem, aimingProgress, rescale, targetTransform, poseStack);
            this.logFirstPersonScopeSelection(itemStack, gunItem, rescale, aimingProgress);
        }
    }

    private void applyFirstPersonPreScopePose(PoseStack poseStack, GunItem gunItem, ItemTransform appliedTransform, ItemTransform targetTransform, float aimingProgress, float rescale, @Nullable Matrix4f handAnchorPose, boolean traceFirstPersonTransform) {
        float v = 1.0f - aimingProgress;
        boolean leftHand = this.firstPersonDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        float handSideSign = leftHand ? -1.0f : 1.0f;
        float appliedTranslationX = handSideSign * appliedTransform.translation.x;
        float appliedRotationX = appliedTransform.rotation.x;
        float appliedRotationY = leftHand ? -appliedTransform.rotation.y : appliedTransform.rotation.y;
        float appliedRotationZ = leftHand ? -appliedTransform.rotation.z : appliedTransform.rotation.z;
        float targetTranslationX = handSideSign * targetTransform.translation.x;
        float targetRotationX = targetTransform.rotation.x;
        float targetRotationY = leftHand ? -targetTransform.rotation.y : targetTransform.rotation.y;
        float targetRotationZ = leftHand ? -targetTransform.rotation.z : targetTransform.rotation.z;
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(new Quaternionf().rotationXYZ(-appliedRotationX * ((float)Math.PI / 180), -appliedRotationY * ((float)Math.PI / 180), -appliedRotationZ * ((float)Math.PI / 180)));
        poseStack.translate(-appliedTranslationX, -appliedTransform.translation.y, -appliedTransform.translation.z);
        if (traceFirstPersonTransform) {
            this.logFirstPersonTransformPose("after_item_transform_undo", gunItem, aimingProgress, rescale, appliedTransform, poseStack);
        }
        if (!Mth.equal((float)rescale, (float)1.0f)) {
            Matrix4f pose = poseStack.last().pose();
            if (handAnchorPose != null) {
                float anchorX = handAnchorPose.m30();
                float anchorY = handAnchorPose.m31();
                float anchorZ = handAnchorPose.m32();
                pose.m30(anchorX + (pose.m30() - anchorX) * rescale);
                pose.m31(anchorY + (pose.m31() - anchorY) * rescale);
                pose.m32(anchorZ + (pose.m32() - anchorZ) * rescale);
            } else {
                pose.m30(pose.m30() * rescale);
                pose.m31(pose.m31() * rescale);
                pose.m32(pose.m32() * rescale);
            }
        }
        if (traceFirstPersonTransform) {
            this.logFirstPersonTransformPose("after_scale_correction", gunItem, aimingProgress, rescale, targetTransform, poseStack);
        }
        poseStack.translate(targetTranslationX * rescale, targetTransform.translation.y * rescale, targetTransform.translation.z * rescale);
        poseStack.mulPose(new Quaternionf().rotationXYZ(v * targetRotationX * ((float)Math.PI / 180), v * targetRotationY * ((float)Math.PI / 180), v * targetRotationZ * ((float)Math.PI / 180)));
        poseStack.translate(aimingProgress * -targetTranslationX * rescale, aimingProgress * -targetTransform.translation.y * rescale, aimingProgress * -targetTransform.translation.z * rescale);
        poseStack.scale(rescale, rescale, rescale);
        if (traceFirstPersonTransform) {
            this.logFirstPersonTransformPose("after_model_scale", gunItem, aimingProgress, rescale, targetTransform, poseStack);
        }
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        this.applyDualWieldFirstPersonPose(poseStack, aimingProgress);
        float curve = Mth.sin((float)((float)Math.PI * aimingProgress));
        double aimingCurveX = gunItem.getAimingCurveX();
        double aimingCurveY = gunItem.getAimingCurveY();
        double aimingCurveZ = gunItem.getAimingCurveZ();
        double aimingCurvePitch = gunItem.getAimingCurvePitch();
        double aimingCurveYaw = gunItem.getAimingCurveYaw();
        double aimingCurveRoll = gunItem.getAimingCurveRoll();
        poseStack.translate((double)curve * aimingCurveX, (double)curve * aimingCurveY, (double)curve * aimingCurveZ);
        poseStack.mulPose(new Quaternionf((double)curve * aimingCurvePitch * 0.01745329238474369, (double)curve * aimingCurveYaw * 0.01745329238474369, (double)curve * aimingCurveRoll * 0.01745329238474369, 1.0));
        poseStack.translate(handSideSign * 0.48f * v, -1.12f * v, -0.72f * v);
        poseStack.translate(handSideSign * -0.006f, 0.6f, 0.0f);
    }

    private void applyDualWieldFirstPersonPose(PoseStack poseStack, float aimingProgress) {
        Player player = ClientUtil.getClientPlayer();
        float spreadProgress = this.dualWieldSpreadEaser.update(player != null && GunItem.hasGunsInMainAndAltSlots(player) ? 1.0f : 0.0f);
        if (this.firstPersonDisplayContext == null || spreadProgress <= 1.0E-4f) {
            return;
        }
        float sideSign = this.firstPersonDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1.0f : 1.0f;
        float spread = 0.2f * spreadProgress;
        poseStack.translate(sideSign * spread, 0.0f, 0.0f);
        float currentDepth = Math.abs(poseStack.last().pose().m32());
        float convergenceDepth = Math.max(currentDepth, 1.25f);
        float inwardYaw = Mth.clamp((float)((float)Math.atan2(spread, convergenceDepth) * 57.295776f), (float)0.0f, (float)4.0f);
        float manualBlend = Mth.lerp((float)Mth.clamp((float)aimingProgress, (float)0.0f, (float)1.0f), (float)1.0f, (float)0.35f);
        float yaw = sideSign * (inwardYaw + -5.0f * spreadProgress * manualBlend);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
    }

    private void applyCameraSpaceScopeAlignment(PoseStack poseStack, GunItem gunItem) {
        if (this.firstPersonBasePose == null || this.firstPersonItemStack == null || this.firstPersonAppliedItemTransform == null || this.firstPersonTargetItemTransform == null || this.firstPersonAimingProgress <= 1.0E-4f) {
            return;
        }
        ScopeBoneSelection selectedScope = this.selectScopeBone(this.firstPersonItemStack, BONE_SCOPE);
        if (selectedScope == null) {
            selectedScope = this.selectScopeBone(this.firstPersonItemStack, BONE_SCOPE_PIP);
        }
        if (selectedScope == null) {
            return;
        }
        PoseStack fullyAimedPose = new PoseStack();
        fullyAimedPose.last().pose().set((Matrix4fc)this.firstPersonBasePose);
        this.applyFirstPersonPreScopePose(fullyAimedPose, gunItem, this.firstPersonAppliedItemTransform, this.firstPersonTargetItemTransform, 1.0f, this.firstPersonModelScale, this.firstPersonHandAnchorPose, false);
        fullyAimedPose.translate(0.5f, 0.51f, 0.5f);
        Matrix4f aimingTransform = ScopeAlignmentHelper.computeAimingTransform(fullyAimedPose, selectedScope.inverseScopeBoneMatrix(), SCOPE_AIM_POINT_OFFSET, (-0.7f + gunItem.getAdsZOffset()) * this.firstPersonModelScale);
        if (aimingTransform == null) {
            return;
        }
        poseStack.mulPose(this.scopeSwitchAimingInterpolator.resolve(aimingTransform, selectedScope.scopePath(), this.firstPersonAimingProgress));
        if (this.traceCurrentFirstPersonFrame) {
            Platform.LOGGER.info("[fp-ads-target-1.21] sample=" + this.traceCurrentFirstPersonSample + " gun=" + gunItem.getName() + " aiming=" + GunItemRenderer.fmt(this.firstPersonAimingProgress) + " targetZ=" + GunItemRenderer.fmt((-0.7f + gunItem.getAdsZOffset()) * this.firstPersonModelScale) + " correction=" + GunItemRenderer.formatMatrixSummary(aimingTransform));
            this.logFirstPersonTransformPose("after_scope_alignment", gunItem, this.firstPersonAimingProgress, this.firstPersonModelScale, this.firstPersonTargetItemTransform, poseStack);
        }
    }

    @Nullable
    private ScopeBoneSelection selectScopeBone(ItemStack itemStack, String boneName) {
        Pair<ItemStack, Matrix4f> selected;
        NavigableMap<String, Pair<ItemStack, Matrix4f>> inverseBones = AttachmentModelInfo.findInverseBoneMatrices(itemStack, boneName, 1.0f);
        if (inverseBones.isEmpty()) {
            return null;
        }
        Features.EnabledFeature aimingFeature = Features.getFirstEnabledFeature(itemStack, AimingFeature.class);
        String selectedPath = aimingFeature != null ? aimingFeature.ownerPath() : null;
        Pair<ItemStack, Matrix4f> pair = selected = aimingFeature != null ? (Pair<ItemStack, Matrix4f>)inverseBones.get(selectedPath) : null;
        if (selected == null) {
            selectedPath = (String)inverseBones.firstKey();
            selected = inverseBones.firstEntry().getValue();
        }
        return new ScopeBoneSelection(selectedPath, new Matrix4f((Matrix4fc)selected.getSecond()));
    }

    private void clearFirstPersonAlignmentState() {
        this.firstPersonBasePose = null;
        this.firstPersonItemStack = null;
        this.firstPersonAppliedItemTransform = null;
        this.firstPersonTargetItemTransform = null;
        this.firstPersonHandAnchorPose = null;
        this.firstPersonAimingProgress = 0.0f;
        this.firstPersonModelScale = 1.0f;
        this.firstPersonDisplayContext = null;
        this.traceCurrentFirstPersonFrame = false;
    }

    private void applyFirstPersonBodyInertia(ItemStack itemStack, PoseStack poseStack, float aimingProgress) {
        ClientEventHandler clientEventHandler = ClientEventHandler.getInstance();
        if (clientEventHandler == null) {
            return;
        }
        float aimingAttenuation = Mth.lerp((float)Mth.clamp((float)aimingProgress, (float)0.0f, (float)1.0f), (float)1.0f, (float)0.0f);
        float inertiaPitch = Mth.clamp((float)(clientEventHandler.getCurrentGunBodyInertiaPitch() * -1.0f * 1.0f * aimingAttenuation), (float)-0.010471975f, (float)0.010471975f);
        float inertiaYaw = Mth.clamp((float)(clientEventHandler.getCurrentGunBodyInertiaYaw() * -1.0f * 1.5f * aimingAttenuation), (float)-0.02268928f, (float)0.02268928f);
        if (Math.abs(inertiaPitch) < 1.0E-4f && Math.abs(inertiaYaw) < 1.0E-4f) {
            return;
        }
        Vector3f pivot = new Vector3f((Vector3fc)FIRST_PERSON_BODY_INERTIA_FALLBACK_PIVOT);
        poseStack.translate(pivot.x, pivot.y, pivot.z);
        poseStack.mulPose(Axis.XP.rotation(inertiaPitch));
        poseStack.mulPose(Axis.YP.rotation(inertiaYaw));
        poseStack.translate(-pivot.x, -pivot.y, -pivot.z);
    }

    private float getAimingProgress(@Nullable GunClientState state) {
        if (state == null) {
            return 0.0f;
        }
        BiDirectionalInterpolator aimingController = (BiDirectionalInterpolator)state.getAnimationController("aiming");
        return aimingController != null ? (float)aimingController.getValue() : 0.0f;
    }

    public void applyFirstPersonBodyInertiaForCurrentState(ItemStack itemStack, PoseStack poseStack) {
        this.applyFirstPersonBodyInertia(itemStack, poseStack, this.getAimingProgress(this.gunClientState));
    }

    @Nullable
    private Vector3f resolveFirstPersonBodyInertiaPivot(ItemStack rootStack) {
        Vector3f pivot = this.resolveFirstPersonBodyInertiaPivot(rootStack, PRIMARY_MUZZLE_PIVOT_BONES);
        if (pivot != null) {
            return pivot;
        }
        return this.resolveFirstPersonBodyInertiaPivot(rootStack, FALLBACK_MUZZLE_PIVOT_BONES);
    }

    @Nullable
    private Vector3f resolveFirstPersonBodyInertiaPivot(ItemStack rootStack, String[] candidateBones) {
        HierarchicalRenderContext current = HierarchicalRenderContext.current();
        if (current == null) {
            return null;
        }
        ItemDisplayContext displayContext = current.getItemDisplayContext();
        for (String boneName : candidateBones) {
            Vector3f pivot = this.selectActiveMuzzlePivot(rootStack, displayContext, boneName);
            if (pivot == null) continue;
            return pivot;
        }
        return null;
    }

    @Nullable
    private Vector3f selectActiveMuzzlePivot(ItemStack rootStack, ItemDisplayContext displayContext, String boneName) {
        NavigableMap<String, Pair<ItemStack, Matrix4f>> inverseBoneMatrices = AttachmentModelInfo.findInverseBoneMatrices(rootStack, boneName, 1.0f);
        if (inverseBoneMatrices.isEmpty()) {
            return null;
        }
        Map.Entry selectedEntry = null;
        int selectedDepth = -1;
        for (Map.Entry entry : inverseBoneMatrices.entrySet()) {
            ItemStack currentStack = (ItemStack)((Pair)entry.getValue()).getFirst();
            if (!ActiveMuzzleFeature.isActiveMuzzle(rootStack, currentStack, displayContext, boneName)) continue;
            int currentDepth = this.getAttachmentPathDepth((String)entry.getKey());
            if (selectedEntry != null && currentDepth <= selectedDepth) continue;
            selectedEntry = entry;
            selectedDepth = currentDepth;
        }
        if (selectedEntry == null) {
            return null;
        }
        Matrix4f pivotMatrix = new Matrix4f((Matrix4fc)((Pair)selectedEntry.getValue()).getSecond()).invert();
        Vector3f pivotLocal = pivotMatrix.getTranslation(new Vector3f());
        float forwardDepth = Math.max(0.0f, -pivotLocal.z);
        if (forwardDepth > 5.0f) {
            pivotLocal.z = -5.0f;
        }
        return pivotLocal;
    }

    private int getAttachmentPathDepth(@Nullable String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return 0;
        }
        int depth = 0;
        for (int index = 0; index < path.length(); ++index) {
            if (path.charAt(index) != '/') continue;
            ++depth;
        }
        return depth;
    }

    private void initTransforms(Player player, ItemStack stack, ItemDisplayContext itemDisplayContext) {
        this.transforms = Platform.getInstance().getItemTransforms(player, stack).apply(itemDisplayContext);
    }

    public void createVerticesOfQuad(GeoQuad quad, Matrix4f poseState, Vector3f normal, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        float[][] texUV;
        if (RenderPass.current() == RenderPass.RETICLE && quad.direction() != Direction.SOUTH) {
            return;
        }
        GeoVertex[] vertices = quad.vertices();
        if (this.glowingSpriteUVProviderSupplier != null) {
            SpriteUVProvider spriteUVProvider = this.glowingSpriteUVProviderSupplier.get();
            float[] uv = spriteUVProvider.getSpriteUV(this.glowingProgress);
            float minU = uv[0];
            float minV = uv[1];
            float maxU = uv[2];
            float maxV = uv[3];
            texUV = new float[][]{{minU, minV}, {maxU, minV}, {maxU, maxV}, {minU, maxV}};
        } else {
            texUV = new float[][]{{0.0f, 0.0f}, {1.0f, 0.0f}, {1.0f, 1.0f}, {0.0f, 1.0f}};
        }
        for (int i = 0; i < vertices.length; ++i) {
            float texV;
            float texU;
            GeoVertex vertex = vertices[i];
            Vector3f position = vertex.position();
            Vector4f vector4f = poseState.transform(new Vector4f(position.x(), position.y(), position.z(), 1.0f));
            RenderPass renderPass = RenderPass.current();
            if (renderPass == RenderPass.GLOW && this.useCustomGlowingTexture) {
                texU = texUV[i][0];
                texV = texUV[i][1];
            } else {
                texU = vertex.texU();
                texV = vertex.texV();
            }
            buffer.addVertex(vector4f.x(), vector4f.y(), vector4f.z(), color, texU, texV, packedOverlay, packedLight, normal.x(), normal.y(), normal.z());
        }
    }

    private boolean shouldRenderBone(String boneName) {
        HierarchicalRenderContext current = HierarchicalRenderContext.current();
        ItemStack rootStack = HierarchicalRenderContext.getRoot().getItemStack();
        boolean shouldRender = true;
        for (GeoRenderLayer layer : this.getRenderLayers()) {
            RenderPass renderPass;
            RenderPass renderPass2;
            if (!(layer instanceof RenderApprover)) continue;
            RenderApprover renderApprover = (RenderApprover)layer;
            if (layer instanceof RenderPassProvider) {
                RenderPassProvider rp = (RenderPassProvider)layer;
                renderPass2 = rp.getRenderPass();
            } else {
                renderPass2 = null;
            }
            if (renderApprover.approveRendering(renderPass = renderPass2, boneName, rootStack, current.getItemStack(), current.getPath(), current.getItemDisplayContext())) continue;
            shouldRender = false;
            break;
        }
        return shouldRender && (boneName.charAt(0) != '_' || RenderPass.current() == RenderPass.AMMO_COUNTER || boneName.equals("_shelleject_"));
    }

    public void renderRecursively(PoseStack poseStack, GunItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        boolean shouldRender;
        ItemDisplayContext itemDisplayContext;
        HierarchicalRenderContext current = HierarchicalRenderContext.current();
        ItemStack rootStack = HierarchicalRenderContext.getRoot().getItemStack();
        if (!isReRender && current != null && current.getItemStack() == rootStack && bone.getName().equals(BONE_CAMERA) && ((itemDisplayContext = current.getItemDisplayContext()) == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND)) {
            float rotX = bone.getRotX();
            float rotY = bone.getRotY();
            float rotZ = bone.getRotZ();
            cameraBoneRotX = rotX;
            cameraBoneRotY = rotY;
            cameraBoneRotZ = rotZ;
            if (itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                cameraBoneRotRightX = rotX;
                cameraBoneRotRightY = rotY;
                cameraBoneRotRightZ = rotZ;
            } else {
                cameraBoneRotLeftX = rotX;
                cameraBoneRotLeftY = rotY;
                cameraBoneRotLeftZ = rotZ;
            }
        }
        if (shouldRender = this.approveRendering(this.getRenderPass(), bone.getName(), rootStack, current.getItemStack(), current.getPath(), current.getItemDisplayContext())) {
            boolean isGlowEnabled;
            if (!isReRender && RenderPass.current() == RenderPass.MAIN_ITEM && current.getItemStack() == rootStack && bone.getName().equals("_shelleject_")) {
                poseStack.pushPose();
                software.bernie.geckolib.util.RenderUtil.translateToPivotPoint((PoseStack)poseStack, (GeoBone)bone);
                this.renderShellEffects(poseStack, rootStack, packedLight, packedOverlay);
                poseStack.popPose();
            }
            GlowAnimationController glowEffect = this.gunClientState != null ? (GlowAnimationController)this.gunClientState.getAnimationController("glowEffect" + String.valueOf(RenderPass.getEffectId())) : null;
            Runnable r = () -> {
                RotationAnimationController spinner;
                RotationAnimationController rotationAnimationController = spinner = this.gunClientState != null ? (RotationAnimationController)this.gunClientState.getAnimationController("rotation" + bone.getName()) : null;
                if (spinner != null) {
                    spinner.renderRecursively(this, poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
                } else {
                    super.renderRecursively(poseStack, (Item)animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
                }
            };
            boolean bl = isGlowEnabled = glowEffect != null && glowEffect.getGlowingPartNames().contains(bone.getName());
            if (isGlowEnabled) {
                try (HierarchicalRenderContext subHrc = HierarchicalRenderContext.push(current.getItemStack(), current.getItemDisplayContext());){
                    GlowingItemLayer.setGlowEnabled(isGlowEnabled);
                    this.renderWithFirstPersonBodyInertiaIfNeeded(poseStack, bone, current, isReRender, r);
                }
            } else {
                this.renderWithFirstPersonBodyInertiaIfNeeded(poseStack, bone, current, isReRender, r);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderWithFirstPersonBodyInertiaIfNeeded(PoseStack poseStack, GeoBone bone, @Nullable HierarchicalRenderContext current, boolean isReRender, Runnable renderAction) {
        if (isReRender || current == null || bone.getParent() != null || RenderPass.current() != RenderPass.MAIN_ITEM) {
            renderAction.run();
            return;
        }
        ItemDisplayContext itemDisplayContext = current.getItemDisplayContext();
        if (itemDisplayContext != ItemDisplayContext.FIRST_PERSON_LEFT_HAND && itemDisplayContext != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            renderAction.run();
            return;
        }
        poseStack.pushPose();
        this.applyFirstPersonBodyInertia(current.getItemStack(), poseStack, this.getAimingProgress(this.gunClientState));
        try {
            renderAction.run();
        }
        finally {
            poseStack.popPose();
        }
    }

    public void renderRecursivelySuper(PoseStack poseStack, GunItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        super.renderRecursively(poseStack, (Item)animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
    }

    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        RenderPass renderPass = RenderPass.current();
        if (!this.shouldRenderBone(bone.getName())) {
            return;
        }
        HierarchicalRenderContext hrc = HierarchicalRenderContext.current();
        ItemDisplayContext itemDisplayContext = hrc.getItemDisplayContext();
        boolean isFirstPerson = itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        double aimingProgress = 0.0;
        if (this.gunClientState != null) {
            BiDirectionalInterpolator aimingController = (BiDirectionalInterpolator)this.gunClientState.getAnimationController("aiming");
            aimingProgress = aimingController.getValue();
            if ((!MiscUtil.isGreaterThanZero(this.gunClientState.getGunItem().getPipScopeZoom()) || !Config.pipScopesEnabled) && this.hasScopeOverlay && !this.gunClientState.isReloading() && isFirstPerson && aimingProgress > 0.4) {
                return;
            }
        }
        if (this.traceCurrentFirstPersonFrame && isFirstPerson && bone.getName().equals(BONE_SCOPE_PIP) && renderPass == RenderPass.PIP_OVERLAY) {
            this.logFirstPersonPipGeometry(poseStack, bone, aimingProgress);
        }
        switch (renderPass) {
            case GLOW: {
                this.renderGlow(poseStack, bone, buffer, packedLight, packedOverlay, color);
                break;
            }
            case HANDS: {
                boolean renderMainRightOnly;
                LocalPlayer player = Minecraft.getInstance().player;
                ItemStack renderedStack = HierarchicalRenderContext.getRootItemStack();
                GunItem.OperableGunContext renderedContext = player != null ? GunItem.resolveRenderableContextForStack((Player)player, renderedStack) : null;
                boolean renderOffhandOnly = renderedContext != null && renderedContext.offhand();
                boolean bl = renderMainRightOnly = player != null && GunItem.shouldUseRightHandOnlyMainGrip((Player)player, renderedContext);
                if (bone.getName().equals(BONE_RIGHTARM) && !renderOffhandOnly) {
                    this.renderRightArm(poseStack, bone, buffer, packedLight, packedOverlay, color);
                    break;
                }
                if (!bone.getName().equals(BONE_LEFTARM) || renderMainRightOnly) break;
                this.renderLeftArm(poseStack, bone, buffer, packedLight, packedOverlay, color);
                break;
            }
            case AMMO_COUNTER: {
                if (!bone.getName().equals(BONE_AMMO_SCREEN)) break;
                this.renderAmmoCounter(poseStack, bone, buffer, packedLight, packedOverlay, color);
                break;
            }
            case PIP: {
                if (!bone.getName().equals(BONE_SCOPE_PIP)) break;
                this.renderPip(poseStack, bone, buffer, packedLight, color, aimingProgress);
                break;
            }
            case PIP_OVERLAY: {
                if (!bone.getName().equals(BONE_SCOPE_PIP) || !(aimingProgress > 0.0)) break;
                this.renderPipOverlay(poseStack, bone, buffer, packedLight, color, aimingProgress, PipItemLayer.isParallaxEnabled());
                break;
            }
            case PIP_MASK: {
                if (!bone.getName().equals(BONE_SCOPE_PIP)) break;
                this.renderPipMask(poseStack, bone, buffer, packedLight, color, aimingProgress);
                break;
            }
            case PIP_MASK_FAR: {
                if (!bone.getName().equals(BONE_SCOPE_PIP)) break;
                this.renderPipMaskFar(poseStack, bone, buffer, packedLight, color, aimingProgress);
                break;
            }
            case SEE_THROUGH_MASK: {
                if (!bone.getName().equals(BONE_SCOPE_PIP)) break;
                this.renderSeeThroughMask(poseStack, bone, buffer, packedLight, color, aimingProgress);
                break;
            }
            case SEE_THROUGH_MASK_FRONT: {
                if (!bone.getName().equals(BONE_SCOPE_PIP)) break;
                this.renderSeeThroughMaskFront(poseStack, bone, buffer, packedLight, color, aimingProgress);
                break;
            }
            case SEE_THROUGH_OVERLAY: {
                if (!bone.getName().equals(BONE_SCOPE_PIP) || !(aimingProgress > 0.0)) break;
                this.renderSeeThroughOverlay(poseStack, bone, buffer, packedLight, color, aimingProgress, PipItemLayer.isParallaxEnabled());
                break;
            }
            case RETICLE: {
                boolean isParallaxEnabled = ReticleItemLayer.isParallaxEnabled();
                if (isParallaxEnabled && bone.getName().equals(BONE_RETICLE)) {
                    this.renderReticleWithParallax(poseStack, bone, buffer, packedLight, color, aimingProgress, hrc.getAttribute("max_angular_offset_cos", Float.valueOf(DEFAULT_MAX_ANGULAR_RETICLE_OFFSET)).floatValue());
                    break;
                }
                if (isParallaxEnabled || !bone.getName().equals(BONE_SCOPE)) break;
                this.renderReticle(poseStack, bone, buffer, packedLight, packedOverlay, color, aimingProgress);
                break;
            }
            case MUZZLE_FLASH: {
                if (!bone.getName().equals(BONE_MUZZLEFLASH) && !bone.getName().equals(BONE_MUZZLEFLASH2) && !bone.getName().equals(BONE_MUZZLEFLASH3) && !bone.getName().equals(BONE_MUZZLEFLASH_ALT)) break;
                this.renderMuzzleFlash(poseStack, bone, buffer, packedLight);
                break;
            }
            case MAIN_ITEM: 
            case ATTACHMENTS: {
                if (bone.getName().equals(BONE_MUZZLEFLASH) || bone.getName().equals(BONE_MUZZLEFLASH2) || bone.getName().equals(BONE_MUZZLEFLASH3) || bone.getName().equals(BONE_MUZZLEFLASH_ALT) || bone.getName().equals(BONE_MUZZLE) || bone.getName().equals(BONE_MUZZLE2) || bone.getName().equals(BONE_MUZZLE3)) {
                    if (!ActiveMuzzleFeature.isActiveMuzzle(HierarchicalRenderContext.getRootItemStack(), hrc.getItemStack(), itemDisplayContext, bone.getName())) break;
                    this.captureMuzzlePose(bone, poseStack, itemDisplayContext);
                    break;
                }
                if (!this.canRenderPart(bone.getName())) break;
                super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, color);
            }
        }
    }

    private void renderShellEffects(PoseStack poseStack, ItemStack rootStack, int packedLight, int packedOverlay) {
        if (this.gunClientState == null || this.currentBufferSource == null || rootStack == null) {
            return;
        }
        List<FirstPersonShellEffect> shellEffects = this.gunClientState.getShellEffects();
        if (shellEffects.isEmpty()) {
            return;
        }
        for (FirstPersonShellEffect shellEffect : shellEffects) {
            BakedGeoModel shellModel = (BakedGeoModel)GeckoLibCache.getBakedModels().get(shellEffect.getModelResource());
            if (shellModel == null) {
                if (!MISSING_SHELL_MODEL_WARNINGS.add(shellEffect.getModelResource())) continue;
                Platform.LOGGER.warn("Missing shell casing model resource {}", (Object)shellEffect.getModelResource());
                continue;
            }
            RenderType shellRenderType = RenderType.entityCutout((ResourceLocation)shellEffect.getTextureResource());
            VertexConsumer shellBuffer = this.currentBufferSource.getBuffer(shellRenderType);
            poseStack.pushPose();
            Vec3 renderPosition = shellEffect.getRenderPosition(this.currentPartialTick);
            poseStack.translate(renderPosition.x, renderPosition.y, renderPosition.z);
            poseStack.mulPose(shellEffect.getRenderOrientation(this.currentPartialTick));
            Vec3 renderOffset = shellEffect.getRenderOffset();
            poseStack.translate(renderOffset.x, renderOffset.y, renderOffset.z);
            float scale = shellEffect.getRenderScale(this.currentPartialTick);
            poseStack.scale(scale, scale, scale);
            if (DEBUG_SHELL) {
                Platform.LOGGER.info("[shell-debug] render gun={} stateId={} model={} pos=({}, {}, {}) scale={}", (Object)this.gunClientState.getGunItem().getName(), (Object)this.gunClientState.getId(), (Object)shellEffect.getModelResource(), (Object)String.format(Locale.ROOT, "%.4f", renderPosition.x), (Object)String.format(Locale.ROOT, "%.4f", renderPosition.y), (Object)String.format(Locale.ROOT, "%.4f", renderPosition.z), (Object)String.format(Locale.ROOT, "%.4f", Float.valueOf(scale)));
            }
            this.renderShellModel(shellModel, poseStack, shellBuffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private void renderShellModel(BakedGeoModel shellModel, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        for (GeoBone shellBone : shellModel.topLevelBones()) {
            this.renderShellBone(shellBone, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private void renderShellBone(GeoBone shellBone, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        software.bernie.geckolib.util.RenderUtil.prepMatrixForBone((PoseStack)poseStack, (GeoBone)shellBone);
        super.renderCubesOfBone(poseStack, shellBone, buffer, packedLight, packedOverlay, -1);
        for (GeoBone childBone : shellBone.getChildBones()) {
            this.renderShellBone(childBone, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private void renderGlow(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        GlowAnimationController glowEffect;
        if (this.gunClientState == null) {
            return;
        }
        if (GlowingItemLayer.isGlowEnabled() && (glowEffect = (GlowAnimationController)this.gunClientState.getAnimationController("glowEffect" + String.valueOf(RenderPass.getEffectId()))) != null) {
            glowEffect.renderCubesOfBone(this, poseStack, bone, buffer, packedLight, packedOverlay, color);
        }
    }

    private void renderMuzzleFlash(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight) {
        if (this.gunClientState == null || this.gunClientState.isReloading() || this.gunClientState.isInspecting()) {
            return;
        }
        List cubes = bone.getCubes();
        if (cubes != null && !cubes.isEmpty()) {
            GeoCube cube = (GeoCube)cubes.get(0);
            GeoQuad quad1 = cube.quads()[0];
            Vector3f v1Position = quad1.vertices()[0].position();
            GeoQuad quad2 = cube.quads()[2];
            Vector3f v3Position = quad2.vertices()[2].position();
            Vector3f position = new Vector3f();
            position = v3Position.sub((Vector3fc)v1Position, position);
            position = position.mul(0.5f);
            position = position.add((Vector3fc)v1Position);
            EffectRenderContext context = new EffectRenderContext().withPoseStack(poseStack).withPosition(new Vec3((double)position.x, (double)position.y, (double)position.z)).withVertexBuffer(buffer).withLightColor(packedLight);
            for (MuzzleFlashEffect effect : this.gunClientState.getMuzzleFlashEffects()) {
                UUID effectId = EffectRegistry.getEffectId(effect.getName());
                String selectedBone = effect.getSelectedMuzzleFlashBone();
                boolean isSelectedBone = AlternatingMuzzleFeature.shouldRenderMuzzleFlashBone(selectedBone, bone.getName());
                if (!isSelectedBone || !com.google.common.base.Objects.equal((Object)effectId, (Object)RenderPass.getEffectId())) continue;
                effect.render(context);
            }
        }
    }

    private void renderReticleWithParallax(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int color, double aimingProgress, float maxAngularOffsetCos) {
        if (bone == null) {
            return;
        }
        if (this.gunClientState == null) {
            return;
        }
        if (aimingProgress < 0.8) {
            return;
        }
        List cubes = bone.getCubes();
        if (cubes != null && !cubes.isEmpty()) {
            float threshold;
            Vector4f n;
            Vector4f p0;
            GeoCube cube = (GeoCube)cubes.get(0);
            GeoQuad northQuad = cube.quads()[3];
            Matrix4f modelMatrix = this.captureCubeMatrix(poseStack, cube, 3);
            Pair<Vector4f, Float> intersection = GunItemRenderer.getPlayerViewIntersection(modelMatrix, p0 = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f), n = new Vector4f(0.0f, 0.0f, -1.0f, 1.0f));
            Vector4f intersectionPoint = (Vector4f)intersection.getFirst();
            if (intersectionPoint == null) {
                return;
            }
            float angularOffsetCos = ((Float)intersection.getSecond()).floatValue();
            float f = threshold = aimingProgress < 1.0 || this.gunClientState.isReloading() || this.gunClientState.isInspecting() ? DEFAULT_MAX_ANGULAR_RETICLE_OFFSET_NOT_AIMED : maxAngularOffsetCos;
            if (angularOffsetCos > threshold) {
                poseStack.pushPose();
                float maxOffsetFromTheCenter = 1.0f;
                float smoothFactor = 0.8f;
                poseStack.translate(Mth.clamp((float)(intersectionPoint.x() * smoothFactor), (float)(-maxOffsetFromTheCenter), (float)maxOffsetFromTheCenter), Mth.clamp((float)(intersectionPoint.y() * smoothFactor), (float)(-maxOffsetFromTheCenter), (float)maxOffsetFromTheCenter), Mth.clamp((float)(intersectionPoint.z() * smoothFactor), (float)(-maxOffsetFromTheCenter), (float)maxOffsetFromTheCenter));
                float alpha = (float)aimingProgress;
                RenderUtil.renderQuad(poseStack, northQuad, buffer, 0.0f, 0.0f, color);
                poseStack.popPose();
            }
        }
    }

    private void renderReticle(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color, double aimingProgress) {
        if (this.gunClientState == null || this.gunClientState.isReloading() || this.gunClientState.isInspecting()) {
            return;
        }
        BakedGeoModel reticleBakedGeoModel = this.getReticleModel();
        GeoBone reticleBone = (GeoBone)reticleBakedGeoModel.getBone(BONE_SCOPE).get();
        if (reticleBone != null) {
            poseStack.pushPose();
            if (aimingProgress > 0.9 && !this.gunClientState.isReloading()) {
                this.applyRefTransforms(poseStack, bone, reticleBone);
                double yaw = ClientEventHandler.reticleInertiaController.getYaw();
                double pitch = ClientEventHandler.reticleInertiaController.getPitch();
                Quaternionf q = new Quaternionf(pitch, yaw, 0.0, 1.0);
                poseStack.translate(-yaw * 25.0, pitch * 25.0, -8.0);
                poseStack.mulPose(q);
                super.renderCubesOfBone(poseStack, reticleBone, buffer, packedLight, packedOverlay, color);
            }
            poseStack.popPose();
        }
    }

    private void renderPip(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int color, double aimingProgress) {
        List cubes;
        poseStack.pushPose();
        if (aimingProgress > 0.9 && this.gunClientState != null && (cubes = bone.getCubes()) != null && !cubes.isEmpty()) {
            GeoCube cube = (GeoCube)cubes.get(0);
            GeoQuad northQuad = cube.quads()[3];
            ClientSystem.getInstance().getAuxLevelRenderer().renderToBuffer(poseStack, northQuad, buffer, packedLight);
        }
        poseStack.popPose();
    }

    private void renderAmmoCounter(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, color);
        AmmoCounterFeature feature = AmmoCounterFeature.getEnabled(HierarchicalRenderContext.getRootItemStack());
        if (feature == null || this.gunClientState == null || this.currentBufferSource == null) {
            return;
        }
        List cubes = bone.getCubes();
        if (cubes == null || cubes.isEmpty()) {
            return;
        }
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(HierarchicalRenderContext.getRootItemStack());
        if (fireModeInstance == null) {
            return;
        }
        ItemStack rootStack = HierarchicalRenderContext.getRootItemStack();
        int currentAmmo = this.gunClientState.getAmmoCount(fireModeInstance);
        int maxAmmo = ((GunItem)rootStack.getItem()).getMaxAmmoCapacity(rootStack, fireModeInstance);
        String ammoText = AmmoCounterFeature.formatAmmoCount(currentAmmo, maxAmmo);
        this.renderAmmoCounterText(poseStack, ((GeoCube)cubes.get(0)).quads()[3], ammoText, feature.getTextColor());
    }

    private void renderAmmoCounterText(PoseStack poseStack, GeoQuad quad, String text, int textColor) {
        Font font = Minecraft.getInstance().font;
        if (font == null || text == null || text.isEmpty()) {
            return;
        }
        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        for (GeoVertex vertex : quad.vertices()) {
            Vector3f position = vertex.position();
            minX = Math.min(minX, position.x());
            maxX = Math.max(maxX, position.x());
            minY = Math.min(minY, position.y());
            maxY = Math.max(maxY, position.y());
            minZ = Math.min(minZ, position.z());
            maxZ = Math.max(maxZ, position.z());
        }
        float quadWidth = maxX - minX;
        float quadHeight = maxY - minY;
        int textWidth = font.width(text);
        if (quadWidth <= 0.0f || quadHeight <= 0.0f || textWidth <= 0) {
            return;
        }
        float f = quadWidth * 0.85f / (float)textWidth;
        Objects.requireNonNull(font);
        float scale = Math.min(f, quadHeight * 0.85f / 9.0f);
        if (!Float.isFinite(scale) || scale <= 0.0f) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate((minX + maxX) * 0.5f, (minY + maxY) * 0.5f, (minZ + maxZ) * 0.5f + 0.001f);
        poseStack.scale(scale, -scale, scale);
        float x = (float)(-textWidth) * 0.5f;
        Objects.requireNonNull(font);
        float y = (float)(-9) * 0.5f;
        font.drawInBatch(text, x, y, textColor, false, poseStack.last().pose(), this.currentBufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        poseStack.popPose();
        MultiBufferSource multiBufferSource = this.currentBufferSource;
        if (multiBufferSource instanceof Flushable) {
            Flushable flushable = (Flushable)multiBufferSource;
            flushable.flush();
        }
    }

    private void renderPipOverlay(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int color, double aimingProgress, boolean isParallaxEnabled) {
        List cubes = bone.getCubes();
        if (cubes != null && !cubes.isEmpty()) {
            Vector4f n;
            Vector4f p0;
            Matrix4f modelMatrix;
            Pair<Vector4f, Float> intersection;
            Vector4f intersectionPoint;
            GeoCube cube = (GeoCube)cubes.get(0);
            GeoQuad northQuad = cube.quads()[3];
            if (isParallaxEnabled && (intersectionPoint = (Vector4f)(intersection = GunItemRenderer.getPlayerViewIntersection(modelMatrix = this.captureCubeMatrix(poseStack, cube, 3), p0 = new Vector4f(0.0f, 0.0f, 0.01f, 1.0f), n = new Vector4f(0.0f, 0.0f, -1.0f, 1.0f))).getFirst()) != null) {
                float maxOffsetFromTheCenter = 1.0f;
                float smoothFactor = 0.8f;
                poseStack.translate(Mth.clamp((float)(intersectionPoint.x() * smoothFactor), (float)(-maxOffsetFromTheCenter), (float)maxOffsetFromTheCenter), Mth.clamp((float)(intersectionPoint.y() * smoothFactor), (float)(-maxOffsetFromTheCenter), (float)maxOffsetFromTheCenter), Mth.clamp((float)(intersectionPoint.z() * smoothFactor), (float)(-maxOffsetFromTheCenter), (float)maxOffsetFromTheCenter));
            }
            float alpha = (float)aimingProgress;
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 0.001f);
            RenderUtil.renderQuad(poseStack, northQuad, buffer, 0.0f, 0.0f, color);
            poseStack.popPose();
        }
    }

    private void renderSeeThroughOverlay(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int color, double aimingProgress, boolean isParallaxEnabled) {
        List cubes = bone.getCubes();
        if (cubes != null && !cubes.isEmpty()) {
            Vector4f n;
            Vector4f p0;
            Matrix4f modelMatrix;
            Pair<Vector4f, Float> intersection;
            Vector4f intersectionPoint;
            GeoCube cube = (GeoCube)cubes.get(0);
            GeoQuad northQuad = cube.quads()[3];
            if (isParallaxEnabled && (intersectionPoint = (Vector4f)(intersection = GunItemRenderer.getPlayerViewIntersection(modelMatrix = this.captureCubeMatrix(poseStack, cube, 3), p0 = new Vector4f(0.0f, 0.0f, 0.01f, 1.0f), n = new Vector4f(0.0f, 0.0f, -1.0f, 1.0f))).getFirst()) != null) {
                float maxOffsetFromTheCenter = 1.0f;
                float smoothFactor = 0.8f;
                poseStack.translate(Mth.clamp((float)(intersectionPoint.x() * smoothFactor), (float)(-maxOffsetFromTheCenter), (float)maxOffsetFromTheCenter), Mth.clamp((float)(intersectionPoint.y() * smoothFactor), (float)(-maxOffsetFromTheCenter), (float)maxOffsetFromTheCenter), Mth.clamp((float)(intersectionPoint.z() * smoothFactor), (float)(-maxOffsetFromTheCenter), (float)maxOffsetFromTheCenter));
            }
            float alpha = (float)aimingProgress;
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 0.001f);
            RenderUtil.renderQuad(poseStack, northQuad, buffer, 0.0f, 0.0f, color);
            poseStack.popPose();
        }
    }

    private void renderSeeThroughMask(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int color, double aimingProgress) {
        List cubes = bone.getCubes();
        if (cubes != null && !cubes.isEmpty()) {
            GeoCube cube = (GeoCube)cubes.get(0);
            GeoQuad northQuad = cube.quads()[3];
            float alpha = (float)aimingProgress;
            poseStack.pushPose();
            RenderUtil.renderQuad(poseStack, northQuad, buffer, 0.0f, 0.0f, color);
            poseStack.popPose();
        }
    }

    private void renderSeeThroughMaskFront(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int color, double aimingProgress) {
        List cubes = bone.getCubes();
        if (cubes != null && !cubes.isEmpty()) {
            GeoCube cube = (GeoCube)cubes.get(0);
            GeoQuad northQuad = cube.quads()[3];
            GeoQuad resizedQuad = RenderUtil.resizeQuad(northQuad, 4.6f * (float)aimingProgress);
            float alpha = (float)aimingProgress;
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, -2.0f);
            RenderUtil.renderQuad(poseStack, resizedQuad, buffer, 0.0f, 0.0f, color);
            poseStack.popPose();
        }
    }

    private void renderPipMask(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int color, double aimingProgress) {
        List cubes = bone.getCubes();
        if (cubes != null && !cubes.isEmpty()) {
            GeoCube cube = (GeoCube)cubes.get(0);
            GeoQuad northQuad = cube.quads()[3];
            float alpha = (float)aimingProgress;
            poseStack.pushPose();
            RenderUtil.renderQuad(poseStack, northQuad, buffer, 0.0f, 0.0f, color);
            poseStack.popPose();
        }
    }

    private void renderPipMaskFar(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int color, double aimingProgress) {
        List cubes = bone.getCubes();
        if (cubes != null && !cubes.isEmpty()) {
            GeoCube cube = (GeoCube)cubes.get(0);
            GeoQuad northQuad = cube.quads()[3];
            GeoQuad resizedQuad = RenderUtil.resizeQuad(northQuad, 5.0f * (float)aimingProgress);
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, -2.0f);
            RenderUtil.renderQuad(poseStack, resizedQuad, buffer, 0.0f, 0.0f, color);
            poseStack.popPose();
        }
    }

    private void renderLeftArm(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        BakedGeoModel handsBakedGeoModel = this.getLeftHandModel();
        GeoBone leftArmBone = handsBakedGeoModel.getBone(BONE_LEFTARM).orElse(null);
        if (leftArmBone != null) {
            poseStack.pushPose();
            this.applyArmRefTransforms(poseStack, bone, leftArmBone);
            super.renderCubesOfBone(poseStack, leftArmBone, buffer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }
    }

    private void renderRightArm(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        BakedGeoModel handsBakedGeoModel = this.getRightHandModel();
        GeoBone rightArmBone = handsBakedGeoModel.getBone(BONE_RIGHTARM).orElse(null);
        if (rightArmBone != null) {
            poseStack.pushPose();
            this.applyArmRefTransforms(poseStack, bone, rightArmBone);
            super.renderCubesOfBone(poseStack, rightArmBone, buffer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }
    }

    private void captureMuzzlePose(GeoBone refBone, PoseStack poseStack, ItemDisplayContext itemDisplayContext) {
        if (this.gunClientState == null) {
            return;
        }
        poseStack.pushPose();
        GeoCube refCube = (GeoCube)refBone.getCubes().get(0);
        software.bernie.geckolib.util.RenderUtil.translateToPivotPoint((PoseStack)poseStack, (GeoCube)refCube);
        software.bernie.geckolib.util.RenderUtil.rotateMatrixAroundCube((PoseStack)poseStack, (GeoCube)refCube);
        software.bernie.geckolib.util.RenderUtil.translateAwayFromPivotPoint((PoseStack)poseStack, (GeoCube)refCube);
        poseStack.translate(refBone.getPivotX() / 16.0f, refBone.getPivotY() / 16.0f, refBone.getPivotZ() / 16.0f);
        GunStatePoseProvider gunStatePoseProvider = GunStatePoseProvider.getInstance();
        if (itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            GunStatePoseProvider.PoseContext poseContext = null;
            if (refBone.getName().equals(BONE_MUZZLE) || refBone.getName().equals(BONE_MUZZLE2)) {
                poseContext = GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE;
            } else if (refBone.getName().equals(BONE_MUZZLEFLASH) || refBone.getName().equals(BONE_MUZZLEFLASH2)) {
                poseContext = GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE_FLASH;
            } else if (refBone.getName().equals(BONE_MUZZLEFLASH_ALT)) {
                poseContext = GunStatePoseProvider.PoseContext.FIRST_PERSON_MUZZLE_FLASH_ALT;
            }
            if (poseContext != null) {
                Minecraft mc = Minecraft.getInstance();
                Camera camera = mc.gameRenderer.getMainCamera();
                if (IrisCompat.getInstance().isShaderPackEnabled()) {
                    PoseStack poseStack1 = new PoseStack();
                    poseStack1.mulPose(new Matrix4f().rotate((Quaternionfc)camera.rotation()));
                    poseStack1.mulPose(poseStack.last().pose());
                    gunStatePoseProvider.setPose(this.gunClientState, poseContext, poseStack1.last().copy());
                } else {
                    gunStatePoseProvider.setPose(this.gunClientState, poseContext, poseStack.last().copy());
                }
                this.setCurrentMuzzlePosition(poseStack, poseContext, itemDisplayContext);
            }
        } else if (itemDisplayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            GunStatePoseProvider.PoseContext poseContext = null;
            if (refBone.getName().equals(BONE_MUZZLE) || refBone.getName().equals(BONE_MUZZLE2)) {
                poseContext = GunStatePoseProvider.PoseContext.THIRD_PERSON_MUZZLE;
            } else if (refBone.getName().equals(BONE_MUZZLEFLASH) || refBone.getName().equals(BONE_MUZZLEFLASH2)) {
                poseContext = GunStatePoseProvider.PoseContext.THIRD_PERSON_MUZZLE_FLASH;
            } else if (refBone.getName().equals(BONE_MUZZLEFLASH_ALT)) {
                poseContext = GunStatePoseProvider.PoseContext.THIRD_PERSON_MUZZLE_FLASH_ALT;
            }
            if (poseContext != null) {
                gunStatePoseProvider.setPose(this.gunClientState, poseContext, poseStack.last().copy());
                this.setCurrentMuzzlePosition(poseStack, poseContext, itemDisplayContext);
            }
        }
        poseStack.popPose();
    }

    private void setCurrentMuzzlePosition(PoseStack poseStack, GunStatePoseProvider.PoseContext poseContext, ItemDisplayContext itemDisplayContext) {
        Matrix4f transform;
        poseStack.pushPose();
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        Matrix4f currentProjectionMatrix = RenderSystem.getProjectionMatrix();
        Matrix4f poseStackMatrix = poseStack.last().pose();
        double fov = ((Integer)mc.options.fov().get()).doubleValue();
        Matrix4f fovProjectionMatrix = mc.gameRenderer.getProjectionMatrix(fov);
        boolean firstPerson = itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        boolean shaderEnabled = IrisCompat.getInstance().isShaderPackEnabled();
        if (firstPerson && !shaderEnabled) {
            transform = new Matrix4f((Matrix4fc)poseStackMatrix);
        } else if (shaderEnabled) {
            transform = new Matrix4f();
            if (firstPerson) {
                transform = transform.mul((Matrix4fc)new Matrix4f().rotation((Quaternionfc)camera.rotation()));
            }
            transform = transform.mul((Matrix4fc)poseStackMatrix);
        } else {
            Quaternionf inverseCameraRotation = camera.rotation().conjugate(new Quaternionf());
            Matrix4f inverseViewRotationMatrix = new Matrix4f().rotation((Quaternionfc)inverseCameraRotation);
            transform = new Matrix4f().mul((Matrix4fc)new Matrix4f((Matrix4fc)RenderSystem.getModelViewMatrix()).invert()).mul((Matrix4fc)fovProjectionMatrix.invert(new Matrix4f())).mul((Matrix4fc)currentProjectionMatrix).mul((Matrix4fc)new Matrix4f((Matrix4fc)inverseViewRotationMatrix)).mul((Matrix4fc)poseStackMatrix);
        }
        Vector4f relPos = transform.transform(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
        Vector4f pos = new Vector4f((Vector4fc)relPos);
        pos.add((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z, 1.0f);
        Vector4f direction = transform.transform(new Vector4f(0.0f, 0.0f, -1.0f, 1.0f));
        direction.sub((Vector4fc)relPos);
        direction.normalize();
        GunStatePoseProvider.getInstance().setPositionAndDirection(this.gunClientState, poseContext, new Vec3((double)pos.x, (double)pos.y, (double)pos.z), new Vec3((double)direction.x, (double)direction.y, (double)direction.z));
        poseStack.popPose();
    }

    public void renderCubesOfBoneParent(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, color);
    }

    public void renderCubesOfBoneParent(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color, boolean useCustomGlowingTexture, Set<Direction> directions, Supplier<SpriteUVProvider> glowingSpriteUVProviderSupplier, float glowingProgress) {
        boolean hadCustomGlowingTexture = this.useCustomGlowingTexture;
        this.useCustomGlowingTexture = useCustomGlowingTexture;
        this.glowDirections = directions;
        this.glowingSpriteUVProviderSupplier = glowingSpriteUVProviderSupplier;
        this.glowingProgress = glowingProgress;
        super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, color);
        this.glowDirections = null;
        this.useCustomGlowingTexture = hadCustomGlowingTexture;
    }

    public void renderCube(PoseStack poseStack, GeoCube cube, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        software.bernie.geckolib.util.RenderUtil.translateToPivotPoint((PoseStack)poseStack, (GeoCube)cube);
        software.bernie.geckolib.util.RenderUtil.rotateMatrixAroundCube((PoseStack)poseStack, (GeoCube)cube);
        software.bernie.geckolib.util.RenderUtil.translateAwayFromPivotPoint((PoseStack)poseStack, (GeoCube)cube);
        Matrix3f normalisedPoseState = poseStack.last().normal();
        Matrix4f poseState = poseStack.last().pose();
        for (GeoQuad quad : cube.quads()) {
            if (quad == null || this.glowDirections != null && !this.glowDirections.contains(quad.direction())) continue;
            Vector3f normal = normalisedPoseState.transform(new Vector3f((Vector3fc)quad.normal()));
            software.bernie.geckolib.util.RenderUtil.fixInvertedFlatCube((GeoCube)cube, (Vector3f)normal);
            this.createVerticesOfQuad(quad, poseState, normal, buffer, packedLight, packedOverlay, color);
        }
    }

    private void applyArmRefTransforms(PoseStack poseStack, GeoBone refBone, GeoBone leftArmBone) {
        GeoCube leftArmBoneCube = (GeoCube)leftArmBone.getCubes().get(0);
        GeoCube refCube = (GeoCube)refBone.getCubes().get(0);
        GeoVertex leftArmBoneVertex = leftArmBoneCube.quads()[0].vertices()[0];
        GeoVertex refVertex = refCube.quads()[0].vertices()[0];
        float dx = refVertex.position().x() - leftArmBoneVertex.position().x();
        float dy = refVertex.position().y() - leftArmBoneVertex.position().y();
        float dz = refVertex.position().z() - leftArmBoneVertex.position().z();
        poseStack.translate(dx, dy, dz);
    }

    private void applyRefTransforms(PoseStack poseStack, GeoBone refBone, GeoBone actualBone) {
        GeoCube actualArmBoneCube = (GeoCube)actualBone.getCubes().get(0);
        GeoCube refCube = (GeoCube)refBone.getCubes().get(0);
        GeoVertex refQ0v0 = refCube.quads()[2].vertices()[0];
        GeoVertex refQ0v2 = refCube.quads()[2].vertices()[2];
        GeoVertex actualQ0v0 = actualArmBoneCube.quads()[2].vertices()[0];
        GeoVertex actualQ0v2 = actualArmBoneCube.quads()[2].vertices()[2];
        float refSizeX = Math.abs(refQ0v0.position().x - refQ0v2.position().x);
        float actualSizeX = Math.abs(actualQ0v0.position().x - actualQ0v2.position().x);
        float refSizeY = Math.abs(refQ0v0.position().y - refQ0v2.position().y);
        float actualSizeY = Math.abs(actualQ0v0.position().y - actualQ0v2.position().y);
        float refXLeft = refQ0v0.position().x;
        float refZLeft = refQ0v2.position().z;
        float actualXLeft = actualQ0v0.position().x;
        float actualYTop = actualQ0v0.position().y;
        float refYTop = refQ0v0.position().y;
        float actualZLeft = actualQ0v0.position().z;
        float dx = -(actualXLeft + (refXLeft - (refSizeX - actualSizeX) / 2.0f));
        float dy = refYTop + (actualSizeY - refSizeY) / 2.0f - actualYTop;
        float dz = refZLeft - actualZLeft;
        poseStack.translate(dx, dy, dz);
    }

    @Override
    public boolean approveRendering(RenderPass renderPass, String boneName, ItemStack rootStack, ItemStack currentStack, String path, ItemDisplayContext itemDisplayContext) {
        List<Features.EnabledFeature> enabledVisibilityFeatures = Features.getEnabledFeatures(rootStack, PartVisibilityFeature.class);
        ConditionContext conditionContext = new ConditionContext(null, rootStack, currentStack, this.gunClientState, itemDisplayContext);
        for (Features.EnabledFeature enabledVisibilityFeature : enabledVisibilityFeatures) {
            PartVisibilityFeature visibilityFeature = (PartVisibilityFeature)enabledVisibilityFeature.feature();
            if (visibilityFeature.isPartVisible((ItemLike)currentStack.getItem(), boneName, conditionContext)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isEffectLayer() {
        return false;
    }

    @Override
    public RenderType getRenderType() {
        return null;
    }

    @Nullable
    public RenderType getRenderType(GunItem animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        HierarchicalRenderContext rootContext = HierarchicalRenderContext.getRoot();
        Object isPipFallbackEnabled = rootContext.getAttribute("isPipFallbackEnabled");
        if (isPipFallbackEnabled != null && ((Boolean)isPipFallbackEnabled).booleanValue()) {
            return RenderTypeProvider.getInstance().getDefaultItemRenderType(new RenderTypeKey(texture, true, 517, 7680, 2));
        }
        return RenderTypeProvider.getInstance().getDefaultItemRenderType(new RenderTypeKey(texture, false, 0, 0, 0));
    }

    @Override
    public boolean isSupportedItemDisplayContext(ItemDisplayContext context) {
        return true;
    }

    @Override
    public boolean canRenderPart(String boneName) {
        return !boneName.equals(BONE_LEFTARM) && !boneName.equals(BONE_RIGHTARM) && !boneName.equals(BONE_RETICLE) && !boneName.equals(BONE_SCOPE) && !boneName.equals(BONE_SCOPE_PIP);
    }

    @Override
    public RenderPass getRenderPass() {
        return RenderPass.MAIN_ITEM;
    }

    @Override
    public Class<? extends Feature> getFeatureType() {
        return null;
    }

    private static Pair<Vector4f, Float> getPlayerViewIntersection(Matrix4f modelMatrix, Vector4f p0, Vector4f n) {
        Matrix4f transform3;
        Minecraft mc = Minecraft.getInstance();
        Player player = ClientUtil.getClientPlayer();
        float partialTicks = mc.getTimer().getGameTimeDeltaPartialTick(true);
        Vec3 playerEyePositionRelativeToCamera = new Vec3(0.0, 0.0, 0.0);
        Vec3 playerViewVector = player.getViewVector(partialTicks);
        if (IrisCompat.getInstance().isShaderPackEnabled()) {
            Camera camera = mc.gameRenderer.getMainCamera();
            Quaternionf cameraRotation = camera.rotation().conjugate(new Quaternionf());
            transform3 = new Matrix4f((Matrix4fc)modelMatrix).invert().mul((Matrix4fc)new Matrix4f((Matrix4fc)RenderSystem.getModelViewMatrix()).invert()).mul((Matrix4fc)new Matrix4f().rotate((Quaternionfc)cameraRotation));
        } else {
            transform3 = new Matrix4f((Matrix4fc)modelMatrix).invert();
        }
        double farDistance = 5.0;
        Vec3 farPointWorld = playerEyePositionRelativeToCamera.add(playerViewVector.scale(farDistance));
        Vector4f eyePositionModel = new Vector4f();
        transform3.transform((float)playerEyePositionRelativeToCamera.x, (float)playerEyePositionRelativeToCamera.y, (float)playerEyePositionRelativeToCamera.z, 1.0f, eyePositionModel);
        Vector4f farPointModel = new Vector4f();
        transform3.transform((float)farPointWorld.x, (float)farPointWorld.y, (float)farPointWorld.z, 1.0f, farPointModel);
        Vector4f l = new Vector4f((Vector4fc)farPointModel).sub((Vector4fc)eyePositionModel).normalize();
        Vector4f l0 = eyePositionModel;
        Vector4f p0MinusL0 = new Vector4f(p0.x, p0.y, p0.z, 1.0f).sub((Vector4fc)l0);
        float numerator = p0MinusL0.dot(n.x, n.y, n.z, 1.0f);
        float denominator = l.dot(n.x, n.y, n.z, 1.0f);
        Vector4f intersectionPoint = null;
        if (denominator > 0.0f) {
            float d = numerator / denominator;
            intersectionPoint = new Vector4f((Vector4fc)l).mul(d).add((Vector4fc)l0);
        }
        return Pair.of(intersectionPoint, (Object)Float.valueOf(denominator));
    }

    public Matrix4f captureCubeMatrix(PoseStack poseStack, GeoCube cube, int side) {
        GeoQuad quad = cube.quads()[side];
        poseStack.pushPose();
        software.bernie.geckolib.util.RenderUtil.translateToPivotPoint((PoseStack)poseStack, (GeoCube)cube);
        software.bernie.geckolib.util.RenderUtil.rotateMatrixAroundCube((PoseStack)poseStack, (GeoCube)cube);
        software.bernie.geckolib.util.RenderUtil.translateAwayFromPivotPoint((PoseStack)poseStack, (GeoCube)cube);
        Vector3f v0Pos = quad.vertices()[0].position();
        Vector3f v2Pos = quad.vertices()[2].position();
        poseStack.translate((v0Pos.x + v2Pos.x) * 0.5f, (v0Pos.y + v2Pos.y) * 0.5f, (v0Pos.z + v2Pos.z) * 0.5f);
        Matrix4f cubeMatrix = poseStack.last().pose();
        poseStack.popPose();
        return cubeMatrix;
    }

    public void preRender(PoseStack poseStack, GunItem animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        RenderPass renderPass = RenderPass.current();
        super.preRender(poseStack, (Item)animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (!isReRender && renderPass == RenderPass.MAIN_ITEM && this.firstPersonItemStack != null) {
            this.applyCameraSpaceScopeAlignment(poseStack, animatable);
        }
        if (renderPass == RenderPass.PIP_MASK || renderPass == RenderPass.PIP_MASK_FAR || renderPass == RenderPass.SEE_THROUGH_MASK || renderPass == RenderPass.SEE_THROUGH_MASK_FRONT) {
            this.updateAnimatedTextureFrame(animatable);
            for (GeoBone group : model.topLevelBones()) {
                this.renderRecursively(poseStack, animatable, group, (RenderType)null, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
            }
        }
    }

    private static final class ScopeSwitchAimingInterpolator {
        private static final float FULLY_AIMED_THRESHOLD = 0.999f;
        private final long durationNanos;
        private final Matrix4f fromMatrix = new Matrix4f();
        private final Matrix4f toMatrix = new Matrix4f();
        private boolean initialized;
        private boolean animating;
        private long startTimeNanos;
        private String lastScopePath;

        private ScopeSwitchAimingInterpolator(long durationMillis) {
            this.durationNanos = Math.max(1L, durationMillis * 1000000L);
        }

        private Matrix4f resolve(Matrix4f targetMatrix, String scopePath, float aimingProgress) {
            if (aimingProgress < 0.999f) {
                this.initialized = true;
                this.animating = false;
                this.lastScopePath = scopePath;
                this.fromMatrix.identity();
                this.toMatrix.set((Matrix4fc)targetMatrix);
                return ScopeAlignmentHelper.lerpAimingTransform(targetMatrix, aimingProgress);
            }
            long now = System.nanoTime();
            if (!this.initialized) {
                this.initialized = true;
                this.lastScopePath = scopePath;
                this.toMatrix.set((Matrix4fc)targetMatrix);
                return new Matrix4f((Matrix4fc)targetMatrix);
            }
            if (!com.google.common.base.Objects.equal((Object)scopePath, (Object)this.lastScopePath)) {
                this.fromMatrix.set((Matrix4fc)(this.animating ? this.getCurrent(now) : this.toMatrix));
                this.toMatrix.set((Matrix4fc)targetMatrix);
                this.startTimeNanos = now;
                this.animating = true;
                this.lastScopePath = scopePath;
            } else {
                if (!this.animating) {
                    this.toMatrix.set((Matrix4fc)targetMatrix);
                    return new Matrix4f((Matrix4fc)targetMatrix);
                }
                this.toMatrix.set((Matrix4fc)targetMatrix);
            }
            return this.getCurrent(now);
        }

        private Matrix4f getCurrent(long now) {
            if (!this.animating) {
                return new Matrix4f((Matrix4fc)this.toMatrix);
            }
            float progress = Mth.clamp((float)((float)(now - this.startTimeNanos) / (float)this.durationNanos), (float)0.0f, (float)1.0f);
            if (progress >= 1.0f) {
                this.animating = false;
                return new Matrix4f((Matrix4fc)this.toMatrix);
            }
            return new Matrix4f((Matrix4fc)this.fromMatrix).lerp((Matrix4fc)this.toMatrix, progress);
        }
    }

    private record ScopeBoneSelection(String scopePath, Matrix4f inverseScopeBoneMatrix) {
    }
}

