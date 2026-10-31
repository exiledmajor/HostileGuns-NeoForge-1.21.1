/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Style
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.vicmatskiv.pointblank.attachment;

import com.vicmatskiv.pointblank.Nameable;
import com.vicmatskiv.pointblank.attachment.Attachment;
import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface AttachmentHost
extends ItemLike,
Nameable,
FeatureProvider {
    public static final int DEFAULT_MAX_ATTACHMENT_CATEGORIES = 6;

    public Collection<Attachment> getCompatibleAttachments();

    default public Collection<Attachment> getDefaultAttachments() {
        return Collections.emptyList();
    }

    default public Collection<AttachmentCategory> getCompatibleAttachmentCategories() {
        return this.getCompatibleAttachments().stream().map(a -> a.getCategory()).toList();
    }

    default public int getMaxAttachmentCategories() {
        return 6;
    }

    default public List<Component> getCompatibleAttachmentTooltipLines(ItemStack itemStack) {
        ArrayList<Component> tooltipLines = new ArrayList<Component>();
        tooltipLines.add((Component)Component.empty());
        Collection<Attachment> compatibleAttachments = this.getCompatibleAttachments();
        if (compatibleAttachments.isEmpty()) {
            tooltipLines.add((Component)Component.translatable((String)"label.pointblank.noCompatibleAttachments").setStyle(Style.EMPTY.withColor(0xAAAAAA).withItalic(Boolean.valueOf(true)).withUnderlined(Boolean.valueOf(false))));
        } else {
            tooltipLines.add((Component)Component.translatable((String)"label.pointblank.compatibleAttachments").append(":").withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.ITALIC));
            for (Attachment attachment : compatibleAttachments) {
                tooltipLines.add((Component)Component.translatable((String)attachment.asItem().getDescriptionId()).withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.ITALIC));
            }
        }
        return tooltipLines;
    }
}

