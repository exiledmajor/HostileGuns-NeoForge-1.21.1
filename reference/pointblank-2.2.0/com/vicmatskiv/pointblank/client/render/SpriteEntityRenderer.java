/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$EmptyTextureStateShard
 *  net.minecraft.client.renderer.RenderStateShard$TextureStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 */
package com.vicmatskiv.pointblank.client.render;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vicmatskiv.pointblank.client.EntityRendererBuilder;
import com.vicmatskiv.pointblank.client.effect.AbstractEffect;
import com.vicmatskiv.pointblank.client.effect.Effect;
import com.vicmatskiv.pointblank.client.uv.LoopingSpriteUVProvider;
import com.vicmatskiv.pointblank.client.uv.PlayOnceSpriteUVProvider;
import com.vicmatskiv.pointblank.client.uv.RandomSpriteUVProvider;
import com.vicmatskiv.pointblank.client.uv.SpriteUVProvider;
import com.vicmatskiv.pointblank.client.uv.StaticSpriteUVProvider;
import com.vicmatskiv.pointblank.entity.ProjectileLike;
import java.util.Random;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class SpriteEntityRenderer<T extends Entity>
extends EntityRenderer<T> {
    private ResourceLocation texture;
    private RenderType renderType;
    private SpriteUVProvider spriteUVProvider;
    private float initialRoll;
    private boolean isGlowEnabled;
    private int brightness;
    private float rotationsPerSecond;
    private float width;
    private float height;

    public SpriteEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(T projectile, float p_114657_, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int lightColor) {
        float progress = (float)((ProjectileLike)projectile).getElapsedTimeMillis() / 1000.0f;
        poseStack.pushPose();
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        poseStack.mulPose(camera.rotation());
        float[] uv = this.spriteUVProvider.getSpriteUV(progress);
        if (uv == null) {
            return;
        }
        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.renderType);
        if (this.isGlowEnabled) {
            lightColor = 240;
        }
        float alpha = 1.0f;
        float spriteWidth = uv[2] - uv[0];
        float textureMinU = 0.0f;
        float textureMaxU = 1.0f;
        float minU = uv[0] + spriteWidth * textureMinU;
        float minV = uv[1];
        float maxU = uv[0] + spriteWidth * textureMaxU;
        float maxV = uv[3];
        Quaternionf rotation = new Quaternionf();
        float roll = this.initialRoll + this.rotationsPerSecond * 360.0f * progress;
        rotation.rotateZ((float)Math.PI / 180 * roll);
        poseStack.mulPose(rotation);
        Matrix4f matrix4f = poseStack.last().pose();
        float halfWidth = (this.width > 0.0f ? this.width : projectile.getBbWidth()) * 0.5f;
        float halfHeight = (this.height > 0.0f ? this.height : projectile.getBbHeight()) * 0.5f;
        for (int k = 0; k < this.brightness; ++k) {
            vertexConsumer.addVertex(matrix4f, -halfWidth, halfHeight, 0.0f).setColor(1.0f, 1.0f, 1.0f, alpha).setUv(maxU, minV).setLight(lightColor);
            vertexConsumer.addVertex(matrix4f, halfWidth, halfHeight, 0.0f).setColor(1.0f, 1.0f, 1.0f, alpha).setUv(minU, minV).setLight(lightColor);
            vertexConsumer.addVertex(matrix4f, halfWidth, -halfHeight, 0.0f).setColor(1.0f, 1.0f, 1.0f, alpha).setUv(minU, maxV).setLight(lightColor);
            vertexConsumer.addVertex(matrix4f, -halfWidth, -halfHeight, 0.0f).setColor(1.0f, 1.0f, 1.0f, (float)this.brightness).setUv(maxU, maxV).setLight(lightColor);
        }
        poseStack.popPose();
    }

    public ResourceLocation getTextureLocation(Entity entity) {
        return this.texture;
    }

    public static class Builder<T extends Entity>
    implements EntityRendererBuilder<Builder<T>, T, EntityRenderer<T>> {
        private static long counter;
        private static final SpriteUVProvider DEFAULT_SPRITE_UV_PROVIDER;
        private static final String DEFAULT_NAME_PREFIX;
        private static Effect.BlendMode DEFAULT_BLEND_MODE;
        private static final int DEFAULT_BRIGHTNESS = 1;
        private static final float DEFAULT_NUM_ROTATIONS = 0.0f;
        private ResourceLocation texture;
        private Effect.BlendMode blendMode;
        private boolean isDepthTestEnabled;
        private boolean isGlowEnabled;
        private int brightness;
        private AbstractEffect.SpriteInfo spriteInfo;
        private float numRotations;
        private float width;
        private float height;

        public Builder() {
            ++counter;
            this.blendMode = DEFAULT_BLEND_MODE;
            this.brightness = 1;
            this.numRotations = 0.0f;
            this.width = Float.NEGATIVE_INFINITY;
            this.height = Float.NEGATIVE_INFINITY;
        }

        public Builder withSize(float width, float hight) {
            this.width = width;
            return this;
        }

        public Builder withSize(float size) {
            this.width = size;
            this.height = size;
            return this;
        }

        public Builder withTexture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public Builder withTexture(String textureName) {
            this.texture = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)textureName);
            return this;
        }

        public Builder withBlendMode(Effect.BlendMode blendMode) {
            this.blendMode = blendMode;
            return this;
        }

        public Builder withDepthTest(boolean isDepthTestEnabled) {
            this.isDepthTestEnabled = isDepthTestEnabled;
            return this;
        }

        public Builder withGlow(boolean isGlowEnabled) {
            this.isGlowEnabled = isGlowEnabled;
            return this;
        }

        public Builder withBrightness(int brightness) {
            this.brightness = brightness;
            return this;
        }

        public Builder withSprites(int rows, int columns, int spritesPerSecond, AbstractEffect.SpriteAnimationType type) {
            this.spriteInfo = new AbstractEffect.SpriteInfo(rows, columns, spritesPerSecond, type);
            return this;
        }

        public Builder withRotations(double numRotations) {
            this.numRotations = (float)numRotations;
            return this;
        }

        @Override
        public Builder<T> withJsonObject(JsonObject obj) {
            return null;
        }

        @Override
        public EntityRenderer<T> build(EntityRendererProvider.Context context) {
            SpriteEntityRenderer renderer = new SpriteEntityRenderer(context);
            renderer.width = this.width;
            renderer.height = this.height;
            renderer.initialRoll = new Random().nextFloat() * 360.0f;
            renderer.isGlowEnabled = this.isGlowEnabled;
            renderer.brightness = this.brightness;
            renderer.rotationsPerSecond = this.numRotations;
            renderer.texture = this.texture;
            String renderTypeName = DEFAULT_NAME_PREFIX + "." + counter;
            renderer.renderType = EffectRenderType.createRenderType(renderTypeName, this.texture, this.blendMode, this.isDepthTestEnabled);
            if (this.spriteInfo != null) {
                switch (this.spriteInfo.type()) {
                    case STATIC: {
                        renderer.spriteUVProvider = StaticSpriteUVProvider.INSTANCE;
                        break;
                    }
                    case LOOP: {
                        LoopingSpriteUVProvider spriteUVProvider = new LoopingSpriteUVProvider(this.spriteInfo.rows(), this.spriteInfo.columns(), this.spriteInfo.spritesPerSecond(), 1000L);
                        renderer.spriteUVProvider = spriteUVProvider;
                        break;
                    }
                    case RANDOM: {
                        renderer.spriteUVProvider = new RandomSpriteUVProvider(this.spriteInfo.rows(), this.spriteInfo.columns(), this.spriteInfo.spritesPerSecond(), 1000L);
                        break;
                    }
                    case PLAY_ONCE: {
                        PlayOnceSpriteUVProvider spriteUVProvider = new PlayOnceSpriteUVProvider(this.spriteInfo.rows(), this.spriteInfo.columns(), this.spriteInfo.spritesPerSecond(), 1000L);
                        renderer.spriteUVProvider = spriteUVProvider;
                        break;
                    }
                }
            } else {
                renderer.spriteUVProvider = DEFAULT_SPRITE_UV_PROVIDER;
            }
            return renderer;
        }

        static {
            DEFAULT_SPRITE_UV_PROVIDER = StaticSpriteUVProvider.INSTANCE;
            DEFAULT_NAME_PREFIX = "pointblank:" + SpriteEntityRenderer.class.getSimpleName();
            DEFAULT_BLEND_MODE = Effect.BlendMode.NORMAL;
        }
    }

    private static final class EffectRenderType
    extends RenderType {
        private EffectRenderType(String renderTypeName, VertexFormat vertexFormat, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable runnable) {
            super(renderTypeName, vertexFormat, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, runnable);
        }

        private static RenderType createRenderType(String name, ResourceLocation texture, Effect.BlendMode blendMode, boolean isDepthTestEnabled) {
            return RenderType.create((String)name, (VertexFormat)DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setTransparencyState(switch (blendMode) {
                case Effect.BlendMode.NORMAL -> TRANSLUCENT_TRANSPARENCY;
                default -> LIGHTNING_TRANSPARENCY;
            }).setDepthTestState(isDepthTestEnabled ? LEQUAL_DEPTH_TEST : NO_DEPTH_TEST).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(texture, false, false)).setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER).createCompositeState(false));
        }
    }
}

