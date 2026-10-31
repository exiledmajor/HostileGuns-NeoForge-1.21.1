/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.Minecraft
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 */
package com.vicmatskiv.pointblank.client;

import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.effect.FirstPersonShellEffect;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.feature.Features;
import com.vicmatskiv.pointblank.feature.ShellEjectionFeature;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class ShellEjectionLauncher {
    private static final boolean DEBUG_SHELL = Boolean.getBoolean("pointblank.debug.shell");
    private static final double DEBUG_SHELL_VELOCITY_SCALE = Double.parseDouble(System.getProperty("pointblank.debug.shell.velocityScale", "1.0"));
    private static final double DEBUG_SHELL_ANGULAR_VELOCITY_SCALE = Double.parseDouble(System.getProperty("pointblank.debug.shell.angularVelocityScale", String.valueOf(DEBUG_SHELL_VELOCITY_SCALE)));
    private static final double FIRST_PERSON_VELOCITY_SCALE = 0.14;
    private static final Vec3 PISTOL_ANGULAR_VELOCITY = new Vec3(18.0, 26.0, 34.0);
    private static final Vec3 RIFLE_ANGULAR_VELOCITY = new Vec3(24.0, 20.0, 40.0);
    private static final Vec3 SHOTGUN_ANGULAR_VELOCITY = new Vec3(10.0, 8.0, 18.0);
    private static final Vec3 PISTOL_ANGULAR_RANDOMIZE = new Vec3(8.0, 10.0, 12.0);
    private static final Vec3 RIFLE_ANGULAR_RANDOMIZE = new Vec3(10.0, 10.0, 14.0);
    private static final Vec3 SHOTGUN_ANGULAR_RANDOMIZE = new Vec3(4.0, 5.0, 7.0);

    private ShellEjectionLauncher() {
    }

    public static void spawnClientShellCasing(Player player, GunClientState state, ItemStack itemStack) {
        Feature feature;
        Minecraft minecraft = Minecraft.getInstance();
        if (DEBUG_SHELL) {
            Platform.LOGGER.info("[shell-debug] launcher entered gun={} stateId={} localPlayerMatch={} clientSide={} camera={} shellConfig={} hasTag={}", (Object)state.getGunItem().getName(), (Object)state.getId(), (Object)(minecraft.player == player ? 1 : 0), (Object)player.level().isClientSide(), (Object)minecraft.options.getCameraType(), (Object)Config.shellEjectionEnabled, (Object)(MiscUtil.getTag(itemStack) != null ? 1 : 0));
        }
        if (minecraft.player != player || !player.level().isClientSide()) {
            if (DEBUG_SHELL) {
                Platform.LOGGER.info("[shell-debug] launcher skipped: not local client player context");
            }
            return;
        }
        if (minecraft.options.getCameraType() != CameraType.FIRST_PERSON) {
            if (DEBUG_SHELL) {
                Platform.LOGGER.info("[shell-debug] launcher skipped: camera is {}", (Object)minecraft.options.getCameraType());
            }
            return;
        }
        if (!Config.shellEjectionEnabled) {
            if (DEBUG_SHELL) {
                Platform.LOGGER.info("[shell-debug] launcher skipped: shell ejection config disabled");
            }
            return;
        }
        Features.EnabledFeature enabledFeature = Features.getFirstEnabledFeature(itemStack, ShellEjectionFeature.class);
        if (enabledFeature == null || !((feature = enabledFeature.feature()) instanceof ShellEjectionFeature)) {
            if (DEBUG_SHELL) {
                ShellEjectionFeature shellEjectionFeature;
                Item item = itemStack.getItem();
                if (item instanceof FeatureProvider) {
                    FeatureProvider featureProvider = (FeatureProvider)item;
                    shellEjectionFeature = featureProvider.getFeature(ShellEjectionFeature.class);
                } else {
                    shellEjectionFeature = null;
                }
                ShellEjectionFeature itemFeature = shellEjectionFeature;
                Platform.LOGGER.info("[shell-debug] launcher skipped: no enabled shell feature gun={} rawItemFeature={} enabledFeatureCount={} hasTag={}", (Object)state.getGunItem().getName(), (Object)(itemFeature != null ? 1 : 0), (Object)Features.getEnabledFeatures(itemStack, ShellEjectionFeature.class).size(), (Object)(MiscUtil.getTag(itemStack) != null ? 1 : 0));
            }
            return;
        }
        ShellEjectionFeature feature2 = (ShellEjectionFeature)feature;
        FirstPersonShellEffect shellEffect = ShellEjectionLauncher.createShellEffect(player, feature2);
        if (shellEffect == null) {
            if (DEBUG_SHELL) {
                Platform.LOGGER.info("[shell-debug] launcher skipped: shell effect not created for trigger {}", (Object)feature2.getTrigger());
            }
            return;
        }
        if (DEBUG_SHELL) {
            Platform.LOGGER.info("[shell-debug] spawned gun={} stateId={} model={} texture={}", (Object)state.getGunItem().getName(), (Object)state.getId(), (Object)shellEffect.getModelResource(), (Object)shellEffect.getTextureResource());
        }
        state.addShellEffect(shellEffect);
    }

    @Nullable
    private static FirstPersonShellEffect createShellEffect(Player player, ShellEjectionFeature feature) {
        if (feature.getTrigger() != ShellEjectionFeature.Trigger.FIRE) {
            return null;
        }
        RandomSource random = player.getRandom();
        Vec3 localVelocity = ShellEjectionLauncher.toFirstPersonShellVelocity(ShellEjectionLauncher.applyRandomVariance(feature.getVelocity(), feature.getRandomize(), random), feature.getVerticalLiftScale()).scale(0.14 * DEBUG_SHELL_VELOCITY_SCALE);
        Vec3 angularVelocity = ShellEjectionLauncher.createAngularVelocity(feature.getVisuals().model(), random).scale(DEBUG_SHELL_ANGULAR_VELOCITY_SCALE);
        return new FirstPersonShellEffect(feature.getVisuals(), localVelocity, angularVelocity);
    }

    private static Vec3 toFirstPersonShellVelocity(Vec3 velocity, float verticalLiftScale) {
        return new Vec3(velocity.x, velocity.y * (double)verticalLiftScale, 0.0);
    }

    private static Vec3 createAngularVelocity(ResourceLocation modelResource, RandomSource random) {
        MotionProfile motionProfile = ShellEjectionLauncher.inferMotionProfile(modelResource);
        Vec3 baseAngularVelocity = switch (motionProfile.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PISTOL_ANGULAR_VELOCITY;
            case 1 -> RIFLE_ANGULAR_VELOCITY;
            case 2 -> SHOTGUN_ANGULAR_VELOCITY;
        };
        Vec3 angularRandomize = switch (motionProfile.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PISTOL_ANGULAR_RANDOMIZE;
            case 1 -> RIFLE_ANGULAR_RANDOMIZE;
            case 2 -> SHOTGUN_ANGULAR_RANDOMIZE;
        };
        return new Vec3(ShellEjectionLauncher.nextSignedMagnitude(random, baseAngularVelocity.x, angularRandomize.x), ShellEjectionLauncher.nextSignedMagnitude(random, baseAngularVelocity.y, angularRandomize.y), ShellEjectionLauncher.nextSignedMagnitude(random, baseAngularVelocity.z, angularRandomize.z));
    }

    private static MotionProfile inferMotionProfile(ResourceLocation modelResource) {
        String path = modelResource.getPath().toLowerCase(Locale.ROOT);
        if (path.contains("shotgun")) {
            return MotionProfile.SHOTGUN;
        }
        if (path.contains("pistol")) {
            return MotionProfile.PISTOL;
        }
        return MotionProfile.RIFLE;
    }

    private static Vec3 applyRandomVariance(Vec3 base, Vec3 variance, RandomSource random) {
        return new Vec3(base.x + ShellEjectionLauncher.nextSignedRandom(random, variance.x), base.y + ShellEjectionLauncher.nextSignedRandom(random, variance.y), base.z + ShellEjectionLauncher.nextSignedRandom(random, variance.z));
    }

    private static double nextSignedRandom(RandomSource random, double magnitude) {
        return magnitude <= 0.0 ? 0.0 : (random.nextDouble() * 2.0 - 1.0) * magnitude;
    }

    private static double nextSignedMagnitude(RandomSource random, double baseMagnitude, double varianceMagnitude) {
        double magnitude = Math.max(0.0, baseMagnitude + ShellEjectionLauncher.nextSignedRandom(random, varianceMagnitude));
        if (magnitude <= 1.0E-6) {
            return 0.0;
        }
        return random.nextBoolean() ? magnitude : -magnitude;
    }

    private static enum MotionProfile {
        PISTOL,
        RIFLE,
        SHOTGUN;

    }
}

