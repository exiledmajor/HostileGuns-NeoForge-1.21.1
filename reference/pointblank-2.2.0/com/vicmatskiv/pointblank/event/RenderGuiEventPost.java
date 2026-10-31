/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.gui.GuiGraphics
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class RenderGuiEventPost
implements CustomEvent {
    private GuiGraphics guiGraphics;
    private DeltaTracker deltaTracker;

    public RenderGuiEventPost(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        this.guiGraphics = guiGraphics;
    }

    public GuiGraphics getGuiGraphics() {
        return this.guiGraphics;
    }

    public DeltaTracker getDeltaTracker() {
        return this.deltaTracker;
    }

    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        throw new UnsupportedOperationException();
    }
}

