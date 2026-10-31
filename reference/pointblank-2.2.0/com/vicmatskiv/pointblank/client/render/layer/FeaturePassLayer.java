/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.world.item.Item
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.renderer.GeoRenderer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package com.vicmatskiv.pointblank.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.client.render.HierarchicalRenderContext;
import com.vicmatskiv.pointblank.client.render.RenderApprover;
import com.vicmatskiv.pointblank.client.render.RenderPass;
import com.vicmatskiv.pointblank.client.render.RenderPassRenderer;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public abstract class FeaturePassLayer<T extends GeoAnimatable>
extends GeoRenderLayer<T>
implements RenderPassRenderer<T>,
RenderApprover {
    protected static final Collection<String> ALL_PARTS = Collections.emptySet();
    private RenderPass renderPass;
    private Class<? extends Feature> featureType;
    private boolean isEffectLayer;
    private Collection<String> renderedParts;
    protected Object effectId;

    public FeaturePassLayer(GeoRenderer<T> renderer, Class<? extends Feature> featureType, RenderPass renderPass, Collection<String> renderedParts, boolean isEffectLayer, Object effectId) {
        super(renderer);
        this.featureType = featureType;
        this.renderPass = renderPass;
        this.isEffectLayer = isEffectLayer;
        this.effectId = effectId;
        this.renderedParts = renderedParts != ALL_PARTS ? Collections.unmodifiableCollection(renderedParts) : ALL_PARTS;
    }

    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.renderPass(() -> {
            RenderType renderType2 = this.getRenderType();
            if (renderType2 != null) {
                this.render(this.getDefaultBakedModel((GeoAnimatable)animatable), poseStack, bufferSource, (GeoAnimatable)animatable, renderType2, bufferSource.getBuffer(renderType2), partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        });
    }

    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.renderPass(() -> {
            RenderType renderType2 = this.getPreRenderType();
            if (renderType2 != null) {
                poseStack.pushPose();
                this.preRender(this.getDefaultBakedModel((GeoAnimatable)animatable), poseStack, bufferSource, (GeoAnimatable)animatable, renderType2, bufferSource.getBuffer(renderType2), partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
                poseStack.popPose();
            }
        });
    }

    @Override
    public RenderPass getRenderPass() {
        return this.renderPass;
    }

    @Override
    public boolean canRenderPart(String partName) {
        return this.renderedParts == ALL_PARTS || this.renderedParts.contains(partName);
    }

    @Override
    public Class<? extends Feature> getFeatureType() {
        return this.featureType;
    }

    @Override
    public boolean isEffectLayer() {
        return this.isEffectLayer;
    }

    public Object getEffectId() {
        return this.effectId;
    }

    public static <F extends Feature> F getFeature(Class<F> featureType) {
        Item item;
        HierarchicalRenderContext hrc = HierarchicalRenderContext.current();
        if (hrc != null && (item = hrc.getItemStack().getItem()) instanceof FeatureProvider) {
            FeatureProvider fp = (FeatureProvider)item;
            HierarchicalRenderContext root = HierarchicalRenderContext.getRoot();
            F feature = fp.getFeature(featureType);
            if (feature != null && feature.isEnabledForAttachment(root.getItemStack(), hrc.getItemStack())) {
                return feature;
            }
        }
        return null;
    }
}

