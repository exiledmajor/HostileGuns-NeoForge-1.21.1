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

public class NumberOption<N extends Number>
implements ConfigOption<N> {
    private final int index;
    private final ConfigOptionBuilder<N, ?> builder;
    private final N value;
    private final List<String> serialized;

    @NotNull
    static <N extends Number, B extends ConfigOptionBuilder<N, B>> ConfigOptionBuilder<N, B> builder(final Class<N> cls, final Function<String, N> converter, final Function<String, Number> futureOptionResolver, final int optionIndex) {
        return new ConfigOptionBuilder<N, B>(){

            @Override
            public Supplier<N> getSupplier() {
                return () -> (Number)cls.cast(futureOptionResolver.apply(this.getName()));
            }

            @Override
            public N normalize(Object value1) {
                if (!(value1 instanceof Number)) {
                    return (Number)this.defaultValue;
                }
                Number number = (Number)value1;
                if (this.minValue != null && number.doubleValue() < ((Number)this.minValue).doubleValue()) {
                    return (Number)this.defaultValue;
                }
                if (this.maxValue != null && number.doubleValue() > ((Number)this.maxValue).doubleValue()) {
                    return (Number)this.defaultValue;
                }
                return number;
            }

            @Override
            public ConfigOption<N> build(String value1, List<String> description, int index) {
                this.validate();
                return new NumberOption<Number>(index >= 0 ? index : optionIndex, this, value1 != null ? (Number)((Number)converter.apply(value1)) : (Number)((Number)this.defaultValue), description);
            }
        };
    }

    NumberOption(int index, ConfigOptionBuilder<N, ?> builder, N value, List<String> description) {
        this.index = index;
        this.builder = builder;
        this.value = value;
        String keyValueLine = String.format("%s = %s", this.getSimpleName(), this.get());
        this.serialized = description != null ? ConfigUtil.join(description, keyValueLine) : List.of("#" + builder.description, String.format("#Range: %s ~ %s", builder.minValue, builder.maxValue), keyValueLine);
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public N get() {
        return this.value;
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
    public ConfigOption<?> createCopy(Object newValue, int newIndex) {
        return new NumberOption<Number>(newIndex, this.builder, (Number)this.builder.normalize(newValue), null);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        NumberOption that = (NumberOption)o;
        return Objects.equals(this.builder, that.builder) && Objects.equals(this.value, that.value) && Objects.equals(this.serialized, that.serialized);
    }

    public int hashCode() {
        return Objects.hash(this.builder, this.value, this.serialized);
    }
}

