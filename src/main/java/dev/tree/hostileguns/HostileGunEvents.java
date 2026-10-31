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
        data.putBoolean(ROLLED, true);
        if (mob.getRandom().nextDouble() >= HostileGunsConfig.armedHostileChance) return;

        Item gun = PointBlankGunPool.chooseWeighted(mob.getRandom());
        if (gun == null) {
            PointBlankGunPool.refresh();
            gun = PointBlankGunPool.chooseWeighted(mob.getRandom());
        }
        if (gun == null) return;

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
        ensureMagazine(mob.getMainHandItem());
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) return;
        var data = mob.getPersistentData();
        if (!data.getBoolean(ARMED) || !mob.isAlive()) return;

        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        ItemStack held = mob.getMainHandItem();

        if (isCreateCyberneticsMob(mobId)) {
            if (held.isEmpty() || !PointBlankReflection.isGun(held.getItem())) {
                restoreCyberneticsGun(mob);
                held = mob.getMainHandItem();
            }
            if (!mob.getOffhandItem().isEmpty()) mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }

        if (held.isEmpty() || !PointBlankReflection.isGun(held.getItem())) {
            data.putBoolean(ARMED, false);
            return;
        }

        ensureMagazine(held);

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

        mob.getLookControl().setLookAt(target, 24.0F, 24.0F);

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
            if (fireWeapon(mob, target, held, distance)) {
                data.putInt(BURST_LEFT, burstLeft - 1);
                data.putInt(BURST_COOLDOWN, Math.max(1, chooseCooldown(mob, held)));
            }
            return;
        }

        int cooldown = data.getInt(COOLDOWN);
        if (cooldown > 0) {
            data.putInt(COOLDOWN, cooldown - 1);
            return;
        }

        Object mode = PointBlankReflection.fireMode(held);
        int ammo = PointBlankReflection.ammo(held, mode);
        if (ammo <= 0) {
            beginReload(mob, held);
            return;
        }

        if (fireWeapon(mob, target, held, distance)) {
            int burst = PointBlankReflection.burstShots(held, mode);
            if (burst > 1) {
                data.putInt(BURST_LEFT, burst - 1);
                data.putInt(BURST_COOLDOWN, Math.max(1, chooseCooldown(mob, held)));
            } else {
                data.putInt(COOLDOWN, chooseCooldown(mob, held));
            }
        }
    }

    private static void ensureMagazine(ItemStack stack) {
        if (stack.isEmpty() || !PointBlankReflection.isGun(stack.getItem())) return;
        Object mode = PointBlankReflection.fireMode(stack);
        if (mode == null) return;
        int capacity = PointBlankReflection.maxAmmo(stack, mode);
        if (capacity > 0 && PointBlankReflection.ammo(stack, mode) <= 0) {
            PointBlankReflection.setAmmo(stack, mode, capacity);
        }
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
        int capacity = PointBlankReflection.maxAmmo(stack, mode);
        if (capacity > 0) PointBlankReflection.setAmmo(stack, mode, capacity);
        mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        mob.getPersistentData().putInt(COOLDOWN, 3);
    }

    private static void restoreCyberneticsGun(Mob mob) {
        String id = mob.getPersistentData().getString(GUN_ID);
        if (id.isEmpty()) return;
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null) return;
        Item gun = BuiltInRegistries.ITEM.get(key);
        if (gun != null && PointBlankReflection.isGun(gun)) {
            equip(mob, gun);
            ensureMagazine(mob.getMainHandItem());
        }
    }

    private static void equip(Mob mob, Item gun) {
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(gun));
        if (isCreateCyberneticsMob(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()))) {
            mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
        mob.setDropChance(EquipmentSlot.MAINHAND, 0.05F);
    }

    private static boolean fireWeapon(Mob mob, LivingEntity target, ItemStack stack, double distance) {
        WeaponBalance.Profile profile = WeaponBalance.profile(stack.getItem());
        double distanceFactor = 0.72D + Math.min(1.0D, distance / HostileGunsConfig.maxRange) * 0.72D;
        double randomFactor = 0.86D + mob.getRandom().nextDouble() * 0.28D;
        double spreadRadians = Math.toRadians(profile.spreadDegrees() * distanceFactor
                * (dataDouble(mob, AIM_SKILL, 1.0D)) * HostileGunsConfig.accuracyMultiplier * randomFactor);
        double spread = Math.tan(spreadRadians);

        if (!PointBlankReflection.fire(mob, target, stack, spread)) return false;

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
        if (HostileGunsConfig.excludedEntities.contains(id.toString())) return false;
        if (!HostileGunsConfig.allowModdedHostiles && !"minecraft".equals(id.getNamespace())) return false;
        return mob.getType().getCategory() == MobCategory.MONSTER || mob instanceof Monster;
    }

    private static boolean isCreateCyberneticsMob(ResourceLocation id) {
        if (id == null || !"createcybernetics".equals(id.getNamespace())) return false;
        return switch (id.getPath()) {
            case "cyberzombie", "cyberskeleton", "punklin", "pigstrom", "hogboy" -> true;
            default -> false;
        };
    }
}
