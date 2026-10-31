/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.ConditionalFeature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.JsonUtil;
import java.util.Locale;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class ShellEjectionFeature
extends ConditionalFeature {
    public static final String BONE_NAME = "_shelleject_";
    public static final Vec3 DEFAULT_VELOCITY = new Vec3(1.1, 0.35, -0.15);
    public static final Vec3 DEFAULT_RANDOMIZE = new Vec3(0.15, 0.1, 0.08);
    public static final float DEFAULT_INHERIT_SHOOTER_VELOCITY = 1.0f;
    public static final float DEFAULT_VERTICAL_LIFT_SCALE = 1.35f;
    private final Trigger trigger;
    private final Vec3 velocity;
    private final Vec3 randomize;
    private final float inheritShooterVelocity;
    private final float verticalLiftScale;
    private final Visuals visuals;
    private final DropSound dropSound;

    private ShellEjectionFeature(FeatureProvider owner, Predicate<ConditionContext> condition, Trigger trigger, Vec3 velocity, Vec3 randomize, float inheritShooterVelocity, float verticalLiftScale, Visuals visuals, DropSound dropSound) {
        super(owner, condition);
        this.trigger = trigger;
        this.velocity = velocity;
        this.randomize = randomize;
        this.inheritShooterVelocity = inheritShooterVelocity;
        this.verticalLiftScale = verticalLiftScale;
        this.visuals = visuals;
        this.dropSound = dropSound;
    }

    public Trigger getTrigger() {
        return this.trigger;
    }

    public Vec3 getVelocity() {
        return this.velocity;
    }

    public Vec3 getRandomize() {
        return this.randomize;
    }

    public float getInheritShooterVelocity() {
        return this.inheritShooterVelocity;
    }

    public float getVerticalLiftScale() {
        return this.verticalLiftScale;
    }

    public Visuals getVisuals() {
        return this.visuals;
    }

    public DropSound getDropSound() {
        return this.dropSound;
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)path);
    }

    public static enum Trigger {
        FIRE,
        RELOAD,
        ANIMATION_EVENT;

    }

    public record Visuals(ResourceLocation model, ResourceLocation texture, Vec3 renderOffset) {
    }

    public record DropSound(ResourceLocation sound, float volume, float pitch, float randomPitch) {
    }

    public static class Builder
    implements FeatureBuilder<Builder, ShellEjectionFeature> {
        private Predicate<ConditionContext> condition = ctx -> true;
        private Trigger trigger = Trigger.FIRE;
        private Vec3 velocity = DEFAULT_VELOCITY;
        private Vec3 randomize = DEFAULT_RANDOMIZE;
        private float inheritShooterVelocity = 1.0f;
        private float verticalLiftScale = 1.35f;
        @Nullable
        private ResourceLocation model;
        @Nullable
        private ResourceLocation texture;
        @Nullable
        private Vec3 renderOffset;
        @Nullable
        private ResourceLocation dropSound;
        @Nullable
        private Float dropSoundVolume;
        @Nullable
        private Float dropSoundPitch;
        @Nullable
        private Float dropSoundRandomPitch;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withTrigger(Trigger trigger) {
            this.trigger = trigger;
            return this;
        }

        public Builder withVelocity(Vec3 velocity) {
            this.velocity = velocity;
            return this;
        }

        public Builder withRandomize(Vec3 randomize) {
            this.randomize = randomize;
            return this;
        }

        public Builder withInheritShooterVelocity(float inheritShooterVelocity) {
            this.inheritShooterVelocity = inheritShooterVelocity;
            return this;
        }

        public Builder withVerticalLiftScale(float verticalLiftScale) {
            this.verticalLiftScale = verticalLiftScale;
            return this;
        }

        public Builder withModel(ResourceLocation model) {
            this.model = model;
            return this;
        }

        public Builder withTexture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public Builder withRenderOffset(Vec3 renderOffset) {
            this.renderOffset = renderOffset;
            return this;
        }

        public Builder withDropSound(ResourceLocation dropSound) {
            this.dropSound = dropSound;
            return this;
        }

        public Builder withDropSoundVolume(float dropSoundVolume) {
            this.dropSoundVolume = Float.valueOf(dropSoundVolume);
            return this;
        }

        public Builder withDropSoundPitch(float dropSoundPitch) {
            this.dropSoundPitch = Float.valueOf(dropSoundPitch);
            return this;
        }

        public Builder withDropSoundRandomPitch(float dropSoundRandomPitch) {
            this.dropSoundRandomPitch = Float.valueOf(dropSoundRandomPitch);
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            String dropSoundName;
            String textureName;
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            this.withTrigger(JsonUtil.getEnum(obj, "trigger", Trigger.class, Trigger.FIRE, true));
            this.withVelocity(Builder.readVec3(obj, "velocity", DEFAULT_VELOCITY));
            this.withRandomize(Builder.readVec3(obj, "randomize", DEFAULT_RANDOMIZE));
            this.withInheritShooterVelocity(JsonUtil.getJsonFloat(obj, "inheritShooterVelocity", 1.0f));
            this.withVerticalLiftScale(JsonUtil.getJsonFloat(obj, "verticalLiftScale", 1.35f));
            String modelName = JsonUtil.getJsonString(obj, "model", null);
            if (modelName != null && !modelName.isBlank()) {
                this.withModel(Builder.parseResourceLocation(modelName));
            }
            if ((textureName = JsonUtil.getJsonString(obj, "texture", null)) != null && !textureName.isBlank()) {
                this.withTexture(Builder.parseResourceLocation(textureName));
            }
            if (obj.has("renderOffset")) {
                this.withRenderOffset(Builder.readVec3(obj, "renderOffset", Vec3.ZERO));
            }
            if ((dropSoundName = JsonUtil.getJsonString(obj, "dropSound", null)) != null && !dropSoundName.isBlank()) {
                this.withDropSound(Builder.parseResourceLocation(dropSoundName));
            }
            if (obj.has("dropSoundVolume")) {
                this.withDropSoundVolume(JsonUtil.getJsonFloat(obj, "dropSoundVolume"));
            }
            if (obj.has("dropSoundPitch")) {
                this.withDropSoundPitch(JsonUtil.getJsonFloat(obj, "dropSoundPitch"));
            }
            if (obj.has("dropSoundRandomPitch")) {
                this.withDropSoundRandomPitch(JsonUtil.getJsonFloat(obj, "dropSoundRandomPitch"));
            }
            return this;
        }

        @Override
        public ShellEjectionFeature build(FeatureProvider featureProvider) {
            ResourceLocation resolvedModel = this.model != null ? this.model : Preset.RIFLE.getDefaultVisuals().model();
            Visuals resolvedVisuals = new Visuals(resolvedModel, this.texture != null ? this.texture : Builder.deriveTextureFromModel(resolvedModel), this.renderOffset != null ? this.renderOffset : Vec3.ZERO);
            DropSound defaultDropSound = Builder.inferPresetFromModel(resolvedModel).getDefaultDropSound();
            DropSound resolvedDropSound = new DropSound(this.dropSound != null ? this.dropSound : defaultDropSound.sound(), this.dropSoundVolume != null ? this.dropSoundVolume.floatValue() : defaultDropSound.volume(), this.dropSoundPitch != null ? this.dropSoundPitch.floatValue() : defaultDropSound.pitch(), this.dropSoundRandomPitch != null ? this.dropSoundRandomPitch.floatValue() : defaultDropSound.randomPitch());
            return new ShellEjectionFeature(featureProvider, this.condition, this.trigger, this.velocity, this.randomize, this.inheritShooterVelocity, this.verticalLiftScale, resolvedVisuals, resolvedDropSound);
        }

        private static Vec3 readVec3(JsonObject obj, String property, Vec3 defaultValue) {
            JsonObject vector = obj.getAsJsonObject(property);
            if (vector == null) {
                return defaultValue;
            }
            return new Vec3(JsonUtil.getJsonDouble(vector, "x", defaultValue.x), JsonUtil.getJsonDouble(vector, "y", defaultValue.y), JsonUtil.getJsonDouble(vector, "z", defaultValue.z));
        }

        private static ResourceLocation parseResourceLocation(String value) {
            return value.indexOf(58) >= 0 ? ResourceLocation.parse((String)value) : ShellEjectionFeature.resource(value);
        }

        private static ResourceLocation deriveTextureFromModel(ResourceLocation modelResource) {
            return ResourceLocation.fromNamespaceAndPath((String)modelResource.getNamespace(), (String)("textures/" + modelResource.getPath() + ".png"));
        }

        private static Preset inferPresetFromModel(ResourceLocation modelResource) {
            String path = modelResource.getPath().toLowerCase(Locale.ROOT);
            if (path.contains("shotgun")) {
                return Preset.SHOTGUN;
            }
            if (path.contains("pistol")) {
                return Preset.PISTOL;
            }
            return Preset.RIFLE;
        }
    }

    public static enum Preset {
        PISTOL("shell_casing/pistol", new Visuals(ShellEjectionFeature.resource("entity/shell_casing/pistol"), ShellEjectionFeature.resource("textures/entity/shell_casing/pistol.png"), Vec3.ZERO), new DropSound(ShellEjectionFeature.resource("shell"), 0.55f, 1.18f, 0.08f)),
        RIFLE("shell_casing/rifle", new Visuals(ShellEjectionFeature.resource("entity/shell_casing/rifle"), ShellEjectionFeature.resource("textures/entity/shell_casing/rifle.png"), Vec3.ZERO), new DropSound(ShellEjectionFeature.resource("shell"), 0.7f, 1.0f, 0.06f)),
        SHOTGUN("shell_casing/shotgun", new Visuals(ShellEjectionFeature.resource("entity/shell_casing/shotgun"), ShellEjectionFeature.resource("textures/entity/shell_casing/shotgun.png"), Vec3.ZERO), new DropSound(ShellEjectionFeature.resource("shell"), 0.85f, 0.88f, 0.05f));

        private final ResourceLocation assetBase;
        private final Visuals defaultVisuals;
        private final DropSound defaultDropSound;

        private Preset(String path, Visuals defaultVisuals, DropSound defaultDropSound) {
            this.assetBase = ShellEjectionFeature.resource(path);
            this.defaultVisuals = defaultVisuals;
            this.defaultDropSound = defaultDropSound;
        }

        public ResourceLocation getAssetBase() {
            return this.assetBase;
        }

        public ResourceLocation getModelResource() {
            return ShellEjectionFeature.resource("entity/" + this.assetBase.getPath());
        }

        public ResourceLocation getTextureResource() {
            return ShellEjectionFeature.resource("textures/entity/" + this.assetBase.getPath() + ".png");
        }

        public Visuals getDefaultVisuals() {
            return this.defaultVisuals;
        }

        public DropSound getDefaultDropSound() {
            return this.defaultDropSound;
        }
    }
}

