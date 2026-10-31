/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.GeckoLibConstants
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animation.Animation
 *  software.bernie.geckolib.animation.AnimationProcessor
 *  software.bernie.geckolib.cache.GeckoLibCache
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.loading.object.BakedAnimations
 *  software.bernie.geckolib.model.DefaultedItemGeoModel
 */
package com.vicmatskiv.pointblank.client.model;

import com.vicmatskiv.pointblank.client.controller.BlendingAnimationProcessor;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class GunGeoModel
extends DefaultedItemGeoModel<GunItem> {
    private BakedGeoModel currentModel = null;
    private final List<ResourceLocation> fallbackAnimations;
    private final BlendingAnimationProcessor<GunItem> blendingAnimationProcessor = new BlendingAnimationProcessor(this);
    private static final boolean DEBUG_DUAL_WIELD = Boolean.getBoolean("pointblank.debug.dualwield");
    private static final Set<String> LOGGED_VARIANT_FALLBACKS = Collections.synchronizedSet(new HashSet());

    public GunGeoModel(ResourceLocation assetSubPath, List<ResourceLocation> fallbackAnimations) {
        super(assetSubPath);
        this.fallbackAnimations = new ArrayList<ResourceLocation>();
        for (ResourceLocation fallbackAnimation : fallbackAnimations) {
            this.fallbackAnimations.add(this.buildFormattedAnimationPath(fallbackAnimation));
        }
    }

    public AnimationProcessor<GunItem> getAnimationProcessor() {
        return this.blendingAnimationProcessor;
    }

    public BakedGeoModel getBakedModel(ResourceLocation location) {
        BakedGeoModel model = (BakedGeoModel)GeckoLibCache.getBakedModels().get(location);
        if (model == null) {
            if (!location.getPath().contains("geo/")) {
                throw GeckoLibConstants.exception((ResourceLocation)location, (String)"Invalid model resource path provided - GeckoLib models must be placed in assets/<modid>/geo/");
            }
            throw GeckoLibConstants.exception((ResourceLocation)location, (String)"Unable to find model");
        }
        if (model != this.currentModel) {
            this.blendingAnimationProcessor.setActiveModel(model);
            this.currentModel = model;
        }
        return this.currentModel;
    }

    public Animation getAnimation(GunItem animatable, String name) {
        String key;
        if (name == null) {
            return null;
        }
        Animation animation = this.findExactAnimation(animatable, name);
        if (animation != null) {
            return animation;
        }
        String baseName = GunItem.stripDualWieldAnimationSuffix(name);
        if (!name.equals(baseName) && (animation = this.findExactAnimation(animatable, baseName)) != null && DEBUG_DUAL_WIELD && LOGGED_VARIANT_FALLBACKS.add(key = String.valueOf(this.getAnimationResource((GeoAnimatable)animatable)) + "|" + name)) {
            GeckoLibConstants.LOGGER.debug("Missing dual-wield animation {}; using {}", (Object)name, (Object)baseName);
        }
        return animation;
    }

    private Animation findExactAnimation(GunItem animatable, String name) {
        ResourceLocation location = this.getAnimationResource((GeoAnimatable)animatable);
        BakedAnimations bakedAnimations = (BakedAnimations)GeckoLibCache.getBakedAnimations().get(location);
        Animation bakedAnimation = null;
        if (bakedAnimations != null) {
            bakedAnimation = bakedAnimations.getAnimation(name);
        }
        if (bakedAnimation == null) {
            for (ResourceLocation animationLocation : this.fallbackAnimations) {
                BakedAnimations altAnimations = (BakedAnimations)GeckoLibCache.getBakedAnimations().get(animationLocation);
                if (altAnimations == null || (bakedAnimation = altAnimations.getAnimation(name)) == null) continue;
                break;
            }
        }
        return bakedAnimation;
    }
}

