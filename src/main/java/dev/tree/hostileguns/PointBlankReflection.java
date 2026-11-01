package dev.tree.hostileguns;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public final class PointBlankReflection {
    private static final String GUN = "com.vicmatskiv.pointblank.item.GunItem";
    private static final String FIREMODE = "com.vicmatskiv.pointblank.feature.FireModeFeature";
    private static final String HITSCAN = "com.vicmatskiv.pointblank.util.HitScan";

    private PointBlankReflection() {}

    private static Class<?> gunClass() throws ClassNotFoundException { return Class.forName(GUN); }

    public static boolean isGun(Item item) {
        try { return gunClass().isInstance(item); }
        catch (Throwable ignored) { return false; }
    }

    public static Object fireMode(ItemStack stack) {
        try { return gunClass().getMethod("getFireModeInstance", ItemStack.class).invoke(null, stack); }
        catch (Throwable ignored) { return null; }
    }

    public static int ammo(ItemStack stack, Object fireMode) {
        if (fireMode == null) return 0;
        try {
            for (Method m : gunClass().getMethods()) {
                if (m.getName().equals("getAmmo") && m.getParameterCount() == 2) return ((Number)m.invoke(null, stack, fireMode)).intValue();
            }
        } catch (Throwable ignored) {}
        return 0;
    }

    public static void setAmmo(ItemStack stack, Object fireMode, int ammo) {
        if (fireMode == null) return;
        try {
            for (Method m : gunClass().getMethods()) {
                if (m.getName().equals("setAmmo") && m.getParameterCount() == 3) {
                    m.invoke(null, stack, fireMode, ammo);
                    return;
                }
            }
        } catch (Throwable ignored) {}
    }

    public static int maxAmmo(ItemStack stack, Object fireMode) {
        if (fireMode == null) return 0;
        try {
            for (Method m : gunClass().getMethods()) {
                if (m.getName().equals("getMaxAmmoCapacity") && m.getParameterCount() == 2)
                    return ((Number)m.invoke(stack.getItem(), stack, fireMode)).intValue();
            }
        } catch (Throwable ignored) {}
        return 0;
    }

    public static int burstShots(ItemStack stack, Object fireMode) {
        if (fireMode == null) return -1;
        try { return ((Number)fireMode.getClass().getMethod("getBurstShots").invoke(fireMode)).intValue(); }
        catch (Throwable ignored) { return -1; }
    }

    private static boolean hasProjectile(Object ammo) {
        try { return (Boolean)ammo.getClass().getMethod("isHasProjectile").invoke(ammo); }
        catch (Throwable ignored) { return false; }
    }

    private static Object firstProjectileAmmo(ItemStack stack, Object fireMode) {
        try {
            boolean defaultPool = (Boolean)fireMode.getClass().getMethod("isUsingDefaultAmmoPool").invoke(fireMode);
            if (!defaultPool) return fireMode.getClass().getMethod("getAmmo").invoke(fireMode);
            List<?> ammo = (List<?>)stack.getItem().getClass().getMethod("getCompatibleAmmo").invoke(stack.getItem());
            for (Object entry : ammo) if (hasProjectile(entry)) return entry;
        } catch (Throwable ignored) {}
        return null;
    }

    public static boolean fire(LivingEntity shooter, LivingEntity target, ItemStack stack, double spread) {
        try {
            if (!isGun(stack.getItem())) return false;
            Object mode = fireMode(stack);
            int current = ammo(stack, mode);
            int capacity = maxAmmo(stack, mode);
            if (mode == null || current <= 0 || capacity <= 0) return false;

            Vec3 eye = shooter.getEyePosition();
            Vec3 direction = target == null ? shooter.getViewVector(0.0F) : target.getEyePosition().subtract(eye).normalize();
            direction = addSpread(shooter, direction, spread);
            Vec3 muzzle = eye.add(0.0D, -0.08D, 0.48D);

            Object projectileAmmo = firstProjectileAmmo(stack, mode);
            boolean fired;
            if (projectileAmmo != null) {
                Object projectile = projectileAmmo.getClass()
                        .getMethod("createProjectile", LivingEntity.class, double.class, double.class, double.class)
                        .invoke(projectileAmmo, shooter, muzzle.x, muzzle.y, muzzle.z);
                projectile.getClass().getMethod("launchAtLookTarget", LivingEntity.class, double.class, long.class)
                        .invoke(projectile, shooter, spread, shooter.getRandom().nextLong());
                fired = projectile instanceof net.minecraft.world.entity.Entity entity
                        && shooter.level() instanceof ServerLevel level
                        && level.addFreshEntity(entity);
            } else {
                fired = fireHitscan(shooter, stack, spread, direction);
            }

            // Never consume ammunition or report a shot when Point Blank's firing
            // pipeline failed. HostileGunEvents can then use its server projectile fallback.
            if (!fired) return false;
            if (capacity < Integer.MAX_VALUE) setAmmo(stack, mode, current - 1);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Server-safe Point Blank hitscan. Do not resolve GunClientState by name:
     * it lives in Point Blank's client package and is absent on dedicated servers.
     * The fire-mode helper accepts a null client state for NPC firing.
     */
    private static boolean fireHitscan(LivingEntity shooter, ItemStack stack, double spread, Vec3 direction) {
        try {
            Class<?> feature = Class.forName(FIREMODE);
            Method pelletMethod = null;
            for (Method method : feature.getMethods()) {
                Class<?>[] params = method.getParameterTypes();
                if (method.getName().equals("getPelletCountAndSpread")
                        && params.length == 3
                        && LivingEntity.class.isAssignableFrom(params[0])
                        && params[2].isAssignableFrom(ItemStack.class)) {
                    pelletMethod = method;
                    break;
                }
            }
            if (pelletMethod == null) return false;

            Object pcs = pelletMethod.invoke(null, shooter, null, stack);
            if (pcs == null) return false;
            int pellets = Math.max(1, ((Number)pcs.getClass().getMethod("getFirst").invoke(pcs)).intValue());
            double pelletSpread = ((Number)pcs.getClass().getMethod("getSecond").invoke(pcs)).doubleValue();
            int range = ((Number)feature.getMethod("getMaxShootingDistance", ItemStack.class).invoke(null, stack)).intValue();

            Method ray = Class.forName(HITSCAN).getMethod("getObjectsInCrosshair",
                    LivingEntity.class, Vec3.class, Vec3.class, float.class, double.class, int.class, double.class,
                    long.class, Predicate.class, Predicate.class, List.class);
            List<?> hits = (List<?>)ray.invoke(null, shooter, shooter.getEyePosition(), direction,
                    0.0F, Math.min(100, range), pellets, pelletSpread + spread,
                    shooter.getRandom().nextLong(), (Predicate<Object>)b -> false, (Predicate<Object>)b -> false,
                    new java.util.ArrayList<>());
            if (hits == null) return false;

            Method hurt = null;
            for (Method method : stack.getItem().getClass().getMethods()) {
                if (!method.getName().equals("hurtEntity") || method.getParameterCount() != 4) continue;
                Class<?>[] params = method.getParameterTypes();
                if (params[0].isAssignableFrom(shooter.getClass())
                        && params[1].isAssignableFrom(EntityHitResult.class)
                        && params[3].isAssignableFrom(ItemStack.class)) {
                    hurt = method;
                    break;
                }
            }

            for (Object hit : hits) {
                if (!(hit instanceof EntityHitResult entityHit)) continue;
                if (hurt == null) return false;
                hurt.invoke(stack.getItem(), shooter, entityHit, null, stack);
            }
            // A valid ray query is a fired hitscan even when it finds no entity.
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Vec3 addSpread(LivingEntity shooter, Vec3 direction, double spread) {
        if (spread <= 0.0D) return direction.normalize();
        return direction.add(
                shooter.getRandom().triangle(0.0D, spread),
                shooter.getRandom().triangle(0.0D, spread),
                shooter.getRandom().triangle(0.0D, spread)).normalize();
    }

    public static int maxShootingDistance(ItemStack stack) {
        try {
            Class<?> feature = Class.forName(FIREMODE);
            return ((Number)feature.getMethod("getMaxShootingDistance", ItemStack.class).invoke(null, stack)).intValue();
        } catch (Throwable ignored) { return 100; }
    }

    public static SoundEvent fireSound(Item item) {
        try {
            Object result = item.getClass().getMethod("getFireSound").invoke(item);
            return result instanceof SoundEvent sound ? sound : null;
        } catch (Throwable ignored) { return null; }
    }

    public static float fireSoundVolume(Item item) {
        try {
            Object result = item.getClass().getMethod("getFireSoundVolume").invoke(item);
            return result instanceof Number n ? Math.max(0.1F, n.floatValue()) : 1.0F;
        } catch (Throwable ignored) { return 1.0F; }
    }

    public static int rpm(Item item) {
        for (Class<?> type = item.getClass(); type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                String name = method.getName().toLowerCase(Locale.ROOT);
                if (method.getParameterCount() != 0 ||
                        !(name.equals("getrpm") || name.equals("rpm") || name.equals("getfirerate") || name.equals("firerate"))) continue;
                try { method.setAccessible(true); Object value = method.invoke(item); if (value instanceof Number n && n.intValue() > 0) return n.intValue(); }
                catch (Throwable ignored) {}
            }
            for (Field field : type.getDeclaredFields()) {
                String name = field.getName().toLowerCase(Locale.ROOT);
                if (!(name.equals("rpm") || name.equals("firerate") || name.equals("fire_rate"))) continue;
                try { field.setAccessible(true); Object value = field.get(item); if (value instanceof Number n && n.intValue() > 0) return n.intValue(); }
                catch (Throwable ignored) {}
            }
        }
        return -1;
    }
}
