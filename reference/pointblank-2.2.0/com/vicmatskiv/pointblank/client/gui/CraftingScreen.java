/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.inventory.CreativeInventoryListener
 *  net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.ContainerListener
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.TooltipFlag$Default
 *  net.minecraft.world.level.Level
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 */
package com.vicmatskiv.pointblank.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vicmatskiv.pointblank.Enableable;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.client.gui.CustomButton;
import com.vicmatskiv.pointblank.client.render.RenderUtil;
import com.vicmatskiv.pointblank.client.uv.PlayOnceSpriteUVProvider;
import com.vicmatskiv.pointblank.client.uv.SpriteUVProvider;
import com.vicmatskiv.pointblank.crafting.Craftable;
import com.vicmatskiv.pointblank.crafting.PointBlankRecipe;
import com.vicmatskiv.pointblank.inventory.CraftingContainerMenu;
import com.vicmatskiv.pointblank.inventory.IngredientSlot;
import com.vicmatskiv.pointblank.inventory.SearchSlot;
import com.vicmatskiv.pointblank.network.CraftingRequestPacket;
import com.vicmatskiv.pointblank.registry.SoundRegistry;
import com.vicmatskiv.pointblank.util.CancellableSound;
import com.vicmatskiv.pointblank.util.ClientUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import com.vicmatskiv.pointblank.util.StateMachine;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.CreativeInventoryListener;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

