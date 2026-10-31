/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.DefaultedItemGeoModel
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoItemRenderer
 */
package com.vicmatskiv.pointblank.client.render;

import com.vicmatskiv.pointblank.item.AmmoItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class DefaultModelRenderer
extends GeoItemRenderer<AmmoItem> {
    public DefaultModelRenderer(String resourceName) {
        super((GeoModel)new DefaultedItemGeoModel(ResourceLocation.fromNamespaceAndPath((String)"pointblank", (String)resourceName)));
    }
}

