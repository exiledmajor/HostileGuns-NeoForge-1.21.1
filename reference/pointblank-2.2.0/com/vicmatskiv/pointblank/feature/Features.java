/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.attachment.Attachment;
import com.vicmatskiv.pointblank.attachment.Attachments;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.feature.AccuracyFeature;
import com.vicmatskiv.pointblank.feature.AimingFeature;
import com.vicmatskiv.pointblank.feature.AlternatingMuzzleFeature;
import com.vicmatskiv.pointblank.feature.AmmoCapacityFeature;
import com.vicmatskiv.pointblank.feature.AmmoCounterFeature;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.DamageFeature;
import com.vicmatskiv.pointblank.feature.DualWieldFeature;
import com.vicmatskiv.pointblank.feature.DynamicRpmFeature;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.feature.FireModeFeature;
import com.vicmatskiv.pointblank.feature.MuzzleFlashFeature;
import com.vicmatskiv.pointblank.feature.OverheatFeature;
import com.vicmatskiv.pointblank.feature.PartVisibilityFeature;
import com.vicmatskiv.pointblank.feature.PipFeature;
import com.vicmatskiv.pointblank.feature.RecoilFeature;
import com.vicmatskiv.pointblank.feature.ReloadFeature;
import com.vicmatskiv.pointblank.feature.ReticleFeature;
import com.vicmatskiv.pointblank.feature.ShellEjectionFeature;
import com.vicmatskiv.pointblank.feature.SkinFeature;
import com.vicmatskiv.pointblank.feature.SoundFeature;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.registry.FeatureTypeRegistry;
import com.vicmatskiv.pointblank.util.JsonUtil;
import com.vicmatskiv.pointblank.util.LRUCache;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class Features {
    private static final LRUCache<Pair<Tag, Class<? extends Feature>>, List<EnabledFeature>> selectedItemFeatureCache = new LRUCache(200);
    private static final LRUCache<Pair<Tag, GunItem.FirePhase>, List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> enabledPhaseEffects = new LRUCache(100);

    public static <T extends Feature> EnabledFeature getFirstEnabledFeature(ItemStack itemStack, Class<? extends Feature> featureClass) {
        List<EnabledFeature> enabledFeatures = Features.getEnabledFeatures(itemStack, featureClass);
        return !enabledFeatures.isEmpty() ? enabledFeatures.get(0) : null;
    }

    public static <T extends Feature> List<EnabledFeature> getEnabledFeatures(ItemStack itemStack, Class<? extends Feature> featureClass) {
        CompoundTag tag = MiscUtil.getTag(itemStack);
        if (tag == null) {
            return Collections.emptyList();
        }
        return selectedItemFeatureCache.computeIfAbsent((Pair<Tag, Class<? extends Feature>>)Pair.of((Object)tag, featureClass), p -> Features.computeEnabledFeatures(itemStack, featureClass));
    }

    private static List<EnabledFeature> computeEnabledFeatures(ItemStack rootStack, Class<? extends Feature> featureType) {
        FeatureProvider fp;
        Feature feature;
        NavigableMap<String, ItemStack> attachmentStacks = Attachments.getAttachments(rootStack, true);
        ArrayList<EnabledFeature> result = new ArrayList<EnabledFeature>();
        for (Map.Entry entry : attachmentStacks.entrySet()) {
            FeatureProvider fp2;
            Feature feature2;
            Item item = ((ItemStack)entry.getValue()).getItem();
            if (!(item instanceof FeatureProvider) || (feature2 = (fp2 = (FeatureProvider)item).getFeature(featureType)) == null || !(((ItemStack)entry.getValue()).getItem() instanceof Attachment) || !feature2.isEnabledForAttachment(rootStack, (ItemStack)entry.getValue())) continue;
            result.add(new EnabledFeature(feature2, (ItemStack)entry.getValue(), (String)entry.getKey()));
        }
        Item item = rootStack.getItem();
        if (item instanceof FeatureProvider && (feature = (fp = (FeatureProvider)item).getFeature(featureType)) != null && feature.isEnabled(rootStack)) {
            result.add(new EnabledFeature(feature, rootStack, "/"));
        }
        return result;
    }

    public static boolean hasFeature(ItemStack itemStack, Feature feature) {
        Item item = itemStack.getItem();
        if (!(item instanceof FeatureProvider)) {
            return false;
        }
        FeatureProvider fp = (FeatureProvider)item;
        return fp.hasFeature(feature);
    }

    public static List<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>> getEnabledPhaseEffects(ItemStack itemStack, GunItem.FirePhase phase) {
        CompoundTag tag = MiscUtil.getTag(itemStack);
        if (tag == null) {
            return Collections.emptyList();
        }
        List conditionalEffects = enabledPhaseEffects.computeIfAbsent((Pair<Tag, GunItem.FirePhase>)Pair.of((Object)tag, (Object)((Object)phase)), p -> Features.computeEnabledPhaseEffects(itemStack, phase));
        ConditionContext context = new ConditionContext(itemStack);
        return conditionalEffects.stream().filter(p -> ((Predicate)p.getSecond()).test(context)).map(p -> (Supplier)p.getFirst()).toList();
    }

    private static List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>> computeEnabledPhaseEffects(ItemStack rootStack, GunItem.FirePhase firePhase) {
        NavigableMap<String, ItemStack> attachmentStacks = Attachments.getAttachments(rootStack, true);
        ArrayList result = new ArrayList();
        for (Class<? extends Feature> featureType : FeatureTypeRegistry.getFeatureTypes().values()) {
            List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>> effectBuilders;
            FeatureProvider fp;
            Feature feature;
            for (Map.Entry entry : attachmentStacks.entrySet()) {
                List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>> effectBuilders2;
                FeatureProvider fp2;
                Feature feature2;
                Item item = ((ItemStack)entry.getValue()).getItem();
                if (!(item instanceof FeatureProvider) || (feature2 = (fp2 = (FeatureProvider)item).getFeature(featureType)) == null || !(((ItemStack)entry.getValue()).getItem() instanceof Attachment) || !feature2.isEnabledForAttachment(rootStack, (ItemStack)entry.getValue()) || (effectBuilders2 = feature2.getEffectBuilders().get((Object)firePhase)) == null) continue;
                result.addAll(effectBuilders2);
            }
            Item item = rootStack.getItem();
            if (!(item instanceof FeatureProvider) || (feature = (fp = (FeatureProvider)item).getFeature(featureType)) == null || !feature.isEnabled(rootStack) || (effectBuilders = feature.getEffectBuilders().get((Object)firePhase)) == null) continue;
            result.addAll(effectBuilders);
        }
        return result;
    }

    @Nullable
    public static FeatureBuilder<?, ?> fromJson(JsonObject obj) {
        String featureType = JsonUtil.getJsonString(obj, "type");
        switch (featureType.toUpperCase(Locale.ROOT)) {
            case "ACCURACY": {
                return new AccuracyFeature.Builder().withJsonObject(obj);
            }
            case "AIMING": {
                return new AimingFeature.Builder().withJsonObject(obj);
            }
            case "AMMOCOUNTER": {
                return new AmmoCounterFeature.Builder().withJsonObject(obj);
            }
            case "DAMAGE": {
                return new DamageFeature.Builder().withJsonObject(obj);
            }
            case "DUALWIELD": 
            case "DUAL_WIELD": 
            case "DUALWIELDING": 
            case "DUAL_WIELDING": {
                return new DualWieldFeature.Builder().withJsonObject(obj);
            }
            case "DYNAMICRPM": 
            case "DYNAMIC_RPM": {
                return new DynamicRpmFeature.Builder().withJsonObject(obj);
            }
            case "FIREMODE": {
                return new FireModeFeature.Builder().withJsonObject(obj);
            }
            case "AMMOCAPACITY": {
                return new AmmoCapacityFeature.Builder().withJsonObject(obj);
            }
            case "ALTERNATINGMUZZLE": 
            case "ALTERNATING_MUZZLE": {
                return new AlternatingMuzzleFeature.Builder().withJsonObject(obj);
            }
            case "RELOAD": {
                return new ReloadFeature.Builder().withJsonObject(obj);
            }
            case "MUZZLEFLASH": {
                return new MuzzleFlashFeature.Builder().withJsonObject(obj);
            }
            case "OVERHEAT": 
            case "OVERHEATING": {
                return new OverheatFeature.Builder().withJsonObject(obj);
            }
            case "PARTVISIBILITY": {
                return new PartVisibilityFeature.Builder().withJsonObject(obj);
            }
            case "PIP": {
                return new PipFeature.Builder().withJsonObject(obj);
            }
            case "RECOIL": {
                return new RecoilFeature.Builder().withJsonObject(obj);
            }
            case "RETICLE": {
                return new ReticleFeature.Builder().withJsonObject(obj);
            }
            case "SOUND": {
                return new SoundFeature.Builder().withJsonObject(obj);
            }
            case "SKIN": {
                return new SkinFeature.Builder().withJsonObject(obj);
            }
            case "SHELLEJECTION": 
            case "SHELL_EJECTION": {
                return new ShellEjectionFeature.Builder().withJsonObject(obj);
            }
        }
        return null;
    }

    public record EnabledFeature(Feature feature, ItemStack ownerStack, String ownerPath) {
    }
}