public class CraftingScreen
extends EffectRenderingInventoryScreen<CraftingContainerMenu> {
    public static final int CELL_WIDTH = 18;
    public static final int CELL_HEIGHT = 18;
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/gui/craftnew.png");
    private static final ResourceLocation GLOW_OUTLINE = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/gui/glow_outline.png");
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace((String)"container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace((String)"container/creative_inventory/scroller_disabled");
    private StateMachine<CraftingState, Context> stateMachine;
    private float scrollOffs;
    private boolean scrolling;
    private EditBox searchBox;
    private CustomButton craftButton;
    private CreativeInventoryListener listener;
    private boolean ignoreTextInput;
    private boolean hasClickedOutside;
    private final Set<TagKey<Item>> visibleTags = new HashSet<TagKey<Item>>();
    private int scrollbarXOffset = 121;
    private int scrollbarYOffset = 18;
    private int scrollbarWidth = 14;
    private int scrollbarHeight = 126;
    private ItemStack selectedItem;
    private PointBlankRecipe selectedItemRecipe;
    private long craftingStartTime;
    private long craftingCompletedCooldownStartTime;
    private long craftingCompletedCooldownDuration = 2000L;
    private float itemRotationAngleDegrees;
    private float itemRotationAngleDegreesPerTick;
    private float idleItemRotationsPerSecond = 0.1f;
    private SpriteUVProvider glowOutlineSpriteUVProvider;

    public CraftingScreen(CraftingContainerMenu menu, Inventory playerInventory, Component title) {
        super((AbstractContainerMenu)menu, playerInventory, title);
        this.stateMachine = this.createStateMachine();
        playerInventory.player.containerMenu = this.menu;
        this.imageWidth = 312;
        this.imageHeight = 151;
        this.itemRotationAngleDegreesPerTick = CraftingScreen.rotationsPerSecondToDegress(this.idleItemRotationsPerSecond);
        this.craftingCompletedCooldownDuration = 600L;
        this.glowOutlineSpriteUVProvider = new PlayOnceSpriteUVProvider(6, 6, 50, 600L);
    }

    private static float rotationsPerSecondToDegress(float rotationsPerSecond) {
        return 360.0f * rotationsPerSecond * 0.05f;
    }

    private StateMachine<CraftingState, Context> createStateMachine() {
        StateMachine.Builder<CraftingState, Context> builder = new StateMachine.Builder<CraftingState, Context>();
        builder.withTransition(CraftingState.IDLE, CraftingState.CRAFTING, context -> {
            Enableable en;
            Item patt0$temp;
            return this.selectedItem != null && this.selectedItem.getItem() instanceof Craftable && this.selectedItemRecipe != null && (!((patt0$temp = this.selectedItem.getItem()) instanceof Enableable) || (en = (Enableable)patt0$temp).isEnabled()) && ((CraftingContainerMenu)this.menu).isIdle() && this.craftButton.isPressed();
        }, StateMachine.TransitionMode.EVENT, null, this::actionStartCrafting);
        builder.withTransition(CraftingState.CRAFTING, CraftingState.IDLE, context -> !this.craftButton.isPressed(), StateMachine.TransitionMode.AUTO, null, this::actionCancelCrafting);
        builder.withTransition(CraftingState.CRAFTING, CraftingState.IDLE, context -> true, StateMachine.TransitionMode.EVENT, null, null);
        builder.withTransition(CraftingState.CRAFTING, CraftingState.CRAFTING_COMPLETED, ctx -> System.currentTimeMillis() - this.craftingStartTime >= ((Craftable)this.selectedItem.getItem()).getCraftingDuration(), StateMachine.TransitionMode.AUTO, null, this::actionCraftingCompleted);
        builder.withTransition(CraftingState.CRAFTING_COMPLETED, CraftingState.CRAFTING_COMPLETED_COOLDOWN, context -> true, StateMachine.TransitionMode.AUTO, null, (ctx, f, t) -> {
            this.craftingCompletedCooldownStartTime = System.currentTimeMillis();
        });
        builder.withTransition(CraftingState.CRAFTING_COMPLETED_COOLDOWN, CraftingState.IDLE, ctx -> System.currentTimeMillis() - this.craftingCompletedCooldownStartTime >= this.craftingCompletedCooldownDuration, StateMachine.TransitionMode.AUTO, null, null);
        builder.withOnSetStateAction(CraftingState.IDLE, this::actionIdle);
        return builder.build(CraftingState.IDLE);
    }

    private void actionIdle(Context context, CraftingState fromState, CraftingState toState) {
        this.craftButton.setFocused(false);
        this.itemRotationAngleDegreesPerTick = CraftingScreen.rotationsPerSecondToDegress(this.idleItemRotationsPerSecond);
    }

    private void actionStartCrafting(Context context, CraftingState fromState, CraftingState toState) {
        this.craftingStartTime = System.currentTimeMillis();
        Player player = ClientUtil.getClientPlayer();
        this.minecraft.getSoundManager().play((SoundInstance)new CancellableSound(player, SoundRegistry.CRAFTING_IN_PROGRESS.get(), player.getSoundSource(), player.getRandom(), s -> this.minecraft.screen == this && this.stateMachine.getCurrentState() == CraftingState.CRAFTING));
        Platform.getInstance().getNetworkService().sendToServer(new CraftingRequestPacket(CraftingRequestPacket.RequestType.START_CRAFTING, this.selectedItemRecipe.getId(ClientUtil.getClientLevel())));
        this.craftButton.setTooltip(null);
    }

    public void onClose() {
        if (this.stateMachine.getCurrentState() == CraftingState.CRAFTING && this.selectedItemRecipe != null) {
            Platform.getInstance().getNetworkService().sendToServer(new CraftingRequestPacket(CraftingRequestPacket.RequestType.CANCEL_CRAFTING, this.selectedItemRecipe.getId(ClientUtil.getClientLevel())));
        }
        super.onClose();
    }

    private void actionCancelCrafting(Context context, CraftingState fromState, CraftingState toState) {
        Platform.getInstance().getNetworkService().sendToServer(new CraftingRequestPacket(CraftingRequestPacket.RequestType.CANCEL_CRAFTING, this.selectedItemRecipe.getId(ClientUtil.getClientLevel())));
    }

    private void actionCraftingCompleted(Context context, CraftingState fromState, CraftingState toState) {
        ClientUtil.getClientPlayer().playSound(SoundRegistry.CRAFTING_COMPLETED.get(), 1.0f, 1.0f);
    }

    private float getCraftingProgress() {
        if (this.stateMachine.getCurrentState() != CraftingState.CRAFTING) {
            return 0.0f;
        }
        return Mth.clamp((float)((float)(System.currentTimeMillis() - this.craftingStartTime) / (float)((Craftable)this.selectedItem.getItem()).getCraftingDuration()), (float)0.0f, (float)1.0f);
    }

    private float getCraftingCompletedCooldownProgress() {
        if (this.stateMachine.getCurrentState() != CraftingState.CRAFTING_COMPLETED_COOLDOWN) {
            return 0.0f;
        }
        return Mth.clamp((float)((float)(System.currentTimeMillis() - this.craftingCompletedCooldownStartTime) / (float)this.craftingCompletedCooldownDuration), (float)0.0f, (float)1.0f);
    }

    private void refreshContents() {
        this.updateIngredientSlots();
    }

    public void containerTick() {
        if (this.minecraft != null) {
            Context context = new Context(this);
            this.stateMachine.update(context);
            this.craftButton.active = this.selectedItem != null && (((CraftingContainerMenu)this.menu).isIdle() || ((CraftingContainerMenu)this.menu).isCrafting()) && (this.stateMachine.getCurrentState() == CraftingState.IDLE || this.stateMachine.getCurrentState() == CraftingState.CRAFTING && this.craftButton.isPressed());
            this.itemRotationAngleDegrees += this.itemRotationAngleDegreesPerTick;
            if (this.minecraft.player != null) {
                this.refreshContents();
            }
        }
    }

    protected void slotClicked(Slot slot, int slotIndex, int mouseButton, ClickType clickType) {
        if (((CraftingContainerMenu)this.menu).isIdle() && ((CraftingContainerMenu)this.menu).isCreativeSlot(slot)) {
            this.searchBox.moveCursorToEnd(true);
            this.searchBox.setHighlightPos(0);
            this.craftButton.setFocused(false);
            this.craftButton.active = false;
            this.onSelectCraftableItem(slot.getItem());
        }
    }

    private void onSelectCraftableItem(ItemStack itemStack) {
        this.craftButton.active = false;
        this.selectedItem = itemStack;
        ((CraftingContainerMenu)this.menu).clearIngredientSlots();
        ClientUtil.getClientPlayer().playSound(SoundRegistry.CRAFTING_ITEM_SELECTED.get(), 1.0f, 1.0f);
        if (this.selectedItem != null && !this.selectedItem.isEmpty() && this.selectedItem.getItem() instanceof Craftable) {
            this.selectedItemRecipe = PointBlankRecipe.getRecipe(ClientUtil.getClientLevel(), itemStack.getItem());
            if (this.selectedItemRecipe != null) {
                this.updateIngredientSlots();
            }
        } else {
            this.selectedItemRecipe = null;
        }
    }

    private void updateIngredientSlots() {
        if (this.selectedItemRecipe == null) {
            this.craftButton.active = false;
            return;
        }
        this.craftButton.active = this.stateMachine.getCurrentState() == CraftingState.IDLE && ((CraftingContainerMenu)this.menu).updateIngredientSlots(this.selectedItemRecipe);
        this.craftButton.active = !(this.selectedItem == null || !((CraftingContainerMenu)this.menu).isIdle() && !((CraftingContainerMenu)this.menu).isCrafting() || this.stateMachine.getCurrentState() != CraftingState.IDLE && this.stateMachine.getCurrentState() != CraftingState.CRAFTING || !((CraftingContainerMenu)this.menu).updateIngredientSlots(this.selectedItemRecipe));
    }

    protected void init() {
        super.init();
        ((CraftingContainerMenu)this.menu).clearIngredientSlots();
        int searchBoxLeftOffset = 46;
        this.searchBox = new EditBox(this.font, this.leftPos + searchBoxLeftOffset, this.topPos + 6, 80, 9, (Component)Component.translatable((String)"itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setVisible(false);
        this.searchBox.setTextColor(0xFFFFFF);
        this.addWidget((GuiEventListener)this.searchBox);
        this.clearDraggingState();
        this.searchBox.setVisible(true);
        this.searchBox.setCanLoseFocus(false);
        this.searchBox.setFocused(true);
        this.searchBox.setWidth(89);
        this.searchBox.setX(this.leftPos + (searchBoxLeftOffset + 89) - this.searchBox.getWidth());
        this.craftButton = CustomButton.builder((Component)Component.translatable((String)"label.pointblank.craft"), b -> {
            Context context = new Context(this);
            this.stateMachine.setState(context, CraftingState.CRAFTING);
        }).onRelease(b -> {}).bounds(this.leftPos + 256, this.topPos + 115, 46, 20).progressProvider(this::getCraftingProgress).tooltip(Tooltip.create((Component)Component.translatable((String)"message.pointblank.press_and_hold_to_craft"))).build();
        this.craftButton.setFocused(false);
        this.craftButton.active = false;
        this.addRenderableWidget((GuiEventListener)this.craftButton);
        this.refreshSearchResults();
        this.scrollOffs = 0.0f;
        ((CraftingContainerMenu)this.menu).scrollTo(0.0f);
        this.selectedItem = null;
        this.selectedItemRecipe = null;
        this.minecraft.player.inventoryMenu.removeSlotListener((ContainerListener)this.listener);
        this.listener = new CreativeInventoryListener(this.minecraft);
        this.minecraft.player.inventoryMenu.addSlotListener((ContainerListener)this.listener);
    }

    public void resize(Minecraft minecraft, int width, int height) {
        int i = ((CraftingContainerMenu)this.menu).getRowIndexForScroll(this.scrollOffs);
        String s = this.searchBox.getValue();
        ItemStack currentSelectedItem = this.selectedItem;
        this.init(minecraft, width, height);
        this.searchBox.setValue(s);
        if (!this.searchBox.getValue().isEmpty()) {
            this.refreshSearchResults();
        }
        this.onSelectCraftableItem(currentSelectedItem);
        this.scrollOffs = ((CraftingContainerMenu)this.menu).getScrollForRowIndex(i);
        ((CraftingContainerMenu)this.menu).scrollTo(this.scrollOffs);
    }

    public void removed() {
        super.removed();
        if (this.minecraft.player != null && this.minecraft.player.getInventory() != null) {
            this.minecraft.player.inventoryMenu.removeSlotListener((ContainerListener)this.listener);
        }
    }

    public boolean charTyped(char p_98521_, int p_98522_) {
        if (this.ignoreTextInput) {
            return false;
        }
        String s = this.searchBox.getValue();
        if (this.searchBox.charTyped(p_98521_, p_98522_)) {
            if (!Objects.equals(s, this.searchBox.getValue())) {
                this.refreshSearchResults();
            }
            return true;
        }
        return false;
    }

    public boolean keyPressed(int p_98547_, int p_98548_, int p_98549_) {
        this.ignoreTextInput = false;
        boolean flag = !((CraftingContainerMenu)this.menu).isCreativeSlot(this.hoveredSlot) || this.hoveredSlot.hasItem();
        boolean flag1 = InputConstants.getKey((int)p_98547_, (int)p_98548_).getNumericKeyValue().isPresent();
        if (flag && flag1 && this.checkHotbarKeyPressed(p_98547_, p_98548_)) {
            this.ignoreTextInput = true;
            return true;
        }
        String s = this.searchBox.getValue();
        if (this.searchBox.keyPressed(p_98547_, p_98548_, p_98549_)) {
            if (!Objects.equals(s, this.searchBox.getValue())) {
                this.refreshSearchResults();
            }
            return true;
        }
        return this.searchBox.isFocused() && this.searchBox.isVisible() && p_98547_ != 256 ? true : super.keyPressed(p_98547_, p_98548_, p_98549_);
    }

    public boolean keyReleased(int p_98612_, int p_98613_, int p_98614_) {
        this.ignoreTextInput = false;
        return super.keyReleased(p_98612_, p_98613_, p_98614_);
    }

    private void refreshSearchResults() {
        ((CraftingContainerMenu)this.menu).refreshSearchResults(this.searchBox.getValue());
        this.scrollOffs = 0.0f;
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        MutableComponent label = this.selectedItem != null && !this.selectedItem.isEmpty() ? this.selectedItem.getItem().getName(this.selectedItem) : Component.translatable((String)"label.pointblank.craft");
        guiGraphics.drawCenteredString(this.font, (Component)label, 225, 8, 0xFFFF00);
    }

    public boolean mouseClicked(double posX, double posY, int mouseButton) {
        if (mouseButton == 0 && this.insideScrollbar(posX, posY)) {
            this.scrolling = this.canScroll();
            return true;
        }
        return super.mouseClicked(posX, posY, mouseButton);
    }

    public boolean mouseReleased(double posX, double posY, int mouseButton) {
        if (this.craftButton.isPressed()) {
            this.craftButton.release();
        }
        if (mouseButton == 0) {
            this.scrolling = false;
        }
        return super.mouseReleased(posX, posY, mouseButton);
    }

    private boolean canScroll() {
        return ((CraftingContainerMenu)this.menu).canScroll();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.canScroll()) {
            return false;
        }
        this.scrollOffs = ((CraftingContainerMenu)this.menu).subtractInputFromScroll(this.scrollOffs, scrollY);
        ((CraftingContainerMenu)this.menu).scrollTo(this.scrollOffs);
        return true;
    }

    protected boolean hasClickedOutside(double mouseX, double mouseY, int p_98543_, int p_98544_, int mouseButton) {
        this.hasClickedOutside = mouseX < (double)p_98543_ || mouseY < (double)p_98544_ || mouseX >= (double)(p_98543_ + this.imageWidth) || mouseY >= (double)(p_98544_ + this.imageHeight);
        return this.hasClickedOutside;
    }

    protected boolean insideScrollbar(double mouseX, double mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        int k = i + this.scrollbarXOffset;
        int l = j + this.scrollbarYOffset;
        int i1 = k + this.scrollbarWidth;
        int j1 = l + this.scrollbarHeight;
        boolean inside = mouseX >= (double)k && mouseY >= (double)l && mouseX < (double)i1 && mouseY < (double)j1;
        return inside;
    }

    public boolean mouseDragged(double p_98535_, double p_98536_, int p_98537_, double p_98538_, double p_98539_) {
        if (this.scrolling) {
            int i = this.topPos + 18;
            int j = i + 112;
            this.scrollOffs = ((float)p_98536_ - (float)i - 7.5f) / ((float)(j - i) - 15.0f);
            this.scrollOffs = Mth.clamp((float)this.scrollOffs, (float)0.0f, (float)1.0f);
            ((CraftingContainerMenu)this.menu).scrollTo(this.scrollOffs);
            return true;
        }
        return super.mouseDragged(p_98535_, p_98536_, p_98537_, p_98538_, p_98539_);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderItemInHand(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.selectedItem == null) {
            return;
        }
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        guiGraphics.enableScissor(left + 151, top + 23, left + 300, top + 98);
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        float itemRotationAngleDegreesWithPartial = this.itemRotationAngleDegrees;
        float yOffset = 0.0f;
        float zoom = 1.0f;
        float partialTick = this.minecraft.getTimer().getGameTimeDeltaPartialTick(true);
        yOffset = Mth.cos((float)((itemRotationAngleDegreesWithPartial += this.itemRotationAngleDegreesPerTick * partialTick) * (float)Math.PI * 0.02f));
        if (this.stateMachine.getCurrentState() == CraftingState.CRAFTING_COMPLETED_COOLDOWN) {
            zoom += 0.2f * Mth.sin((float)(this.getCraftingCompletedCooldownProgress() * (float)Math.PI * 2.0f + (float)Math.PI));
        }
        poseStack.translate((float)(left + 230), (float)(this.height / 2 - 18) + yOffset * 2.0f, 100.0f);
        float interactionPitch = -30.0f;
        float interactionYaw = 150.0f;
        poseStack.mulPose(new Quaternionf().rotationXYZ(interactionPitch * ((float)Math.PI / 180), interactionYaw * ((float)Math.PI / 180), 0.0f));
        poseStack.scale(zoom, zoom, zoom);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f + itemRotationAngleDegreesWithPartial));
        poseStack.mulPose(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
        poseStack.scale(80.0f, 80.0f, 80.0f);
        Matrix4fStack modelStack = RenderSystem.getModelViewStack();
        modelStack.pushMatrix();
        modelStack.mul((Matrix4fc)poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();
        MultiBufferSource.BufferSource buffer = guiGraphics.bufferSource();
        BakedModel model = this.minecraft.getItemRenderer().getModel(this.selectedItem, MiscUtil.getLevel((Entity)this.minecraft.player), (LivingEntity)this.minecraft.player, this.minecraft.player.getId() + ItemDisplayContext.GROUND.ordinal());
        this.minecraft.getItemRenderer().render(this.selectedItem, ItemDisplayContext.GROUND, false, new PoseStack(), (MultiBufferSource)buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, model);
        guiGraphics.flush();
        modelStack.popMatrix();
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
        guiGraphics.disableScissor();
    }

    public List<Component> getTooltipFromContainerItem(ItemStack itemStack) {
        boolean flag = this.hoveredSlot != null && this.hoveredSlot instanceof SearchSlot;
        boolean flag2 = true;
        TooltipFlag.Default tooltipflag$default = this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        TooltipFlag.Default tooltipflag = flag ? tooltipflag$default.asCreative() : tooltipflag$default;
        List list = itemStack.getTooltipLines(Item.TooltipContext.of((Level)ClientUtil.getClientLevel()), (Player)this.minecraft.player, (TooltipFlag)tooltipflag);
        ArrayList tooltipComponents = Lists.newArrayList((Iterable)list);
        if (flag2 && flag) {
            this.visibleTags.forEach(p_205407_ -> {
                if (itemStack.is(p_205407_)) {
                    tooltipComponents.add(1, Component.literal((String)("#" + String.valueOf(p_205407_.location()))).withStyle(ChatFormatting.DARK_PURPLE));
                }
            });
        }
        return tooltipComponents;
    }

    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 328, 328);
        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
        int j = this.leftPos + this.scrollbarXOffset;
        int k = this.topPos + this.scrollbarYOffset;
        int i = k + this.scrollbarHeight;
        ResourceLocation resourcelocation = this.canScroll() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        guiGraphics.blitSprite(resourcelocation, j, k + (int)((float)(i - k - 17) * this.scrollOffs), 12, 15);
        int ingredientSlotOffset = CraftingContainerMenu.SEARCH_CONTAINER.getContainerSize();
        for (i = 0; i < CraftingContainerMenu.INGREDIENT_CONTAINER.getContainerSize(); ++i) {
            IngredientSlot ingredientSlot = (IngredientSlot)((Object)((CraftingContainerMenu)this.menu).slots.get(ingredientSlotOffset + i));
            if (ingredientSlot.isIngredientAvailable()) continue;
            guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos + ingredientSlot.x - 1, this.topPos + ingredientSlot.y - 1, 48.0f, 151.0f, 18, 18, 328, 328);
        }
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        if (this.stateMachine.getCurrentState() == CraftingState.CRAFTING) {
            int topOffset = 98 - (int)(75.0f * this.getCraftingProgress());
            guiGraphics.fillGradient(left + 151, top + topOffset, left + 300, top + 98, 0x1000FF00, 0x5000FF00);
        }
        if (this.stateMachine.getCurrentState() == CraftingState.CRAFTING_COMPLETED_COOLDOWN) {
            float progress = this.getCraftingCompletedCooldownProgress();
            int minAlpha = 32;
            int maxAlpha = 96;
            int alpha1 = (int)((float)minAlpha - (float)minAlpha * progress);
            int alpha2 = (int)((float)maxAlpha - (float)maxAlpha * progress);
            guiGraphics.fillGradient(left + 151, top + 23, left + 300, top + 98, 0xFF00 | alpha1 << 24, 0xFF00 | alpha2 << 24);
            float[] spriteUV = this.glowOutlineSpriteUVProvider.getSpriteUV(progress);
            if (spriteUV != null) {
                float minU = spriteUV[0];
                float minV = spriteUV[1];
                float maxU = spriteUV[2];
                float maxV = spriteUV[3];
                RenderUtil.blit(guiGraphics, GLOW_OUTLINE, left + 139, left + 314, top - 30, top + 145, 0, minU, maxU, minV, maxV, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
        RenderSystem.disableBlend();
        this.renderItemInHand(guiGraphics, mouseX, mouseY);
    }

    public void cancelCrafting() {
        this.stateMachine.setState(new Context(this), CraftingState.IDLE);
    }

    public static enum CraftingState {
        IDLE,
        CRAFTING,
        CRAFTING_COMPLETED,
        CRAFTING_COMPLETED_COOLDOWN;

    }

    private class Context {
        private Context(CraftingScreen craftingScreen) {
        }
    }
}

