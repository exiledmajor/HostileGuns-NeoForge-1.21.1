/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  java.lang.MatchException
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.render.RenderUtil;
import com.vicmatskiv.pointblank.feature.OverheatFeature;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.registry.AmmoUiDefinition;
import com.vicmatskiv.pointblank.registry.AmmoUiRegistry;
import com.vicmatskiv.pointblank.registry.HeatUiDefinition;
import com.vicmatskiv.pointblank.registry.HeatUiRegistry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GunItemOverlay {
    private static final int COUNTER_COLOR = -1;
    private static final int FIRE_MODE_COLOR = 8040160;
    private static final int OUTLINE_COLOR = 0;
    private static final ResourceLocation OVERLAY_RESOURCE = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/gui/ammo.png");
    private static final Map<String, UiInertiaState> AMMO_UI_INERTIA_STATES = new HashMap<String, UiInertiaState>();
    private static final UiInertiaState HEAT_UI_INERTIA = new UiInertiaState();
    private static long overlayRenderFrame;

    public static void beginOverlayRenderFrame() {
        ++overlayRenderFrame;
    }

    public static void renderGunOverlay(GuiGraphics guiGraphics, ItemStack stack) {
        int messageColor;
        int slotIndex;
        if (!Config.ammoIndicatorEnabled) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        boolean isOffhand = (slotIndex = GunItem.resolveSlotIndex((Player)mc.player, stack)) == 40;
        GunClientState gunClientState = GunClientState.getState((Player)mc.player, stack, slotIndex, isOffhand);
        if (gunClientState == null) {
            return;
        }
        Component message = gunClientState.getCurrentMessage();
        if (message != null) {
            messageColor = 0xDDFF00;
        } else {
            FireModeInstance fireModeInstance = GunItem.getFireModeInstance(stack);
            int currentAmmo = gunClientState.getAmmoCount(fireModeInstance);
            GunItem gunItem = (GunItem)stack.getItem();
            int maxAmmo = gunItem.getMaxAmmoCapacity(stack, fireModeInstance);
            message = maxAmmo == Integer.MAX_VALUE ? Component.literal((String)"\u221e") : Component.literal((String)String.format("%d/%d", currentAmmo, maxAmmo));
            messageColor = 0xFFFF00;
        }
        Font font = mc.font;
        int screenX = mc.getWindow().getGuiScaledWidth();
        GunItemOverlay.drawOverlayString(guiGraphics, font, message, (float)(screenX - font.width((FormattedText)message) - 10), 10.0f, messageColor, false);
    }

    public static void renderGunOverlay2(GuiGraphics guiGraphics, ItemStack stack) {
        GunItemOverlay.renderGunOverlay2(guiGraphics, stack, null);
    }

    public static void renderGunOverlayAtPlacement(GuiGraphics guiGraphics, ItemStack stack, Config.AmmoIndicatorPlacement placement) {
        GunItemOverlay.renderGunOverlay2(guiGraphics, stack, placement);
    }

    private static void renderGunOverlay2(GuiGraphics guiGraphics, ItemStack stack, Config.AmmoIndicatorPlacement forcedPlacement) {
        int slotIndex;
        int textureWidth = 160;
        int textureHeight = 32;
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        Minecraft mc = Minecraft.getInstance();
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(stack);
        if (fireModeInstance == null) {
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            return;
        }
        String fireModeDisplayName = fireModeInstance.getDisplayName().getString();
        boolean isOffhand = (slotIndex = GunItem.resolveSlotIndex((Player)mc.player, stack)) == 40;
        GunClientState gunClientState = GunClientState.getState((Player)mc.player, stack, slotIndex, isOffhand);
        if (gunClientState == null) {
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            return;
        }
        int currentAmmo = gunClientState.getAmmoCount(fireModeInstance);
        GunItem gunItem = (GunItem)stack.getItem();
        if (!Config.ammoIndicatorEnabled) {
            GunItemOverlay.renderHeatIndicator(guiGraphics, mc, stack, gunClientState, gunItem, forcedPlacement);
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            return;
        }
        AmmoUiDefinition ammoUiDefinition = AmmoUiRegistry.get(gunItem.getAmmoUi());
        if (ammoUiDefinition != null) {
            boolean mirrorHorizontally = isOffhand && GunItem.hasGunsInMainAndAltSlots((Player)mc.player);
            GunItemOverlay.renderCustomOverlay(guiGraphics, mc, stack, gunClientState, gunItem, fireModeInstance, currentAmmo, fireModeDisplayName, ammoUiDefinition, mirrorHorizontally);
            GunItemOverlay.renderHeatIndicator(guiGraphics, mc, stack, gunClientState, gunItem, forcedPlacement);
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            return;
        }
        int maxAmmo = gunItem.getMaxAmmoCapacity(stack, fireModeInstance);
        String counter = maxAmmo == Integer.MAX_VALUE ? "\u221e" : String.format("%d/%d", currentAmmo, maxAmmo);
        Component message = gunClientState.getCurrentMessage();
        if (message != null) {
            counter = message.getString();
        }
        int textWidth = Math.max(mc.font.width(counter), mc.font.width(fireModeDisplayName));
        int width = 9 + textWidth;
        int height = 22;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        Config.AmmoIndicatorPlacement placement = forcedPlacement != null ? forcedPlacement : (Config.ammoIndicatorPlacement != null ? Config.ammoIndicatorPlacement : Config.AmmoIndicatorPlacement.RIGHT_OF_HOTBAR);
        int hOffset = (screenWidth >> 1) + 97;
        int vOffset = screenHeight - height;
        boolean rightAlignText = false;
        switch (placement) {
            case LEFT_OF_HOTBAR: {
                hOffset = (screenWidth >> 1) - 97 - width;
                rightAlignText = true;
                break;
            }
            case TOP_LEFT: {
                hOffset = 10;
                vOffset = 16;
                break;
            }
            case TOP_RIGHT: {
                hOffset = screenWidth - width - 10;
                vOffset = 16;
                rightAlignText = true;
                break;
            }
            case RIGHT_OF_HOTBAR: {
                hOffset = (screenWidth >> 1) + 97;
            }
        }
        RenderUtil.blitNineSliced(guiGraphics, OVERLAY_RESOURCE, hOffset, vOffset, width, height, 18, 4, textureWidth, textureHeight, 0, 0);
        int textX = rightAlignText ? hOffset + width - 5 : hOffset + 5;
        int counterX = rightAlignText ? textX - mc.font.width(counter) : textX;
        int fireModeX = rightAlignText ? textX - mc.font.width(fireModeDisplayName) : textX;
        GunItemOverlay.drawOutlinedString(guiGraphics, mc.font, counter, counterX, vOffset - 5, -1);
        GunItemOverlay.drawOutlinedString(guiGraphics, mc.font, fireModeDisplayName, fireModeX, vOffset + 7, 8040160);
        GunItemOverlay.renderHeatIndicator(guiGraphics, mc, stack, gunClientState, gunItem, forcedPlacement);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static void renderCustomOverlay(GuiGraphics guiGraphics, Minecraft mc, ItemStack stack, GunClientState gunClientState, GunItem gunItem, FireModeInstance fireModeInstance, int currentAmmo, String fireModeDisplayName, AmmoUiDefinition ammoUiDefinition, boolean mirrorHorizontally) {
        AmmoUiDefinition.TextDefinition fireModeText;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        float hOffset = screenWidth - ammoUiDefinition.width() - ammoUiDefinition.paddingX();
        float vOffset = screenHeight - ammoUiDefinition.height() - ammoUiDefinition.paddingY();
        switch (ammoUiDefinition.screenLocation()) {
            case BOTTOM_LEFT: {
                hOffset = ammoUiDefinition.paddingX();
                vOffset = screenHeight - ammoUiDefinition.height() - ammoUiDefinition.paddingY();
                break;
            }
            case TOP_RIGHT: {
                hOffset = screenWidth - ammoUiDefinition.width() - ammoUiDefinition.paddingX();
                vOffset = ammoUiDefinition.paddingY();
                break;
            }
            case TOP_LEFT: {
                hOffset = ammoUiDefinition.paddingX();
                vOffset = ammoUiDefinition.paddingY();
                break;
            }
            case BOTTOM_RIGHT: {
                hOffset = screenWidth - ammoUiDefinition.width() - ammoUiDefinition.paddingX();
                vOffset = screenHeight - ammoUiDefinition.height() - ammoUiDefinition.paddingY();
            }
        }
        if (mirrorHorizontally) {
            hOffset = (float)screenWidth - hOffset - (float)ammoUiDefinition.width();
        }
        UiInertiaState inertiaState = GunItemOverlay.updateCustomUiInertia(mc, ammoUiDefinition);
        RenderUtil.blit(guiGraphics, ammoUiDefinition.texture(), hOffset, (hOffset += mirrorHorizontally ? -inertiaState.x : inertiaState.x) + (float)ammoUiDefinition.width(), vOffset, (vOffset += inertiaState.y) + (float)ammoUiDefinition.height(), -90.0f, mirrorHorizontally ? 1.0f : 0.0f, mirrorHorizontally ? 0.0f : 1.0f, 0.0f, 1.0f);
        Component message = gunClientState.getCurrentMessage();
        String text = gunClientState.isReloading() ? ammoUiDefinition.reloadText() : (message != null ? message.getString() : GunItemOverlay.formatAmmoText(currentAmmo, gunItem.getMaxAmmoCapacity(stack, fireModeInstance), ammoUiDefinition.ammoText().digitalStyle()));
        AmmoUiDefinition.TextDefinition ammoText = ammoUiDefinition.ammoText();
        int ammoTextColor = currentAmmo <= 0 && !gunClientState.isReloading() && message == null ? ammoText.emptyColor() : ammoText.color();
        GunItemOverlay.drawTextDefinition(guiGraphics, mc.font, text, hOffset, vOffset, ammoUiDefinition.width(), ammoText, ammoTextColor, mirrorHorizontally);
        AmmoUiDefinition.TextDefinition capacityText = ammoUiDefinition.capacityText();
        if (!(capacityText == null || gunClientState.isReloading() && capacityText.hideOnReloadText())) {
            GunItemOverlay.drawTextDefinition(guiGraphics, mc.font, GunItemOverlay.formatCapacityText(gunItem.getMaxAmmoCapacity(stack, fireModeInstance), capacityText.showSlash()), hOffset, vOffset, ammoUiDefinition.width(), capacityText, mirrorHorizontally);
        }
        if ((fireModeText = ammoUiDefinition.fireModeText()) != null && fireModeText.enabled()) {
            GunItemOverlay.drawTextDefinition(guiGraphics, mc.font, fireModeDisplayName, hOffset, vOffset, ammoUiDefinition.width(), fireModeText, mirrorHorizontally);
        }
    }

    private static UiInertiaState updateCustomUiInertia(Minecraft mc, AmmoUiDefinition ammoUiDefinition) {
        UiInertiaState state = AMMO_UI_INERTIA_STATES.computeIfAbsent(ammoUiDefinition.id(), ignored -> new UiInertiaState());
        if (state.lastRenderFrame == overlayRenderFrame) {
            return state;
        }
        state.lastRenderFrame = overlayRenderFrame;
        GunItemOverlay.updateUiInertia(mc, ammoUiDefinition.inertia(), state);
        return state;
    }

    private static void renderHeatIndicator(GuiGraphics guiGraphics, Minecraft mc, ItemStack stack, GunClientState gunClientState, GunItem gunItem, Config.AmmoIndicatorPlacement forcedPlacement) {
        float x;
        if (!Config.heatIndicatorEnabled || !gunItem.supportsOverheating(stack)) {
            return;
        }
        OverheatFeature overheatFeature = OverheatFeature.get(stack);
        HeatUiDefinition definition = HeatUiRegistry.get(overheatFeature != null ? overheatFeature.getHeatUi() : "default");
        if (definition == null) {
            return;
        }
        HeatUiDefinition.ScreenLocation location = definition.screenLocation();
        if ("default".equals(definition.id())) {
            Config.HeatIndicatorPlacement configuredPlacement = Config.heatIndicatorPlacement != null ? Config.heatIndicatorPlacement : Config.HeatIndicatorPlacement.BOTTOM_RIGHT;
            switch (configuredPlacement) {
                default: {
                    throw new MatchException(null, null);
                }
                case BOTTOM_RIGHT: {
                    HeatUiDefinition.ScreenLocation screenLocation = HeatUiDefinition.ScreenLocation.BOTTOM_RIGHT;
                    break;
                }
                case BOTTOM_LEFT: {
                    HeatUiDefinition.ScreenLocation screenLocation = HeatUiDefinition.ScreenLocation.BOTTOM_LEFT;
                    break;
                }
                case TOP_LEFT: {
                    HeatUiDefinition.ScreenLocation screenLocation = HeatUiDefinition.ScreenLocation.TOP_LEFT;
                    break;
                }
                case TOP_RIGHT: {
                    HeatUiDefinition.ScreenLocation screenLocation = location = HeatUiDefinition.ScreenLocation.TOP_RIGHT;
                }
            }
            if (forcedPlacement == Config.AmmoIndicatorPlacement.LEFT_OF_HOTBAR) {
                location = configuredPlacement == Config.HeatIndicatorPlacement.TOP_LEFT || configuredPlacement == Config.HeatIndicatorPlacement.TOP_RIGHT ? HeatUiDefinition.ScreenLocation.TOP_LEFT : HeatUiDefinition.ScreenLocation.BOTTOM_LEFT;
            } else if (forcedPlacement == Config.AmmoIndicatorPlacement.RIGHT_OF_HOTBAR) {
                location = configuredPlacement == Config.HeatIndicatorPlacement.TOP_LEFT || configuredPlacement == Config.HeatIndicatorPlacement.TOP_RIGHT ? HeatUiDefinition.ScreenLocation.TOP_RIGHT : HeatUiDefinition.ScreenLocation.BOTTOM_RIGHT;
            }
        }
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        float y = switch (location) {
            case HeatUiDefinition.ScreenLocation.LEFT_OF_HOTBAR -> {
                x = (screenWidth >> 1) - 97 - definition.width() - definition.paddingX();
                yield screenHeight - definition.height() - definition.paddingY();
            }
            case HeatUiDefinition.ScreenLocation.TOP_LEFT -> {
                x = definition.paddingX();
                yield definition.paddingY();
            }
            case HeatUiDefinition.ScreenLocation.BOTTOM_LEFT -> {
                x = definition.paddingX();
                yield screenHeight - definition.height() - definition.paddingY();
            }
            case HeatUiDefinition.ScreenLocation.TOP_RIGHT -> {
                x = screenWidth - definition.width() - definition.paddingX();
                yield definition.paddingY();
            }
            case HeatUiDefinition.ScreenLocation.BOTTOM_RIGHT -> {
                x = screenWidth - definition.width() - definition.paddingX();
                yield screenHeight - definition.height() - definition.paddingY();
            }
            case HeatUiDefinition.ScreenLocation.RIGHT_OF_HOTBAR -> {
                x = (screenWidth >> 1) + 97 + definition.paddingX();
                yield screenHeight - definition.height() - definition.paddingY();
            }
            default -> throw new IllegalStateException("Unhandled heat UI location: " + String.valueOf((Object)location));
        };
        GunItemOverlay.updateUiInertia(mc, definition.inertia(), HEAT_UI_INERTIA);
        boolean mirrorHorizontally = location == HeatUiDefinition.ScreenLocation.BOTTOM_LEFT || location == HeatUiDefinition.ScreenLocation.TOP_LEFT || location == HeatUiDefinition.ScreenLocation.LEFT_OF_HOTBAR;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderUtil.blit(guiGraphics, definition.backgroundTexture(), x += GunItemOverlay.HEAT_UI_INERTIA.x, x + (float)definition.width(), y += GunItemOverlay.HEAT_UI_INERTIA.y, y + (float)definition.height(), -90.0f, mirrorHorizontally ? 1.0f : 0.0f, mirrorHorizontally ? 0.0f : 1.0f, 0.0f, 1.0f);
        RenderSystem.defaultBlendFunc();
        RenderUtil.blitMaskedProgress(guiGraphics, definition.progressTexture(), definition.maskTexture(), (float)gunClientState.getCurrentHeatRatio(stack), x, y, definition.width(), definition.height(), mirrorHorizontally);
        HeatUiDefinition.PercentageTextDefinition percentageText = definition.percentageText();
        if (percentageText.enabled()) {
            float heatRatio = Mth.clamp((float)((float)gunClientState.getCurrentHeatRatio(stack)), (float)0.0f, (float)1.0f);
            String text = Math.round(heatRatio * 100.0f) + "%";
            float textX = x + (float)percentageText.x();
            if (mirrorHorizontally) {
                textX = x + (float)definition.width() - (float)percentageText.x() - (float)mc.font.width(text) * percentageText.size();
            }
            GunItemOverlay.drawStyledString(guiGraphics, mc.font, text, textX, y + (float)percentageText.y(), percentageText.size(), GunItemOverlay.getHeatPercentageColor(heatRatio, percentageText.emptyColor(), percentageText.fullColor()), percentageText.border(), percentageText.dropShadow(), percentageText.italic(), percentageText.bold());
        }
    }

    private static int getHeatPercentageColor(float heatRatio, int emptyColor, int fullColor) {
        int alpha = Math.round(Mth.lerp((float)heatRatio, (float)(emptyColor >>> 24), (float)(fullColor >>> 24)));
        int red = Math.round(Mth.lerp((float)heatRatio, (float)(emptyColor >> 16 & 0xFF), (float)(fullColor >> 16 & 0xFF)));
        int green = Math.round(Mth.lerp((float)heatRatio, (float)(emptyColor >> 8 & 0xFF), (float)(fullColor >> 8 & 0xFF)));
        int blue = Math.round(Mth.lerp((float)heatRatio, (float)(emptyColor & 0xFF), (float)(fullColor & 0xFF)));
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static void updateUiInertia(Minecraft mc, float inertia, UiInertiaState state) {
        if (mc.player == null || inertia <= 0.0f) {
            state.x = Mth.lerp((float)0.2f, (float)state.x, (float)0.0f);
            state.y = Mth.lerp((float)0.2f, (float)state.y, (float)0.0f);
            state.viewTracked = false;
            state.lastUpdateNanos = 0L;
            return;
        }
        long now = System.nanoTime();
        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();
        if (!state.viewTracked || state.lastUpdateNanos == 0L) {
            state.lastYaw = currentYaw;
            state.lastPitch = currentPitch;
            state.viewTracked = true;
            state.lastUpdateNanos = now;
        }
        float deltaTime = Mth.clamp((float)((float)(now - state.lastUpdateNanos) / 1.0E9f), (float)0.004166667f, (float)0.05f);
        float yawDelta = Mth.wrapDegrees((float)(currentYaw - state.lastYaw));
        float pitchDelta = currentPitch - state.lastPitch;
        state.lastYaw = currentYaw;
        state.lastPitch = currentPitch;
        state.lastUpdateNanos = now;
        float yawVelocity = yawDelta / deltaTime;
        float pitchVelocity = pitchDelta / deltaTime;
        float maxOffset = 32.0f * inertia;
        float targetX = Mth.clamp((float)(-yawVelocity * 0.06f * inertia), (float)(-maxOffset), (float)maxOffset);
        float targetY = Mth.clamp((float)(-pitchVelocity * 0.05f * inertia), (float)(-maxOffset), (float)maxOffset);
        float smoothing = 1.0f - (float)Math.exp(-12.0f * deltaTime);
        state.x = Mth.lerp((float)smoothing, (float)state.x, (float)targetX);
        state.y = Mth.lerp((float)smoothing, (float)state.y, (float)targetY);
    }

    private static void drawOutlinedString(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color) {
        GunItemOverlay.drawOverlayString(guiGraphics, font, text, (float)(x + 1), (float)y, 0, false);
        GunItemOverlay.drawOverlayString(guiGraphics, font, text, (float)(x - 1), (float)y, 0, false);
        GunItemOverlay.drawOverlayString(guiGraphics, font, text, (float)x, (float)(y + 1), 0, false);
        GunItemOverlay.drawOverlayString(guiGraphics, font, text, (float)x, (float)(y - 1), 0, false);
        GunItemOverlay.drawOverlayString(guiGraphics, font, text, (float)x, (float)y, color, false);
    }

    private static void drawStyledString(GuiGraphics guiGraphics, Font font, String text, float x, float y, float size, int color, boolean border, boolean dropShadow, boolean italic, boolean bold) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0f);
        guiGraphics.pose().scale(size, size, 1.0f);
        Component styledText = GunItemOverlay.createStyledText(text, italic, bold);
        if (border) {
            GunItemOverlay.drawOverlayString(guiGraphics, font, styledText, 1.0f, 0.0f, 0, false);
            GunItemOverlay.drawOverlayString(guiGraphics, font, styledText, -1.0f, 0.0f, 0, false);
            GunItemOverlay.drawOverlayString(guiGraphics, font, styledText, 0.0f, 1.0f, 0, false);
            GunItemOverlay.drawOverlayString(guiGraphics, font, styledText, 0.0f, -1.0f, 0, false);
        }
        GunItemOverlay.drawOverlayString(guiGraphics, font, styledText, 0.0f, 0.0f, color, dropShadow);
        guiGraphics.pose().popPose();
    }

    private static int drawOverlayString(GuiGraphics guiGraphics, Font font, String text, float x, float y, int color, boolean dropShadow) {
        return font.drawInBatch(text, x, y, color, dropShadow, guiGraphics.pose().last().pose(), (MultiBufferSource)guiGraphics.bufferSource(), Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);
    }

    private static int drawOverlayString(GuiGraphics guiGraphics, Font font, Component text, float x, float y, int color, boolean dropShadow) {
        return font.drawInBatch(text, x, y, color, dropShadow, guiGraphics.pose().last().pose(), (MultiBufferSource)guiGraphics.bufferSource(), Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);
    }

    private static Component createStyledText(String text, boolean italic, boolean bold) {
        return Component.literal((String)text).withStyle(style -> style.withItalic(Boolean.valueOf(italic)).withBold(Boolean.valueOf(bold)));
    }

    private static void drawTextDefinition(GuiGraphics guiGraphics, Font font, String text, float hOffset, float vOffset, float uiWidth, AmmoUiDefinition.TextDefinition textDefinition, boolean mirrorHorizontally) {
        GunItemOverlay.drawTextDefinition(guiGraphics, font, text, hOffset, vOffset, uiWidth, textDefinition, textDefinition.color(), mirrorHorizontally);
    }

    private static void drawTextDefinition(GuiGraphics guiGraphics, Font font, String text, float hOffset, float vOffset, float uiWidth, AmmoUiDefinition.TextDefinition textDefinition, int color, boolean mirrorHorizontally) {
        float textX = textDefinition.x();
        if (mirrorHorizontally) {
            Component styledText = GunItemOverlay.createStyledText(text, textDefinition.italic(), textDefinition.bold());
            textX = uiWidth - textX - (float)font.width((FormattedText)styledText) * textDefinition.size();
        }
        GunItemOverlay.drawStyledString(guiGraphics, font, text, hOffset + textX, vOffset + (float)textDefinition.y(), textDefinition.size(), color, textDefinition.border(), textDefinition.dropShadow(), textDefinition.italic(), textDefinition.bold());
    }

    private static String formatAmmoText(int currentAmmo, int maxAmmo, boolean digitalStyle) {
        if (maxAmmo == Integer.MAX_VALUE) {
            return "\u221e";
        }
        if (!digitalStyle) {
            return String.valueOf(Math.max(currentAmmo, 0));
        }
        int width = Math.max(1, String.valueOf(Math.max(maxAmmo, 0)).length());
        return String.format("%0" + width + "d", Math.max(currentAmmo, 0));
    }

    private static String formatCapacityText(int maxAmmo, boolean showSlash) {
        if (maxAmmo == Integer.MAX_VALUE) {
            return showSlash ? "/\u221e" : "\u221e";
        }
        String text = String.valueOf(Math.max(maxAmmo, 0));
        return showSlash ? "/" + text : text;
    }

    private static final class UiInertiaState {
        private float x;
        private float y;
        private float lastYaw;
        private float lastPitch;
        private boolean viewTracked;
        private long lastUpdateNanos;
        private long lastRenderFrame = Long.MIN_VALUE;

        private UiInertiaState() {
        }
    }
}

