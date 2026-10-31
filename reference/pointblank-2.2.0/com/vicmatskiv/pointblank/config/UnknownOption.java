/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.config;

import com.vicmatskiv.pointblank.config.ConfigOption;
import com.vicmatskiv.pointblank.config.ConfigUtil;
import java.util.Collections;
import java.util.List;

public class UnknownOption
implements ConfigOption<String> {
    private final int index;
    private final String value;
    private final String name;
    private final List<String> serialized;

    public UnknownOption(String name, int index, String value, List<String> description) {
        this.name = name;
        this.index = index;
        this.value = value;
        String keyValueLine = String.format("%s = %s", this.getSimpleName(), this.get());
        this.serialized = description != null ? ConfigUtil.join(description, keyValueLine) : Collections.singletonList(keyValueLine);
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public List<String> getPath() {
        return Collections.singletonList(this.name);
    }

    @Override
    public List<String> getSerialized() {
        return this.serialized;
    }

    @Override
    public ConfigOption<?> createCopy(Object newValue, int newIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String get() {
        return this.value;
    }
}

