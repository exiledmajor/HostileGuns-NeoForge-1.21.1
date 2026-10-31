/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DestFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SourceFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Vector4f
 */
package com.vicmatskiv.pointblank.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.attachment.Attachment;
import com.vicmatskiv.pointblank.attachment.AttachmentHost;
import com.vicmatskiv.pointblank.client.gui.MouseInteractionHandler;
import com.vicmatskiv.pointblank.event.AttachmentAddedEvent;
import com.vicmatskiv.pointblank.event.AttachmentRemovedEvent;
import com.vicmatskiv.pointblank.event.EventBus;
import com.vicmatskiv.pointblank.event.EventListener;
import com.vicmatskiv.pointblank.inventory.AttachmentContainerMenu;
import com.vicmatskiv.pointblank.inventory.AttachmentSlot;
import com.vicmatskiv.pointblank.inventory.HierarchicalSlot;
import com.vicmatskiv.pointblank.inventory.SimpleAttachmentContainer;
import com.vicmatskiv.pointblank.registry.SoundRegistry;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class AttachmentManagerScreen
extends AbstractContainerScreen<AttachmentContainerMenu> {
    private static final ResourceLocation GUI_TEXTURES = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/gui/attachments4.png");
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/gui/blueprint-background-2.png");
    protected static final ResourceLocation WIDGETS_LOCATION = ResourceLocation.parse((String)"textures/gui/widgets.png");
    private static final Vector4f COLOR_GREEN = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);
    private final Inventory playerInventory;
    private MouseInteractionHandler mouseInteractionHandler;
    private final int inventoryWidth = 176;
    private final int inventoryHeight = 90;
    private final int slotWidth = 18;
    private final int slotHeight = 18;
    private final int slotRightPadding = 4;
    private int headerBottomPadding = 2;
    private AttachmentContainerMenu menu;
    private String selectedAttachmentPath;
    private Queue<AttachmentHighlightEvent> attachmentEventQueue = new ArrayDeque<AttachmentHighlightEvent>();
    private EventListener attachmentAddedListener;
    private EventListener attachmentRemovedListener;

    public AttachmentManagerScreen(AttachmentContainerMenu menu, Inventory playerInventory, Component titleIn) {
        super((AbstractContainerMenu)menu, playerInventory, titleIn);
        this.menu = menu;
        this.playerInventory = playerInventory;
        this.imageHeight = 250;
        this.imageWidth = 370;
        this.mouseInteractionHandler = new MouseInteractionHandler(this::isMouseInScreen, 0.5f, 2.0f, 0.1f);
        EventBus eventBus = Platform.getInstance().getEventBus();
        this.attachmentAddedListener = e -> this.onAttachmentAdded((AttachmentAddedEvent)e);
        eventBus.addListener(AttachmentAddedEvent.class, this.attachmentAddedListener);
        this.attachmentRemovedListener = e -> this.onAttachmentRemoved((AttachmentRemovedEvent)e);
        eventBus.addListener(AttachmentRemovedEvent.class, this.attachmentRemovedListener);
    }

    protected void init() {
        super.init();
    }

    public void containerTick() {
        super.containerTick();
        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack selectedStack = this.playerInventory.getSelected();
            if (!(selectedStack.getItem() instanceof AttachmentHost)) {
                Minecraft.getInstance().setScreen(null);
            } else {
                AttachmentHighlightEvent event = this.attachmentEventQueue.peek();
                if (event != null && event.isExpired()) {
                    this.attachmentEventQueue.poll();
                }
            }
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE);
        guiGraphics.blit(BACKGROUND_TEXTURE, 0, 0, 1, 0.0f, 0.0f, this.width, this.height, this.width, this.height);
        RenderSystem.disableBlend();
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        poseStack.popPose();
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack selectedItem = this.minecraft.player.getMainHandItem();
        if (selectedItem != null && selectedItem.getItem() instanceof AttachmentHost) {
            Component label = selectedItem.getItem().getName(selectedItem);
            guiGraphics.drawCenteredString(this.font, label, 180, 15, 0xFFFF00);
        }
    }

    private void renderItemInHand(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate((float)(this.width / 2 + 5), (float)(this.height / 2 - 30), 180.0f);
        this.applyMouseInteractionTransforms(poseStack, mouseX, mouseY);
        float zoom = this.mouseInteractionHandler.getZoom();
        poseStack.scale(zoom, zoom, zoom);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
        poseStack.mulPose(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
        poseStack.scale(100.0f, 100.0f, 100.0f);
        Matrix4fStack modelStack = RenderSystem.getModelViewStack();
        modelStack.pushMatrix();
        modelStack.mul((Matrix4fc)poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();
        MultiBufferSource.BufferSource buffer = guiGraphics.bufferSource();
        ItemStack itemStack = minecraft.player.getMainHandItem();
        BakedModel model = minecraft.getItemRenderer().getModel(itemStack, MiscUtil.getLevel((Entity)minecraft.player), (LivingEntity)minecraft.player, minecraft.player.getId() + ItemDisplayContext.GROUND.ordinal());
        minecraft.getItemRenderer().render(itemStack, ItemDisplayContext.GROUND, false, new PoseStack(), (MultiBufferSource)buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, model);
        guiGraphics.flush();
        modelStack.popMatrix();
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private void applyMouseInteractionTransforms(PoseStack poseStack, int mouseX, int mouseY) {
        float interactionOffsetX = (float)this.mouseInteractionHandler.getX() + (this.mouseInteractionHandler.isInteracting() && this.mouseInteractionHandler.isTranslating() ? (float)mouseX - this.mouseInteractionHandler.getMouseClickedX() : 0.0f);
        float interactionOffsetY = (float)this.mouseInteractionHandler.getY() + (this.mouseInteractionHandler.isInteracting() && this.mouseInteractionHandler.isTranslating() ? (float)mouseY - this.mouseInteractionHandler.getMouseClickedY() : 0.0f);
        poseStack.translate(interactionOffsetX, interactionOffsetY, 0.0f);
        float interactionPitch = this.mouseInteractionHandler.getRotationPitch() - (this.mouseInteractionHandler.isInteracting() && this.mouseInteractionHandler.isRotating() ? (float)mouseY - this.mouseInteractionHandler.getMouseClickedY() : 0.0f) - 30.0f;
        float interactionYaw = this.mouseInteractionHandler.getRotationYaw() + (this.mouseInteractionHandler.isInteracting() && this.mouseInteractionHandler.isRotating() ? (float)mouseX - this.mouseInteractionHandler.getMouseClickedX() : 0.0f) + 150.0f;
        poseStack.mulPose(new Quaternionf().rotationXYZ(interactionPitch * ((float)Math.PI / 180), interactionYaw * ((float)Math.PI / 180), 0.0f));
    }

    public String getSelectedAttachmentPath() {
        return this.selectedAttachmentPath;
    }

    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        AttachmentSlot attachmentSlot;
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, 250.0f);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        int left = this.getLeft();
        int top = this.getTop();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(GUI_TEXTURES, left, top, 1000, 0.0f, 0.0f, 176, 90, 256, 256);
        int attLeft = this.leftPos + 5;
        int attTop = this.topPos + 8;
        int slotTextureLeftOffset = 176;
        int unavailableSlotTextureTopOffset = 36;
        SimpleAttachmentContainer childContainer = null;
        Slot slot = this.hoveredSlot;
        if (slot instanceof HierarchicalSlot) {
            HierarchicalSlot hSlot = (HierarchicalSlot)slot;
            this.selectedAttachmentPath = hSlot.getPath();
        }
        if ((slot = this.hoveredSlot) instanceof AttachmentSlot && (childContainer = (attachmentSlot = (AttachmentSlot)slot).getChildContainer()) != null) {
            int childContainerIndex = childContainer.getContainerIndex();
            poseStack.pushPose();
            int elementsCount = childContainer.getVirtualInventory().getElements().size();
            guiGraphics.renderOutline(attLeft + childContainerIndex * 22 - 2, attTop - 2, 22, 24 + 18 * elementsCount, -1061093377);
            poseStack.popPose();
        }
        SimpleAttachmentContainer[] attachmentContainers = this.menu.getAttachmentContainers();
        for (int i = 0; i < attachmentContainers.length; ++i) {
            SimpleAttachmentContainer attachmentContainer = attachmentContainers[i];
            int availableSlotTextureTopOffset = i == 0 ? 0 : 18;
            int containerStartIndex = SimpleAttachmentContainer.getContainerStartIndex(attachmentContainers, i);
            if (attachmentContainer.getVirtualInventory() == null || attachmentContainer.getVirtualInventory().getElements().isEmpty()) continue;
            if (this.menu.getSlot(containerStartIndex).isActive()) {
                guiGraphics.blit(GUI_TEXTURES, attLeft + i * 22, attTop, 1000, (float)slotTextureLeftOffset, 54.0f, 18, 18, 256, 256);
            }
            for (int j = 1; j < attachmentContainer.getContainerSize(); ++j) {
                int adjustedSlotIndex = containerStartIndex + j;
                AttachmentSlot slot2 = (AttachmentSlot)this.menu.getSlot(adjustedSlotIndex);
                if (!slot2.isActive()) continue;
                int textureTopOffset = this.menu.getCarried() == null || this.menu.getCarried().isEmpty() || slot2.mayPlace(this.menu.getCarried()) ? availableSlotTextureTopOffset : unavailableSlotTextureTopOffset;
                guiGraphics.blit(GUI_TEXTURES, attLeft + i * 22, attTop + this.headerBottomPadding + j * 18, 1000, (float)slotTextureLeftOffset, (float)textureTopOffset, 18, 18, 256, 256);
            }
        }
        poseStack.popPose();
        poseStack.pushPose();
        this.renderItemInHand(guiGraphics, mouseX, mouseY);
        this.selectedAttachmentPath = null;
        poseStack.popPose();
        RenderSystem.disableBlend();
        poseStack.translate(0.0f, 0.0f, 2000.0f);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.mouseInteractionHandler.onMouseScrolled(mouseX, mouseY, scrollY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.mouseInteractionHandler.onMouseButtonClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.mouseInteractionHandler.onMouseButtonReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
    }

    private int getLeft() {
        return (this.width - 176) / 2;
    }

    private int getTop() {
        return (this.height - 90) / 2 + 74;
    }

    private boolean isMouseInScreen(double mouseX, double mouseY) {
        boolean isMouseInInventory;
        int width = 176;
        int left = this.getLeft();
        int top = this.getTop();
        boolean bl = isMouseInInventory = mouseX >= (double)left && mouseX <= (double)(left + width) && mouseY >= (double)top && mouseY <= (double)(top + 90);
        if (isMouseInInventory) {
            return false;
        }
        int attLeft = this.leftPos + 5;
        int attTop = this.topPos + 8;
        boolean isMouseInAttachmentContainer = false;
        SimpleAttachmentContainer[] attachmentContainers = this.menu.getAttachmentContainers();
        for (int i = 0; i < attachmentContainers.length; ++i) {
            SimpleAttachmentContainer attachmentContainer = attachmentContainers[i];
            int containerStartIndex = SimpleAttachmentContainer.getContainerStartIndex(attachmentContainers, i);
            if (attachmentContainer.getVirtualInventory() == null || attachmentContainer.getVirtualInventory().getElements().isEmpty()) continue;
            int slotLeft = attLeft + i * 22;
            int slotRight = slotLeft + 18 + 4;
            if (mouseX < (double)slotLeft || mouseX > (double)slotRight || mouseY < (double)attTop) continue;
            int yBottom = 0;
            for (int j = 1; j < attachmentContainer.getContainerSize(); ++j) {
                int adjustedSlotIndex = containerStartIndex + j;
                AttachmentSlot slot = (AttachmentSlot)this.menu.getSlot(adjustedSlotIndex);
                if (slot.isActive()) continue;
                yBottom = attTop + this.headerBottomPadding + j * 18;
                break;
            }
            if (!(mouseY < (double)yBottom)) continue;
            isMouseInAttachmentContainer = true;
            break;
        }
        return !isMouseInAttachmentContainer;
    }

    public void onClose() {
        this.selectedAttachmentPath = null;
        Platform.getInstance().getEventBus().removeListener(AttachmentAddedEvent.class, this.attachmentAddedListener);
        Platform.getInstance().getEventBus().removeListener(AttachmentRemovedEvent.class, this.attachmentRemovedListener);
        super.onClose();
    }

    public void beforeRenderingSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot instanceof AttachmentSlot) {
            return;
        }
        if (this.menu.getPlayerInventory().selected == 9 - (this.menu.slots.size() - slot.index)) {
            guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 1348235500);
        }
        if (slot.index >= this.menu.getTotalAttachmentSlots() && this.mayPlaceAttachment(slot.getItem())) {
            guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, -1605328816);
        }
    }

    private boolean mayPlaceAttachment(ItemStack attachmentStack) {
        if (attachmentStack.isEmpty() || !(attachmentStack.getItem() instanceof Attachment)) {
            return false;
        }
        SimpleAttachmentContainer[] attachmentContainers = this.menu.getAttachmentContainers();
        for (int i = 0; i < attachmentContainers.length; ++i) {
            SimpleAttachmentContainer attachmentContainer = attachmentContainers[i];
            int containerStartIndex = SimpleAttachmentContainer.getContainerStartIndex(attachmentContainers, i);
            if (attachmentContainer.getVirtualInventory() == null || attachmentContainer.getVirtualInventory().getElements().isEmpty()) continue;
            for (int j = 1; j < attachmentContainer.getContainerSize(); ++j) {
                int adjustedSlotIndex = containerStartIndex + j;
                AttachmentSlot slot = (AttachmentSlot)this.menu.getSlot(adjustedSlotIndex);
                if (!slot.isActive() || !slot.mayPlace(attachmentStack)) continue;
                return true;
            }
        }
        return false;
    }

    public Pair<RenderType, Vector4f> getRenderTypeOverride(ItemStack baseItemStack, ItemStack attachmentItemStack, String attachmentName, String attachmentPath) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)("textures/item/" + attachmentName + ".png"));
        AttachmentHighlightEvent attachmentHighlight = this.attachmentEventQueue.peek();
        if (attachmentHighlight != null && ItemStack.isSameItem((ItemStack)attachmentItemStack, (ItemStack)attachmentHighlight.attachmentStack)) {
            if (attachmentHighlight.isHighlighted()) {
                RenderType renderType = RenderType.eyes((ResourceLocation)texture);
                return Pair.of((Object)renderType, (Object)COLOR_GREEN);
            }
            return null;
        }
        String selectedAttachmentPath = this.getSelectedAttachmentPath();
        if (Objects.equals(attachmentPath, selectedAttachmentPath)) {
            RenderType renderType = RenderType.eyes((ResourceLocation)texture);
            return Pair.of((Object)renderType, (Object)COLOR_GREEN);
        }
        if (selectedAttachmentPath != null && selectedAttachmentPath.endsWith("/") && attachmentPath.startsWith(selectedAttachmentPath.substring(0, selectedAttachmentPath.length() - 1))) {
            RenderType renderType = RenderType.eyes((ResourceLocation)texture);
            return Pair.of((Object)renderType, (Object)COLOR_GREEN);
        }
        return null;
    }

    public void onAttachmentAdded(AttachmentAddedEvent event) {
        Level level = MiscUtil.getLevel((Entity)event.getPlayer());
        if (level.isClientSide) {
            if (this.minecraft.screen != this) {
                return;
            }
            ClientUtil.getClientPlayer().playSound(SoundRegistry.ATTACHMENT_ADDED.get(), 1.0f, 1.0f);
            this.attachmentEventQueue.add(new AttachmentHighlightEvent(System.currentTimeMillis(), 750L, event.getParentStack(), event.getAttachmentStack()));
        }
    }

    public void onAttachmentRemoved(AttachmentRemovedEvent event) {
        Level level = MiscUtil.getLevel((Entity)event.getPlayer());
        if (level.isClientSide) {
            if (this.minecraft.screen != this) {
                return;
            }
            ClientUtil.getClientPlayer().playSound(SoundRegistry.ATTACHMENT_REMOVED.get(), 1.0f, 1.0f);
            this.attachmentEventQueue.add(new AttachmentHighlightEvent(System.currentTimeMillis(), 750L, event.getParentStack(), event.getAttachmentStack()));
        }
    }

    private static class AttachmentHighlightEvent {
        private long startTime;
        private long duration;
        private ItemStack parentStack;
        private ItemStack attachmentStack;
        private long blinkInterval;

        public AttachmentHighlightEvent(long startTime, long duration, ItemStack parentStack, ItemStack attachmentStack) {
            this.startTime = startTime;
            this.duration = duration;
            this.parentStack = parentStack;
            this.attachmentStack = attachmentStack;
            this.blinkInterval = 150L;
        }

        public boolean isHighlighted() {
            long elapsedTime = System.currentTimeMillis() - this.startTime;
            if (elapsedTime >= this.duration) {
                return false;
            }
            long k = elapsedTime / this.blinkInterval;
            return k % 2L == 0L;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= this.startTime + this.duration;
        }
    }
}

