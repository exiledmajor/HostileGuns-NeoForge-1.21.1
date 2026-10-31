/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.ConditionalFeature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.feature.Features;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.registry.SoundRegistry;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.JsonUtil;
import com.vicmatskiv.pointblank.util.MiscUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SoundFeature
extends ConditionalFeature {
    private List<Pair<SoundDescriptor, Predicate<ConditionContext>>> fireSounds;

    private SoundFeature(FeatureProvider owner, Predicate<ConditionContext> predicate, List<Pair<SoundDescriptor, Predicate<ConditionContext>>> fireSounds) {
        super(owner, predicate);
        this.fireSounds = Collections.unmodifiableList(fireSounds);
    }

    public static SoundDescriptor getFireSoundAndVolume(ItemStack itemStack) {
        List<Features.EnabledFeature> enabledFeatures = Features.getEnabledFeatures(itemStack, SoundFeature.class);
        for (Features.EnabledFeature enabledFeature : enabledFeatures) {
            SoundFeature soundFeature = (SoundFeature)enabledFeature.feature();
            ConditionContext context = new ConditionContext(itemStack);
            for (Pair<SoundDescriptor, Predicate<ConditionContext>> fireSound : soundFeature.fireSounds) {
                if (!((Predicate)fireSound.getSecond()).test(context)) continue;
                return (SoundDescriptor)fireSound.getFirst();
            }
        }
        return null;
    }

    public static void playFireSound(Player player, ItemStack itemStack) {
        SoundDescriptor fsv = SoundFeature.getFireSoundAndVolume(itemStack);
        SoundEvent fireSound = null;
        float fireSoundVolume = 0.0f;
        if (fsv != null) {
            fireSound = fsv.soundSupplier.get();
            fireSoundVolume = fsv.volume;
        } else {
            Item item = itemStack.getItem();
            if (item instanceof GunItem) {
                GunItem gunItem = (GunItem)item;
                fireSound = gunItem.getFireSound();
                fireSoundVolume = gunItem.getFireSoundVolume();
            }
        }
        if (fireSound != null && fireSoundVolume > 0.0f) {
            MiscUtil.getLevel((Entity)player).playSound(player, player.getX(), player.getY(), player.getZ(), fireSound, SoundSource.PLAYERS, fireSoundVolume, 1.0f);
        }
    }

    public record SoundDescriptor(Supplier<SoundEvent> soundSupplier, float volume) {
    }

    public static class Builder
    implements FeatureBuilder<Builder, SoundFeature> {
        private static final float DEFAULT_FIRE_SOUND_VOLUME = 5.0f;
        private Predicate<ConditionContext> condition = ctx -> true;
        private SoundDescriptor fireSoundDescriptor;
        private List<Pair<SoundDescriptor, Predicate<ConditionContext>>> fireSounds = new ArrayList<Pair<SoundDescriptor, Predicate<ConditionContext>>>();

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withFireSound(Supplier<SoundEvent> sound, double volume) {
            this.fireSoundDescriptor = new SoundDescriptor(sound, (float)volume);
            return this;
        }

        public Builder withFireSound(Supplier<SoundEvent> sound, double volume, Predicate<ConditionContext> condition) {
            this.fireSounds.add((Pair<SoundDescriptor, Predicate<ConditionContext>>)Pair.of((Object)new SoundDescriptor(sound, (float)volume), condition));
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            for (JsonObject fireSoundObj : JsonUtil.getJsonObjects(obj, "fireSounds")) {
                Predicate<ConditionContext> condition;
                if (fireSoundObj.has("condition")) {
                    JsonObject conditionObj = fireSoundObj.getAsJsonObject("condition");
                    condition = Conditions.fromJson((JsonElement)conditionObj);
                } else {
                    condition = ctx -> true;
                }
                String fireSoundName = JsonUtil.getJsonString(fireSoundObj, "sound");
                float fireSoundVolume = JsonUtil.getJsonFloat(fireSoundObj, "volume", 5.0f);
                this.withFireSound(() -> SoundRegistry.getSoundEvent(fireSoundName), fireSoundVolume, condition);
            }
            if (!obj.has("fireSounds")) {
                String fireSoundName = JsonUtil.getJsonString(obj, "fireSound");
                float fireSoundVolume = JsonUtil.getJsonFloat(obj, "fireSoundVolume", 5.0f);
                this.withFireSound(() -> SoundRegistry.getSoundEvent(fireSoundName), fireSoundVolume);
            }
            return this;
        }

        @Override
        public SoundFeature build(FeatureProvider featureProvider) {
            ArrayList<Pair<SoundDescriptor, Predicate<ConditionContext>>> fireSounds = new ArrayList<Pair<SoundDescriptor, Predicate<ConditionContext>>>(this.fireSounds);
            if (this.fireSoundDescriptor != null) {
                fireSounds.add((Pair<SoundDescriptor, Predicate<ConditionContext>>)Pair.of((Object)this.fireSoundDescriptor, this.condition));
            }
            return new SoundFeature(featureProvider, this.condition, fireSounds);
        }
    }
}

