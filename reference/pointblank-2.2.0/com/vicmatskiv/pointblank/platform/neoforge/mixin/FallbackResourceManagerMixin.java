/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.PackResources
 *  net.minecraft.server.packs.PackType
 *  net.minecraft.server.packs.resources.FallbackResourceManager
 *  net.minecraft.server.packs.resources.FallbackResourceManager$PackEntry
 *  net.minecraft.server.packs.resources.IoSupplier
 *  net.minecraft.server.packs.resources.Resource
 *  net.minecraft.server.packs.resources.ResourceMetadata
 *  org.jetbrains.annotations.NotNull
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.vicmatskiv.pointblank.platform.neoforge.mixin;

import com.google.common.collect.Lists;
import com.vicmatskiv.pointblank.platform.neoforge.DelegatingPackResources;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={FallbackResourceManager.class})
public class FallbackResourceManagerMixin {
    @Final
    @Shadow
    public List<FallbackResourceManager.PackEntry> fallbacks;
    @Final
    @Shadow
    private PackType type;

    @Inject(method={"getResourceStack"}, at={@At(value="HEAD")}, cancellable=true)
    public void onGetResourceStack(ResourceLocation location, CallbackInfoReturnable<List<Resource>> cir) {
        if (location.getNamespace().equals("pointblank")) {
            cir.setReturnValue(this.getPointblankResourceStack(location));
            cir.cancel();
        }
    }

    @Unique
    public List<Resource> getPointblankResourceStack(ResourceLocation resourceLocation) {
        ResourceLocation metadataResourceLocation = FallbackResourceManagerMixin.getPointblankMetadataLocation(resourceLocation);
        ArrayList<Resource> list = new ArrayList<Resource>();
        boolean flag = false;
        for (int i = this.fallbacks.size() - 1; i >= 0; --i) {
            FallbackResourceManager.PackEntry packEntry = this.fallbacks.get(i);
            PackResources pack = packEntry.resources();
            if (pack != null) {
                Collection<PackResources> collection;
                if (pack instanceof DelegatingPackResources) {
                    DelegatingPackResources epr = (DelegatingPackResources)pack;
                    collection = epr.getChildren();
                } else {
                    collection = null;
                }
                Collection<PackResources> children = collection;
                Collection<PackResources> packs = children == null ? List.of(pack) : children;
                for (PackResources packresources : packs) {
                    IoSupplier iosupplier = packresources.getResource(this.type, resourceLocation);
                    if (iosupplier == null) continue;
                    IoSupplier<ResourceMetadata> iosupplier1 = this.getPointblankResourceMetadataIoSupplier(packresources, flag, metadataResourceLocation);
                    list.add(new Resource(packresources, iosupplier, iosupplier1));
                }
            }
            if (packEntry.isFiltered(resourceLocation)) break;
            if (!packEntry.isFiltered(metadataResourceLocation)) continue;
            flag = true;
        }
        return Lists.reverse(list);
    }

    @Unique
    @NotNull
    private IoSupplier<ResourceMetadata> getPointblankResourceMetadataIoSupplier(PackResources packresources, boolean flag, ResourceLocation metadataResourceLocation) {
        IoSupplier iosupplier1 = flag ? ResourceMetadata.EMPTY_SUPPLIER : () -> {
            IoSupplier iosupplier2 = packresources.getResource(this.type, metadataResourceLocation);
            return iosupplier2 != null ? FallbackResourceManager.parseMetadata((IoSupplier)iosupplier2) : ResourceMetadata.EMPTY;
        };
        return iosupplier1;
    }

    @Unique
    private static ResourceLocation getPointblankMetadataLocation(ResourceLocation location) {
        return location.withPath(location.getPath() + ".mcmeta");
    }
}

