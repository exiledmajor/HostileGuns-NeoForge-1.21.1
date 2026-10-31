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

public class BooleanOption
implements ConfigOption<Boolean> {
    private final int index;
    private final ConfigOptionBuilder<Boolean, ?> builder;
    private final Boolean value;
    private final List<String> serialized;

    @NotNull
    static <B extends ConfigOptionBuilder<Boolean, B>> ConfigOptionBuilder<Boolean, B> builder(final Function<String, Boolean> futureOptionResolver, final int optionIndex) {
        return new ConfigOptionBuilder<Boolean, B>(){

            @Override
            public Supplier<Boolean> getSupplier() {
                return () -> (Boolean)futureOptionResolver.apply(this.getName());
            }

            @Override
            public Boolean normalize(Object value1) {
                if (!(value1 instanceof Boolean)) {
                    return (Boolean)this.defaultValue;
                }
                Boolean b = (Boolean)value1;
                return b;
            }

            @Override
            public ConfigOption<Boolean> build(String value1, List<String> description, int index) {
                this.validate();
                return new BooleanOption(index >= 0 ? index : optionIndex, this, value1 != null ? Boolean.parseBoolean(value1) : (Boolean)this.defaultValue, description);
            }
        };
    }

    BooleanOption(int index, ConfigOptionBuilder<Boolean, ?> builder, Boolean value, List<String> description) {
        this.index = index;
        this.builder = builder;
        this.value = value;
        String keyValueString = String.format("%s = %s", this.getSimpleName(), this.get());
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
    public Boolean get() {
        return this.value;
    }

    @Override
    public ConfigOption<?> createCopy(Object newValue, int newIndex) {
        return new BooleanOption(newIndex, this.builder, this.builder.normalize(newValue), null);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BooleanOption that = (BooleanOption)o;
        return Objects.equals(this.builder, that.builder) && Objects.equals(this.value, that.value) && Objects.equals(this.serialized, that.serialized);
    }

    public int hashCode() {
        return Objects.hash(this.builder, this.value, this.serialized);
    }
}

