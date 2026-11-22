package dev.tree.mainframeprogression;

import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Optional Chromeware integration owned by Mainframe.
 *
 * Mainframe only grants the Cyberware Versatility capacity bonus. Humanity,
 * implant strain, wear, malfunctions and their recovery are
 * exclusively managed by Chromeware.
 */
public final class ChromewareBridge {
    private static boolean unavailable;

    private ChromewareBridge() {}

    public static void tick(Player player, int versatilityPoints) {
        if (unavailable || player.level().isClientSide() || player.tickCount % 20 != 0) return;
        syncVersatility(player, versatilityPoints);
    }

    public static void syncVersatility(Player player, int points) {
        if (unavailable || player.level().isClientSide()) return;
        try {
            Class<?> configClass = Class.forName("com.chromeware.config.ChromewareConfig");
            int base = ((Number) configClass.getMethod("startingCapacity").invoke(null)).intValue();

            Class<?> dataClass = Class.forName("com.chromeware.cyberware.PlayerData");
            Supplier<?> limitSupplier = (Supplier<?>) dataClass.getField("CAPACITY_LIMIT").get(null);
            Supplier<?> customSupplier = (Supplier<?>) dataClass.getField("CAPACITY_CUSTOM_LIMIT").get(null);

            Method getData = Player.class.getMethod("getData", Supplier.class);
            Method setData = Player.class.getMethod("setData", Supplier.class, Object.class);

            int desired = Math.max(0, Math.min(10000, base + Math.max(0, points)));
            int current = ((Number) getData.invoke(player, limitSupplier)).intValue();
            boolean custom = (Boolean) getData.invoke(player, customSupplier);
            if (current != desired) setData.invoke(player, limitSupplier, desired);
            if (!custom) setData.invoke(player, customSupplier, true);
        } catch (ClassNotFoundException e) {
            unavailable = true;
        } catch (Throwable ignored) {
            // Chromeware is optional; compatibility failures must not break player ticks.
        }
    }
}
