/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package com.vicmatskiv.pointblank.mixin;

import com.vicmatskiv.pointblank.entity.EntityExt;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={Entity.class})
public class EntityMixin
implements EntityExt {
    @Unique
    private long lastHitSoundTimestamp;

    @Override
    public long getLastHitSoundTimestamp() {
        return this.lastHitSoundTimestamp;
    }

    @Override
    public void setLastHitSoundTimestamp(long lastHitSoundTimestamp) {
        this.lastHitSoundTimestamp = lastHitSoundTimestamp;
    }
}

