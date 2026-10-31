/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationRegistry;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimatorCompat;
import java.io.InputStream;
import java.util.function.Supplier;

public class ThirdPersonAnimationRegistry {
    public static final String DEFAULT_RIFLE_ANIMATIONS = "__DEFAULT_RIFLE_ANIMATIONS__";
    public static final String DEFAULT_PISTOL_ANIMATIONS = "__DEFAULT_PISTOL_ANIMATIONS__";
    private static final PlayerAnimationRegistry<?> REGISTRY = PlayerAnimatorCompat.getInstance().getAnimationRegistry();

    public static void register(String ownerId, Supplier<InputStream> resourceReaderFactory) {
        REGISTRY.register(ownerId, resourceReaderFactory);
    }

    public static void register(String ownerId, String resourceName) {
        ThirdPersonAnimationRegistry.register(ownerId, () -> ThirdPersonAnimationRegistry.class.getResourceAsStream(resourceName));
    }

    public static void init() {
        ThirdPersonAnimationRegistry.register(DEFAULT_RIFLE_ANIMATIONS, "/assets/pointblank/animations/player/rifle.animation.json");
        ThirdPersonAnimationRegistry.register(DEFAULT_PISTOL_ANIMATIONS, "/assets/pointblank/animations/player/pistol.animation.json");
    }
}

