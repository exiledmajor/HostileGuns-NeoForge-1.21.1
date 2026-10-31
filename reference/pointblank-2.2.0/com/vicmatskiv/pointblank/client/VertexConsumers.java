/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 */
package com.vicmatskiv.pointblank.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;

public class VertexConsumers {
    public static Function<VertexConsumer, VertexConsumer> PARTICLE = vc -> vc;
    public static Function<VertexConsumer, VertexConsumer> ENTITY = vc -> vc;
}

