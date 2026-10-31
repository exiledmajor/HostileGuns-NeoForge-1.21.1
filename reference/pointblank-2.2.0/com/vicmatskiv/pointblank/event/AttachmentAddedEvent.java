/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventVisitor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AttachmentAddedEvent
implements CustomEvent {
    private Player owner;
    private ItemStack parentStack;
    private ItemStack attachmentStack;

    public AttachmentAddedEvent(Player owner, ItemStack parentStack, ItemStack attachmentStack) {
        this.owner = owner;
        this.parentStack = parentStack;
        this.attachmentStack = attachmentStack;
    }

    public Player getPlayer() {
        return this.owner;
    }

    public ItemStack getParentStack() {
        return this.parentStack;
    }

    public ItemStack getAttachmentStack() {
        return this.attachmentStack;
    }

    @Override
    public <T> T accept(EventVisitor<T> visitor) {
        return null;
    }
}

