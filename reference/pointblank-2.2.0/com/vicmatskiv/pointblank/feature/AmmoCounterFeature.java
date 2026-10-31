/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
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
import java.util.Locale;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AmmoCounterFeature
extends ConditionalFeature {
    public static final int DEFAULT_TEXT_COLOR = -1;
    private final int textColor;

    private AmmoCounterFeature(FeatureProvider owner, Predicate<ConditionContext> condition, int textColor) {
        super(owner, condition);
        this.textColor = textColor;
    }

    public MutableComponent getDescription() {
        return Component.literal((String)"Ammo counter");
    }

    public int getTextColor() {
        return this.textColor;
    }

    public static AmmoCounterFeature getEnabled(ItemStack itemStack) {
        FeatureProvider fp;
        AmmoCounterFeature feature;
        Item item = itemStack.getItem();
        if (item instanceof FeatureProvider && (feature = (fp = (FeatureProvider)item).getFeature(AmmoCounterFeature.class)) != null && feature.isEnabled(itemStack)) {
            return feature;
        }
        return null;
    }

    public static String formatAmmoCount(int currentAmmo, int maxAmmo) {
        if (maxAmmo == Integer.MAX_VALUE) {
            return "\u221e";
        }
        int width = Math.max(1, String.valueOf(Math.max(maxAmmo, 0)).length());
        return String.format(Locale.ROOT, "%0" + width + "d", Math.max(currentAmmo, 0));
    }

    public static class Builder
    implements FeatureBuilder<Builder, AmmoCounterFeature> {
        private Predicate<ConditionContext> condition = ctx -> true;
        private int textColor = -1;

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            JsonElement colorElement;
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson(obj.get("condition")));
            }
            JsonElement jsonElement = colorElement = obj.has("textColor") ? obj.get("textColor") : obj.get("color");
            if (colorElement != null && !colorElement.isJsonNull()) {
                this.withTextColor(Builder.parseColor(colorElement));
            }
            return this;
        }

        @Override
        public AmmoCounterFeature build(FeatureProvider featureProvider) {
            return new AmmoCounterFeature(featureProvider, this.condition, this.textColor);
        }

        private static int parseColor(JsonElement element) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                int value = element.getAsInt();
                return (value & 0xFF000000) == 0 ? value | 0xFF000000 : value;
            }
            String value = element.getAsString().trim();
            if (value.startsWith("#")) {
                value = value.substring(1);
            } else if (value.startsWith("0x") || value.startsWith("0X")) {
                value = value.substring(2);
            }
            long parsed = Long.parseLong(value, 16);
            if (value.length() <= 6) {
                parsed |= 0xFF000000L;
            }
            return (int)parsed;
        }
    }
}

