/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.ConditionalFeature;
import com.vicmatskiv.pointblank.feature.FeatureBuilder;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.item.AmmoItem;
import com.vicmatskiv.pointblank.item.AnimationProvider;
import com.vicmatskiv.pointblank.item.ConditionalAnimationProvider;
import com.vicmatskiv.pointblank.item.FireMode;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.registry.AmmoRegistry;
import com.vicmatskiv.pointblank.registry.EffectRegistry;
import com.vicmatskiv.pointblank.registry.ItemRegistry;
import com.vicmatskiv.pointblank.util.Conditions;
import com.vicmatskiv.pointblank.util.JsonUtil;
import com.vicmatskiv.pointblank.util.TimeUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FireModeFeature
extends ConditionalFeature {
    public static final int DEFAULT_RPM = -1;
    public static final int DEFAULT_BURST_SHOTS = -1;
    private static final int DEFAULT_MAX_AMMO_CAPACITY = 1;
    private static final double DEFAULT_SHAKE_RECOIL_AMPLITUDE = 0.5;
    private static final double DEFAULT_SHAKE_RECOIL_SPEED = 8.0;
    private static final int DEFAULT_SHAKE_RECOIL_DURATION = 400;
    private static final AnimationProvider DEFAULT_ANIMATION_PROVIDER = new AnimationProvider.Simple("animation.model.fire");
    private List<FireModeInstance> fireModeInstances;
    private Map<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> effectBuilders;

    private FireModeFeature(FeatureProvider owner, Predicate<ConditionContext> predicate, List<FireModeInstance> fireModes) {
        super(owner, predicate);
        this.fireModeInstances = fireModes;
        this.effectBuilders = new HashMap();
        for (FireModeInstance fireModeInstance : this.fireModeInstances) {
            Map<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> fireModeInstanceEffectBuilders = fireModeInstance.getEffectBuilders();
            for (Map.Entry<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> e : fireModeInstanceEffectBuilders.entrySet()) {
                List firePhaseEffectBuilders = this.effectBuilders.computeIfAbsent(e.getKey(), k -> new ArrayList());
                firePhaseEffectBuilders.addAll((Collection)e.getValue());
            }
        }
    }

    public List<FireModeInstance> getFireModes() {
        return this.fireModeInstances;
    }

    @Override
    public Map<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> getEffectBuilders() {
        return this.effectBuilders;
    }

    public MutableComponent getDescription() {
        MutableComponent description = Component.translatable((String)"label.pointblank.fireMode").append(": ");
        boolean isFirst = true;
        for (FireModeInstance instance : this.fireModeInstances) {
            if (!isFirst) {
                description.append(", ");
            }
            isFirst = false;
            description.append(instance.getDisplayName());
        }
        return description;
    }

    public static int getRpm(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return 0;
        }
        GunItem gunItem = (GunItem)item;
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        int rpm = fireModeInstance.getRpm();
        return rpm != -1 ? rpm : gunItem.getRpm();
    }

    public static float getDamage(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof AmmoItem) {
            AmmoItem ammoItem = (AmmoItem)item;
            return ammoItem.getDamage();
        }
        if (!(itemStack.getItem() instanceof GunItem)) {
            return 0.0f;
        }
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        return fireModeInstance.getDamage();
    }

    public static int getMaxShootingDistance(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return 200;
        }
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        return fireModeInstance.getMaxShootingDistance();
    }

    public static String getFireAnimation(LivingEntity player, GunClientState state, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return null;
        }
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        if (fireModeInstance == null) {
            return null;
        }
        AnimationProvider.Descriptor descriptor = fireModeInstance.getFireAnimationDescriptor(player, itemStack, state);
        return descriptor != null ? descriptor.animationName() : null;
    }

    public static String getPrepareFireAnimation(LivingEntity player, GunClientState state, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return null;
        }
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        if (fireModeInstance == null) {
            return null;
        }
        AnimationProvider.Descriptor descriptor = fireModeInstance.getPrepareFireAnimationDescriptor(player, itemStack, state);
        return descriptor != null ? descriptor.animationName() : null;
    }

    public static String getCompleteFireAnimation(LivingEntity player, GunClientState state, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return null;
        }
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        if (fireModeInstance == null) {
            return null;
        }
        AnimationProvider.Descriptor descriptor = fireModeInstance.getCompleteFireAnimationDescriptor(player, itemStack, state);
        return descriptor != null ? descriptor.animationName() : null;
    }

    public static String getEnableFireModeAnimation(LivingEntity player, GunClientState state, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return null;
        }
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        if (fireModeInstance == null) {
            return null;
        }
        AnimationProvider.Descriptor descriptor = fireModeInstance.getEnableFireModeAnimationDescriptor(player, itemStack, state);
        return descriptor != null ? descriptor.animationName() : null;
    }

    public static FireModeInstance.ViewShakeDescriptor getViewShakeDescriptor(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof GunItem)) {
            return null;
        }
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        return fireModeInstance.getViewShakeDescriptor();
    }

    public static long getPrepareFireCooldownDuration(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return 0L;
        }
        GunItem gunItem = (GunItem)item;
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        if (fireModeInstance == null) {
            return gunItem.getPrepareFireCooldownDuration();
        }
        AnimationProvider.Descriptor descriptor = fireModeInstance.getPrepareFireAnimationDescriptor(player, itemStack, state);
        return descriptor != null ? descriptor.timeUnit().toMillis(descriptor.duration()) : gunItem.getPrepareFireCooldownDuration();
    }

    public static long getCompleteFireCooldownDuration(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return 0L;
        }
        GunItem gunItem = (GunItem)item;
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        if (fireModeInstance == null) {
            return gunItem.getCompleteFireCooldownDuration();
        }
        AnimationProvider.Descriptor descriptor = fireModeInstance.getCompleteFireAnimationDescriptor(player, itemStack, state);
        return descriptor != null ? descriptor.timeUnit().toMillis(descriptor.duration()) : gunItem.getCompleteFireCooldownDuration();
    }

    public static long getEnableFireModeCooldownDuration(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return 0L;
        }
        GunItem gunItem = (GunItem)item;
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        if (fireModeInstance == null) {
            return gunItem.getEnableFireModeCooldownDuration();
        }
        AnimationProvider.Descriptor descriptor = fireModeInstance.getEnableFireModeAnimationDescriptor(player, itemStack, state);
        return descriptor != null ? descriptor.timeUnit().toMillis(descriptor.duration()) : gunItem.getEnableFireModeCooldownDuration();
    }

    public static Pair<Integer, Double> getPelletCountAndSpread(LivingEntity player, GunClientState state, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (!(item instanceof GunItem)) {
            return Pair.of((Object)0, (Object)1.0);
        }
        GunItem gunItem = (GunItem)item;
        FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
        if (fireModeInstance == null) {
            return Pair.of((Object)gunItem.getPelletCount(), (Object)gunItem.getPelletSpread());
        }
        return Pair.of((Object)fireModeInstance.getPelletCount(), (Object)fireModeInstance.getPelletSpread());
    }

    public static class Builder
    implements FeatureBuilder<Builder, FireModeFeature> {
        private Predicate<ConditionContext> condition = ctx -> true;
        private List<FireModeDescriptor> fireModes = new ArrayList<FireModeDescriptor>();

        public Builder withCondition(Predicate<ConditionContext> condition) {
            this.condition = condition;
            return this;
        }

        public Builder withFireMode(String name, FireMode type, Supplier<AmmoItem> ammoSupplier, int maxAmmoCapacity, int rpm, double damage, String fireAnimationName) {
            FireModeDescriptor fireModeDesriptor = new FireModeDescriptor(name, (Component)Component.translatable((String)name), type, ammoSupplier, maxAmmoCapacity, rpm, -1, (float)damage, 200, 0, 1.0, true, null, new AnimationProvider.Simple(fireAnimationName), null, null, null, Collections.emptyMap());
            return this.withFireMode(fireModeDesriptor);
        }

        public Builder withFireMode(String name, FireMode type, Supplier<AmmoItem> ammoSupplier, int maxAmmoCapacity, int rpm, double damage, AnimationProvider fireAnimationProvider) {
            FireModeDescriptor fireModeDesriptor = new FireModeDescriptor(name, (Component)Component.translatable((String)name), type, ammoSupplier, maxAmmoCapacity, rpm, -1, (float)damage, 200, 0, 1.0, true, null, fireAnimationProvider, null, null, null, Collections.emptyMap());
            return this.withFireMode(fireModeDesriptor);
        }

        public Builder withFireMode(String name, FireMode type, Supplier<AmmoItem> ammoSupplier, int maxAmmoCapacity, int rpm, int burstShots, double damage, String fireAnimationName) {
            FireModeDescriptor fireModeDesriptor = new FireModeDescriptor(name, (Component)Component.translatable((String)name), type, ammoSupplier, maxAmmoCapacity, rpm, burstShots, (float)damage, 200, 0, 1.0, true, null, new AnimationProvider.Simple(fireAnimationName), null, null, null, Collections.emptyMap());
            return this.withFireMode(fireModeDesriptor);
        }

        public Builder withFireMode(String name, FireMode type, Component displayName, int rpm, double damage, boolean isUsingDefaultMuzzle, String fireAnimationName, FireModeInstance.ViewShakeDescriptor viewShakeDescriptor) {
            return this.withFireMode(name, type, displayName, AmmoRegistry.DEFAULT_AMMO_POOL, 0, rpm, -1, damage, 200, isUsingDefaultMuzzle, fireAnimationName, viewShakeDescriptor);
        }

        public Builder withFireMode(String name, FireMode type, Component displayName, int rpm, int burstShots, double damage, boolean isUsingDefaultMuzzle, String fireAnimationName, FireModeInstance.ViewShakeDescriptor viewShakeDescriptor) {
            return this.withFireMode(name, type, displayName, AmmoRegistry.DEFAULT_AMMO_POOL, 0, rpm, burstShots, damage, 200, isUsingDefaultMuzzle, fireAnimationName, viewShakeDescriptor);
        }

        public Builder withFireMode(String name, FireMode type, Component displayName, Supplier<AmmoItem> ammoSupplier, int maxAmmoCapacity, int rpm, double damage, boolean isUsingDefaultMuzzle, String fireAnimationName, FireModeInstance.ViewShakeDescriptor viewShakeDescriptor) {
            FireModeDescriptor fireModeDesriptor = new FireModeDescriptor(name, displayName, type, ammoSupplier, maxAmmoCapacity, rpm, -1, damage, 200, 0, 1.0, isUsingDefaultMuzzle, null, new AnimationProvider.Simple(fireAnimationName), null, null, viewShakeDescriptor, Collections.emptyMap());
            return this.withFireMode(fireModeDesriptor);
        }

        public Builder withFireMode(String name, FireMode type, Component displayName, Supplier<AmmoItem> ammoSupplier, int maxAmmoCapacity, int rpm, int burstShots, double damage, int maxShootingDistance, boolean isUsingDefaultMuzzle, String fireAnimationName, FireModeInstance.ViewShakeDescriptor viewShakeDescriptor) {
            FireModeDescriptor fireModeDesriptor = new FireModeDescriptor(name, displayName, type, ammoSupplier, maxAmmoCapacity, rpm, burstShots, damage, 200, 0, 1.0, isUsingDefaultMuzzle, null, new AnimationProvider.Simple(fireAnimationName), null, null, viewShakeDescriptor, Collections.emptyMap());
            return this.withFireMode(fireModeDesriptor);
        }

        public Builder withFireMode(String name, FireMode type, Component displayName, Supplier<AmmoItem> ammoSupplier, int maxAmmoCapacity, int rpm, int burstShots, double damage, int maxShootingDistance, boolean isUsingDefaultMuzzle, AnimationProvider fireAnimationProvider, FireModeInstance.ViewShakeDescriptor viewShakeDescriptor) {
            FireModeDescriptor fireModeDesriptor = new FireModeDescriptor(name, displayName, type, ammoSupplier, maxAmmoCapacity, rpm, burstShots, damage, 200, 0, 1.0, isUsingDefaultMuzzle, null, fireAnimationProvider, null, null, viewShakeDescriptor, Collections.emptyMap());
            return this.withFireMode(fireModeDesriptor);
        }

        public Builder withFireMode(FireModeDescriptor descriptor) {
            this.fireModes.add(descriptor);
            return this;
        }

        @Override
        public Builder withJsonObject(JsonObject obj) {
            if (obj.has("condition")) {
                this.withCondition(Conditions.fromJson((JsonElement)obj.getAsJsonObject("condition")));
            }
            for (JsonObject fireModeObj : JsonUtil.getJsonObjects(obj, "fireModes")) {
                FireModeDescriptor.Builder fireModeBuilder = new FireModeDescriptor.Builder();
                String name = JsonUtil.getJsonString(fireModeObj, "name");
                fireModeBuilder.withName(name);
                fireModeBuilder.withDisplayName((Component)Component.translatable((String)JsonUtil.getJsonString(fireModeObj, "displayName", name)));
                fireModeBuilder.withType(JsonUtil.getEnum(fireModeObj, "type", FireMode.class, FireMode.SINGLE, true));
                String ammoName = JsonUtil.getJsonString(fireModeObj, "ammo", null);
                Supplier<AmmoItem> ammoSupplier = null;
                ammoSupplier = ammoName != null ? ItemRegistry.ITEMS.getDeferredRegisteredObject(ammoName) : AmmoRegistry.DEFAULT_AMMO_POOL;
                fireModeBuilder.withAmmoSupplier(ammoSupplier);
                fireModeBuilder.withRpm(JsonUtil.getJsonInt(fireModeObj, "rpm", -1));
                fireModeBuilder.withBurstShots(JsonUtil.getJsonInt(fireModeObj, "burstShots", -1));
                fireModeBuilder.withMaxAmmoCapacity(JsonUtil.getJsonInt(fireModeObj, "maxAmmoCapacity", 1));
                fireModeBuilder.withPelletCount(JsonUtil.getJsonInt(fireModeObj, "pelletCount", 0));
                fireModeBuilder.withPelletSpread(JsonUtil.getJsonDouble(fireModeObj, "pelletSpread", 1.0));
                fireModeBuilder.withIsUsingDefaultMuzzle(JsonUtil.getJsonBoolean(fireModeObj, "isUsingDefaultMuzzle", true));
                FireModeInstance.ViewShakeDescriptor viewShakeDescriptor = null;
                if (fireModeObj.has("shakeRecoilAmplitude") || fireModeObj.has("shakeRecoilSpeed") || fireModeObj.has("shakeRecoilDuration")) {
                    long shakeRecoilDuration = JsonUtil.getJsonInt(fireModeObj, "shakeRecoilDuration", 400);
                    double shakeRecoilAmplitude = JsonUtil.getJsonDouble(fireModeObj, "shakeRecoilAmplitude", 0.5);
                    double shakeRecoilSpeed = JsonUtil.getJsonDouble(fireModeObj, "shakeRecoilSpeed", 8.0);
                    viewShakeDescriptor = new FireModeInstance.ViewShakeDescriptor(shakeRecoilDuration, shakeRecoilAmplitude, shakeRecoilSpeed);
                }
                fireModeBuilder.withViewShakeDescriptor(viewShakeDescriptor);
                fireModeBuilder.withDamage(JsonUtil.getJsonDouble(fireModeObj, "damage", 5.0));
                fireModeBuilder.withMaxShootingDistance(JsonUtil.getJsonInt(fireModeObj, "maxShootingDistance", 200));
                String fireAnimationName = JsonUtil.getJsonString(fireModeObj, "animationName", null);
                if (fireAnimationName != null) {
                    fireModeBuilder.withFireAnimationProvider(new AnimationProvider.Simple(fireAnimationName));
                }
                ConditionalAnimationProvider.Builder fireAnimationProvider = new ConditionalAnimationProvider.Builder();
                for (JsonObject jsonObject : JsonUtil.getJsonObjects(fireModeObj, "fireAnimations")) {
                    String animationName = JsonUtil.getJsonString(jsonObject, "name");
                    Predicate<ConditionContext> animationCondition = Conditions.fromJson(jsonObject.get("condition"));
                    fireAnimationProvider.withAnimation(animationName, animationCondition, 0L, TimeUnit.MILLISECOND);
                }
                if (!fireAnimationProvider.getAnimations().isEmpty()) {
                    fireModeBuilder.withFireAnimationProvider(fireAnimationProvider.build());
                }
                ConditionalAnimationProvider.Builder prepareFireAnimationProvider = new ConditionalAnimationProvider.Builder();
                for (JsonObject prepareFireAnimationObj : JsonUtil.getJsonObjects(fireModeObj, "prepareFireAnimations")) {
                    String animationName = JsonUtil.getJsonString(prepareFireAnimationObj, "name");
                    int animationDuration = JsonUtil.getJsonInt(prepareFireAnimationObj, "duration");
                    Predicate<ConditionContext> animationCondition = prepareFireAnimationObj.has("condition") ? Conditions.fromJson(prepareFireAnimationObj.get("condition")) : ctx -> true;
                    prepareFireAnimationProvider.withAnimation(animationName, animationCondition, animationDuration, TimeUnit.MILLISECOND);
                }
                if (!prepareFireAnimationProvider.getAnimations().isEmpty()) {
                    fireModeBuilder.withPrepareFireAnimationProvider(prepareFireAnimationProvider.build());
                }
                ConditionalAnimationProvider.Builder builder = new ConditionalAnimationProvider.Builder();
                for (JsonObject completeFireAnimationObj : JsonUtil.getJsonObjects(fireModeObj, "completeFireAnimations")) {
                    String animationName = JsonUtil.getJsonString(completeFireAnimationObj, "name");
                    int animationDuration = JsonUtil.getJsonInt(completeFireAnimationObj, "duration");
                    Predicate<ConditionContext> animationCondition = completeFireAnimationObj.has("condition") ? Conditions.fromJson(completeFireAnimationObj.get("condition")) : ctx -> true;
                    builder.withAnimation(animationName, animationCondition, animationDuration, TimeUnit.MILLISECOND);
                }
                if (!builder.getAnimations().isEmpty()) {
                    fireModeBuilder.withCompleteFireAnimationProvider(builder.build());
                }
                ConditionalAnimationProvider.Builder enableFireModeAnimationProvider = new ConditionalAnimationProvider.Builder();
                for (JsonObject enableFireModeAnimationObj : JsonUtil.getJsonObjects(fireModeObj, "enableFireModeAnimations")) {
                    String animationName = JsonUtil.getJsonString(enableFireModeAnimationObj, "name");
                    int animationDuration = JsonUtil.getJsonInt(enableFireModeAnimationObj, "duration");
                    Predicate<ConditionContext> animationCondition = Conditions.fromJson(obj.get("condition"));
                    enableFireModeAnimationProvider.withAnimation(animationName, animationCondition, animationDuration, TimeUnit.MILLISECOND);
                }
                if (!enableFireModeAnimationProvider.getAnimations().isEmpty()) {
                    fireModeBuilder.withEnableFireModeAnimationProvider(enableFireModeAnimationProvider.build());
                }
                for (JsonObject effect : JsonUtil.getJsonObjects(fireModeObj, "effects")) {
                    GunItem.FirePhase firePhase = JsonUtil.getEnum(effect, "phase", GunItem.FirePhase.class, null, true);
                    String effectName = JsonUtil.getJsonString(effect, "name");
                    Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>> supplier = () -> EffectRegistry.getEffectBuilderSupplier(effectName).get();
                    Predicate<ConditionContext> condition = Conditions.selectedFireMode(name);
                    if (effect.has("condition")) {
                        JsonObject conditionObj = effect.getAsJsonObject("condition");
                        condition = condition.and(Conditions.fromJson((JsonElement)conditionObj));
                    }
                    fireModeBuilder.withEffect(firePhase, supplier, condition);
                }
                this.withFireMode(fireModeBuilder.build());
            }
            return this;
        }

        @Override
        public FireModeFeature build(FeatureProvider featureProvider) {
            return new FireModeFeature(featureProvider, this.condition, Collections.unmodifiableList(this.fireModes.stream().map(info -> FireModeInstance.create(info.name, featureProvider, info.displayName, info.type, info.ammoSupplier, info.maxAmmoCapacity, info.rpm, info.burstShots, info.damage, info.maxShootingDistance, info.pelletCount, info.pelletSpread, info.isUsingDefaultMuzzle, info.prepareFireAnimationProvider, info.fireAnimationProvider, info.completeFireAnimationProvider, info.enableFireModeAnimationProvider, info.viewShakeDescriptor, info.effectBuilders)).toList()));
        }
    }

    public record FireModeDescriptor(String name, Component displayName, FireMode type, Supplier<AmmoItem> ammoSupplier, int maxAmmoCapacity, int rpm, int burstShots, double damage, int maxShootingDistance, int pelletCount, double pelletSpread, boolean isUsingDefaultMuzzle, AnimationProvider prepareFireAnimationProvider, AnimationProvider fireAnimationProvider, AnimationProvider completeFireAnimationProvider, AnimationProvider enableFireModeAnimationProvider, FireModeInstance.ViewShakeDescriptor viewShakeDescriptor, Map<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> effectBuilders) {

        public static class Builder {
            private String name;
            private FireMode type = FireMode.SINGLE;
            private Component displayName;
            private Supplier<AmmoItem> ammoSupplier = AmmoRegistry.DEFAULT_AMMO_POOL;
            private int maxAmmoCapacity = 1;
            private int rpm = -1;
            private int burstShots = -1;
            private double damage = 5.0;
            private int maxShootingDistance = 200;
            private boolean isUsingDefaultMuzzle = true;
            private AnimationProvider prepareFireAnimationProvider;
            private AnimationProvider fireAnimationProvider = DEFAULT_ANIMATION_PROVIDER;
            private AnimationProvider completeFireAnimationProvider;
            private AnimationProvider enableFireModeAnimationProvider;
            private FireModeInstance.ViewShakeDescriptor viewShakeDescriptor;
            private int pelletCount;
            private double pelletSpread = 1.0;
            private Map<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> effectBuilders = new HashMap();

            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            public Builder withType(FireMode type) {
                this.type = type;
                return this;
            }

            public Builder withDisplayName(Component displayName) {
                this.displayName = displayName;
                return this;
            }

            public Builder withAmmoSupplier(Supplier<AmmoItem> ammoSupplier) {
                this.ammoSupplier = ammoSupplier;
                return this;
            }

            public Builder withMaxAmmoCapacity(int maxAmmoCapacity) {
                this.maxAmmoCapacity = maxAmmoCapacity;
                return this;
            }

            public Builder withRpm(int rpm) {
                this.rpm = rpm;
                return this;
            }

            public Builder withBurstShots(int burstShots) {
                this.burstShots = burstShots;
                return this;
            }

            public Builder withDamage(double damage) {
                this.damage = damage;
                return this;
            }

            public Builder withMaxShootingDistance(int maxShootingDistance) {
                this.maxShootingDistance = maxShootingDistance;
                return this;
            }

            public Builder withPelletCount(int pelletCount) {
                this.pelletCount = pelletCount;
                return this;
            }

            public Builder withPelletSpread(double pelletSpread) {
                this.pelletSpread = pelletSpread;
                return this;
            }

            public Builder withIsUsingDefaultMuzzle(boolean isUsingDefaultMuzzle) {
                this.isUsingDefaultMuzzle = isUsingDefaultMuzzle;
                return this;
            }

            public Builder withPrepareFireAnimationProvider(AnimationProvider prepareFireAnimationProvider) {
                this.prepareFireAnimationProvider = prepareFireAnimationProvider;
                return this;
            }

            public Builder withFireAnimationProvider(AnimationProvider fireAnimationProvider) {
                this.fireAnimationProvider = fireAnimationProvider;
                return this;
            }

            public Builder withCompleteFireAnimationProvider(AnimationProvider completeFireAnimationProvider) {
                this.completeFireAnimationProvider = completeFireAnimationProvider;
                return this;
            }

            public Builder withEnableFireModeAnimationProvider(AnimationProvider enableFireModeAnimationProvider) {
                this.enableFireModeAnimationProvider = enableFireModeAnimationProvider;
                return this;
            }

            public Builder withViewShakeDescriptor(FireModeInstance.ViewShakeDescriptor viewShakeDescriptor) {
                this.viewShakeDescriptor = viewShakeDescriptor;
                return this;
            }

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

            public FireModeDescriptor build() {
                if (this.pelletCount > 1) {
                    this.maxShootingDistance = 50;
                }
                return new FireModeDescriptor(this.name, (Component)(this.displayName != null ? this.displayName : Component.translatable((String)"label.pointblank.fireMode.single")), this.type, this.ammoSupplier, this.maxAmmoCapacity, this.rpm, this.burstShots, this.damage, this.maxShootingDistance, this.pelletCount, this.pelletSpread, this.isUsingDefaultMuzzle, this.prepareFireAnimationProvider, this.fireAnimationProvider, this.completeFireAnimationProvider, this.enableFireModeAnimationProvider, this.viewShakeDescriptor, this.effectBuilders);
            }
        }
    }
}

