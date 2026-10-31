/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level$ExplosionInteraction
 */
package com.vicmatskiv.pointblank.item;

import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.Level;

public record ExplosionDescriptor(float power, boolean fire, Level.ExplosionInteraction interaction, String soundName, float soundVolume, List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> effects) {
}

