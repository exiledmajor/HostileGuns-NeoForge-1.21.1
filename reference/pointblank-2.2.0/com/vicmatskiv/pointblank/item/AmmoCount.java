/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.ItemLike
 */
package com.vicmatskiv.pointblank.item;

import com.vicmatskiv.pointblank.item.FireModeInstance;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.level.ItemLike;

public class AmmoCount {
    private Map<ItemLike, AtomicInteger> ammoByType = new HashMap<ItemLike, AtomicInteger>();

    private AtomicInteger get(FireModeInstance fireModeInstance) {
        return this.ammoByType.computeIfAbsent((ItemLike)fireModeInstance.getAmmo(), ati -> new AtomicInteger());
    }

    public int incrementAmmoCount(FireModeInstance fireMode, int delta) {
        return this.get(fireMode).addAndGet(delta);
    }

    public void setAmmoCount(FireModeInstance fireMode, int mainAmmoCount) {
        this.get(fireMode).set(mainAmmoCount);
    }

    public int getAmmoCount(FireModeInstance fireMode) {
        int result = this.get(fireMode).get();
        return result;
    }
}

