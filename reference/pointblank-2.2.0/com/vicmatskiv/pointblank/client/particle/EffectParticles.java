/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DestFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SourceFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.Util
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleProvider
 *  net.minecraft.client.particle.ParticleRenderType
 *  net.minecraft.client.particle.SpriteSet
 *  net.minecraft.client.renderer.texture.TextureManager
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vicmatskiv.pointblank.client.effect.Effect;
import com.vicmatskiv.pointblank.client.effect.EffectRenderContext;
import com.vicmatskiv.pointblank.client.uv.SpriteUVProvider;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.TimeUnit;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EffectParticles {
    private static final Logger LOGGER = LogManager.getLogger((String)"pointblank");
    private static Function<EffectRenderKey, ParticleRenderType> effectRenderTypes = Util.memoize(key -> new EffectParticleRenderType(key.texture, key.blendMode, key.isDepthTestEnabled));

    public static class EffectParticleRenderType
    implements ParticleRenderType {
        private ResourceLocation texture;
        private Effect.BlendMode blendMode;
        private boolean isDepthTestEnabled;

        public EffectParticleRenderType(ResourceLocation texture, Effect.BlendMode blendMode, boolean isDepthTestEnabled) {
            this.texture = texture;
            this.blendMode = blendMode;
            this.isDepthTestEnabled = isDepthTestEnabled;
        }

        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.setShaderTexture((int)0, (ResourceLocation)this.texture);
            RenderSystem.enableBlend();
            if (this.isDepthTestEnabled) {
                RenderSystem.depthMask((boolean)true);
                RenderSystem.enableDepthTest();
            } else {
                RenderSystem.depthMask((boolean)false);
                RenderSystem.disableDepthTest();
            }
            RenderSystem.disableCull();
            switch (this.blendMode) {
                case ADDITIVE: {
                    RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE);
                    break;
                }
                default: {
                    RenderSystem.defaultBlendFunc();
                }
            }
            Minecraft mc = Minecraft.getInstance();
            mc.gameRenderer.lightTexture().turnOnLightLayer();
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }
    }

    private static class EffectRenderKey {
        ResourceLocation texture;
        Effect.BlendMode blendMode;
        boolean isDepthTestEnabled;

        EffectRenderKey(ResourceLocation texture, Effect.BlendMode blendMode, boolean isDepthTestEnabled) {
            this.texture = texture;
            this.blendMode = blendMode;
            this.isDepthTestEnabled = isDepthTestEnabled;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.blendMode, this.isDepthTestEnabled, this.texture});
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            EffectRenderKey other = (EffectRenderKey)obj;
            return this.blendMode == other.blendMode && this.isDepthTestEnabled == other.isDepthTestEnabled && Objects.equals(this.texture, other.texture);
        }
    }

    public static class EffectParticleProvider
    implements ParticleProvider<SimpleParticleType> {
        public EffectParticleProvider(SpriteSet spriteSet) {
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double posX, double posY, double posZ, double xd, double yd, double zd) {
            EffectParticle particle = new EffectParticle(level, posX, posY, posZ);
            return particle;
        }
    }

    public static class EffectParticle
    extends Particle {
        public static final AABB INFINITE_EXTENT_AABB = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        private ParticleRenderType renderType;
        private Entity owner;
        private float initialRoll;
        protected Effect effect;
        private boolean hasInfiniteBounds;
        protected SpriteUVProvider spriteUVProvider;
        private int delay;

        protected EffectParticle(ClientLevel level, double x, double y, double z) {
            super(level, x, y, z);
        }

        public EffectParticle(Entity owner, Effect effect) {
            super((ClientLevel)MiscUtil.getLevel(owner), 0.0, 0.0, 0.0);
            this.effect = effect;
            this.hasInfiniteBounds = effect.hasInfiniteBounds();
            Vec3 startPosition = effect.getStartPositionProvider().get();
            Random random = new Random();
            this.setPos(startPosition.x + (double)(random.nextFloat() * 0.01f), startPosition.y + (double)(random.nextFloat() * 0.01f), startPosition.z + (double)(random.nextFloat() * 0.01f));
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            this.xd *= (double)0.1f;
            this.yd *= (double)0.1f;
            this.zd *= (double)0.1f;
            Vec3 velocity = effect.getVelocityProvider().get();
            float c = 1.0f;
            this.xd += velocity.x * (double)c;
            this.yd += velocity.y * (double)c;
            this.zd += velocity.z * (double)c;
            this.hasPhysics = effect.hasPhysics();
            this.owner = owner;
            this.delay = (int)TimeUnit.MILLISECOND.toTicks(effect.getDelay());
            this.lifetime = (int)TimeUnit.MILLISECOND.toTicks(effect.getDuration()) + this.delay;
            this.renderType = effectRenderTypes.apply(new EffectRenderKey(effect.getTexture(), effect.getBlendMode(), effect.isDepthTestEnabled()));
            this.initialRoll = effect.getInitialRoll();
            this.friction = effect.getFriction();
            this.gravity = effect.getGravity();
            this.speedUpWhenYMotionIsBlocked = true;
            this.spriteUVProvider = effect.getSpriteUVProvider();
        }

        public boolean shouldCull() {
            return true;
        }

        protected float getProgress(float partialTick) {
            int adjustedAge = this.age - this.delay;
            float elapsedTimeTicks = (float)adjustedAge + partialTick;
            float progress = adjustedAge < 0 ? elapsedTimeTicks / (float)this.delay : elapsedTimeTicks / (float)(this.lifetime - this.delay);
            return Mth.clamp((float)progress, (float)-1.0f, (float)1.0f);
        }

        public void render(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
            try {
                if (camera.getEntity() != this.owner) {
                    // empty if block
                }
                int lightColor = this.getLightColor(partialTick);
                double posX = Mth.lerp((double)partialTick, (double)this.xo, (double)this.x);
                double posY = Mth.lerp((double)partialTick, (double)this.yo, (double)this.y);
                double posZ = Mth.lerp((double)partialTick, (double)this.zo, (double)this.z);
                float progress = this.getProgress(partialTick);
                EffectRenderContext effectRenderContext = new EffectRenderContext().withCamera(camera).withRotation(this.effect.getRotation()).withPosition(new Vec3(posX, posY, posZ)).withInitialAngle(this.initialRoll).withVertexBuffer(vertexConsumer).withProgress(progress).withLightColor(lightColor).withSpriteUVProvider(this.spriteUVProvider);
                this.effect.render(effectRenderContext);
            }
            catch (Exception e) {
                LOGGER.error("Failed to render effect particle: {}", (Throwable)e);
            }
        }

        public ParticleRenderType getRenderType() {
            return this.renderType;
        }

        public AABB getBoundingBox() {
            if (this.hasInfiniteBounds) {
                return INFINITE_EXTENT_AABB;
            }
            return super.getBoundingBox();
        }

        public void tick() {
            super.tick();
        }
    }
}

