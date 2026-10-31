/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank;

import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.config.ConfigManager;
import com.vicmatskiv.pointblank.event.ModConfigEvent;
import java.util.List;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config {
    private static final Logger LOGGER = LogManager.getLogger(Config.class);
    protected Supplier<AutoReload> AUTO_RELOAD;
    protected Supplier<Double> SCOPE_AIMING_MOUSE_SENSITIVITY;
    protected Supplier<Boolean> RESET_AUTO_FIRE_PITCH_ENABLED;
    protected Supplier<Double> KNOCKBACK;
    protected Supplier<Boolean> PARTICLE_EFFECTS_ENABLED;
    protected Supplier<Integer> ARMS_DEALER_HOUSE_WEIGHT;
    protected Supplier<Double> EMERALD_EXCHANGE_RATE;
    protected Supplier<Boolean> THIRD_PERSON_ARM_POSE_ALWAYS_ON;
    protected Supplier<Integer> PIP_SCOPE_REFRESH_FRAME;
    protected Supplier<Boolean> PIP_SCOPES_ENABLED;
    protected Supplier<List<String>> PIP_FALLBACK_MOD_IDS;
    protected Supplier<Integer> PIP_SCOPE_COLOR_BALANCE_RED;
    protected Supplier<Integer> PIP_SCOPE_COLOR_BALANCE_GREEN;
    protected Supplier<Integer> PIP_SCOPE_COLOR_BALANCE_BLUE;
    protected Supplier<Boolean> CUSTOM_SHADERS_ENABLED;
    protected Supplier<Boolean> EXPLOSION_DESTROY_BLOCKS_ENABLED;
    protected Supplier<Double> ITEM_DROP_CHANCE;
    protected Supplier<Integer> MAX_ITEM_DROP_COUNT;
    protected Supplier<Boolean> BULLETS_BREAK_GLASS_ENABLED;
    protected Supplier<Double> HITSCAN_DAMAGE_MODIFIER;
    protected Supplier<CrosshairType> CROSSHAIR_TYPE;
    protected Supplier<Double> HEADSHOT_DAMAGE_MODIFIER;
    protected Supplier<Boolean> GORE_ENABLED;
    protected Supplier<Boolean> GORE_SOUNDS_ENABLED;
    protected Supplier<Boolean> SHELL_EJECTION_ENABLED;
    protected Supplier<ScopeViewType> SCOPE_VIEW_TYPE;
    protected Supplier<Boolean> AMMO_INDICATOR_ENABLED;
    protected Supplier<AmmoIndicatorPlacement> AMMO_INDICATOR_PLACEMENT;
    protected Supplier<Boolean> HEAT_INDICATOR_ENABLED;
    protected Supplier<HeatIndicatorPlacement> HEAT_INDICATOR_PLACEMENT;
    protected Supplier<Boolean> INTERACTION_PROMPT_ENABLED;
    protected Supplier<Boolean> ADVANCED_IRIS_INTEGRATION_ENABLED;
    protected Supplier<Boolean> FIRST_PERSON_ANIMATIONS_ENABLED;
    protected Supplier<Boolean> THIRD_PERSON_ANIMATIONS_ENABLED;
    protected Supplier<String> EXTENSION_PACK_BROWSE_URL;
    public static volatile AutoReload autoReload;
    public static volatile double scopeAimingMouseSensitivity;
    public static volatile boolean resetAutoFirePitchEnabled;
    public static volatile double knockback;
    public static volatile boolean particleEffectsEnabled;
    public static volatile int armsDealerHouseWeight;
    public static volatile double emeraldExchangeRate;
    public static volatile boolean thirdPersonArmPoseAlwaysOn;
    public static volatile boolean pipScopesEnabled;
    public static volatile List<String> pipFallbackModIds;
    public static volatile boolean pipFallbackModDetected;
    public static volatile int pipScopeRefreshFrame;
    public static volatile int pipScopeColorBalanceRed;
    public static volatile int pipScopeColorBalanceGreen;
    public static volatile int pipScopeColorBalanceBlue;
    public static volatile boolean customShadersEnabled;
    public static volatile boolean explosionDestroyBlocksEnabled;
    public static volatile double itemDropChance;
    public static volatile int maxItemDropCount;
    public static volatile boolean bulletsBreakGlassEnabled;
    public static volatile double hitscanDamageModifier;
    public static volatile CrosshairType crosshairType;
    public static volatile double headshotDamageModifier;
    public static volatile boolean goreEnabled;
    public static volatile boolean goreSoundsEnabled;
    public static volatile boolean shellEjectionEnabled;
    public static volatile ScopeViewType scopeViewType;
    public static volatile boolean ammoIndicatorEnabled;
    public static volatile AmmoIndicatorPlacement ammoIndicatorPlacement;
    public static volatile boolean heatIndicatorEnabled;
    public static volatile HeatIndicatorPlacement heatIndicatorPlacement;
    public static volatile boolean interactionPromptEnabled;
    public static volatile boolean advancedIrisIntegrationEnabled;
    public static volatile boolean firstPersonAnimationsEnabled;
    public static volatile boolean thirdPersonAnimationsEnabled;
    public static volatile String extensionPackBrowseUrl;

    public void onLoad(ModConfigEvent event) {
        autoReload = this.AUTO_RELOAD.get();
        scopeAimingMouseSensitivity = this.SCOPE_AIMING_MOUSE_SENSITIVITY.get();
        resetAutoFirePitchEnabled = this.RESET_AUTO_FIRE_PITCH_ENABLED.get();
        knockback = this.KNOCKBACK.get();
        particleEffectsEnabled = this.PARTICLE_EFFECTS_ENABLED.get();
        armsDealerHouseWeight = this.ARMS_DEALER_HOUSE_WEIGHT.get();
        emeraldExchangeRate = this.EMERALD_EXCHANGE_RATE.get();
        thirdPersonArmPoseAlwaysOn = this.THIRD_PERSON_ARM_POSE_ALWAYS_ON.get();
        pipScopeRefreshFrame = this.PIP_SCOPE_REFRESH_FRAME.get();
        pipScopesEnabled = this.PIP_SCOPES_ENABLED.get();
        pipFallbackModIds = this.PIP_FALLBACK_MOD_IDS != null ? this.PIP_FALLBACK_MOD_IDS.get() : List.of();
        pipFallbackModDetected = this.checkPipFallbackModsLoaded();
        pipScopeColorBalanceRed = this.PIP_SCOPE_COLOR_BALANCE_RED.get();
        pipScopeColorBalanceGreen = this.PIP_SCOPE_COLOR_BALANCE_GREEN.get();
        pipScopeColorBalanceBlue = this.PIP_SCOPE_COLOR_BALANCE_BLUE.get();
        customShadersEnabled = this.CUSTOM_SHADERS_ENABLED.get();
        explosionDestroyBlocksEnabled = this.EXPLOSION_DESTROY_BLOCKS_ENABLED.get();
        itemDropChance = this.ITEM_DROP_CHANCE.get();
        maxItemDropCount = this.MAX_ITEM_DROP_COUNT.get();
        bulletsBreakGlassEnabled = this.BULLETS_BREAK_GLASS_ENABLED.get();
        hitscanDamageModifier = this.HITSCAN_DAMAGE_MODIFIER.get();
        crosshairType = this.CROSSHAIR_TYPE.get();
        headshotDamageModifier = this.HEADSHOT_DAMAGE_MODIFIER.get();
        goreEnabled = this.GORE_ENABLED.get();
        goreSoundsEnabled = this.GORE_SOUNDS_ENABLED.get();
        shellEjectionEnabled = this.SHELL_EJECTION_ENABLED != null ? this.SHELL_EJECTION_ENABLED.get() : true;
        scopeViewType = this.SCOPE_VIEW_TYPE != null ? this.SCOPE_VIEW_TYPE.get() : ScopeViewType.SQUARE;
        ammoIndicatorEnabled = this.AMMO_INDICATOR_ENABLED != null ? this.AMMO_INDICATOR_ENABLED.get() : true;
        ammoIndicatorPlacement = this.AMMO_INDICATOR_PLACEMENT != null ? this.AMMO_INDICATOR_PLACEMENT.get() : AmmoIndicatorPlacement.RIGHT_OF_HOTBAR;
        heatIndicatorEnabled = this.HEAT_INDICATOR_ENABLED != null ? this.HEAT_INDICATOR_ENABLED.get() : true;
        heatIndicatorPlacement = this.HEAT_INDICATOR_PLACEMENT != null ? this.HEAT_INDICATOR_PLACEMENT.get() : HeatIndicatorPlacement.BOTTOM_RIGHT;
        interactionPromptEnabled = this.INTERACTION_PROMPT_ENABLED != null ? this.INTERACTION_PROMPT_ENABLED.get() : true;
        advancedIrisIntegrationEnabled = this.ADVANCED_IRIS_INTEGRATION_ENABLED.get();
        firstPersonAnimationsEnabled = this.FIRST_PERSON_ANIMATIONS_ENABLED.get();
        thirdPersonAnimationsEnabled = this.THIRD_PERSON_ANIMATIONS_ENABLED.get();
        extensionPackBrowseUrl = this.EXTENSION_PACK_BROWSE_URL.get();
    }

    private static ConfigManager getConfigManager() {
        return ConfigManager.getInstance("pointblank-common");
    }

    public static void setAutoReload(AutoReload value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("autoReloadEnabled", (Object)value);
        }
    }

    public static void setScopeAimingMouseSensitivity(double value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("scopeAimingMouseSensitivity", value);
        }
    }

    public static void setResetAutoFirePitchEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("resetAutoFirePitchEnabled", value);
            return;
        }
        if (!Platform.getInstance().setCommonConfigValue("resetAutoFirePitchEnabled", value)) {
            LOGGER.warn("Failed to persist resetAutoFirePitchEnabled via platform config backend; applying in-memory value only");
            resetAutoFirePitchEnabled = value;
        }
    }

    public static void setKnockback(double value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("knockback", value);
        }
    }

    public static void setParticleEffectsEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("particleEffectsEnabled", value);
        }
    }

    public static void setArmsDealerHouseWeight(int value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("armsDealerHouse", value);
        }
    }

    public static void setEmeraldExchangeRate(double value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("emeraldExchangeRate", value);
        }
    }

    public static void setThirdPersonArmPoseAlwaysOn(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("thirdPersonArmPoseAlwaysOn", value);
        }
    }

    public static void setPipScopeRefreshFrame(int value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("pipScopeRefreshRate", value);
        }
    }

    public static void setPipScopesEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("pipScopesEnabled", value);
            return;
        }
        if (!Platform.getInstance().setCommonConfigValue("pipScopesEnabled", value)) {
            LOGGER.warn("Failed to persist pipScopesEnabled via platform config backend; applying in-memory value only");
            pipScopesEnabled = value;
        }
    }

    public static void setPipFallbackModIds(List<String> modIds) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("pipFallbackModIds", modIds);
        }
    }

    public static void setPipScopeColorBalanceRed(int value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("pipScopeColorBalanceRed", value);
        }
    }

    public static void setPipScopeColorBalanceGreen(int value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("pipScopeColorBalanceGreen", value);
        }
    }

    public static void setPipScopeColorBalanceBlue(int value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("pipScopeColorBalanceBlue", value);
        }
    }

    public static void setCustomShadersEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("customShadersEnabled", value);
        }
    }

    public static void setExplosionDestroyBlocksEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("explosionDestroyBlocksEnabled", value);
            return;
        }
        if (!Platform.getInstance().setCommonConfigValue("explosionDestroyBlocksEnabled", value)) {
            LOGGER.warn("Failed to persist explosionDestroyBlocksEnabled via platform config backend; applying in-memory value only");
            explosionDestroyBlocksEnabled = value;
        }
    }

    public static void setItemDropChance(double value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("itemDropChance", value);
        }
    }

    public static void setMaxItemDropCount(int value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("maxItemDropCount", value);
        }
    }

    public static void setBulletsBreakGlassEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("bulletsBreakGlassEnabled", value);
        }
    }

    public static void setHitscanDamageModifier(double value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("hitscanDamageModifier", value);
        }
    }

    public static void setCrosshairType(CrosshairType value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("crosshair", (Object)value);
            return;
        }
        if (!Platform.getInstance().setCommonConfigValue("crosshair", (Object)value)) {
            LOGGER.warn("Failed to persist crosshair via platform config backend; applying in-memory value only");
            crosshairType = value;
        }
    }

    public static void setHeadshotDamageModifier(double value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("headshotDamageModifier", value);
        }
    }

    public static void setGoreEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("goreEnabled", value);
            return;
        }
        if (!Platform.getInstance().setCommonConfigValue("goreEnabled", value)) {
            LOGGER.warn("Failed to persist goreEnabled via platform config backend; applying in-memory value only");
            goreEnabled = value;
        }
    }

    public static void setGoreSoundsEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("goreSoundsEnabled", value);
            return;
        }
        if (!Platform.getInstance().setCommonConfigValue("goreSoundsEnabled", value)) {
            LOGGER.warn("Failed to persist goreSoundsEnabled via platform config backend; applying in-memory value only");
            goreSoundsEnabled = value;
        }
    }

    public static void setShellEjectionEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("shellEjectionEnabled", value);
            return;
        }
        if (!Platform.getInstance().setCommonConfigValue("shellEjectionEnabled", value)) {
            LOGGER.warn("Failed to persist shellEjectionEnabled via platform config backend; applying in-memory value only");
            shellEjectionEnabled = value;
        }
    }

    public static void setScopeViewType(ScopeViewType value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("scopeViewType", (Object)value);
            return;
        }
        if (!Platform.getInstance().setCommonConfigValue("scopeViewType", (Object)value)) {
            LOGGER.warn("Failed to persist scopeViewType via platform config backend; applying in-memory value only");
            scopeViewType = value;
        }
    }

    public static void setAmmoIndicatorEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("ammoIndicatorEnabled", value);
            return;
        }
        if (!Platform.getInstance().setCommonConfigValue("ammoIndicatorEnabled", value)) {
            LOGGER.warn("Failed to persist ammoIndicatorEnabled via platform config backend; applying in-memory value only");
            ammoIndicatorEnabled = value;
        }
    }

    public static void setAmmoIndicatorPlacement(AmmoIndicatorPlacement value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("ammoIndicatorPlacement", (Object)value);
            return;
        }
        if (!Platform.getInstance().setCommonConfigValue("ammoIndicatorPlacement", (Object)value)) {
            LOGGER.warn("Failed to persist ammoIndicatorPlacement via platform config backend; applying in-memory value only");
            ammoIndicatorPlacement = value;
        }
    }

    public static void setHeatIndicatorEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("heatIndicatorEnabled", value);
            return;
        }
        if (!Platform.getInstance().setCommonConfigValue("heatIndicatorEnabled", value)) {
            LOGGER.warn("Failed to persist heatIndicatorEnabled via platform config backend; applying in-memory value only");
            heatIndicatorEnabled = value;
        }
    }

    public static void setHeatIndicatorPlacement(HeatIndicatorPlacement value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("heatIndicatorPlacement", (Object)value);
            return;
        }
        if (!Platform.getInstance().setCommonConfigValue("heatIndicatorPlacement", (Object)value)) {
            LOGGER.warn("Failed to persist heatIndicatorPlacement via platform config backend; applying in-memory value only");
            heatIndicatorPlacement = value;
        }
    }

    public static void setInteractionPromptEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("interactionPromptEnabled", value);
            return;
        }
        if (!Platform.getInstance().setCommonConfigValue("interactionPromptEnabled", value)) {
            LOGGER.warn("Failed to persist interactionPromptEnabled via platform config backend; applying in-memory value only");
            interactionPromptEnabled = value;
        }
    }

    public static void setAdvancedIrisIntegrationEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("advancedIrisIntegrationEnabled", value);
        }
    }

    public static void setFirstPersonAnimationsEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("firstPersonAnimationsEnabled", value);
        }
    }

    public static void setThirdPersonAnimationsEnabled(boolean value) {
        ConfigManager manager = Config.getConfigManager();
        if (manager != null) {
            manager.setValue("thirdPersonAnimationsEnabled", value);
        }
    }

    protected boolean checkPipFallbackModsLoaded() {
        return false;
    }

    public static boolean isPipFallbackRequired() {
        return !pipScopesEnabled || pipFallbackModDetected;
    }

    public static enum AutoReload {
        CREATIVE,
        SURVIVAL,
        ENABLED,
        DISABLED;

    }

    public static enum CrosshairType {
        DEFAULT,
        VANILLA,
        DISABLED;

    }

    public static enum ScopeViewType {
        SQUARE,
        ROUND;

    }

    public static enum AmmoIndicatorPlacement {
        RIGHT_OF_HOTBAR,
        LEFT_OF_HOTBAR,
        TOP_LEFT,
        TOP_RIGHT;

    }

    public static enum HeatIndicatorPlacement {
        BOTTOM_RIGHT,
        BOTTOM_LEFT,
        TOP_LEFT,
        TOP_RIGHT;

    }
}

