/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank;

public interface Enableable {
    default public boolean isEnabled() {
        return true;
    }

    default public void setEnabled(boolean isEnabled) {
        throw new UnsupportedOperationException();
    }
}

