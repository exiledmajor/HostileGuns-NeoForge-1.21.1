/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank;

import java.util.function.Supplier;

public interface RegistryService<T> {
    public <I extends T> Supplier<I> register(String var1, Supplier<? extends I> var2);
}

