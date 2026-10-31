/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.vicmatskiv.pointblank.util;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public interface LazyOptional<T>
extends Supplier<T> {
    public static <T> LazyOptional<T> of(@NotNull Supplier<T> supplier) {
        return new Impl<T>(supplier);
    }

    public static final class Impl<T>
    implements LazyOptional<T> {
        private Supplier<T> supplier;
        private T instance;

        private Impl(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public final T get() {
            if (this.supplier != null) {
                this.instance = this.supplier.get();
                this.supplier = null;
            }
            return this.instance;
        }
    }
}

