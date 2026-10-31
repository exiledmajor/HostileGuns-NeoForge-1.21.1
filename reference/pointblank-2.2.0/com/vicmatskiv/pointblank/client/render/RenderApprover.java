/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.client.render;

import com.vicmatskiv.pointblank.client.render.RenderPass;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.Features;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public interface RenderApprover {
    public boolean canRenderPart(String var1);

    public Class<? extends Feature> getFeatureType();

    default public boolean approveRendering(RenderPass renderPass, String partName, ItemStack rootStack, ItemStack currentStack, String path, ItemDisplayContext itemDisplayContext) {
        if (renderPass != RenderPass.current()) {
            return true;
        }
        if (!this.canRenderPart(partName)) {
            return false;
        }
        Class<? extends Feature> approvedFeatureType = this.getFeatureType();
        if (approvedFeatureType == null) {
            return true;
        }
        boolean isApproved = false;
        for (Features.EnabledFeature enabledFeature : Features.getEnabledFeatures(rootStack, approvedFeatureType)) {
            if (!enabledFeature.ownerPath().equals(path)) continue;
            isApproved = true;
            break;
        }
        return isApproved;
    }
}

