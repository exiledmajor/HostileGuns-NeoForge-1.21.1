/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.core.particles.SimpleParticleType
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.RegistryService;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public class ParticleRegistry {
    private static RegistryService<ParticleType<?>> particleTypeRegistry = Platform.getInstance().getParticleTypeRegistry();
    public static final Supplier<SimpleParticleType> EFFECT_PARTICLE_TYPE = ParticleRegistry.register("effect", false);

    private static Supplier<SimpleParticleType> register(String name, boolean overrideLimiter) {
        return particleTypeRegistry.register(name, () -> new SimpleParticleType(overrideLimiter));
    }

    public static void init() {
    }
}

