package dev.tree.hostileguns;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class HostileBulletEntity extends Snowball {
    private static final int MAX_LIFETIME = 80;
    private float shotDamage = 4.0F;

    public HostileBulletEntity(EntityType<? extends HostileBulletEntity> type, Level level) {
        super(type, level);
        setItem(new ItemStack(Items.IRON_NUGGET));
        setNoGravity(true);
    }

    public HostileBulletEntity(Level level, LivingEntity shooter, Vec3 position,
                               Vec3 velocity, float damage) {
        this(ModEntities.HOSTILE_BULLET.get(), level);
        setOwner(shooter);
        setPos(position.x, position.y, position.z);
        setDeltaMovement(velocity);
        shotDamage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.IRON_NUGGET;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide && !isRemoved() && HostileGunsConfig.tracerEffects && !isInvisible()) {
            level().addParticle(ParticleTypes.CRIT, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
            if (tickCount % 2 == 0) {
                level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

        if (tickCount > MAX_LIFETIME) discard();
    }

    @Override
    protected void onHit(HitResult result) {
        if (!level().isClientSide) {
            if (result instanceof EntityHitResult entityHit) {
                Entity target = entityHit.getEntity();
                if (target instanceof LivingEntity living
                        && getOwner() instanceof LivingEntity shooter
                        && !shooter.isAlliedTo(living)) {
                    living.hurt(level().damageSources().mobAttack(shooter), shotDamage);
                }
            }

            if (HostileGunsConfig.impactEffects && level() instanceof ServerLevel server) {
                Vec3 point = result.getLocation();
                server.sendParticles(ParticleTypes.CRIT, point.x, point.y, point.z,
                        5, 0.12D, 0.12D, 0.12D, 0.03D);
                server.sendParticles(ParticleTypes.SMOKE, point.x, point.y, point.z,
                        2, 0.08D, 0.08D, 0.08D, 0.01D);
            }
        }
        discard();
    }
}
