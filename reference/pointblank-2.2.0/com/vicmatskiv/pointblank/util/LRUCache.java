/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.util;

import com.vicmatskiv.pointblank.Platform;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class LRUCache<K, V> {
    private final CacheImpl<K, V> clientCache;
    private final CacheImpl<K, V> serverCache;

    public LRUCache(int cacheSize) {
        this.clientCache = new CacheImpl(cacheSize);
        this.serverCache = new CacheImpl(cacheSize);
    }

    private CacheImpl<K, V> getCache() {
        return Platform.getInstance().isClientThread() ? this.clientCache : this.serverCache;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        CacheImpl<K, V> cache;
        CacheImpl<K, V> cacheImpl = cache = this.getCache();
        synchronized (cacheImpl) {
            Object value = cache.get(key);
            if (value != null || cache.containsKey(key)) {
                return value;
            }
            V computedValue = mappingFunction.apply(key);
            cache.put(key, computedValue);
            return computedValue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public V get(K key) {
        CacheImpl<K, V> cache;
        CacheImpl<K, V> cacheImpl = cache = this.getCache();
        synchronized (cacheImpl) {
            return cache.get(key);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public V put(K key, V value) {
        CacheImpl<K, V> cache;
        CacheImpl<K, V> cacheImpl = cache = this.getCache();
        synchronized (cacheImpl) {
            return cache.put(key, value);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public V remove(K key) {
        CacheImpl<K, V> cache;
        CacheImpl<K, V> cacheImpl = cache = this.getCache();
        synchronized (cacheImpl) {
            return cache.remove(key);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int size() {
        CacheImpl<K, V> cache;
        CacheImpl<K, V> cacheImpl = cache = this.getCache();
        synchronized (cacheImpl) {
            return cache.size();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void clear() {
        CacheImpl<K, V> cache;
        CacheImpl<K, V> cacheImpl = cache = this.getCache();
        synchronized (cacheImpl) {
            cache.clear();
        }
    }

    private static class CacheImpl<K, V>
    extends LinkedHashMap<K, V> {
        private static final long serialVersionUID = 1L;
        private final int cacheSize;

        private CacheImpl(int cacheSize) {
            super(16, 0.75f, false);
            this.cacheSize = cacheSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return this.size() > this.cacheSize;
        }
    }
}

