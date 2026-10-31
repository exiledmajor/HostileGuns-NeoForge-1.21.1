/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.util;

import com.vicmatskiv.pointblank.Config;

public interface Tradeable {
    public static final int MAX_BUNDLE_QUANTITY = 64;

    public float getPrice();

    public int getTradeLevel();

    default public int getBundleQuantity() {
        float price = this.getPrice();
        int bundleQuantity = (double)price < Config.emeraldExchangeRate ? (int)(Config.emeraldExchangeRate / (double)price) : 1;
        return Math.min(bundleQuantity, 64);
    }
}

