/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.LayeredDraw
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.vicmatskiv.pointblank.mixin;

import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.event.RenderGuiEventPost;
import com.vicmatskiv.pointblank.event.RenderGuiOverlayEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Gui.class})
public class GuiMixin {
    @Shadow
    private LayeredDraw layers;

    @Inject(method={"renderCrosshair"}, cancellable=true, at={@At(value="HEAD")})
    private void onRenderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo callback) {
        RenderGuiOverlayEvent event = this.publishRenderOverlayEvent(guiGraphics, deltaTracker);
        if (event.isCanceled()) {
            callback.cancel();
        }
    }

    private RenderGuiOverlayEvent publishRenderOverlayEvent(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        RenderGuiOverlayEvent event = new RenderGuiOverlayEvent(guiGraphics, deltaTracker, RenderGuiOverlayEvent.Type.CROSSHAIR);
        Platform.getInstance().getEventBus().postEvent(event);
        return event;
    }

    @Inject(method={"renderHotbarAndDecorations"}, cancellable=true, at={@At(value="HEAD")})
    private void onRenderHotbarAndDecorations(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo callback) {
        RenderGuiOverlayEvent event = this.publishRenderHotbarAndDecorationsEvent(guiGraphics, deltaTracker);
        if (event.isCanceled()) {
            callback.cancel();
        }
    }

    private RenderGuiOverlayEvent publishRenderHotbarAndDecorationsEvent(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        RenderGuiOverlayEvent event = new RenderGuiOverlayEvent(guiGraphics, deltaTracker, RenderGuiOverlayEvent.Type.HOTBAR);
        Platform.getInstance().getEventBus().postEvent(event);
        return event;
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void onConstruct(Minecraft minecraft, CallbackInfo ci) {
        LayeredDraw newLayer = new LayeredDraw().add(this::publishRenderOverlayPostEvent);
        this.layers.add(newLayer, () -> !minecraft.options.hideGui);
    }

    private void publishRenderOverlayPostEvent(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        RenderGuiEventPost event = new RenderGuiEventPost(guiGraphics, deltaTracker);
        Platform.getInstance().getEventBus().postEvent(event);
    }
}

