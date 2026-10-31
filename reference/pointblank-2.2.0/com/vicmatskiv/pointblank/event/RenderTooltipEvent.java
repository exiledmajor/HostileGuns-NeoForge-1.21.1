/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
 *  net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.world.inventory.tooltip.TooltipComponent
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.event;

import com.mojang.datafixers.util.Either;
import com.vicmatskiv.pointblank.event.CancellableEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public abstract class RenderTooltipEvent
extends CancellableEvent {
    protected final ItemStack itemStack;
    protected final GuiGraphics graphics;
    protected Font font;
    protected final List<ClientTooltipComponent> components;
    protected int x;
    protected int y;

    public RenderTooltipEvent(GuiGraphics graphics, ItemStack itemStack, List<ClientTooltipComponent> components, int x, int y, Font font) {
        this.graphics = graphics;
        this.itemStack = itemStack;
        this.components = components;
        this.font = font;
        this.x = x;
        this.y = y;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public GuiGraphics getGraphics() {
        return this.graphics;
    }

    public Font getFont() {
        return this.font;
    }

    public List<ClientTooltipComponent> getComponents() {
        return this.components;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public static class GatherComponents
    extends CancellableEvent {
        private final ItemStack itemStack;
        private final List<Either<FormattedText, TooltipComponent>> tooltipElements;
        private int screenWidth;
        private int screenHeight;
        private int maxWidth;

        public GatherComponents(ItemStack itemStack, List<Either<FormattedText, TooltipComponent>> tooltipElements) {
            this.itemStack = itemStack;
            this.tooltipElements = tooltipElements;
        }

        public GatherComponents(ItemStack itemStack, int screenWidth, int screenHeight, List<Either<FormattedText, TooltipComponent>> tooltipElements, int maxWidth) {
            this.itemStack = itemStack;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.tooltipElements = tooltipElements;
            this.maxWidth = maxWidth;
        }

        public ItemStack getItemStack() {
            return this.itemStack;
        }

        public List<Either<FormattedText, TooltipComponent>> getTooltipElements() {
            return this.tooltipElements;
        }

        public int getMaxWidth() {
            return this.maxWidth;
        }

        public int getScreenWidth() {
            return this.screenWidth;
        }

        public int getScreenHeight() {
            return this.screenHeight;
        }

        @Override
        public <T> T accept(EventVisitor<T> visitor) {
            return null;
        }
    }

    public static class Pre
    extends RenderTooltipEvent {
        private final ClientTooltipPositioner positioner;

        public Pre(GuiGraphics graphics, ItemStack itemStack, List<ClientTooltipComponent> components, int x, int y, Font font, ClientTooltipPositioner positioner) {
            super(graphics, itemStack, components, x, y, font);
            this.positioner = positioner;
        }

        public ClientTooltipPositioner getTooltipPositioner() {
            return this.positioner;
        }

        @Override
        public <T> T accept(EventVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}

