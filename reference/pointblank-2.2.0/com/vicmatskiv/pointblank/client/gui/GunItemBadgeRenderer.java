/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.client.gui;

import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class GunItemBadgeRenderer {
    private static final int BACKGROUND_COLOR = -652994528;
    private static final int BORDER_COLOR = -736178;
    private static final int TEXT_COLOR = -3656;

    private GunItemBadgeRenderer() {
    }

    public static void renderAkimboBadge(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        GunItem gunItem;
        Item item;
        if (stack.isEmpty() || !((item = stack.getItem()) instanceof GunItem) || !(gunItem = (GunItem)item).isDualWieldingEnabled(stack)) {
            return;
        }
        int minX = x + 10;
        int minY = y;
        int maxX = x + 16;
        int maxY = y + 6;
        graphics.fill(minX, minY, maxX, maxY, -652994528);
        graphics.fill(minX, minY, maxX, minY + 1, -736178);
        graphics.fill(minX, minY, minX + 1, maxY, -736178);
        graphics.fill(maxX - 1, minY, maxX, maxY, -736178);
        graphics.fill(minX, maxY - 1, maxX, maxY, -736178);
        graphics.pose().pushPose();
        graphics.pose().translate((float)minX + 1.0f, (float)minY + 1.0f, 200.0f);
        graphics.pose().scale(0.5f, 0.5f, 1.0f);
        graphics.drawString(font, "A", 0, 0, -3656, false);
        graphics.pose().popPose();
    }
}

