package dev.tree.hostileguns;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class HostileGunEvents {
    private static final String ROLLED = "hostileguns_rolled";
    private static final String ARMED = "hostileguns_armed";
    private static final String GUN_ID = "hostileguns_gun_id";
    private static final String COOLDOWN = "hostileguns_cooldown";
    private static final String AIM_TARGET = "hostileguns_aim_target";
    private static final String AIM_DELAY = "hostileguns_aim_delay";
    private static final String AIM_SKILL = "hostileguns_aim_skill";
    private static final String RELOAD_TICKS = "hostileguns_reload_ticks";
    private static final String BURST_LEFT = "hostileguns_burst_left";
    private static final String BURST_COOLDOWN = "hostileguns_burst_cooldown";
    private static final String FALLBACK_AMMO = "hostileguns_fallback_ammo";
    private static final String FALLBACK_AMMO_MAX = "hostileguns_fallback_ammo_max";

    private HostileGunEvents() {}

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) return;
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (entityId == null || !eligibleType(mob, entityId)) return;
        tryArm(mob);
    }

    private static void tryArm(Mob mob) {
        var data = mob.getPersistentData();
        if (data.getBoolean(ROLLED)) return;

        Item gun = PointBlankGunPool.chooseWeighted(mob.getRandom());
        if (gun == null) {
            PointBlankGunPool.refresh();
            gun = PointBlankGunPool.chooseWeighted(mob.getRandom());
        }
        // Point Blank's registry can finish loading after an entity join callback.
        // Keep the mob eligible and retry on its server ticks instead of permanently
        // marking it as rolled with an empty gun pool.
        if (gun == null) return;

        data.putBoolean(ROLLED, true);
        if (mob.getRandom().nextDouble() >= HostileGunsConfig.armedHostileChance) return;

        equip(mob, gun);
        ResourceLocation gunId = BuiltInRegistries.ITEM.getKey(gun);
        if (gunId != null) data.putString(GUN_ID, gunId.toString());
        data.putBoolean(ARMED, true);
        data.putInt(COOLDOWN, mob.getRandom().nextIntBetweenInclusive(6, 16));
        data.putDouble(AIM_SKILL, 0.78D + mob.getRandom().nextDouble() * 0.60D);
        data.putInt(AIM_TARGET, -1);
        data.putInt(AIM_DELAY, 0);
        data.putInt(RELOAD_TICKS, 0);
        data.putInt(BURST_LEFT, 0);
        data.putInt(BURST_COOLDOWN, 0);
        ensureMagazine(mob, mob.getMainHandItem());
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) return;
        var data = mob.getPersistentData();
        if (!mob.isAlive()) return;
        if (!data.getBoolean(ARMED)) {
            if (!data.getBoolean(ROLLED)) tryArm(mob);
            return;
        }

        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        ItemStack held = mob.getMainHandItem();

        if (held.isEmpty() || !PointBlankReflection.isGun(held.getItem())) {
            data.putBoolean(ARMED, false);
            return;
        }

        // Do not auto-fill an empty magazine here. Initialization happens on equip;
        // subsequent empty magazines must pass through the reload state machine.
        int reload = data.getInt(RELOAD_TICKS);
        if (reload > 0) {
            data.putInt(RELOAD_TICKS, reload - 1);
            if (reload == 1) {
                reloadMagazine(mob, held);
            }
            return;
        }

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive() || target == mob) {
            target = acquirePlayerTarget(mob);
            if (target != null) mob.setTarget(target);
        }
        if (target == null || !target.isAlive() || target == mob) {
            data.putInt(AIM_TARGET, -1);
            data.putInt(AIM_DELAY, 0);
            data.putInt(BURST_LEFT, 0);
            return;
        }

        double distance = mob.distanceTo(target);
        if (distance > HostileGunsConfig.maxRange || distance < 2.0D) return;
        if (!mob.getSensing().hasLineOfSight(target)) {
            data.putInt(AIM_TARGET, -1);
            data.putInt(AIM_DELAY, 0);
            return;
        }

        // Keep the body, head and view vector aligned. LookControl alone only turns
        // the head on many mobs, which leaves the weapon firing along the old body yaw.
        Vec3 aim = target.getEyePosition().subtract(mob.getEyePosition());
        double horizontalAim = Math.sqrt(aim.x * aim.x + aim.z * aim.z);
        float aimYaw = (float)(Math.toDegrees(Math.atan2(-aim.x, aim.z)));
        float aimPitch = (float)(-Math.toDegrees(Math.atan2(aim.y, horizontalAim)));
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        mob.setYRot(aimYaw);
        mob.yBodyRot = aimYaw;
        mob.yHeadRot = aimYaw;
        mob.setXRot(aimPitch);

        if (data.getInt(AIM_TARGET) != target.getId()) {
            data.putInt(AIM_TARGET, target.getId());
            data.putInt(AIM_DELAY, mob.getRandom().nextIntBetweenInclusive(
                    HostileGunsConfig.minAimDelayTicks, HostileGunsConfig.maxAimDelayTicks));
            data.putInt(BURST_LEFT, 0);
            return;
        }

        int aimDelay = data.getInt(AIM_DELAY);
        if (aimDelay > 0) {
            data.putInt(AIM_DELAY, aimDelay - 1);
            return;
        }

        int burstCooldown = data.getInt(BURST_COOLDOWN);
        if (burstCooldown > 0) {
            data.putInt(BURST_COOLDOWN, burstCooldown - 1);
            return;
        }

        int burstLeft = data.getInt(BURST_LEFT);
        if (burstLeft > 0) {
            Object burstMode = PointBlankReflection.fireMode(held);
            if (getAmmo(mob, held, burstMode) <= 0) {
                beginReload(mob, held);
                return;
            }
            if (fireWeapon(mob, target, held, distance)) {
                data.putInt(BURST_LEFT, burstLeft - 1);
                data.putInt(BURST_COOLDOWN, Math.max(1, chooseCooldown(mob, held.getItem())));
            }
            return;
        }

        int cooldown = data.getInt(COOLDOWN);
        if (cooldown > 0) {
            data.putInt(COOLDOWN, cooldown - 1);
            return;
        }

        Object mode = PointBlankReflection.fireMode(held);
        int ammo = getAmmo(mob, held, mode);
        if (ammo <= 0) {
            beginReload(mob, held);
            return;
        }

        if (fireWeapon(mob, target, held, distance)) {
            int burst = PointBlankReflection.burstShots(held, mode);
            if (burst > 1) {
                data.putInt(BURST_LEFT, burst - 1);
                data.putInt(BURST_COOLDOWN, Math.max(1, chooseCooldown(mob, held.getItem())));
            } else {
                data.putInt(COOLDOWN, chooseCooldown(mob, held.getItem()));
            }
        }
    }

    private static LivingEntity acquirePlayerTarget(Mob mob) {
        if (!(mob instanceof Monster) && mob.getType().getCategory() != MobCategory.MONSTER) return null;
        AABB search = mob.getBoundingBox().inflate(HostileGunsConfig.maxRange);
        return mob.level().getEntitiesOfClass(Player.class, search,
                        player -> player.isAlive() && !player.isSpectator() && !player.isCreative()
                                && !mob.isAlliedTo(player) && mob.getSensing().hasLineOfSight(player))
                .stream()
                .min(Comparator.comparingDouble(mob::distanceToSqr))
                .orElse(null);
    }

    private static void ensureMagazine(Mob mob, ItemStack stack) {
        if (stack.isEmpty() || !PointBlankReflection.isGun(stack.getItem())) return;
        Object mode = PointBlankReflection.fireMode(stack);
        int capacity = mode == null ? 0 : PointBlankReflection.maxAmmo(stack, mode);
        if (capacity > 0) {
            if (PointBlankReflection.ammo(stack, mode) <= 0) PointBlankReflection.setAmmo(stack, mode, capacity);
            return;
        }
        int fallbackCapacity = fallbackMagazineSize(stack.getItem());
        var data = mob.getPersistentData();
        if (data.getInt(FALLBACK_AMMO_MAX) != fallbackCapacity) {
            data.putInt(FALLBACK_AMMO_MAX, fallbackCapacity);
            data.putInt(FALLBACK_AMMO, fallbackCapacity);
        } else if (data.getInt(FALLBACK_AMMO) <= 0) {
            data.putInt(FALLBACK_AMMO, fallbackCapacity);
        }
    }

    private static int getAmmo(Mob mob, ItemStack stack, Object mode) {
        int capacity = mode == null ? 0 : PointBlankReflection.maxAmmo(stack, mode);
        return capacity > 0 ? PointBlankReflection.ammo(stack, mode)
                : mob.getPersistentData().getInt(FALLBACK_AMMO);
    }

    private static void consumeAmmo(Mob mob, ItemStack stack, Object mode, int amount) {
        int capacity = mode == null ? 0 : PointBlankReflection.maxAmmo(stack, mode);
        if (capacity > 0) {
            PointBlankReflection.setAmmo(stack, mode,
                    Math.max(0, PointBlankReflection.ammo(stack, mode) - amount));
        } else {
            var data = mob.getPersistentData();
            data.putInt(FALLBACK_AMMO, Math.max(0, data.getInt(FALLBACK_AMMO) - amount));
        }
    }

    private static int fallbackMagazineSize(Item gun) {
        return switch (WeaponBalance.profile(gun).category()) {
            case PISTOL -> 12;
            case SMG -> 30;
            case RIFLE -> 30;
            case SHOTGUN -> 8;
            case SNIPER -> 5;
            case LMG -> 60;
            case HEAVY -> 6;
            case UNKNOWN -> 20;
        };
    }

    private static void beginReload(Mob mob, ItemStack stack) {
        var data = mob.getPersistentData();
        if (data.getInt(RELOAD_TICKS) > 0) return;
        int rpm = PointBlankReflection.rpm(stack.getItem());
        int base = rpm > 0 ? Math.max(20, Math.min(50, (int)Math.round(1200.0D / rpm))) : 30;
        data.putInt(RELOAD_TICKS, base + mob.getRandom().nextIntBetweenInclusive(10, 24));
        data.putInt(BURST_LEFT, 0);
        data.putInt(BURST_COOLDOWN, 0);
    }

    private static void reloadMagazine(Mob mob, ItemStack stack) {
        Object mode = PointBlankReflection.fireMode(stack);
        int capacity = mode == null ? 0 : PointBlankReflection.maxAmmo(stack, mode);
        if (capacity > 0) {
            PointBlankReflection.setAmmo(stack, mode, capacity);
        } else {
            int fallbackCapacity = fallbackMagazineSize(stack.getItem());
            mob.getPersistentData().putInt(FALLBACK_AMMO_MAX, fallbackCapacity);
            mob.getPersistentData().putInt(FALLBACK_AMMO, fallbackCapacity);
        }
        mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        mob.getPersistentData().putInt(COOLDOWN, 3);
    }

    private static void equip(Mob mob, Item gun) {
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(gun));
        mob.setDropChance(EquipmentSlot.MAINHAND, 0.05F);
    }

    private static boolean fireWeapon(Mob mob, LivingEntity target, ItemStack stack, double distance) {
        WeaponBalance.Profile profile = WeaponBalance.profile(stack.getItem());
        double distanceFactor = 0.72D + Math.min(1.0D, distance / HostileGunsConfig.maxRange) * 0.72D;
        double randomFactor = 0.86D + mob.getRandom().nextDouble() * 0.28D;
        double spreadRadians = Math.toRadians(profile.spreadDegrees() * distanceFactor
                * (dataDouble(mob, AIM_SKILL, 1.0D)) * HostileGunsConfig.accuracyMultiplier * randomFactor);
        double spread = Math.tan(spreadRadians);

        boolean pointBlankFired = PointBlankReflection.fire(mob, target, stack, spread);
        if (!pointBlankFired && !fireFallbackProjectile(mob, target, stack, spread, profile)) {
            return false;
        }

        mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        ServerLevel level = (ServerLevel)mob.level();
        Vec3 muzzle = mob.getEyePosition().add(mob.getLookAngle().scale(0.5D));
        SoundEvent fireSound = PointBlankReflection.fireSound(stack.getItem());
        if (fireSound != null) {
            level.playSound(null, muzzle.x, muzzle.y, muzzle.z, fireSound, SoundSource.HOSTILE,
                    PointBlankReflection.fireSoundVolume(stack.getItem()),
                    0.94F + mob.getRandom().nextFloat() * 0.12F);
        }
        return true;
    }

    /**
     * Last-resort server-authoritative shot. Point Blank's projectile/hitscan APIs
     * differ between weapon implementations; if their reflective entry point is
     * unavailable, do not silently turn an armed mob into a non-firing mob.
     */
    private static boolean fireFallbackProjectile(Mob mob, LivingEntity target, ItemStack stack,
                                                  double spread, WeaponBalance.Profile profile) {
        if (!(mob.level() instanceof ServerLevel level)) return false;
        Object mode = PointBlankReflection.fireMode(stack);
        int ammo = getAmmo(mob, stack, mode);
        if (ammo <= 0) return false;

        Vec3 direction = target.getEyePosition().subtract(mob.getEyePosition()).normalize();
        direction = direction.add(
                mob.getRandom().triangle(0.0D, spread),
                mob.getRandom().triangle(0.0D, spread),
                mob.getRandom().triangle(0.0D, spread)).normalize();
        Vec3 muzzle = mob.getEyePosition().add(direction.scale(0.45D));
        float damage = HostileGunsConfig.baseDamage * profile.damageMultiplier();
        Vec3 velocity = direction.scale(profile.projectileSpeed());
        HostileBulletEntity bullet = new HostileBulletEntity(level, mob, muzzle, velocity, damage);
        if (!level.addFreshEntity(bullet)) return false;

        consumeAmmo(mob, stack, mode, 1);
        return true;
    }

    private static double dataDouble(Mob mob, String key, double fallback) {
        return mob.getPersistentData().contains(key) ? mob.getPersistentData().getDouble(key) : fallback;
    }

    private static int chooseCooldown(Mob mob, Item gun) {
        int rpm = PointBlankReflection.rpm(gun);
        if (rpm > 0) {
            int ideal = Math.max(3, Math.min(50, (int)Math.round(1200.0D / rpm)));
            int chosen = (int)Math.round(ideal * (0.82D + mob.getRandom().nextDouble() * 0.46D));
            return Math.max(3, chosen);
        }
        return mob.getRandom().nextIntBetweenInclusive(6, 18);
    }

    private static boolean eligibleType(Mob mob, ResourceLocation id) {
        // Create: Cybernetics mobs retain their native AI and natural spawning;
        // they are deliberately excluded from this mod's gun assignment.
        if ("createcybernetics".equals(id.getNamespace())) return false;
        if (HostileGunsConfig.excludedEntities.contains(id.toString())) return false;
        if (!HostileGunsConfig.allowModdedHostiles && !"minecraft".equals(id.getNamespace())) return false;
        return mob.getType().getCategory() == MobCategory.MONSTER || mob instanceof Monster;
    }

}
