/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.fml.ModList
 *  net.neoforged.neoforge.common.ModConfigSpec
 *  net.neoforged.neoforge.common.ModConfigSpec$Builder
 *  net.neoforged.neoforge.common.ModConfigSpec$ConfigValue
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.vicmatskiv.pointblank.platform.neoforge;

import com.vicmatskiv.pointblank.Config;
import com.vicmatskiv.pointblank.event.ModConfigEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NeoforgeConfig
extends Config {
    private static final Logger LOGGER = LogManager.getLogger(NeoforgeConfig.class);
    private final Map<String, ModConfigSpec.ConfigValue<?>> writableValues = new HashMap();
    protected final ModConfigSpec SPEC;

    public NeoforgeConfig() {
        ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
        this.AUTO_RELOAD = this.registerWritable("autoReloadEnabled", BUILDER.comment("Enables auto-reloading of guns").defineEnum("autoReloadEnabled", (Enum)Config.AutoReload.CREATIVE));
        this.SCOPE_AIMING_MOUSE_SENSITIVITY = this.registerWritable("scopeAimingMouseSensitivity", BUILDER.comment("Adjusts mouse sensitivity when using scopes").defineInRange("scopeAimingMouseSensitivity", 0.4, 0.01, 0.9));
        this.RESET_AUTO_FIRE_PITCH_ENABLED = this.registerWritable("resetAutoFirePitchEnabled", BUILDER.comment("Toggle to reset player pitch to original after auto fire").define("resetAutoFirePitchEnabled", true));
        this.KNOCKBACK = this.registerWritable("knockback", BUILDER.comment("Adjusts the knockback force applied to entities hit by gunfire, with higher values causing greater knockback distance.").defineInRange("knockback", 1.0, 0.1, 2.0));
        this.PARTICLE_EFFECTS_ENABLED = this.registerWritable("particleEffectsEnabled", BUILDER.comment("Enables particle effects").define("particleEffectsEnabled", true));
        this.ARMS_DEALER_HOUSE_WEIGHT = this.registerWritable("armsDealerHouse", BUILDER.comment("Sets the likelihood of an 'Arms Dealer' house appearing in new villages, with higher values increasing frequency and lower values making it rarer.").defineInRange("armsDealerHouse", 10, 0, 20));
        this.EMERALD_EXCHANGE_RATE = this.registerWritable("emeraldExchangeRate", BUILDER.comment("Set the exchange rate to determine how many in-game price units are equivalent to one emerald.").defineInRange("emeraldExchangeRate", 100.0, 1.0, 1000.0));
        this.THIRD_PERSON_ARM_POSE_ALWAYS_ON = this.registerWritable("thirdPersonArmPoseAlwaysOn", BUILDER.comment("Controls whether the player's arm pose is permanently set to the aiming/firing position in third-person view, regardless of their current action with a gun.").define("thirdPersonArmPoseAlwaysOn", true));
        this.PIP_SCOPE_REFRESH_FRAME = this.registerWritable("pipScopeRefreshRate", BUILDER.comment("Specifies how often the \"picture-in-picture\" scope updates, with 1 being every frame, 2 for every other frame, etc. A higher number may improve performance.").defineInRange("pipScopeRefreshRate", 2, 0, 5));
        this.PIP_SCOPES_ENABLED = this.registerWritable("pipScopesEnabled", BUILDER.comment("Enables pip scopes").define("pipScopesEnabled", true));
        this.PIP_SCOPE_COLOR_BALANCE_RED = this.registerWritable("pipScopeColorBalanceRed", BUILDER.comment("Sets pip scope red color balance. This options works for shaders only.").defineInRange("pipScopeColorBalanceRed", 90, 0, 255));
        this.PIP_SCOPE_COLOR_BALANCE_GREEN = this.registerWritable("pipScopeColorBalanceGreen", BUILDER.comment("Sets pip scope green color balance. This options works for shaders only.").defineInRange("pipScopeColorBalanceGreen", 105, 0, 255));
        this.PIP_SCOPE_COLOR_BALANCE_BLUE = this.registerWritable("pipScopeColorBalanceBlue", BUILDER.comment("Sets pip scope blue color balance. This options works for shaders only.").defineInRange("pipScopeColorBalanceBlue", 110, 0, 255));
        this.CUSTOM_SHADERS_ENABLED = this.registerWritable("customShadersEnabled", BUILDER.comment("Enables custom shaders").define("customShadersEnabled", true));
        this.EXPLOSION_DESTROY_BLOCKS_ENABLED = this.registerWritable("explosionDestroyBlocksEnabled", BUILDER.comment("Enables explosions to destroy blocks").define("explosionDestroyBlocksEnabled", true));
        this.ITEM_DROP_CHANCE = this.registerWritable("itemDropChance", BUILDER.comment("Sets the probability of dropping some item(s) by monster mobs.").defineInRange("itemDropChance", 0.35, 0.0, 1.0));
        this.MAX_ITEM_DROP_COUNT = this.registerWritable("maxItemDropCount", BUILDER.comment("Sets the max count of items to be dropped by a monster mob.").defineInRange("maxItemDropCount", 7, 2, 64));
        this.BULLETS_BREAK_GLASS_ENABLED = this.registerWritable("bulletsBreakGlassEnabled", BUILDER.comment("Enables bullets breaking glass").define("bulletsBreakGlassEnabled", true));
        this.HITSCAN_DAMAGE_MODIFIER = this.registerWritable("hitscanDamageModifier", BUILDER.comment("Modifier to adjust damage dealt by hitscan weapons").defineInRange("hitscanDamageModifier", 1.0, 0.1, 10.0));
        this.CROSSHAIR_TYPE = this.registerWritable("crosshair", BUILDER.comment("Sets the crosshair type.").defineEnum("crosshair", (Enum)Config.CrosshairType.DEFAULT));
        this.HEADSHOT_DAMAGE_MODIFIER = this.registerWritable("headshotDamageModifier", BUILDER.comment("Modifier to adjust damage dealt by headshot.").defineInRange("headshotDamageModifier", 3.0, 1.0, 10.0));
        this.GORE_ENABLED = this.registerWritable("goreEnabled", BUILDER.comment("Enables gore effects").define("goreEnabled", true));
        this.GORE_SOUNDS_ENABLED = this.registerWritable("goreSoundsEnabled", BUILDER.comment("Enables gore hit sounds").define("goreSoundsEnabled", true));
        this.SHELL_EJECTION_ENABLED = this.registerWritable("shellEjectionEnabled", BUILDER.comment("Enables first-person shell ejection effects").define("shellEjectionEnabled", true));
        this.ADVANCED_IRIS_INTEGRATION_ENABLED = this.registerWritable("advancedIrisIntegrationEnabled", BUILDER.comment("Enables advanced integraiton with Iris shaders").define("advancedIrisIntegrationEnabled", true));
        this.FIRST_PERSON_ANIMATIONS_ENABLED = this.registerWritable("firstPersonAnimationsEnabled", BUILDER.comment("Enables advanced first person animations").define("firstPersonAnimationsEnabled", true));
        this.THIRD_PERSON_ANIMATIONS_ENABLED = this.registerWritable("thirdPersonAnimationsEnabled", BUILDER.comment("Enables advanced third person animations").define("thirdPersonAnimationsEnabled", true));
        this.SCOPE_VIEW_TYPE = this.registerWritable("scopeViewType", BUILDER.comment("Sets the scope view shape. SQUARE uses a square mask, ROUND uses a round mask. Only applies to scopes without a custom mask.").defineEnum("scopeViewType", (Enum)Config.ScopeViewType.SQUARE));
        this.AMMO_INDICATOR_ENABLED = this.registerWritable("ammoIndicatorEnabled", BUILDER.comment("Enables the on-screen ammo counter and fire mode indicator.").define("ammoIndicatorEnabled", true));
        this.AMMO_INDICATOR_PLACEMENT = this.registerWritable("ammoIndicatorPlacement", BUILDER.comment("Sets where the ammo counter and fire mode indicator are displayed on screen.").defineEnum("ammoIndicatorPlacement", (Enum)Config.AmmoIndicatorPlacement.RIGHT_OF_HOTBAR));
        this.HEAT_INDICATOR_ENABLED = this.registerWritable("heatIndicatorEnabled", BUILDER.comment("Enables the on-screen heat indicator for guns with overheating.").define("heatIndicatorEnabled", true));
        this.HEAT_INDICATOR_PLACEMENT = this.registerWritable("heatIndicatorPlacement", BUILDER.comment("Sets which screen corner displays the heat indicator.").defineEnum("heatIndicatorPlacement", (Enum)Config.HeatIndicatorPlacement.BOTTOM_RIGHT));
        this.INTERACTION_PROMPT_ENABLED = this.registerWritable("interactionPromptEnabled", BUILDER.comment("Enables the on-screen prompt for opening interactable blocks while holding a gun.").define("interactionPromptEnabled", true));
        this.EXTENSION_PACK_BROWSE_URL = this.registerWritable("extensionPackBrowseUrl", BUILDER.comment("URL to open when clicking the Browse button in the Extension Packs screen").define("extensionPackBrowseUrl", (Object)"https://www.curseforge.com/minecraft/search?page=1&pageSize=20&sortBy=relevancy&class=customization&search=point+blank"));
        this.SPEC = BUILDER.build();
    }

    private <T, V extends ModConfigSpec.ConfigValue<T>> V registerWritable(String name, V value) {
        this.writableValues.put(name, value);
        return value;
    }

    public synchronized boolean setValue(String optionName, Object value) {
        ModConfigSpec.ConfigValue<?> configValue = this.writableValues.get(optionName);
        if (configValue == null) {
            LOGGER.warn("Unknown NeoForge config option: {}", (Object)optionName);
            return false;
        }
        if (!this.SPEC.isLoaded()) {
            LOGGER.warn("NeoForge config not loaded yet, cannot persist option: {}", (Object)optionName);
            return false;
        }
        try {
            Object correctedValue;
            Object object = correctedValue = configValue.getSpec().test(value) ? value : configValue.getSpec().correct(value);
            if (Objects.equals(configValue.get(), correctedValue)) {
                return true;
            }
            configValue.set(correctedValue);
            this.SPEC.save();
            this.onLoad(new ModConfigEvent());
            return true;
        }
        catch (Exception e) {
            LOGGER.error("Failed to persist NeoForge config option {}", (Object)optionName, (Object)e);
            return false;
        }
    }

    @Override
    protected boolean checkPipFallbackModsLoaded() {
        List<String> fallbackModIds = List.of("distanthorizons");
        for (String modId : fallbackModIds) {
            if (!ModList.get().isLoaded(modId)) continue;
            return true;
        }
        return false;
    }
}

