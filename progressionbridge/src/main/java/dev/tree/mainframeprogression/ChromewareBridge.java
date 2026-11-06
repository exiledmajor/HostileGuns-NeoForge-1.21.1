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
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class ChromewareBridge {
    private static final String DEATH_TRAUMA = "mainframe_death_trauma";
    private static final String WEAR_PREFIX = "mainframe_cyberware_wear_";

    private ChromewareBridge() {}

    public static void tick(Player player, int versatilityPoints) {
        if (player.level().isClientSide()) return;
        try {
            if (player.tickCount % 20 == 0) syncVersatility(player, versatilityPoints);
            if (player.tickCount % 200 == 0) applyCyberwareWear(player);
            if (player.tickCount % 20 == 0) recoverDeathTrauma(player);
            if (player.tickCount % 100 == 0) cyberwareMalfunction(player);
        } catch (Throwable ignored) {
            // Optional compatibility must never break the server player tick.
        }
    }

    /**
     * Versatility now affects capacity only. It deliberately does not alter implant
     * cooldowns, active timers, or Chromeware's native ability state.
     */
    public static void syncVersatility(Player player, int points) {
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
        } catch (Throwable ignored) {
            // Chromeware is optional; a changed API simply disables this bridge path.
        }
    }

    public static void onDeath(Player player, net.minecraft.world.damagesource.DamageSource source) {
        if (player.level().isClientSide() || !hasCyberware(player)) return;
        double pressure = deathPressure(player, source);
        CompoundTag data = player.getPersistentData();
        double trauma = Math.min(0.10D, data.getDouble(DEATH_TRAUMA) + pressure);
        data.putDouble(DEATH_TRAUMA, trauma);
        addCyberpsychosisStrain(player, pressure);
        damageCyberwareOnDeath(player);
    }

    private static double deathPressure(Player victim, net.minecraft.world.damagesource.DamageSource source) {
        net.minecraft.world.entity.Entity attacker = source.getEntity();
        net.minecraft.world.entity.Entity direct = source.getDirectEntity();
        Player killer = resolvePlayerAttacker(attacker, direct);

        if (killer != null) return hasCyberware(killer) ? 0.08D : 0.06D;
        if (attacker instanceof net.minecraft.world.entity.LivingEntity
                || direct instanceof net.minecraft.world.entity.LivingEntity) return 0.03D;
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

    private static void addCyberpsychosisStrain(Player player, double amount) {
        try {
            Class<?> humanity = Class.forName("com.chromeware.humanity.Humanity");
            // Chromeware 1.2.x exposes the native strain path; use it first so its
            // own cyberpsychosis thresholds and behavior remain authoritative.
            humanity.getMethod("strain", Player.class, double.class).invoke(null, player, amount);
            return;
        } catch (Throwable ignored) {}

        try {
            Class<?> humanity = Class.forName("com.chromeware.humanity.Humanity");
            Object state = humanity.getMethod("state", Player.class).invoke(null, player);
            double permanent = ((Number)state.getClass().getMethod("permanent").invoke(state)).doubleValue();
            double temporary = ((Number)state.getClass().getMethod("temporary").invoke(state)).doubleValue();
            Object next = state.getClass().getMethod("with", double.class, double.class)
                    .invoke(state, permanent + amount, temporary);
            Supplier<?> supplier = (Supplier<?>)humanity.getField("STATE").get(null);
            player.setData((Supplier)supplier, next);
            humanity.getMethod("sync", Player.class, boolean.class).invoke(null, player, true);
        } catch (Throwable ignored) {}
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
            Object next = state.getClass().getMethod("with", double.class, double.class)
                    .invoke(state, Math.max(0.0D, permanent - step), temporary);
            Supplier<?> supplier = (Supplier<?>)humanity.getField("STATE").get(null);
            player.setData((Supplier)supplier, next);
            humanity.getMethod("sync", Player.class, boolean.class).invoke(null, player, true);
            data.putDouble(DEATH_TRAUMA, trauma - step);
        } catch (Throwable ignored) {
            // If this Chromeware version does not expose recoverable state, keep the
            // trauma counter rather than silently reducing it without healing strain.
        }
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
        } catch (Throwable ignored) {
            return 0.0D;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<ItemStack> installedCyberware(Player player) {
        try {
            Class<?> capacity = Class.forName("com.chromeware.cyberware.CyberwareCapacity");
            Object installed = capacity.getMethod("installed", Player.class).invoke(null, player);
            return installed instanceof List<?> list ? (List<ItemStack>)list : List.of();
        } catch (Throwable ignored) {
            return List.of();
        }
    }

    private static int capacity(ItemStack stack) {
        try {
            Class<?> stats = Class.forName("com.chromeware.cyberware.CyberwareStats");
            Object result = stats.getMethod("get", ItemStack.class).invoke(null, stack);
            return ((Number)result.getClass().getMethod("capacity").invoke(result)).intValue();
        } catch (Throwable ignored) {
            return 1;
        }
    }

    private static void addWear(Player player, ItemStack stack, double amount) {
        if (stack.isEmpty() || amount <= 0.0D) return;
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        double current = custom.getDouble("mainframe_cyberware_wear");
        if (!custom.contains("mainframe_cyberware_wear_id")) {
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

        double chance = Math.min(0.35D,
                (worst - 0.75D) * 0.35D + Math.max(0.0D, loss - 70.0D) * 0.004D);
        if (player.getRandom().nextDouble() >= chance) return;

        int ticks = player.getRandom().nextIntBetweenInclusive(30, 70);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 0, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 0, false, true));
        if (player.getRandom().nextBoolean()) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, Math.min(80, ticks), 0, false, true));
        }
    }

    public static double wear(Player player, ItemStack stack) {
        if (stack.isEmpty()) return 0.0D;
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (custom.contains("mainframe_cyberware_wear_id")) {
            return Math.max(0.0D, Math.min(1.0D, custom.getDouble("mainframe_cyberware_wear")));
        }
        return Math.max(0.0D, Math.min(1.0D,
                player.getPersistentData().getDouble(legacyWearKey(stack))));
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

    /** Repairs every installed implant by the supplied normalized amount and returns the count repaired. */
    public static int repairAll(Player player, double amount) {
        if (!Double.isFinite(amount) || amount <= 0.0D) return 0;
        int repaired = 0;
        for (ItemStack stack : installedCyberware(player)) {
            double before = wear(player, stack);
            if (before <= 0.0D) continue;
            double costFactor = Math.sqrt(20.0D / Math.max(1, capacity(stack)));
            repairWear(player, stack, Math.min(1.0D, amount) * costFactor);
            if (wear(player, stack) < before) repaired++;
        }
        return repaired;
    }

    public static boolean isImplantDamaged(Player player, Object implant) {
        try {
            Field id = implant.getClass().getField("id");
            String implantId = String.valueOf(id.get(implant));
            for (ItemStack stack : installedCyberware(player)) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (itemId != null && itemId.getPath().equals(implantId)) {
                    return wear(player, stack) >= 1.0D;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
