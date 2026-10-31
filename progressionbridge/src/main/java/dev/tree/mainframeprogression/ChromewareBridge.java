package dev.tree.mainframeprogression;

import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public final class ChromewareBridge {
    private static boolean unavailable;

    private ChromewareBridge() {}

    public static void syncVersatility(Player player, int versatilityPoints) {
        if (unavailable) return;

        try {
            Class<?> configClass = Class.forName("com.chromeware.config.ChromewareConfig");
            Method startingCapacity = configClass.getMethod("startingCapacity");
            int base = ((Number) startingCapacity.invoke(null)).intValue();

            Class<?> dataClass = Class.forName("com.chromeware.cyberware.PlayerData");
            Field limitField = dataClass.getField("CAPACITY_LIMIT");
            Field customField = dataClass.getField("CAPACITY_CUSTOM_LIMIT");

            Object limitSupplier = limitField.get(null);
            Object customSupplier = customField.get(null);

            Method getData = Player.class.getMethod("getData", Supplier.class);
            Method setData = Player.class.getMethod("setData", Supplier.class, Object.class);

            // Versatility is intentionally less capacity-efficient than the old bridge:
            // one RAS point buys one capacity instead of two. The same investment also
            // feeds the cooldown bridge below, so it remains worthwhile rather than nerfed flat.
            int desired = Math.max(0, Math.min(10000, base + versatilityPoints));
            int current = ((Number) getData.invoke(player, limitSupplier)).intValue();
            boolean custom = (Boolean) getData.invoke(player, customSupplier);

            if (current != desired) setData.invoke(player, limitSupplier, Integer.valueOf(desired));
            if (!custom) setData.invoke(player, customSupplier, Boolean.TRUE);

            syncCooldownHint(player, versatilityPoints);
        } catch (ClassNotFoundException ignored) {
            unavailable = true;
        } catch (Throwable ignored) {
            // Leave Chromeware's own capacity untouched if its internal API changes.
        }
    }

    private static void syncCooldownHint(Player player, int versatilityPoints) {
        // Expose a conservative, capped recovery factor for cyberware integrations.
        // 0.35% per point, capped at 15%, keeps cooldowns meaningful at high investment.
        double reduction = Math.min(0.15D, Math.max(0, versatilityPoints) * 0.0035D);
        player.getPersistentData().putDouble("mainframe_cyberware_cooldown_reduction", reduction);
    }
}
