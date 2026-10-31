/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.FeatureProvider;

public interface FeatureBuilder<T extends FeatureBuilder<T, F>, F extends Feature> {
    public T withJsonObject(JsonObject var1);

    public F build(FeatureProvider var1);
}

