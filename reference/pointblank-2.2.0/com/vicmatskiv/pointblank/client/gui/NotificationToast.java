/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.toasts.Toast
 *  net.minecraft.client.gui.components.toasts.Toast$Visibility
 *  net.minecraft.client.gui.components.toasts.ToastComponent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.resources.ResourceLocation
 */
package com.vicmatskiv.pointblank.client.gui;

import com.vicmatskiv.pointblank.client.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;

public class NotificationToast
implements Toast {
    public static final ResourceLocation BUTTON_RESOURCE = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/gui/buttons.png");
    private Component title;
    private long lastChanged;
    private boolean changed;
    private final int width;
    private long displayTime;

    public NotificationToast(Component title, long displayTime) {
        this.title = title;
        Minecraft mc = Minecraft.getInstance();
        this.width = Math.max(90, 30 + mc.font.width((FormattedText)title));
        this.displayTime = displayTime;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return 26;
    }

    public Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long currentTime) {
        if (this.changed) {
            this.lastChanged = currentTime;
            this.changed = false;
        }
        int textureWidth = 160;
        int textureHeight = 32;
        RenderUtil.blitNineSliced(guiGraphics, BUTTON_RESOURCE, 0, 0, this.width(), this.height(), 18, 4, textureWidth, textureHeight, 0, 0);
        Minecraft minecraft = toastComponent.getMinecraft();
        guiGraphics.drawString(minecraft.font, this.title, 18, 9, -256, false);
        return (double)(currentTime - this.lastChanged) < (double)this.displayTime * toastComponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }
}

