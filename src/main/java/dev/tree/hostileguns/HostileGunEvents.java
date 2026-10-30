package dev.tree.hostileguns;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
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
    private static final String COOLDOWN = "hostileguns_cooldown";
    private static final String AIM_TARGET = "hostileguns_aim_target";
    private static final String AIM_DELAY = "hostileguns_aim_delay";
    private static final String AIM_SKILL = "hostileguns_aim_skill";

    private HostileGunEvents() {}

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

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

        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(gun));
        mob.setDropChance(EquipmentSlot.MAINHAND, 0.05F);
        data.putBoolean(ARMED, true);
        data.putInt(COOLDOWN, mob.getRandom().nextIntBetweenInclusive(6, 16));
        data.putDouble(AIM_SKILL, 0.78D + mob.getRandom().nextDouble() * 0.60D);
        data.putInt(AIM_TARGET, -1);
        data.putInt(AIM_DELAY, 0);
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) return;

        var data = mob.getPersistentData();
        if (!data.getBoolean(ARMED) || !mob.isAlive()) return;

        ItemStack held = mob.getMainHandItem();
        if (held.isEmpty() || !isPointBlankGun(held.getItem())) {
            data.putBoolean(ARMED, false);
            return;
        }

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive() || target == mob) {
            data.putInt(AIM_TARGET, -1);
            data.putInt(AIM_DELAY, 0);
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
            return;
        }

        int aimDelay = data.getInt(AIM_DELAY);
        if (aimDelay > 0) {
            data.putInt(AIM_DELAY, aimDelay - 1);
            return;
        }

        int cooldown = data.getInt(COOLDOWN);
        if (cooldown > 0) {
            data.putInt(COOLDOWN, cooldown - 1);
            return;
        }

        fireWeapon(mob, target, held.getItem(), distance);
    }

    private static void fireWeapon(Mob mob, LivingEntity target, Item gun, double distance) {
        if (!(mob.level() instanceof ServerLevel level)) return;

        WeaponBalance.Profile profile = WeaponBalance.profile(gun);
        Vec3 eye = mob.getEyePosition();
        Vec3 desired = target.getEyePosition().subtract(eye).normalize();

        var data = mob.getPersistentData();
        double mobSkill = data.contains(AIM_SKILL) ? data.getDouble(AIM_SKILL) : 1.1D;
        double distanceFactor = 0.72D + Math.min(1.0D, distance / HostileGunsConfig.maxRange) * 0.72D;
        double randomFactor = 0.86D + mob.getRandom().nextDouble() * 0.28D;
        double spreadRadians = Math.toRadians(profile.spreadDegrees()
                * distanceFactor * mobSkill * HostileGunsConfig.accuracyMultiplier * randomFactor);
        double spread = Math.tan(spreadRadians);

        Vec3 muzzleDirection = addSpread(desired, mob, spread);
        Vec3 muzzle = eye.add(0.0D, -0.08D, 0.0D).add(muzzleDirection.scale(0.48D));

        if (HostileGunsConfig.muzzleFlashEffects) {
            level.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z,
                    3, 0.025D, 0.025D, 0.025D, 0.006D);
            level.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z,
                    1, 0.018D, 0.018D, 0.018D, 0.004D);
        }

        SoundEvent fireSound = PointBlankReflection.fireSound(gun);
        if (fireSound != null) {
            level.playSound(null, muzzle.x, muzzle.y, muzzle.z, fireSound, SoundSource.HOSTILE,
                    Math.max(0.5F, PointBlankReflection.fireSoundVolume(gun)),
                    0.94F + mob.getRandom().nextFloat() * 0.12F);
        } else {
            level.playSound(null, muzzle.x, muzzle.y, muzzle.z, SoundEvents.CROSSBOW_SHOOT,
                    SoundSource.HOSTILE, 0.75F, 0.90F + mob.getRandom().nextFloat() * 0.20F);
        }

        int pellets = profile.pellets();
        float pelletDamage = HostileGunsConfig.baseDamage * profile.damageMultiplier()
                / (pellets > 1 ? 2.4F : 1.0F);

        for (int i = 0; i < pellets; i++) {
            double pelletSpread = pellets > 1 ? spread * 1.35D : spread;
            Vec3 direction = addSpread(desired, mob, pelletSpread);
            Vec3 origin = eye.add(0.0D, -0.08D, 0.0D).add(direction.scale(0.48D));
            HostileBulletEntity bullet = new HostileBulletEntity(level, mob, origin,
                    direction.scale(profile.projectileSpeed()), pelletDamage);
            level.addFreshEntity(bullet);
        }

        mob.swing(InteractionHand.MAIN_HAND);
        data.putInt(COOLDOWN, chooseCooldown(mob, gun, profile));
    }

    private static Vec3 addSpread(Vec3 desired, Mob mob, double spread) {
        Vec3 offset = new Vec3(
                mob.getRandom().triangle(0.0D, spread),
                mob.getRandom().triangle(0.0D, spread),
                mob.getRandom().triangle(0.0D, spread)
        );
        return desired.add(offset).normalize();
    }

    private static int chooseCooldown(Mob mob, Item gun, WeaponBalance.Profile profile) {
        int min = profile.minCooldown();
        int max = profile.maxCooldown();

        int rpm = PointBlankReflection.rpm(gun);
        if (rpm > 0) {
            int idealTicks = (int) Math.round(1200.0D / rpm);
            idealTicks = Math.max(3, Math.min(50, idealTicks));
            min = Math.max(3, (int) Math.round(idealTicks * 0.82D));
            max = Math.max(min, (int) Math.round(idealTicks * 1.28D));
        }

        int chosen = mob.getRandom().nextIntBetweenInclusive(min, max);
        if (mob.getRandom().nextFloat() < 0.08F) {
            chosen += mob.getRandom().nextIntBetweenInclusive(2, 7);
        }
        return chosen;
    }

    private static boolean eligibleType(Mob mob, ResourceLocation id) {
        if (HostileGunsConfig.excludedEntities.contains(id.toString())) return false;
        if (!HostileGunsConfig.allowModdedHostiles && !"minecraft".equals(id.getNamespace())) return false;
        return mob.getType().getCategory() == MobCategory.MONSTER || mob instanceof Monster;
    }

    private static boolean isPointBlankGun(Item item) {
        try {
            Class<?> gunClass = Class.forName("com.vicmatskiv.pointblank.item.GunItem", false,
                    PointBlankGunPool.class.getClassLoader());
            return gunClass.isInstance(item);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
