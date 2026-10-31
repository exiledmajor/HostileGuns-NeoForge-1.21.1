/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.ItemLike
 */
package com.vicmatskiv.pointblank.attachment;

import com.vicmatskiv.pointblank.Nameable;
import com.vicmatskiv.pointblank.attachment.AttachmentCategory;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import java.util.Set;
import net.minecraft.world.level.ItemLike;

public interface Attachment
extends Nameable,
ItemLike,
FeatureProvider {
    public AttachmentCategory getCategory();

    public Set<String> getGroups();

    default public void applyFeature(Class<Feature> featureType, Object context) {
    }
}

