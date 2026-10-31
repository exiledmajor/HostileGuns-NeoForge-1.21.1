/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.client.effect.MuzzleFlashEffect;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.ConditionalFeature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.registry.EffectRegistry;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.JsonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

public final class MuzzleFlashFeature
extends ConditionalFeature {
    private List<Pair<MuzzleFlashEffect.Builder, Predicate<ConditionContext>>> muzzleEffectBuilders = new ArrayList<Pair<MuzzleFlashEffect.Builder, Predicate<ConditionContext>>>();

    public MuzzleFlashFeature(FeatureProvider owner, Predicate<ConditionContext> predicate, Map<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> effectBuilders) {
        super(owner, predicate, effectBuilders);
        for (Map.Entry<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> e : effectBuilders.entrySet()) {
            for (Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>> pair : e.getValue()) {
                EffectBuilder eb = (EffectBuilder)((Supplier)pair.getFirst()).get();
                if (!(eb instanceof MuzzleFlashEffect.Builder)) continue;
                MuzzleFlashEffect.Builder mfeb = (MuzzleFlashEffect.Builder)eb;
                this.muzzleEffectBuilders.add((Pair<MuzzleFlashEffect.Builder, Predicate<ConditionContext>>)Pair.of((Object)mfeb, (Object)((Predicate)pair.getSecond())));
            }
        }
    }

    public List<Pair<MuzzleFlashEffect.Builder, Predicate<ConditionContext>>> getMuzzleFlashEffectBuilders() {
        return this.muzzleEffectBuilders;
    }

    @Override
    public boolean isEnabled(ItemStack itemStack) {
        return super.isEnabled(itemStack);
    }

    public static class Builder
    implements FeatureBuilder<Builder, MuzzleFlashFeature> {
        private Map<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> effectBuilders = new HashMap();
        private Predicate<ConditionContext> condition = ctx -> true;

        public Builder withEffect(GunItem.FirePhase firePhase, Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> effectBuilder) {
            List builders = this.effectBuilders.computeIfAbsent(firePhase, k -> new ArrayList());
            builders.add(Pair.of(effectBuilder, ctx -> true));
            return this;
        }

        public Builder withEffect(GunItem.FirePhase firePhase, Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> effectBuilder, Predicate<ConditionContext> condition) {
            List builders = this.effectBuilders.computeIfAbsent(firePhase, k -> new ArrayList());
            builders.add(Pair.of(effectBuilder, condition));
            return this;
        }

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            for (JsonObject effect : JsonUtil.getJsonObjects(obj, "effects")) {
                Predicate<ConditionContext> condition;
                GunItem.FirePhase firePhase = JsonUtil.getEnum(effect, "phase", GunItem.FirePhase.class, null, true);
                String effectName = JsonUtil.getJsonString(effect, "name");
                Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> supplier = () -> EffectRegistry.getEffectBuilderSupplier(effectName).get();
                if (effect.has("condition")) {
                    JsonObject conditionObj = effect.getAsJsonObject("condition");
                    condition = Conditions.fromJson((JsonElement)conditionObj);
                } else {
                    condition = ctx -> true;
                }
                this.withEffect(firePhase, supplier, condition);
            }
            return this;
        }

        @Override
        public MuzzleFlashFeature build(FeatureProvider featureProvider) {
            return new MuzzleFlashFeature(featureProvider, this.condition, this.effectBuilders);
        }
    }
}

