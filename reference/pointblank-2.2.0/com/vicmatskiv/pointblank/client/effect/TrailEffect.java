/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 */
package com.vicmatskiv.pointblank.client.effect;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.effect.AbstractEffect;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.EffectRenderContext;
import com.vicmatskiv.pointblank.client.particle.EffectParticles;
import com.vicmatskiv.pointblank.client.uv.SpriteUVProvider;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.JsonUtil;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class TrailEffect
extends AbstractEffect {
    private Vec3 previousPosition;
    private Vec3 previousVelocity;
    private double longitudeOffset;

    @Override
    public boolean hasInfiniteBounds() {
        return false;
    }

    @Override
    public void render(EffectRenderContext effectRenderContext) {
        Vec3 topLeft;
        Vec3 bottomLeft;
        Vec3 cameraRightVector;
        TrailRenderContext trailRenderContext = (TrailRenderContext)effectRenderContext;
        float progress = effectRenderContext.getProgress();
        float partialTick = effectRenderContext.getPartialTick();
        Camera camera = effectRenderContext.getCamera();
        Vec3 cameraPos = camera.getPosition();
        Vector3f viewDirection3f = camera.getLookVector();
        Vec3 viewDirection = new Vec3((double)viewDirection3f.x, (double)viewDirection3f.y, (double)viewDirection3f.z);
        Vec3 cameraUpVector = new Vec3(0.0, 1.0, 0.0);
        Vec3 defaultVector = cameraRightVector = viewDirection.cross(cameraUpVector);
        Vec3 position = effectRenderContext.getPosition();
        Vec3 velocity = effectRenderContext.getVelocity();
        Vec3 r2 = viewDirection.cross(velocity).normalize();
        Vec3 up2 = r2;
        if (up2.lengthSqr() < 1.0E-5) {
            up2 = defaultVector;
        }
        float halfWidth = this.widthProvider.getValue(progress) * 0.5f;
        if (trailRenderContext.prevVelocity != null && trailRenderContext.prevPos != null) {
            Vec3 prevR2 = viewDirection.cross(trailRenderContext.prevVelocity).normalize();
            Vec3 prevUp2 = prevR2;
            if (prevUp2.lengthSqr() < 1.0E-5) {
                prevUp2 = defaultVector;
            }
            bottomLeft = this.getVerticePos(trailRenderContext.prevPos, trailRenderContext.prevVelocity, prevUp2, 1.0f, -halfWidth);
            topLeft = this.getVerticePos(trailRenderContext.prevPos, trailRenderContext.prevVelocity, prevUp2, 1.0f, halfWidth);
        } else {
            bottomLeft = this.getVerticePos(position, velocity, up2, 0.0f, -halfWidth);
            topLeft = this.getVerticePos(position, velocity, up2, 0.0f, halfWidth);
        }
        Vec3 bottomRight = this.getVerticePos(position, velocity, up2, partialTick, -halfWidth);
        Vec3 topRight = this.getVerticePos(position, velocity, up2, partialTick, halfWidth);
        bottomLeft = bottomLeft.subtract(cameraPos);
        bottomRight = bottomRight.subtract(cameraPos);
        topLeft = topLeft.subtract(cameraPos);
        topRight = topRight.subtract(cameraPos);
        SpriteUVProvider spriteUVProvider = effectRenderContext.getSpriteUVProvider();
        float[] uv = spriteUVProvider.getSpriteUV(progress);
        float u0 = uv[0];
        float v0 = uv[1];
        float u1 = uv[2];
        float v1 = uv[3];
        int lightColor = effectRenderContext.getLightColor();
        float alpha = this.alphaProvider.getValue(progress);
        float rCol = 1.0f;
        float gCol = 1.0f;
        float bCol = 1.0f;
        VertexConsumer vertexConsumer = effectRenderContext.getVertexBuffer();
        vertexConsumer.addVertex((float)topRight.x, (float)topRight.y, (float)topRight.z).setUv(u1, v1).setColor(rCol, gCol, bCol, alpha).setLight(lightColor);
        vertexConsumer.addVertex((float)bottomRight.x, (float)bottomRight.y, (float)bottomRight.z).setUv(u1, v0).setColor(rCol, gCol, bCol, alpha).setLight(lightColor);
        vertexConsumer.addVertex((float)bottomLeft.x, (float)bottomLeft.y, (float)bottomLeft.z).setUv(u0, v0).setColor(rCol, gCol, bCol, alpha).setLight(lightColor);
        vertexConsumer.addVertex((float)topLeft.x, (float)topLeft.y, (float)topLeft.z).setUv(u0, v1).setColor(rCol, gCol, bCol, alpha).setLight(lightColor);
    }

    private Vec3 getVerticePos(Vec3 position, Vec3 velocity, Vec3 side, float segmentProgress, float sideOffset) {
        Vec3 vertixPos = position.add(velocity.scale((double)segmentProgress)).add(side.scale((double)sideOffset));
        return vertixPos;
    }

    @Override
    public void launch(Entity player) {
        Minecraft mc = Minecraft.getInstance();
        EffectParticles.EffectParticle particle = new EffectParticles.EffectParticle(player, this);
        mc.particleEngine.add((Particle)particle);
    }

    public void launchNext(Entity owner, Vec3 position, Vec3 velocity) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 positionWithOffset = position.subtract(velocity.normalize().scale(this.longitudeOffset));
        if (this.previousPosition != null) {
            TrailParticle particle = new TrailParticle(owner, this, positionWithOffset, velocity, this.previousPosition, this.previousVelocity);
            mc.particleEngine.add((Particle)particle);
        }
        this.previousVelocity = velocity;
        this.previousPosition = positionWithOffset;
    }

    private static class TrailRenderContext
    extends EffectRenderContext {
        private Vec3 prevPos;
        private Vec3 prevVelocity;

        private TrailRenderContext() {
        }
    }

    private static class TrailParticle
    extends EffectParticles.EffectParticle {
        private Vec3 position;
        private Vec3 velocity;
        private Vec3 prevVelocity;
        private Vec3 prevPos;
        private float segmentProgress;

        TrailParticle(ClientLevel level, double x, double y, double z) {
            super(level, x, y, z);
        }

        public TrailParticle(Entity owner, TrailEffect effect, Vec3 position, Vec3 velocity, Vec3 prevPos, Vec3 prevVelocity) {
            super(owner, effect);
            this.setPos(position.x, position.y, position.z);
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            this.position = position;
            this.velocity = velocity;
            this.prevPos = prevPos;
            this.prevVelocity = prevVelocity;
        }

        @Override
        public boolean shouldCull() {
            return false;
        }

        @Override
        public void render(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
            int lightColor = this.getLightColor(partialTick);
            float progress = this.getProgress(partialTick);
            this.segmentProgress = this.segmentProgress < partialTick ? partialTick : 1.0f;
            TrailRenderContext effectRenderContext = (TrailRenderContext)new TrailRenderContext().withCamera(camera).withPosition(this.position).withVelocity(this.velocity).withVertexBuffer(vertexConsumer).withProgress(progress).withPartialTick(this.segmentProgress).withLightColor(lightColor).withSpriteUVProvider(this.spriteUVProvider);
            effectRenderContext.prevPos = this.prevPos;
            effectRenderContext.prevVelocity = this.prevVelocity;
            this.effect.render(effectRenderContext);
        }
    }

    public static class Builder
    extends AbstractEffect.AbstractEffectBuilder<Builder, TrailEffect> {
        private Collection<GunItem.FirePhase> compatiblePhases = Collections.singletonList(GunItem.FirePhase.FLYING);
        private double longitudeOffset;

        public Builder withLongitudeOffset(double longitudeOffset) {
            this.longitudeOffset = longitudeOffset;
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            super.withJsonObject(obj);
            this.withLongitudeOffset(JsonUtil.getJsonFloat(obj, "longitudeOffset", 0.0f));
            return this;
        }

        @Override
        public Collection<GunItem.FirePhase> getCompatiblePhases() {
            return this.compatiblePhases;
        }

        @Override
        public boolean isEffectAttached() {
            return false;
        }

        @Override
        public TrailEffect build(EffectBuilder.Context effectContext) {
            TrailEffect effect = new TrailEffect();
            this.apply(effect, effectContext);
            effect.longitudeOffset = this.longitudeOffset;
            return effect;
        }
    }
}

