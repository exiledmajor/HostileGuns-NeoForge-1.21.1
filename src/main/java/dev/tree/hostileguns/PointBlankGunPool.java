package dev.tree.hostileguns;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PointBlankGunPool {
    private static volatile List<Item> cached = List.of();
    private static volatile long lastScanNanos = 0L;

    private PointBlankGunPool() {}

    public static List<Item> guns() {
        long now = System.nanoTime();
        if (!cached.isEmpty() || now - lastScanNanos < 5_000_000_000L) return cached;
        synchronized (PointBlankGunPool.class) {
            if (!cached.isEmpty() || now - lastScanNanos < 5_000_000_000L) return cached;
            lastScanNanos = now;
            cached = scan();
            return cached;
        }
    }

    public static void refresh() {
        synchronized (PointBlankGunPool.class) {
            lastScanNanos = System.nanoTime();
            cached = scan();
        }
    }

    private static List<Item> scan() {
        try {
            Class<?> gunClass = Class.forName(
                    "com.vicmatskiv.pointblank.item.GunItem",
                    false,
                    PointBlankGunPool.class.getClassLoader()
            );
            List<Item> guns = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                if (gunClass.isInstance(item)) guns.add(item);
            }
            return Collections.unmodifiableList(guns);
        } catch (Throwable ignored) {
            return List.of();
        }
    }
}
