/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 */
package com.vicmatskiv.pointblank.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.client.effect.MuzzleFlashEffect;
import com.vicmatskiv.pointblank.client.render.GunItemRenderer;
import com.vicmatskiv.pointblank.client.render.HierarchicalRenderContext;
import com.vicmatskiv.pointblank.client.render.RenderPass;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.client.render.layer.FeaturePassLayer;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.feature.MuzzleFlashFeature;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.registry.EffectRegistry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class MuzzleFlashItemLayer
extends FeaturePassLayer<GunItem> {
    private RenderType placeholderRenderType = RenderType.entityCutoutNoCull((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/effect/calibration.png"));

    public MuzzleFlashItemLayer(GunItemRenderer renderer) {
        super(renderer, MuzzleFlashFeature.class, RenderPass.MUZZLE_FLASH, Set.of("muzzle", "muzzleflash", "muzzleflashalt"), true, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void render(BakedGeoModel bakedGeoModel, PoseStack poseStack, MultiBufferSource bufferSource, GunItem animatable, RenderType renderType, VertexConsumer buffer, float partialTick, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        FeatureProvider fp;
        MuzzleFlashFeature muzzleFlashFeature;
        HierarchicalRenderContext hrc = HierarchicalRenderContext.current();
        ItemStack itemStack = hrc.getItemStack();
        ConditionContext context = new ConditionContext(HierarchicalRenderContext.getRootItemStack());
        Item item = itemStack.getItem();
        if (item instanceof FeatureProvider && (muzzleFlashFeature = (fp = (FeatureProvider)item).getFeature(MuzzleFlashFeature.class)) != null) {
            for (Pair<MuzzleFlashEffect.Builder, Predicate<ConditionContext>> p : muzzleFlashFeature.getMuzzleFlashEffectBuilders()) {
                MuzzleFlashEffect.Builder effectBuilder;
                UUID effectId;
                Predicate predicate = (Predicate)p.getSecond();
                if (!predicate.test(context) || (effectId = EffectRegistry.getEffectId((effectBuilder = (MuzzleFlashEffect.Builder)p.getFirst()).getName())) == null) continue;
                RenderType renderTypeOverride = RenderTypeProvider.getInstance().getMuzzleFlashRenderType(effectBuilder.getTexture());
                RenderPass.push(this.getRenderPass());
                try {
                    RenderPass.setEffectId(effectId);
                    super.render(bakedGeoModel, poseStack, bufferSource, (GeoAnimatable)animatable, renderTypeOverride, bufferSource.getBuffer(renderTypeOverride), partialTick, packedLight, overlay, red, green, blue, alpha);
                }
                finally {
                    RenderPass.pop();
                }
            }
        }
    }

    @Override
    public RenderType getRenderType() {
        return this.placeholderRenderType;
    }

    @Override
    public boolean isSupportedItemDisplayContext(ItemDisplayContext itemDisplayContext) {
        return itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || itemDisplayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }
}

