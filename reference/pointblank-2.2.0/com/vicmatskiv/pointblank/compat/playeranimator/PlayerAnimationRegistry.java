/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.compat.playeranimator;

import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimation;
import com.vicmatskiv.pointblank.compat.playeranimator.PlayerAnimationType;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

public interface PlayerAnimationRegistry<T> {
    public void reload();

    public void register(String var1, Supplier<InputStream> var2);

    public boolean isRegistered(String var1);

    public List<PlayerAnimation<T>> getAnimations(String var1, PlayerAnimationType var2);
}

