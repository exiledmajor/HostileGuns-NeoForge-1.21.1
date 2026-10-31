/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package com.vicmatskiv.pointblank.item;

import com.mojang.datafixers.util.Pair;
import com.vicmatskiv.pointblank.Nameable;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.client.effect.EffectBuilder;
import com.vicmatskiv.pointblank.feature.ConditionContext;
import com.vicmatskiv.pointblank.feature.FeatureProvider;
import com.vicmatskiv.pointblank.item.AmmoItem;
import com.vicmatskiv.pointblank.item.AnimationProvider;
import com.vicmatskiv.pointblank.item.FireMode;
import com.vicmatskiv.pointblank.item.GunItem;
import com.vicmatskiv.pointblank.registry.AmmoRegistry;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class FireModeInstance
implements Comparable<FireModeInstance> {
    private static final Map<UUID, FireModeInstance> fireModesById = new HashMap<UUID, FireModeInstance>();
    private final UUID id;
    private String name;
    private final Component displayName;
    private final FireMode type;
    private Supplier<AmmoItem> ammoSupplier;
    private int maxAmmoCapacity;
    private int rpm;
    private int burstShots;
    private float damage;
    private boolean isUsingDefaultMuzzle;
    private AnimationProvider prepareFireAnimationProvider;
    private AnimationProvider fireAnimationProvider;
    private AnimationProvider completeFireAnimationProvider;
    private AnimationProvider enableFireModeAnimationProvider;
    private ViewShakeDescriptor viewShakeDescriptor;
    private Map<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> effectBuilders;
    private FeatureProvider featureProvider;
    private int maxShootingDistance;
    private int pelletCount = 0;
    private double pelletSpread = 1.0;

    private FireModeInstance(String name, FeatureProvider featureProvider, FireMode type, Component displayName, Supplier<AmmoItem> ammoSupplier, int maxAmmoCapacity, int rpm, int burstShots, float damage, int maxShootingDistance, int pelletCount, double pelletSpread, boolean isUsingDefaultMuzzle, AnimationProvider prepareFireAnimationProvider, AnimationProvider fireAnimationProvider, AnimationProvider completeFireAnimationProvider, AnimationProvider enableFireModeAnimationProvider, ViewShakeDescriptor viewShakeDescriptor, Map<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> effectBuilders) {
        String string;
        this.name = name;
        this.featureProvider = featureProvider;
        if (featureProvider instanceof Nameable) {
            Nameable n = (Nameable)((Object)featureProvider);
            string = n.getName();
        } else {
            string = featureProvider.toString();
        }
        String ownerName = string;
        this.id = UUID.nameUUIDFromBytes((ownerName + ":" + name + ":" + String.valueOf((Object)type)).getBytes(Charset.forName("utf-8")));
        if (fireModesById.put(this.id, this) != null) {
            throw new IllegalArgumentException("Duplicate fire mode for item " + name);
        }
        this.displayName = displayName;
        this.type = type;
        this.rpm = rpm;
        this.burstShots = burstShots;
        this.damage = damage;
        this.ammoSupplier = ammoSupplier;
        this.maxAmmoCapacity = maxAmmoCapacity;
        this.pelletCount = pelletCount;
        this.pelletSpread = pelletSpread;
        this.isUsingDefaultMuzzle = isUsingDefaultMuzzle;
        this.prepareFireAnimationProvider = prepareFireAnimationProvider;
        this.fireAnimationProvider = fireAnimationProvider;
        this.completeFireAnimationProvider = completeFireAnimationProvider;
        this.enableFireModeAnimationProvider = enableFireModeAnimationProvider;
        this.viewShakeDescriptor = viewShakeDescriptor;
        this.maxShootingDistance = maxShootingDistance;
        this.effectBuilders = effectBuilders;
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getRpm() {
        return this.rpm;
    }

    public float getDamage() {
        return this.damage;
    }

    public int getMaxShootingDistance() {
        return this.maxShootingDistance;
    }

    public int getPelletCount() {
        return this.pelletCount;
    }

    public double getPelletSpread() {
        return this.pelletSpread;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public boolean isUsingDefaultAmmoPool() {
        return this.ammoSupplier == null || this.ammoSupplier.get() == AmmoRegistry.DEFAULT_AMMO_POOL.get();
    }

    public boolean isUsingDefaultMuzzle() {
        return this.isUsingDefaultMuzzle;
    }

    public AmmoItem getAmmo() {
        return this.ammoSupplier != null ? this.ammoSupplier.get() : AmmoRegistry.DEFAULT_AMMO_POOL.get();
    }

    public List<AmmoItem> getActualAmmo() {
        FeatureProvider featureProvider = this.featureProvider;
        if (featureProvider instanceof GunItem) {
            GunItem gunItem = (GunItem)featureProvider;
            return gunItem.getCompatibleAmmo();
        }
        return Collections.emptyList();
    }

    public int getMaxAmmoCapacity() {
        return this.maxAmmoCapacity;
    }

    public int getBurstShots() {
        return this.burstShots;
    }

    public FireMode getType() {
        return this.type;
    }

    public Map<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> getEffectBuilders() {
        return this.effectBuilders;
    }

    public AnimationProvider.Descriptor getPrepareFireAnimationDescriptor(LivingEntity player, ItemStack itemStack, GunClientState gunClientState) {
        return this.prepareFireAnimationProvider != null ? this.prepareFireAnimationProvider.getDescriptor(player, itemStack, gunClientState) : null;
    }

    public AnimationProvider.Descriptor getFireAnimationDescriptor(LivingEntity player, ItemStack itemStack, GunClientState gunClientState) {
        return this.fireAnimationProvider.getDescriptor(player, itemStack, gunClientState);
    }

    public AnimationProvider.Descriptor getCompleteFireAnimationDescriptor(LivingEntity player, ItemStack itemStack, GunClientState gunClientState) {
        return this.completeFireAnimationProvider != null ? this.completeFireAnimationProvider.getDescriptor(player, itemStack, gunClientState) : null;
    }

    public AnimationProvider.Descriptor getEnableFireModeAnimationDescriptor(LivingEntity player, ItemStack itemStack, GunClientState gunClientState) {
        return this.enableFireModeAnimationProvider != null ? this.enableFireModeAnimationProvider.getDescriptor(player, itemStack, gunClientState) : null;
    }

    public ViewShakeDescriptor getViewShakeDescriptor() {
        return this.viewShakeDescriptor;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public int compareTo(FireModeInstance other) {
        return this.id.compareTo(other.id);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        FireModeInstance fireModeInstance = (FireModeInstance)obj;
        return this.id.equals(fireModeInstance.id);
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public static FireModeInstance getOrElse(UUID id, FireModeInstance _default) {
        FireModeInstance result = fireModesById.get(id);
        if (result == null) {
            result = _default;
        }
        return result;
    }

    public static FireModeInstance create(String name, FeatureProvider featureProvider, Component displayName, FireMode type, Supplier<AmmoItem> ammoSupplier, int maxAmmoCapacity, int rpm, int burstShots, double damage, int maxShootingDistance, int pelletCount, double pelletSpread, boolean isUsingDefaultMuzzle, AnimationProvider prepareFireAnimationProvider, AnimationProvider fireAnimationProvider, AnimationProvider completeFireAnimationProvider, AnimationProvider changeFireModeAnimationProvider, ViewShakeDescriptor viewShakeDescriptor, Map<GunItem.FirePhase, List<Pair<Supplier<EffectBuilder<? extends EffectBuilder<?, ?>, ?>>, Predicate<ConditionContext>>>> effectBuilders) {
        FireModeInstance fireMode = new FireModeInstance(name, featureProvider, type, displayName, ammoSupplier, maxAmmoCapacity, rpm, burstShots, (float)damage, maxShootingDistance, pelletCount, pelletSpread, isUsingDefaultMuzzle, prepareFireAnimationProvider, fireAnimationProvider, completeFireAnimationProvider, changeFireModeAnimationProvider, viewShakeDescriptor, effectBuilders);
        return fireMode;
    }

    public void writeToBuf(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.id);
    }

    public static FireModeInstance readFromBuf(FriendlyByteBuf buffer) {
        UUID id = buffer.readUUID();
        return fireModesById.get(id);
    }

    public record ViewShakeDescriptor(long duration, double amplitude, double speed) {
    }
}

