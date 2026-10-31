/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigUtil {
    public static <T, K> List<T> mergeCollections(Collection<T> refCollection, Collection<T> actualCollection, Function<T, K> getKey, Comparator<T> comparator, BiFunction<T, T, T> mergeRule) {
        LinkedHashMap<K, T> mergedMap = new LinkedHashMap<K, T>();
        for (T item : refCollection) {
            mergedMap.merge(getKey.apply(item), item, mergeRule);
        }
        for (T item : actualCollection) {
            K key = getKey.apply(item);
            if (!mergedMap.containsKey(key)) continue;
            mergedMap.merge(key, item, mergeRule);
        }
        return mergedMap.values().stream().sorted(comparator).collect(Collectors.toList());
    }

    static List<String> join(List<String> l, String keyValueEntry) {
        ArrayList<String> result = new ArrayList<String>();
        for (String e : l) {
            Object t = e.trim();
            if (!((String)t).startsWith("#")) {
                t = "#" + (String)t;
            }
            result.add((String)t);
        }
        result.add(keyValueEntry);
        return result;
    }
}

