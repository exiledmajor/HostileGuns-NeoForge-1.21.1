/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.feature;

import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.attachment.Attachment;
import com.vicmatskiv.pointblank.attachment.Attachments;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface Feature {
    public FeatureProvider getOwner();

    default public Component getDescription() {
        return Component.empty();
    }

    default public boolean isEnabledForAttachment(ItemStack rootStack, ItemStack attachmentStack) {
        if (rootStack == null) {
            return false;
        }
        Item item = attachmentStack.getItem();
        if (!(item instanceof Attachment)) {
            return false;
        }
        Attachment attachment = (Attachment)item;
        if (!attachment.getCategory().requiresAttachmentSelection(this.getClass())) {
            return attachment.getFeature(this.getClass()) == this && this.isEnabled(rootStack);
        }
        Pair<String, ItemStack> selectedAttachment = Attachments.getSelectedAttachment(rootStack, attachment.getCategory());
        if (selectedAttachment != null && selectedAttachment.getSecond() == attachmentStack) {
            return attachment.getFeature(this.getClass()) == this && this.isEnabled(rootStack);
        }
        return false;
    }

    default public boolean isEnabled(ItemStack value) {
        return true;
    }

    default public Map<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> getEffectBuilders() {
        return Collections.emptyMap();
    }
}

