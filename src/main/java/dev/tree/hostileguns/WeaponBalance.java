package dev.tree.hostileguns;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Locale;

public final class WeaponBalance {
    public enum Category {
        PISTOL, SMG, RIFLE, SHOTGUN, SNIPER, LMG, HEAVY, UNKNOWN
    }

    public enum Rarity {
        COMMON, UNCOMMON, RARE, EXOTIC
    }

    public record Profile(Category category, Rarity rarity, double spreadDegrees,
                          float damageMultiplier, int minCooldown, int maxCooldown,
                          double projectileSpeed, int pellets) {}

    private WeaponBalance() {}

    public static Profile profile(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String path = key == null ? "" : key.getPath().toLowerCase(Locale.ROOT);
        Category category = category(path);
        Rarity rarity = rarity(path, category);

        return switch (category) {
            case PISTOL -> new Profile(category, rarity, 5.0D, 0.78F, 12, 20, 3.0D, 1);
            case SMG -> new Profile(category, rarity, 5.5D, 0.72F, 5, 9, 3.2D, 1);
            case RIFLE -> new Profile(category, rarity, 3.4D, 1.0F, 7, 13, 4.0D, 1);
            case SHOTGUN -> new Profile(category, rarity, 8.0D, 1.25F, 18, 30, 2.6D, 4);
            case SNIPER -> new Profile(category, rarity, 2.6D, 1.65F, 30, 50, 5.5D, 1);
            case LMG -> new Profile(category, rarity, 4.5D, 0.9F, 4, 8, 3.8D, 1);
            case HEAVY -> new Profile(category, rarity, 6.0D, 1.8F, 26, 44, 2.5D, 1);
            case UNKNOWN -> new Profile(category, rarity, 4.8D, 0.9F,
                    HostileGunsConfig.minShotCooldownTicks,
                    HostileGunsConfig.maxShotCooldownTicks, 3.3D, 1);
        };
    }

    public static double rarityWeight(Item item) {
        return switch (profile(item).rarity()) {
            case COMMON -> HostileGunsConfig.rarityWeightCommon;
            case UNCOMMON -> HostileGunsConfig.rarityWeightUncommon;
            case RARE -> HostileGunsConfig.rarityWeightRare;
            case EXOTIC -> HostileGunsConfig.rarityWeightExotic;
        };
    }

    private static Category category(String path) {
        if (contains(path, "launcher", "rocket", "grenade", "railgun", "gauss", "minigun",
                "malorian", "kanone", "anti_materiel", "antimateriel", "m202", "rpg")) {
            return Category.HEAVY;
        }
        if (contains(path, "sniper", "marksman", "lynx", "l96", "wa2000", "ballista",
                "ashura", "nekomata", "grad", "prospecta", "m82", "m107")) {
            return Category.SNIPER;
        }
        if (contains(path, "lmg", "hbar", "m249", "machinegun", "machine_gun", "beltfed")) {
            return Category.LMG;
        }
        if (contains(path, "shotgun", "spas", "m870", "m590", "aa12", "carnage",
                "crusher", "satara", "tactician", "db2")) {
            return Category.SHOTGUN;
        }
        if (contains(path, "smg", "mp5", "mp7", "p90", "vector", "uzi", "mac10",
                "shingen", "fenrir", "buzzsaw", "yinglong", "saratoga", "ppsh")) {
            return Category.SMG;
        }
        if (contains(path, "pistol", "handgun", "glock", "m1911", "m9", "mk23",
                "overture", "nue", "unity", "liberty", "kappa", "tamayura",
                "lexington", "revolver", "sidearm")) {
            return Category.PISTOL;
        }
        if (contains(path, "rifle", "assault", "carbine", "battle", "m4", "ak",
                "masamune", "ajax", "copperhead", "kyubi", "moron", "ar57")) {
            return Category.RIFLE;
        }
        return Category.UNKNOWN;
    }

    private static Rarity rarity(String path, Category category) {
        if (category == Category.HEAVY || contains(path, "exotic", "prototype", "unique", "iconic")) {
            return Rarity.EXOTIC;
        }
        if (category == Category.SNIPER || category == Category.LMG
                || contains(path, "anti_materiel", "antimateriel", "precision")) {
            return Rarity.RARE;
        }
        if (category == Category.RIFLE || category == Category.SHOTGUN) {
            return Rarity.UNCOMMON;
        }
        return Rarity.COMMON;
    }

    private static boolean contains(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }
}
