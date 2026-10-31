/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.inventory;

public interface HierarchicalSlot {
    public String getPath();

    public HierarchicalSlot getParentSlot();

    public void setParentSlot(HierarchicalSlot var1);
}

