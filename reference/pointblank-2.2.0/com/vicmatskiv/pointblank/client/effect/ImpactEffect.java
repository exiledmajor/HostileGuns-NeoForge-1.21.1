/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package com.vicmatskiv.pointblank.client.effect;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.effect.AbstractEffect;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.EffectRenderContext;
import com.vicmatskiv.pointblank.client.particle.EffectParticles;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.Interpolators;
import com.vicmatskiv.pointblank.util.ParticleValueProviders;
import java.util.Collection;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class ImpactEffect
extends AbstractEffect {
    private float textureMinU = 0.0f;
    private float textureMaxU = 1.0f;
    private int count;

    private ImpactEffect() {
    }

    @Override
    public boolean hasInfiniteBounds() {
        return false;
    }

    @Override
    public void render(EffectRenderContext effectRenderContext) {
        Vec3 pos = effectRenderContext.getPosition();
        if (pos == null) {
            throw new IllegalArgumentException("Incorrect render context, missing position");
        }
        Camera camera = effectRenderContext.getCamera();
        if (camera == null) {
            throw new IllegalArgumentException("Incorrect render context, missing camera");
        }
        float progress = effectRenderContext.getProgress();
        if (progress < 0.0f) {
            return;
        }
        float[] uv = effectRenderContext.getSpriteUVProvider().getSpriteUV(progress);
        if (uv == null) {
            return;
        }
        VertexConsumer vertexConsumer = effectRenderContext.getVertexBuffer();
        int lightColor = this.isGlowEnabled ? 240 : effectRenderContext.getLightColor();
        float size = this.widthProvider.getValue(progress);
        float alpha = this.alphaProvider.getValue(progress);
        float spriteWidth = uv[2] - uv[0];
        float minU = uv[0] + spriteWidth * this.textureMinU;
        float minV = uv[1];
        float maxU = uv[0] + spriteWidth * this.textureMaxU;
        float maxV = uv[3];
        float u0 = minU;
        float u1 = maxU;
        float v0 = minV;
        float v1 = maxV;
        Vec3 cameraPosition = camera.getPosition();
        float dx = (float)(pos.x - cameraPosition.x());
        float dy = (float)(pos.y - cameraPosition.y());
        float dz = (float)(pos.z - cameraPosition.z());
        Quaternionf rotation = new Quaternionf();
        float roll = -(effectRenderContext.getInitialAngle() + this.numRotations * 360.0f * progress);
        if (effectRenderContext.getRotation() != null) {
            rotation.mul((Quaternionfc)effectRenderContext.getRotation());
            rotation.rotateZ((float)Math.PI / 180 * roll);
        } else {
            rotation.mul((Quaternionfc)camera.rotation());
            rotation.rotateZ((float)Math.PI / 180 * roll);
        }
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0f, -1.0f, 0.0f), new Vector3f(-1.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f, -1.0f, 0.0f)};
        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate((Quaternionfc)rotation);
            vector3f.mul(size);
            vector3f.add(dx, dy, dz);
        }
        float rCol = 1.0f;
        float gCol = 1.0f;
        float bCol = 1.0f;
        for (int k = 0; k < this.brightness; ++k) {
            vertexConsumer.addVertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).setUv(u1, v1).setColor(rCol, gCol, bCol, alpha).setLight(lightColor);
            vertexConsumer.addVertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).setUv(u1, v0).setColor(rCol, gCol, bCol, alpha).setLight(lightColor);
            vertexConsumer.addVertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).setUv(u0, v0).setColor(rCol, gCol, bCol, alpha).setLight(lightColor);
            vertexConsumer.addVertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).setUv(u0, v1).setColor(rCol, gCol, bCol, alpha).setLight(lightColor);
        }
    }

    @Override
    public void launch(Entity player) {
        Minecraft mc = Minecraft.getInstance();
        for (int i = 0; i < this.count; ++i) {
            EffectParticles.EffectParticle particle = new EffectParticles.EffectParticle(player, this);
            mc.particleEngine.add((Particle)particle);
        }
    }

    public static class Builder
    extends AbstractEffect.AbstractEffectBuilder<Builder, ImpactEffect> {
        private static final Set<GunItem.FirePhase> COMPATIBLE_PHASES = Set.of(GunItem.FirePhase.HIT_TARGET);
        private ParticleValueProviders.ParticleCountProvider countProvider = new ParticleValueProviders.ConstantParticleCountProvider(1);
        private ParticleValueProviders.ParticleWidthProvider impactWidthProvider;

        public Builder withCount(int count) {
            this.countProvider = new ParticleValueProviders.ConstantParticleCountProvider(count);
            return this;
        }

        public Builder withCount(ParticleValueProviders.ParticleCountProvider countProvider) {
            this.countProvider = countProvider;
            return this;
        }

        public Builder withWidth(ParticleValueProviders.ParticleWidthProvider widthProvider) {
            this.impactWidthProvider = widthProvider;
            return this;
        }

        @Override
        public boolean isEffectAttached() {
            return false;
        }

        @Override
        public ImpactEffect build(EffectBuilder.Context effectContext) {
            ImpactEffect effect = new ImpactEffect();
            super.apply(effect, effectContext);
            effect.count = this.countProvider.getCount(effectContext);
            if (this.impactWidthProvider != null) {
                float width = this.impactWidthProvider.getWidth(effectContext);
                effect.widthProvider = new Interpolators.ConstantFloatProvider(width);
            }
            return effect;
        }

        @Override
        public Collection<GunItem.FirePhase> getCompatiblePhases() {
            return COMPATIBLE_PHASES;
        }
    }
}

