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

public class AttachmentRemovedEvent
implements CustomEvent {
    private ItemStack rootStack;
    private ItemStack parentStack;
    private ItemStack attachmentStack;
    private Player player;

    public AttachmentRemovedEvent(Player player, ItemStack rootStack, ItemStack parentStack, ItemStack attachmentStack) {
        this.player = player;
        this.rootStack = rootStack;
        this.parentStack = parentStack;
        this.attachmentStack = attachmentStack;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ItemStack getRootStack() {
        return this.rootStack;
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

