package dev.tree.hostileguns;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PointBlankGunPool {
    private static volatile List<Item> cached = List.of();
    private static volatile long lastScanNanos = 0L;

    private PointBlankGunPool() {}

    public static List<Item> guns() {
        List<Item> current = cached;
        long now = System.nanoTime();
        if (!current.isEmpty() || now - lastScanNanos < 5_000_000_000L) return current;

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

    public static Item chooseWeighted(RandomSource random) {
        List<Item> available = guns();
        if (available.isEmpty()) return null;

        double totalWeight = 0.0D;
        double[] weights = new double[available.size()];
        for (int i = 0; i < available.size(); i++) {
            double variation = 0.85D + random.nextDouble() * 0.30D;
            weights[i] = Math.max(0.0D, WeaponBalance.rarityWeight(available.get(i))) * variation;
            totalWeight += weights[i];
        }

        if (totalWeight <= 0.0D) return available.get(random.nextInt(available.size()));

        double selected = random.nextDouble() * totalWeight;
        for (int i = 0; i < available.size(); i++) {
            selected -= weights[i];
            if (selected <= 0.0D) return available.get(i);
        }
        return available.get(available.size() - 1);
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
