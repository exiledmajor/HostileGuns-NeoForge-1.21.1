/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.player.Player
 */
package com.vicmatskiv.pointblank.util;

import java.util.function.Predicate;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public final class CancellableSound
extends AbstractTickableSoundInstance {
    private Predicate<CancellableSound> predicate;
    private Player player;

    public CancellableSound(Player player, SoundEvent soundEvent, SoundSource soundSource, RandomSource randomSource, Predicate<CancellableSound> predicate) {
        super(soundEvent, soundSource, randomSource);
        this.player = player;
        this.predicate = predicate;
    }

    public void tick() {
        if (!this.predicate.test(this)) {
            this.stop();
        }
    }

    public double getX() {
        return this.player.getX();
    }

    public double getY() {
        return this.player.getY();
    }

    public double getZ() {
        return this.player.getZ();
    }
}

