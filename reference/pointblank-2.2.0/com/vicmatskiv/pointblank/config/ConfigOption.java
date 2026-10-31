/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.config;

import java.util.List;
import java.util.function.Supplier;

public interface ConfigOption<T>
extends Supplier<T> {
    public int getIndex();

    public List<String> getPath();

    public List<String> getSerialized();

    default public String getName() {
        return String.join((CharSequence)".", this.getPath());
    }

    default public String getSimpleName() {
        List<String> path = this.getPath();
        return this.getPath().get(path.size() - 1);
    }

    default public ConfigOption<?> copy(ConfigOption<?> source, Object value) {
        return this.createCopy(source.get(), source.getIndex());
    }

    public ConfigOption<?> createCopy(Object var1, int var2);
}

