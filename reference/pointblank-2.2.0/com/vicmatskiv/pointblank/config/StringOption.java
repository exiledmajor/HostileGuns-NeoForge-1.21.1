/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.vicmatskiv.pointblank.config;

import com.vicmatskiv.pointblank.config.ConfigOption;
import com.vicmatskiv.pointblank.config.ConfigOptionBuilder;
import com.vicmatskiv.pointblank.config.ConfigUtil;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class StringOption
implements ConfigOption<String> {
    private final int index;
    private final ConfigOptionBuilder<String, ?> builder;
    private final String value;
    private final List<String> serialized;

    @NotNull
    static <B extends ConfigOptionBuilder<String, B>> ConfigOptionBuilder<String, B> builder(final Function<String, String> futureOptionResolver, final int optionIndex) {
        return new ConfigOptionBuilder<String, B>(){

            @Override
            public Supplier<String> getSupplier() {
                return () -> (String)futureOptionResolver.apply(this.getName());
            }

            @Override
            public String normalize(Object value1) {
                if (value1 instanceof String) {
                    String s = (String)value1;
                    return s;
                }
                return (String)this.defaultValue;
            }

            @Override
            public ConfigOption<String> build(String value1, List<String> description, int index) {
                this.validate();
                return new StringOption(index >= 0 ? index : optionIndex, this, value1 != null ? value1 : (String)this.defaultValue, description);
            }
        };
    }

    StringOption(int index, ConfigOptionBuilder<String, ?> builder, String value, List<String> description) {
        this.index = index;
        this.builder = builder;
        this.value = value;
        String keyValueString = String.format("%s = \"%s\"", this.getSimpleName(), this.get());
        this.serialized = description != null ? ConfigUtil.join(description, keyValueString) : List.of("#" + builder.description, keyValueString);
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public List<String> getPath() {
        return this.builder.path;
    }

    @Override
    public List<String> getSerialized() {
        return this.serialized;
    }

    @Override
    public String get() {
        return this.value;
    }

    @Override
    public ConfigOption<?> createCopy(Object newValue, int newIndex) {
        return new StringOption(newIndex, this.builder, this.builder.normalize(newValue), null);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        StringOption that = (StringOption)o;
        return Objects.equals(this.builder, that.builder) && Objects.equals(this.value, that.value) && Objects.equals(this.serialized, that.serialized);
    }

    public int hashCode() {
        return Objects.hash(this.builder, this.value, this.serialized);
    }
}

