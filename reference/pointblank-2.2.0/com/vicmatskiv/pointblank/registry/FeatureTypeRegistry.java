/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.registry;

import com.vicmatskiv.pointblank.feature.AccuracyFeature;
import com.vicmatskiv.pointblank.feature.ActiveMuzzleFeature;
import com.vicmatskiv.pointblank.feature.AimingFeature;
import com.vicmatskiv.pointblank.feature.AlternatingMuzzleFeature;
import com.vicmatskiv.pointblank.feature.AmmoCapacityFeature;
import com.vicmatskiv.pointblank.feature.AmmoCounterFeature;
import com.vicmatskiv.pointblank.feature.DamageFeature;
import com.vicmatskiv.pointblank.feature.DualWieldFeature;
import com.vicmatskiv.pointblank.feature.DynamicRpmFeature;
import com.vicmatskiv.pointblank.feature.Feature;
import com.vicmatskiv.pointblank.feature.FireModeFeature;
import com.vicmatskiv.pointblank.feature.GlowFeature;
import com.vicmatskiv.pointblank.feature.MuzzleFlashFeature;
import com.vicmatskiv.pointblank.feature.OverheatFeature;
import com.vicmatskiv.pointblank.feature.PartVisibilityFeature;
import com.vicmatskiv.pointblank.feature.PipFeature;
import com.vicmatskiv.pointblank.feature.RecoilFeature;
import com.vicmatskiv.pointblank.feature.ReticleFeature;
import com.vicmatskiv.pointblank.feature.ShellEjectionFeature;
import com.vicmatskiv.pointblank.feature.SkinFeature;
import com.vicmatskiv.pointblank.feature.SoundFeature;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FeatureTypeRegistry {
    private static int featureTypeId;
    private static final Map<Class<? extends Feature>, Integer> typeToId;
    private static final Map<Integer, Class<? extends Feature>> idToType;
    public static final int AIMING_FEATURE;
    public static final int PIP_FEATURE;
    public static final int RETICLE_FEATURE;
    public static final int MUZZLE_FLASH_FEATURE;
    public static final int OVERHEAT_FEATURE;
    public static final int GLOW_FEATURE;
    public static final int VISIBILITY_FEATURE;
    public static final int RECOIL_FEATURE;
    public static final int ACCURACY_FEATURE;
    public static final int ACTIVE_MUZZLE_FEATURE;
    public static final int AMMO_CAPACITY_FEATURE;
    public static final int AMMO_COUNTER_FEATURE;
    public static final int DAMAGE_FEATURE;
    public static final int SKIN_FEATURE;
    public static final int SOUND_FEATURE;
    public static final int FIRE_MODE_FEATURE;
    public static final int SHELL_EJECTION_FEATURE;
    public static final int DUAL_WIELD_FEATURE;
    public static final int ALTERNATING_MUZZLE_FEATURE;
    public static final int DYNAMIC_RPM_FEATURE;

    private static int registerFeatureType(Class<? extends Feature> featureType) {
        if (typeToId.put(featureType, ++featureTypeId) != null) {
            throw new IllegalArgumentException("Duplicate feature type: " + String.valueOf(featureType));
        }
        idToType.put(featureTypeId, featureType);
        return featureTypeId;
    }

    public static Map<Integer, Class<? extends Feature>> getFeatureTypes() {
        return Collections.unmodifiableMap(idToType);
    }

    public static Class<? extends Feature> getFeatureType(int featureTypeId) {
        return idToType.get(featureTypeId);
    }

    public static int getFeatureTypeId(Class<? extends Feature> featureType) {
        Integer id = typeToId.get(featureType);
        if (id == null) {
            throw new IllegalArgumentException("Feature type not registered: " + String.valueOf(featureType));
        }
        return id;
    }

    public static void init() {
    }

    static {
        typeToId = new HashMap<Class<? extends Feature>, Integer>();
        idToType = new HashMap<Integer, Class<? extends Feature>>();
        AIMING_FEATURE = FeatureTypeRegistry.registerFeatureType(AimingFeature.class);
        PIP_FEATURE = FeatureTypeRegistry.registerFeatureType(PipFeature.class);
        RETICLE_FEATURE = FeatureTypeRegistry.registerFeatureType(ReticleFeature.class);
        MUZZLE_FLASH_FEATURE = FeatureTypeRegistry.registerFeatureType(MuzzleFlashFeature.class);
        OVERHEAT_FEATURE = FeatureTypeRegistry.registerFeatureType(OverheatFeature.class);
        GLOW_FEATURE = FeatureTypeRegistry.registerFeatureType(GlowFeature.class);
        VISIBILITY_FEATURE = FeatureTypeRegistry.registerFeatureType(PartVisibilityFeature.class);
        RECOIL_FEATURE = FeatureTypeRegistry.registerFeatureType(RecoilFeature.class);
        ACCURACY_FEATURE = FeatureTypeRegistry.registerFeatureType(AccuracyFeature.class);
        ACTIVE_MUZZLE_FEATURE = FeatureTypeRegistry.registerFeatureType(ActiveMuzzleFeature.class);
        AMMO_CAPACITY_FEATURE = FeatureTypeRegistry.registerFeatureType(AmmoCapacityFeature.class);
        AMMO_COUNTER_FEATURE = FeatureTypeRegistry.registerFeatureType(AmmoCounterFeature.class);
        DAMAGE_FEATURE = FeatureTypeRegistry.registerFeatureType(DamageFeature.class);
        SKIN_FEATURE = FeatureTypeRegistry.registerFeatureType(SkinFeature.class);
        SOUND_FEATURE = FeatureTypeRegistry.registerFeatureType(SoundFeature.class);
        FIRE_MODE_FEATURE = FeatureTypeRegistry.registerFeatureType(FireModeFeature.class);
        SHELL_EJECTION_FEATURE = FeatureTypeRegistry.registerFeatureType(ShellEjectionFeature.class);
        DUAL_WIELD_FEATURE = FeatureTypeRegistry.registerFeatureType(DualWieldFeature.class);
        ALTERNATING_MUZZLE_FEATURE = FeatureTypeRegistry.registerFeatureType(AlternatingMuzzleFeature.class);
        DYNAMIC_RPM_FEATURE = FeatureTypeRegistry.registerFeatureType(DynamicRpmFeature.class);
    }
}

