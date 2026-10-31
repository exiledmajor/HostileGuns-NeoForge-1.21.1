/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.sounds.SoundEvent
 */
package com.vicmatskiv.pointblank.util;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;

public record SoundInfo(Supplier<SoundEvent> soundEvent, float volume) {
}

