/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionf
 */
package com.vicmatskiv.pointblank.client.effect;

import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.client.effect.Effect;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.uv.LoopingSpriteUVProvider;
import com.vicmatskiv.pointblank.client.uv.PlayOnceSpriteUVProvider;
import com.vicmatskiv.pointblank.client.uv.RandomSpriteUVProvider;
import com.vicmatskiv.pointblank.client.uv.SpriteUVProvider;
import com.vicmatskiv.pointblank.client.uv.StaticSpriteUVProvider;
import com.vicmatskiv.pointblank.util.Interpolators;
import com.vicmatskiv.pointblank.util.JsonUtil;
import com.vicmatskiv.pointblank.util.TimeUnit;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public abstract class AbstractEffect
implements Effect {
    protected String name;
    protected ResourceLocation texture;
    protected long lifetimeNanos;
    protected long duration;
    protected long delay;
    protected float initialRoll;
    protected Effect.BlendMode blendMode;
    protected boolean isGlowEnabled;
    protected boolean isDepthTestEnabled;
    protected Supplier<SpriteUVProvider> spriteUVProviderSupplier;
    protected Interpolators.FloatInterpolator widthProvider;
    protected Interpolators.FloatInterpolator alphaProvider;
    protected int brightness;
    protected float gravity;
    protected float friction;
    protected boolean hasPhysics;
    protected Supplier<Vec3> velocityProvider;
    protected Supplier<Vec3> startPositionProvider;
    protected Quaternionf rotation;
    protected float numRotations;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ResourceLocation getTexture() {
        return this.texture;
    }

    @Override
    public Effect.BlendMode getBlendMode() {
        return this.blendMode;
    }

    @Override
    public long getDuration() {
        return this.duration;
    }

    @Override
    public long getDelay() {
        return this.delay;
    }

    @Override
    public boolean isDepthTestEnabled() {
        return this.isDepthTestEnabled;
    }

    @Override
    public boolean isGlowEnabled() {
        return this.isGlowEnabled;
    }

    @Override
    public int getBrightness() {
        return this.brightness;
    }

    @Override
    public float getGravity() {
        return this.gravity;
    }

    @Override
    public float getFriction() {
        return this.friction;
    }

    @Override
    public boolean hasPhysics() {
        return this.hasPhysics;
    }

    @Override
    public Supplier<Vec3> getStartPositionProvider() {
        return this.startPositionProvider;
    }

    @Override
    public Supplier<Vec3> getVelocityProvider() {
        return this.velocityProvider;
    }

    @Override
    public Quaternionf getRotation() {
        return this.rotation;
    }

    @Override
    public float getInitialRoll() {
        return this.initialRoll;
    }

    @Override
    public SpriteUVProvider getSpriteUVProvider() {
        return this.spriteUVProviderSupplier.get();
    }

    public static abstract class AbstractEffectBuilder<T extends AbstractEffectBuilder<T, E>, E extends AbstractEffect>
    implements EffectBuilder<T, E> {
        private static long counter;
        public static final SpriteUVProvider DEFAULT_SPRITE_UV_PROVIDER;
        private static final String DEFAULT_NAME_PREFIX = "pointblank:effect";
        private static Effect.BlendMode DEFAULT_BLEND_MODE;
        private static final Interpolators.FloatInterpolator DEFAULT_WIDTH_PROVIDER;
        private static final Interpolators.FloatInterpolator DEFAULT_ALPHA_PROVIDER;
        private static final Interpolators.FloatProvider DEFAULT_INITIAL_ROLL_PROVIDER;
        private static final int DEFAULT_BRIGHTNESS = 1;
        private static final float DEFAULT_NUM_ROTATIONS = 0.0f;
        protected String name;
        protected ResourceLocation texture;
        protected long duration;
        protected long delay;
        protected Effect.BlendMode blendMode;
        protected boolean isDepthTestEnabled;
        protected long lifetimeNanos;
        protected boolean isGlowEnabled;
        protected int brightness = 1;
        private SpriteInfo spriteInfo;
        protected Interpolators.FloatInterpolator widthProvider;
        protected Interpolators.FloatInterpolator alphaProvider;
        protected Interpolators.FloatProvider initialRollProvider = DEFAULT_INITIAL_ROLL_PROVIDER;
        protected float gravity = 0.0f;
        protected float friction = 0.96f;
        protected boolean hasPhysics = true;
        protected Supplier<Vec3> velocityProvider;
        protected Supplier<Vec3> startPositionProvider;
        private float numRotations = 0.0f;

        protected T cast(AbstractEffectBuilder<T, E> _this) {
            return (T)_this;
        }

        public AbstractEffectBuilder() {
            this.name = DEFAULT_NAME_PREFIX + ++counter;
            this.blendMode = DEFAULT_BLEND_MODE;
            this.widthProvider = DEFAULT_WIDTH_PROVIDER;
            this.alphaProvider = DEFAULT_ALPHA_PROVIDER;
        }

        public T withName(String name) {
            this.name = name;
            return this.cast(this);
        }

        public T withTexture(ResourceLocation texture) {
            this.texture = texture;
            return this.cast(this);
        }

        public T withTexture(String textureName) {
            this.texture = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)textureName);
            return this.cast(this);
        }

        public T withBlendMode(Effect.BlendMode blendMode) {
            this.blendMode = blendMode;
            return this.cast(this);
        }

        public T withDepthTest(boolean isDepthTestEnabled) {
            this.isDepthTestEnabled = isDepthTestEnabled;
            return this.cast(this);
        }

        public T withDuration(long duration) {
            this.duration = duration;
            return this.cast(this);
        }

        public T withDelay(long delay) {
            this.delay = delay;
            return this.cast(this);
        }

        public T withGlow(boolean isGlowEnabled) {
            this.isGlowEnabled = isGlowEnabled;
            return this.cast(this);
        }

        public T withBrightness(int brightness) {
            this.brightness = brightness;
            return this.cast(this);
        }

        public T withSprites(int rows, int columns, int spritesPerSecond, SpriteAnimationType type) {
            this.spriteInfo = new SpriteInfo(rows, columns, spritesPerSecond, type);
            return this.cast(this);
        }

        public T withAlphaProvider(Interpolators.FloatInterpolator alphaProvider) {
            this.alphaProvider = alphaProvider;
            return this.cast(this);
        }

        public T withWidthProvider(Interpolators.FloatInterpolator widthProvider) {
            this.widthProvider = widthProvider;
            return this.cast(this);
        }

        public T withFriction(float friction) {
            this.friction = friction;
            return this.cast(this);
        }

        public T withGravity(float gravity) {
            this.gravity = gravity;
            return this.cast(this);
        }

        public T withPhysics(boolean hasPhysics) {
            this.hasPhysics = hasPhysics;
            return this.cast(this);
        }

        public T withVelocityProvider(Supplier<Vec3> velocityProvider) {
            this.velocityProvider = velocityProvider;
            return this.cast(this);
        }

        public T withRotations(double numRotations) {
            this.numRotations = (float)numRotations;
            return this.cast(this);
        }

        public T withInitialRollProvider(Interpolators.FloatProvider initialRollProvider) {
            this.initialRollProvider = initialRollProvider;
            return this.cast(this);
        }

        @Override
        public T withJsonObject(JsonObject obj) {
            JsonObject spritesObj;
            Interpolators.FloatInterpolator widthInt;
            this.withName(JsonUtil.getJsonString(obj, "name"));
            this.withDuration(JsonUtil.getJsonInt(obj, "duration", 1000));
            this.withDelay(JsonUtil.getJsonInt(obj, "delay", 0));
            this.withTexture(obj.getAsJsonPrimitive("texture").getAsString());
            this.withBlendMode(JsonUtil.getEnum(obj, "blendMode", Effect.BlendMode.class, DEFAULT_BLEND_MODE, true));
            this.withDepthTest(JsonUtil.getJsonBoolean(obj, "depthTest", true));
            this.withGlow(JsonUtil.getJsonBoolean(obj, "glow", false));
            this.withBrightness(JsonUtil.getJsonInt(obj, "brightness", 1));
            this.withRotations(JsonUtil.getJsonFloat(obj, "numRotations", 0.0f));
            this.withInitialRollProvider(JsonUtil.getJsonFloatProvider(obj, "initialRoll", DEFAULT_INITIAL_ROLL_PROVIDER));
            Interpolators.FloatInterpolator alphaInt = JsonUtil.getJsonInterpolator(obj, "alpha");
            if (alphaInt != null) {
                this.withAlphaProvider(alphaInt);
            }
            if ((widthInt = JsonUtil.getJsonInterpolator(obj, "width")) != null) {
                this.withWidthProvider(widthInt);
            }
            if ((spritesObj = obj.getAsJsonObject("sprites")) != null) {
                int rows = JsonUtil.getJsonInt(spritesObj, "rows", 1);
                int columns = JsonUtil.getJsonInt(spritesObj, "columns", 1);
                int fps = JsonUtil.getJsonInt(spritesObj, "fps", 60);
                SpriteAnimationType spriteAnimationType = JsonUtil.getEnum(spritesObj, "type", SpriteAnimationType.class, SpriteAnimationType.LOOP, true);
                this.withSprites(rows, columns, fps, spriteAnimationType);
            }
            return this.cast(this);
        }

        public E apply(E effect, EffectBuilder.Context context) {
            ((AbstractEffect)effect).name = this.name;
            ((AbstractEffect)effect).texture = this.texture;
            ((AbstractEffect)effect).blendMode = this.blendMode;
            ((AbstractEffect)effect).isGlowEnabled = this.isGlowEnabled;
            ((AbstractEffect)effect).brightness = this.brightness;
            ((AbstractEffect)effect).isDepthTestEnabled = this.isDepthTestEnabled;
            ((AbstractEffect)effect).lifetimeNanos = TimeUnit.MILLISECOND.toNanos(this.duration);
            ((AbstractEffect)effect).widthProvider = this.widthProvider;
            ((AbstractEffect)effect).alphaProvider = this.alphaProvider;
            ((AbstractEffect)effect).delay = this.delay;
            ((AbstractEffect)effect).duration = this.duration;
            ((AbstractEffect)effect).numRotations = this.numRotations;
            if (context.getStartPosition() != null) {
                ((AbstractEffect)effect).startPositionProvider = () -> context.getStartPosition();
            }
            if (((AbstractEffect)effect).startPositionProvider == null) {
                ((AbstractEffect)effect).startPositionProvider = this.startPositionProvider;
            }
            if (((AbstractEffect)effect).startPositionProvider == null) {
                ((AbstractEffect)effect).startPositionProvider = () -> Vec3.ZERO;
            }
            if (context.getVelocity() != null) {
                ((AbstractEffect)effect).velocityProvider = () -> context.getVelocity();
            }
            if (((AbstractEffect)effect).velocityProvider == null) {
                ((AbstractEffect)effect).velocityProvider = this.velocityProvider;
            }
            if (((AbstractEffect)effect).velocityProvider == null) {
                ((AbstractEffect)effect).velocityProvider = () -> Vec3.ZERO;
            }
            ((AbstractEffect)effect).initialRoll = this.initialRollProvider.getValue();
            ((AbstractEffect)effect).rotation = context.getRotation();
            ((AbstractEffect)effect).friction = this.friction;
            ((AbstractEffect)effect).gravity = this.gravity;
            ((AbstractEffect)effect).hasPhysics = this.hasPhysics;
            if (this.spriteInfo != null) {
                switch (this.spriteInfo.type().ordinal()) {
                    case 0: {
                        ((AbstractEffect)effect).spriteUVProviderSupplier = () -> StaticSpriteUVProvider.INSTANCE;
                        break;
                    }
                    case 2: {
                        LoopingSpriteUVProvider spriteUVProvider = new LoopingSpriteUVProvider(this.spriteInfo.rows(), this.spriteInfo.columns(), this.spriteInfo.spritesPerSecond(), this.duration);
                        ((AbstractEffect)effect).spriteUVProviderSupplier = () -> spriteUVProvider;
                        break;
                    }
                    case 1: {
                        ((AbstractEffect)effect).spriteUVProviderSupplier = () -> new RandomSpriteUVProvider(this.spriteInfo.rows(), this.spriteInfo.columns(), this.spriteInfo.spritesPerSecond(), this.duration);
                        break;
                    }
                    case 3: {
                        PlayOnceSpriteUVProvider spriteUVProvider = new PlayOnceSpriteUVProvider(this.spriteInfo.rows(), this.spriteInfo.columns(), this.spriteInfo.spritesPerSecond(), this.duration);
                        ((AbstractEffect)effect).spriteUVProviderSupplier = () -> spriteUVProvider;
                        break;
                    }
                }
            } else {
                ((AbstractEffect)effect).spriteUVProviderSupplier = () -> DEFAULT_SPRITE_UV_PROVIDER;
            }
            return effect;
        }

        @Override
        public String getName() {
            return this.name;
        }

        public ResourceLocation getTexture() {
            return this.texture;
        }

        public boolean isDepthTestEnabled() {
            return this.isDepthTestEnabled;
        }

        public boolean isGlowEnabled() {
            return this.isGlowEnabled;
        }

        public Effect.BlendMode getBlendMode() {
            return this.blendMode;
        }

        static {
            DEFAULT_SPRITE_UV_PROVIDER = StaticSpriteUVProvider.INSTANCE;
            DEFAULT_BLEND_MODE = Effect.BlendMode.NORMAL;
            DEFAULT_WIDTH_PROVIDER = new Interpolators.ConstantFloatProvider(1.0f);
            DEFAULT_ALPHA_PROVIDER = new Interpolators.ConstantFloatProvider(1.0f);
            DEFAULT_INITIAL_ROLL_PROVIDER = new Interpolators.RandomFloatProvider(360.0f);
        }
    }

    public record SpriteInfo(int rows, int columns, int spritesPerSecond, SpriteAnimationType type) {
    }

    public static enum SpriteAnimationType {
        STATIC,
        RANDOM,
        LOOP,
        PLAY_ONCE;

    }
}

