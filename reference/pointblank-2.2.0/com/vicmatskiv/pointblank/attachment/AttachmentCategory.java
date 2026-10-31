/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.attachment;

import com.vicmatskiv.pointblank.feature.ActiveMuzzleFeature;
import com.vicmatskiv.pointblank.feature.AimingFeature;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.PipFeature;
import com.vicmatskiv.pointblank.feature.ReticleFeature;
import com.vicmatskiv.pointblank.util.Conditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;

public class AttachmentCategory
implements Comparable<AttachmentCategory> {
    private static SortedMap<String, AttachmentCategory> categories = new TreeMap<String, AttachmentCategory>();
    public static final AttachmentCategory SCOPE = AttachmentCategory.fromString("scope", c -> c == AimingFeature.class || c == ReticleFeature.class || c == PipFeature.class);
    public static final AttachmentCategory MUZZLE = AttachmentCategory.fromString("muzzle", c -> false).withDefaultFeatures(List.of(new ActiveMuzzleFeature.Builder().withCondition(Conditions.isUsingDefaultMuzzle())));
    public static final AttachmentCategory RAIL = AttachmentCategory.fromString("rail", c -> false);
    public static final AttachmentCategory UNDERBARREL = AttachmentCategory.fromString("underbarrel", c -> false);
    public static final AttachmentCategory SKIN = AttachmentCategory.fromString("skin", c -> false);
    public static final AttachmentCategory STOCK = AttachmentCategory.fromString("stock", c -> false);
    public static final AttachmentCategory MAGAZINE = AttachmentCategory.fromString("magazine", c -> false);
    private final String name;
    private Predicate<Class<? extends Feature>> isActiveAttachmentRequiredPredicate;
    private List<FeatureBuilder<?, ?>> defaultFeatures;

    public static Collection<AttachmentCategory> values() {
        return categories.values();
    }

    public static AttachmentCategory fromString(String categoryName) {
        return AttachmentCategory.fromString(categoryName, c -> false);
    }

    private static AttachmentCategory fromString(String categoryName, Predicate<Class<? extends Feature>> isActiveAttachmentRequiredPredicate) {
        AttachmentCategory category = categories.computeIfAbsent(categoryName.toLowerCase(), n -> new AttachmentCategory((String)n, isActiveAttachmentRequiredPredicate));
        return category;
    }

    public static AttachmentCategory fromOrdinal(int ordinal) {
        int index = 0;
        AttachmentCategory result = null;
        if (ordinal >= categories.size()) {
            throw new IllegalArgumentException("Invalid ordinal " + ordinal);
        }
        for (AttachmentCategory ac : categories.values()) {
            if (index == ordinal) {
                result = ac;
                break;
            }
            ++index;
        }
        return result;
    }

    private AttachmentCategory(String name, Predicate<Class<? extends Feature>> isActiveAttachmentRequiredPredicate) {
        this.name = name;
        this.isActiveAttachmentRequiredPredicate = isActiveAttachmentRequiredPredicate;
        this.defaultFeatures = new ArrayList();
    }

    private AttachmentCategory withDefaultFeatures(List<FeatureBuilder<?, ?>> defaultFeatures) {
        this.defaultFeatures.addAll(defaultFeatures);
        return this;
    }

    public String getName() {
        return this.name;
    }

    public List<FeatureBuilder<?, ?>> getDefaultFeatures() {
        return this.defaultFeatures;
    }

    public boolean requiresAttachmentSelection(Class<? extends Feature> featureType) {
        return this.isActiveAttachmentRequiredPredicate.test(featureType);
    }

    public int ordinal() {
        int result = 0;
        for (AttachmentCategory ac : categories.values()) {
            if (ac == this) {
                return result;
            }
            ++result;
        }
        return -1;
    }

    public int hashCode() {
        return Objects.hash(this.name);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        AttachmentCategory other = (AttachmentCategory)obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public int compareTo(AttachmentCategory o) {
        return this.name.compareTo(o.name);
    }

    public String toString() {
        return String.format("AttachmentCategory[%s]", this.name);
    }
}

