/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.ConditionalFeature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.util.Conditions;
import java.util.function.Predicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AlternatingMuzzleFeature
extends ConditionalFeature {
    public static final String PRIMARY_MUZZLE_FLASH_BONE = "muzzleflash";
    public static final String ALTERNATE_MUZZLE_FLASH_BONE = "muzzleflashalt";

    private AlternatingMuzzleFeature(FeatureProvider owner, Predicate<ConditionContext> predicate) {
        super(owner, predicate);
    }

    public static String getMuzzleFlashBone(long shotSequence) {
        return (shotSequence & 1L) == 0L ? PRIMARY_MUZZLE_FLASH_BONE : ALTERNATE_MUZZLE_FLASH_BONE;
    }

    public static boolean shouldRenderMuzzleFlashBone(String selectedBone, String candidateBone) {
        if (selectedBone == null) {
            return !ALTERNATE_MUZZLE_FLASH_BONE.equals(candidateBone);
        }
        return selectedBone.equals(candidateBone);
    }

    public static boolean isEnabledFor(ItemStack itemStack) {
        Item item;
        if (itemStack == null || !((item = itemStack.getItem()) instanceof FeatureProvider)) {
            return false;
        }
        FeatureProvider featureProvider = (FeatureProvider)item;
        AlternatingMuzzleFeature feature = featureProvider.getFeature(AlternatingMuzzleFeature.class);
        return feature != null && feature.isEnabled(itemStack);
    }

    public static final class Builder
    implements FeatureBuilder<Builder, AlternatingMuzzleFeature> {
        private Predicate<ConditionContext> condition = context -> true;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            return this;
        }

        @Override
        public AlternatingMuzzleFeature build(FeatureProvider featureProvider) {
            return new AlternatingMuzzleFeature(featureProvider, this.condition);
        }
    }
}

