package dev.tree.mainframeprogression;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public final class ChromewareBridge {
    private static boolean unavailable;
    private static final Map<UUID, Map<String, Integer>> LAST_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> LAST_CLOCKS = new HashMap<>();

    private static final String DEATH_TRAUMA = "mainframe_death_trauma";
    private static final String WEAR_PREFIX = "mainframe_cyberware_wear_";

    private ChromewareBridge() {}

    public static void tick(Player player, int versatilityPoints) {
        if (unavailable || player.level().isClientSide()) return;
        try {
            if (player.tickCount % 20 == 0) syncVersatility(player, versatilityPoints);
            applyCooldownScaling(player, versatilityPoints);
            applyDurationScaling(player, versatilityPoints);
            if (player.tickCount % 200 == 0) applyCyberwareWear(player);
            if (player.tickCount % 20 == 0) recoverDeathTrauma(player);
            if (player.tickCount % 100 == 0) cyberwareMalfunction(player);
        } catch (ClassNotFoundException ignored) {
            // A missing optional feature class must not disable unrelated bridge features.
            // syncVersatility separately detects whether Chromeware itself is installed.
        } catch (Throwable ignored) {
            // Chromeware is optional and its internals may change. Never break the player tick.
        }
    }

    public static void syncVersatility(Player player, int points) {
        if (unavailable) return;
        try {
            Class<?> configClass = Class.forName("com.chromeware.config.ChromewareConfig");
            int base = ((Number)configClass.getMethod("startingCapacity").invoke(null)).intValue();

            Class<?> dataClass = Class.forName("com.chromeware.cyberware.PlayerData");
            Supplier<?> limitSupplier = (Supplier<?>)dataClass.getField("CAPACITY_LIMIT").get(null);
            Supplier<?> customSupplier = (Supplier<?>)dataClass.getField("CAPACITY_CUSTOM_LIMIT").get(null);

            Method getData = Player.class.getMethod("getData", Supplier.class);
            Method setData = Player.class.getMethod("setData", Supplier.class, Object.class);

            int desired = Math.max(0, Math.min(10000, base + Math.max(0, points)));
            int current = ((Number)getData.invoke(player, limitSupplier)).intValue();
            boolean custom = (Boolean)getData.invoke(player, customSupplier);
            if (current != desired) setData.invoke(player, limitSupplier, desired);
            if (!custom) setData.invoke(player, customSupplier, true);

            player.getPersistentData().putDouble("mainframe_cyberware_cooldown_reduction", cooldownReduction(points));
            player.getPersistentData().putDouble("mainframe_cyberware_duration_bonus", durationBonus(points));
        } catch (ClassNotFoundException e) {
            unavailable = true;
        } catch (Throwable ignored) {}
    }

    private static double cooldownReduction(int points) {
        return Math.min(0.15D, Math.max(0, points) * 0.0035D);
    }

    private static double durationBonus(int points) {
        return Math.min(0.075D, Math.max(0, points) * 0.0015D);
    }

    private static void applyCooldownScaling(Player player, int points) throws Exception {
        double reduction = cooldownReduction(points);
        if (reduction <= 0.0D) return;

        Class<?> cooldowns = Class.forName("com.chromeware.cyberware.ImplantCooldowns");
        Method forEach = cooldowns.getMethod("forEachRemaining", Player.class, java.util.function.BiConsumer.class);
        Map<String, Integer> previous = LAST_COOLDOWNS.computeIfAbsent(player.getUUID(), id -> new HashMap<>());
        Map<String, Integer> current = new HashMap<>();

        final boolean[] newlyStarted = {false};
        forEach.invoke(null, player, (java.util.function.BiConsumer<Object, Object>)(id, remaining) -> {
            String key = String.valueOf(id);
            int now = ((Number)remaining).intValue();
            current.put(key, now);
            int before = previous.getOrDefault(key, 0);
            if (now > 0 && (before == 0 || now > before)) newlyStarted[0] = true;
        });

        // reduce() scales every active cooldown, so apply it once even when several
        // implants start together rather than compounding the reduction per implant.
        if (newlyStarted[0]) {
            cooldowns.getMethod("reduce", Player.class, double.class).invoke(null, player, reduction);
        }
        previous.clear();
        previous.putAll(current);
        adjustSandevistanCooldown(player, reduction);
    }

    private static void adjustSandevistanCooldown(Player player, double reduction) {
        try {
            Class<?> data = Class.forName("com.chromeware.cyberware.PlayerData");
            Supplier<?> supplier = (Supplier<?>)data.getField("SANDEVISTAN_STATE").get(null);
            Object state = player.getData((Supplier)supplier);
            int active = ((Number)state.getClass().getMethod("activeTicks").invoke(state)).intValue();
            int cooldown = ((Number)state.getClass().getMethod("cooldown").invoke(state)).intValue();

            CompoundTag tag = player.getPersistentData();
            int previousActive = tag.getInt("mainframe_sandevistan_active");
            tag.putInt("mainframe_sandevistan_active", active);

            if (previousActive <= 0 && active > 0) {
                int extended = (int)Math.ceil(active * durationBonus((int)Math.round(
                        player.getAttributeValue(MainframeProgression.CYBERWARE_VERSATILITY))));
                int reducedCooldown = (int)Math.round(cooldown * (1.0D - reduction));
                Class<?> stateClass = Class.forName("com.chromeware.cyberware.SandevistanState");
                Object next = stateClass.getConstructor(int.class, int.class).newInstance(active + extended, reducedCooldown);
                player.setData((Supplier)supplier, next);
            }
        } catch (Throwable ignored) {}
    }

    private static void applyDurationScaling(Player player, int points) {
        double bonus = durationBonus(points);
        if (bonus <= 0.0D) return;
        try {
            Class<?> effects = Class.forName("com.chromeware.cyberware.ImplantEffects");
            Class<?> data = Class.forName("com.chromeware.cyberware.ImplantData");
            Object state = effects.getMethod("state", Player.class).invoke(null, player);
            Map<?, ?> clocks = (Map<?, ?>)data.getMethod("clocks").invoke(state);

            Map<String, Integer> previous = LAST_CLOCKS.computeIfAbsent(player.getUUID(), id -> new HashMap<>());
            Map<String, Integer> current = new HashMap<>();
            Method clock = effects.getMethod("clock", Player.class, String.class, int.class);

            for (Map.Entry<?, ?> entry : clocks.entrySet()) {
                String key = String.valueOf(entry.getKey());
                int now = ((Number)entry.getValue()).intValue();
                current.put(key, now);
                int before = previous.getOrDefault(key, 0);
                if (now > 0 && before == 0 && !key.endsWith("_cooldown")) {
                    int extra = Math.max(1, (int)Math.round(now * bonus));
                    clock.invoke(null, player, key, now + extra);
                }
            }
            previous.clear();
            previous.putAll(current);
        } catch (Throwable ignored) {}
    }

    public static void onDeath(Player player, net.minecraft.world.damagesource.DamageSource source) {
        if (unavailable || player.level().isClientSide() || !hasCyberware(player)) return;
        double pressure = deathPressure(player, source);
        CompoundTag data = player.getPersistentData();
        double trauma = Math.min(0.10D, data.getDouble(DEATH_TRAUMA) + pressure);
        data.putDouble(DEATH_TRAUMA, trauma);
        addPermanentHumanity(player, pressure);
        damageCyberwareOnDeath(player);
    }

    private static double deathPressure(Player victim, net.minecraft.world.damagesource.DamageSource source) {
        net.minecraft.world.entity.Entity attacker = source.getEntity();
        net.minecraft.world.entity.Entity direct = source.getDirectEntity();
        Player killer = resolvePlayerAttacker(attacker, direct);

        if (killer != null) return hasCyberware(killer) ? 0.08D : 0.06D;
        if (attacker instanceof net.minecraft.world.entity.LivingEntity || direct instanceof net.minecraft.world.entity.LivingEntity) return 0.03D;
        return 0.02D;
    }

    private static Player resolvePlayerAttacker(net.minecraft.world.entity.Entity attacker,
                                                net.minecraft.world.entity.Entity direct) {
        if (attacker instanceof Player player) return player;
        if (direct instanceof Player player) return player;
        if (direct instanceof net.minecraft.world.entity.projectile.Projectile projectile
                && projectile.getOwner() instanceof Player player) return player;
        if (attacker instanceof net.minecraft.world.entity.projectile.Projectile projectile
                && projectile.getOwner() instanceof Player player) return player;
        return null;
    }

    private static void addPermanentHumanity(Player player, double amount) {
        try {
            Class<?> humanity = Class.forName("com.chromeware.humanity.Humanity");
            Object state = humanity.getMethod("state", Player.class).invoke(null, player);
            double permanent = ((Number)state.getClass().getMethod("permanent").invoke(state)).doubleValue();
            double temporary = ((Number)state.getClass().getMethod("temporary").invoke(state)).doubleValue();
            Object next = state.getClass().getMethod("with", double.class, double.class).invoke(state, permanent + amount, temporary);

            Class<?> data = Class.forName("com.chromeware.humanity.Humanity");
            Supplier<?> supplier = (Supplier<?>)data.getField("STATE").get(null);
            player.setData((Supplier)supplier, next);
            humanity.getMethod("sync", Player.class, boolean.class).invoke(null, player, true);
        } catch (Throwable ignored) {
            try {
                Class<?> humanity = Class.forName("com.chromeware.humanity.Humanity");
                humanity.getMethod("strain", Player.class, double.class).invoke(null, player, amount);
            } catch (Throwable ignoredAgain) {}
        }
    }

    private static void recoverDeathTrauma(Player player) {
        CompoundTag data = player.getPersistentData();
        double trauma = data.getDouble(DEATH_TRAUMA);
        if (trauma <= 0.0D) return;
        double step = Math.min(0.0001D, trauma);
        try {
            Class<?> humanity = Class.forName("com.chromeware.humanity.Humanity");
            Object state = humanity.getMethod("state", Player.class).invoke(null, player);
            double permanent = ((Number)state.getClass().getMethod("permanent").invoke(state)).doubleValue();
            double temporary = ((Number)state.getClass().getMethod("temporary").invoke(state)).doubleValue();
            Object next = state.getClass().getMethod("with", double.class, double.class).invoke(state, Math.max(0.0D, permanent - step), temporary);
            Supplier<?> supplier = (Supplier<?>)humanity.getField("STATE").get(null);
            player.setData((Supplier)supplier, next);
            humanity.getMethod("sync", Player.class, boolean.class).invoke(null, player, true);
            data.putDouble(DEATH_TRAUMA, trauma - step);
        } catch (Throwable ignored) {}
    }

    private static boolean hasCyberware(Player player) {
        try {
            Class<?> capacity = Class.forName("com.chromeware.cyberware.CyberwareCapacity");
            return ((Number)capacity.getMethod("used", Player.class).invoke(null, player)).intValue() > 0;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void damageCyberwareOnDeath(Player player) {
        for (ItemStack stack : installedCyberware(player)) {
            int capacity = Math.max(1, capacity(stack));
            double damage = Math.min(0.15D, 0.06D * Math.sqrt(20.0D / capacity));
            addWear(player, stack, damage);
        }
    }

    private static void applyCyberwareWear(Player player) {
        if (!hasCyberware(player)) return;
        double loss = humanityLoss(player);
        double strain = 0.001D + Math.max(0.0D, loss - 50.0D) * 0.00002D;
        for (ItemStack stack : installedCyberware(player)) {
            int capacity = Math.max(1, capacity(stack));
            addWear(player, stack, strain * Math.sqrt(20.0D / capacity));
        }
    }

    private static double humanityLoss(Player player) {
        try {
            Class<?> humanity = Class.forName("com.chromeware.humanity.Humanity");
            Object state = humanity.getMethod("state", Player.class).invoke(null, player);
            return ((Number)state.getClass().getMethod("loss").invoke(state)).doubleValue();
        } catch (Throwable ignored) { return 0.0D; }
    }

    private static java.util.List<ItemStack> installedCyberware(Player player) {
        try {
            Class<?> capacity = Class.forName("com.chromeware.cyberware.CyberwareCapacity");
            return (java.util.List<ItemStack>)capacity.getMethod("installed", Player.class).invoke(null, player);
        } catch (Throwable ignored) {
            return java.util.List.of();
        }
    }

    private static int capacity(ItemStack stack) {
        try {
            Class<?> stats = Class.forName("com.chromeware.cyberware.CyberwareStats");
            Object result = stats.getMethod("get", ItemStack.class).invoke(null, stack);
            return ((Number)result.getClass().getMethod("capacity").invoke(result)).intValue();
        } catch (Throwable ignored) { return 1; }
    }

    private static void addWear(Player player, ItemStack stack, double amount) {
        if (stack.isEmpty() || amount <= 0.0D) return;
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        double current = custom.getDouble("mainframe_cyberware_wear");
        if (!custom.contains("mainframe_cyberware_wear_id")) {
            // Preserve wear from older versions while moving to per-implant storage.
            current = Math.max(current, player.getPersistentData().getDouble(legacyWearKey(stack)));
            custom.putString("mainframe_cyberware_wear_id", UUID.randomUUID().toString());
        }
        custom.putDouble("mainframe_cyberware_wear", Math.min(1.0D, current + amount));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
    }

    private static String legacyWearKey(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return WEAR_PREFIX + (id == null ? "unknown" : id.toString().replace(':', '_'));
    }

    private static void cyberwareMalfunction(Player player) {
        if (!hasCyberware(player)) return;
        double worst = 0.0D;
        for (ItemStack stack : installedCyberware(player)) worst = Math.max(worst, wear(player, stack));
        double loss = humanityLoss(player);
        if (worst < 0.80D || loss < 70.0D) return;
        double chance = Math.min(0.35D, (worst - 0.75D) * 0.35D + Math.max(0.0D, loss - 70.0D) * 0.004D);
        if (player.getRandom().nextDouble() >= chance) return;
        int ticks = player.getRandom().nextIntBetweenInclusive(30, 70);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 0, false, true));
        if (worst >= 0.90D) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 0, false, true));
            if (player.getRandom().nextBoolean()) player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, Math.min(80, ticks), 0, false, true));
        }
    }

    public static double wear(Player player, ItemStack stack) {
        if (stack.isEmpty()) return 0.0D;
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (custom.contains("mainframe_cyberware_wear_id")) {
            return Math.max(0.0D, Math.min(1.0D, custom.getDouble("mainframe_cyberware_wear")));
        }
        return Math.max(0.0D, Math.min(1.0D, player.getPersistentData().getDouble(legacyWearKey(stack))));
    }

    /** Repairs an installed implant's accumulated wear. Amount is normalized from 0.0 to 1.0. */
    public static void repairWear(Player player, ItemStack stack, double amount) {
        if (stack.isEmpty() || !Double.isFinite(amount) || amount <= 0.0D) return;
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        double current = wear(player, stack);
        if (!custom.contains("mainframe_cyberware_wear_id")) {
            custom.putString("mainframe_cyberware_wear_id", UUID.randomUUID().toString());
        }
        custom.putDouble("mainframe_cyberware_wear", Math.max(0.0D, current - Math.min(1.0D, amount)));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
    }

    public static boolean isImplantDamaged(Player player, Object implant) {
        try {
            Field id = implant.getClass().getField("id");
            String implantId = String.valueOf(id.get(implant));
            for (ItemStack stack : installedCyberware(player)) {
                if (BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().equals(implantId)) return wear(player, stack) >= 1.0D;
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
