/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionf
 */
package com.vicmatskiv.pointblank.client.effect;

import com.vicmatskiv.pointblank.client.effect.EffectRenderContext;
import com.vicmatskiv.pointblank.client.uv.SpriteUVProvider;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public interface Effect {
    public String getName();

    public ResourceLocation getTexture();

    public BlendMode getBlendMode();

    public boolean isDepthTestEnabled();

    public long getDuration();

    public long getDelay();

    public boolean isGlowEnabled();

    public int getBrightness();

    public void render(EffectRenderContext var1);

    public void launch(Entity var1);

    public float getGravity();

    public float getFriction();

    public boolean hasPhysics();

    public Supplier<Vec3> getStartPositionProvider();

    public Supplier<Vec3> getVelocityProvider();

    public Quaternionf getRotation();

    public float getInitialRoll();

    public boolean hasInfiniteBounds();

    public SpriteUVProvider getSpriteUVProvider();

    public static enum BlendMode {
        NORMAL,
        ADDITIVE;

    }
}

