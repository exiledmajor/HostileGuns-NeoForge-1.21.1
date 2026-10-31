/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.renderer.GeoRenderer
 */
package com.vicmatskiv.pointblank.client.render;

import com.vicmatskiv.pointblank.client.render.RenderPassRenderer;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;

public interface RenderPassGeoRenderer<T extends GeoAnimatable>
extends GeoRenderer<T>,
RenderPassRenderer<T> {
}

