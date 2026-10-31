/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText$ContentConsumer
 *  net.minecraft.network.chat.MutableComponent
 */
package com.vicmatskiv.pointblank.feature;

import com.vicmatskiv.pointblank.feature.Feature;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public interface FeatureProvider {
    default public <T extends Feature> T getFeature(Class<T> featureType) {
        return null;
    }

    default public boolean hasFeature(Feature feature) {
        return this.getFeature(feature.getClass()) == feature;
    }

    public Collection<Feature> getFeatures();

    default public List<Component> getDescriptionTooltipLines() {
        return Collections.emptyList();
    }

    default public List<Component> getFeatureTooltipLines() {
        ArrayList<Component> tooltipLines = new ArrayList<Component>();
        for (Feature feature : this.getFeatures()) {
            MutableComponent featureDescription = feature.getDescription().copy().withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC);
            FormattedText.ContentConsumer consumer = s -> Optional.of(s != null && !s.isEmpty());
            if (!featureDescription.getContents().visit(consumer).orElse(false).booleanValue()) continue;
            tooltipLines.add((Component)featureDescription);
        }
        return tooltipLines;
    }
}

