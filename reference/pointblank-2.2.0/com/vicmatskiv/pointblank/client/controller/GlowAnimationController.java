/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  software.bernie.geckolib.cache.object.GeoBone
 */
package com.vicmatskiv.pointblank.client.controller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.controller.AbstractProceduralAnimationController;
import com.vicmatskiv.pointblank.client.effect.AbstractEffect;
import com.vicmatskiv.pointblank.client.render.GunItemRenderer;
import com.vicmatskiv.pointblank.client.uv.LoopingSpriteUVProvider;
import com.vicmatskiv.pointblank.client.uv.PlayOnceSpriteUVProvider;
import com.vicmatskiv.pointblank.client.uv.RandomSpriteUVProvider;
import com.vicmatskiv.pointblank.client.uv.SpriteUVProvider;
import com.vicmatskiv.pointblank.client.uv.StaticSpriteUVProvider;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;

public class GlowAnimationController
extends AbstractProceduralAnimationController {
    private boolean isGlowing;
    private final boolean hasCustomTexture;
    private Set<String> glowingPartNames;
    private Set<GunItem.FirePhase> firePhases;
    private Set<Direction> directions;
    private Supplier<SpriteUVProvider> spriteUVProviderSupplier;

    protected GlowAnimationController(long duration, Set<GunItem.FirePhase> firePhases, Set<String> glowingPartNames, Set<Direction> directions, boolean hasCustomTexture, Supplier<SpriteUVProvider> spriteUVProviderSupplier) {
        super(duration);
        this.firePhases = Collections.unmodifiableSet(firePhases);
        this.glowingPartNames = Collections.unmodifiableSet(glowingPartNames);
        this.directions = directions;
        this.hasCustomTexture = hasCustomTexture;
        this.spriteUVProviderSupplier = spriteUVProviderSupplier;
        this.reset();
    }

    public Set<String> getGlowingPartNames() {
        return this.glowingPartNames;
    }

    @Override
    public void onStartFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
        this.reset();
        this.isGlowing = this.firePhases.contains((Object)GunItem.FirePhase.FIRING) || this.firePhases.contains((Object)GunItem.FirePhase.ANY);
    }

    @Override
    public void onPrepareFiring(LivingEntity player, GunClientState state, ItemStack itemStack) {
        this.reset();
        this.isGlowing = this.firePhases.contains((Object)GunItem.FirePhase.PREPARING) || this.firePhases.contains((Object)GunItem.FirePhase.ANY);
    }

    @Override
    public void onCompleteFiring(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
        this.isGlowing = this.firePhases.contains((Object)GunItem.FirePhase.COMPLETETING) || this.firePhases.contains((Object)GunItem.FirePhase.ANY);
    }

    @Override
    public void onPrepareIdle(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
        this.isGlowing = this.firePhases.contains((Object)GunItem.FirePhase.ANY);
    }

    @Override
    public void onIdle(LivingEntity player, GunClientState gunClientState, ItemStack itemStack) {
        this.isGlowing = this.firePhases.contains((Object)GunItem.FirePhase.ANY);
    }

    public void renderCubesOfBone(GunItemRenderer gunItemRenderer, PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (this.isGlowing || this.firePhases.contains((Object)GunItem.FirePhase.ANY)) {
            packedLight = 240;
            float progress = (float)this.getProgress(null, 0.0f);
            gunItemRenderer.renderCubesOfBoneParent(poseStack, bone, buffer, packedLight, packedOverlay, color, this.hasCustomTexture, this.directions, this.spriteUVProviderSupplier, progress);
        }
    }

    public static class Builder {
        private static int effectIdCounter = 0;
        protected int effectId;
        protected ResourceLocation texture;
        protected Set<String> glowingPartNames = new HashSet<String>();
        protected Set<GunItem.FirePhase> firePhases = new HashSet<GunItem.FirePhase>();
        protected AbstractEffect.SpriteInfo spriteInfo;
        protected Set<Direction> directions;

        public Builder() {
            this.effectId = effectIdCounter++;
        }

        public int getEffectId() {
            return this.effectId;
        }

        public ResourceLocation getTexture() {
            return this.texture;
        }

        public Builder withTexture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public Builder withGlowingPartNames(Collection<String> glowingPartNames) {
            this.glowingPartNames.addAll(glowingPartNames);
            return this;
        }

        public Builder withFirePhases(Collection<GunItem.FirePhase> firePhases) {
            this.firePhases.addAll(firePhases);
            return this;
        }

        public Builder withSprites(int rows, int columns, int spritesPerSecond, AbstractEffect.SpriteAnimationType type) {
            this.spriteInfo = new AbstractEffect.SpriteInfo(rows, columns, spritesPerSecond, type);
            return this;
        }

        public Builder withDirections(Direction ... directions) {
            if (directions != null && directions.length > 0) {
                this.directions = Set.of(directions);
            }
            return this;
        }

        public GlowAnimationController build() {
            Supplier<SpriteUVProvider> spriteUVProviderSupplier = null;
            if (this.spriteInfo != null) {
                switch (this.spriteInfo.type()) {
                    case STATIC: {
                        spriteUVProviderSupplier = () -> StaticSpriteUVProvider.INSTANCE;
                        break;
                    }
                    case LOOP: {
                        LoopingSpriteUVProvider spriteUVProvider = new LoopingSpriteUVProvider(this.spriteInfo.rows(), this.spriteInfo.columns(), this.spriteInfo.spritesPerSecond(), Integer.MAX_VALUE);
                        spriteUVProviderSupplier = () -> spriteUVProvider;
                        break;
                    }
                    case RANDOM: {
                        spriteUVProviderSupplier = () -> new RandomSpriteUVProvider(this.spriteInfo.rows(), this.spriteInfo.columns(), this.spriteInfo.spritesPerSecond(), Integer.MAX_VALUE);
                        break;
                    }
                    case PLAY_ONCE: {
                        PlayOnceSpriteUVProvider spriteUVProvider = new PlayOnceSpriteUVProvider(this.spriteInfo.rows(), this.spriteInfo.columns(), this.spriteInfo.spritesPerSecond(), Integer.MAX_VALUE);
                        spriteUVProviderSupplier = () -> spriteUVProvider;
                        break;
                    }
                }
            }
            return new GlowAnimationController(Integer.MAX_VALUE, this.firePhases, this.glowingPartNames, this.directions, this.texture != null, spriteUVProviderSupplier);
        }
    }
}

