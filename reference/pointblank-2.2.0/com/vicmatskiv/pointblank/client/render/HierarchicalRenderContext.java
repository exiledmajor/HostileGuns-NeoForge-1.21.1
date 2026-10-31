/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.client.render;

import com.vicmatskiv.pointblank.Nameable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HierarchicalRenderContext
implements AutoCloseable {
    private static Deque<HierarchicalRenderContext> contextStack = new ArrayDeque<HierarchicalRenderContext>();
    private ItemStack itemStack;
    private String path;
    private ItemDisplayContext itemDisplayContext;
    private Map<String, Object> attributes = new HashMap<String, Object>();

    private HierarchicalRenderContext(ItemStack itemStack, HierarchicalRenderContext parent, ItemDisplayContext itemDisplayContext) {
        this.itemStack = itemStack;
        this.itemDisplayContext = itemDisplayContext;
        if (parent == null) {
            this.path = "/";
        } else {
            String string;
            Item item = itemStack.getItem();
            if (item instanceof Nameable) {
                Nameable nameable = (Nameable)item;
                string = nameable.getName();
            } else {
                string = itemStack.getItem().toString();
            }
            String name = string;
            this.path = parent.getPath() + "/" + name;
        }
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public String getPath() {
        return this.path;
    }

    public ItemDisplayContext getItemDisplayContext() {
        return this.itemDisplayContext;
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public <T> T getAttribute(String name, T _default) {
        if (this.attributes.containsKey(name)) {
            return (T)this.attributes.get(name);
        }
        return _default;
    }

    public static HierarchicalRenderContext push(ItemStack itemStack, ItemDisplayContext itemDisplayContext) {
        HierarchicalRenderContext parent = contextStack.peekFirst();
        HierarchicalRenderContext context = new HierarchicalRenderContext(itemStack, parent, itemDisplayContext);
        contextStack.addFirst(context);
        return context;
    }

    public static HierarchicalRenderContext push() {
        HierarchicalRenderContext current = HierarchicalRenderContext.current();
        if (current == null) {
            throw new IllegalStateException("No parent hierarchical render context to inherit from");
        }
        HierarchicalRenderContext context = new HierarchicalRenderContext(current.itemStack, current, current.itemDisplayContext);
        context.path = current.path;
        contextStack.addFirst(context);
        return context;
    }

    public static void pop() {
        contextStack.removeFirst();
    }

    public static HierarchicalRenderContext current() {
        return contextStack.peekFirst();
    }

    public static HierarchicalRenderContext getRoot() {
        return contextStack.peekLast();
    }

    public static ItemStack getRootItemStack() {
        HierarchicalRenderContext root = HierarchicalRenderContext.getRoot();
        return root != null ? root.itemStack : null;
    }

    @Override
    public void close() {
        HierarchicalRenderContext.pop();
    }
}

