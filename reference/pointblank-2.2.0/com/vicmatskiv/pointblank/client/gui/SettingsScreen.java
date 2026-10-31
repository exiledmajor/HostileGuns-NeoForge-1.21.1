/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package com.vicmatskiv.pointblank.client.gui;

import com.vicmatskiv.pointblank.client.gui.ConfigurationScreen;
import com.vicmatskiv.pointblank.client.gui.ExtensionPackScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SettingsScreen
extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int TITLE_HEIGHT = 9;
    private static final int TITLE_TO_BUTTON_GAP = 20;
    private int titleY;

    public SettingsScreen() {
        super((Component)Component.translatable((String)"screen.pointblank.settings.title"));
    }

    protected void init() {
        int blockTopY;
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int totalButtons = 3;
        int buttonBlockHeight = 20 * totalButtons + 4 * (totalButtons - 1);
        int contentHeight = 29 + buttonBlockHeight;
        this.titleY = blockTopY = centerY - contentHeight / 2;
        int buttonY = blockTopY + 9 + 20;
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.translatable((String)"screen.pointblank.settings.packs"), button -> this.minecraft.setScreen((Screen)new ExtensionPackScreen(this))).bounds(centerX - 100, buttonY, 200, 20).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.translatable((String)"screen.pointblank.settings.configuration"), button -> this.minecraft.setScreen((Screen)new ConfigurationScreen(this))).bounds(centerX - 100, buttonY += 24, 200, 20).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.translatable((String)"gui.done"), button -> this.onClose()).bounds(centerX - 100, buttonY += 24, 200, 20).build());
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.titleY, 0xFFFFFF);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

