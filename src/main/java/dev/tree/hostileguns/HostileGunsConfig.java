package dev.tree.hostileguns;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public final class HostileGunsConfig {
    private HostileGunsConfig() {}

    public static double armedHostileChance = 0.12D;
    public static double maxRange = 32.0D;
    public static float baseDamage = 5.0F;
    public static int minShotCooldownTicks = 5;
    public static int maxShotCooldownTicks = 18;
    public static int minAimDelayTicks = 5;
    public static int maxAimDelayTicks = 12;
    public static double accuracyMultiplier = 1.0D;
    public static boolean allowModdedHostiles = true;
    public static boolean muzzleFlashEffects = true;
    public static boolean tracerEffects = true;
    public static boolean impactEffects = true;

    public static double rarityWeightCommon = 9.0D;
    public static double rarityWeightUncommon = 3.0D;
    public static double rarityWeightRare = 0.75D;
    public static double rarityWeightExotic = 0.15D;

    public static final Set<String> excludedEntities = new LinkedHashSet<>(Arrays.asList(
            "minecraft:ender_dragon", "minecraft:wither", "minecraft:warden",
            "minecraft:elder_guardian", "minecraft:guardian", "minecraft:ravager",
            "minecraft:creeper", "minecraft:ghast", "minecraft:phantom",
            "minecraft:slime", "minecraft:magma_cube", "minecraft:shulker",
            "minecraft:spider", "minecraft:cave_spider", "minecraft:silverfish",
            "minecraft:endermite", "minecraft:blaze", "minecraft:breeze",
            "minecraft:hoglin", "minecraft:zoglin", "minecraft:vex"
    ));

    public static void load() {
        Path path = FMLPaths.CONFIGDIR.get().resolve("hostileguns.properties");
        Properties p = new Properties();

        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                p.load(in);
            } catch (IOException ignored) {
            }
        }

        armedHostileChance = clamp(parseDouble(p, "armedHostileChance", armedHostileChance), 0.0D, 1.0D);
        maxRange = clamp(parseDouble(p, "maxRange", maxRange), 8.0D, 96.0D);
        baseDamage = (float) clamp(parseDouble(p, "baseDamage", baseDamage), 0.5D, 30.0D);
        minShotCooldownTicks = (int) clamp(parseDouble(p, "minShotCooldownTicks", minShotCooldownTicks), 2, 40);
        maxShotCooldownTicks = (int) clamp(parseDouble(p, "maxShotCooldownTicks", maxShotCooldownTicks), minShotCooldownTicks, 60);
        minAimDelayTicks = (int) clamp(parseDouble(p, "minAimDelayTicks", minAimDelayTicks), 0, 40);
        maxAimDelayTicks = (int) clamp(parseDouble(p, "maxAimDelayTicks", maxAimDelayTicks), minAimDelayTicks, 60);
        accuracyMultiplier = clamp(parseDouble(p, "accuracyMultiplier", accuracyMultiplier), 0.35D, 2.5D);

        allowModdedHostiles = Boolean.parseBoolean(p.getProperty("allowModdedHostiles", Boolean.toString(allowModdedHostiles)));
        muzzleFlashEffects = Boolean.parseBoolean(p.getProperty("muzzleFlashEffects", Boolean.toString(muzzleFlashEffects)));
        tracerEffects = Boolean.parseBoolean(p.getProperty("tracerEffects", Boolean.toString(tracerEffects)));
        impactEffects = Boolean.parseBoolean(p.getProperty("impactEffects", Boolean.toString(impactEffects)));

        rarityWeightCommon = clamp(parseDouble(p, "rarityWeightCommon", rarityWeightCommon), 0.0D, 100.0D);
        rarityWeightUncommon = clamp(parseDouble(p, "rarityWeightUncommon", rarityWeightUncommon), 0.0D, 100.0D);
        rarityWeightRare = clamp(parseDouble(p, "rarityWeightRare", rarityWeightRare), 0.0D, 100.0D);
        rarityWeightExotic = clamp(parseDouble(p, "rarityWeightExotic", rarityWeightExotic), 0.0D, 100.0D);

        String excluded = p.getProperty("excludedEntities");
        if (excluded != null) {
            excludedEntities.clear();
            for (String id : excluded.split(",")) {
                String trimmed = id.trim();
                if (!trimmed.isEmpty()) excludedEntities.add(trimmed);
            }
        }

        p.setProperty("armedHostileChance", Double.toString(armedHostileChance));
        p.setProperty("maxRange", Double.toString(maxRange));
        p.setProperty("baseDamage", Float.toString(baseDamage));
        p.setProperty("minShotCooldownTicks", Integer.toString(minShotCooldownTicks));
        p.setProperty("maxShotCooldownTicks", Integer.toString(maxShotCooldownTicks));
        p.setProperty("minAimDelayTicks", Integer.toString(minAimDelayTicks));
        p.setProperty("maxAimDelayTicks", Integer.toString(maxAimDelayTicks));
        p.setProperty("accuracyMultiplier", Double.toString(accuracyMultiplier));
        p.setProperty("allowModdedHostiles", Boolean.toString(allowModdedHostiles));
        p.setProperty("muzzleFlashEffects", Boolean.toString(muzzleFlashEffects));
        p.setProperty("tracerEffects", Boolean.toString(tracerEffects));
        p.setProperty("impactEffects", Boolean.toString(impactEffects));
        p.setProperty("rarityWeightCommon", Double.toString(rarityWeightCommon));
        p.setProperty("rarityWeightUncommon", Double.toString(rarityWeightUncommon));
        p.setProperty("rarityWeightRare", Double.toString(rarityWeightRare));
        p.setProperty("rarityWeightExotic", Double.toString(rarityWeightExotic));
        p.setProperty("excludedEntities", String.join(",", excludedEntities));

        try {
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                p.store(out,
                        "Hostile Guns for NeoForge 1.21.1\n" +
                        "12% default armed mob chance. Common weapons are weighted much more heavily than rare/exotic weapons.\n" +
                        "Aim delay, spread, firing cadence and visible projectile effects are configurable.");
            }
        } catch (IOException ignored) {
        }
    }

    private static double parseDouble(Properties p, String key, double fallback) {
        try {
            return Double.parseDouble(p.getProperty(key, Double.toString(fallback)));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
