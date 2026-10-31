/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Vector4f
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.renderer.layer.GeoRenderLayer
 */
package com.vicmatskiv.pointblank.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.attachment.Attachment;
import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.attachment.AttachmentHost;
import com.vicmatskiv.pointblank.attachment.AttachmentModelInfo;
import com.vicmatskiv.pointblank.attachment.Attachments;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.gui.AttachmentManagerScreen;
import com.vicmatskiv.pointblank.client.render.GunItemRenderer;
import com.vicmatskiv.pointblank.client.render.HierarchicalRenderContext;
import com.vicmatskiv.pointblank.client.render.RenderApprover;
import com.vicmatskiv.pointblank.client.render.RenderPass;
import com.vicmatskiv.pointblank.client.render.RenderPassGeoRenderer;
import com.vicmatskiv.pointblank.client.render.RenderPassRenderer;
import com.vicmatskiv.pointblank.client.render.RenderTypeKey;
import com.vicmatskiv.pointblank.client.render.RenderTypeProvider;
import com.vicmatskiv.pointblank.client.render.layer.FeaturePassLayer;
import java.util.Collection;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class AttachmentLayer<T extends GeoAnimatable>
extends FeaturePassLayer<T> {
    private static final Vector4f COLOR_GREEN = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);
    private static final Vector4f COLOR_NORMAL = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private RenderPassRenderer<T> currentRenderer;
    private RenderPassGeoRenderer<T> mainRenderer;

    public AttachmentLayer(RenderPassGeoRenderer<T> renderer) {
        super(renderer, null, RenderPass.ATTACHMENTS, ALL_PARTS, true, null);
        this.mainRenderer = renderer;
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!(animatable instanceof AttachmentHost)) {
            return;
        }
        ItemStack rootStack = HierarchicalRenderContext.current().getItemStack();
        this.currentRenderer = this.mainRenderer;
        Runnable renderAttachmentsPass = () -> {
            this.renderAttachments(null, null, animatable, "/", rootStack, poseStack, bufferSource, buffer, partialTick, packedLight, packedOverlay);
            for (GeoRenderLayer effectLayer : this.mainRenderer.getRenderLayers()) {
                GunClientState state;
                RenderPassRenderer renderPassRenderer;
                if (effectLayer == this || !(effectLayer instanceof RenderPassRenderer) || !(renderPassRenderer = (RenderPassRenderer)effectLayer).isEffectLayer()) continue;
                RenderPassGeoRenderer<T> renderPassGeoRenderer = this.mainRenderer;
                if (renderPassGeoRenderer instanceof GunItemRenderer) {
                    GunItemRenderer gunRenderer = (GunItemRenderer)renderPassGeoRenderer;
                    v0 = gunRenderer.getGunClientState();
                } else {
                    v0 = state = GunClientState.getMainHeldState();
                }
                if (state == null) continue;
                renderPassRenderer.renderPass(() -> {
                    this.currentRenderer = renderPassRenderer;
                    this.renderAttachments(state, null, animatable, "/", rootStack, poseStack, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                    this.currentRenderer = null;
                });
            }
        };
        this.renderPass(() -> {
            ItemDisplayContext itemDisplayContext = HierarchicalRenderContext.current().getItemDisplayContext();
            RenderPassGeoRenderer<T> patt0$temp = this.mainRenderer;
            if (patt0$temp instanceof GunItemRenderer) {
                GunItemRenderer gunItemRenderer = (GunItemRenderer)patt0$temp;
                if (itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                    poseStack.pushPose();
                    gunItemRenderer.applyFirstPersonBodyInertiaForCurrentState(rootStack, poseStack);
                    try {
                        renderAttachmentsPass.run();
                        return;
                    }
                    finally {
                        poseStack.popPose();
                    }
                }
            }
            renderAttachmentsPass.run();
        });
    }

    public void renderAttachments(GunClientState state, RenderType renderTypeOverride, T gunItem, String parentPath, ItemStack baseItemStack, PoseStack poseStack, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        Item item = baseItemStack.getItem();
        if (!(item instanceof AttachmentHost)) {
            return;
        }
        AttachmentHost attachmentHost = (AttachmentHost)item;
        Collection<ItemStack> attachmentItemStacks = Attachments.getAttachments(baseItemStack);
        for (ItemStack attachmentItemStack : attachmentItemStacks) {
            Attachment attachment;
            AttachmentModelInfo attachmentInfo;
            Item item2 = attachmentItemStack.getItem();
            if (!(item2 instanceof Attachment) || (attachmentInfo = (AttachmentModelInfo)AttachmentModelInfo.attachmentInfos.apply(attachmentHost, attachment = (Attachment)item2).orElse(null)) == null) continue;
            this.renderAttachment(state, renderTypeOverride, gunItem, parentPath, baseItemStack, attachmentInfo.baseModel(), attachmentInfo.baseBone(), attachmentItemStack, attachmentInfo.attachmentModel(), attachmentInfo.attachmentBone(), poseStack, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }
    }

    protected void renderAttachment(GunClientState state, RenderType renderTypeOverride, T gunItem, String parentPath, ItemStack baseItemStack, BakedGeoModel baseModel, GeoBone baseBone, ItemStack attachmentItemStack, BakedGeoModel attachmentModel, GeoBone attachmentBone, PoseStack poseStack, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        Item item = attachmentItemStack.getItem();
        if (!(item instanceof Attachment)) {
            return;
        }
        Attachment attachment = (Attachment)item;
        ItemDisplayContext itemDisplayContext = HierarchicalRenderContext.current().getItemDisplayContext();
        try (HierarchicalRenderContext hrc = HierarchicalRenderContext.push(attachmentItemStack, itemDisplayContext);){
            String attachmentPath = parentPath + "/" + attachment.getName();
            Pair basePos = AttachmentModelInfo.modelBonePositions.apply(new AttachmentModelInfo.ModelBoneKey(baseModel, baseBone.getName(), 1.0f)).orElse(null);
            Pair attachmentPos = AttachmentModelInfo.modelBonePositions.apply(new AttachmentModelInfo.ModelBoneKey(attachmentModel, attachmentBone.getName(), 1.0f)).orElse(null);
            if (basePos != null && attachmentPos != null) {
                RenderType attachmentRenderType = null;
                Vector4f color = COLOR_NORMAL;
                if (renderTypeOverride == null) {
                    Pair<RenderType, Vector4f> p = this.getRenderType(baseItemStack, attachmentItemStack, attachment.getName(), attachmentPath, itemDisplayContext);
                    attachmentRenderType = (RenderType)p.getFirst();
                    color = (Vector4f)p.getSecond();
                } else {
                    attachmentRenderType = renderTypeOverride;
                    color = COLOR_GREEN;
                }
                if (attachmentRenderType != null) {
                    poseStack.pushPose();
                    AttachmentModelInfo.preparePoseStackForBoneInHierarchy(poseStack, baseBone);
                    poseStack.mulPose((Matrix4f)basePos.getFirst());
                    poseStack.mulPose((Matrix4f)attachmentPos.getSecond());
                    this.currentRenderer.render(attachmentModel, poseStack, bufferSource, gunItem, attachmentRenderType, bufferSource.getBuffer(attachmentRenderType), partialTick, packedLight, OverlayTexture.NO_OVERLAY, color.x, color.y, color.z, color.w);
                    this.renderAttachments(state, renderTypeOverride, gunItem, attachmentPath, attachmentItemStack, poseStack, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                    poseStack.popPose();
                }
            }
        }
    }

    private Pair<RenderType, Vector4f> getRenderType(ItemStack baseItemStack, ItemStack attachmentItemStack, String attachmentName, String attachmentPath, ItemDisplayContext itemDisplayContext) {
        AttachmentManagerScreen ams;
        Pair<RenderType, Vector4f> p;
        Screen screen;
        RenderType renderType = null;
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)("textures/item/" + attachmentName + ".png"));
        Vector4f color = null;
        Minecraft mc = Minecraft.getInstance();
        if (itemDisplayContext == ItemDisplayContext.GROUND && (screen = mc.screen) instanceof AttachmentManagerScreen && (p = (ams = (AttachmentManagerScreen)screen).getRenderTypeOverride(baseItemStack, attachmentItemStack, attachmentName, attachmentPath)) != null) {
            renderType = (RenderType)p.getFirst();
            color = (Vector4f)p.getSecond();
        }
        if (renderType == null) {
            renderType = this.currentRenderer.getRenderType();
        }
        if (renderType == null) {
            HierarchicalRenderContext rootContext = HierarchicalRenderContext.getRoot();
            Object isPipFallbackEnabled = rootContext.getAttribute("isPipFallbackEnabled");
            renderType = isPipFallbackEnabled != null && (Boolean)isPipFallbackEnabled != false ? RenderTypeProvider.getInstance().getDefaultItemRenderType(new RenderTypeKey(texture, true, 517, 7680, 2)) : RenderTypeProvider.getInstance().getDefaultItemRenderType(new RenderTypeKey(texture, false, 0, 0, 0));
        }
        if (color == null) {
            color = COLOR_NORMAL;
        }
        return Pair.of((Object)renderType, (Object)color);
    }

    @Override
    public boolean approveRendering(RenderPass renderPass, String boneName, ItemStack rootStack, ItemStack currentStack, String path, ItemDisplayContext itemDisplayContext) {
        if (renderPass == RenderPass.ATTACHMENTS) {
            return true;
        }
        HierarchicalRenderContext hrc = HierarchicalRenderContext.current();
        Item item = hrc.getItemStack().getItem();
        if (!(item instanceof Attachment)) {
            return true;
        }
        Attachment currentAttachment = (Attachment)item;
        AttachmentCategory category = currentAttachment.getCategory();
        Pair<String, ItemStack> activeCategoryAttachment = Attachments.getSelectedAttachment(rootStack, category);
        if (activeCategoryAttachment != null && !Objects.equals(activeCategoryAttachment.getFirst(), hrc.getPath())) {
            return false;
        }
        RenderPassRenderer<T> renderPassRenderer = this.currentRenderer;
        if (!(renderPassRenderer instanceof RenderApprover)) {
            return true;
        }
        RenderApprover renderApprover = (RenderApprover)((Object)renderPassRenderer);
        return renderApprover.approveRendering(renderPass, boneName, rootStack, currentStack, path, itemDisplayContext);
    }

    @Override
    public RenderType getRenderType() {
        return null;
    }

    @Override
    public boolean isSupportedItemDisplayContext(ItemDisplayContext context) {
        return context != ItemDisplayContext.GUI;
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!(animatable instanceof AttachmentHost)) {
            return;
        }
        ItemStack rootStack = HierarchicalRenderContext.current().getItemStack();
        this.currentRenderer = this.mainRenderer;
        this.renderPass(() -> this.preRenderAttachments(null, null, animatable, "/", rootStack, poseStack, bufferSource, buffer, partialTick, packedLight, packedOverlay));
        for (GeoRenderLayer effectLayer : this.mainRenderer.getRenderLayers()) {
            GunClientState state;
            RenderPassRenderer renderPassRenderer;
            if (effectLayer == this || !(effectLayer instanceof RenderPassRenderer) || !(renderPassRenderer = (RenderPassRenderer)effectLayer).isEffectLayer()) continue;
            RenderPassGeoRenderer<T> renderPassGeoRenderer = this.mainRenderer;
            if (renderPassGeoRenderer instanceof GunItemRenderer) {
                GunItemRenderer gunRenderer = (GunItemRenderer)renderPassGeoRenderer;
                v0 = gunRenderer.getGunClientState();
            } else {
                v0 = state = GunClientState.getMainHeldState();
            }
            if (state == null) continue;
            renderPassRenderer.renderPass(() -> {
                this.currentRenderer = renderPassRenderer;
                this.preRenderAttachments(state, null, animatable, "/", rootStack, poseStack, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                this.currentRenderer = null;
            });
        }
    }

    public void preRenderAttachments(GunClientState state, RenderType renderTypeOverride, T gunItem, String parentPath, ItemStack baseItemStack, PoseStack poseStack, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        Item item = baseItemStack.getItem();
        if (!(item instanceof AttachmentHost)) {
            return;
        }
        AttachmentHost attachmentHost = (AttachmentHost)item;
        Collection<ItemStack> attachmentItemStacks = Attachments.getAttachments(baseItemStack);
        for (ItemStack attachmentItemStack : attachmentItemStacks) {
            Attachment attachment;
            AttachmentModelInfo attachmentInfo;
            Item item2 = attachmentItemStack.getItem();
            if (!(item2 instanceof Attachment) || (attachmentInfo = (AttachmentModelInfo)AttachmentModelInfo.attachmentInfos.apply(attachmentHost, attachment = (Attachment)item2).orElse(null)) == null) continue;
            this.preRenderAttachment(state, renderTypeOverride, gunItem, parentPath, baseItemStack, attachmentInfo.baseModel(), attachmentInfo.baseBone(), attachmentItemStack, attachmentInfo.attachmentModel(), attachmentInfo.attachmentBone(), poseStack, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }
    }

    protected void preRenderAttachment(GunClientState state, RenderType renderTypeOverride, T gunItem, String parentPath, ItemStack baseItemStack, BakedGeoModel baseModel, GeoBone baseBone, ItemStack attachmentItemStack, BakedGeoModel attachmentModel, GeoBone attachmentBone, PoseStack poseStack, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        Item item = attachmentItemStack.getItem();
        if (!(item instanceof Attachment)) {
            return;
        }
        Attachment attachment = (Attachment)item;
        ItemDisplayContext itemDisplayContext = HierarchicalRenderContext.current().getItemDisplayContext();
        try (HierarchicalRenderContext hrc = HierarchicalRenderContext.push(attachmentItemStack, itemDisplayContext);){
            String attachmentPath = parentPath + "/" + attachment.getName();
            Pair basePos = AttachmentModelInfo.modelBonePositions.apply(new AttachmentModelInfo.ModelBoneKey(baseModel, baseBone.getName(), 1.0f)).orElse(null);
            Pair attachmentPos = AttachmentModelInfo.modelBonePositions.apply(new AttachmentModelInfo.ModelBoneKey(attachmentModel, attachmentBone.getName(), 1.0f)).orElse(null);
            if (basePos != null && attachmentPos != null) {
                RenderType attachmentRenderType = null;
                Vector4f color = COLOR_NORMAL;
                if (renderTypeOverride == null) {
                    Pair<RenderType, Vector4f> p = this.getPreRenderType(baseItemStack, attachmentItemStack, attachment.getName(), attachmentPath, itemDisplayContext);
                    attachmentRenderType = (RenderType)p.getFirst();
                    color = (Vector4f)p.getSecond();
                } else {
                    attachmentRenderType = renderTypeOverride;
                    color = COLOR_GREEN;
                }
                if (attachmentRenderType != null) {
                    poseStack.pushPose();
                    AttachmentModelInfo.preparePoseStackForBoneInHierarchy(poseStack, baseBone);
                    poseStack.mulPose((Matrix4f)basePos.getFirst());
                    poseStack.mulPose((Matrix4f)attachmentPos.getSecond());
                    this.currentRenderer.preRender(attachmentModel, poseStack, bufferSource, gunItem, attachmentRenderType, bufferSource.getBuffer(attachmentRenderType), partialTick, packedLight, OverlayTexture.NO_OVERLAY, color.x, color.y, color.z, color.w);
                    this.preRenderAttachments(state, renderTypeOverride, gunItem, attachmentPath, attachmentItemStack, poseStack, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                    poseStack.popPose();
                }
            }
        }
    }

    private Pair<RenderType, Vector4f> getPreRenderType(ItemStack baseItemStack, ItemStack attachmentItemStack, String attachmentName, String attachmentPath, ItemDisplayContext itemDisplayContext) {
        AttachmentManagerScreen ams;
        Pair<RenderType, Vector4f> p;
        Screen screen;
        RenderType renderType = null;
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)("textures/item/" + attachmentName + ".png"));
        Vector4f color = null;
        Minecraft mc = Minecraft.getInstance();
        if (itemDisplayContext == ItemDisplayContext.GROUND && (screen = mc.screen) instanceof AttachmentManagerScreen && (p = (ams = (AttachmentManagerScreen)screen).getRenderTypeOverride(baseItemStack, attachmentItemStack, attachmentName, attachmentPath)) != null) {
            renderType = (RenderType)p.getFirst();
            color = (Vector4f)p.getSecond();
        }
        if (renderType == null) {
            renderType = this.currentRenderer.getRenderType();
        }
        if (renderType == null || renderType == RenderTypeProvider.NO_RENDER_TYPE) {
            renderType = RenderType.entityCutoutNoCull((ResourceLocation)texture);
        }
        if (color == null) {
            color = COLOR_NORMAL;
        }
        return Pair.of((Object)renderType, (Object)color);
    }
}

