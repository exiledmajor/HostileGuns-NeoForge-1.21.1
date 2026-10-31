/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.world.entity.Entity
 */
package com.vicmatskiv.pointblank.client;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public interface EntityRendererBuilder<T extends EntityRendererBuilder<T, E, R>, E extends Entity, R extends EntityRenderer<E>> {
    public T withJsonObject(JsonObject var1);

    public R build(EntityRendererProvider.Context var1);
}

