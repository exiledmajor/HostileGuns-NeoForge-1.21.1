/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractButton
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.narration.NarrationElementOutput
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 */
package com.vicmatskiv.pointblank.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vicmatskiv.pointblank.client.gui.ButtonExt;
import com.vicmatskiv.pointblank.client.render.RenderUtil;
import com.vicmatskiv.pointblank.util.Interpolators;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CustomButton
extends AbstractButton
implements ButtonExt {
    public static final ResourceLocation BUTTON_RESOURCE = ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)"textures/gui/buttons.png");
    public static final int SMALL_WIDTH = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;
    protected static final CreateNarration DEFAULT_NARRATION = p_253298_ -> (MutableComponent)p_253298_.get();
    private final Event onPress;
    private Event onRelease;
    protected final CreateNarration createNarration;
    private boolean isPressed;
    private Interpolators.FloatProvider progressProvider;

    public static Builder builder(Component title, Event onPress) {
        return new Builder(title, onPress);
    }

    protected CustomButton(int x, int y, int width, int height, Component title, Event onPress, Event onRelease, CreateNarration narration, Interpolators.FloatProvider progressProvider) {
        super(x, y, width, height, title);
        this.onPress = onPress;
        this.onRelease = onRelease;
        this.createNarration = narration;
        this.progressProvider = progressProvider;
    }

    protected CustomButton(Builder builder) {
        this(builder.x, builder.y, builder.width, builder.height, builder.message, builder.onPress, builder.onRelease, builder.createNarration, builder.progressProvider);
        this.setTooltip(builder.tooltip);
    }

    public boolean isPressed() {
        return this.isPressed;
    }

    public void onPress() {
        this.onPress.handle(this);
    }

    public void onClick(double mouseX, double mouseY) {
        this.isPressed = true;
        super.onClick(mouseX, mouseY);
    }

    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        this.release();
    }

    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        int textureWidth = 48;
        int textureHeight = 20;
        int vOffset = 32;
        if (!this.active) {
            vOffset += 40;
        } else if (this.isPressed) {
            vOffset += 20;
        } else if (this.isHoveredOrFocused()) {
            vOffset += 60;
        }
        RenderUtil.blitNineSliced(guiGraphics, BUTTON_RESOURCE, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, textureWidth, textureHeight, 0, vOffset);
        if (this.progressProvider != null) {
            float progress = this.progressProvider.getValue();
            int height = this.getHeight() - 2;
            guiGraphics.fill(this.getX() + 1, this.getY() + 1 + height - (int)((float)height * progress), this.getX() + this.getWidth() - 1, this.getY() + 1 + height, -2147434496);
        }
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        int i = this.getFGColor();
        CustomButton.renderScrollingString((GuiGraphics)guiGraphics, (Font)minecraft.font, (Component)this.getMessage(), (int)(this.getX() + 8), (int)this.getY(), (int)(this.getX() + this.getWidth() - 8), (int)(this.getY() + this.getHeight()), (int)(i | Mth.ceil((float)(this.alpha * 255.0f)) << 24));
    }

    public void renderString(GuiGraphics guiGraphics, Font font, int color) {
        this.renderScrollingString(guiGraphics, font, 2, color);
    }

    protected MutableComponent createNarrationMessage() {
        return this.createNarration.createNarrationMessage(() -> super.createNarrationMessage());
    }

    public void updateWidgetNarration(NarrationElementOutput p_259196_) {
        this.defaultButtonNarrationText(p_259196_);
    }

    public void release() {
        if (this.isPressed && this.onRelease != null) {
            this.onRelease.handle(this);
        }
        this.isPressed = false;
    }

    public static class Builder {
        private final Component message;
        private final Event onPress;
        private Event onRelease;
        private Interpolators.FloatProvider progressProvider;
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private CreateNarration createNarration = DEFAULT_NARRATION;

        public Builder(Component p_254097_, Event onPress) {
            this.message = p_254097_;
            this.onPress = onPress;
        }

        public Builder progressProvider(Interpolators.FloatProvider progressProvider) {
            this.progressProvider = progressProvider;
            return this;
        }

        public Builder onRelease(Event onRelease) {
            this.onRelease = onRelease;
            return this;
        }

        public Builder pos(int posX, int posY) {
            this.x = posX;
            this.y = posY;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder bounds(int posX, int posY, int width, int height) {
            return this.pos(posX, posY).size(width, height);
        }

        public Builder tooltip(Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder createNarration(CreateNarration p_253638_) {
            this.createNarration = p_253638_;
            return this;
        }

        public CustomButton build() {
            return this.build(CustomButton::new);
        }

        public CustomButton build(Function<Builder, CustomButton> builder) {
            return builder.apply(this);
        }
    }

    public static interface Event {
        public void handle(CustomButton var1);
    }

    public static interface CreateNarration {
        public MutableComponent createNarrationMessage(Supplier<MutableComponent> var1);
    }
}

