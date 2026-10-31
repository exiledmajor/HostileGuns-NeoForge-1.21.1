/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank;

import com.vicmatskiv.pointblank.util.ModInfo;
import java.util.ServiceLoader;

public interface CommonLoader {
    public static final CommonLoader INSTANCE = CommonLoader.load(CommonLoader.class);

    public static CommonLoader getInstance() {
        return INSTANCE;
    }

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }

    public ModInfo getModInfo(String var1);
}

