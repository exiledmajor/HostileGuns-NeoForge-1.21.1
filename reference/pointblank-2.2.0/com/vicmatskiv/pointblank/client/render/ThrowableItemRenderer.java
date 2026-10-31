/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.ItemTransform
 *  net.minecraft.client.renderer.block.model.ItemTransforms
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Quaternionf
 *  software.bernie.geckolib.cache.GeckoLibCache
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.cache.object.GeoCube
 *  software.bernie.geckolib.cache.object.GeoVertex
 *  software.bernie.geckolib.model.DefaultedItemGeoModel
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoItemRenderer
 *  software.bernie.geckolib.renderer.GeoRenderer
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package com.vicmatskiv.pointblank.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.ClientEventHandler;
import com.vicmatskiv.pointblank.client.render.HierarchicalRenderContext;
import com.vicmatskiv.pointblank.client.render.RenderApprover;
import com.vicmatskiv.pointblank.client.render.RenderPass;
import com.vicmatskiv.pointblank.client.render.RenderPassGeoRenderer;
import com.vicmatskiv.pointblank.client.render.RenderPassProvider;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.client.render.layer.GunHandsItemLayer;
import com.vicmatskiv.pointblank.compat.iris.IrisCompat;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.SkinFeature;
import com.vicmatskiv.pointblank.item.ThrowableItem;
import com.vicmatskiv.pointblank.util.ClientUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ThrowableItemRenderer
extends GeoItemRenderer<ThrowableItem>
implements RenderPassGeoRenderer<ThrowableItem>,
RenderApprover {
    public static final String BONE_RIGHTARM = "rightarm";
    public static final String BONE_LEFTARM = "leftarm";
    public static final String BONE_CAMERA = "_camera_";
    private ItemTransforms transforms;
    private final ResourceLocation leftHandModelResource = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"geo/item/left_arm.geo.json");
    private final ResourceLocation rightHandModelResource = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"geo/item/right_arm.geo.json");
    private final ResourceLocation reticleModelResource = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"geo/item/reticle.geo.json");

    public ThrowableItemRenderer(ResourceLocation modelResource) {
        super((GeoModel)new DefaultedItemGeoModel(modelResource));
        this.addRenderLayer(new GunHandsItemLayer<ThrowableItem>(this));
    }

    public ResourceLocation getTextureLocation(ThrowableItem animatable) {
        ResourceLocation texture = null;
        HierarchicalRenderContext hrc = HierarchicalRenderContext.getRoot();
        if (hrc != null) {
            ItemStack itemStack = hrc.getItemStack();
            texture = SkinFeature.getTexture(itemStack);
        }
        if (texture == null) {
            texture = super.getTextureLocation((Item)animatable);
        }
        return texture;
    }

    @Override
    public GeoRenderer<ThrowableItem> getRenderer() {
        return this;
    }

    private BakedGeoModel getLeftHandModel() {
        return (BakedGeoModel)GeckoLibCache.getBakedModels().get(this.leftHandModelResource);
    }

    private BakedGeoModel getRightHandModel() {
        return (BakedGeoModel)GeckoLibCache.getBakedModels().get(this.rightHandModelResource);
    }

    private Player getPlayer(ItemDisplayContext itemDisplayContext) {
        if (itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || itemDisplayContext == ItemDisplayContext.GROUND) {
            return ClientUtil.getClientPlayer();
        }
        LivingEntity renderedEntity = ClientEventHandler.getCurrentEntityLiving();
        if (renderedEntity instanceof Player) {
            return (Player)renderedEntity;
        }
        return null;
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (IrisCompat.getInstance().isRenderingShadows()) {
            return;
        }
        Player player = this.getPlayer(itemDisplayContext);
        if (player == null) {
            return;
        }
        MultiBufferSource wrappedBufferSource = RenderTypeProvider.getInstance().wrapBufferSource(bufferSource);
        try (HierarchicalRenderContext hrc = HierarchicalRenderContext.push(stack, itemDisplayContext);){
            this.renderPass(() -> {
                boolean isFirstPerson = itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
                poseStack.pushPose();
                ThrowableItem gunItem = (ThrowableItem)stack.getItem();
                GeoModel geoModel = this.getGeoModel();
                if (isFirstPerson) {
                    this.initTransforms(player, stack, itemDisplayContext);
                    this.adjustFirstPersonPose(stack, gunItem, poseStack, (GeoModel<ThrowableItem>)geoModel);
                }
                super.renderByItem(stack, itemDisplayContext, poseStack, wrappedBufferSource, packedLight, packedOverlay);
                poseStack.popPose();
            });
        }
    }

    @Override
    public void renderPass(Runnable runnablePass) {
        RenderPass.push(this.getRenderPass());
        try {
            runnablePass.run();
        }
        finally {
            RenderPass.pop();
        }
    }

    private void adjustFirstPersonPose(ItemStack itemStack, ThrowableItem throwableItem, PoseStack poseStack, GeoModel<ThrowableItem> geoModel) {
        if (this.transforms == null) {
            return;
        }
        ItemTransform fprt = this.transforms.firstPersonRightHand;
        if (fprt == null) {
            return;
        }
        float v = 1.0f;
        float rescale = throwableItem.getModelScale();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(new Quaternionf().rotationXYZ(-fprt.rotation.x * ((float)Math.PI / 180), -fprt.rotation.y * ((float)Math.PI / 180), -fprt.rotation.z * ((float)Math.PI / 180)));
        poseStack.translate(-fprt.translation.x, -fprt.translation.y, -fprt.translation.z);
        poseStack.translate(fprt.translation.x * rescale, fprt.translation.y * rescale, fprt.translation.z * rescale);
        poseStack.mulPose(new Quaternionf().rotationXYZ(fprt.rotation.x * ((float)Math.PI / 180), fprt.rotation.y * ((float)Math.PI / 180), fprt.rotation.z * ((float)Math.PI / 180)));
        poseStack.scale(rescale, rescale, rescale);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        poseStack.translate(0.48f, -1.12f, -0.72f);
        poseStack.translate(-0.006f, 0.6f, 0.0f);
    }

    private void initTransforms(Player player, ItemStack stack, ItemDisplayContext itemDisplayContext) {
        if (this.transforms == null) {
            this.transforms = Platform.getInstance().getItemTransforms(player, stack).apply(itemDisplayContext);
        }
    }

    private boolean shouldRenderBone(String boneName) {
        if (boneName.charAt(0) == '_') {
            return false;
        }
        HierarchicalRenderContext current = HierarchicalRenderContext.current();
        ItemStack rootStack = HierarchicalRenderContext.getRoot().getItemStack();
        boolean shouldRender = true;
        for (GeoRenderLayer layer : this.getRenderLayers()) {
            RenderPass renderPass;
            RenderPass renderPass2;
            if (!(layer instanceof RenderApprover)) continue;
            RenderApprover renderApprover = (RenderApprover)layer;
            if (layer instanceof RenderPassProvider) {
                RenderPassProvider rp = (RenderPassProvider)layer;
                renderPass2 = rp.getRenderPass();
            } else {
                renderPass2 = null;
            }
            if (renderApprover.approveRendering(renderPass = renderPass2, boneName, rootStack, current.getItemStack(), current.getPath(), current.getItemDisplayContext())) continue;
            shouldRender = false;
            break;
        }
        return shouldRender;
    }

    public void renderRecursively(PoseStack poseStack, ThrowableItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        HierarchicalRenderContext current = HierarchicalRenderContext.current();
        ItemStack rootStack = HierarchicalRenderContext.getRoot().getItemStack();
        boolean shouldRender = this.approveRendering(this.getRenderPass(), bone.getName(), rootStack, current.getItemStack(), current.getPath(), current.getItemDisplayContext());
        if (shouldRender) {
            super.renderRecursively(poseStack, (Item)animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
        }
    }

    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        RenderPass renderPass = RenderPass.current();
        if (!this.shouldRenderBone(bone.getName())) {
            return;
        }
        HierarchicalRenderContext hrc = HierarchicalRenderContext.current();
        ItemDisplayContext itemDisplayContext = hrc.getItemDisplayContext();
        boolean isFirstPerson = itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        double aimingProgress = 0.0;
        switch (renderPass) {
            case HANDS: {
                if (bone.getName().equals(BONE_RIGHTARM)) {
                    this.renderRightArm(poseStack, bone, buffer, packedLight, packedOverlay, color);
                    break;
                }
                if (!bone.getName().equals(BONE_LEFTARM)) break;
                this.renderLeftArm(poseStack, bone, buffer, packedLight, packedOverlay, color);
                break;
            }
            case MAIN_ITEM: {
                if (!this.canRenderPart(bone.getName())) break;
                super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, color);
            }
        }
    }

    private void renderLeftArm(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        BakedGeoModel handsBakedGeoModel = this.getLeftHandModel();
        GeoBone leftArmBone = handsBakedGeoModel.getBone(BONE_LEFTARM).orElse(null);
        if (leftArmBone != null) {
            poseStack.pushPose();
            this.applyArmRefTransforms(poseStack, bone, leftArmBone);
            super.renderCubesOfBone(poseStack, leftArmBone, buffer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }
    }

    private void renderRightArm(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        BakedGeoModel handsBakedGeoModel = this.getRightHandModel();
        GeoBone rightArmBone = handsBakedGeoModel.getBone(BONE_RIGHTARM).orElse(null);
        if (rightArmBone != null) {
            poseStack.pushPose();
            this.applyArmRefTransforms(poseStack, bone, rightArmBone);
            super.renderCubesOfBone(poseStack, rightArmBone, buffer, packedLight, packedOverlay, color);
            poseStack.popPose();
        }
    }

    private void applyArmRefTransforms(PoseStack poseStack, GeoBone refBone, GeoBone leftArmBone) {
        GeoCube leftArmBoneCube = (GeoCube)leftArmBone.getCubes().get(0);
        GeoCube refCube = (GeoCube)refBone.getCubes().get(0);
        GeoVertex leftArmBoneVertex = leftArmBoneCube.quads()[0].vertices()[0];
        GeoVertex refVertex = refCube.quads()[0].vertices()[0];
        float dx = refVertex.position().x() - leftArmBoneVertex.position().x();
        float dy = refVertex.position().y() - leftArmBoneVertex.position().y();
        float dz = refVertex.position().z() - leftArmBoneVertex.position().z();
        poseStack.translate(dx, dy, dz);
    }

    private void applyRefTransforms(PoseStack poseStack, GeoBone refBone, GeoBone actualBone) {
        GeoCube actualArmBoneCube = (GeoCube)actualBone.getCubes().get(0);
        GeoCube refCube = (GeoCube)refBone.getCubes().get(0);
        GeoVertex refQ0v0 = refCube.quads()[2].vertices()[0];
        GeoVertex refQ0v2 = refCube.quads()[2].vertices()[2];
        GeoVertex actualQ0v0 = actualArmBoneCube.quads()[2].vertices()[0];
        GeoVertex actualQ0v2 = actualArmBoneCube.quads()[2].vertices()[2];
        float refSizeX = Math.abs(refQ0v0.position().x - refQ0v2.position().x);
        float actualSizeX = Math.abs(actualQ0v0.position().x - actualQ0v2.position().x);
        float refSizeY = Math.abs(refQ0v0.position().y - refQ0v2.position().y);
        float actualSizeY = Math.abs(actualQ0v0.position().y - actualQ0v2.position().y);
        float refXLeft = refQ0v0.position().x;
        float refZLeft = refQ0v2.position().z;
        float actualXLeft = actualQ0v0.position().x;
        float actualYTop = actualQ0v0.position().y;
        float refYTop = refQ0v0.position().y;
        float actualZLeft = actualQ0v0.position().z;
        float dx = -(actualXLeft + (refXLeft - (refSizeX - actualSizeX) / 2.0f));
        float dy = refYTop + (actualSizeY - refSizeY) / 2.0f - actualYTop;
        float dz = refZLeft - actualZLeft;
        poseStack.translate(dx, dy, dz);
    }

    @Override
    public boolean isEffectLayer() {
        return false;
    }

    @Override
    public RenderType getRenderType() {
        return null;
    }

    @Override
    public boolean isSupportedItemDisplayContext(ItemDisplayContext context) {
        return true;
    }

    @Override
    public boolean canRenderPart(String boneName) {
        return !boneName.equals(BONE_LEFTARM) && !boneName.equals(BONE_RIGHTARM);
    }

    @Override
    public RenderPass getRenderPass() {
        return RenderPass.MAIN_ITEM;
    }

    @Override
    public Class<? extends Feature> getFeatureType() {
        return null;
    }
}

