/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 */
package com.vicmatskiv.pointblank.event;

import com.mojang.blaze3d.platform.Window;
import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class RenderGuiOverlayEvent
implements CustomEvent {
    private GuiGraphics guiGraphics;
    private DeltaTracker deltaTracker;
    private Type type;
    private boolean isCanceled;

    public RenderGuiOverlayEvent(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Type type) {
        this.guiGraphics = guiGraphics;
        this.deltaTracker = deltaTracker;
        this.type = type;
    }

    public boolean isCancelable() {
        return true;
    }

    public boolean isHotbarType() {
        return this.type == Type.HOTBAR;
    }

    public boolean isCrosshairType() {
        return this.type == Type.CROSSHAIR;
    }

    public GuiGraphics getGuiGraphics() {
        return this.guiGraphics;
    }

    public Window getWindow() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.getWindow();
    }

    public float getPartialTick() {
        return this.deltaTracker.getGameTimeDeltaPartialTick(true);
    }

    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return null;
    }

    public boolean isCanceled() {
        return this.isCanceled;
    }

    public void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    public static enum Type {
        HOTBAR,
        CROSSHAIR;

    }
}

