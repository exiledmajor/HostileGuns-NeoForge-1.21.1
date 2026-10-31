/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 */
package com.vicmatskiv.pointblank.client.gui;

import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ConfigurationScreen
extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int TITLE_Y = 12;
    private static final int FIRST_BUTTON_Y = 33;
    private static final int DONE_BUTTON_BOTTOM_PADDING = 8;
    private static final int LIST_BOTTOM_GAP = 8;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_GAP = 6;
    private final Screen parent;
    private Button pipScopesButton;
    private Button scopeViewButton;
    private Button crosshairTypeButton;
    private Button goreButton;
    private Button goreSoundsButton;
    private Button shellEjectionButton;
    private Button destroyBlocksButton;
    private Button autoResetRecoilButton;
    private Button ammoIndicatorButton;
    private Button ammoIndicatorPlacementButton;
    private Button heatIndicatorButton;
    private Button heatIndicatorPlacementButton;
    private Button interactionPromptButton;
    private Button doneButton;
    private List<Button> settingButtons = List.of();
    private int titleY;
    private int listBottom;
    private int scrollOffset;
    private boolean draggingScrollbar;

    public ConfigurationScreen(Screen parent) {
        super((Component)Component.translatable((String)"screen.pointblank.configuration.title"));
        this.parent = parent;
    }

    protected void init() {
        super.init();
        int centerX = this.width / 2;
        this.titleY = 12;
        int buttonY = 33;
        this.pipScopesButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.getPipScopesButtonText(), this::onPipScopesToggle).bounds(centerX - 100, buttonY, 200, 20).build());
        this.scopeViewButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.getScopeViewButtonText(), this::onScopeViewToggle).bounds(centerX - 100, buttonY += 24, 200, 20).build());
        this.crosshairTypeButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.getCrosshairTypeButtonText(), this::onCrosshairTypeToggle).bounds(centerX - 100, buttonY += 24, 200, 20).build());
        this.goreButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.getGoreButtonText(), this::onGoreToggle).bounds(centerX - 100, buttonY += 24, 200, 20).build());
        this.goreSoundsButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.getGoreSoundsButtonText(), this::onGoreSoundsToggle).bounds(centerX - 100, buttonY += 24, 200, 20).build());
        this.shellEjectionButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.getShellEjectionButtonText(), this::onShellEjectionToggle).bounds(centerX - 100, buttonY += 24, 200, 20).build());
        this.destroyBlocksButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.getDestroyBlocksButtonText(), this::onDestroyBlocksToggle).bounds(centerX - 100, buttonY += 24, 200, 20).build());
        this.autoResetRecoilButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.getAutoResetRecoilButtonText(), this::onAutoResetRecoilToggle).bounds(centerX - 100, buttonY += 24, 200, 20).build());
        this.ammoIndicatorButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.getAmmoIndicatorButtonText(), this::onAmmoIndicatorToggle).bounds(centerX - 100, buttonY += 24, 200, 20).build());
        this.ammoIndicatorPlacementButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.getAmmoIndicatorPlacementButtonText(), this::onAmmoIndicatorPlacementToggle).bounds(centerX - 100, buttonY += 24, 200, 20).build());
        this.heatIndicatorButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.getHeatIndicatorButtonText(), this::onHeatIndicatorToggle).bounds(centerX - 100, buttonY += 24, 200, 20).build());
        this.heatIndicatorPlacementButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.getHeatIndicatorPlacementButtonText(), this::onHeatIndicatorPlacementToggle).bounds(centerX - 100, buttonY += 24, 200, 20).build());
        this.interactionPromptButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.getInteractionPromptButtonText(), this::onInteractionPromptToggle).bounds(centerX - 100, buttonY += 24, 200, 20).build());
        buttonY += 24;
        this.settingButtons = List.of(this.pipScopesButton, this.scopeViewButton, this.crosshairTypeButton, this.goreButton, this.goreSoundsButton, this.shellEjectionButton, this.destroyBlocksButton, this.autoResetRecoilButton, this.ammoIndicatorButton, this.ammoIndicatorPlacementButton, this.heatIndicatorButton, this.heatIndicatorPlacementButton, this.interactionPromptButton);
        int doneButtonY = Math.max(33, this.height - 20 - 8);
        this.listBottom = doneButtonY - 8;
        this.doneButton = (Button)this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.translatable((String)"gui.done"), button -> this.onClose()).bounds(centerX - 100, doneButtonY, 200, 20).build());
        this.updateSettingsLayout();
        this.syncButtonStates();
    }

    private int getSettingsContentHeight() {
        return this.settingButtons.isEmpty() ? 0 : this.settingButtons.size() * 24 - 4;
    }

    private int getSettingsViewportHeight() {
        return Math.max(0, this.listBottom - 33);
    }

    private int getMaxScroll() {
        return Math.max(0, this.getSettingsContentHeight() - this.getSettingsViewportHeight());
    }

    private void updateSettingsLayout() {
        this.scrollOffset = Mth.clamp((int)this.scrollOffset, (int)0, (int)this.getMaxScroll());
        for (int index = 0; index < this.settingButtons.size(); ++index) {
            Button button = this.settingButtons.get(index);
            int y = 33 + index * 24 - this.scrollOffset;
            button.setY(y);
            button.visible = y >= 33 && y + 20 <= this.listBottom;
        }
    }

    private int getScrollbarX() {
        return this.width / 2 + 100 + 6;
    }

    private int getScrollbarThumbHeight() {
        int viewportHeight = this.getSettingsViewportHeight();
        if (viewportHeight <= 0) {
            return 0;
        }
        int contentHeight = this.getSettingsContentHeight();
        return contentHeight <= 0 ? viewportHeight : Mth.clamp((int)(viewportHeight * viewportHeight / contentHeight), (int)12, (int)viewportHeight);
    }

    private void updateScrollFromMouse(double mouseY) {
        int maxScroll = this.getMaxScroll();
        int trackRange = this.getSettingsViewportHeight() - this.getScrollbarThumbHeight();
        if (maxScroll <= 0 || trackRange <= 0) {
            this.scrollOffset = 0;
            return;
        }
        double relativeY = mouseY - 33.0 - (double)this.getScrollbarThumbHeight() / 2.0;
        this.scrollOffset = Mth.clamp((int)((int)Math.round(relativeY * (double)maxScroll / (double)trackRange)), (int)0, (int)maxScroll);
        this.updateSettingsLayout();
    }

    private int getScrollbarThumbY() {
        int maxScroll = this.getMaxScroll();
        int trackRange = this.getSettingsViewportHeight() - this.getScrollbarThumbHeight();
        return maxScroll <= 0 || trackRange <= 0 ? 33 : 33 + Math.round((float)this.scrollOffset / (float)maxScroll * (float)trackRange);
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        int scrollbarX = this.getScrollbarX();
        return this.getMaxScroll() > 0 && mouseX >= (double)scrollbarX && mouseX < (double)(scrollbarX + 6) && mouseY >= 33.0 && mouseY < (double)this.listBottom;
    }

    private void syncButtonStates() {
        if (this.pipScopesButton != null) {
            this.pipScopesButton.active = !Config.pipFallbackModDetected;
            this.pipScopesButton.setMessage(this.getPipScopesButtonText());
        }
        if (this.scopeViewButton != null) {
            this.scopeViewButton.setMessage(this.getScopeViewButtonText());
        }
        if (this.crosshairTypeButton != null) {
            this.crosshairTypeButton.setMessage(this.getCrosshairTypeButtonText());
        }
        if (this.goreButton != null) {
            this.goreButton.setMessage(this.getGoreButtonText());
        }
        if (this.goreSoundsButton != null) {
            this.goreSoundsButton.setMessage(this.getGoreSoundsButtonText());
        }
        if (this.shellEjectionButton != null) {
            this.shellEjectionButton.setMessage(this.getShellEjectionButtonText());
        }
        if (this.destroyBlocksButton != null) {
            this.destroyBlocksButton.active = !ConfigurationScreen.isConnectedToServer();
            this.destroyBlocksButton.setMessage(this.getDestroyBlocksButtonText());
        }
        if (this.autoResetRecoilButton != null) {
            this.autoResetRecoilButton.setMessage(this.getAutoResetRecoilButtonText());
        }
        if (this.ammoIndicatorButton != null) {
            this.ammoIndicatorButton.setMessage(this.getAmmoIndicatorButtonText());
        }
        if (this.ammoIndicatorPlacementButton != null) {
            this.ammoIndicatorPlacementButton.active = Config.ammoIndicatorEnabled && !ConfigurationScreen.isDualWieldingWithGuns();
            this.ammoIndicatorPlacementButton.setMessage(this.getAmmoIndicatorPlacementButtonText());
        }
        if (this.heatIndicatorButton != null) {
            this.heatIndicatorButton.setMessage(this.getHeatIndicatorButtonText());
        }
        if (this.heatIndicatorPlacementButton != null) {
            this.heatIndicatorPlacementButton.active = Config.heatIndicatorEnabled && !ConfigurationScreen.isDualWieldingWithGuns();
            this.heatIndicatorPlacementButton.setMessage(this.getHeatIndicatorPlacementButtonText());
        }
        if (this.interactionPromptButton != null) {
            this.interactionPromptButton.setMessage(this.getInteractionPromptButtonText());
        }
    }

    private Component getPipScopesButtonText() {
        boolean forcedOff = Config.pipFallbackModDetected;
        boolean enabled = Config.pipScopesEnabled && !forcedOff;
        String statusKey = enabled ? "options.on" : "options.off";
        MutableComponent message = Component.translatable((String)"screen.pointblank.settings.pip_scopes").append(": ").append((Component)Component.translatable((String)statusKey));
        if (forcedOff) {
            message.append(" ").append((Component)Component.translatable((String)"screen.pointblank.settings.lock"));
        }
        return message;
    }

    private void onPipScopesToggle(Button button) {
        Config.setPipScopesEnabled(!Config.pipScopesEnabled);
        button.setMessage(this.getPipScopesButtonText());
    }

    private Component getScopeViewButtonText() {
        String viewType = Config.scopeViewType == Config.ScopeViewType.ROUND ? "Round" : "Square";
        return Component.translatable((String)"screen.pointblank.settings.scope_view").append(": ").append((Component)Component.literal((String)viewType));
    }

    private void onScopeViewToggle(Button button) {
        Config.ScopeViewType newType = Config.scopeViewType == Config.ScopeViewType.SQUARE ? Config.ScopeViewType.ROUND : Config.ScopeViewType.SQUARE;
        Config.setScopeViewType(newType);
        button.setMessage(this.getScopeViewButtonText());
    }

    private Component getCrosshairTypeButtonText() {
        Config.CrosshairType crosshairType = Config.crosshairType != null ? Config.crosshairType : Config.CrosshairType.DEFAULT;
        return Component.translatable((String)"screen.pointblank.configuration.crosshair").append(": ").append((Component)Component.translatable((String)ConfigurationScreen.getCrosshairTypeTranslationKey(crosshairType)));
    }

    private static String getCrosshairTypeTranslationKey(Config.CrosshairType crosshairType) {
        return switch (crosshairType) {
            default -> throw new MatchException(null, null);
            case Config.CrosshairType.DEFAULT -> "screen.pointblank.configuration.crosshair.default";
            case Config.CrosshairType.VANILLA -> "screen.pointblank.configuration.crosshair.vanilla";
            case Config.CrosshairType.DISABLED -> "screen.pointblank.configuration.crosshair.disabled";
        };
    }

    private void onCrosshairTypeToggle(Button button) {
        Config.CrosshairType current = Config.crosshairType != null ? Config.crosshairType : Config.CrosshairType.DEFAULT;
        Config.CrosshairType next = switch (current) {
            default -> throw new MatchException(null, null);
            case Config.CrosshairType.DEFAULT -> Config.CrosshairType.VANILLA;
            case Config.CrosshairType.VANILLA -> Config.CrosshairType.DISABLED;
            case Config.CrosshairType.DISABLED -> Config.CrosshairType.DEFAULT;
        };
        Config.setCrosshairType(next);
        button.setMessage(this.getCrosshairTypeButtonText());
    }

    private Component getGoreButtonText() {
        String statusKey = Config.goreEnabled ? "options.on" : "options.off";
        return Component.translatable((String)"screen.pointblank.configuration.gore").append(": ").append((Component)Component.translatable((String)statusKey));
    }

    private void onGoreToggle(Button button) {
        Config.setGoreEnabled(!Config.goreEnabled);
        button.setMessage(this.getGoreButtonText());
    }

    private Component getGoreSoundsButtonText() {
        String statusKey = Config.goreSoundsEnabled ? "options.on" : "options.off";
        return Component.translatable((String)"screen.pointblank.configuration.gore_sounds").append(": ").append((Component)Component.translatable((String)statusKey));
    }

    private void onGoreSoundsToggle(Button button) {
        Config.setGoreSoundsEnabled(!Config.goreSoundsEnabled);
        button.setMessage(this.getGoreSoundsButtonText());
    }

    private Component getShellEjectionButtonText() {
        String statusKey = Config.shellEjectionEnabled ? "options.on" : "options.off";
        return Component.translatable((String)"screen.pointblank.configuration.shell_ejection").append(": ").append((Component)Component.translatable((String)statusKey));
    }

    private void onShellEjectionToggle(Button button) {
        Config.setShellEjectionEnabled(!Config.shellEjectionEnabled);
        button.setMessage(this.getShellEjectionButtonText());
    }

    private Component getDestroyBlocksButtonText() {
        String statusKey = Config.explosionDestroyBlocksEnabled ? "options.on" : "options.off";
        MutableComponent message = Component.translatable((String)"screen.pointblank.configuration.destroy_blocks").append(": ").append((Component)Component.translatable((String)statusKey));
        if (ConfigurationScreen.isConnectedToServer()) {
            message.append(" ").append((Component)Component.translatable((String)"screen.pointblank.configuration.server_only"));
        }
        return message;
    }

    private void onDestroyBlocksToggle(Button button) {
        if (!ConfigurationScreen.isConnectedToServer()) {
            Config.setExplosionDestroyBlocksEnabled(!Config.explosionDestroyBlocksEnabled);
            button.setMessage(this.getDestroyBlocksButtonText());
        }
    }

    private Component getAutoResetRecoilButtonText() {
        String statusKey = Config.resetAutoFirePitchEnabled ? "options.on" : "options.off";
        return Component.translatable((String)"screen.pointblank.configuration.auto_reset_recoil").append(": ").append((Component)Component.translatable((String)statusKey));
    }

    private void onAutoResetRecoilToggle(Button button) {
        Config.setResetAutoFirePitchEnabled(!Config.resetAutoFirePitchEnabled);
        button.setMessage(this.getAutoResetRecoilButtonText());
    }

    private Component getAmmoIndicatorButtonText() {
        String statusKey = Config.ammoIndicatorEnabled ? "options.on" : "options.off";
        return Component.translatable((String)"screen.pointblank.configuration.ammo_indicator").append(": ").append((Component)Component.translatable((String)statusKey));
    }

    private void onAmmoIndicatorToggle(Button button) {
        Config.setAmmoIndicatorEnabled(!Config.ammoIndicatorEnabled);
        button.setMessage(this.getAmmoIndicatorButtonText());
        if (this.ammoIndicatorPlacementButton != null) {
            this.ammoIndicatorPlacementButton.active = Config.ammoIndicatorEnabled && !ConfigurationScreen.isDualWieldingWithGuns();
        }
    }

    private Component getAmmoIndicatorPlacementButtonText() {
        Config.AmmoIndicatorPlacement placement = Config.ammoIndicatorPlacement != null ? Config.ammoIndicatorPlacement : Config.AmmoIndicatorPlacement.RIGHT_OF_HOTBAR;
        String placementKey = switch (placement) {
            default -> throw new MatchException(null, null);
            case Config.AmmoIndicatorPlacement.RIGHT_OF_HOTBAR -> "screen.pointblank.configuration.ammo_indicator_position.right_hotbar";
            case Config.AmmoIndicatorPlacement.LEFT_OF_HOTBAR -> "screen.pointblank.configuration.ammo_indicator_position.left_hotbar";
            case Config.AmmoIndicatorPlacement.TOP_LEFT -> "screen.pointblank.configuration.ammo_indicator_position.top_left";
            case Config.AmmoIndicatorPlacement.TOP_RIGHT -> "screen.pointblank.configuration.ammo_indicator_position.top_right";
        };
        return Component.translatable((String)"screen.pointblank.configuration.ammo_indicator_position").append(": ").append((Component)Component.translatable((String)placementKey));
    }

    private void onAmmoIndicatorPlacementToggle(Button button) {
        if (!button.active) {
            return;
        }
        Config.AmmoIndicatorPlacement current = Config.ammoIndicatorPlacement != null ? Config.ammoIndicatorPlacement : Config.AmmoIndicatorPlacement.RIGHT_OF_HOTBAR;
        Config.AmmoIndicatorPlacement next = switch (current) {
            default -> throw new MatchException(null, null);
            case Config.AmmoIndicatorPlacement.RIGHT_OF_HOTBAR -> Config.AmmoIndicatorPlacement.LEFT_OF_HOTBAR;
            case Config.AmmoIndicatorPlacement.LEFT_OF_HOTBAR -> Config.AmmoIndicatorPlacement.TOP_LEFT;
            case Config.AmmoIndicatorPlacement.TOP_LEFT -> Config.AmmoIndicatorPlacement.TOP_RIGHT;
            case Config.AmmoIndicatorPlacement.TOP_RIGHT -> Config.AmmoIndicatorPlacement.RIGHT_OF_HOTBAR;
        };
        Config.setAmmoIndicatorPlacement(next);
        button.setMessage(this.getAmmoIndicatorPlacementButtonText());
    }

    private Component getHeatIndicatorButtonText() {
        String statusKey = Config.heatIndicatorEnabled ? "options.on" : "options.off";
        return Component.translatable((String)"screen.pointblank.configuration.heat_indicator").append(": ").append((Component)Component.translatable((String)statusKey));
    }

    private void onHeatIndicatorToggle(Button button) {
        Config.setHeatIndicatorEnabled(!Config.heatIndicatorEnabled);
        button.setMessage(this.getHeatIndicatorButtonText());
        if (this.heatIndicatorPlacementButton != null) {
            this.heatIndicatorPlacementButton.active = Config.heatIndicatorEnabled && !ConfigurationScreen.isDualWieldingWithGuns();
        }
    }

    private Component getHeatIndicatorPlacementButtonText() {
        Config.HeatIndicatorPlacement placement = Config.heatIndicatorPlacement != null ? Config.heatIndicatorPlacement : Config.HeatIndicatorPlacement.BOTTOM_RIGHT;
        String placementKey = switch (placement) {
            default -> throw new MatchException(null, null);
            case Config.HeatIndicatorPlacement.BOTTOM_RIGHT -> "screen.pointblank.configuration.heat_indicator_position.bottom_right";
            case Config.HeatIndicatorPlacement.BOTTOM_LEFT -> "screen.pointblank.configuration.heat_indicator_position.bottom_left";
            case Config.HeatIndicatorPlacement.TOP_LEFT -> "screen.pointblank.configuration.heat_indicator_position.top_left";
            case Config.HeatIndicatorPlacement.TOP_RIGHT -> "screen.pointblank.configuration.heat_indicator_position.top_right";
        };
        return Component.translatable((String)"screen.pointblank.configuration.heat_indicator_position").append(": ").append((Component)Component.translatable((String)placementKey));
    }

    private void onHeatIndicatorPlacementToggle(Button button) {
        if (!button.active) {
            return;
        }
        Config.HeatIndicatorPlacement current = Config.heatIndicatorPlacement != null ? Config.heatIndicatorPlacement : Config.HeatIndicatorPlacement.BOTTOM_RIGHT;
        Config.HeatIndicatorPlacement next = switch (current) {
            default -> throw new MatchException(null, null);
            case Config.HeatIndicatorPlacement.BOTTOM_RIGHT -> Config.HeatIndicatorPlacement.BOTTOM_LEFT;
            case Config.HeatIndicatorPlacement.BOTTOM_LEFT -> Config.HeatIndicatorPlacement.TOP_LEFT;
            case Config.HeatIndicatorPlacement.TOP_LEFT -> Config.HeatIndicatorPlacement.TOP_RIGHT;
            case Config.HeatIndicatorPlacement.TOP_RIGHT -> Config.HeatIndicatorPlacement.BOTTOM_RIGHT;
        };
        Config.setHeatIndicatorPlacement(next);
        button.setMessage(this.getHeatIndicatorPlacementButtonText());
    }

    private Component getInteractionPromptButtonText() {
        String statusKey = Config.interactionPromptEnabled ? "options.on" : "options.off";
        return Component.translatable((String)"screen.pointblank.configuration.interaction_prompt").append(": ").append((Component)Component.translatable((String)statusKey));
    }

    private void onInteractionPromptToggle(Button button) {
        Config.setInteractionPromptEnabled(!Config.interactionPromptEnabled);
        button.setMessage(this.getInteractionPromptButtonText());
    }

    private static boolean isConnectedToServer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getCurrentServer() != null && !mc.isSingleplayer();
    }

    private static boolean isDualWieldingWithGuns() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        return GunItem.hasGunsInMainAndAltSlots((Player)mc.player);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0 || scrollY == 0.0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int scrollStep = 24;
        int delta = (int)Math.round(scrollY * (double)scrollStep);
        if (delta == 0) {
            delta = scrollY > 0.0 ? scrollStep : -scrollStep;
        }
        this.scrollOffset = Mth.clamp((int)(this.scrollOffset - delta), (int)0, (int)maxScroll);
        this.updateSettingsLayout();
        return true;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isOverScrollbar(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            this.updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        if (mouseButton == 0 && this.draggingScrollbar) {
            this.updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            this.draggingScrollbar = false;
        }
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.syncButtonStates();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.getMaxScroll() > 0 && this.getSettingsViewportHeight() > 0) {
            int scrollbarX = this.getScrollbarX();
            int thumbY = this.getScrollbarThumbY();
            guiGraphics.fill(scrollbarX, 33, scrollbarX + 6, this.listBottom, 0x66000000);
            guiGraphics.fill(scrollbarX, thumbY, scrollbarX + 6, thumbY + this.getScrollbarThumbHeight(), -5592406);
        }
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.titleY, 0xFFFFFF);
    }

    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

