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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ListOption
implements ConfigOption<List<String>> {
    private final int index;
    private final ConfigOptionBuilder<List<String>, ?> builder;
    private final List<String> value;
    private final List<String> serialized;

    @NotNull
    static <B extends ConfigOptionBuilder<List<String>, B>> ConfigOptionBuilder<List<String>, B> builder(final Function<String, List<String>> futureOptionResolver, final int optionIndex) {
        return new ConfigOptionBuilder<List<String>, B>(){

            @Override
            public Supplier<List<String>> getSupplier() {
                return () -> (List)futureOptionResolver.apply(this.getName());
            }

            @Override
            public List<String> normalize(Object value1) {
                if (value1 instanceof List) {
                    List list = (List)value1;
                    return list.stream().map(Object::toString).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                }
                if (value1 instanceof String) {
                    String s2 = (String)value1;
                    return this.parseListValue(s2);
                }
                return this.defaultValue != null ? new ArrayList((Collection)this.defaultValue) : new ArrayList();
            }

            @Override
            public ConfigOption<List<String>> build(String value1, List<String> description, int index) {
                this.validate();
                ArrayList<String> parsedValue = value1 != null ? this.parseListValue(value1) : (this.defaultValue != null ? new ArrayList<String>((Collection)this.defaultValue) : new ArrayList());
                return new ListOption(index >= 0 ? index : optionIndex, this, parsedValue, description);
            }

            private List<String> parseListValue(String value) {
                if (value == null || value.isBlank()) {
                    return new ArrayList<String>();
                }
                String trimmed = value.trim();
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    String content = trimmed.substring(1, trimmed.length() - 1).trim();
                    if (content.isEmpty()) {
                        return new ArrayList<String>();
                    }
                    return this.parseArrayContent(content);
                }
                return Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            }

            private List<String> parseArrayContent(String content) {
                ArrayList<String> result = new ArrayList<String>();
                StringBuilder current = new StringBuilder();
                boolean inQuotes = false;
                for (int i = 0; i < content.length(); ++i) {
                    char c = content.charAt(i);
                    if (c == '\"') {
                        inQuotes = !inQuotes;
                        continue;
                    }
                    if (c == ',' && !inQuotes) {
                        String item = current.toString().trim();
                        if (item.startsWith("\"") && item.endsWith("\"")) {
                            item = item.substring(1, item.length() - 1);
                        }
                        if (!item.isEmpty()) {
                            result.add(item);
                        }
                        current = new StringBuilder();
                        continue;
                    }
                    current.append(c);
                }
                String item = current.toString().trim();
                if (item.startsWith("\"") && item.endsWith("\"")) {
                    item = item.substring(1, item.length() - 1);
                }
                if (!item.isEmpty()) {
                    result.add(item);
                }
                return result;
            }
        };
    }

    ListOption(int index, ConfigOptionBuilder<List<String>, ?> builder, List<String> value, List<String> description) {
        this.index = index;
        this.builder = builder;
        this.value = Collections.unmodifiableList(new ArrayList<String>(value));
        String arrayString = value.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ", "[", "]"));
        String keyValueString = String.format("%s = %s", this.getSimpleName(), arrayString);
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
    public List<String> get() {
        return this.value;
    }

    @Override
    public ConfigOption<?> createCopy(Object newValue, int newIndex) {
        return new ListOption(newIndex, this.builder, this.builder.normalize(newValue), null);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ListOption that = (ListOption)o;
        return Objects.equals(this.builder, that.builder) && Objects.equals(this.value, that.value) && Objects.equals(this.serialized, that.serialized);
    }

    public int hashCode() {
        return Objects.hash(this.builder, this.value, this.serialized);
    }
}

