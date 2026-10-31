/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 */
package com.vicmatskiv.pointblank.client.effect;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.effect.AbstractEffect;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.EffectRenderContext;
import com.vicmatskiv.pointblank.client.uv.SpriteUVProvider;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.util.TimeUnit;
import java.util.Collection;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class MuzzleFlashEffect
extends AbstractEffect {
    private GunClientState gunState;
    private String selectedMuzzleFlashBone;
    private long startTime;
    private long nanoDuration;
    private SpriteUVProvider spriteUVProvider;

    public boolean isExpired() {
        return System.nanoTime() - this.startTime > this.nanoDuration;
    }

    public String getSelectedMuzzleFlashBone() {
        return this.selectedMuzzleFlashBone;
    }

    private float getProgress() {
        float progress = (float)((double)(System.nanoTime() - this.startTime) / (double)this.nanoDuration);
        return Mth.clamp((float)progress, (float)0.0f, (float)1.0f);
    }

    @Override
    public void render(EffectRenderContext effectRenderContext) {
        PoseStack poseStack = effectRenderContext.getPoseStack();
        if (poseStack == null) {
            return;
        }
        poseStack.pushPose();
        this.renderQuad(poseStack, effectRenderContext.getPosition(), effectRenderContext.getVertexBuffer(), effectRenderContext.getLightColor(), 1.0f, 1.0f, 1.0f, 1.0f, this.getProgress());
        poseStack.popPose();
    }

    protected void renderQuad(PoseStack poseStack, Vec3 position, VertexConsumer buffer, int packedLight, float red, float green, float blue, float alpha, float progress) {
        Matrix4f poseState = poseStack.last().pose();
        this.createVerticesOfQuad(position, poseState, buffer, packedLight, red, green, blue, progress);
    }

    protected void createVerticesOfQuad(Vec3 position, Matrix4f poseState, VertexConsumer buffer, int packedLight, float red, float green, float blue, float progress) {
        float expand;
        float[] uv = this.spriteUVProvider.getSpriteUV(progress);
        float alpha = this.alphaProvider.getValue(progress);
        if (uv == null) {
            return;
        }
        float minU = uv[0];
        float minV = uv[1];
        float maxU = uv[2];
        float maxV = uv[3];
        float[][] texUV = new float[][]{{maxU, maxV}, {minU, maxV}, {minU, minV}, {maxU, minV}};
        float expandX = expand = this.widthProvider.getValue(progress);
        float expandY = expand;
        float zOffset = 0.0f;
        float[][] positionOffsets = new float[][]{{expandX, -expandY, zOffset}, {-expandX, -expandY, zOffset}, {-expandX, expandY, zOffset}, {expandX, expandY, zOffset}};
        if (this.isGlowEnabled) {
            packedLight = 240;
        }
        for (int i = 0; i < 4; ++i) {
            buffer.addVertex(poseState, (float)position.x() + positionOffsets[i][0], (float)position.y() + positionOffsets[i][1], (float)position.z() + positionOffsets[i][2]).setColor(red, green, blue, alpha).setUv(texUV[i][0], texUV[i][1]).setOverlay(0).setLight(packedLight).setNormal(0.0f, 1.0f, 0.0f);
        }
    }

    @Override
    public void launch(Entity player) {
        this.gunState.addMuzzleEffect(this);
    }

    @Override
    public boolean hasInfiniteBounds() {
        return false;
    }

    public static class Builder
    extends AbstractEffect.AbstractEffectBuilder<Builder, MuzzleFlashEffect> {
        private static final Set<GunItem.FirePhase> COMPATIBLE_PHASES = Set.of(GunItem.FirePhase.FIRING);

        @Override
        public Collection<GunItem.FirePhase> getCompatiblePhases() {
            return COMPATIBLE_PHASES;
        }

        @Override
        public boolean isEffectAttached() {
            return true;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            return (Builder)super.withJsonObject(obj);
        }

        @Override
        public MuzzleFlashEffect build(EffectBuilder.Context context) {
            MuzzleFlashEffect effect = new MuzzleFlashEffect();
            super.apply(effect, context);
            effect.gunState = context.getGunClientState();
            effect.selectedMuzzleFlashBone = effect.gunState != null ? effect.gunState.getSelectedAlternatingMuzzleFlashBone() : null;
            effect.startTime = System.nanoTime();
            effect.spriteUVProvider = effect.getSpriteUVProvider();
            effect.nanoDuration = TimeUnit.MILLISECOND.toNanos(effect.duration);
            return effect;
        }
    }
}

