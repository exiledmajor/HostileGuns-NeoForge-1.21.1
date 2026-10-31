/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 */
package com.vicmatskiv.pointblank.client;

import com.vicmatskiv.pointblank.util.UpDownCounter;
import net.minecraft.world.entity.Entity;

public class LockableTarget {
    private static final int DEFAULT_LOCK_TIME_TICKS = 10;
    private Entity targetEntity;
    private UpDownCounter targetLockCounter = new UpDownCounter(this.lockTimeTicks);
    private long lockTimeTicks = 10L;
    private TargetLocker targetLocker;

    public void setLocker(TargetLocker targetLocker) {
        if (this.targetLocker != targetLocker) {
            this.targetLocker = targetLocker;
            if (targetLocker != null) {
                this.setLockTimeTicks(targetLocker.getTargetLockTimeTicks());
            } else {
                this.setLockTimeTicks(0L);
            }
        }
    }

    public boolean tryLock(Entity hitScanEntity) {
        if (this.targetEntity != null && this.targetEntity != hitScanEntity) {
            return false;
        }
        this.targetEntity = hitScanEntity;
        UpDownCounter.State previousState = this.targetLockCounter.getState();
        boolean wasAtMax = this.targetLockCounter.isAtMax();
        boolean result = this.targetLockCounter.countUp(false);
        if (this.targetLocker != null && result && this.targetLockCounter.isAtMax() && !wasAtMax) {
            this.targetLocker.onTargetLocked(hitScanEntity);
        } else if (this.targetLocker != null && result && previousState != UpDownCounter.State.UP && this.targetLockCounter.getState() == UpDownCounter.State.UP) {
            this.targetLocker.onTargetStartLocking(hitScanEntity);
        }
        return result;
    }

    public void unlock(Entity hitScanEntity) {
        boolean wasAtMax = this.targetLockCounter.isAtMax();
        this.targetLockCounter.countDown(true);
        if (this.targetLocker != null && wasAtMax) {
            this.targetLocker.onTargetStartUnlocking(this.targetEntity);
        }
        if (this.targetLockCounter.getState() == UpDownCounter.State.NONE) {
            this.targetEntity = hitScanEntity;
        }
    }

    public UpDownCounter getLockCounter() {
        return this.targetLockCounter;
    }

    public Entity getTargetEntity() {
        return this.targetEntity;
    }

    private void setLockTimeTicks(long lockTimeTicks) {
        this.lockTimeTicks = lockTimeTicks;
        this.targetLockCounter.setMaxValue(lockTimeTicks);
    }

    public long getLockTimeTicks() {
        return this.lockTimeTicks;
    }

    public static interface TargetLocker {
        public long getTargetLockTimeTicks();

        default public void onTargetStartLocking(Entity targetEntity) {
        }

        default public void onTargetLocked(Entity targetEntity) {
        }

        default public void onTargetStartUnlocking(Entity targetEntity) {
        }
    }
}

